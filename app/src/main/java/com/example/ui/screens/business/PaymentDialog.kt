package com.example.ui.screens.business

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.business.Customer
import com.example.data.business.Supplier
import com.example.localization.AppLanguage
import com.example.localization.Strings
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.RedAccent

enum class PaymentPartyType {
    CUSTOMER,
    SUPPLIER
}

@Composable
fun RecordPaymentDialog(
    initialType: PaymentPartyType,
    initialCustomer: Customer? = null,
    initialSupplier: Supplier? = null,
    initialAmount: Double = 0.0,
    customers: List<Customer>,
    suppliers: List<Supplier>,
    customerDuesMap: Map<Customer, Double>,
    supplierPayablesMap: Map<Supplier, Double>,
    currency: String,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSaveCustomerPayment: (customer: Customer, amount: Double, note: String) -> Unit,
    onSaveSupplierPayment: (supplier: Supplier, amount: Double, note: String) -> Unit
) {
    var partyType by remember { mutableStateOf(initialType) }

    var selectedCustomer by remember {
        mutableStateOf(initialCustomer ?: customers.firstOrNull())
    }
    var selectedSupplier by remember {
        mutableStateOf(initialSupplier ?: suppliers.firstOrNull())
    }

    val currentDueOrPayable: Double = remember(partyType, selectedCustomer, selectedSupplier, customerDuesMap, supplierPayablesMap) {
        if (partyType == PaymentPartyType.CUSTOMER) {
            selectedCustomer?.let { customerDuesMap[it] } ?: 0.0
        } else {
            selectedSupplier?.let { supplierPayablesMap[it] } ?: 0.0
        }
    }

    var amountText by remember {
        mutableStateOf(
            if (initialAmount > 0) initialAmount.toString()
            else if (currentDueOrPayable > 0) currentDueOrPayable.toString()
            else ""
        )
    }

    var note by remember { mutableStateOf("") }
    var customerDropdownExpanded by remember { mutableStateOf(false) }
    var supplierDropdownExpanded by remember { mutableStateOf(false) }

    val amountVal = amountText.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (partyType == PaymentPartyType.CUSTOMER) {
                    if (language == AppLanguage.BANGLA) "কাস্টমার বাকি আদায় (Cash In)" else "Receive Customer Payment"
                } else {
                    if (language == AppLanguage.BANGLA) "মহাজন দেনা পরিশোধ (Cash Out)" else "Pay Supplier"
                },
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Customer or Supplier Selector
                if (partyType == PaymentPartyType.CUSTOMER) {
                    if (customers.isEmpty()) {
                        Text(
                            text = if (language == AppLanguage.BANGLA) "কোনো কাস্টমার নিবন্ধিত নেই। আগে কাস্টমার যোগ করুন।" else "No registered customers found. Please add a customer first.",
                            color = RedAccent,
                            fontSize = 13.sp
                        )
                    } else {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { customerDropdownExpanded = true }
                                .padding(vertical = 4.dp),
                            color = DarkSurfaceVariant,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = GreenSuccess)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = selectedCustomer?.name ?: (if (language == AppLanguage.BANGLA) "কাস্টমার নির্বাচন করুন" else "Select Customer"),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (selectedCustomer?.phone?.isNotBlank() == true) {
                                            Text(selectedCustomer!!.phone, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)

                                DropdownMenu(
                                    expanded = customerDropdownExpanded,
                                    onDismissRequest = { customerDropdownExpanded = false },
                                    containerColor = DarkSurfaceElevated
                                ) {
                                    customers.forEach { cust ->
                                        val custDue = customerDuesMap[cust] ?: 0.0
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Column {
                                                        Text(cust.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                                                        if (cust.phone.isNotBlank()) {
                                                            Text(cust.phone, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                        }
                                                    }
                                                    Text(
                                                        "$currency ${formatMoney(custDue)}",
                                                        color = if (custDue > 0) RedAccent else GreenSuccess,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp
                                                    )
                                                }
                                            },
                                            onClick = {
                                                selectedCustomer = cust
                                                val due = customerDuesMap[cust] ?: 0.0
                                                if (due > 0) amountText = due.toString()
                                                customerDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (suppliers.isEmpty()) {
                        Text(
                            text = if (language == AppLanguage.BANGLA) "কোনো সাপ্লায়ার নিবন্ধিত নেই।" else "No registered suppliers found.",
                            color = RedAccent,
                            fontSize = 13.sp
                        )
                    } else {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { supplierDropdownExpanded = true }
                                .padding(vertical = 4.dp),
                            color = DarkSurfaceVariant,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Storefront, contentDescription = null, tint = Color(0xFFFF9800))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = selectedSupplier?.name ?: (if (language == AppLanguage.BANGLA) "সাপ্লায়ার নির্বাচন করুন" else "Select Supplier"),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (selectedSupplier?.companyName?.isNotBlank() == true) {
                                            Text(selectedSupplier!!.companyName, style = MaterialTheme.typography.labelSmall, color = Color(0xFFFF9800))
                                        }
                                    }
                                }
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)

                                DropdownMenu(
                                    expanded = supplierDropdownExpanded,
                                    onDismissRequest = { supplierDropdownExpanded = false },
                                    containerColor = DarkSurfaceElevated
                                ) {
                                    suppliers.forEach { supp ->
                                        val suppPayable = supplierPayablesMap[supp] ?: 0.0
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Column {
                                                        Text(supp.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                                                        if (supp.companyName.isNotBlank()) {
                                                            Text(supp.companyName, fontSize = 11.sp, color = Color(0xFFFF9800))
                                                        }
                                                    }
                                                    Text(
                                                        "$currency ${formatMoney(suppPayable)}",
                                                        color = if (suppPayable > 0) Color(0xFFFF9800) else GreenSuccess,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp
                                                    )
                                                }
                                            },
                                            onClick = {
                                                selectedSupplier = supp
                                                val payable = supplierPayablesMap[supp] ?: 0.0
                                                if (payable > 0) amountText = payable.toString()
                                                supplierDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Balance indicator
                if (currentDueOrPayable > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (partyType == PaymentPartyType.CUSTOMER) RedAccent.copy(alpha = 0.15f) else Color(0xFFFF9800).copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (partyType == PaymentPartyType.CUSTOMER) {
                                    if (language == AppLanguage.BANGLA) "বর্তমানে বাকি আছে:" else "Current Due:"
                                } else {
                                    if (language == AppLanguage.BANGLA) "বর্তমানে দেনা আছে:" else "Current Payable:"
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$currency ${formatMoney(currentDueOrPayable)}",
                                fontWeight = FontWeight.Bold,
                                color = if (partyType == PaymentPartyType.CUSTOMER) RedAccent else Color(0xFFFF9800),
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Amount Text Field & Full button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("${if (language == AppLanguage.BANGLA) "পেমেন্ট টাকা" else "Payment Amount"} ($currency) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (partyType == PaymentPartyType.CUSTOMER) GreenSuccess else Color(0xFFFF9800),
                            unfocusedBorderColor = DarkBorder,
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant
                        )
                    )

                    if (currentDueOrPayable > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        OutlinedButton(
                            onClick = { amountText = currentDueOrPayable.toString() },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(if (language == AppLanguage.BANGLA) "পুরোটা" else "Full", fontSize = 12.sp)
                        }
                    }
                }

                // Remaining balance calculation
                if (currentDueOrPayable > 0 && amountVal > 0) {
                    val remaining = (currentDueOrPayable - amountVal).coerceAtLeast(0.0)
                    Text(
                        text = "${if (language == AppLanguage.BANGLA) "পেমেন্টের পর বাকি থাকবে:" else "Balance after payment:"} $currency ${formatMoney(remaining)}",
                        fontSize = 12.sp,
                        color = if (remaining > 0) MaterialTheme.colorScheme.onSurfaceVariant else GreenSuccess,
                        fontWeight = if (remaining == 0.0) FontWeight.Bold else FontWeight.Normal
                    )
                }

                // Note / Method
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(if (language == AppLanguage.BANGLA) "পেমেন্ট মাধ্যম / বিবরণ (যেমন: ক্যাশ, বিকাশ)" else "Payment Note / Method (e.g. Cash, Card)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (partyType == PaymentPartyType.CUSTOMER) GreenSuccess else Color(0xFFFF9800),
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
                    if (amountVal > 0) {
                        if (partyType == PaymentPartyType.CUSTOMER && selectedCustomer != null) {
                            onSaveCustomerPayment(selectedCustomer!!, amountVal, note)
                        } else if (partyType == PaymentPartyType.SUPPLIER && selectedSupplier != null) {
                            onSaveSupplierPayment(selectedSupplier!!, amountVal, note)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (partyType == PaymentPartyType.CUSTOMER) GreenSuccess else Color(0xFFFF9800)
                ),
                enabled = amountVal > 0 && ((partyType == PaymentPartyType.CUSTOMER && selectedCustomer != null) || (partyType == PaymentPartyType.SUPPLIER && selectedSupplier != null))
            ) {
                Text(Strings.confirm(language))
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
