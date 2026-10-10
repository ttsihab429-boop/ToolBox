package com.example.ui.screens.business

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.localization.AppLanguage
import com.example.localization.Strings
import com.example.ui.components.ToolActions
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.RedAccent
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class TimePeriod {
    TODAY,
    MONTH,
    ALL
}

enum class BusinessSubTab {
    DASHBOARD,
    CUSTOMERS,
    SUPPLIERS,
    INVENTORY,
    REPORTS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessManagerScreen(
    language: AppLanguage,
    prefs: PreferencesManager,
    businessRepository: BusinessRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val businesses by businessRepository.businesses.collectAsState(initial = emptyList())

    var activeBusinessId by remember { mutableLongStateOf(0L) }
    val currentBusiness = remember(businesses, activeBusinessId) {
        businesses.firstOrNull { it.id == activeBusinessId } ?: businesses.firstOrNull()
    }

    LaunchedEffect(currentBusiness) {
        if (currentBusiness != null && activeBusinessId != currentBusiness.id) {
            activeBusinessId = currentBusiness.id
        }
    }

    val transactions by remember(currentBusiness?.id) {
        if (currentBusiness != null) {
            businessRepository.getTransactions(currentBusiness.id)
        } else {
            flowOf(emptyList())
        }
    }.collectAsState(initial = emptyList())

    val customers by remember(currentBusiness?.id) {
        if (currentBusiness != null) {
            businessRepository.getCustomers(currentBusiness.id)
        } else {
            flowOf(emptyList())
        }
    }.collectAsState(initial = emptyList())

    val suppliers by remember(currentBusiness?.id) {
        if (currentBusiness != null) {
            businessRepository.getSuppliers(currentBusiness.id)
        } else {
            flowOf(emptyList())
        }
    }.collectAsState(initial = emptyList())

    val products by remember(currentBusiness?.id) {
        if (currentBusiness != null) {
            businessRepository.getProducts(currentBusiness.id)
        } else {
            flowOf(emptyList())
        }
    }.collectAsState(initial = emptyList())

    val syncState by businessRepository.syncState.collectAsState()

    var selectedSubTab by remember { mutableStateOf(BusinessSubTab.DASHBOARD) }
    var selectedPeriod by remember { mutableStateOf(TimePeriod.TODAY) }
    var transactionTypeFilter by remember { mutableStateOf<String?>(null) } // null = ALL, "SALE", "PURCHASE", "EXPENSE", "PAYMENT"

    // Dialog States
    var showAddBusinessDialog by remember { mutableStateOf(false) }
    var showTransactionDialog by remember { mutableStateOf(false) }
    var transactionToEdit by remember { mutableStateOf<BusinessTransaction?>(null) }
    var defaultTransactionType by remember { mutableStateOf("SALE") }
    var transactionToDelete by remember { mutableStateOf<BusinessTransaction?>(null) }

    // Payment Dialog state
    var showPaymentDialog by remember { mutableStateOf(false) }
    var paymentDialogPartyType by remember { mutableStateOf(PaymentPartyType.CUSTOMER) }
    var paymentCustomerTarget by remember { mutableStateOf<Customer?>(null) }
    var paymentSupplierTarget by remember { mutableStateOf<Supplier?>(null) }
    var paymentInitialAmount by remember { mutableStateOf(0.0) }

    // Date filters calculation
    val todayString = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    val currentMonthPrefix = remember { SimpleDateFormat("yyyy-MM", Locale.US).format(Date()) }

    val filteredByPeriod = remember(transactions, selectedPeriod) {
        when (selectedPeriod) {
            TimePeriod.TODAY -> transactions.filter { it.dateString == todayString }
            TimePeriod.MONTH -> transactions.filter { it.dateString.startsWith(currentMonthPrefix) }
            TimePeriod.ALL -> transactions
        }
    }

    val currency = currentBusiness?.currency ?: "৳"

    // Metrics calculations
    val totalSales = remember(filteredByPeriod) {
        filteredByPeriod.filter { it.type == "SALE" }.sumOf { it.amount }
    }
    val totalPurchases = remember(filteredByPeriod) {
        filteredByPeriod.filter { it.type == "PURCHASE" }.sumOf { it.amount }
    }
    val totalExpenses = remember(filteredByPeriod) {
        filteredByPeriod.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    }
    val netProfit = remember(totalSales, totalPurchases, totalExpenses) {
        totalSales - (totalPurchases + totalExpenses)
    }

    // Customer dues map & total
    val customerDuesMap = remember(customers, transactions) {
        customers.associateWith { customer ->
            businessRepository.calculateCustomerDue(customer, transactions)
        }
    }
    val totalCustomerDues = remember(customerDuesMap, transactions) {
        if (customers.isNotEmpty()) {
            customerDuesMap.values.sum()
        } else {
            transactions.filter { it.type == "SALE" }.sumOf { it.dueAmount }
        }
    }

    // Supplier payables map & total
    val supplierPayablesMap = remember(suppliers, transactions) {
        suppliers.associateWith { supplier ->
            businessRepository.calculateSupplierPayable(supplier, transactions)
        }
    }
    val totalSupplierPayables = remember(supplierPayablesMap, transactions) {
        if (suppliers.isNotEmpty()) {
            supplierPayablesMap.values.sum()
        } else {
            transactions.filter { it.type == "PURCHASE" }.sumOf { it.dueAmount }
        }
    }

    // Filtered transaction list for display
    val displayedTransactions = remember(filteredByPeriod, transactionTypeFilter) {
        when (transactionTypeFilter) {
            null -> filteredByPeriod
            "PAYMENT" -> filteredByPeriod.filter { it.type == "CUSTOMER_PAYMENT" || it.type == "SUPPLIER_PAYMENT" }
            else -> filteredByPeriod.filter { it.type == transactionTypeFilter }
        }
    }

    var businessDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { businessDropdownExpanded = true }
                            .padding(vertical = 4.dp, horizontal = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = RedAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = currentBusiness?.name ?: (if (language == AppLanguage.BANGLA) "ব্যবসা ম্যানেজার" else "Business Manager"),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Text(
                                text = currentBusiness?.businessType ?: (if (language == AppLanguage.BANGLA) "স্মার্ট হিসাব খাতা" else "Ledger & Accounts"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Switch business",
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
                                            Text(b.name, fontWeight = if (b.id == currentBusiness?.id) FontWeight.Bold else FontWeight.Normal, color = MaterialTheme.colorScheme.onSurface)
                                            Text(b.businessType, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    },
                                    onClick = {
                                        activeBusinessId = b.id
                                        businessDropdownExpanded = false
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = RedAccent, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(if (language == AppLanguage.BANGLA) "+ নতুন ব্যবসা যোগ করুন" else "+ Add New Business", color = RedAccent, fontWeight = FontWeight.Bold)
                                    }
                                },
                                onClick = {
                                    businessDropdownExpanded = false
                                    showAddBusinessDialog = true
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Business Sub Tabs Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                color = DarkSurfaceVariant
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val tabs = listOf(
                        Triple(BusinessSubTab.DASHBOARD, Strings.bizDashboard(language), Icons.Default.Dashboard),
                        Triple(BusinessSubTab.CUSTOMERS, Strings.bizCustomers(language), Icons.Default.Person),
                        Triple(BusinessSubTab.SUPPLIERS, Strings.bizSuppliers(language), Icons.Default.Storefront),
                        Triple(BusinessSubTab.INVENTORY, Strings.bizInventory(language), Icons.Default.Inventory2),
                        Triple(BusinessSubTab.REPORTS, Strings.bizReports(language), Icons.Default.Description)
                    )

                    tabs.forEach { (tab, title, icon) ->
                        val isSelected = selectedSubTab == tab
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedSubTab = tab }
                                .testTag("biz_tab_${tab.name.lowercase()}"),
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) RedAccent else Color.Transparent
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Sub Tab Content
            when (selectedSubTab) {
                BusinessSubTab.DASHBOARD -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(2.dp))

                            // Cloud Backup & Online Sync
                            CloudSyncCard(
                                syncState = syncState,
                                lang = language,
                                onSyncNow = {
                                    scope.launch {
                                        businessRepository.triggerSync(isManual = true)
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Time Period Filter Tabs
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DarkSurfaceVariant)
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf(
                                    TimePeriod.TODAY to (if (language == AppLanguage.BANGLA) "আজ (Today)" else "Today"),
                                    TimePeriod.MONTH to (if (language == AppLanguage.BANGLA) "চলতি মাস" else "This Month"),
                                    TimePeriod.ALL to (if (language == AppLanguage.BANGLA) "সর্বমোট" else "All Time")
                                ).forEach { (period, title) ->
                                    val isSelected = selectedPeriod == period
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { selectedPeriod = period },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) RedAccent else Color.Transparent
                                    ) {
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        // Summary Metric Dashboard Grid
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                // Net Profit / Loss Banner
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
                                                text = if (netProfit >= 0) {
                                                    if (language == AppLanguage.BANGLA) "নিট লাভ (Net Profit)" else "Net Profit"
                                                } else {
                                                    if (language == AppLanguage.BANGLA) "নিট ক্ষতি (Net Loss)" else "Net Loss"
                                                },
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "$currency ${formatMoney(kotlin.math.abs(netProfit))}",
                                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (netProfit >= 0) GreenSuccess else RedAccent
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .background(if (netProfit >= 0) GreenSuccess.copy(alpha = 0.15f) else RedAccent.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (netProfit >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                                contentDescription = null,
                                                tint = if (netProfit >= 0) GreenSuccess else RedAccent,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                    }
                                }

                                // Sales & Purchases Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    BusinessMetricCard(
                                        modifier = Modifier.weight(1f),
                                        title = if (language == AppLanguage.BANGLA) "মোট বিক্রি" else "Total Sales",
                                        amount = "$currency ${formatMoney(totalSales)}",
                                        icon = Icons.Default.Payments,
                                        color = GreenSuccess
                                    )
                                    BusinessMetricCard(
                                        modifier = Modifier.weight(1f),
                                        title = if (language == AppLanguage.BANGLA) "মোট ক্রয়" else "Purchases",
                                        amount = "$currency ${formatMoney(totalPurchases)}",
                                        icon = Icons.Default.ShoppingCart,
                                        color = Color(0xFFFF9800)
                                    )
                                }

                                // Expenses & Customer Dues Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    BusinessMetricCard(
                                        modifier = Modifier.weight(1f),
                                        title = if (language == AppLanguage.BANGLA) "মোট খরচ" else "Expenses",
                                        amount = "$currency ${formatMoney(totalExpenses)}",
                                        icon = Icons.Default.MoneyOff,
                                        color = RedAccent
                                    )
                                    BusinessMetricCard(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedSubTab = BusinessSubTab.CUSTOMERS },
                                        title = if (language == AppLanguage.BANGLA) "কাস্টমার বাকি" else "Customer Due",
                                        amount = "$currency ${formatMoney(totalCustomerDues)}",
                                        icon = Icons.Default.ReceiptLong,
                                        color = if (totalCustomerDues > 0) RedAccent else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Supplier Due Card
                                if (totalSupplierPayables > 0) {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable { selectedSubTab = BusinessSubTab.SUPPLIERS },
                                        shape = RoundedCornerShape(14.dp),
                                        color = DarkSurface,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF9800).copy(alpha = 0.4f))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Storefront, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = if (language == AppLanguage.BANGLA) "মহাজন / সাপ্লায়ার দেনা বাকি:" else "Supplier Payable Due:",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Text(
                                                text = "$currency ${formatMoney(totalSupplierPayables)}",
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFFF9800)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Quick Add Action Buttons
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            defaultTransactionType = "SALE"
                                            transactionToEdit = null
                                            showTransactionDialog = true
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (language == AppLanguage.BANGLA) "বিক্রি" else "Sale", fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            defaultTransactionType = "PURCHASE"
                                            transactionToEdit = null
                                            showTransactionDialog = true
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (language == AppLanguage.BANGLA) "ক্রয়" else "Purchase", fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            defaultTransactionType = "EXPENSE"
                                            transactionToEdit = null
                                            showTransactionDialog = true
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = RedAccent)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (language == AppLanguage.BANGLA) "খরচ" else "Expense", fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Quick Payment Button
                                OutlinedButton(
                                    onClick = {
                                        paymentDialogPartyType = PaymentPartyType.CUSTOMER
                                        paymentCustomerTarget = null
                                        paymentSupplierTarget = null
                                        paymentInitialAmount = 0.0
                                        showPaymentDialog = true
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp), tint = RedAccent)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (language == AppLanguage.BANGLA) "+ বাকি আদায় বা দেনা পরিশোধ রেকর্ড করুন" else "+ Record Customer / Supplier Payment",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                // Quick Reports Button
                                OutlinedButton(
                                    onClick = { selectedSubTab = BusinessSubTab.REPORTS },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp), tint = RedAccent)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (language == AppLanguage.BANGLA) "📊 ব্যবসা রিপোর্ট ও এক্সপোর্ট (PDF / CSV)" else "📊 Business Reports & Export (PDF / CSV)",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // Transaction Filter Chips & Header
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (language == AppLanguage.BANGLA) "লেনদেন ও হিসাব খতিয়ান" else "Transaction Ledger",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Text(
                                    text = "${displayedTransactions.size} Entries",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                item {
                                    FilterChip(
                                        selected = transactionTypeFilter == null,
                                        onClick = { transactionTypeFilter = null },
                                        label = { Text(if (language == AppLanguage.BANGLA) "সকল (${filteredByPeriod.size})" else "All") },
                                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RedAccent, containerColor = DarkSurfaceElevated)
                                    )
                                }
                                item {
                                    FilterChip(
                                        selected = transactionTypeFilter == "SALE",
                                        onClick = { transactionTypeFilter = "SALE" },
                                        label = { Text(if (language == AppLanguage.BANGLA) "বিক্রি (${filteredByPeriod.count { it.type == "SALE" }})" else "Sales") },
                                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GreenSuccess, containerColor = DarkSurfaceElevated)
                                    )
                                }
                                item {
                                    FilterChip(
                                        selected = transactionTypeFilter == "PURCHASE",
                                        onClick = { transactionTypeFilter = "PURCHASE" },
                                        label = { Text(if (language == AppLanguage.BANGLA) "ক্রয় (${filteredByPeriod.count { it.type == "PURCHASE" }})" else "Purchases") },
                                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFFF9800), containerColor = DarkSurfaceElevated)
                                    )
                                }
                                item {
                                    FilterChip(
                                        selected = transactionTypeFilter == "EXPENSE",
                                        onClick = { transactionTypeFilter = "EXPENSE" },
                                        label = { Text(if (language == AppLanguage.BANGLA) "খরচ (${filteredByPeriod.count { it.type == "EXPENSE" }})" else "Expenses") },
                                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RedAccent, containerColor = DarkSurfaceElevated)
                                    )
                                }
                                item {
                                    FilterChip(
                                        selected = transactionTypeFilter == "PAYMENT",
                                        onClick = { transactionTypeFilter = "PAYMENT" },
                                        label = { Text(if (language == AppLanguage.BANGLA) "পেমেন্ট (${filteredByPeriod.count { it.type == "CUSTOMER_PAYMENT" || it.type == "SUPPLIER_PAYMENT" }})" else "Payments") },
                                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF2196F3), containerColor = DarkSurfaceElevated)
                                    )
                                }
                            }
                        }

                        // Transaction Items
                        if (displayedTransactions.isEmpty()) {
                            item {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    color = DarkSurface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(40.dp))
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = if (language == AppLanguage.BANGLA) "কোনো লেনদেন রেকর্ড নেই" else "No transactions in this period",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (language == AppLanguage.BANGLA) "উপরের বাটন দিয়ে বিক্রি, ক্রয় বা খরচ যুক্ত করুন" else "Tap Sale, Purchase, or Expense above to record entries",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else {
                            items(displayedTransactions, key = { it.id }) { item ->
                                TransactionItemRow(
                                    item = item,
                                    currency = currency,
                                    language = language,
                                    onEdit = {
                                        transactionToEdit = item
                                        showTransactionDialog = true
                                    },
                                    onDelete = {
                                        transactionToDelete = item
                                    }
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }

                BusinessSubTab.CUSTOMERS -> {
                    if (currentBusiness != null) {
                        CustomerSection(
                            currentBusiness = currentBusiness,
                            businessRepository = businessRepository,
                            language = language,
                            prefs = prefs,
                            allTransactions = transactions,
                            onRecordPayment = { targetCustomer, due ->
                                paymentDialogPartyType = PaymentPartyType.CUSTOMER
                                paymentCustomerTarget = targetCustomer
                                paymentSupplierTarget = null
                                paymentInitialAmount = due
                                showPaymentDialog = true
                            }
                        )
                    }
                }

                BusinessSubTab.SUPPLIERS -> {
                    if (currentBusiness != null) {
                        SupplierSection(
                            currentBusiness = currentBusiness,
                            businessRepository = businessRepository,
                            language = language,
                            prefs = prefs,
                            allTransactions = transactions,
                            onRecordPayment = { targetSupplier, payable ->
                                paymentDialogPartyType = PaymentPartyType.SUPPLIER
                                paymentCustomerTarget = null
                                paymentSupplierTarget = targetSupplier
                                paymentInitialAmount = payable
                                showPaymentDialog = true
                            }
                        )
                    }
                }

                BusinessSubTab.INVENTORY -> {
                    if (currentBusiness != null) {
                        InventorySection(
                            currentBusiness = currentBusiness,
                            businessRepository = businessRepository,
                            language = language,
                            prefs = prefs
                        )
                    }
                }

                BusinessSubTab.REPORTS -> {
                    if (currentBusiness != null) {
                        ReportsSection(
                            currentBusiness = currentBusiness,
                            businesses = businesses,
                            businessRepository = businessRepository,
                            language = language,
                            prefs = prefs,
                            allTransactions = transactions,
                            customers = customers,
                            suppliers = suppliers,
                            products = products,
                            onSelectBusiness = { selectedBiz ->
                                activeBusinessId = selectedBiz.id
                            }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Transaction Dialog
    if (showTransactionDialog && currentBusiness != null) {
        AddEditTransactionDialog(
            initialTransaction = transactionToEdit,
            defaultType = defaultTransactionType,
            currency = currency,
            language = language,
            customers = customers,
            suppliers = suppliers,
            products = products,
            onDismiss = { showTransactionDialog = false },
            onSave = { type, title, amount, paid, partyName, partyPhone, note, custId, suppId, prodId, qty, unitPrice ->
                scope.launch {
                    businessRepository.saveTransaction(
                        id = transactionToEdit?.id ?: 0L,
                        businessId = currentBusiness.id,
                        type = type,
                        title = title,
                        amount = amount,
                        paidAmount = paid,
                        partyName = partyName,
                        partyPhone = partyPhone,
                        note = note,
                        timestamp = transactionToEdit?.timestamp ?: System.currentTimeMillis(),
                        customerId = custId,
                        supplierId = suppId,
                        productId = prodId,
                        quantity = qty,
                        unitPrice = unitPrice
                    )
                    ToolActions.triggerHaptic(context, prefs.hapticEnabled.value)
                    showTransactionDialog = false
                }
            }
        )
    }

    // Record Payment Dialog
    if (showPaymentDialog && currentBusiness != null) {
        RecordPaymentDialog(
            initialType = paymentDialogPartyType,
            initialCustomer = paymentCustomerTarget,
            initialSupplier = paymentSupplierTarget,
            initialAmount = paymentInitialAmount,
            customers = customers,
            suppliers = suppliers,
            customerDuesMap = customerDuesMap,
            supplierPayablesMap = supplierPayablesMap,
            currency = currency,
            language = language,
            onDismiss = { showPaymentDialog = false },
            onSaveCustomerPayment = { cust, amount, note ->
                scope.launch {
                    businessRepository.recordCustomerPayment(
                        businessId = currentBusiness.id,
                        customerId = cust.id,
                        customerName = cust.name,
                        customerPhone = cust.phone,
                        amount = amount,
                        note = note
                    )
                    ToolActions.triggerHaptic(context, prefs.hapticEnabled.value)
                    showPaymentDialog = false
                }
            },
            onSaveSupplierPayment = { supp, amount, note ->
                scope.launch {
                    businessRepository.recordSupplierPayment(
                        businessId = currentBusiness.id,
                        supplierId = supp.id,
                        supplierName = supp.name,
                        supplierPhone = supp.phone,
                        amount = amount,
                        note = note
                    )
                    ToolActions.triggerHaptic(context, prefs.hapticEnabled.value)
                    showPaymentDialog = false
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (transactionToDelete != null) {
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text(if (language == AppLanguage.BANGLA) "লেনদেন মুছবেন?" else "Delete Transaction?") },
            text = {
                Text(
                    text = if (language == AppLanguage.BANGLA) "'${transactionToDelete?.title}' হিসাবটি মুছে ফেলতে চান?" else "Are you sure you want to delete '${transactionToDelete?.title}' ($currency ${transactionToDelete?.amount})?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val toDel = transactionToDelete
                        transactionToDelete = null
                        if (toDel != null) {
                            scope.launch {
                                businessRepository.deleteTransaction(toDel)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedAccent)
                ) {
                    Text(Strings.delete(language))
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text(Strings.cancel(language))
                }
            },
            containerColor = DarkSurfaceElevated
        )
    }

    // Add Business Profile Dialog
    if (showAddBusinessDialog) {
        var newBizName by remember { mutableStateOf("") }
        var newBizType by remember { mutableStateOf("Retail") }
        var newBizCurrency by remember { mutableStateOf("৳") }

        AlertDialog(
            onDismissRequest = { showAddBusinessDialog = false },
            title = { Text(if (language == AppLanguage.BANGLA) "নতুন ব্যবসা যোগ করুন" else "Create Business Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newBizName,
                        onValueChange = { newBizName = it },
                        label = { Text(if (language == AppLanguage.BANGLA) "ব্যবসার নাম" else "Business Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RedAccent,
                            unfocusedBorderColor = DarkBorder,
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant
                        )
                    )

                    OutlinedTextField(
                        value = newBizType,
                        onValueChange = { newBizType = it },
                        label = { Text(if (language == AppLanguage.BANGLA) "ব্যবসার ধরন (যেমন: মুদি, ফার্মেসি, পাইকারি)" else "Business Type") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RedAccent,
                            unfocusedBorderColor = DarkBorder,
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newBizName.isNotBlank()) {
                            scope.launch {
                                val id = businessRepository.addBusiness(newBizName, newBizType, newBizCurrency)
                                activeBusinessId = id
                                showAddBusinessDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedAccent)
                ) {
                    Text(if (language == AppLanguage.BANGLA) "সংরক্ষণ করুন" else "Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBusinessDialog = false }) {
                    Text(Strings.cancel(language))
                }
            },
            containerColor = DarkSurfaceElevated
        )
    }
}

@Composable
fun BusinessMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    amount: String,
    icon: ImageVector,
    color: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = DarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = amount,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
    }
}

@Composable
fun TransactionItemRow(
    item: BusinessTransaction,
    currency: String,
    language: AppLanguage,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val typeColor: Color = when (item.type) {
        "SALE" -> GreenSuccess
        "PURCHASE" -> Color(0xFFFF9800)
        "CUSTOMER_PAYMENT" -> Color(0xFF2196F3)
        "SUPPLIER_PAYMENT" -> Color(0xFF9C27B0)
        else -> RedAccent
    }
    val typeLabel: String = when (item.type) {
        "SALE" -> if (language == AppLanguage.BANGLA) "বিক্রি" else "Sale"
        "PURCHASE" -> if (language == AppLanguage.BANGLA) "ক্রয়" else "Purchase"
        "CUSTOMER_PAYMENT" -> if (language == AppLanguage.BANGLA) "আদায়" else "Payment"
        "SUPPLIER_PAYMENT" -> if (language == AppLanguage.BANGLA) "পরিশোধ" else "Payment"
        else -> if (language == AppLanguage.BANGLA) "খরচ" else "Expense"
    }

    val dateFormatted = remember(item.timestamp) {
        SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date(item.timestamp))
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = DarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = typeColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = typeLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = typeColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (item.partyName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${item.partyName}${if (item.partyPhone.isNotBlank()) " • ${item.partyPhone}" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (item.quantity > 0) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${formatQuantity(item.quantity)} items @ $currency ${formatMoney(item.unitPrice)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateFormatted,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                if (item.dueAmount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${if (language == AppLanguage.BANGLA) "বাকি:" else "Due:"} $currency ${formatMoney(item.dueAmount)} (Paid: $currency ${formatMoney(item.paidAmount)})",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = RedAccent
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$currency ${formatMoney(item.amount)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = typeColor
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RedAccent.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditTransactionDialog(
    initialTransaction: BusinessTransaction?,
    defaultType: String,
    currency: String,
    language: AppLanguage,
    customers: List<Customer>,
    suppliers: List<Supplier>,
    products: List<ProductItem>,
    onDismiss: () -> Unit,
    onSave: (
        type: String,
        title: String,
        amount: Double,
        paid: Double,
        partyName: String,
        partyPhone: String,
        note: String,
        customerId: Long?,
        supplierId: Long?,
        productId: Long?,
        quantity: Double,
        unitPrice: Double
    ) -> Unit
) {
    var type by remember { mutableStateOf(initialTransaction?.type ?: defaultType) }
    var title by remember { mutableStateOf(initialTransaction?.title ?: "") }
    var amountText by remember { mutableStateOf(initialTransaction?.let { if (it.amount > 0) it.amount.toString() else "" } ?: "") }
    var paidText by remember { mutableStateOf(initialTransaction?.let { if (it.paidAmount > 0) it.paidAmount.toString() else "" } ?: "") }
    var partyName by remember { mutableStateOf(initialTransaction?.partyName ?: "") }
    var partyPhone by remember { mutableStateOf(initialTransaction?.partyPhone ?: "") }
    var note by remember { mutableStateOf(initialTransaction?.note ?: "") }

    var selectedCustomerId by remember { mutableStateOf(initialTransaction?.customerId) }
    var selectedSupplierId by remember { mutableStateOf(initialTransaction?.supplierId) }
    var selectedProductId by remember { mutableStateOf(initialTransaction?.productId) }
    var quantityText by remember { mutableStateOf(initialTransaction?.let { if (it.quantity > 0) it.quantity.toString() else "" } ?: "") }
    var unitPriceText by remember { mutableStateOf(initialTransaction?.let { if (it.unitPrice > 0) it.unitPrice.toString() else "" } ?: "") }

    var partyDropdownExpanded by remember { mutableStateOf(false) }
    var productDropdownExpanded by remember { mutableStateOf(false) }

    val amountVal = amountText.toDoubleOrNull() ?: 0.0
    val paidVal = paidText.toDoubleOrNull() ?: 0.0
    val calculatedDue = (amountVal - paidVal).coerceAtLeast(0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialTransaction != null) {
                    if (language == AppLanguage.BANGLA) "লেনদেন সংশোধন করুন" else "Edit Entry"
                } else {
                    when (type) {
                        "SALE" -> if (language == AppLanguage.BANGLA) "নতুন বিক্রি যুক্ত করুন" else "Record New Sale"
                        "PURCHASE" -> if (language == AppLanguage.BANGLA) "নতুন ক্রয় যুক্ত করুন" else "Record New Purchase"
                        else -> if (language == AppLanguage.BANGLA) "নতুন খরচ যুক্ত করুন" else "Record New Expense"
                    }
                },
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Type selector row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "SALE" to (if (language == AppLanguage.BANGLA) "বিক্রি" else "Sale"),
                            "PURCHASE" to (if (language == AppLanguage.BANGLA) "ক্রয়" else "Purchase"),
                            "EXPENSE" to (if (language == AppLanguage.BANGLA) "খরচ" else "Expense")
                        ).forEach { (t, label) ->
                            FilterChip(
                                selected = type == t,
                                onClick = {
                                    type = t
                                    if (title.isBlank() || title == "Sale" || title == "Purchase" || title == "Expense" || title == "বিক্রি" || title == "ক্রয়" || title == "খরচ") {
                                        title = label
                                    }
                                },
                                label = { Text(label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = when (t) {
                                        "SALE" -> GreenSuccess
                                        "PURCHASE" -> Color(0xFFFF9800)
                                        else -> RedAccent
                                    },
                                    containerColor = DarkSurfaceVariant
                                )
                            )
                        }
                    }
                }

                // Inventory Product Picker (for SALE or PURCHASE)
                if ((type == "SALE" || type == "PURCHASE") && products.isNotEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { productDropdownExpanded = true },
                            color = DarkSurfaceVariant,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val selectedProd = products.firstOrNull { it.id == selectedProductId }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Inventory2, contentDescription = null, tint = RedAccent, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = selectedProd?.let { "${it.name} (Stock: ${formatQuantity(it.currentQuantity)} ${it.unit})" }
                                            ?: (if (language == AppLanguage.BANGLA) "ইনভেন্টরি পণ্য নির্বাচন (ঐচ্ছিক)" else "Select Product from Inventory (Optional)"),
                                        fontSize = 13.sp,
                                        color = if (selectedProd != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)

                                DropdownMenu(
                                    expanded = productDropdownExpanded,
                                    onDismissRequest = { productDropdownExpanded = false },
                                    containerColor = DarkSurfaceElevated
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(if (language == AppLanguage.BANGLA) "পণ্য ছাড়া সাধারণ লেনদেন" else "None (Manual Entry)") },
                                        onClick = {
                                            selectedProductId = null
                                            productDropdownExpanded = false
                                        }
                                    )
                                    products.forEach { prod ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(prod.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                                    Text(
                                                        "Stock: ${formatQuantity(prod.currentQuantity)} ${prod.unit} • Sell: $currency ${formatMoney(prod.sellingPrice)} • Cost: $currency ${formatMoney(prod.purchasePrice)}",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            },
                                            onClick = {
                                                selectedProductId = prod.id
                                                title = prod.name
                                                val unitP = if (type == "SALE") prod.sellingPrice else prod.purchasePrice
                                                unitPriceText = unitP.toString()
                                                if (quantityText.isBlank()) quantityText = "1"
                                                val q = quantityText.toDoubleOrNull() ?: 1.0
                                                val total = unitP * q
                                                amountText = total.toString()
                                                productDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Quantity and Unit Price row if a product or quantity is specified
                if (type == "SALE" || type == "PURCHASE") {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = quantityText,
                                onValueChange = {
                                    quantityText = it
                                    val q = it.toDoubleOrNull() ?: 0.0
                                    val up = unitPriceText.toDoubleOrNull() ?: 0.0
                                    if (q > 0 && up > 0) {
                                        amountText = (q * up).toString()
                                    }
                                },
                                label = { Text(if (language == AppLanguage.BANGLA) "পরিমাণ (Qty)" else "Quantity") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = RedAccent,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedContainerColor = DarkSurfaceVariant,
                                    unfocusedContainerColor = DarkSurfaceVariant
                                )
                            )

                            OutlinedTextField(
                                value = unitPriceText,
                                onValueChange = {
                                    unitPriceText = it
                                    val q = quantityText.toDoubleOrNull() ?: 1.0
                                    val up = it.toDoubleOrNull() ?: 0.0
                                    if (q > 0 && up > 0) {
                                        amountText = (q * up).toString()
                                    }
                                },
                                label = { Text("${if (language == AppLanguage.BANGLA) "দর / ইউনিট" else "Unit Price"} ($currency)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = RedAccent,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedContainerColor = DarkSurfaceVariant,
                                    unfocusedContainerColor = DarkSurfaceVariant
                                )
                            )
                        }
                    }
                }

                // Title / Description
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(if (language == AppLanguage.BANGLA) "বিবরণ / পণ্যের নাম" else "Description / Item Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RedAccent,
                            unfocusedBorderColor = DarkBorder,
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant
                        )
                    )
                }

                // Total Amount
                item {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = {
                            amountText = it
                            if (type == "EXPENSE") {
                                paidText = it
                            }
                        },
                        label = { Text("${if (language == AppLanguage.BANGLA) "মোট টাকা" else "Total Amount"} ($currency) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RedAccent,
                            unfocusedBorderColor = DarkBorder,
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant
                        )
                    )
                }

                // Paid Amount & Full Paid button
                if (type != "EXPENSE") {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = paidText,
                                onValueChange = { paidText = it },
                                label = { Text("${if (language == AppLanguage.BANGLA) "জমা / পরিশোধ" else "Paid Amount"} ($currency)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = RedAccent,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedContainerColor = DarkSurfaceVariant,
                                    unfocusedContainerColor = DarkSurfaceVariant
                                )
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            OutlinedButton(
                                onClick = { paidText = amountText },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(if (language == AppLanguage.BANGLA) "পুরো পরিশোধ" else "Full", fontSize = 12.sp)
                            }
                        }

                        if (calculatedDue > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${if (language == AppLanguage.BANGLA) "বাকি থাকবে:" else "Pending Due:"} $currency ${formatMoney(calculatedDue)}",
                                color = RedAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Party selection (Customer for SALE, Supplier for PURCHASE)
                if (type == "SALE" && customers.isNotEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { partyDropdownExpanded = true },
                            color = DarkSurfaceVariant,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val cust = customers.firstOrNull { it.id == selectedCustomerId }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = GreenSuccess, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = cust?.let { "${it.name} (${it.phone})" }
                                            ?: (if (language == AppLanguage.BANGLA) "কাস্টমার তালিকা থেকে নির্বাচন করুন" else "Select Customer from list"),
                                        fontSize = 13.sp,
                                        color = if (cust != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)

                                DropdownMenu(
                                    expanded = partyDropdownExpanded,
                                    onDismissRequest = { partyDropdownExpanded = false },
                                    containerColor = DarkSurfaceElevated
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(if (language == AppLanguage.BANGLA) "হাতে লিখে নাম লিখুন" else "Manual Name") },
                                        onClick = {
                                            selectedCustomerId = null
                                            partyDropdownExpanded = false
                                        }
                                    )
                                    customers.forEach { c ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(c.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                                    if (c.phone.isNotBlank()) Text(c.phone, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            },
                                            onClick = {
                                                selectedCustomerId = c.id
                                                partyName = c.name
                                                partyPhone = c.phone
                                                partyDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else if (type == "PURCHASE" && suppliers.isNotEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { partyDropdownExpanded = true },
                            color = DarkSurfaceVariant,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val supp = suppliers.firstOrNull { it.id == selectedSupplierId }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Storefront, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = supp?.let { "${it.name} (${it.companyName})" }
                                            ?: (if (language == AppLanguage.BANGLA) "সাপ্লায়ার তালিকা থেকে নির্বাচন করুন" else "Select Supplier from list"),
                                        fontSize = 13.sp,
                                        color = if (supp != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)

                                DropdownMenu(
                                    expanded = partyDropdownExpanded,
                                    onDismissRequest = { partyDropdownExpanded = false },
                                    containerColor = DarkSurfaceElevated
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(if (language == AppLanguage.BANGLA) "হাতে লিখে নাম লিখুন" else "Manual Name") },
                                        onClick = {
                                            selectedSupplierId = null
                                            partyDropdownExpanded = false
                                        }
                                    )
                                    suppliers.forEach { s ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(s.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                                    if (s.companyName.isNotBlank()) Text(s.companyName, fontSize = 11.sp, color = Color(0xFFFF9800))
                                                }
                                            },
                                            onClick = {
                                                selectedSupplierId = s.id
                                                partyName = s.name
                                                partyPhone = s.phone
                                                partyDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Party Name text field
                item {
                    OutlinedTextField(
                        value = partyName,
                        onValueChange = { partyName = it },
                        label = {
                            Text(
                                when (type) {
                                    "SALE" -> if (language == AppLanguage.BANGLA) "কাস্টমার / গ্রাহকের নাম" else "Customer Name"
                                    "PURCHASE" -> if (language == AppLanguage.BANGLA) "সাপ্লায়ার / মহাজনের নাম" else "Supplier Name"
                                    else -> if (language == AppLanguage.BANGLA) "প্রাপক / খাত (ঐচ্ছিক)" else "Paid To / Category"
                                }
                            )
                        },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RedAccent,
                            unfocusedBorderColor = DarkBorder,
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant
                        )
                    )
                }

                // Party Phone
                if (type != "EXPENSE") {
                    item {
                        OutlinedTextField(
                            value = partyPhone,
                            onValueChange = { partyPhone = it },
                            label = { Text(if (language == AppLanguage.BANGLA) "মোবাইল নম্বর (ঐচ্ছিক)" else "Phone Number (Optional)") },
                            leadingIcon = { Icon(Icons.Default.Call, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RedAccent,
                                unfocusedBorderColor = DarkBorder,
                                focusedContainerColor = DarkSurfaceVariant,
                                unfocusedContainerColor = DarkSurfaceVariant
                            )
                        )
                    }
                }

                // Note
                item {
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text(if (language == AppLanguage.BANGLA) "নোট / মন্তব্য (ঐচ্ছিক)" else "Note (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RedAccent,
                            unfocusedBorderColor = DarkBorder,
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (amountVal > 0) {
                        val finalTitle = title.ifBlank {
                            when (type) {
                                "SALE" -> "Sale"
                                "PURCHASE" -> "Purchase"
                                else -> "Expense"
                            }
                        }
                        val finalPaid = if (type == "EXPENSE") amountVal else paidVal
                        val q = quantityText.toDoubleOrNull() ?: 0.0
                        val up = unitPriceText.toDoubleOrNull() ?: 0.0

                        onSave(
                            type,
                            finalTitle,
                            amountVal,
                            finalPaid,
                            partyName,
                            partyPhone,
                            note,
                            selectedCustomerId,
                            selectedSupplierId,
                            selectedProductId,
                            q,
                            up
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RedAccent)
            ) {
                Text(Strings.save(language))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Strings.cancel(language))
            }
        },
        containerColor = DarkSurfaceElevated
    )
}

fun formatMoney(amount: Double): String {
    return NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 2
    }.format(amount)
}
