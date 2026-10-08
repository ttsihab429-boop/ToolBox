package com.example.ui.screens.everyday

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.data.PreferencesManager
import com.example.localization.AppLanguage
import com.example.ui.components.PrimaryActionButton
import com.example.ui.components.ToolActions
import com.example.ui.components.ToolBoxTopBar
import com.example.ui.components.ToolResultCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.RedAccent
import java.security.SecureRandom

@Composable
fun PasswordGeneratorScreen(
    language: AppLanguage,
    prefs: PreferencesManager,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val isBn = language == AppLanguage.BANGLA

    var length by remember { mutableFloatStateOf(16f) }
    var useUpper by remember { mutableStateOf(true) }
    var useLower by remember { mutableStateOf(true) }
    var useDigits by remember { mutableStateOf(true) }
    var useSymbols by remember { mutableStateOf(true) }

    fun generatePassword(): String {
        val upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val lower = "abcdefghijklmnopqrstuvwxyz"
        val digits = "0123456789"
        val symbols = "!@#$%^&*()_+-=[]{}|;:,.<>?"

        var charPool = ""
        if (useUpper) charPool += upper
        if (useLower) charPool += lower
        if (useDigits) charPool += digits
        if (useSymbols) charPool += symbols
        if (charPool.isEmpty()) charPool = lower

        val random = SecureRandom()
        val len = length.toInt()
        val sb = StringBuilder(len)
        for (i in 0 until len) {
            sb.append(charPool[random.nextInt(charPool.length)])
        }
        return sb.toString()
    }

    var generatedPassword by remember { mutableStateOf(generatePassword()) }

    val strength = when {
        length >= 16 && useUpper && useLower && useDigits && useSymbols -> if (isBn) "খুব শক্তিশালী (Very Strong)" else "Very Strong"
        length >= 12 && (useUpper || useLower) && useDigits -> if (isBn) "শক্তিশালী (Strong)" else "Strong"
        length >= 8 -> if (isBn) "মাঝারি (Moderate)" else "Moderate"
        else -> if (isBn) "দুর্বল (Weak)" else "Weak"
    }

    Scaffold(
        topBar = {
            ToolBoxTopBar(
                title = if (isBn) "পাসওয়ার্ড জেনারেটর" else "Password Generator",
                onBackClick = onBackClick,
                isFavorite = prefs.isFavorite("password_gen"),
                onFavoriteToggle = { prefs.toggleFavorite("password_gen") }
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
            // Generated Password Display Card
            ToolResultCard(
                resultValue = generatedPassword,
                title = if (isBn) "উৎপন্ন পাসওয়ার্ড" else "Generated Password",
                subtitle = "Security Level: $strength"
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Length Slider
            Text(
                text = "${if (isBn) "দৈর্ঘ্য" else "Length"}: ${length.toInt()}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Slider(
                value = length,
                onValueChange = {
                    length = it
                    generatedPassword = generatePassword()
                },
                valueRange = 6f..32f,
                steps = 25,
                colors = SliderDefaults.colors(thumbColor = RedAccent, activeTrackColor = RedAccent)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Option Toggles
            OptionToggleRow(if (isBn) "বড় হাতের অক্ষর (A-Z)" else "Uppercase Letters (A-Z)", useUpper) {
                useUpper = it
                generatedPassword = generatePassword()
            }
            OptionToggleRow(if (isBn) "ছোট হাতের অক্ষর (a-z)" else "Lowercase Letters (a-z)", useLower) {
                useLower = it
                generatedPassword = generatePassword()
            }
            OptionToggleRow(if (isBn) "সংখ্যা (0-9)" else "Digits (0-9)", useDigits) {
                useDigits = it
                generatedPassword = generatePassword()
            }
            OptionToggleRow(if (isBn) "প্রতীক ও চিহ্ন (!@#$)" else "Symbols (!@#$%^&*)", useSymbols) {
                useSymbols = it
                generatedPassword = generatePassword()
            }

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryActionButton(
                text = if (isBn) "নতুন পাসওয়ার্ড তৈরি করুন" else "Regenerate Password",
                onClick = {
                    ToolActions.triggerHaptic(context, prefs.hapticEnabled.value)
                    generatedPassword = generatePassword()
                }
            )
        }
    }
}

@Composable
fun OptionToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = RedAccent, checkedTrackColor = RedAccent.copy(alpha = 0.3f))
        )
    }
}
