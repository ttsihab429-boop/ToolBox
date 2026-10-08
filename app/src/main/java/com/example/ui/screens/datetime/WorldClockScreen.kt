package com.example.ui.screens.datetime

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

data class CityClock(val cityEn: String, val cityBn: String, val timeZoneId: String, val country: String)

@Composable
fun WorldClockScreen(
    language: AppLanguage,
    prefs: PreferencesManager,
    onBackClick: () -> Unit
) {
    val isBn = language == AppLanguage.BANGLA
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTimeMillis = System.currentTimeMillis()
        }
    }

    val cities = listOf(
        CityClock("Dhaka", "ঢাকা", "Asia/Dhaka", "Bangladesh (GMT+6)"),
        CityClock("London", "লন্ডন", "Europe/London", "United Kingdom (GMT+0)"),
        CityClock("New York", "নিউইয়র্ক", "America/New_York", "United States (GMT-4)"),
        CityClock("Tokyo", "টোকিও", "Asia/Tokyo", "Japan (GMT+9)"),
        CityClock("Dubai", "দুবাই", "Asia/Dubai", "UAE (GMT+4)"),
        CityClock("Singapore", "সিঙ্গাপুর", "Asia/Singapore", "Singapore (GMT+8)"),
        CityClock("Sydney", "সিডনি", "Australia/Sydney", "Australia (GMT+10)"),
        CityClock("Los Angeles", "লস অ্যাঞ্জেলেস", "America/Los_Angeles", "United States (GMT-7)"),
        CityClock("UTC / GMT", "ইউটিসি", "UTC", "Coordinated Universal Time")
    )

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
            items(cities) { city ->
                val tz = TimeZone.getTimeZone(city.timeZoneId)
                val timeFormat = SimpleDateFormat("hh:mm:ss a", Locale.US).apply { timeZone = tz }
                val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.US).apply { timeZone = tz }
                val currentDate = Date(currentTimeMillis)

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
                                text = dateFormat.format(currentDate),
                                style = MaterialTheme.typography.labelSmall,
                                color = RedAccent
                            )
                        }

                        Text(
                            text = timeFormat.format(currentDate),
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
