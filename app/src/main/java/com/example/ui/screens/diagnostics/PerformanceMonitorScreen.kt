package com.example.ui.screens.diagnostics

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PreferencesManager
import com.example.localization.AppLanguage
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.RedAccent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceMonitorScreen(
    language: AppLanguage,
    prefs: PreferencesManager,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current

    var totalRamGb by remember { mutableDoubleStateOf(0.0) }
    var usedRamGb by remember { mutableDoubleStateOf(0.0) }
    var ramProgress by remember { mutableFloatStateOf(0f) }
    var isLowRam by remember { mutableStateOf(false) }

    var totalStorageGb by remember { mutableDoubleStateOf(0.0) }
    var usedStorageGb by remember { mutableDoubleStateOf(0.0) }
    var storageProgress by remember { mutableFloatStateOf(0f) }

    var batteryPct by remember { mutableStateOf("0%") }
    var batteryTemp by remember { mutableFloatStateOf(0f) }
    var batteryVoltage by remember { mutableStateOf("0 mV") }
    var isCharging by remember { mutableStateOf(false) }

    var uptimeString by remember { mutableStateOf("0h 0m") }
    val cpuCores = remember { Runtime.getRuntime().availableProcessors() }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val act = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            val memInfo = ActivityManager.MemoryInfo()

            while (isActive) {
                // Update RAM
                act?.getMemoryInfo(memInfo)
                val totalR = memInfo.totalMem.toDouble() / (1024 * 1024 * 1024)
                val availR = memInfo.availMem.toDouble() / (1024 * 1024 * 1024)
                val usedR = totalR - availR
                val ramProg = if (totalR > 0) (usedR / totalR).toFloat() else 0f

                // Update Storage
                val stat = StatFs(Environment.getDataDirectory().path)
                val totalS = stat.totalBytes.toDouble() / (1024 * 1024 * 1024)
                val availS = stat.availableBytes.toDouble() / (1024 * 1024 * 1024)
                val usedS = totalS - availS
                val storProg = if (totalS > 0) (usedS / totalS).toFloat() else 0f

                // Update Battery
                val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                var bPct = "0%"
                var bTemp = 0f
                var bVolt = "0 mV"
                var bCharge = false
                if (intent != null) {
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    val p = if (level >= 0 && scale > 0) (level * 100) / scale else 0
                    bPct = "$p%"
                    bTemp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10.0f
                    bVolt = "${intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)} mV"
                    bCharge = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1) == BatteryManager.BATTERY_STATUS_CHARGING
                }

                // Update Uptime
                val uptimeMs = SystemClock.elapsedRealtime()
                val hrs = uptimeMs / (1000 * 60 * 60)
                val mins = (uptimeMs / (1000 * 60)) % 60
                val uptimeStr = "${hrs}h ${mins}m"

                withContext(Dispatchers.Main) {
                    totalRamGb = totalR
                    usedRamGb = usedR
                    ramProgress = ramProg
                    isLowRam = memInfo.lowMemory

                    totalStorageGb = totalS
                    usedStorageGb = usedS
                    storageProgress = storProg

                    batteryPct = bPct
                    batteryTemp = bTemp
                    batteryVoltage = bVolt
                    isCharging = bCharge

                    uptimeString = uptimeStr
                }

                delay(2000)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (language == AppLanguage.BANGLA) "পারফরম্যান্স মনিটর" else "Performance Monitor",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Info Disclaimer Banner
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = DarkSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Speed, contentDescription = null, tint = RedAccent, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (language == AppLanguage.BANGLA) "প্রকৃত হার্ডওয়্যার মেট্রিক্স (কোনো ভুয়া বুস্টার বা ফেইক স্কোর নয়)" else "Real hardware telemetry from Android OS. No fake boosters or scores.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // RAM Card
            item {
                MetricProgressCard(
                    title = if (language == AppLanguage.BANGLA) "র‍্যাম মেমোরি ব্যবহার" else "RAM Memory",
                    subtitle = "${String.format(Locale.US, "%.1f", usedRamGb)} GB / ${String.format(Locale.US, "%.1f", totalRamGb)} GB",
                    percent = (ramProgress * 100).toInt(),
                    progress = ramProgress,
                    icon = Icons.Default.Memory,
                    extraInfo = if (isLowRam) "Low Memory Warning Active" else "RAM Status Stable"
                )
            }

            // Storage Card
            item {
                MetricProgressCard(
                    title = if (language == AppLanguage.BANGLA) "ইন্টারনাল স্টোরেজ" else "Internal Storage",
                    subtitle = "${String.format(Locale.US, "%.1f", usedStorageGb)} GB / ${String.format(Locale.US, "%.1f", totalStorageGb)} GB",
                    percent = (storageProgress * 100).toInt(),
                    progress = storageProgress,
                    icon = Icons.Default.Storage,
                    extraInfo = "${String.format(Locale.US, "%.1f", totalStorageGb - usedStorageGb)} GB Available"
                )
            }

            // Battery & Temperature Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricMiniCard(
                        modifier = Modifier.weight(1f),
                        title = if (language == AppLanguage.BANGLA) "ব্যাটারি লেভেল" else "Battery",
                        value = batteryPct,
                        subvalue = if (isCharging) "Charging" else "Discharging",
                        icon = Icons.Default.BatteryChargingFull,
                        accentColor = GreenSuccess
                    )
                    MetricMiniCard(
                        modifier = Modifier.weight(1f),
                        title = if (language == AppLanguage.BANGLA) "আসল তাপমাত্রা" else "Temperature",
                        value = "${String.format(Locale.US, "%.1f", batteryTemp)}°C",
                        subvalue = if (batteryTemp > 42f) "Hot" else "Normal",
                        icon = Icons.Default.Thermostat,
                        accentColor = if (batteryTemp > 42f) RedAccent else GreenSuccess
                    )
                }
            }

            // System Uptime & CPU
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricMiniCard(
                        modifier = Modifier.weight(1f),
                        title = if (language == AppLanguage.BANGLA) "সিপিইউ কোর" else "CPU Cores",
                        value = "$cpuCores Cores",
                        subvalue = Build.HARDWARE,
                        icon = Icons.Default.Speed,
                        accentColor = RedAccent
                    )
                    MetricMiniCard(
                        modifier = Modifier.weight(1f),
                        title = if (language == AppLanguage.BANGLA) "সিস্টেম আপটাইম" else "Device Uptime",
                        value = uptimeString,
                        subvalue = "Since last boot",
                        icon = Icons.Default.Timer,
                        accentColor = RedAccent
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun MetricProgressCard(
    title: String,
    subtitle: String,
    percent: Int,
    progress: Float,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    extraInfo: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = DarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = RedAccent, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Text(
                    text = "$percent%",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (percent > 88) RedAccent else GreenSuccess
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (percent > 88) RedAccent else GreenSuccess,
                trackColor = DarkSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = extraInfo,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MetricMiniCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subvalue: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: androidx.compose.ui.graphics.Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = DarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subvalue, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
