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

@Composable
fun BanglaNumberConverterScreen(
    language: AppLanguage,
    prefs: PreferencesManager,
    onBackClick: () -> Unit
) {
    val isBn = language == AppLanguage.BANGLA
    var inputNum by remember { mutableStateOf("1250000") }
    var mode by remember { mutableStateOf(0) } // 0: English -> Bangla, 1: Bangla -> English, 2: Number to Bangla Words

    val convertedResult by remember(inputNum, mode) {
        derivedStateOf {
            when (mode) {
                0 -> toBanglaDigits(inputNum)
                1 -> toEnglishDigits(inputNum)
                2 -> {
                    val englishVal = toEnglishDigits(inputNum).toLongOrNull()
                    if (englishVal != null) "${convertNumberToBanglaWords(englishVal)} টাকা মাত্র"
                    else "সঠিক সংখ্যা লিখুন"
                }
                else -> ""
            }
        }
    }

    Scaffold(
        topBar = {
            ToolBoxTopBar(
                title = if (isBn) "বাংলা ↔ ইংরেজি সংখ্যা" else "Bangla ↔ English Numbers",
                onBackClick = onBackClick,
                isFavorite = prefs.isFavorite("bangla_number"),
                onFavoriteToggle = { prefs.toggleFavorite("bangla_number") }
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
                    0 to "En → বাংলা",
                    1 to "বাংলা → En",
                    2 to if (isBn) "কথায় রূপান্তর" else "In Words"
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

            ToolInputField(
                value = inputNum,
                onValueChange = { inputNum = it },
                label = when (mode) {
                    0 -> "ইংরেজি সংখ্যা লিখুন (e.g. 125000)"
                    1 -> "বাংলা সংখ্যা লিখুন (যেমন: ১২৫০০০)"
                    else -> "যেকোনো সংখ্যা লিখুন"
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            ToolResultCard(
                resultValue = convertedResult,
                title = if (isBn) "রূপান্তরিত ফলাফল" else "Converted Output"
            )

            Spacer(modifier = Modifier.height(16.dp))

            SecondaryActionButton(
                text = if (isBn) "ক্লিয়ার" else "Clear",
                onClick = { inputNum = "" }
            )
        }
    }
}

fun toBanglaDigits(str: String): String {
    val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    val sb = StringBuilder()
    for (ch in str) {
        if (ch in '0'..'9') {
            sb.append(bnDigits[ch - '0'])
        } else {
            sb.append(ch)
        }
    }
    return sb.toString()
}

fun toEnglishDigits(str: String): String {
    val sb = StringBuilder()
    for (ch in str) {
        when (ch) {
            '০' -> sb.append('0')
            '১' -> sb.append('1')
            '২' -> sb.append('2')
            '৩' -> sb.append('3')
            '৪' -> sb.append('4')
            '৫' -> sb.append('5')
            '৬' -> sb.append('6')
            '৭' -> sb.append('7')
            '৮' -> sb.append('8')
            '৯' -> sb.append('9')
            else -> sb.append(ch)
        }
    }
    return sb.toString()
}
