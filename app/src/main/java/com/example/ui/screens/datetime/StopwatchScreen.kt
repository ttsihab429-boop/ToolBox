package com.example.ui.screens.datetime

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PreferencesManager
import com.example.localization.AppLanguage
import com.example.ui.components.ToolActions
import com.example.ui.components.ToolBoxTopBar
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.RedAccent
import kotlinx.coroutines.delay

@Composable
fun StopwatchScreen(
    language: AppLanguage,
    prefs: PreferencesManager,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var isRunning by remember { mutableStateOf(false) }
    var elapsedMillis by remember { mutableLongStateOf(0L) }
    val laps = remember { mutableStateListOf<Long>() }

    LaunchedEffect(isRunning) {
        var lastTime = System.currentTimeMillis()
        while (isRunning) {
            delay(10)
            val now = System.currentTimeMillis()
            elapsedMillis += (now - lastTime)
            lastTime = now
        }
    }

    fun formatTime(ms: Long): String {
        val minutes = (ms / 60000) % 60
        val seconds = (ms / 1000) % 60
        val millis = (ms % 1000) / 10
        return String.format("%02d:%02d.%02d", minutes, seconds, millis)
    }

    Scaffold(
        topBar = {
            ToolBoxTopBar(
                title = if (language == AppLanguage.BANGLA) "স্টপওয়াচ" else "Stopwatch",
                onBackClick = onBackClick,
                isFavorite = prefs.isFavorite("stopwatch"),
                onFavoriteToggle = { prefs.toggleFavorite("stopwatch") }
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Big Timer Display
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .background(DarkSurface)
                    .border(2.dp, RedAccent.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = formatTime(elapsedMillis),
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    modifier = Modifier.testTag("stopwatch_display")
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset Button
                IconButton(
                    onClick = {
                        ToolActions.triggerHaptic(context, prefs.hapticEnabled.value)
                        isRunning = false
                        elapsedMillis = 0L
                        laps.clear()
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceElevated)
                        .testTag("stopwatch_reset_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Play / Pause Button
                IconButton(
                    onClick = {
                        ToolActions.triggerHaptic(context, prefs.hapticEnabled.value)
                        isRunning = !isRunning
                    },
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(RedAccent)
                        .testTag("stopwatch_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Start",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Lap Button
                IconButton(
                    onClick = {
                        if (isRunning) {
                            ToolActions.triggerHaptic(context, prefs.hapticEnabled.value)
                            laps.add(0, elapsedMillis)
                        }
                    },
                    enabled = isRunning,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceElevated)
                        .testTag("stopwatch_lap_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = "Lap",
                        tint = if (isRunning) RedAccent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Laps List
            if (laps.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Lap #", style = MaterialTheme.typography.labelMedium, color = RedAccent)
                            Text(text = "Time", style = MaterialTheme.typography.labelMedium, color = RedAccent)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(color = DarkBorder)
                    }
                    itemsIndexed(laps) { index, lapTime ->
                        val lapNumber = laps.size - index
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Lap $lapNumber",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatTime(lapTime),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        HorizontalDivider(color = DarkBorder, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}
