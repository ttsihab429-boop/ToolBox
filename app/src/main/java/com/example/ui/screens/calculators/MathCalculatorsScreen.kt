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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun MathCalculatorsScreen(
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
                    "avg_calc" -> if (isBn) "গড় ক্যালকুলেটর" else "Average Calculator"
                    "fraction_calc" -> if (isBn) "ভগ্নাংশ ক্যালকুলেটর" else "Fraction Calculator"
                    "ratio_calc" -> if (isBn) "অনুপাত ক্যালকুলেটর" else "Ratio Calculator"
                    "age_calc" -> if (isBn) "বয়স ক্যালকুলেটর" else "Age Calculator"
                    "date_diff_calc" -> if (isBn) "তারিখের ব্যবধান" else "Date Difference"
                    else -> "Math Tool"
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val tools = listOf(
                    "avg_calc" to (if (isBn) "গড়" else "Average"),
                    "fraction_calc" to (if (isBn) "ভগ্নাংশ" else "Fraction"),
                    "ratio_calc" to (if (isBn) "অনুপাত" else "Ratio"),
                    "age_calc" to (if (isBn) "বয়স" else "Age"),
                    "date_diff_calc" to (if (isBn) "তারিখ" else "Date Diff")
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
                "avg_calc" -> AverageCalculatorContent(isBn, df)
                "fraction_calc" -> FractionCalculatorContent(isBn, df)
                "ratio_calc" -> RatioCalculatorContent(isBn, df)
                "age_calc" -> AgeCalculatorContent(isBn)
                "date_diff_calc" -> DateDifferenceContent(isBn)
            }
        }
    }
}

@Composable
fun AverageCalculatorContent(isBn: Boolean, df: DecimalFormat) {
    var rawInput by remember { mutableStateOf("") }

    val numbers = remember(rawInput) {
        rawInput.split(',', ' ', '\n', '\t')
            .mapNotNull { it.trim().toDoubleOrNull() }
    }

    val count = numbers.size
    val sum = numbers.sum()
    val mean = if (count > 0) sum / count else 0.0
    val median = if (count > 0) {
        val sorted = numbers.sorted()
        if (count % 2 == 1) sorted[count / 2]
        else (sorted[(count / 2) - 1] + sorted[count / 2]) / 2.0
    } else 0.0
    val min = numbers.minOrNull() ?: 0.0
    val max = numbers.maxOrNull() ?: 0.0

    ToolInputField(
        value = rawInput,
        onValueChange = { rawInput = it },
        label = if (isBn) "সংখ্যাগুলো লিখুন (কমা বা স্পেস দিয়ে)" else "Enter numbers (comma or space separated)",
        placeholder = "e.g. 15, 24, 38, 92, 105",
        singleLine = false,
        maxLines = 4
    )

    Spacer(modifier = Modifier.height(16.dp))

    ToolResultCard(
        resultValue = if (count > 0) df.format(mean) else "",
        title = if (isBn) "গড় মান (Mean)" else "Mean / Average",
        subtitle = if (count > 0) {
            "${if (isBn) "মোট সংখ্যা" else "Count"}: $count | ${if (isBn) "মোট যোগফল" else "Sum"}: ${df.format(sum)}\n" +
            "${if (isBn) "মধ্যক" else "Median"}: ${df.format(median)} | Min: ${df.format(min)} | Max: ${df.format(max)}"
        } else null
    )

    Spacer(modifier = Modifier.height(16.dp))
    SecondaryActionButton(
        text = if (isBn) "ক্লিয়ার" else "Clear",
        onClick = { rawInput = "" }
    )
}

