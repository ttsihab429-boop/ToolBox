package com.example.ui.screens.converters

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.PreferencesManager
import com.example.localization.AppLanguage
import com.example.ui.components.SecondaryActionButton
import com.example.ui.components.ToolActions
import com.example.ui.components.ToolBoxTopBar
import com.example.ui.components.ToolInputField
import com.example.ui.components.ToolResultCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.RedAccent
import java.text.DecimalFormat

data class UnitDef(val key: String, val nameEn: String, val nameBn: String, val factorToBase: Double)

enum class UnitCategory(val id: String, val titleEn: String, val titleBn: String) {
    LENGTH("unit_length", "Length", "দৈর্ঘ্য"),
    WEIGHT("unit_weight", "Weight", "ওজন"),
    TEMPERATURE("unit_temp", "Temperature", "তাপমাত্রা"),
    AREA("unit_area", "Area", "ক্ষেত্রফল"),
    VOLUME("unit_volume", "Volume", "আয়তন"),
    SPEED("unit_speed", "Speed", "গতিবেগ"),
    TIME("unit_time", "Time", "সময়"),
    DATA("unit_data", "Data Storage", "ডিজিটাল ডাটা"),
    PRESSURE("unit_pressure", "Pressure", "চাপ"),
    ENERGY("unit_energy", "Energy", "শক্তি")
}

