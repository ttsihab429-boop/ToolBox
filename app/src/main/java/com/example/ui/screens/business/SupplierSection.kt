package com.example.ui.screens.business

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.collectAsState
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SupplierSection(
    currentBusiness: BusinessProfile,
    businessRepository: BusinessRepository,
    language: AppLanguage,
    prefs: PreferencesManager,
    allTransactions: List<BusinessTransaction>,
    onRecordPayment: (Supplier, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currency = currentBusiness.currency

    val suppliers by businessRepository.getSuppliers(currentBusiness.id).collectAsState(initial = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var filterOnlyWithPayable by remember { mutableStateOf(false) }

    // Dialog states
    var showAddSupplierDialog by remember { mutableStateOf(false) }
    var supplierToEdit by remember { mutableStateOf<Supplier?>(null) }
    var supplierToDelete by remember { mutableStateOf<Supplier?>(null) }
    var supplierForDetails by remember { mutableStateOf<Supplier?>(null) }

    // Computed supplier payables map for fast rendering
    val supplierPayablesMap = remember(suppliers, allTransactions) {
        suppliers.associateWith { supplier ->
            businessRepository.calculateSupplierPayable(supplier, allTransactions)
        }
    }

    val totalPayables = remember(supplierPayablesMap) {
        supplierPayablesMap.values.sum()
    }

    val filteredSuppliers = remember(suppliers, searchQuery, filterOnlyWithPayable, supplierPayablesMap) {
        suppliers.filter { supplier ->
            val matchesQuery = searchQuery.isBlank() ||
                supplier.name.contains(searchQuery, ignoreCase = true) ||
                supplier.companyName.contains(searchQuery, ignoreCase = true) ||
                supplier.phone.contains(searchQuery, ignoreCase = true)
            val payable = supplierPayablesMap[supplier] ?: 0.0
            val matchesPayable = !filterOnlyWithPayable || payable > 0.0
            matchesQuery && matchesPayable
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Summary Card
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
                        text = if (language == AppLanguage.BANGLA) "মহাজন / সাপ্লায়ার দেনা বাকি (Payable)" else "Total Supplier Payables",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$currency ${formatMoney(totalPayables)}",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (totalPayables > 0) Color(0xFFFF9800) else GreenSuccess
                    )
                    Text(
                        text = "${suppliers.size} ${if (language == AppLanguage.BANGLA) "জন নিবন্ধিত সাপ্লায়ার" else "Registered Suppliers"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = {
                        supplierToEdit = null
                        showAddSupplierDialog = true
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                    modifier = Modifier.testTag("add_supplier_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (language == AppLanguage.BANGLA) "মহাজন" else "Add", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Search Bar & Filter
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        if (language == AppLanguage.BANGLA) "নাম, কোম্পানি বা মোবাইল দিয়ে খুঁজুন…" else "Search name, company, or phone…",
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("search_supplier_field"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFF9800),
                    unfocusedBorderColor = DarkBorder,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface
                )
            )

            FilterChip(
                selected = filterOnlyWithPayable,
                onClick = { filterOnlyWithPayable = !filterOnlyWithPayable },
                label = { Text(if (language == AppLanguage.BANGLA) "দেনা আছে" else "Payable Only", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFFF9800),
                    containerColor = DarkSurface
                )
            )
        }

        // Supplier List
        if (filteredSuppliers.isEmpty()) {
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
                    Icon(
                        Icons.Default.Storefront,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (language == AppLanguage.BANGLA) "কোনো সাপ্লায়ার পাওয়া যায়নি" else "No suppliers found",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (language == AppLanguage.BANGLA) "নতুন মহাজন যোগ করতে উপরের '+ মহাজন' চাপুন" else "Tap '+ Add' to record supplier details and payables",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredSuppliers, key = { it.id }) { supplier ->
                    val payable = supplierPayablesMap[supplier] ?: 0.0

                    SupplierCardItem(
                        supplier = supplier,
                        payableAmount = payable,
                        currency = currency,
                        language = language,
                        onCall = {
                            if (supplier.phone.isNotBlank()) {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${supplier.phone}")
                                }
                                context.startActivity(intent)
                            }
                        },
                        onRecordPayment = {
                            onRecordPayment(supplier, payable)
                        },
                        onViewDetails = {
                            supplierForDetails = supplier
                        },
                        onEdit = {
                            supplierToEdit = supplier
                            showAddSupplierDialog = true
                        },
                        onDelete = {
                            supplierToDelete = supplier
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    // Add / Edit Supplier Dialog
    if (showAddSupplierDialog) {
        AddEditSupplierDialog(
            supplierToEdit = supplierToEdit,
            currency = currency,
            language = language,
            onDismiss = { showAddSupplierDialog = false },
            onSave = { name, companyName, phone, address, openingBalance, note ->
                scope.launch {
                    businessRepository.saveSupplier(
                        id = supplierToEdit?.id ?: 0L,
                        businessId = currentBusiness.id,
                        name = name,
                        companyName = companyName,
                        phone = phone,
                        address = address,
                        openingBalance = openingBalance,
                        note = note
                    )
                    ToolActions.triggerHaptic(context, prefs.hapticEnabled.value)
                    showAddSupplierDialog = false
                }
            }
        )
    }

    // Delete Supplier Confirmation
    if (supplierToDelete != null) {
        val target = supplierToDelete!!
        AlertDialog(
            onDismissRequest = { supplierToDelete = null },
            title = { Text(if (language == AppLanguage.BANGLA) "সাপ্লায়ার মুছবেন?" else "Delete Supplier?") },
            text = {
                Text(
                    if (language == AppLanguage.BANGLA)
                        "'${target.name}' এর রেকর্ডটি মুছে ফেলতে চান?"
                    else
                        "Are you sure you want to delete supplier '${target.name}'?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            businessRepository.deleteSupplier(target)
                            supplierToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedAccent)
                ) {
                    Text(Strings.delete(language))
                }
            },
            dismissButton = {
                TextButton(onClick = { supplierToDelete = null }) {
                    Text(Strings.cancel(language))
                }
            },
            containerColor = DarkSurfaceElevated
        )
    }

    // Supplier Detail Sheet / Dialog
    if (supplierForDetails != null) {
        val target = supplierForDetails!!
        val supplierTransactions = remember(allTransactions, target.id) {
            allTransactions.filter { it.supplierId == target.id }
        }
        val targetPayable = supplierPayablesMap[target] ?: 0.0

        SupplierDetailsDialog(
            supplier = target,
            payableAmount = targetPayable,
            transactions = supplierTransactions,
            currency = currency,
            language = language,
            onDismiss = { supplierForDetails = null },
            onRecordPayment = {
                supplierForDetails = null
                onRecordPayment(target, targetPayable)
            }
        )
    }
}

@Composable
fun SupplierCardItem(
    supplier: Supplier,
    payableAmount: Double,
    currency: String,
    language: AppLanguage,
    onCall: () -> Unit,
    onRecordPayment: () -> Unit,
    onViewDetails: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onViewDetails() },
        shape = RoundedCornerShape(14.dp),
        color = DarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar initial / icon
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (payableAmount > 0) Color(0xFFFF9800).copy(alpha = 0.15f) else GreenSuccess.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = null,
                        tint = if (payableAmount > 0) Color(0xFFFF9800) else GreenSuccess,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = supplier.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (supplier.companyName.isNotBlank()) {
                        Text(
                            text = supplier.companyName,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xFFFF9800)
                        )
                    }

                    if (supplier.phone.isNotBlank()) {
                        Text(
                            text = supplier.phone,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$currency ${formatMoney(payableAmount)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (payableAmount > 0) Color(0xFFFF9800) else GreenSuccess
                    )
                    Text(
                        text = if (payableAmount > 0) {
                            if (language == AppLanguage.BANGLA) "দেনা বাকি" else "Payable Due"
                        } else {
                            if (language == AppLanguage.BANGLA) "পরিশোধিত" else "Cleared"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (payableAmount > 0) Color(0xFFFF9800) else GreenSuccess
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (supplier.phone.isNotBlank()) {
                        IconButton(onClick = onCall, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Call, contentDescription = "Call", tint = GreenSuccess, modifier = Modifier.size(16.dp))
                        }
                    }

                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RedAccent.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onViewDetails,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(if (language == AppLanguage.BANGLA) "হিসাব" else "History", fontSize = 11.sp)
                    }

                    if (payableAmount > 0) {
                        Button(
                            onClick = onRecordPayment,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (language == AppLanguage.BANGLA) "পরিশোধ" else "Pay", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditSupplierDialog(
    supplierToEdit: Supplier?,
    currency: String,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (name: String, companyName: String, phone: String, address: String, openingBalance: Double, note: String) -> Unit
) {
    var name by remember { mutableStateOf(supplierToEdit?.name ?: "") }
    var companyName by remember { mutableStateOf(supplierToEdit?.companyName ?: "") }
    var phone by remember { mutableStateOf(supplierToEdit?.phone ?: "") }
    var address by remember { mutableStateOf(supplierToEdit?.address ?: "") }
    var openingBalanceText by remember {
        mutableStateOf(supplierToEdit?.let { if (it.openingBalance > 0) it.openingBalance.toString() else "" } ?: "")
    }
    var note by remember { mutableStateOf(supplierToEdit?.note ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (supplierToEdit != null) {
                    if (language == AppLanguage.BANGLA) "সাপ্লায়ার সম্পাদনা" else "Edit Supplier"
                } else {
                    if (language == AppLanguage.BANGLA) "নতুন সাপ্লায়ার যোগ করুন" else "Add New Supplier"
                },
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (language == AppLanguage.BANGLA) "সাপ্লায়ার / মহাজনের নাম *" else "Supplier Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFF9800),
                        unfocusedBorderColor = DarkBorder,
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant
                    )
                )

                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text(if (language == AppLanguage.BANGLA) "কোম্পানি / প্রতিষ্ঠান" else "Company / Business Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFF9800),
                        unfocusedBorderColor = DarkBorder,
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant
                    )
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(if (language == AppLanguage.BANGLA) "মোবাইল নম্বর" else "Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFF9800),
                        unfocusedBorderColor = DarkBorder,
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant
                    )
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(if (language == AppLanguage.BANGLA) "ঠিকানা" else "Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFF9800),
                        unfocusedBorderColor = DarkBorder,
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant
                    )
                )

                if (supplierToEdit == null) {
                    OutlinedTextField(
                        value = openingBalanceText,
                        onValueChange = { openingBalanceText = it },
                        label = { Text("${if (language == AppLanguage.BANGLA) "পূর্বের দেনা (Opening Payable)" else "Opening Payable"} ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF9800),
                            unfocusedBorderColor = DarkBorder,
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant
                        )
                    )
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(if (language == AppLanguage.BANGLA) "নোট / মন্তব্য" else "Notes / Remarks") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFF9800),
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
                    if (name.isNotBlank()) {
                        val ob = openingBalanceText.toDoubleOrNull() ?: 0.0
                        onSave(name, companyName, phone, address, ob, note)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
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

@Composable
fun SupplierDetailsDialog(
    supplier: Supplier,
    payableAmount: Double,
    transactions: List<BusinessTransaction>,
    currency: String,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onRecordPayment: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(supplier.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    if (supplier.companyName.isNotBlank()) {
                        Text(supplier.companyName, style = MaterialTheme.typography.bodySmall, color = Color(0xFFFF9800))
                    }
                }
                Text(
                    text = "$currency ${formatMoney(payableAmount)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (payableAmount > 0) Color(0xFFFF9800) else GreenSuccess
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (supplier.phone.isNotBlank()) {
                    Text(
                        text = "${if (language == AppLanguage.BANGLA) "মোবাইল:" else "Phone:"} ${supplier.phone}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (supplier.openingBalance > 0) {
                    Text(
                        text = "${if (language == AppLanguage.BANGLA) "পূর্বের দেনা (Opening Balance):" else "Opening Balance:"} $currency ${formatMoney(supplier.openingBalance)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFF9800)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = if (language == AppLanguage.BANGLA) "ক্রয় ও পেমেন্ট খতিয়ান (${transactions.size})" else "Purchase & Payment Ledger (${transactions.size})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                if (transactions.isEmpty()) {
                    Text(
                        text = if (language == AppLanguage.BANGLA) "কোনো রেকর্ড পাওয়া যায়নি" else "No transactions recorded yet",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(transactions, key = { it.id }) { item ->
                            val isPurchase = item.type == "PURCHASE"
                            val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(item.timestamp))

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = DarkSurfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = if (isPurchase) (if (language == AppLanguage.BANGLA) "ক্রয়: ${item.title}" else "Purchase: ${item.title}") else (if (language == AppLanguage.BANGLA) "পরিশোধ / পেমেন্ট" else "Supplier Payment"),
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = if (isPurchase) MaterialTheme.colorScheme.onSurface else Color(0xFFFF9800)
                                        )
                                        Text(dateStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        if (isPurchase && item.dueAmount > 0) {
                                            Text(
                                                "${if (language == AppLanguage.BANGLA) "দেনা বাকি:" else "Unpaid Due:"} $currency ${formatMoney(item.dueAmount)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFFFF9800)
                                            )
                                        }
                                    }

                                    Text(
                                        text = "$currency ${formatMoney(item.amount)}",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isPurchase) MaterialTheme.colorScheme.onSurface else Color(0xFFFF9800)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (payableAmount > 0) {
                Button(
                    onClick = onRecordPayment,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                ) {
                    Text(if (language == AppLanguage.BANGLA) "দেনা পরিশোধ করুন" else "Pay Supplier")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text(if (language == AppLanguage.BANGLA) "বন্ধ করুন" else "Close")
                }
            }
        },
        dismissButton = {
            if (payableAmount > 0) {
                TextButton(onClick = onDismiss) {
                    Text(Strings.cancel(language))
                }
            }
        },
        containerColor = DarkSurfaceElevated
    )
}
