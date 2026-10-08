package com.example.ui.screens.everyday

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.runtime.getValue
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
import com.example.ui.components.ToolActions
import com.example.ui.components.ToolBoxTopBar
import com.example.ui.components.ToolInputField
import com.example.ui.components.ToolResultCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.RedAccent
import java.security.SecureRandom
import kotlin.random.Random

@Composable
fun RandomToolsScreen(
    initialToolId: String,
    language: AppLanguage,
    prefs: PreferencesManager,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val isBn = language == AppLanguage.BANGLA
    var activeTool by remember { mutableStateOf(initialToolId) }

    Scaffold(
        topBar = {
            ToolBoxTopBar(
                title = when (activeTool) {
                    "random_number" -> if (isBn) "র‍্যান্ডম সংখ্যা" else "Random Number Generator"
                    "random_picker" -> if (isBn) "র‍্যান্ডম সিলেক্টর" else "Random Picker (Decision)"
                    else -> "Random Tool"
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
                    "random_number" to (if (isBn) "র‍্যান্ডম সংখ্যা" else "Random Number"),
                    "random_picker" to (if (isBn) "লটারি / সিলেক্টর" else "Random Picker")
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
                "random_number" -> {
                    var minVal by remember { mutableStateOf("1") }
                    var maxVal by remember { mutableStateOf("100") }
                    var countVal by remember { mutableStateOf("1") }
                    var rollResult by remember { mutableStateOf("42") }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ToolInputField(modifier = Modifier.weight(1f), value = minVal, onValueChange = { minVal = it }, label = "Min", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        ToolInputField(modifier = Modifier.weight(1f), value = maxVal, onValueChange = { maxVal = it }, label = "Max", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        ToolInputField(modifier = Modifier.weight(1f), value = countVal, onValueChange = { countVal = it }, label = "Count", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    ToolResultCard(
                        resultValue = rollResult,
                        title = if (isBn) "রোল ফলাফল" else "Generated Number(s)"
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    PrimaryActionButton(
                        text = if (isBn) "রোল করুন (Roll)" else "Roll / Generate",
                        onClick = {
                            ToolActions.triggerHaptic(context, prefs.hapticEnabled.value)
                            val min = minVal.toIntOrNull() ?: 1
                            val max = maxVal.toIntOrNull() ?: 100
                            val count = (countVal.toIntOrNull() ?: 1).coerceIn(1, 20)
                            if (min <= max) {
                                val list = (1..count).map { Random.nextInt(min, max + 1) }
                                rollResult = list.joinToString(", ")
                            } else {
                                rollResult = "Min must be <= Max"
                            }
                        }
                    )
                }

                "random_picker" -> {
                    var optionsText by remember {
                        mutableStateOf("Biryani\nPizza\nBurger\nKhichuri\nPasta")
                    }
                    var pickedResult by remember { mutableStateOf("") }

                    ToolInputField(
                        value = optionsText,
                        onValueChange = { optionsText = it },
                        label = if (isBn) "অপশনগুলো লিখুন (প্রতি লাইনে একটি)" else "Enter choices (one per line)",
                        singleLine = false,
                        maxLines = 6
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    ToolResultCard(
                        resultValue = pickedResult.ifBlank { "Ready to Pick" },
                        title = if (isBn) "নির্বাচিত বিজয়ী" else "Selected Choice",
                        subtitle = if (pickedResult.isNotBlank()) "🎉 Selected at random!" else null
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    PrimaryActionButton(
                        text = if (isBn) "লটারি করুন (Pick One)" else "Pick at Random!",
                        onClick = {
                            ToolActions.triggerHaptic(context, true)
                            val list = optionsText.lines().map { it.trim() }.filter { it.isNotBlank() }
                            if (list.isNotEmpty()) {
                                val winner = list[Random.nextInt(list.size)]
                                pickedResult = winner
                            }
                        }
                    )
                }
            }
        }
    }
}