@Composable
fun FractionCalculatorContent(isBn: Boolean, df: DecimalFormat) {
    var num1 by remember { mutableStateOf("1") }
    var den1 by remember { mutableStateOf("2") }
    var num2 by remember { mutableStateOf("1") }
    var den2 by remember { mutableStateOf("3") }
    var operator by remember { mutableStateOf("+") }

    fun gcd(a: Long, b: Long): Long = if (b == 0L) a else gcd(b, a % b)

    val result by remember(num1, den1, num2, den2, operator) {
        derivedStateOf {
            val n1 = num1.toLongOrNull() ?: 0L
            val d1 = den1.toLongOrNull() ?: 1L
            val n2 = num2.toLongOrNull() ?: 0L
            val d2 = den2.toLongOrNull() ?: 1L

            if (d1 == 0L || d2 == 0L) return@derivedStateOf "Denominator cannot be 0"

            val resNum: Long
            val resDen: Long
            when (operator) {
                "+" -> {
                    resNum = (n1 * d2) + (n2 * d1)
                    resDen = d1 * d2
                }
                "-" -> {
                    resNum = (n1 * d2) - (n2 * d1)
                    resDen = d1 * d2
                }
                "*" -> {
                    resNum = n1 * n2
                    resDen = d1 * d2
                }
                "÷" -> {
                    if (n2 == 0L) return@derivedStateOf "Cannot divide by zero fraction"
                    resNum = n1 * d2
                    resDen = d1 * n2
                }
                else -> {
                    resNum = 0L
                    resDen = 1L
                }
            }

            val divisor = gcd(Math.abs(resNum), Math.abs(resDen))
            val simpNum = resNum / divisor
            val simpDen = resDen / divisor
            val decimal = simpNum.toDouble() / simpDen.toDouble()

            val mixed = if (Math.abs(simpNum) >= simpDen && simpDen > 1) {
                val whole = simpNum / simpDen
                val rem = Math.abs(simpNum % simpDen)
                if (rem > 0) " (Mixed: $whole $rem/$simpDen)" else ""
            } else ""

            "$simpNum / $simpDen$mixed = ${df.format(decimal)}"
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("+", "-", "*", "÷").forEach { op ->
            FilterChip(
                selected = operator == op,
                onClick = { operator = op },
                label = { Text(op, style = MaterialTheme.typography.titleMedium) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RedAccent)
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Fraction 1", style = MaterialTheme.typography.labelSmall, color = RedAccent)
            Spacer(modifier = Modifier.height(4.dp))
            ToolInputField(value = num1, onValueChange = { num1 = it }, label = "Numerator", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            Spacer(modifier = Modifier.height(6.dp))
            ToolInputField(value = den1, onValueChange = { den1 = it }, label = "Denominator", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text("Fraction 2", style = MaterialTheme.typography.labelSmall, color = RedAccent)
            Spacer(modifier = Modifier.height(4.dp))
            ToolInputField(value = num2, onValueChange = { num2 = it }, label = "Numerator", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            Spacer(modifier = Modifier.height(6.dp))
            ToolInputField(value = den2, onValueChange = { den2 = it }, label = "Denominator", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    ToolResultCard(
        resultValue = result,
        title = if (isBn) "ভগ্নাংশের সমাধান" else "Fraction Result"
    )
}

@Composable
fun RatioCalculatorContent(isBn: Boolean, df: DecimalFormat) {
    var a by remember { mutableStateOf("16") }
    var b by remember { mutableStateOf("9") }
    var c by remember { mutableStateOf("1920") }
    var d by remember { mutableStateOf("") }

    val valA = a.toDoubleOrNull()
    val valB = b.toDoubleOrNull()
    val valC = c.toDoubleOrNull()
    val valD = d.toDoubleOrNull()

    val solved by remember(valA, valB, valC, valD) {
        derivedStateOf {
            when {
                valD == null && valA != null && valB != null && valC != null && valA != 0.0 -> {
                    val calcD = (valB * valC) / valA
                    "D = ${df.format(calcD)}"
                }
                valC == null && valA != null && valB != null && valD != null && valB != 0.0 -> {
                    val calcC = (valA * valD) / valB
                    "C = ${df.format(calcC)}"
                }
                valB == null && valA != null && valC != null && valD != null && valC != 0.0 -> {
                    val calcB = (valA * valD) / valC
                    "B = ${df.format(calcB)}"
                }
                valA == null && valB != null && valC != null && valD != null && valD != 0.0 -> {
                    val calcA = (valB * valC) / valD
                    "A = ${df.format(calcA)}"
                }
                else -> "Leave 1 field empty to solve (A : B = C : D)"
            }
        }
    }

    Text(
        text = if (isBn) "সমানুপাত সমাধান: A : B = C : D (যেকোনো ৩টি পূরণ করুন)" else "Proportion Solver: A : B = C : D (Leave one empty)",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(12.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ToolInputField(modifier = Modifier.weight(1f), value = a, onValueChange = { a = it }, label = "A", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
        ToolInputField(modifier = Modifier.weight(1f), value = b, onValueChange = { b = it }, label = "B", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
    }
    Spacer(modifier = Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ToolInputField(modifier = Modifier.weight(1f), value = c, onValueChange = { c = it }, label = "C", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
        ToolInputField(modifier = Modifier.weight(1f), value = d, onValueChange = { d = it }, label = "D", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
    }

    Spacer(modifier = Modifier.height(16.dp))

    ToolResultCard(
        resultValue = solved,
        title = if (isBn) "অনুপাতের ফলাফল" else "Proportion Result"
    )
}

@Composable
fun AgeCalculatorContent(isBn: Boolean) {
    var birthYear by remember { mutableStateOf("2000") }
    var birthMonth by remember { mutableStateOf("1") }
    var birthDay by remember { mutableStateOf("1") }

    val ageResult by remember(birthYear, birthMonth, birthDay) {
        derivedStateOf {
            val y = birthYear.toIntOrNull() ?: return@derivedStateOf null
            val m = birthMonth.toIntOrNull()?.minus(1) ?: return@derivedStateOf null
            val d = birthDay.toIntOrNull() ?: return@derivedStateOf null

            val birthCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, y)
                set(Calendar.MONTH, m)
                set(Calendar.DAY_OF_MONTH, d)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            val now = Calendar.getInstance()

            if (birthCal.after(now)) return@derivedStateOf Triple("Future Date", "Birthdate cannot be in the future", "")

            var years = now.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR)
            var months = now.get(Calendar.MONTH) - birthCal.get(Calendar.MONTH)
            var days = now.get(Calendar.DAY_OF_MONTH) - birthCal.get(Calendar.DAY_OF_MONTH)

            if (days < 0) {
                months--
                val prevMonth = now.clone() as Calendar
                prevMonth.add(Calendar.MONTH, -1)
                days += prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
            }
            if (months < 0) {
                years--
                months += 12
            }

            val diffMillis = now.timeInMillis - birthCal.timeInMillis
            val totalDays = TimeUnit.MILLISECONDS.toDays(diffMillis)

            // Next birthday countdown
            val nextBday = birthCal.clone() as Calendar
            nextBday.set(Calendar.YEAR, now.get(Calendar.YEAR))
            if (nextBday.before(now)) {
                nextBday.add(Calendar.YEAR, 1)
            }
            val daysToNextBday = TimeUnit.MILLISECONDS.toDays(nextBday.timeInMillis - now.timeInMillis)

            Triple("$years years, $months months, $days days", "Total: $totalDays days lived", "Next Birthday in $daysToNextBday days")
        }
    }

    Text(
        text = if (isBn) "জন্মতারিখ লিখুন" else "Enter Date of Birth",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface
    )

    Spacer(modifier = Modifier.height(12.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ToolInputField(modifier = Modifier.weight(1f), value = birthDay, onValueChange = { birthDay = it }, label = if (isBn) "দিন" else "Day", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        ToolInputField(modifier = Modifier.weight(1f), value = birthMonth, onValueChange = { birthMonth = it }, label = if (isBn) "মাস" else "Month", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        ToolInputField(modifier = Modifier.weight(1.2f), value = birthYear, onValueChange = { birthYear = it }, label = if (isBn) "বছর" else "Year", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
    }

    Spacer(modifier = Modifier.height(16.dp))

    ToolResultCard(
        resultValue = ageResult?.first ?: "",
        title = if (isBn) "বর্তমান সঠিক বয়স" else "Exact Age",
        subtitle = if (ageResult != null) "${ageResult?.second}\n${ageResult?.third}" else null
    )
}

@Composable
fun DateDifferenceContent(isBn: Boolean) {
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    var startDateStr by remember { mutableStateOf("2026-01-01") }
    var endDateStr by remember { mutableStateOf(sdf.format(Date())) }

    val diffResult by remember(startDateStr, endDateStr) {
        derivedStateOf {
            try {
                val d1 = sdf.parse(startDateStr) ?: return@derivedStateOf null
                val d2 = sdf.parse(endDateStr) ?: return@derivedStateOf null
                val diffMs = Math.abs(d2.time - d1.time)
                val totalDays = TimeUnit.MILLISECONDS.toDays(diffMs)
                val weeks = totalDays / 7
                val remDays = totalDays % 7
                val totalHours = TimeUnit.MILLISECONDS.toHours(diffMs)

                Triple(
                    "$totalDays days",
                    "$weeks weeks and $remDays days",
                    "Total hours: $totalHours hours"
                )
            } catch (_: Exception) {
                null
            }
        }
    }

    ToolInputField(
        value = startDateStr,
        onValueChange = { startDateStr = it },
        label = if (isBn) "শুরুর তারিখ (YYYY-MM-DD)" else "Start Date (YYYY-MM-DD)"
    )
    Spacer(modifier = Modifier.height(12.dp))
    ToolInputField(
        value = endDateStr,
        onValueChange = { endDateStr = it },
        label = if (isBn) "শেষ তারিখ (YYYY-MM-DD)" else "End Date (YYYY-MM-DD)"
    )

    Spacer(modifier = Modifier.height(16.dp))

    ToolResultCard(
        resultValue = diffResult?.first ?: "",
        title = if (isBn) "তারিখের পার্থক্য" else "Date Difference",
        subtitle = if (diffResult != null) "${diffResult?.second} | ${diffResult?.third}" else null
    )
}
