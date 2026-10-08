package com.example.ui.screens.diagnostics

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.Vibrator
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket
import java.util.Locale

enum class DiagnosticStatus {
    GOOD,
    ATTENTION,
    UNSUPPORTED
}

data class DiagnosticItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val status: DiagnosticStatus,
    val summary: String,
    val details: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickScanScreen(
    language: AppLanguage,
    prefs: PreferencesManager,
    onBack: () -> Unit,
    onNavigateToHardwareTest: () -> Unit = {}
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isScanning by remember { mutableStateOf(false) }
    var scanProgress by remember { mutableFloatStateOf(0f) }
    var currentStepName by remember { mutableStateOf("") }
    var scanCompleted by remember { mutableStateOf(false) }
    var diagnosticResults by remember { mutableStateOf<List<DiagnosticItem>>(emptyList()) }

    fun runScan() {
        if (isScanning) return
        isScanning = true
        scanCompleted = false
        scanProgress = 0f
        diagnosticResults = emptyList()

        scope.launch {
            val results = mutableListOf<DiagnosticItem>()

            // Step 1: Device Information
            currentStepName = if (language == AppLanguage.BANGLA) "ডিভাইস তথ্য স্ক্যান হচ্ছে..." else "Scanning Device Specs..."
            scanProgress = 0.12f
            delay(180)
            val deviceModel = "${Build.MANUFACTURER.replaceFirstChar { it.titlecase() }} ${Build.MODEL}"
            val osVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
            results.add(
                DiagnosticItem(
                    id = "device",
                    title = if (language == AppLanguage.BANGLA) "ডিভাইস ও ওএস" else "Device & System",
                    icon = Icons.Default.PhoneAndroid,
                    status = DiagnosticStatus.GOOD,
                    summary = deviceModel,
                    details = "$osVersion • Build: ${Build.ID}"
                )
            )

            // Step 2: Battery Status
            currentStepName = if (language == AppLanguage.BANGLA) "ব্যাটারি স্বাস্থ্য স্ক্যান হচ্ছে..." else "Analyzing Battery Health..."
            scanProgress = 0.28f
            delay(180)
            val batteryStatus = withContext(Dispatchers.IO) {
                val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                if (intent != null) {
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    val pct = if (level >= 0 && scale > 0) (level * 100) / scale else -1
                    val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
                    val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10.0f
                    val isCharging = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1) == BatteryManager.BATTERY_STATUS_CHARGING
                    Triple(pct, health, temp to isCharging)
                } else null
            }

            if (batteryStatus != null) {
                val pct = batteryStatus.first
                val healthCode = batteryStatus.second
                val (temp, isCharging) = batteryStatus.third
                val isHealthy = healthCode == BatteryManager.BATTERY_HEALTH_GOOD
                val isHot = temp > 43.0f

                val status = when {
                    !isHealthy || isHot -> DiagnosticStatus.ATTENTION
                    pct in 0..15 && !isCharging -> DiagnosticStatus.ATTENTION
                    else -> DiagnosticStatus.GOOD
                }

                val healthText = if (isHealthy) "Good Health" else "Health Code $healthCode"
                val chargingText = if (isCharging) "Charging" else "Discharging"

                results.add(
                    DiagnosticItem(
                        id = "battery",
                        title = if (language == AppLanguage.BANGLA) "ব্যাটারি হেলথ" else "Battery Status",
                        icon = Icons.Default.BatteryChargingFull,
                        status = status,
                        summary = "$pct% • $healthText",
                        details = "$chargingText • Temp: ${String.format(Locale.US, "%.1f", temp)}°C"
                    )
                )
            } else {
                results.add(
                    DiagnosticItem(
                        id = "battery",
                        title = if (language == AppLanguage.BANGLA) "ব্যাটারি হেলথ" else "Battery Status",
                        icon = Icons.Default.BatteryChargingFull,
                        status = DiagnosticStatus.UNSUPPORTED,
                        summary = "Not Available",
                        details = "Battery telemetry unavailable"
                    )
                )
            }

            // Step 3: RAM Memory
            currentStepName = if (language == AppLanguage.BANGLA) "র‍্যাম মেমোরি স্ক্যান হচ্ছে..." else "Evaluating RAM Memory..."
            scanProgress = 0.44f
            delay(180)
            val ramInfo = withContext(Dispatchers.IO) {
                val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
                val memInfo = ActivityManager.MemoryInfo()
                actManager?.getMemoryInfo(memInfo)
                memInfo
            }

            val totalRamGb = ramInfo.totalMem.toDouble() / (1024 * 1024 * 1024)
            val availRamGb = ramInfo.availMem.toDouble() / (1024 * 1024 * 1024)
            val usedRamGb = totalRamGb - availRamGb
            val ramUsagePct = if (totalRamGb > 0) ((usedRamGb / totalRamGb) * 100).toInt() else 0
            val ramStatus = if (ramUsagePct > 88 || ramInfo.lowMemory) DiagnosticStatus.ATTENTION else DiagnosticStatus.GOOD

            results.add(
                DiagnosticItem(
                    id = "ram",
                    title = if (language == AppLanguage.BANGLA) "র‍্যাম মেমোরি" else "RAM Memory",
                    icon = Icons.Default.Memory,
                    status = ramStatus,
                    summary = "${String.format(Locale.US, "%.1f", usedRamGb)} GB / ${String.format(Locale.US, "%.1f", totalRamGb)} GB ($ramUsagePct% used)",
                    details = "${String.format(Locale.US, "%.1f", availRamGb)} GB available • Low memory: ${ramInfo.lowMemory}"
                )
            )

            // Step 4: Storage
            currentStepName = if (language == AppLanguage.BANGLA) "স্টোরেজ স্ক্যান হচ্ছে..." else "Checking Internal Storage..."
            scanProgress = 0.58f
            delay(180)
            val (totalStorageGb, freeStorageGb, storagePct) = withContext(Dispatchers.IO) {
                try {
                    val stat = StatFs(Environment.getDataDirectory().path)
                    val total = stat.totalBytes.toDouble() / (1024 * 1024 * 1024)
                    val free = stat.availableBytes.toDouble() / (1024 * 1024 * 1024)
                    val used = total - free
                    val pct = if (total > 0) ((used / total) * 100).toInt() else 0
                    Triple(total, free, pct)
                } catch (e: Exception) {
                    Triple(0.0, 0.0, 0)
                }
            }

            val storageStatus = if (storagePct > 90 || freeStorageGb < 2.0) DiagnosticStatus.ATTENTION else DiagnosticStatus.GOOD
            results.add(
                DiagnosticItem(
                    id = "storage",
                    title = if (language == AppLanguage.BANGLA) "স্টোরেজ" else "Internal Storage",
                    icon = Icons.Default.Storage,
                    status = storageStatus,
                    summary = "${String.format(Locale.US, "%.1f", freeStorageGb)} GB Free of ${String.format(Locale.US, "%.1f", totalStorageGb)} GB",
                    details = "$storagePct% used"
                )
            )

            // Step 5: Network Connectivity
            currentStepName = if (language == AppLanguage.BANGLA) "নেটওয়ার্ক ও ইন্টারনেট যাচাই..." else "Verifying Network & Internet..."
            scanProgress = 0.72f
            delay(180)
            val netDiagnostic = withContext(Dispatchers.IO) {
                val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                val activeNet = connMgr?.activeNetwork
                val caps = connMgr?.getNetworkCapabilities(activeNet)

                val isWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
                val isCellular = caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
                val isEthernet = caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true

                var hasInternet = false
                if (caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    try {
                        Socket().use { socket ->
                            socket.connect(InetSocketAddress("8.8.8.8", 53), 1500)
                            hasInternet = true
                        }
                    } catch (e: Exception) {
                        hasInternet = false
                    }
                }

                val type = when {
                    isWifi -> "Wi-Fi"
                    isCellular -> "Cellular Mobile Data"
                    isEthernet -> "Ethernet"
                    else -> "No Active Network"
                }
                type to hasInternet
            }

            val (netType, internetWorking) = netDiagnostic
            val netStatus = when {
                internetWorking -> DiagnosticStatus.GOOD
                netType != "No Active Network" -> DiagnosticStatus.ATTENTION
                else -> DiagnosticStatus.ATTENTION
            }

            results.add(
                DiagnosticItem(
                    id = "network",
                    title = if (language == AppLanguage.BANGLA) "নেটওয়ার্ক ও সংযোগ" else "Network & Internet",
                    icon = Icons.Default.Wifi,
                    status = netStatus,
                    summary = "$netType • ${if (internetWorking) "Internet Reachable" else "No Internet"}",
                    details = if (internetWorking) "Active internet connection detected" else "Check Wi-Fi or mobile data data pack"
                )
            )

            // Step 6: Sensors Availability
            currentStepName = if (language == AppLanguage.BANGLA) "অনবোর্ড সেন্সর পরীক্ষা..." else "Checking Hardware Sensors..."
            scanProgress = 0.86f
            delay(180)
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            val allSensors = sensorManager?.getSensorList(Sensor.TYPE_ALL) ?: emptyList()
            val hasAccel = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null
            val hasGyro = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null
            val hasProx = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null
            val hasLight = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT) != null
            val hasCompass = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null

            val sensorCount = allSensors.size
            results.add(
                DiagnosticItem(
                    id = "sensors",
                    title = if (language == AppLanguage.BANGLA) "অনবোর্ড সেন্সর" else "Hardware Sensors",
                    icon = Icons.Default.Sensors,
                    status = if (hasAccel) DiagnosticStatus.GOOD else DiagnosticStatus.ATTENTION,
                    summary = "$sensorCount Physical Sensors Available",
                    details = "Accel: ${if (hasAccel) "Yes" else "No"} • Gyro: ${if (hasGyro) "Yes" else "No"} • Prox: ${if (hasProx) "Yes" else "No"} • Compass: ${if (hasCompass) "Yes" else "No"}"
                )
            )

            // Step 7: Core Hardware Peripherals
            currentStepName = if (language == AppLanguage.BANGLA) "কোর হার্ডওয়্যার অবস্থা যাচাই..." else "Verifying Core Hardware Status..."
            scanProgress = 1.0f
            delay(180)
            val pm = context.packageManager
            val hasCamera = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
            val hasFlash = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            val hasVibrator = vibrator?.hasVibrator() == true

            results.add(
                DiagnosticItem(
                    id = "hardware",
                    title = if (language == AppLanguage.BANGLA) "বেসিক হার্ডওয়্যার" else "Hardware Peripherals",
                    icon = Icons.Default.Build,
                    status = if (hasCamera && hasVibrator) DiagnosticStatus.GOOD else DiagnosticStatus.ATTENTION,
                    summary = "Camera: ${if (hasCamera) "OK" else "None"} • Flash: ${if (hasFlash) "OK" else "None"}",
                    details = "Vibration Motor: ${if (hasVibrator) "Supported" else "Unsupported"}"
                )
            )

            diagnosticResults = results
            isScanning = false
            scanCompleted = true
            currentStepName = if (language == AppLanguage.BANGLA) "স্ক্যান সম্পন্ন!" else "Diagnostic Complete!"
            ToolActions.triggerHaptic(context, prefs.hapticEnabled.value)
        }
    }

    LaunchedEffect(Unit) {
        // Auto-run scan on screen open
        runScan()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (language == AppLanguage.BANGLA) "কুইক ফোন স্ক্যান" else "Quick Scan",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isScanning) {
                        IconButton(onClick = { runScan() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Rescan")
                        }
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
                Spacer(modifier = Modifier.height(6.dp))

                // Scan Hero Radar / Status Card
                ScanProgressCard(
                    isScanning = isScanning,
                    progress = scanProgress,
                    stepName = currentStepName,
                    completed = scanCompleted,
                    results = diagnosticResults,
                    language = language,
                    onRescan = { runScan() }
                )
            }

            if (scanCompleted) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (language == AppLanguage.BANGLA) "বিস্তারিত স্বাস্থ্য রিপোর্ট" else "Diagnostic Report",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "${diagnosticResults.size} Checks",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                items(diagnosticResults, key = { it.id }) { item ->
                    DiagnosticReportCard(item = item, language = language)
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onNavigateToHardwareTest,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RedAccent)
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, tint = RedAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (language == AppLanguage.BANGLA) "সম্পূর্ণ হার্ডওয়্যার টেস্টে যান" else "Open Individual Hardware Tests",
                            color = RedAccent,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun ScanProgressCard(
    isScanning: Boolean,
    progress: Float,
    stepName: String,
    completed: Boolean,
    results: List<DiagnosticItem>,
    language: AppLanguage,
    onRescan: () -> Unit
) {
    val attentionCount = results.count { it.status == DiagnosticStatus.ATTENTION }
    val goodCount = results.count { it.status == DiagnosticStatus.GOOD }

    val infiniteTransition = rememberInfiniteTransition(label = "scan_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = DarkSurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceVariant)
                    .scale(if (isScanning) pulseScale else 1f),
                contentAlignment = Alignment.Center
            ) {
                if (isScanning) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(110.dp),
                        color = RedAccent,
                        strokeWidth = 5.dp
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = RedAccent
                    )
                } else if (completed) {
                    val statusColor = if (attentionCount == 0) GreenSuccess else RedAccent
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (attentionCount == 0) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = RedAccent,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (isScanning) {
                Text(
                    text = if (language == AppLanguage.BANGLA) "ডায়াগনস্টিক স্ক্যান চলছে..." else "Running Diagnostic...",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stepName,
                    style = MaterialTheme.typography.bodySmall,
                    color = RedAccent
                )
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = RedAccent,
                    trackColor = DarkSurfaceVariant
                )
            } else if (completed) {
                val overallTitle = if (attentionCount == 0) {
                    if (language == AppLanguage.BANGLA) "সবকিছু ঠিকঠাক আছে!" else "All Systems Optimal"
                } else {
                    if (language == AppLanguage.BANGLA) "$attentionCount টি বিষয়ে নজর দেওয়া দরকার" else "$attentionCount Issues Require Attention"
                }

                Text(
                    text = overallTitle,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (attentionCount == 0) GreenSuccess else RedAccent
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusPill(
                        label = "$goodCount Good",
                        color = GreenSuccess
                    )
                    if (attentionCount > 0) {
                        StatusPill(
                            label = "$attentionCount Attention",
                            color = RedAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onRescan,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RedAccent)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (language == AppLanguage.BANGLA) "আবার স্ক্যান করুন" else "Run Scan Again")
                }
            }
        }
    }
}

@Composable
fun StatusPill(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun DiagnosticReportCard(
    item: DiagnosticItem,
    language: AppLanguage
) {
    val (statusLabel, statusColor) = when (item.status) {
        DiagnosticStatus.GOOD -> (if (language == AppLanguage.BANGLA) "ভালো" else "Good") to GreenSuccess
        DiagnosticStatus.ATTENTION -> (if (language == AppLanguage.BANGLA) "নজর দিন" else "Attention") to RedAccent
        DiagnosticStatus.UNSUPPORTED -> (if (language == AppLanguage.BANGLA) "অনুপস্থিত" else "Unsupported") to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
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
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    StatusPill(label = statusLabel, color = statusColor)
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = item.summary,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = item.details,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
