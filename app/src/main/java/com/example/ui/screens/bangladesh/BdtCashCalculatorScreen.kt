package com.example.ui.screens.bangladesh

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
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
import com.example.ui.components.ToolBoxTopBar
import com.example.ui.components.ToolInputField
import com.example.ui.components.ToolResultCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.RedAccent
import java.text.DecimalFormat

@Composable
fun BdtCashCalculatorScreen(
    language: AppLanguage,
    prefs: PreferencesManager,
    onBackClick: () -> Unit
) {
    val isBn = language == AppLanguage.BANGLA
    val denominations = listOf(1000, 500, 200, 100, 50, 20, 10, 5, 2, 1)
    val noteCounts = remember { mutableStateMapOf<Int, String>() }
    val df = remember { DecimalFormat("#,##0") }

    val totalAmount by remember {
        derivedStateOf {
            denominations.sumOf { denom ->
                val count = noteCounts[denom]?.toLongOrNull() ?: 0L
                denom * count
            }
        }
    }

    val totalNotes by remember {
        derivedStateOf {
            denominations.sumOf { denom ->
                noteCounts[denom]?.toLongOrNull() ?: 0L
            }
        }
    }

    val wordsBn by remember(totalAmount) {
        derivedStateOf { convertNumberToBanglaWords(totalAmount) }
    }

    Scaffold(
        topBar = {
            ToolBoxTopBar(
                title = if (isBn) "টাকা নোট গণক (BDT)" else "BDT Cash Note Counter",
                onBackClick = onBackClick,
                isFavorite = prefs.isFavorite("bdt_cash"),
                onFavoriteToggle = { prefs.toggleFavorite("bdt_cash") }
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
            // Big Summary Card
            ToolResultCard(
                resultValue = "৳ ${df.format(totalAmount)}",
                title = if (isBn) "মোট টাকা (টাকার পরিমাণ)" else "Total Cash Amount",
                subtitle = "${if (isBn) "মোট নোট সংখ্যা" else "Total Notes"}: $totalNotes\n" +
                        if (totalAmount > 0) "কথায়: $wordsBn টাকা মাত্র" else ""
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isBn) "নোট সংখ্যা লিখুন" else "Enter Note Counts",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            denominations.forEach { denom ->
                val currentCountStr = noteCounts[denom].orEmpty()
                val subtotal = (currentCountStr.toLongOrNull() ?: 0L) * denom

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(70.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceElevated)
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "৳ $denom",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (denom >= 100) RedAccent else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        ToolInputField(
                            value = currentCountStr,
                            onValueChange = { noteCounts[denom] = it },
                            label = if (isBn) "নোট সংখ্যা" else "Notes",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "= ৳ ${df.format(subtotal)}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(90.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SecondaryActionButton(
                text = if (isBn) "সব ক্লিয়ার করুন" else "Clear All Counts",
                onClick = { noteCounts.clear() }
            )
        }
    }
}

fun convertNumberToBanglaWords(amount: Long): String {
    if (amount == 0L) return "শূন্য"
    var n = amount
    val sb = StringBuilder()

    val crore = n / 10000000L
    if (crore > 0) {
        sb.append("${convertBelowHundred(crore)} কোটি ")
        n %= 10000000L
    }

    val lakh = n / 100000L
    if (lakh > 0) {
        sb.append("${convertBelowHundred(lakh)} লাখ ")
        n %= 100000L
    }

    val thousand = n / 1000L
    if (thousand > 0) {
        sb.append("${convertBelowHundred(thousand)} হাজার ")
        n %= 1000L
    }

    val hundred = n / 100L
    if (hundred > 0) {
        sb.append("${convertBelowHundred(hundred)} শত ")
        n %= 100L
    }

    if (n > 0) {
        sb.append(convertBelowHundred(n))
    }

    return sb.toString().trim()
}

fun convertBelowHundred(num: Long): String {
    val bnUnits = mapOf(
        1L to "এক", 2L to "দুই", 3L to "তিন", 4L to "চার", 5L to "পাঁচ",
        6L to "ছয়", 7L to "সাত", 8L to "আট", 9L to "নয়", 10L to "দশ",
        11L to "এগারো", 12L to "বারো", 13L to "তেরো", 14L to "চৌদ্দ", 15L to "পনেরো",
        16L to "ষোল", 17L to "সতেরো", 18L to "আঠারো", 19L to "উনিশ", 20L to "বিশ",
        25L to "পঁচিশ", 30L to "ত্রিশ", 40L to "চল্লিশ", 50L to "পঞ্চাশ",
        60L to "ষাট", 70L to "সত্তর", 80L to "আশি", 90L to "নব্বই"
    )
    return bnUnits[num] ?: num.toString()
}
