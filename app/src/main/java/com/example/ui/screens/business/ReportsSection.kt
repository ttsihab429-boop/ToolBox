package com.example.ui.screens.business

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PreferencesManager
import com.example.data.business.BusinessProfile
import com.example.data.business.BusinessRepository
import com.example.data.business.BusinessTransaction
import com.example.data.business.Customer
import com.example.data.business.ProductItem
import com.example.data.business.Supplier
import com.example.data.business.report.BusinessReportCsvExporter
import com.example.data.business.report.BusinessReportFileManager
import com.example.data.business.report.BusinessReportGenerator
import com.example.data.business.report.BusinessReportJpgExporter
import com.example.data.business.report.BusinessReportPdfExporter
import com.example.data.business.report.BusinessReportSummary
import com.example.data.business.report.ReportPeriodType
import com.example.localization.AppLanguage
import com.example.localization.Strings
import com.example.ui.components.ToolActions
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.RedAccent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportsSection(
    currentBusiness: BusinessProfile,
    businesses: List<BusinessProfile>,
    businessRepository: BusinessRepository,
    language: AppLanguage,
    prefs: PreferencesManager,
    allTransactions: List<BusinessTransaction>,
    customers: List<Customer>,
    suppliers: List<Supplier>,
    products: List<ProductItem>,
    onSelectBusiness: (BusinessProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isBangla = language == AppLanguage.BANGLA
    val currency = currentBusiness.currency

    var selectedPeriodType by remember { mutableStateOf(ReportPeriodType.DAILY) }

    // Date range states for custom filter
    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    val monthStartStr = remember { SimpleDateFormat("yyyy-MM-01", Locale.US).format(Date()) }
    var customStartDate by remember { mutableStateOf(monthStartStr) }
    var customEndDate by remember { mutableStateOf(todayStr) }

    var businessDropdownExpanded by remember { mutableStateOf(false) }

    // Exporting states
    var isExporting by remember { mutableStateOf(false) }
    var exportedFiles by remember { mutableStateOf<List<File>>(emptyList()) }
    var exportedMimeType by remember { mutableStateOf("") }
    var exportedTitle by remember { mutableStateOf("") }
    var showExportActionDialog by remember { mutableStateOf(false) }
    var exportErrorMessage by remember { mutableStateOf<String?>(null) }

    // Generate current report data dynamically
    val report: BusinessReportSummary = remember(
        currentBusiness,
        selectedPeriodType,
        customStartDate,
        customEndDate,
        allTransactions,
        customers,
        suppliers,
        products
    ) {
        BusinessReportGenerator.generateReport(
            business = currentBusiness,
            periodType = selectedPeriodType,
            customStart = customStartDate,
            customEnd = customEndDate,
            allTransactions = allTransactions,
            customers = customers,
            suppliers = suppliers,
            products = products,
            repository = businessRepository
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- 1. Business & Date Period Selector Bar ---
        item {
            Spacer(modifier = Modifier.height(4.dp))

            // Business Selector Banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { businessDropdownExpanded = true }
                    .testTag("report_business_picker"),
                color = DarkSurfaceElevated,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = RedAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = currentBusiness.name,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${Strings.bizSelectBusiness(language)} • ${currentBusiness.businessType}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Business",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    DropdownMenu(
                        expanded = businessDropdownExpanded,
                        onDismissRequest = { businessDropdownExpanded = false },
                        containerColor = DarkSurfaceElevated
                    ) {
                        businesses.forEach { b ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = b.name,
                                            fontWeight = if (b.id == currentBusiness.id) FontWeight.Bold else FontWeight.Normal,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = b.businessType,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    businessDropdownExpanded = false
                                    onSelectBusiness(b)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Period Selection Tabs
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = DarkSurfaceVariant
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(
                        ReportPeriodType.DAILY to Strings.bizDailyReport(language),
                        ReportPeriodType.MONTHLY to Strings.bizMonthlyReport(language),
                        ReportPeriodType.PREV_MONTH to Strings.bizPrevMonthReport(language),
                        ReportPeriodType.CUSTOM to Strings.bizCustomRange(language)
                    ).forEach { (period, label) ->
                        val isSelected = selectedPeriodType == period
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedPeriodType = period }
                                .testTag("report_period_${period.name.lowercase()}"),
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) RedAccent else Color.Transparent
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Custom Date Range Inputs
            AnimatedVisibility(visible = selectedPeriodType == ReportPeriodType.CUSTOM) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customStartDate,
                            onValueChange = { customStartDate = it },
                            label = { Text(if (isBangla) "শুরুর তারিখ (YYYY-MM-DD)" else "Start Date") },
                            leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = RedAccent, modifier = Modifier.size(16.dp)) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RedAccent,
                                unfocusedBorderColor = DarkBorder,
                                focusedContainerColor = DarkSurfaceElevated,
                                unfocusedContainerColor = DarkSurfaceElevated
                            )
                        )

                        OutlinedTextField(
                            value = customEndDate,
                            onValueChange = { customEndDate = it },
                            label = { Text(if (isBangla) "শেষ তারিখ (YYYY-MM-DD)" else "End Date") },
                            leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = RedAccent, modifier = Modifier.size(16.dp)) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RedAccent,
                                unfocusedBorderColor = DarkBorder,
                                focusedContainerColor = DarkSurfaceElevated,
                                unfocusedContainerColor = DarkSurfaceElevated
                            )
                        )
                    }
                }
            }
        }

        // --- 2. Export Actions Buttons (PDF & CSV) ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Export PDF Button
                    Button(
                        onClick = {
                            scope.launch {
                                isExporting = true
                                val result = withContext(Dispatchers.IO) {
                                    BusinessReportPdfExporter.exportToPdf(
                                        context = context,
                                        report = report,
                                        language = language
                                    )
                                }
                                isExporting = false
                                result.onSuccess { file ->
                                    exportedFiles = listOf(file)
                                    exportedMimeType = "application/pdf"
                                    exportedTitle = "${report.business.name} PDF Report (${report.startDate} to ${report.endDate})"
                                    showExportActionDialog = true
                                    ToolActions.triggerHaptic(context, prefs.hapticEnabled.value)
                                }.onFailure { error ->
                                    exportErrorMessage = error.message ?: Strings.bizExportFailed(language)
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("report_export_pdf_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                        enabled = !isExporting
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(Strings.bizExportPdf(language), fontWeight = FontWeight.Bold)
                        }
                    }

                    // Export JPG Button
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                isExporting = true
                                val result = withContext(Dispatchers.IO) {
                                    BusinessReportJpgExporter.exportToJpg(
                                        context = context,
                                        report = report,
                                        language = language
                                    )
                                }
                                isExporting = false
                                result.onSuccess { files ->
                                    exportedFiles = files
                                    exportedMimeType = "image/jpeg"
                                    exportedTitle = "${report.business.name} JPG Report (${report.startDate} to ${report.endDate})"
                                    showExportActionDialog = true
                                    ToolActions.triggerHaptic(context, prefs.hapticEnabled.value)
                                }.onFailure { error ->
                                    exportErrorMessage = error.message ?: Strings.bizExportFailed(language)
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("report_export_jpg_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF9800)),
                        enabled = !isExporting
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(Strings.bizExportJpg(language), color = Color(0xFFFF9800), fontWeight = FontWeight.Bold)
                    }
                }

                // Date range indicator chip
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = DarkSurface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${if (isBangla) "রিপোর্ট সময়কাল:" else "Period:"} ${report.startDate} - ${report.endDate}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${report.totalTransactionsCount} ${if (isBangla) "টি এন্ট্রি" else "entries"}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // --- 3. Executive Financial Metrics Cards ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Net Profit / Net Loss Card
                val isProfit = report.netProfit >= 0
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isProfit) {
                                    if (isBangla) "নিট লাভ (Net Profit)" else "Net Profit"
                                } else {
                                    if (isBangla) "নিট ক্ষতি (Net Loss)" else "Net Loss"
                                },
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$currency ${formatMoney(Math.abs(report.netProfit))}",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isProfit) GreenSuccess else RedAccent
                            )
                            if (report.grossProfit != report.netProfit) {
                                Text(
                                    text = "${if (isBangla) "মোট লাভ (Gross Profit):" else "Gross Profit:"} $currency ${formatMoney(report.grossProfit)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (isProfit) GreenSuccess.copy(alpha = 0.15f) else RedAccent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isProfit) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = if (isProfit) GreenSuccess else RedAccent,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                // Total Sales & Total Purchases Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BusinessMetricCard(
                        modifier = Modifier.weight(1f),
                        title = if (isBangla) "মোট বিক্রি" else "Total Sales",
                        amount = "$currency ${formatMoney(report.totalSales)}",
                        icon = Icons.Default.Payments,
                        color = GreenSuccess
                    )
                    BusinessMetricCard(
                        modifier = Modifier.weight(1f),
                        title = if (isBangla) "মোট ক্রয়" else "Purchases",
                        amount = "$currency ${formatMoney(report.totalPurchases)}",
                        icon = Icons.Default.ShoppingCart,
                        color = Color(0xFFFF9800)
                    )
                }

                // Expenses & Customer Due Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BusinessMetricCard(
                        modifier = Modifier.weight(1f),
                        title = if (isBangla) "মোট দোকান খরচ" else "Expenses",
                        amount = "$currency ${formatMoney(report.totalExpenses)}",
                        icon = Icons.Default.MoneyOff,
                        color = RedAccent
                    )
                    BusinessMetricCard(
                        modifier = Modifier.weight(1f),
                        title = if (isBangla) "কাস্টমার বকেয়া বাকি" else "Customer Due",
                        amount = "$currency ${formatMoney(report.totalCustomerOutstandingDues)}",
                        icon = Icons.Default.ReceiptLong,
                        color = if (report.totalCustomerOutstandingDues > 0) RedAccent else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Cash In / Cash Out Summary
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (isBangla) "নগদ বিক্রয় জমা (Cash In)" else "Sales Cash In",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$currency ${formatMoney(report.salesCashReceived)}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = GreenSuccess
                            )
                            if (report.customerPaymentsReceived > 0) {
                                Text(
                                    text = "+ $currency ${formatMoney(report.customerPaymentsReceived)} (বাকি আদায়)",
                                    fontSize = 10.sp,
                                    color = Color(0xFF2196F3)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (isBangla) "মহাজন দেনা বাকি (Payable)" else "Supplier Payable",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$currency ${formatMoney(report.totalSupplierOutstandingPayables)}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFFF9800)
                            )
                            if (report.supplierPaymentsMade > 0) {
                                Text(
                                    text = "$currency ${formatMoney(report.supplierPaymentsMade)} (পরিশোধ)",
                                    fontSize = 10.sp,
                                    color = Color(0xFF9C27B0)
                                )
                            }
                        }
                    }
                }

                // Inventory Summary Banner
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Inventory2, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isBangla) "ইনভেন্টরি মজুদ ও মূল্যায়ন" else "Inventory Stock & Valuation",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${report.totalProductsCount} ${if (isBangla) "টি পণ্য" else "items"} (${report.lowStockCount} ${if (isBangla) "কম স্টক" else "low stock"})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = "$currency ${formatMoney(report.totalStockValuation)}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFFF9800)
                        )
                    }
                }
            }
        }

        // --- 4. Transaction Ledger Section Header ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBangla) "সময়কালের বিস্তারিত খতিয়ান" else "Ledger Entries in Period",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${report.transactions.size} Entries",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // --- 5. Transactions Table / Empty State ---
        if (report.transactions.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = DarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = Strings.bizNoTransactionsFound(language),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isBangla) "এক্সপোর্ট বাটনে চাপ দিয়ে খালি রিপোর্টও সংরক্ষণ করতে পারেন" else "You can still export a zeroed summary report",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        } else {
            items(report.transactions) { item ->
                TransactionItemRow(
                    item = item,
                    currency = currency,
                    language = language,
                    onEdit = {},
                    onDelete = {}
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // --- Export Action Dialog (Share or Save to Device) ---
    if (showExportActionDialog && exportedFiles.isNotEmpty()) {
        val isJpg = exportedMimeType == "image/jpeg"
        val count = exportedFiles.size
        AlertDialog(
            onDismissRequest = { showExportActionDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isJpg) Icons.Default.Image else Icons.Default.Description,
                        contentDescription = null,
                        tint = if (isJpg) Color(0xFFFF9800) else RedAccent
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBangla) {
                            if (isJpg) "জেপিজি রিপোর্ট তৈরি সম্পন্ন!" else "পিডিএফ রিপোর্ট তৈরি সম্পন্ন!"
                        } else {
                            if (isJpg) "JPG Report Ready!" else "PDF Report Ready!"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (count == 1) {
                        Text(
                            text = exportedFiles[0].name,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        Text(
                            text = if (isBangla) "মোট $count টি পৃষ্ঠার জেপিজি ছবি প্রস্তুত:" else "$count JPG report pages generated:",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFFF9800)
                        )
                        exportedFiles.forEach { f ->
                            Text(
                                text = "• ${f.name}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = if (isBangla) {
                            if (isJpg) "রিপোর্ট ছবিটি সরাসরি অন্যান্য অ্যাপে (WhatsApp, Messenger) শেয়ার করুন অথবা আপনার ফোনে Downloads এ সংরক্ষণ করুন।"
                            else "রিপোর্টটি সরাসরি অন্যান্য অ্যাপে শেয়ার করুন অথবা আপনার ফোনের Downloads ফোল্ডারে সংরক্ষণ করুন।"
                        } else {
                            if (isJpg) "Share report image(s) directly to WhatsApp/Email or save to Downloads/Gallery."
                            else "Share this report with apps (WhatsApp, Email, Drive) or save directly to Downloads."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Share Button (Native Share Sheet)
                    Button(
                        onClick = {
                            showExportActionDialog = false
                            val result = BusinessReportFileManager.shareReportFiles(
                                context = context,
                                files = exportedFiles,
                                mimeType = exportedMimeType,
                                title = exportedTitle
                            )
                            if (result.isFailure) {
                                Toast.makeText(context, "Failed to share: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isJpg) Color(0xFFFF9800) else RedAccent)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(Strings.bizShareReport(language))
                    }

                    // Save to Downloads Button
                    OutlinedButton(
                        onClick = {
                            showExportActionDialog = false
                            val result = BusinessReportFileManager.saveReportFilesToDownloads(
                                context = context,
                                files = exportedFiles,
                                mimeType = exportedMimeType,
                                baseDisplayName = exportedFiles[0].name
                            )
                            result.onSuccess { path ->
                                Toast.makeText(context, "${Strings.bizExportSuccess(language)}: $path", Toast.LENGTH_LONG).show()
                            }.onFailure { err ->
                                Toast.makeText(context, "Failed to save: ${err.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(Strings.bizSaveToDevice(language))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportActionDialog = false }) {
                    Text(Strings.cancel(language))
                }
            },
            containerColor = DarkSurfaceElevated
        )
    }

    // Error Alert Dialog
    if (exportErrorMessage != null) {
        AlertDialog(
            onDismissRequest = { exportErrorMessage = null },
            title = { Text(Strings.bizExportFailed(language), color = RedAccent) },
            text = { Text(exportErrorMessage ?: "Unknown error") },
            confirmButton = {
                Button(
                    onClick = { exportErrorMessage = null },
                    colors = ButtonDefaults.buttonColors(containerColor = RedAccent)
                ) {
                    Text(Strings.ok(language))
                }
            },
            containerColor = DarkSurfaceElevated
        )
    }
}
