package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PreferencesManager
import com.example.localization.AppLanguage
import com.example.localization.Strings
import com.example.model.ToolCategory
import com.example.model.ToolItem
import com.example.model.ToolRegistry
import com.example.ui.components.ToolActions
import com.example.ui.components.ToolCard
import com.example.ui.components.ToolboxLogo
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.RedAccent

@Composable
fun HomeScreen(
    language: AppLanguage,
    prefs: PreferencesManager,
    onToolClick: (ToolItem) -> Unit,
    onNavigateToCategory: (ToolCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val favorites by prefs.favorites.collectAsState()
    val recentIds by prefs.recentToolIds.collectAsState()
    val trackRecents by prefs.trackRecents.collectAsState()

    val popularTools = remember { ToolRegistry.allTools.filter { it.isPopular } }
    val recentTools = remember(recentIds, trackRecents) {
        if (!trackRecents) emptyList()
        else recentIds.mapNotNull { ToolRegistry.getTool(it) }
    }
    val favoriteTools = remember(favorites) {
        favorites.mapNotNull { ToolRegistry.getTool(it) }
    }

    val searchResults = remember(searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else {
            val q = searchQuery.trim().lowercase()
            ToolRegistry.allTools.filter { tool ->
                tool.titleEn.lowercase().contains(q) ||
                tool.titleBn.contains(q) ||
                tool.descEn.lowercase().contains(q) ||
                tool.descBn.contains(q) ||
                tool.category.name.lowercase().contains(q) ||
                tool.keywords.any { it.contains(q) }
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))

            // Header with Logo, Title, and Subtitle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ToolboxLogo(size = 50.dp)

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = Strings.appName(language),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = Strings.tagline(language),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_search_bar"),
                placeholder = { Text(Strings.searchPlaceholder(language)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RedAccent,
                    unfocusedBorderColor = DarkBorder,
                    focusedContainerColor = DarkSurfaceVariant,
                    unfocusedContainerColor = DarkSurfaceVariant
                )
            )
        }

        // Search Results
        if (searchQuery.isNotBlank()) {
            item {
                Text(
                    text = "${searchResults.size} ${if (language == AppLanguage.BANGLA) "টি ফলাফল পাওয়া গেছে" else "tools found"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = RedAccent
                )
            }
            if (searchResults.isEmpty()) {
                item {
                    Text(
                        text = Strings.noResults(language),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(searchResults, key = { it.id }) { tool ->
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
        } else {
            // Recently Used section (if enabled & not empty)
            if (recentTools.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Strings.recentlyUsed(language),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        TextButton(onClick = { prefs.clearRecents() }) {
                            Text(Strings.clear(language), color = RedAccent, style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(recentTools.take(6), key = { "recent_${it.id}" }) { tool ->
                            RecentToolChip(
                                tool = tool,
                                language = language,
                                onClick = { onToolClick(tool) }
                            )
                        }
                    }
                }
            }

            // Categories Grid / Row
            item {
                Text(
                    text = Strings.categories(language),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(10.dp))

                val categories = remember(language) {
                    listOf(
                        ToolCategory.CALCULATORS to Strings.catCalculators(language),
                        ToolCategory.UNIT_CONVERTER to Strings.catConverters(language),
                        ToolCategory.DATE_TIME to Strings.catDateTime(language),
                        ToolCategory.QR_SCANNER to Strings.catQrScanner(language),
                        ToolCategory.TEXT_TOOLS to Strings.catTextTools(language),
                        ToolCategory.EVERYDAY_TOOLS to Strings.catEveryday(language),
                        ToolCategory.BANGLADESH_TOOLS to Strings.catBangladesh(language),
                        ToolCategory.FILE_TOOLS to Strings.catFiles(language)
                    )
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories, key = { it.first.name }) { (cat, title) ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onNavigateToCategory(cat) },
                            shape = RoundedCornerShape(12.dp),
                            color = DarkSurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }

            // Quick Tools / Popular Tools
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = Strings.popularTools(language),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(popularTools, key = { it.id }) { tool ->
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

            // Subtle Developer Credit Footer
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ToolBox • ${Strings.devCredit(language)} • ${Strings.devContact(language)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun RecentToolChip(
    tool: ToolItem,
    language: AppLanguage,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(140.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("recent_tool_${tool.id}"),
        shape = RoundedCornerShape(14.dp),
        color = DarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = null,
                    tint = RedAccent,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = tool.title(language),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}
