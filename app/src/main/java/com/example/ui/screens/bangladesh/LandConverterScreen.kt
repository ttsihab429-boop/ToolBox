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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.PreferencesManager
import com.example.localization.AppLanguage
import com.example.ui.components.SecondaryActionButton
import com.example.ui.components.ToolActions
import com.example.ui.components.ToolBoxTopBar
import com.example.ui.components.ToolInputField
import com.example.ui.components.ToolResultCard
import com.example.ui.screens.converters.UnitDef
import com.example.ui.screens.converters.UnitDropdownPicker
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.RedAccent
import java.text.DecimalFormat

@Composable
fun LandConverterScreen(
    language: AppLanguage,
    prefs: PreferencesManager,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val isBn = language == AppLanguage.BANGLA
    val df = remember { DecimalFormat("#,##0.####") }

    // Base unit: Square Feet
    val landUnits = remember {
        listOf(
            UnitDef("shatak", "Shatak / Decimal (শতাংশ)", "শতাংশ / ডেসিমেল", 435.6),
            UnitDef("katha", "Katha (কাঠা)", "কাঠা (১.৬৫ শতাংশ)", 720.0), // Standard 720 sq ft
            UnitDef("bigha", "Bigha (বিঘা)", "বিঘা (২০ কাঠা = ৩৩ শতাংশ)", 14400.0),
            UnitDef("sq_ft", "Square Feet (বর্গফুট)", "বর্গফুট", 1.0),
            UnitDef("sq_meter", "Square Meter (বর্গমিটার)", "বর্গমিটার", 10.7639),
            UnitDef("acre", "Acre (একর)", "একর (১০০ শতাংশ)", 43560.0),
            UnitDef("kani", "Kani (কানি)", "কানি (১৬ গণ্ডা)", 17280.0),
            UnitDef("gonda", "Gonda (গণ্ডা)", "গণ্ডা (৪ কড়া)", 864.0)
        )
    }

    var fromUnit by remember { mutableStateOf(landUnits[0]) } // Shatak
    var toUnit by remember { mutableStateOf(landUnits[1]) } // Katha
    var inputValue by remember { mutableStateOf("10") }

    val convertedValue by remember(inputValue, fromUnit, toUnit) {
        derivedStateOf {
            val num = inputValue.toDoubleOrNull() ?: return@derivedStateOf ""
            val sqFt = num * fromUnit.factorToBase
            val res = sqFt / toUnit.factorToBase
            "${df.format(res)} ${if (isBn) toUnit.nameBn else toUnit.nameEn}"
        }
    }

    Scaffold(
        topBar = {
            ToolBoxTopBar(
                title = if (isBn) "জমি পরিমাপ কনভার্টার" else "BD Land Measurement",
                onBackClick = onBackClick,
                isFavorite = prefs.isFavorite("land_converter"),
                onFavoriteToggle = { prefs.toggleFavorite("land_converter") }
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
            Text(
                text = if (isBn) "বাংলাদেশের প্রমিত ভূমি পরিমাপ মান (১ শতাংশ = ৪৩৫.৬ বর্গফুট, ১ কাঠা = ৭২০ বর্গফুট, ১ বিঘা = ১৪,৪০০ বর্গফুট, ১ একর = ১০০ শতাংশ)"
                else "Standard Bangladesh Land Units: 1 Shatak = 435.6 sq ft, 1 Katha = 720 sq ft, 1 Bigha = 20 Katha (33.33 Shatak), 1 Acre = 100 Shatak.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            ToolInputField(
                value = inputValue,
                onValueChange = { inputValue = it },
                label = if (isBn) "জমির পরিমাণ লিখুন" else "Enter Land Area",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UnitDropdownPicker(
                    label = if (isBn) "যে একক থেকে" else "From",
                    selectedUnit = fromUnit,
                    availableUnits = landUnits,
                    isBn = isBn,
                    onSelect = { fromUnit = it },
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        ToolActions.triggerHaptic(context, prefs.hapticEnabled.value)
                        val tmp = fromUnit
                        fromUnit = toUnit
                        toUnit = tmp
                    },
                    modifier = Modifier.padding(horizontal = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.SwapVert, contentDescription = "Swap", tint = RedAccent)
                }

                UnitDropdownPicker(
                    label = if (isBn) "যে এককে রূপান্তর" else "To",
                    selectedUnit = toUnit,
                    availableUnits = landUnits,
                    isBn = isBn,
                    onSelect = { toUnit = it },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            ToolResultCard(
                resultValue = convertedValue,
                title = if (isBn) "রূপান্তরিত জমির পরিমাপ" else "Converted Land Area",
                subtitle = if (inputValue.isNotBlank()) "$inputValue ${fromUnit.nameEn} = $convertedValue" else null
            )

            Spacer(modifier = Modifier.height(16.dp))

            SecondaryActionButton(
                text = if (isBn) "ক্লিয়ার" else "Clear",
                onClick = { inputValue = "" }
            )
        }
    }
}
