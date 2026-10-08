package com.example.ui.screens.text

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.data.PreferencesManager
import com.example.localization.AppLanguage
import com.example.ui.components.SecondaryActionButton
import com.example.ui.components.ToolActions
import com.example.ui.components.ToolBoxTopBar
import com.example.ui.components.ToolInputField
import com.example.ui.components.ToolResultCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.RedAccent
import java.util.Locale

@Composable
fun TextToolsScreen(
    initialToolId: String,
    language: AppLanguage,
    prefs: PreferencesManager,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val isBn = language == AppLanguage.BANGLA
    var activeTool by remember { mutableStateOf(initialToolId) }
    var inputText by remember { mutableStateOf("The quick brown fox jumps over the lazy dog.") }

    Scaffold(
        topBar = {
            ToolBoxTopBar(
                title = when (activeTool) {
                    "text_counter" -> if (isBn) "শব্দ ও বর্ণ গণক" else "Text & Word Counter"
                    "case_converter" -> if (isBn) "কেস কনভার্টার" else "Case Converter"
                    "remove_spaces" -> if (isBn) "অতিরিক্ত স্পেস মোছা" else "Remove Extra Spaces"
                    "remove_duplicates" -> if (isBn) "ডুপ্লিকেট লাইন রিমুভার" else "Duplicate Line Remover"
                    "text_sorter" -> if (isBn) "টেক্সট সর্টার" else "Text Sorter"
                    "text_cleaner" -> if (isBn) "টেক্সট ক্লিনার" else "Text Cleaner"
                    else -> "Text Tool"
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
            // Tool Selector Chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "text_counter" to (if (isBn) "গণনা" else "Counter"),
                    "case_converter" to (if (isBn) "কেস" else "Case"),
                    "remove_spaces" to (if (isBn) "স্পেস" else "Spaces"),
                    "remove_duplicates" to (if (isBn) "ডুপ্লিকেট" else "Duplicates"),
                    "text_sorter" to (if (isBn) "সর্ট" else "Sort"),
                    "text_cleaner" to (if (isBn) "ক্লিনার" else "Cleaner")
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

            // Main Text Input Area
            ToolInputField(
                value = inputText,
                onValueChange = { inputText = it },
                label = if (isBn) "টেক্সট লিখুন বা পেস্ট করুন" else "Enter or paste your text",
                placeholder = "Type here...",
                singleLine = false,
                maxLines = 6
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (activeTool) {
                "text_counter" -> {
                    val charCount = inputText.length
                    val charNoSpace = inputText.replace("\\s+".toRegex(), "").length
                    val words = if (inputText.isBlank()) 0 else inputText.trim().split("\\s+".toRegex()).size
                    val lines = if (inputText.isBlank()) 0 else inputText.lines().size
                    val readingTimeSec = Math.ceil((words / 200.0) * 60).toInt()

                    ToolResultCard(
                        resultValue = "$words words | $charCount chars",
                        title = if (isBn) "গণনার পরিসংখ্যান" else "Text Statistics",
                        subtitle = "${if (isBn) "স্পেস ছাড়া বর্ণ" else "Chars (no space)"}: $charNoSpace\n" +
                                "${if (isBn) "মোট লাইন" else "Lines"}: $lines\n" +
                                "${if (isBn) "পড়ার সময়" else "Est. Reading time"}: ~$readingTimeSec sec"
                    )
                }

                "case_converter" -> {
                    var selectedCase by remember { mutableStateOf("UPPERCASE") }
                    val converted by remember(inputText, selectedCase) {
                        derivedStateOf {
                            when (selectedCase) {
                                "UPPERCASE" -> inputText.uppercase(Locale.ROOT)
                                "lowercase" -> inputText.lowercase(Locale.ROOT)
                                "Title Case" -> inputText.split(" ").joinToString(" ") { word ->
                                    word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                                }
                                "camelCase" -> {
                                    val parts = inputText.split("\\s+|_|-".toRegex()).filter { it.isNotBlank() }
                                    if (parts.isEmpty()) ""
                                    else parts.first().lowercase() + parts.drop(1).joinToString("") { p ->
                                        p.lowercase().replaceFirstChar { it.uppercase() }
                                    }
                                }
                                "snake_case" -> inputText.trim().lowercase().replace("\\s+".toRegex(), "_")
                                else -> inputText
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("UPPERCASE", "lowercase", "Title Case", "camelCase", "snake_case").forEach { c ->
                            FilterChip(
                                selected = selectedCase == c,
                                onClick = { selectedCase = c },
                                label = { Text(c, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RedAccent)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    ToolResultCard(
                        resultValue = converted,
                        title = if (isBn) "রূপান্তরিত টেক্সট" else "Converted Text"
                    )
                }

                "remove_spaces" -> {
                    val trimmed = inputText.trim().replace("[ \\t]+".toRegex(), " ")
                    ToolResultCard(
                        resultValue = trimmed,
                        title = if (isBn) "অতিরিক্ত স্পেস ছাড়া টেক্সট" else "Cleaned Spacing"
                    )
                }

                "remove_duplicates" -> {
                    val uniqueLines = inputText.lines().distinct().joinToString("\n")
                    ToolResultCard(
                        resultValue = uniqueLines,
                        title = if (isBn) "ইউনিক লাইনসমূহ" else "Deduplicated Lines",
                        subtitle = "Removed ${inputText.lines().size - uniqueLines.lines().size} duplicate lines"
                    )
                }

                "text_sorter" -> {
                    var sortMode by remember { mutableStateOf("A-Z") }
                    val sorted = remember(inputText, sortMode) {
                        when (sortMode) {
                            "A-Z" -> inputText.lines().sorted().joinToString("\n")
                            "Z-A" -> inputText.lines().sortedDescending().joinToString("\n")
                            "Length" -> inputText.lines().sortedBy { it.length }.joinToString("\n")
                            else -> inputText
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("A-Z", "Z-A", "Length").forEach { m ->
                            FilterChip(
                                selected = sortMode == m,
                                onClick = { sortMode = m },
                                label = { Text(m) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RedAccent)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    ToolResultCard(
                        resultValue = sorted,
                        title = if (isBn) "সাজানো টেক্সট" else "Sorted Lines"
                    )
                }

                "text_cleaner" -> {
                    val stripped = inputText
                        .replace("<[^>]*>".toRegex(), "") // Strip HTML
                        .replace("[0-9]".toRegex(), "") // Strip numbers
                        .replace("[!\"#$%&'()*+,-./:;<=>?@\\[\\]^_`{|}~]".toRegex(), "") // Strip punctuation
                        .trim()

                    ToolResultCard(
                        resultValue = stripped,
                        title = if (isBn) "পরিষ্কার টেক্সট" else "Sanitized Text"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SecondaryActionButton(
                text = if (isBn) "ক্লিয়ার" else "Clear Input",
                onClick = { inputText = "" }
            )
        }
    }
}
