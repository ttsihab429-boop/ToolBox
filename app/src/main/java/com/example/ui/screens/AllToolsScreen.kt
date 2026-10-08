package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.PreferencesManager
import com.example.localization.AppLanguage
import com.example.localization.Strings
import com.example.model.ToolCategory
import com.example.model.ToolItem
import com.example.model.ToolRegistry
import com.example.ui.components.ToolActions
import com.example.ui.components.ToolCard
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.RedAccent

@Composable
fun AllToolsScreen(
    language: AppLanguage,
    prefs: PreferencesManager,
    selectedCategory: ToolCategory?,
    onToolClick: (ToolItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var activeCategory by remember(selectedCategory) { mutableStateOf(selectedCategory) }
    var searchQuery by remember { mutableStateOf("") }
    val favorites by prefs.favorites.collectAsState()

    val filteredTools = remember(activeCategory, searchQuery) {
        ToolRegistry.allTools.filter { tool ->
            val matchesCategory = activeCategory == null || tool.category == activeCategory
            val matchesSearch = searchQuery.isBlank() ||
                    tool.titleEn.contains(searchQuery, ignoreCase = true) ||
                    tool.titleBn.contains(searchQuery) ||
                    tool.descEn.contains(searchQuery, ignoreCase = true) ||
                    tool.descBn.contains(searchQuery)
            matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = Strings.navTools(language),
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("all_tools_search"),
            placeholder = { Text(Strings.searchPlaceholder(language)) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RedAccent,
                unfocusedBorderColor = DarkBorder,
                focusedContainerColor = DarkSurfaceVariant,
                unfocusedContainerColor = DarkSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category Filter Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = activeCategory == null,
                    onClick = { activeCategory = null },
                    label = { Text(if (language == AppLanguage.BANGLA) "সব টুলস" else "All Tools") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = RedAccent,
                        containerColor = DarkSurfaceElevated
                    )
                )
            }
            items(ToolCategory.entries, key = { it.name }) { cat ->
                val label = when (cat) {
                    ToolCategory.CALCULATORS -> Strings.catCalculators(language)
                    ToolCategory.UNIT_CONVERTER -> Strings.catConverters(language)
                    ToolCategory.DATE_TIME -> Strings.catDateTime(language)
                    ToolCategory.QR_SCANNER -> Strings.catQrScanner(language)
                    ToolCategory.TEXT_TOOLS -> Strings.catTextTools(language)
                    ToolCategory.EVERYDAY_TOOLS -> Strings.catEveryday(language)
                    ToolCategory.BANGLADESH_TOOLS -> Strings.catBangladesh(language)
                    ToolCategory.FILE_TOOLS -> Strings.catFiles(language)
                }
                FilterChip(
                    selected = activeCategory == cat,
                    onClick = { activeCategory = cat },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = RedAccent,
                        containerColor = DarkSurfaceElevated
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tool List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredTools, key = { it.id }) { tool ->
                ToolCard(
                    tool = tool,
                    language = language,
                    isFavorite = favorites.contains(tool.id),
                    onFavoriteToggle = {
                        ToolActions.triggerHaptic(context, prefs.hapticEnabled.value)
                        prefs.toggleFavorite(tool.id)
                    },
                    onClick = { onToolClick(tool) }
                )
            }
        }
    }
}
