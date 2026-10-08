package com.example.ui.screens.datetime

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PreferencesManager
import com.example.localization.AppLanguage
import com.example.ui.components.ToolBoxTopBar
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.RedAccent
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class CityClock(
    val cityEn: String,
    val cityBn: String,
    val timeZoneId: String,
    val country: String,
    val timeFormat: SimpleDateFormat,
    val dateFormat: SimpleDateFormat
)

private val CITIES_LIST = listOf(
    createCityClock("Dhaka", "ঢাকা", "Asia/Dhaka", "Bangladesh (GMT+6)"),
    createCityClock("London", "লন্ডন", "Europe/London", "United Kingdom (GMT+0)"),
    createCityClock("New York", "নিউইয়র্ক", "America/New_York", "United States (GMT-4)"),
    createCityClock("Tokyo", "টোকিও", "Asia/Tokyo", "Japan (GMT+9)"),
    createCityClock("Dubai", "দুবাই", "Asia/Dubai", "UAE (GMT+4)"),
    createCityClock("Singapore", "সিঙ্গাপুর", "Asia/Singapore", "Singapore (GMT+8)"),
    createCityClock("Sydney", "সিডনি", "Australia/Sydney", "Australia (GMT+10)"),
    createCityClock("Los Angeles", "লস অ্যাঞ্জেলেস", "America/Los_Angeles", "United States (GMT-7)"),
    createCityClock("UTC / GMT", "ইউটিসি", "UTC", "Coordinated Universal Time")
)

private fun createCityClock(cityEn: String, cityBn: String, tzId: String, country: String): CityClock {
    val tz = TimeZone.getTimeZone(tzId)
    val tf = SimpleDateFormat("hh:mm:ss a", Locale.US).apply { timeZone = tz }
    val df = SimpleDateFormat("EEE, dd MMM yyyy", Locale.US).apply { timeZone = tz }
    return CityClock(cityEn, cityBn, tzId, country, tf, df)
}

@Composable
fun WorldClockScreen(
    language: AppLanguage,
    prefs: PreferencesManager,
    onBackClick: () -> Unit
) {
    val isBn = language == AppLanguage.BANGLA
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val reusableDate = remember { Date() }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTimeMillis = System.currentTimeMillis()
        }
    }

    Scaffold(
        topBar = {
            ToolBoxTopBar(
                title = if (isBn) "বিশ্ব ঘড়ি" else "World Clock",
                onBackClick = onBackClick,
                isFavorite = prefs.isFavorite("world_clock"),
                onFavoriteToggle = { prefs.toggleFavorite("world_clock") }
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = CITIES_LIST,
                key = { it.timeZoneId },
                contentType = { "city_card" }
            ) { city ->
                reusableDate.time = currentTimeMillis
                val timeStr = city.timeFormat.format(reusableDate)
                val dateStr = city.dateFormat.format(reusableDate)

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isBn) city.cityBn else city.cityEn,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = city.country,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = dateStr,
                                style = MaterialTheme.typography.labelSmall,
                                color = RedAccent
                            )
                        }

                        Text(
                            text = timeStr,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 20.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
