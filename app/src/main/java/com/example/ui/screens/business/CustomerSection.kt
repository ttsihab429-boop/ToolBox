package com.example.ui.screens.business

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
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
import com.example.data.business.Customer
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
fun CustomerSection(
    currentBusiness: BusinessProfile,
    businessRepository: BusinessRepository,
    language: AppLanguage,
    prefs: PreferencesManager,
    allTransactions: List<BusinessTransaction>,
    onRecordPayment: (Customer, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currency = currentBusiness.currency

    val customers by businessRepository.getCustomers(currentBusiness.id).collectAsState(initial = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var filterOnlyWithDue by remember { mutableStateOf(false) }

    // Dialog states
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var customerToEdit by remember { mutableStateOf<Customer?>(null) }
    var customerToDelete by remember { mutableStateOf<Customer?>(null) }
    var customerForDetails by remember { mutableStateOf<Customer?>(null) }

    // Computed customer stats map for fast rendering
    val customerDuesMap = remember(customers, allTransactions) {
        customers.associateWith { customer ->
            businessRepository.calculateCustomerDue(customer, allTransactions)
        }
    }

    val totalDues = remember(customerDuesMap) {
        customerDuesMap.values.sum()
    }

    val filteredCustomers = remember(customers, searchQuery, filterOnlyWithDue, customerDuesMap) {
        customers.filter { customer ->
            val matchesQuery = searchQuery.isBlank() ||
                customer.name.contains(searchQuery, ignoreCase = true) ||
                customer.phone.contains(searchQuery, ignoreCase = true)
            val due = customerDuesMap[customer] ?: 0.0
            val matchesDue = !filterOnlyWithDue || due > 0.0
            matchesQuery && matchesDue
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
                        text = if (language == AppLanguage.BANGLA) "কাস্টমারদের মোট বাকি (Receivable)" else "Total Customer Due",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$currency ${formatMoney(totalDues)}",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (totalDues > 0) RedAccent else GreenSuccess
                    )
                    Text(
                        text = "${customers.size} ${if (language == AppLanguage.BANGLA) "জন নিবন্ধিত কাস্টমার" else "Registered Customers"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = {
                        customerToEdit = null
                        showAddCustomerDialog = true
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                    modifier = Modifier.testTag("add_customer_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (language == AppLanguage.BANGLA) "কাস্টমার" else "Add", fontWeight = FontWeight.Bold)
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
                        if (language == AppLanguage.BANGLA) "নাম বা মোবাইল দিয়ে খুঁজুন…" else "Search name or phone…",
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
                    .testTag("search_customer_field"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RedAccent,
                    unfocusedBorderColor = DarkBorder,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface
                )
            )

            FilterChip(
                selected = filterOnlyWithDue,
                onClick = { filterOnlyWithDue = !filterOnlyWithDue },
                label = { Text(if (language == AppLanguage.BANGLA) "বাকি আছে" else "Due Only", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = RedAccent,
                    containerColor = DarkSurface
                )
            )
        }

        // Customer List
        if (filteredCustomers.isEmpty()) {
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
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (language == AppLanguage.BANGLA) "কোনো কাস্টমার পাওয়া যায়নি" else "No customers found",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (language == AppLanguage.BANGLA) "নতুন কাস্টমার যোগ করতে উপরের '+ কাস্টমার' চাপুন" else "Tap '+ Add' to record customer details and opening dues",
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
                items(filteredCustomers, key = { it.id }) { customer ->
                    val due = customerDuesMap[customer] ?: 0.0

                    CustomerCardItem(
                        customer = customer,
                        dueAmount = due,
                        currency = currency,
                        language = language,
                        onCall = {
                            if (customer.phone.isNotBlank()) {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${customer.phone}")
                                }
                                context.startActivity(intent)
                            }
                        },
                        onRecordPayment = {
                            onRecordPayment(customer, due)
                        },
                        onViewDetails = {
                            customerForDetails = customer
                        },
                        onEdit = {
                            customerToEdit = customer
                            showAddCustomerDialog = true
                        },
                        onDelete = {
                            customerToDelete = customer
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    // Add / Edit Customer Dialog
    if (showAddCustomerDialog) {
        AddEditCustomerDialog(
            customerToEdit = customerToEdit,
            currency = currency,
            language = language,
            onDismiss = { showAddCustomerDialog = false },
            onSave = { name, phone, address, openingBalance, note ->
                scope.launch {
                    businessRepository.saveCustomer(
                        id = customerToEdit?.id ?: 0L,
                        businessId = currentBusiness.id,
                        name = name,
                        phone = phone,
                        address = address,
                        openingBalance = openingBalance,
                        note = note
                    )
                    ToolActions.triggerHaptic(context, prefs.hapticEnabled.value)
                    showAddCustomerDialog = false
                }
            }
        )
    }

    // Delete Customer Confirmation
    if (customerToDelete != null) {
        val target = customerToDelete!!
        AlertDialog(
            onDismissRequest = { customerToDelete = null },
            title = { Text(if (language == AppLanguage.BANGLA) "কাস্টমার মুছবেন?" else "Delete Customer?") },
            text = {
                Text(
                    if (language == AppLanguage.BANGLA)
                        "'${target.name}' এর রেকর্ডটি মুছে ফেলতে চান?"
                    else
                        "Are you sure you want to delete customer '${target.name}'?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            businessRepository.deleteCustomer(target)
                            customerToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedAccent)
                ) {
                    Text(Strings.delete(language))
                }
            },
            dismissButton = {
                TextButton(onClick = { customerToDelete = null }) {
                    Text(Strings.cancel(language))
                }
            },
            containerColor = DarkSurfaceElevated
        )
    }

    // Customer Detail Sheet / Dialog
    if (customerForDetails != null) {
        val target = customerForDetails!!
        val customerTransactions = remember(allTransactions, target.id) {
            allTransactions.filter { it.customerId == target.id }
        }
        val targetDue = customerDuesMap[target] ?: 0.0

        CustomerDetailsDialog(
            customer = target,
            dueAmount = targetDue,
            transactions = customerTransactions,
            currency = currency,
            language = language,
            onDismiss = { customerForDetails = null },
            onRecordPayment = {
                customerForDetails = null
                onRecordPayment(target, targetDue)
            }
        )
    }
}

@Composable
fun CustomerCardItem(
    customer: Customer,
    dueAmount: Double,
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
                // Avatar initial
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (dueAmount > 0) RedAccent.copy(alpha = 0.15f) else GreenSuccess.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = customer.name.firstOrNull()?.uppercase() ?: "C",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (dueAmount > 0) RedAccent else GreenSuccess
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = customer.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (customer.phone.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = customer.phone,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (customer.address.isNotBlank()) {
                        Text(
                            text = customer.address,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$currency ${formatMoney(dueAmount)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (dueAmount > 0) RedAccent else GreenSuccess
                    )
                    Text(
                        text = if (dueAmount > 0) {
                            if (language == AppLanguage.BANGLA) "বাকি পাওনা" else "Outstanding Due"
                        } else {
                            if (language == AppLanguage.BANGLA) "পরিশোধিত" else "Cleared"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (dueAmount > 0) RedAccent.copy(alpha = 0.8f) else GreenSuccess
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
                    if (customer.phone.isNotBlank()) {
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
                        Text(if (language == AppLanguage.BANGLA) "খতিয়ান" else "History", fontSize = 11.sp)
                    }

                    if (dueAmount > 0) {
                        Button(
                            onClick = onRecordPayment,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (language == AppLanguage.BANGLA) "আদায়" else "Pay", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditCustomerDialog(
    customerToEdit: Customer?,
    currency: String,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, address: String, openingBalance: Double, note: String) -> Unit
) {
    var name by remember { mutableStateOf(customerToEdit?.name ?: "") }
    var phone by remember { mutableStateOf(customerToEdit?.phone ?: "") }
    var address by remember { mutableStateOf(customerToEdit?.address ?: "") }
    var openingBalanceText by remember {
        mutableStateOf(customerToEdit?.let { if (it.openingBalance > 0) it.openingBalance.toString() else "" } ?: "")
    }
    var note by remember { mutableStateOf(customerToEdit?.note ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (customerToEdit != null) {
                    if (language == AppLanguage.BANGLA) "কাস্টমার সম্পাদনা" else "Edit Customer"
                } else {
                    if (language == AppLanguage.BANGLA) "নতুন কাস্টমার যোগ করুন" else "Add New Customer"
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
                    label = { Text(if (language == AppLanguage.BANGLA) "কাস্টমারের নাম *" else "Customer Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RedAccent,
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
                        focusedBorderColor = RedAccent,
                        unfocusedBorderColor = DarkBorder,
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant
                    )
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(if (language == AppLanguage.BANGLA) "ঠিকানা / এলাকা" else "Address / Location") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RedAccent,
                        unfocusedBorderColor = DarkBorder,
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant
                    )
                )

                if (customerToEdit == null) {
                    OutlinedTextField(
                        value = openingBalanceText,
                        onValueChange = { openingBalanceText = it },
                        label = { Text("${if (language == AppLanguage.BANGLA) "পূর্বের বাকি (Opening Due)" else "Opening Due Balance"} ($currency)") },
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

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(if (language == AppLanguage.BANGLA) "নোট / মন্তব্য" else "Notes / Remarks") },
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
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val ob = openingBalanceText.toDoubleOrNull() ?: 0.0
                        onSave(name, phone, address, ob, note)
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

@Composable
fun CustomerDetailsDialog(
    customer: Customer,
    dueAmount: Double,
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
                    Text(customer.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    if (customer.phone.isNotBlank()) {
                        Text(customer.phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text(
                    text = "$currency ${formatMoney(dueAmount)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (dueAmount > 0) RedAccent else GreenSuccess
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (customer.address.isNotBlank()) {
                    Text(
                        text = "${if (language == AppLanguage.BANGLA) "ঠিকানা:" else "Address:"} ${customer.address}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (customer.openingBalance > 0) {
                    Text(
                        text = "${if (language == AppLanguage.BANGLA) "পূর্বের বাকি (Opening Balance):" else "Opening Balance:"} $currency ${formatMoney(customer.openingBalance)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = RedAccent
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = if (language == AppLanguage.BANGLA) "লেনদেন খতিয়ান (${transactions.size})" else "Transaction History (${transactions.size})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                if (transactions.isEmpty()) {
                    Text(
                        text = if (language == AppLanguage.BANGLA) "কোনো লেনদেন পাওয়া যায়নি" else "No transactions recorded yet",
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
                            val isSale = item.type == "SALE"
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
                                            text = if (isSale) (if (language == AppLanguage.BANGLA) "বিক্রি: ${item.title}" else "Sale: ${item.title}") else (if (language == AppLanguage.BANGLA) "আদায় / পেমেন্ট" else "Payment Received"),
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = if (isSale) MaterialTheme.colorScheme.onSurface else GreenSuccess
                                        )
                                        Text(dateStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        if (isSale && item.dueAmount > 0) {
                                            Text(
                                                "${if (language == AppLanguage.BANGLA) "বাকি:" else "Due:"} $currency ${formatMoney(item.dueAmount)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = RedAccent
                                            )
                                        }
                                    }

                                    Text(
                                        text = "$currency ${formatMoney(item.amount)}",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isSale) MaterialTheme.colorScheme.onSurface else GreenSuccess
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (dueAmount > 0) {
                Button(
                    onClick = onRecordPayment,
                    colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess)
                ) {
                    Text(if (language == AppLanguage.BANGLA) "বাকি আদায় করুন" else "Receive Payment")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text(if (language == AppLanguage.BANGLA) "বন্ধ করুন" else "Close")
                }
            }
        },
        dismissButton = {
            if (dueAmount > 0) {
                TextButton(onClick = onDismiss) {
                    Text(Strings.cancel(language))
                }
            }
        },
        containerColor = DarkSurfaceElevated
    )
}
