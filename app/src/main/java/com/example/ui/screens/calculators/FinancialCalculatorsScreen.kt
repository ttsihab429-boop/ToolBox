package com.example.ui.screens.calculators

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.PreferencesManager
import com.example.localization.AppLanguage
import com.example.ui.components.PrimaryActionButton
import com.example.ui.components.SecondaryActionButton
import com.example.ui.components.ToolBoxTopBar
import com.example.ui.components.ToolInputField
import com.example.ui.components.ToolResultCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.RedAccent
import java.text.DecimalFormat

@Composable
fun FinancialCalculatorsScreen(
    initialToolId: String,
    language: AppLanguage,
    prefs: PreferencesManager,
    onBackClick: () -> Unit
) {
    var activeTool by remember { mutableStateOf(initialToolId) }
    val isBn = language == AppLanguage.BANGLA
    val df = remember { DecimalFormat("#,##0.00") }

    Scaffold(
        topBar = {
            ToolBoxTopBar(
                title = when (activeTool) {
                    "pct_calc" -> if (isBn) "শতকরা ক্যালকুলেটর" else "Percentage Calculator"
                    "discount_calc" -> if (isBn) "ছাড় / ডিসকাউন্ট" else "Discount Calculator"
                    "profit_loss_calc" -> if (isBn) "লাভ ও ক্ষতি" else "Profit & Loss Calculator"
                    "gst_vat_calc" -> if (isBn) "ভ্যাট / ট্যাক্স" else "GST / VAT Calculator"
                    "tip_calc" -> if (isBn) "টিপ ও বিল ভাগ" else "Tip & Bill Split"
                    else -> "Financial"
                },
                onBackClick = onBackClick,
                isFavorite = prefs.isFavorite(activeTool),
                onFavoriteToggle = { prefs.toggleFavorite(activeTool) }
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Quick Subtool selector tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val tools = listOf(
                    "pct_calc" to (if (isBn) "শতকরা" else "Percentage"),
                    "discount_calc" to (if (isBn) "ডিসকাউন্ট" else "Discount"),
                    "profit_loss_calc" to (if (isBn) "লাভ-ক্ষতি" else "Profit/Loss"),
                    "gst_vat_calc" to (if (isBn) "ভ্যাট/ট্যাক্স" else "VAT/GST"),
                    "tip_calc" to (if (isBn) "টিপ" else "Tip")
                )
                tools.forEach { (id, label) ->
                    val isSelected = activeTool == id
                    FilterChip(
                        selected = isSelected,
                        onClick = { activeTool = id },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RedAccent,
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White,
                            containerColor = DarkSurfaceElevated,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (activeTool) {
                "pct_calc" -> PercentageCalculatorContent(isBn, df)
                "discount_calc" -> DiscountCalculatorContent(isBn, df)
                "profit_loss_calc" -> ProfitLossCalculatorContent(isBn, df)
                "gst_vat_calc" -> GstVatCalculatorContent(isBn, df)
                "tip_calc" -> TipCalculatorContent(isBn, df)
            }
        }
    }
}

@Composable
fun PercentageCalculatorContent(isBn: Boolean, df: DecimalFormat) {
    var mode by remember { mutableIntStateOf(0) } // 0: X% of Y, 1: X is what % of Y, 2: % change
    var valA by remember { mutableStateOf("") }
    var valB by remember { mutableStateOf("") }

    val result by remember {
        derivedStateOf {
            val a = valA.toDoubleOrNull()
            val b = valB.toDoubleOrNull()
            if (a != null && b != null) {
                when (mode) {
                    0 -> {
                        val res = (a / 100.0) * b
                        df.format(res)
                    }
                    1 -> {
                        if (b != 0.0) {
                            val res = (a / b) * 100.0
                            "${df.format(res)}%"
                        } else "Cannot divide by 0"
                    }
                    2 -> {
                        if (a != 0.0) {
                            val diff = b - a
                            val pct = (diff / a) * 100.0
                            val sign = if (pct >= 0) "+" else ""
                            "$sign${df.format(pct)}% (${if (pct >= 0) "Increase" else "Decrease"})"
                        } else "Initial value cannot be 0"
                    }
                    else -> ""
                }
            } else ""
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = mode == 0,
            onClick = { mode = 0 },
            label = { Text(if (isBn) "X% of Y" else "X% of Y") },
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RedAccent)
        )
        FilterChip(
            selected = mode == 1,
            onClick = { mode = 1 },
            label = { Text(if (isBn) "X is what % of Y" else "X is what % of Y") },
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RedAccent)
        )
        FilterChip(
            selected = mode == 2,
            onClick = { mode = 2 },
            label = { Text(if (isBn) "% পরিবর্তন" else "% Change") },
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RedAccent)
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    ToolInputField(
        value = valA,
        onValueChange = { valA = it },
        label = when (mode) {
            0 -> if (isBn) "শতকরা হার (%)" else "Percentage (%)"
            1 -> if (isBn) "অংশ (Part X)" else "Part (X)"
            else -> if (isBn) "প্রারম্ভিক মান (From)" else "Initial Value (From)"
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )

    Spacer(modifier = Modifier.height(12.dp))

    ToolInputField(
        value = valB,
        onValueChange = { valB = it },
        label = when (mode) {
            0 -> if (isBn) "মোট মান (Of Y)" else "Total Value (Of Y)"
            1 -> if (isBn) "পুরো মান (Whole Y)" else "Whole (Y)"
            else -> if (isBn) "চূড়ান্ত মান (To)" else "Final Value (To)"
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )

    Spacer(modifier = Modifier.height(16.dp))

    ToolResultCard(
        resultValue = result,
        title = if (isBn) "হিসাবের ফলাফল" else "Percentage Result"
    )

    Spacer(modifier = Modifier.height(16.dp))

    SecondaryActionButton(
        text = if (isBn) "ক্লিয়ার" else "Clear",
        onClick = { valA = ""; valB = "" }
    )
}

@Composable
fun DiscountCalculatorContent(isBn: Boolean, df: DecimalFormat) {
    var originalPrice by remember { mutableStateOf("") }
    var discountPercent by remember { mutableStateOf("") }
    var additionalDiscount by remember { mutableStateOf("") }

    val p = originalPrice.toDoubleOrNull() ?: 0.0
    val d = discountPercent.toDoubleOrNull() ?: 0.0
    val extra = additionalDiscount.toDoubleOrNull() ?: 0.0

    val savings = (p * (d / 100.0)) + ((p - (p * (d / 100.0))) * (extra / 100.0))
    val finalPrice = (p - savings).coerceAtLeast(0.0)

    ToolInputField(
        value = originalPrice,
        onValueChange = { originalPrice = it },
        label = if (isBn) "মূল দাম (Original Price)" else "Original Price",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
    Spacer(modifier = Modifier.height(12.dp))
    ToolInputField(
        value = discountPercent,
        onValueChange = { discountPercent = it },
        label = if (isBn) "ছাড়ের হার (%)" else "Discount (%)",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
    Spacer(modifier = Modifier.height(12.dp))
    ToolInputField(
        value = additionalDiscount,
        onValueChange = { additionalDiscount = it },
        label = if (isBn) "অতিরিক্ত কুপন/ছাড় (%) [ঐচ্ছিক]" else "Extra Coupon / Off (%) [Optional]",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )

    Spacer(modifier = Modifier.height(16.dp))

    ToolResultCard(
        resultValue = if (p > 0) df.format(finalPrice) else "",
        title = if (isBn) "চূড়ান্ত মূল্য (Final Price)" else "Final Price to Pay",
        subtitle = if (p > 0) "${if (isBn) "মোট সাশ্রয়" else "You save"}: ${df.format(savings)}" else null
    )

    Spacer(modifier = Modifier.height(16.dp))
    SecondaryActionButton(
        text = if (isBn) "ক্লিয়ার" else "Clear",
        onClick = { originalPrice = ""; discountPercent = ""; additionalDiscount = "" }
    )
}

@Composable
fun ProfitLossCalculatorContent(isBn: Boolean, df: DecimalFormat) {
    var costPrice by remember { mutableStateOf("") }
    var sellingPrice by remember { mutableStateOf("") }

    val cp = costPrice.toDoubleOrNull() ?: 0.0
    val sp = sellingPrice.toDoubleOrNull() ?: 0.0

    val diff = sp - cp
    val pct = if (cp > 0) (diff / cp) * 100.0 else 0.0

    ToolInputField(
        value = costPrice,
        onValueChange = { costPrice = it },
        label = if (isBn) "ক্রয়মূল্য (Cost Price)" else "Cost Price",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
    Spacer(modifier = Modifier.height(12.dp))
    ToolInputField(
        value = sellingPrice,
        onValueChange = { sellingPrice = it },
        label = if (isBn) "বিক্রয়মূল্য (Selling Price)" else "Selling Price",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )

    Spacer(modifier = Modifier.height(16.dp))

    val status = when {
        diff > 0 -> if (isBn) "লাভ (Profit)" else "Profit"
        diff < 0 -> if (isBn) "ক্ষতি (Loss)" else "Loss"
        else -> if (isBn) "সমান সমান" else "Break Even"
    }

    ToolResultCard(
        resultValue = if (cp > 0 && sp > 0) "${df.format(Math.abs(diff))} ($status)" else "",
        title = if (isBn) "লাভ/ক্ষতির হিসাব" else "Profit / Loss Summary",
        subtitle = if (cp > 0 && sp > 0) "${if (isBn) "হার" else "Margin / Ratio"}: ${df.format(pct)}%" else null
    )

    Spacer(modifier = Modifier.height(16.dp))
    SecondaryActionButton(
        text = if (isBn) "ক্লিয়ার" else "Clear",
        onClick = { costPrice = ""; sellingPrice = "" }
    )
}

@Composable
fun GstVatCalculatorContent(isBn: Boolean, df: DecimalFormat) {
    var amount by remember { mutableStateOf("") }
    var vatRate by remember { mutableStateOf("15") } // Default 15% VAT in Bangladesh
    var isAddVat by remember { mutableStateOf(true) }

    val amt = amount.toDoubleOrNull() ?: 0.0
    val rate = vatRate.toDoubleOrNull() ?: 0.0

    val vatAmount = if (isAddVat) {
        amt * (rate / 100.0)
    } else {
        amt - (amt / (1.0 + (rate / 100.0)))
    }
    val totalAmount = if (isAddVat) amt + vatAmount else amt - vatAmount

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = isAddVat,
            onClick = { isAddVat = true },
            label = { Text(if (isBn) "ভ্যাট যোগ করুন" else "Add VAT (+)") },
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RedAccent)
        )
        FilterChip(
            selected = !isAddVat,
            onClick = { isAddVat = false },
            label = { Text(if (isBn) "ভ্যাট বাদ দিন" else "Remove / Extract VAT (-)") },
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RedAccent)
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    ToolInputField(
        value = amount,
        onValueChange = { amount = it },
        label = if (isBn) "টাকার পরিমাণ (Amount)" else "Base Amount",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
    Spacer(modifier = Modifier.height(12.dp))
    ToolInputField(
        value = vatRate,
        onValueChange = { vatRate = it },
        label = if (isBn) "ভ্যাটের হার (%)" else "VAT / Tax Rate (%)",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )

    Spacer(modifier = Modifier.height(16.dp))

    ToolResultCard(
        resultValue = if (amt > 0) df.format(totalAmount) else "",
        title = if (isAddVat) (if (isBn) "মোট প্রদেয় মূল্য" else "Total Price with VAT")
        else (if (isBn) "ভ্যাট ছাড়া নিট মূল্য" else "Net Price without VAT"),
        subtitle = if (amt > 0) "${if (isBn) "ভ্যাট পরিমাণ" else "VAT Component"}: ${df.format(vatAmount)}" else null
    )

    Spacer(modifier = Modifier.height(16.dp))
    SecondaryActionButton(
        text = if (isBn) "ক্লিয়ার" else "Clear",
        onClick = { amount = "" }
    )
}

@Composable
fun TipCalculatorContent(isBn: Boolean, df: DecimalFormat) {
    var billAmount by remember { mutableStateOf("") }
    var tipPercent by remember { mutableStateOf("10") }
    var splitCount by remember { mutableStateOf("1") }

    val bill = billAmount.toDoubleOrNull() ?: 0.0
    val tip = tipPercent.toDoubleOrNull() ?: 0.0
    val people = splitCount.toIntOrNull()?.coerceAtLeast(1) ?: 1

    val tipTotal = bill * (tip / 100.0)
    val grandTotal = bill + tipTotal
    val totalPerPerson = grandTotal / people
    val tipPerPerson = tipTotal / people

    ToolInputField(
        value = billAmount,
        onValueChange = { billAmount = it },
        label = if (isBn) "মোট বিলের পরিমাণ" else "Total Bill Amount",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
    Spacer(modifier = Modifier.height(12.dp))
    ToolInputField(
        value = tipPercent,
        onValueChange = { tipPercent = it },
        label = if (isBn) "টিপের হার (%)" else "Tip Percentage (%)",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
    Spacer(modifier = Modifier.height(12.dp))
    ToolInputField(
        value = splitCount,
        onValueChange = { splitCount = it },
        label = if (isBn) "মানুষের সংখ্যা (Split Among)" else "Split Between (People)",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )

    Spacer(modifier = Modifier.height(16.dp))

    ToolResultCard(
        resultValue = if (bill > 0) df.format(totalPerPerson) else "",
        title = if (isBn) "জনপ্রতি প্রদেয় টাকা" else "Total Per Person",
        subtitle = if (bill > 0) "${if (isBn) "জনপ্রতি টিপ" else "Tip per person"}: ${df.format(tipPerPerson)} | ${if (isBn) "মোট বিল" else "Grand Total"}: ${df.format(grandTotal)}" else null
    )

    Spacer(modifier = Modifier.height(16.dp))
    SecondaryActionButton(
        text = if (isBn) "ক্লিয়ার" else "Clear",
        onClick = { billAmount = ""; splitCount = "1" }
    )
}
