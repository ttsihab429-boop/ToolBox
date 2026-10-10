package com.example.ui.screens.diagnostics

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import android.util.DisplayMetrics
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PreferencesManager
import com.example.localization.AppLanguage
import com.example.localization.Strings
import com.example.ui.components.ToolActions
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.RedAccent
import java.util.Locale

enum class DeviceInfoTab {
    SYSTEM,
    DISPLAY,
    CPU_RAM,
    STORAGE,
    BATTERY,
    SENSORS
}

data class SpecRow(
    val label: String,
    val value: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceToolsScreen(
    initialTab: DeviceInfoTab = DeviceInfoTab.SYSTEM,
    language: AppLanguage,
    prefs: PreferencesManager,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(initialTab) }
    val sensors = remember(context) { getSensorsList(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (language == AppLanguage.BANGLA) "ডিভাইস ও সিস্টেম তথ্য" else "Device Tools & Specs",
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
        ) {
            // Horizontal Tab Selector
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(DeviceInfoTab.entries) { tab ->
                    val tabName = when (tab) {
                        DeviceInfoTab.SYSTEM -> if (language == AppLanguage.BANGLA) "সিস্টেম" else "System & OS"
                        DeviceInfoTab.DISPLAY -> if (language == AppLanguage.BANGLA) "ডিসপ্লে" else "Display"
                        DeviceInfoTab.CPU_RAM -> if (language == AppLanguage.BANGLA) "সিপিইউ ও র্যাম" else "CPU & RAM"
                        DeviceInfoTab.STORAGE -> if (language == AppLanguage.BANGLA) "স্টোরেজ" else "Storage"
                        DeviceInfoTab.BATTERY -> if (language == AppLanguage.BANGLA) "ব্যাটারি" else "Battery"
                        DeviceInfoTab.SENSORS -> if (language == AppLanguage.BANGLA) "সেন্সর" else "Sensors"
                    }

                    FilterChip(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        label = { Text(tabName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RedAccent,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = DarkSurfaceElevated
                        )
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                }

                when (selectedTab) {
                    DeviceInfoTab.SYSTEM -> {
                        item {
                            SpecSectionCard(
                                title = if (language == AppLanguage.BANGLA) "ডিভাইস ও ওএস বিবরণ" else "Device & OS Specifications",
                                icon = Icons.Default.PhoneAndroid,
                                rows = getSystemSpecs(context, language)
                            )
                        }
                    }

                    DeviceInfoTab.DISPLAY -> {
                        item {
                            SpecSectionCard(
                                title = if (language == AppLanguage.BANGLA) "ডিসপ্লে ও স্ক্রিন তথ্য" else "Display & Screen Parameters",
                                icon = Icons.Default.Tv,
                                rows = getDisplaySpecs(context)
                            )
                        }
                    }

                    DeviceInfoTab.CPU_RAM -> {
                        item {
                            val ramInfo = getRamUsage(context)
                            UsageGaugeCard(
                                title = if (language == AppLanguage.BANGLA) "র‍্যাম মেমোরি ব্যবহার" else "RAM Memory Utilization",
                                usedText = "${String.format(Locale.US, "%.2f", ramInfo.second)} GB Used",
                                totalText = "${String.format(Locale.US, "%.2f", ramInfo.first)} GB Total",
                                progress = ramInfo.third
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            SpecSectionCard(
                                title = if (language == AppLanguage.BANGLA) "সিপিইউ ও হার্ডওয়্যার তথ্য" else "CPU & Hardware Specs",
                                icon = Icons.Default.Memory,
                                rows = getCpuSpecs()
                            )
                        }
                    }

                    DeviceInfoTab.STORAGE -> {
                        item {
                            val storage = getStorageUsage()
                            UsageGaugeCard(
                                title = if (language == AppLanguage.BANGLA) "ইন্টারনাল স্টোরেজ" else "Internal Storage Utilization",
                                usedText = "${String.format(Locale.US, "%.1f", storage.second)} GB Used",
                                totalText = "${String.format(Locale.US, "%.1f", storage.first)} GB Total",
                                progress = storage.third
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            SpecSectionCard(
                                title = if (language == AppLanguage.BANGLA) "স্টোরেজ পার্টিশন বিবরণ" else "Storage Partitions",
                                icon = Icons.Default.Storage,
                                rows = listOf(
                                    SpecRow("Internal Total", "${String.format(Locale.US, "%.2f", storage.first)} GB"),
                                    SpecRow("Internal Used", "${String.format(Locale.US, "%.2f", storage.second)} GB"),
                                    SpecRow("Internal Free", "${String.format(Locale.US, "%.2f", storage.first - storage.second)} GB"),
                                    SpecRow("Data Path", Environment.getDataDirectory().absolutePath),
                                    SpecRow("Storage State", Environment.getExternalStorageState())
                                )
                            )
                        }
                    }

                    DeviceInfoTab.BATTERY -> {
                        item {
                            SpecSectionCard(
                                title = if (language == AppLanguage.BANGLA) "ব্যাটারি বিবরণ ও স্বাস্থ্য" else "Battery Health & Specs",
                                icon = Icons.Default.BatteryChargingFull,
                                rows = getBatterySpecs(context)
                            )
                        }
                    }

                    DeviceInfoTab.SENSORS -> {
                        item {
                            Text(
                                text = "${sensors.size} Hardware Sensors Detected",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        itemsIndexed(sensors, key = { index, sensor -> "${sensor.type}_${sensor.name}_$index" }) { _, sensor ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
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
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(DarkSurfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Sensors, contentDescription = null, tint = RedAccent, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = sensor.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        Text(text = "Vendor: ${sensor.vendor} • Power: ${sensor.power} mA", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = "Max Range: ${sensor.maximumRange} • Res: ${sensor.resolution}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun UsageGaugeCard(
    title: String,
    usedText: String,
    totalText: String,
    progress: Float
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = DarkSurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = if (progress > 0.88f) RedAccent else GreenSuccess,
                trackColor = DarkSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = usedText, fontWeight = FontWeight.SemiBold, color = if (progress > 0.88f) RedAccent else GreenSuccess)
                Text(text = "${(progress * 100).toInt()}% • $totalText", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun SpecSectionCard(
    title: String,
    icon: ImageVector,
    rows: List<SpecRow>
) {
    val context = LocalContext.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = DarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = RedAccent, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.height(14.dp))
            rows.forEachIndexed { index, row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = row.label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = row.value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = { ToolActions.copyToClipboard(context, row.value) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                        }
                    }
                }
                if (index < rows.lastIndex) {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkBorder.copy(alpha = 0.5f)))
                }
            }
        }
    }
}

fun getSystemSpecs(context: Context, lang: AppLanguage): List<SpecRow> {
    val uptimeHours = SystemClock.elapsedRealtime() / (1000 * 60 * 60)
    val uptimeMins = (SystemClock.elapsedRealtime() / (1000 * 60)) % 60

    return listOf(
        SpecRow("Model", Build.MODEL),
        SpecRow("Manufacturer", Build.MANUFACTURER.replaceFirstChar { it.titlecase() }),
        SpecRow("Brand", Build.BRAND.replaceFirstChar { it.titlecase() }),
        SpecRow("Device / Product", "${Build.DEVICE} (${Build.PRODUCT})"),
        SpecRow("Android Version", "Android ${Build.VERSION.RELEASE}"),
        SpecRow("API Level", "SDK ${Build.VERSION.SDK_INT}"),
        SpecRow("Security Patch", if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Build.VERSION.SECURITY_PATCH else "Unsupported"),
        SpecRow("Build ID", Build.ID),
        SpecRow("Bootloader", Build.BOOTLOADER),
        SpecRow("Board", Build.BOARD),
        SpecRow("Hardware", Build.HARDWARE),
        SpecRow("System Uptime", "${uptimeHours}h ${uptimeMins}m")
    )
}

fun getDisplaySpecs(context: Context): List<SpecRow> {
    val wm = context.getSystemService(Context.WINDOW_SERVICE) as? android.view.WindowManager
    val metrics = DisplayMetrics()
    @Suppress("DEPRECATION")
    wm?.defaultDisplay?.getRealMetrics(metrics)

    @Suppress("DEPRECATION")
    val refreshRate = wm?.defaultDisplay?.refreshRate ?: 60f

    return listOf(
        SpecRow("Resolution", "${metrics.widthPixels} x ${metrics.heightPixels} px"),
        SpecRow("Density", "${metrics.densityDpi} dpi (${metrics.density}x)"),
        SpecRow("Refresh Rate", "${String.format(Locale.US, "%.1f", refreshRate)} Hz"),
        SpecRow("Aspect Ratio", "${String.format(Locale.US, "%.2f", metrics.heightPixels.toFloat() / metrics.widthPixels.coerceAtLeast(1))}:1"),
        SpecRow("Scaled Font Density", "${metrics.scaledDensity}x")
    )
}

fun getCpuSpecs(): List<SpecRow> {
    val cores = Runtime.getRuntime().availableProcessors()
    val abis = Build.SUPPORTED_ABIS.joinToString(", ")
    return listOf(
        SpecRow("CPU Cores", "$cores Cores"),
        SpecRow("Primary ABI", Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown"),
        SpecRow("Supported ABIs", abis),
        SpecRow("Hardware Chipset", Build.HARDWARE)
    )
}

fun getRamUsage(context: Context): Triple<Double, Double, Float> {
    val act = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    val mem = ActivityManager.MemoryInfo()
    act?.getMemoryInfo(mem)
    val total = mem.totalMem.toDouble() / (1024 * 1024 * 1024)
    val avail = mem.availMem.toDouble() / (1024 * 1024 * 1024)
    val used = total - avail
    val progress = if (total > 0) (used / total).toFloat() else 0f
    return Triple(total, used, progress)
}

fun getStorageUsage(): Triple<Double, Double, Float> {
    return try {
        val stat = StatFs(Environment.getDataDirectory().path)
        val total = stat.totalBytes.toDouble() / (1024 * 1024 * 1024)
        val free = stat.availableBytes.toDouble() / (1024 * 1024 * 1024)
        val used = total - free
        val progress = if (total > 0) (used / total).toFloat() else 0f
        Triple(total, used, progress)
    } catch (e: Exception) {
        Triple(0.0, 0.0, 0f)
    }
}

fun getBatterySpecs(context: Context): List<SpecRow> {
    val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        ?: return listOf(SpecRow("Status", "Battery telemetry unavailable"))

    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
    val pct = if (level >= 0 && scale > 0) (level * 100) / scale else -1
    val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
    val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
    val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10.0f
    val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
    val tech = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"

    val healthStr = when (health) {
        BatteryManager.BATTERY_HEALTH_GOOD -> "Good Health"
        BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheated"
        BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
        else -> "Normal"
    }

    val statusStr = when (status) {
        BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
        BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
        BatteryManager.BATTERY_STATUS_FULL -> "Fully Charged"
        else -> "Not Charging"
    }

    val plugStr = when (plugged) {
        BatteryManager.BATTERY_PLUGGED_AC -> "AC Wall Charger"
        BatteryManager.BATTERY_PLUGGED_USB -> "USB Cable"
        BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Dock"
        else -> "Battery Power (Unplugged)"
    }

    return listOf(
        SpecRow("Battery Level", "$pct%"),
        SpecRow("Health", healthStr),
        SpecRow("Charging Status", statusStr),
        SpecRow("Power Source", plugStr),
        SpecRow("Real Temperature", "${String.format(Locale.US, "%.1f", temp)} °C"),
        SpecRow("Voltage", "$voltage mV"),
        SpecRow("Technology", tech)
    )
}

fun getSensorsList(context: Context): List<Sensor> {
    val sm = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    return sm?.getSensorList(Sensor.TYPE_ALL) ?: emptyList()
}
