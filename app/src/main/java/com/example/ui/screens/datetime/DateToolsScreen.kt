package com.example.ui.screens.datetime

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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun DateToolsScreen(
    initialToolId: String,
    language: AppLanguage,
    prefs: PreferencesManager,
    onBackClick: () -> Unit
) {
    var activeTool by remember { mutableStateOf(initialToolId) }
    val isBn = language == AppLanguage.BANGLA
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val displayFormat = remember { SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.US) }

    Scaffold(
        topBar = {
            ToolBoxTopBar(
                title = when (activeTool) {
                    "add_days" -> if (isBn) "তারিখ যোগ করুন" else "Add Days to Date"
                    "subtract_days" -> if (isBn) "তারিখ বিয়োগ করুন" else "Subtract Days from Date"
                    "days_until" -> if (isBn) "দিন গণনা" else "Days Until Date"
                    else -> "Date Tool"
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "add_days" to (if (isBn) "দিন যোগ (+)" else "Add Days (+)"),
                    "subtract_days" to (if (isBn) "দিন বিয়োগ (-)" else "Subtract Days (-)"),
                    "days_until" to (if (isBn) "দিন গণনা" else "Days Until")
                ).forEach { (id, label) ->
                    FilterChip(
                        selected = activeTool == id,
                        onClick = { activeTool = id },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RedAccent,
                            containerColor = DarkSurfaceElevated
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (activeTool) {
                "add_days", "subtract_days" -> {
                    val isAdd = activeTool == "add_days"
                    var baseDateStr by remember { mutableStateOf(sdf.format(Date())) }
                    var daysToAdd by remember { mutableStateOf("30") }
                    var weeksToAdd by remember { mutableStateOf("0") }
                    var monthsToAdd by remember { mutableStateOf("0") }

                    val calcResult by remember(baseDateStr, daysToAdd, weeksToAdd, monthsToAdd, isAdd) {
                        derivedStateOf {
                            try {
                                val d = sdf.parse(baseDateStr) ?: return@derivedStateOf null
                                val cal = Calendar.getInstance().apply { time = d }
                                val factor = if (isAdd) 1 else -1

                                val dCount = (daysToAdd.toIntOrNull() ?: 0) * factor
                                val wCount = (weeksToAdd.toIntOrNull() ?: 0) * factor
                                val mCount = (monthsToAdd.toIntOrNull() ?: 0) * factor

                                cal.add(Calendar.DAY_OF_YEAR, dCount + (wCount * 7))
                                cal.add(Calendar.MONTH, mCount)

                                val resDate = cal.time
                                displayFormat.format(resDate)
                            } catch (_: Exception) {
                                null
                            }
                        }
                    }

                    ToolInputField(
                        value = baseDateStr,
                        onValueChange = { baseDateStr = it },
                        label = if (isBn) "মূল তারিখ (YYYY-MM-DD)" else "Base Date (YYYY-MM-DD)"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ToolInputField(
                            modifier = Modifier.weight(1f),
                            value = daysToAdd,
                            onValueChange = { daysToAdd = it },
                            label = if (isBn) "দিন" else "Days",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        ToolInputField(
                            modifier = Modifier.weight(1f),
                            value = weeksToAdd,
                            onValueChange = { weeksToAdd = it },
                            label = if (isBn) "সপ্তাহ" else "Weeks",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        ToolInputField(
                            modifier = Modifier.weight(1f),
                            value = monthsToAdd,
                            onValueChange = { monthsToAdd = it },
                            label = if (isBn) "মাস" else "Months",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    ToolResultCard(
                        resultValue = calcResult ?: "",
                        title = if (isAdd) (if (isBn) "পরবর্তী তারিখ" else "Calculated Target Date")
                        else (if (isBn) "পূর্ববর্তী তারিখ" else "Calculated Past Date")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SecondaryActionButton(
                        text = if (isBn) "আজকের তারিখে রিসেট" else "Reset to Today",
                        onClick = { baseDateStr = sdf.format(Date()); daysToAdd = "30"; weeksToAdd = "0"; monthsToAdd = "0" }
                    )
                }
                "days_until" -> {
                    var targetDateStr by remember { mutableStateOf("2027-01-01") }
                    var eventName by remember { mutableStateOf("New Year 2027") }

                    val countdown by remember(targetDateStr) {
                        derivedStateOf {
                            try {
                                val target = sdf.parse(targetDateStr) ?: return@derivedStateOf null
                                val today = Calendar.getInstance().apply {
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }.time
                                val diff = target.time - today.time
                                val days = TimeUnit.MILLISECONDS.toDays(diff)
                                when {
                                    days > 0 -> "$days days remaining"
                                    days == 0L -> "Today is the day!"
                                    else -> "${Math.abs(days)} days ago"
                                }
                            } catch (_: Exception) {
                                null
                            }
                        }
                    }

                    // Preset buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            "New Year" to "2027-01-01",
                            "Eid-ul-Fitr" to "2027-03-10",
                            "Victory Day" to "2026-12-16"
                        ).forEach { (name, date) ->
                            FilterChip(
                                selected = targetDateStr == date,
                                onClick = {
                                    targetDateStr = date
                                    eventName = name
                                },
                                label = { Text(name, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RedAccent,
                                    containerColor = DarkSurfaceElevated
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    ToolInputField(
                        value = eventName,
                        onValueChange = { eventName = it },
                        label = if (isBn) "ইভেন্ট বা দিবসের নাম" else "Event / Occasion Name"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ToolInputField(
                        value = targetDateStr,
                        onValueChange = { targetDateStr = it },
                        label = if (isBn) "লক্ষ্য তারিখ (YYYY-MM-DD)" else "Target Date (YYYY-MM-DD)"
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    ToolResultCard(
                        resultValue = countdown ?: "",
                        title = if (isBn) "কাউন্টডাউন ফলাফল" else "Countdown to $eventName"
                    )
                }
            }
        }
    }
}
