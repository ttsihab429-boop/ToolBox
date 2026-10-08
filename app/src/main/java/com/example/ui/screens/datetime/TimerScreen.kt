package com.example.ui.screens.datetime

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.RedAccent
import kotlinx.coroutines.delay

@Composable
fun TimerScreen(
    language: AppLanguage,
    prefs: PreferencesManager,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val isBn = language == AppLanguage.BANGLA
    var totalSeconds by remember { mutableIntStateOf(60) }
    var remainingSeconds by remember { mutableIntStateOf(60) }
    var isRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning, remainingSeconds) {
        if (isRunning && remainingSeconds > 0) {
            delay(1000)
            remainingSeconds -= 1
            if (remainingSeconds == 0) {
                isRunning = false
                ToolActions.triggerHaptic(context, true)
            }
        }
    }

    val progress by animateFloatAsState(
        targetValue = if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds.toFloat() else 0f,
        label = "timer_progress"
    )

    fun formatCountdown(sec: Int): String {
        val m = sec / 60
        val s = sec % 60
        return String.format("%02d:%02d", m, s)
    }

    Scaffold(
        topBar = {
            ToolBoxTopBar(
                title = if (isBn) "কাউন্টডাউন টাইমার" else "Countdown Timer",
                onBackClick = onBackClick,
                isFavorite = prefs.isFavorite("countdown_timer"),
                onFavoriteToggle = { prefs.toggleFavorite("countdown_timer") }
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
            Spacer(modifier = Modifier.height(20.dp))

            // Quick Preset Chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(1, 3, 5, 10, 25).forEach { mins ->
                    FilterChip(
                        selected = totalSeconds == mins * 60,
                        onClick = {
                            if (!isRunning) {
                                totalSeconds = mins * 60
                                remainingSeconds = mins * 60
                            }
                        },
                        label = { Text("${mins}m") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RedAccent,
                            containerColor = DarkSurfaceElevated
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Circular Progress with Countdown
            Box(
                modifier = Modifier.size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = DarkSurfaceElevated,
                    strokeWidth = 10.dp
                )
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = RedAccent,
                    strokeWidth = 10.dp
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formatCountdown(remainingSeconds),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 44.sp,
                            color = Color.White
                        ),
                        modifier = Modifier.testTag("timer_countdown_display")
                    )
                    if (remainingSeconds == 0) {
                        Text(
                            text = if (isBn) "সময় সমাপ্ত!" else "Time's up!",
                            style = MaterialTheme.typography.titleMedium,
                            color = RedAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Control Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        ToolActions.triggerHaptic(context, prefs.hapticEnabled.value)
                        isRunning = false
                        remainingSeconds = totalSeconds
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceElevated)
                        .testTag("timer_reset_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Timer",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = {
                        ToolActions.triggerHaptic(context, prefs.hapticEnabled.value)
                        if (remainingSeconds == 0) remainingSeconds = totalSeconds
                        isRunning = !isRunning
                    },
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(RedAccent)
                        .testTag("timer_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Start",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}
