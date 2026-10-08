package com.example.ui.screens.bangladesh

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.PreferencesManager
import com.example.localization.AppLanguage
import com.example.ui.components.SecondaryActionButton
import com.example.ui.components.ToolBoxTopBar
import com.example.ui.components.ToolInputField
import com.example.ui.components.ToolResultCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.RedAccent
import java.text.DecimalFormat

@Composable
fun BdUnitsScreen(
    language: AppLanguage,
    prefs: PreferencesManager,
    onBackClick: () -> Unit
) {
    val isBn = language == AppLanguage.BANGLA
    val df = remember { DecimalFormat("#,##0.00") }
    var mode by remember { mutableStateOf(0) } // 0: Mon/Ser Weight, 1: Gold Vori/Tola, 2: Price per Mon to kg

    Scaffold(
        topBar = {
            ToolBoxTopBar(
                title = if (isBn) "মণ, সের, তোলা ও স্বর্ণের দর" else "BD Traditional Units & Rates",
                onBackClick = onBackClick,
                isFavorite = prefs.isFavorite("bd_units"),
                onFavoriteToggle = { prefs.toggleFavorite("bd_units") }
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    0 to if (isBn) "মণ ↔ কেজি" else "Mon / Ser ↔ Kg",
                    1 to if (isBn) "ভরি/তোলা (স্বর্ণ)" else "Gold Tola (ভরি)",
                    2 to if (isBn) "মণ দর → কেজি দর" else "Price Calculator"
                ).forEach { (m, label) ->
                    FilterChip(
                        selected = mode == m,
                        onClick = { mode = m },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RedAccent,
                            containerColor = DarkSurfaceElevated
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (mode) {
                0 -> {
                    var monCount by remember { mutableStateOf("1") }
                    val mon = monCount.toDoubleOrNull() ?: 0.0
                    val kg = mon * 37.3242
                    val ser = mon * 40.0

                    ToolInputField(
                        value = monCount,
                        onValueChange = { monCount = it },
                        label = if (isBn) "মণের পরিমাণ লিখুন" else "Enter Mon (মণ)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ToolResultCard(
                        resultValue = "${df.format(kg)} kg (কেজি)",
                        title = if (isBn) "কেজিতে ওজন" else "Weight in Kilograms",
                        subtitle = "${df.format(ser)} Ser (সের) [১ মণ = ৪০ সের = ৩৭.৩২৪২ কেজি]"
                    )
                }

                1 -> {
                    var voriCount by remember { mutableStateOf("1") }
                    var pricePerVori by remember { mutableStateOf("125000") }

                    val vori = voriCount.toDoubleOrNull() ?: 0.0
                    val rate = pricePerVori.toDoubleOrNull() ?: 0.0

                    val grams = vori * 11.664
                    val totalPrice = vori * rate
                    val pricePerGram = if (grams > 0) totalPrice / grams else 0.0

                    ToolInputField(
                        value = voriCount,
                        onValueChange = { voriCount = it },
                        label = if (isBn) "ভরি / তোলা সংখ্যা" else "Enter Vori / Tola (ভরি)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ToolInputField(
                        value = pricePerVori,
                        onValueChange = { pricePerVori = it },
                        label = if (isBn) "প্রতি ভরি স্বর্ণের দাম (৳)" else "Gold Price per Vori (৳)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ToolResultCard(
                        resultValue = "৳ ${df.format(totalPrice)}",
                        title = if (isBn) "মোট স্বর্ণের মূল্য" else "Total Gold Price",
                        subtitle = "মোট ওজন: ${df.format(grams)} গ্রাম (১ ভরি = ১১.৬৬৪ গ্রাম = ১৬ আনা = ৯৬ রতি)\nপ্রতি গ্রামের দর: ৳ ${df.format(pricePerGram)}"
                    )
                }

                2 -> {
                    var pricePerMon by remember { mutableStateOf("2000") }
                    val price = pricePerMon.toDoubleOrNull() ?: 0.0
                    val perKg = if (price > 0) price / 37.3242 else 0.0
                    val perSer = if (price > 0) price / 40.0 else 0.0

                    ToolInputField(
                        value = pricePerMon,
                        onValueChange = { pricePerMon = it },
                        label = if (isBn) "প্রতি মণের দর (৳)" else "Price per Mon (৳)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ToolResultCard(
                        resultValue = "৳ ${df.format(perKg)} / kg",
                        title = if (isBn) "প্রতি কেজির দর" else "Price per Kilogram",
                        subtitle = "প্রতি সেরের দর: ৳ ${df.format(perSer)}"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SecondaryActionButton(
                text = if (isBn) "রিসেট" else "Reset",
                onClick = { mode = 0 }
            )
        }
    }
}
