package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import com.example.data.NotesRepository
import com.example.data.PreferencesManager
import com.example.data.ShoppingRepository
import com.example.data.TodoRepository
import com.example.localization.AppLanguage
import com.example.localization.Strings
import com.example.model.ToolCategory
import com.example.ui.components.ToolActions
import com.example.ui.screens.AllToolsScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.ToolDetailHostScreen
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.RedAccent
import com.example.ui.theme.ToolBoxTheme

enum class MainTab { HOME, TOOLS, FAVORITES, SETTINGS }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = PreferencesManager(applicationContext)
        val notesRepo = NotesRepository(applicationContext)
        val todoRepo = TodoRepository(applicationContext)
        val shoppingRepo = ShoppingRepository(applicationContext)

        setContent {
            val themeMode by prefs.themeMode.collectAsState()
            val language by prefs.language.collectAsState()

            val isDarkTheme = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            ToolBoxTheme(darkTheme = isDarkTheme) {
                var showSplash by remember { mutableStateOf(true) }

                if (showSplash) {
                    SplashScreen(
                        language = language,
                        onFinish = { showSplash = false }
                    )
                } else {
                    ToolBoxMainContent(
                        language = language,
                        prefs = prefs,
                        notesRepository = notesRepo,
                        todoRepository = todoRepo,
                        shoppingRepository = shoppingRepo
                    )
                }
            }
        }
    }
}

@Composable
fun ToolBoxMainContent(
    language: AppLanguage,
    prefs: PreferencesManager,
    notesRepository: NotesRepository,
    todoRepository: TodoRepository,
    shoppingRepository: ShoppingRepository
) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(MainTab.HOME) }
    var activeToolId by remember { mutableStateOf<String?>(null) }
    var selectedCategoryForTools by remember { mutableStateOf<ToolCategory?>(null) }
    val hapticEnabled by prefs.hapticEnabled.collectAsState()
    val animationsEnabled by prefs.animationsEnabled.collectAsState()

    // If an individual tool is open, render its full-screen view
    if (activeToolId != null) {
        ToolDetailHostScreen(
            toolId = activeToolId!!,
            language = language,
            prefs = prefs,
            notesRepository = notesRepository,
            todoRepository = todoRepository,
            shoppingRepository = shoppingRepository,
            onBack = { activeToolId = null }
        )
    } else {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                NavigationBar(
                    containerColor = DarkSurface,
                    tonalElevation = 4.dp,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("main_bottom_nav_bar")
                ) {
                    val tabs = listOf(
                        MainTab.HOME to (Strings.navHome(language) to (Icons.Default.Home to Icons.Outlined.Home)),
                        MainTab.TOOLS to (Strings.navTools(language) to (Icons.Default.Build to Icons.Outlined.Build)),
                        MainTab.FAVORITES to (Strings.navFavorites(language) to (Icons.Default.Favorite to Icons.Outlined.FavoriteBorder)),
                        MainTab.SETTINGS to (Strings.navSettings(language) to (Icons.Default.Settings to Icons.Outlined.Settings))
                    )

                    tabs.forEach { (tab, details) ->
                        val (label, icons) = details
                        val isSelected = currentTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                ToolActions.triggerHaptic(context, hapticEnabled)
                                currentTab = tab
                                if (tab == MainTab.TOOLS) {
                                    selectedCategoryForTools = null
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) icons.first else icons.second,
                                    contentDescription = label
                                )
                            },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = RedAccent,
                                selectedTextColor = RedAccent,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = RedAccent.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Crossfade(
                targetState = currentTab,
                animationSpec = if (animationsEnabled) tween(200) else snap(),
                label = "tab_transition",
                modifier = Modifier.padding(innerPadding)
            ) { tab ->
                when (tab) {
                    MainTab.HOME -> {
                        HomeScreen(
                            language = language,
                            prefs = prefs,
                            onToolClick = { tool ->
                                prefs.recordToolUsed(tool.id)
                                activeToolId = tool.id
                            },
                            onNavigateToCategory = { cat ->
                                selectedCategoryForTools = cat
                                currentTab = MainTab.TOOLS
                            }
                        )
                    }

                    MainTab.TOOLS -> {
                        AllToolsScreen(
                            language = language,
                            prefs = prefs,
                            selectedCategory = selectedCategoryForTools,
                            onToolClick = { tool ->
                                prefs.recordToolUsed(tool.id)
                                activeToolId = tool.id
                            }
                        )
                    }

                    MainTab.FAVORITES -> {
                        FavoritesScreen(
                            language = language,
                            prefs = prefs,
                            onToolClick = { tool ->
                                prefs.recordToolUsed(tool.id)
                                activeToolId = tool.id
                            }
                        )
                    }

                    MainTab.SETTINGS -> {
                        SettingsScreen(
                            language = language,
                            prefs = prefs,
                            notesRepository = notesRepository,
                            todoRepository = todoRepository,
                            shoppingRepository = shoppingRepository
                        )
                    }
                }
            }
        }
    }
}
