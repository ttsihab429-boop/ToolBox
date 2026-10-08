package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NotesRepository
import com.example.data.PreferencesManager
import com.example.data.ShoppingRepository
import com.example.data.TodoRepository
import com.example.localization.AppLanguage
import com.example.localization.Strings
import com.example.ui.components.ToolActions
import com.example.ui.components.ToolboxLogo
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.RedAccent

@Composable
fun SettingsScreen(
    language: AppLanguage,
    prefs: PreferencesManager,
    notesRepository: NotesRepository,
    todoRepository: TodoRepository,
    shoppingRepository: ShoppingRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isBn = language == AppLanguage.BANGLA

    val themeMode by prefs.themeMode.collectAsState()
    val hapticEnabled by prefs.hapticEnabled.collectAsState()
    val animationsEnabled by prefs.animationsEnabled.collectAsState()
    val trackRecents by prefs.trackRecents.collectAsState()

    var showClearAllConfirm by remember { mutableStateOf(false) }
    var showClearNotesConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = Strings.navSettings(language),
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Appearance
        SettingsSectionHeader(title = Strings.appearance(language), icon = Icons.Default.Palette)

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (isBn) "থিম নির্বাচন করুন" else "Select App Theme",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "dark" to Strings.themeDark(language),
                        "light" to Strings.themeLight(language),
                        "system" to Strings.themeSystem(language)
                    ).forEach { (mode, label) ->
                        FilterChip(
                            selected = themeMode == mode,
                            onClick = {
                                ToolActions.triggerHaptic(context, hapticEnabled)
                                prefs.setThemeMode(mode)
                            },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RedAccent,
                                containerColor = DarkSurfaceElevated
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Language
        SettingsSectionHeader(title = Strings.language(language), icon = Icons.Default.Language)

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (isBn) "অ্যাপের ভাষা পরিবর্তন করুন" else "Choose Display Language",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AppLanguage.entries.forEach { lang ->
                        FilterChip(
                            selected = language == lang,
                            onClick = {
                                ToolActions.triggerHaptic(context, hapticEnabled)
                                prefs.setLanguage(lang)
                            },
                            label = { Text(lang.nativeName, style = MaterialTheme.typography.labelMedium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RedAccent,
                                containerColor = DarkSurfaceElevated
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Preferences
        SettingsSectionHeader(title = Strings.preferences(language), icon = Icons.Default.Tune)

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                SettingsSwitchRow(
                    label = Strings.hapticFeedback(language),
                    checked = hapticEnabled,
                    onCheckedChange = { prefs.setHapticEnabled(it) }
                )
                HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 8.dp))
                SettingsSwitchRow(
                    label = Strings.enableAnimations(language),
                    checked = animationsEnabled,
                    onCheckedChange = { prefs.setAnimationsEnabled(it) }
                )
                HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 8.dp))
                SettingsSwitchRow(
                    label = Strings.trackRecents(language),
                    checked = trackRecents,
                    onCheckedChange = { prefs.setTrackRecents(it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Data & Storage
        SettingsSectionHeader(title = Strings.dataManagement(language), icon = Icons.Default.DeleteForever)

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                SettingsActionRow(Strings.clearRecents(language)) {
                    prefs.clearRecents()
                    Toast.makeText(context, if (isBn) "ইতিহাস ক্লিয়ার করা হয়েছে" else "Recents cleared", Toast.LENGTH_SHORT).show()
                }
                HorizontalDivider(color = DarkBorder)
                SettingsActionRow(Strings.clearFavorites(language)) {
                    prefs.clearFavorites()
                    Toast.makeText(context, if (isBn) "পছন্দের তালিকা ক্লিয়ার করা হয়েছে" else "Favorites cleared", Toast.LENGTH_SHORT).show()
                }
                HorizontalDivider(color = DarkBorder)
                SettingsActionRow(Strings.clearNotes(language)) {
                    showClearNotesConfirm = true
                }
                HorizontalDivider(color = DarkBorder)
                SettingsActionRow(Strings.clearAllData(language), isDestructive = true) {
                    showClearAllConfirm = true
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // About & Developer Section
        SettingsSectionHeader(title = Strings.aboutApp(language), icon = Icons.Default.Info)

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ToolboxLogo(size = 46.dp)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = Strings.appName(language),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Version 1.0.0 • Production Build",
                            style = MaterialTheme.typography.labelSmall,
                            color = RedAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = DarkBorder)
                Spacer(modifier = Modifier.height(12.dp))

                // Developer Credit (Prominently but elegantly placed)
                Text(
                    text = Strings.devCredit(language),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Developer Contact: ${Strings.devContact(language)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = DarkBorder)
                Spacer(modifier = Modifier.height(12.dp))

                // Privacy Note
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = RedAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Strings.privacyInfo(language),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = Strings.privacyDesc(language),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }

    // Confirm Clear Notes Dialog
    if (showClearNotesConfirm) {
        AlertDialog(
            onDismissRequest = { showClearNotesConfirm = false },
            title = { Text(if (isBn) "নোট ও টাস্ক মুছবেন?" else "Clear Notes & Tasks?") },
            text = { Text(if (isBn) "আপনার সংরক্ষিত সব নোট, টু-ডু এবং শপিং তালিকা মুছে ফেলা হবে।" else "All your saved notes, tasks, and shopping items will be erased.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        notesRepository.clearAllNotes()
                        todoRepository.clearAll()
                        shoppingRepository.clearAll()
                        showClearNotesConfirm = false
                        Toast.makeText(context, if (isBn) "নোট ও টাস্ক মোছা হয়েছে" else "Notes and tasks cleared", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(if (isBn) "মুছুন" else "Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearNotesConfirm = false }) {
                    Text(if (isBn) "বাতিল" else "Cancel")
                }
            },
            containerColor = DarkSurfaceElevated
        )
    }

    // Confirm Clear All Dialog
    if (showClearAllConfirm) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirm = false },
            title = { Text(if (isBn) "সব ডাটা রিসেট করবেন?" else "Reset All Data?") },
            text = { Text(if (isBn) "অ্যাপের যাবতীয় সেটিংস, পছন্দের তালিকা, ইতিহাস এবং নোট সম্পূর্ণ রিসেট হবে।" else "This will reset all favorites, history, scratchpad, notes, and local preferences to default.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        prefs.clearAll()
                        notesRepository.clearAllNotes()
                        todoRepository.clearAll()
                        shoppingRepository.clearAll()
                        showClearAllConfirm = false
                        Toast.makeText(context, if (isBn) "সব ডাটা রিসেট সম্পন্ন" else "All data reset successfully", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(if (isBn) "সম্পূর্ণ রিসেট" else "Reset All", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllConfirm = false }) {
                    Text(if (isBn) "বাতিল" else "Cancel")
                }
            },
            containerColor = DarkSurfaceElevated
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = RedAccent, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SettingsSwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
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

@Composable
fun SettingsActionRow(label: String, isDestructive: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (isDestructive) FontWeight.SemiBold else FontWeight.Normal
            ),
            color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}
