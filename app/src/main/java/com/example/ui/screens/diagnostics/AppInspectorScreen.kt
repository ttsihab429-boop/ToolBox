package com.example.ui.screens.diagnostics

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.data.PreferencesManager
import com.example.localization.AppLanguage
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.RedAccent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class InspectedApp(
    val name: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val sizeMb: Double,
    val isSystemApp: Boolean,
    val targetSdk: Int,
    val minSdk: Int,
    val installTime: Long,
    val updateTime: Long,
    val permissions: List<String>
)

enum class AppFilter {
    ALL,
    USER,
    SYSTEM
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppInspectorScreen(
    language: AppLanguage,
    prefs: PreferencesManager,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current

    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf(AppFilter.ALL) }
    val allApps = remember { mutableStateListOf<InspectedApp>() }
    var selectedApp by remember { mutableStateOf<InspectedApp?>(null) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
            val list = mutableListOf<InspectedApp>()

            for (pkg in packages) {
                try {
                    val appInfo = pkg.applicationInfo ?: continue
                    val name = pm.getApplicationLabel(appInfo).toString()
                    val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    val file = File(appInfo.sourceDir)
                    val sizeMb = if (file.exists()) file.length().toDouble() / (1024 * 1024) else 0.0
                    val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        pkg.longVersionCode
                    } else {
                        @Suppress("DEPRECATION")
                        pkg.versionCode.toLong()
                    }
                    val minSdk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) appInfo.minSdkVersion else 21
                    val perms = pkg.requestedPermissions?.toList() ?: emptyList()

                    list.add(
                        InspectedApp(
                            name = name,
                            packageName = pkg.packageName,
                            versionName = pkg.versionName ?: "1.0",
                            versionCode = versionCode,
                            sizeMb = sizeMb,
                            isSystemApp = isSystem,
                            targetSdk = appInfo.targetSdkVersion,
                            minSdk = minSdk,
                            installTime = pkg.firstInstallTime,
                            updateTime = pkg.lastUpdateTime,
                            permissions = perms
                        )
                    )
                } catch (e: Exception) {}
            }

            list.sortBy { it.name.lowercase(Locale.ROOT) }
            withContext(Dispatchers.Main) {
                allApps.clear()
                allApps.addAll(list)
                isLoading = false
            }
        }
    }

    val filteredApps = remember(allApps, searchQuery, activeFilter) {
        allApps.filter { app ->
            val matchesFilter = when (activeFilter) {
                AppFilter.ALL -> true
                AppFilter.USER -> !app.isSystemApp
                AppFilter.SYSTEM -> app.isSystemApp
            }
            val q = searchQuery.trim().lowercase(Locale.ROOT)
            val matchesSearch = q.isBlank() || app.name.lowercase(Locale.ROOT).contains(q) || app.packageName.lowercase(Locale.ROOT).contains(q)
            matchesFilter && matchesSearch
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (language == AppLanguage.BANGLA) "অ্যাপ ইন্সপেক্টর" else "App Inspector",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // Search Box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(if (language == AppLanguage.BANGLA) "অ্যাপ নাম বা প্যাকেজ দিয়ে খুঁজুন…" else "Search installed apps…") },
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

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = activeFilter == AppFilter.ALL,
                    onClick = { activeFilter = AppFilter.ALL },
                    label = { Text("All (${allApps.size})") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RedAccent, containerColor = DarkSurfaceElevated)
                )
                FilterChip(
                    selected = activeFilter == AppFilter.USER,
                    onClick = { activeFilter = AppFilter.USER },
                    label = { Text("User Apps (${allApps.count { !it.isSystemApp }})") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RedAccent, containerColor = DarkSurfaceElevated)
                )
                FilterChip(
                    selected = activeFilter == AppFilter.SYSTEM,
                    onClick = { activeFilter = AppFilter.SYSTEM },
                    label = { Text("System (${allApps.count { it.isSystemApp }})") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RedAccent, containerColor = DarkSurfaceElevated)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = RedAccent)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = if (language == AppLanguage.BANGLA) "অ্যাপ তালিকা লোড হচ্ছে..." else "Analyzing installed apps...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredApps, key = { it.packageName }) { app ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { selectedApp = app },
                            shape = RoundedCornerShape(14.dp),
                            color = DarkSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(DarkSurfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Apps, contentDescription = null, tint = RedAccent, modifier = Modifier.size(22.dp))
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = app.name,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = app.packageName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "v${app.versionName} • ${String.format(Locale.US, "%.1f", app.sizeMb)} MB",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }

                                if (app.isSystemApp) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = DarkSurfaceVariant
                                    ) {
                                        Text(
                                            text = "System",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // App Detail Bottom Sheet
    if (selectedApp != null) {
        val app = selectedApp!!
        ModalBottomSheet(
            onDismissRequest = { selectedApp = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = DarkSurfaceElevated
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Apps, contentDescription = null, tint = RedAccent, modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = app.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                        Text(text = app.packageName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val dateFmt = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "• Version: ${app.versionName} (Build ${app.versionCode})", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                    Text(text = "• APK Size: ${String.format(Locale.US, "%.2f", app.sizeMb)} MB", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                    Text(text = "• Target SDK: Android API ${app.targetSdk} (Min: ${app.minSdk})", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                    Text(text = "• Installed: ${dateFmt.format(Date(app.installTime))}", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                    Text(text = "• Updated: ${dateFmt.format(Date(app.updateTime))}", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                    Text(text = "• Requested Permissions: ${app.permissions.size} permissions", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", app.packageName, null)
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("App Settings")
                    }

                    val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                    if (launchIntent != null) {
                        OutlinedButton(
                            onClick = { context.startActivity(launchIntent) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open App")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
