package com.example.ui.screens.everyday

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.data.PreferencesManager
import com.example.localization.AppLanguage
import com.example.ui.components.ToolActions
import com.example.ui.components.ToolBoxTopBar
import com.example.ui.components.ToolInputField
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.RedAccent

data class CheckItem(val text: String, var isChecked: Boolean = false)

@Composable
fun ChecklistScreen(
    language: AppLanguage,
    prefs: PreferencesManager,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val isBn = language == AppLanguage.BANGLA
    var selectedTemplate by remember { mutableIntStateOf(0) } // 0: Travel, 1: Daily, 2: Custom

    val travelItems = remember {
        mutableStateListOf(
            CheckItem("Passport / NID Card", false),
            CheckItem("Phone Charger & Power Bank", false),
            CheckItem("Toothbrush & Toiletries", false),
            CheckItem("Emergency Medicine / First Aid", false),
            CheckItem("Extra Clothes & Shoes", false),
            CheckItem("Cash & Credit Cards", false),
            CheckItem("House Keys Locked", false)
        )
    }

    val dailyItems = remember {
        mutableStateListOf(
            CheckItem("Morning Exercise / Walk", false),
            CheckItem("Drink 2L Water", false),
            CheckItem("Check Priority Emails", false),
            CheckItem("Read 15 mins", false),
            CheckItem("Review Budget / Expenses", false)
        )
    }

    var customItemText by remember { mutableStateOf("") }
    val currentList = if (selectedTemplate == 0) travelItems else dailyItems

    Scaffold(
        topBar = {
            ToolBoxTopBar(
                title = if (isBn) "রুটিন চেকলিস্ট" else "Routine Checklist",
                onBackClick = onBackClick,
                isFavorite = prefs.isFavorite("checklist"),
                onFavoriteToggle = { prefs.toggleFavorite("checklist") },
                actions = {
                    IconButton(
                        onClick = {
                            currentList.forEachIndexed { i, item ->
                                currentList[i] = item.copy(isChecked = false)
                            }
                        },
                        modifier = Modifier.testTag("reset_checklist_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset Checklist", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedTemplate == 0,
                    onClick = { selectedTemplate = 0 },
                    label = { Text(if (isBn) "ভ্রমণ প্যাকিং" else "Travel Packing") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RedAccent, containerColor = DarkSurfaceElevated)
                )
                FilterChip(
                    selected = selectedTemplate == 1,
                    onClick = { selectedTemplate = 1 },
                    label = { Text(if (isBn) "দৈনিক রুটিন" else "Daily Routine") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RedAccent, containerColor = DarkSurfaceElevated)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Add custom checkitem
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ToolInputField(
                    value = customItemText,
                    onValueChange = { customItemText = it },
                    label = if (isBn) "আইটেম যোগ করুন..." else "Add item...",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.size(8.dp))
                IconButton(
                    onClick = {
                        if (customItemText.isNotBlank()) {
                            currentList.add(CheckItem(customItemText.trim(), false))
                            customItemText = ""
                        }
                    },
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(RedAccent)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Header
            val doneCount = currentList.count { it.isChecked }
            Text(
                text = "${if (isBn) "সম্পন্ন" else "Completed"}: $doneCount / ${currentList.size}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = RedAccent
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(currentList, key = { index, item -> "${item.text}_$index" }) { index, item ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        color = DarkSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = item.isChecked,
                                onCheckedChange = {
                                    ToolActions.triggerHaptic(context, prefs.hapticEnabled.value)
                                    currentList[index] = item.copy(isChecked = it)
                                },
                                colors = CheckboxDefaults.colors(checkedColor = RedAccent)
                            )
                            Text(
                                text = item.text,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None
                                ),
                                color = if (item.isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