@Composable
fun UnitConverterScreen(
    initialToolId: String,
    language: AppLanguage,
    prefs: PreferencesManager,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val isBn = language == AppLanguage.BANGLA
    var activeCategory by remember {
        mutableStateOf(
            UnitCategory.entries.find { it.id == initialToolId } ?: UnitCategory.LENGTH
        )
    }

    val unitsForCat = remember(activeCategory) { getUnitsForCategory(activeCategory) }
    var fromUnit by remember(activeCategory) { mutableStateOf(unitsForCat.first()) }
    var toUnit by remember(activeCategory) { mutableStateOf(unitsForCat.getOrElse(1) { unitsForCat.first() }) }
    var inputValue by remember { mutableStateOf("1") }

    val resultValue by remember(inputValue, fromUnit, toUnit, activeCategory) {
        derivedStateOf {
            val num = inputValue.toDoubleOrNull() ?: return@derivedStateOf ""
            if (activeCategory == UnitCategory.TEMPERATURE) {
                convertTemperature(num, fromUnit.key, toUnit.key)
            } else {
                val base = num * fromUnit.factorToBase
                val converted = base / toUnit.factorToBase
                val df = DecimalFormat("#,##0.######")
                "${df.format(converted)} ${if (isBn) toUnit.nameBn else toUnit.nameEn}"
            }
        }
    }

    Scaffold(
        topBar = {
            ToolBoxTopBar(
                title = if (isBn) activeCategory.titleBn else activeCategory.titleEn,
                onBackClick = onBackClick,
                isFavorite = prefs.isFavorite(activeCategory.id),
                onFavoriteToggle = { prefs.toggleFavorite(activeCategory.id) }
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
            // Category Chips Row (horizontal scroll via wrapping row)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UnitCategory.entries.take(5).forEach { cat ->
                    val isSelected = activeCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            activeCategory = cat
                            val u = getUnitsForCategory(cat)
                            fromUnit = u.first()
                            toUnit = u.getOrElse(1) { u.first() }
                        },
                        label = { Text(if (isBn) cat.titleBn else cat.titleEn, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RedAccent,
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White,
                            containerColor = DarkSurfaceElevated
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UnitCategory.entries.drop(5).forEach { cat ->
                    val isSelected = activeCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            activeCategory = cat
                            val u = getUnitsForCategory(cat)
                            fromUnit = u.first()
                            toUnit = u.getOrElse(1) { u.first() }
                        },
                        label = { Text(if (isBn) cat.titleBn else cat.titleEn, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RedAccent,
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White,
                            containerColor = DarkSurfaceElevated
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Input Field
            ToolInputField(
                value = inputValue,
                onValueChange = { inputValue = it },
                label = if (isBn) "পরিমাণ লিখুন" else "Enter Value",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // From and To Unit Selectors + Swap
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UnitDropdownPicker(
                    label = if (isBn) "যেটি থেকে" else "From",
                    selectedUnit = fromUnit,
                    availableUnits = unitsForCat,
                    isBn = isBn,
                    onSelect = { fromUnit = it },
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        ToolActions.triggerHaptic(context, prefs.hapticEnabled.value)
                        val temp = fromUnit
                        fromUnit = toUnit
                        toUnit = temp
                    },
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceElevated)
                        .testTag("swap_units_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Swap units",
                        tint = RedAccent
                    )
                }

                UnitDropdownPicker(
                    label = if (isBn) "যেটিতে রূপান্তর" else "To",
                    selectedUnit = toUnit,
                    availableUnits = unitsForCat,
                    isBn = isBn,
                    onSelect = { toUnit = it },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Result Card
            ToolResultCard(
                resultValue = resultValue,
                title = if (isBn) "রূপান্তরিত ফলাফল" else "Converted Result",
                subtitle = if (inputValue.isNotBlank()) "$inputValue ${if (isBn) fromUnit.nameBn else fromUnit.nameEn} = $resultValue" else null
            )

            Spacer(modifier = Modifier.height(16.dp))

            SecondaryActionButton(
                text = if (isBn) "ক্লিয়ার" else "Clear",
                onClick = { inputValue = "" }
            )
        }
    }
}

@Composable
fun UnitDropdownPicker(
    label: String,
    selectedUnit: UnitDef,
    availableUnits: List<UnitDef>,
    isBn: Boolean,
    onSelect: (UnitDef) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurface)
                .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 14.dp)
        ) {
            Text(
                text = if (isBn) selectedUnit.nameBn else selectedUnit.nameEn,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(DarkSurfaceElevated)
            ) {
                availableUnits.forEach { unit ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = if (isBn) unit.nameBn else unit.nameEn,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        onClick = {
                            onSelect(unit)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

fun convertTemperature(v: Double, from: String, to: String): String {
    val celsius = when (from) {
        "c" -> v
        "f" -> (v - 32) * 5 / 9
        "k" -> v - 273.15
        else -> v
    }
    val converted = when (to) {
        "c" -> celsius
        "f" -> (celsius * 9 / 5) + 32
        "k" -> celsius + 273.15
        else -> celsius
    }
    val df = DecimalFormat("#,##0.##")
    val symbol = when (to) {
        "c" -> "°C"
        "f" -> "°F"
        "k" -> "K"
        else -> ""
    }
    return "${df.format(converted)} $symbol"
}

fun getUnitsForCategory(category: UnitCategory): List<UnitDef> = when (category) {
    UnitCategory.LENGTH -> listOf(
        UnitDef("mm", "Millimeter (mm)", "মিলিমিটার", 0.001),
        UnitDef("cm", "Centimeter (cm)", "সেন্টিমিটার", 0.01),
        UnitDef("m", "Meter (m)", "মিটার", 1.0),
        UnitDef("km", "Kilometer (km)", "কিলোমিটার", 1000.0),
        UnitDef("in", "Inch (in)", "ইঞ্চি", 0.0254),
        UnitDef("ft", "Feet (ft)", "ফুট", 0.3048),
        UnitDef("yd", "Yard (yd)", "গজ", 0.9144),
        UnitDef("mi", "Mile (mi)", "মাইল", 1609.344)
    )
    UnitCategory.WEIGHT -> listOf(
        UnitDef("mg", "Milligram (mg)", "মিলিগ্রাম", 0.000001),
        UnitDef("g", "Gram (g)", "গ্রাম", 0.001),
        UnitDef("kg", "Kilogram (kg)", "কিলোগ্রাম / কেজি", 1.0),
        UnitDef("oz", "Ounce (oz)", "আউন্স", 0.0283495),
        UnitDef("lb", "Pound (lb)", "পাউন্ড", 0.453592),
        UnitDef("ton", "Metric Ton", "মেট্রিক টন", 1000.0)
    )
    UnitCategory.TEMPERATURE -> listOf(
        UnitDef("c", "Celsius (°C)", "সেলসিয়াস", 1.0),
        UnitDef("f", "Fahrenheit (°F)", "ফারেনহাইট", 1.0),
        UnitDef("k", "Kelvin (K)", "কেলভিন", 1.0)
    )
    UnitCategory.AREA -> listOf(
        UnitDef("sq_m", "Square Meter (m²)", "বর্গমিটার", 1.0),
        UnitDef("sq_km", "Square Kilometer (km²)", "বর্গকিমি", 1000000.0),
        UnitDef("sq_ft", "Square Feet (ft²)", "বর্গফুট", 0.092903),
        UnitDef("acre", "Acre", "একর", 4046.86),
        UnitDef("hectare", "Hectare", "হেক্টর", 10000.0)
    )
    UnitCategory.VOLUME -> listOf(
        UnitDef("ml", "Milliliter (ml)", "মিলিমিটার", 0.001),
        UnitDef("l", "Liter (L)", "লিটার", 1.0),
        UnitDef("m3", "Cubic Meter (m³)", "ঘনমিটার", 1000.0),
        UnitDef("cup", "Cup (US)", "কাপ", 0.24),
        UnitDef("gal", "Gallon (US)", "গ্যালন", 3.78541)
    )
    UnitCategory.SPEED -> listOf(
        UnitDef("ms", "Meters per second (m/s)", "মিটার/সেকেন্ড", 1.0),
        UnitDef("kmh", "Kilometers per hour (km/h)", "কিমি/ঘণ্টা", 0.277778),
        UnitDef("mph", "Miles per hour (mph)", "মাইল/ঘণ্টা", 0.44704),
        UnitDef("knot", "Knot", "নট", 0.514444)
    )
    UnitCategory.TIME -> listOf(
        UnitDef("ms", "Millisecond (ms)", "মিলিসেকেন্ড", 0.001),
        UnitDef("s", "Second (s)", "সেকেন্ড", 1.0),
        UnitDef("min", "Minute (min)", "মিনিট", 60.0),
        UnitDef("hr", "Hour (hr)", "ঘণ্টা", 3600.0),
        UnitDef("day", "Day", "দিন", 86400.0),
        UnitDef("week", "Week", "সপ্তাহ", 604800.0),
        UnitDef("year", "Year", "বছর", 31536000.0)
    )
    UnitCategory.DATA -> listOf(
        UnitDef("b", "Bit", "বিট", 0.125),
        UnitDef("B", "Byte (B)", "বাইট", 1.0),
        UnitDef("KB", "Kilobyte (KB)", "কিলোবাইট", 1024.0),
        UnitDef("MB", "Megabyte (MB)", "মেগাবাইট", 1048576.0),
        UnitDef("GB", "Gigabyte (GB)", "গিগাবাইট", 1073741824.0),
        UnitDef("TB", "Terabyte (TB)", "টেরাবাইট", 1099511627776.0)
    )
    UnitCategory.PRESSURE -> listOf(
        UnitDef("pa", "Pascal (Pa)", "প্যাসকেল", 1.0),
        UnitDef("bar", "Bar", "বার", 100000.0),
        UnitDef("psi", "Pound per sq inch (psi)", "পিএসআই", 6894.76),
        UnitDef("atm", "Atmosphere (atm)", "অ্যাটমোস্ফিয়ার", 101325.0)
    )
    UnitCategory.ENERGY -> listOf(
        UnitDef("j", "Joule (J)", "জুল", 1.0),
        UnitDef("kj", "Kilojoule (kJ)", "কিলোজুল", 1000.0),
        UnitDef("cal", "Calorie (cal)", "ক্যালরি", 4.184),
        UnitDef("kcal", "Kilocalorie (kcal)", "কিলোক্যালরি", 4184.0),
        UnitDef("kwh", "Kilowatt-hour (kWh)", "কিলোওয়াট-ঘণ্টা", 3600000.0)
    )
}
