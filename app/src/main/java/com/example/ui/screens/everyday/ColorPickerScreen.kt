package com.example.ui.screens.everyday

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.data.PreferencesManager
import com.example.localization.AppLanguage
import com.example.ui.components.ToolBoxTopBar
import com.example.ui.components.ToolResultCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.RedAccent

@Composable
fun ColorPickerScreen(
    language: AppLanguage,
    prefs: PreferencesManager,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val isBn = language == AppLanguage.BANGLA

    var r by remember { mutableFloatStateOf(229f) }
    var g by remember { mutableFloatStateOf(57f) }
    var b by remember { mutableFloatStateOf(53f) }

    val currentColor = Color(r.toInt() / 255f, g.toInt() / 255f, b.toInt() / 255f)
    val hexString = String.format("#%02X%02X%02X", r.toInt(), g.toInt(), b.toInt())
    val rgbString = "rgb(${r.toInt()}, ${g.toInt()}, ${b.toInt()})"

    Scaffold(
        topBar = {
            ToolBoxTopBar(
                title = if (isBn) "কালার পিকার ও প্যালেট" else "Color Picker & Palette",
                onBackClick = onBackClick,
                isFavorite = prefs.isFavorite("color_picker"),
                onFavoriteToggle = { prefs.toggleFavorite("color_picker") }
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
            // Live Color Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(currentColor)
                    .border(2.dp, DarkBorder, RoundedCornerShape(20.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            ToolResultCard(
                resultValue = hexString,
                title = if (isBn) "রঙের হেক্স কোড" else "Color Codes",
                subtitle = rgbString
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Sliders for R, G, B
            Text("Red: ${r.toInt()}", color = RedAccent, style = MaterialTheme.typography.labelMedium)
            Slider(
                value = r,
                onValueChange = { r = it },
                valueRange = 0f..255f,
                colors = SliderDefaults.colors(thumbColor = RedAccent, activeTrackColor = RedAccent)
            )

            Text("Green: ${g.toInt()}", color = Color(0xFF4CAF50), style = MaterialTheme.typography.labelMedium)
            Slider(
                value = g,
                onValueChange = { g = it },
                valueRange = 0f..255f,
                colors = SliderDefaults.colors(thumbColor = Color(0xFF4CAF50), activeTrackColor = Color(0xFF4CAF50))
            )

            Text("Blue: ${b.toInt()}", color = Color(0xFF2196F3), style = MaterialTheme.typography.labelMedium)
            Slider(
                value = b,
                onValueChange = { b = it },
                valueRange = 0f..255f,
                colors = SliderDefaults.colors(thumbColor = Color(0xFF2196F3), activeTrackColor = Color(0xFF2196F3))
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isBn) "জনপ্রিয় রঙসমূহ" else "Color Presets",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Preset Swatches
            val presets = listOf(
                Color(0xFFE53935), Color(0xFF1E88E5), Color(0xFF43A047),
                Color(0xFFFB8C00), Color(0xFF8E24AA), Color(0xFF00ACC1),
                Color(0xFFFDD835), Color(0xFFD81B60), Color(0xFF3949AB)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                presets.forEach { preset ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(preset)
                            .clickable {
                                r = preset.red * 255f
                                g = preset.green * 255f
                                b = preset.blue * 255f
                            }
                    )
                }
            }
        }
    }
}
