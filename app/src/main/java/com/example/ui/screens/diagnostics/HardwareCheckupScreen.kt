package com.example.ui.screens.diagnostics

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.PreferencesManager
import com.example.localization.AppLanguage
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
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.sin

enum class TestState {
    UNTESTED,
    PASS,
    FAILED,
    UNSUPPORTED
}

data class HardwareTest(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HardwareCheckupScreen(
    language: AppLanguage,
    prefs: PreferencesManager,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var activeTestId by remember { mutableStateOf<String?>(null) }
    val testResults = remember { mutableStateMapOf<String, TestState>() }

    val tests = remember(language) {
        listOf(
            HardwareTest("touch", if (language == AppLanguage.BANGLA) "টাচস্ক্রিন গ্রিড" else "Touchscreen Test", if (language == AppLanguage.BANGLA) "স্ক্রিনে স্পর্শ সংবেদনশীলতা টেস্ট" else "Test touch responsiveness across display", Icons.Default.TouchApp),
            HardwareTest("multitouch", if (language == AppLanguage.BANGLA) "মাল্টি-টাচ টেস্ট" else "Multi-Touch Test", if (language == AppLanguage.BANGLA) "একসাথে কয়টি আঙুল শনাক্ত হয়" else "Detect multi-finger simultaneous touch", Icons.Default.TouchApp),
            HardwareTest("display", if (language == AppLanguage.BANGLA) "ডিসপ্লে ও ডেড পিক্সেল" else "Display Color & Pixels", if (language == AppLanguage.BANGLA) "লাল, সবুজ, নীল, সাদা রঙে পিক্সেল পরীক্ষা" else "Pure RGBW screen color cycle", Icons.Default.ColorLens),
            HardwareTest("speaker", if (language == AppLanguage.BANGLA) "লাউডস্পিকার সাউন্ড" else "Loudspeaker Audio", if (language == AppLanguage.BANGLA) "স্পষ্ট অডিও টোন প্লেব্যাক টেস্ট" else "Play pure 440Hz / 880Hz test tone", Icons.Default.VolumeUp),
            HardwareTest("mic", if (language == AppLanguage.BANGLA) "মাইক্রোফোন লেভেল" else "Microphone Input", if (language == AppLanguage.BANGLA) "লাইভ ভয়েস ডেসিবেল লেভেল মিটার" else "Live decibel amplitude meter", Icons.Default.Mic),
            HardwareTest("vibration", if (language == AppLanguage.BANGLA) "ভাইব্রেশন মোটর" else "Vibration Motor", if (language == AppLanguage.BANGLA) "হ্যাপটিক ভাইব্রেশন প্যাটার্ন পরীক্ষা" else "Trigger vibration pulse pattern", Icons.Default.Vibration),
            HardwareTest("flashlight", if (language == AppLanguage.BANGLA) "ক্যামেরা ফ্ল্যাশলাইট" else "Camera Flashlight", if (language == AppLanguage.BANGLA) "ক্যামেরা টর্চ অন/অফ পরীক্ষা" else "Toggle camera torch mode", Icons.Default.FlashlightOn),
            HardwareTest("camera", if (language == AppLanguage.BANGLA) "ক্যামেরা হার্ডওয়্যার" else "Camera Availability", if (language == AppLanguage.BANGLA) "সামনের ও পেছনের ক্যামেরা লেন্স যাচাই" else "Inspect front & back camera specs", Icons.Default.CameraAlt),
            HardwareTest("proximity", if (language == AppLanguage.BANGLA) "প্রক্সিমিটি সেন্সর" else "Proximity Sensor", if (language == AppLanguage.BANGLA) "ফোনের ওপর হাত আনলে শনাক্তকরণ" else "Detect near / far object distance", Icons.Default.NearMe),
            HardwareTest("accelerometer", if (language == AppLanguage.BANGLA) "অ্যাক্সেলেরোমিটার" else "Accelerometer Sensor", if (language == AppLanguage.BANGLA) "ফোনের ৩-অক্ষীয় নড়াচড়া (X, Y, Z)" else "Live X, Y, Z motion readout", Icons.Default.ScreenRotation),
            HardwareTest("gyroscope", if (language == AppLanguage.BANGLA) "জাইরোস্কোপ" else "Gyroscope Sensor", if (language == AppLanguage.BANGLA) "ঘূর্ণন হার (Rotation Rate)" else "Live angular rotation rate", Icons.Default.Sensors),
            HardwareTest("light", if (language == AppLanguage.BANGLA) "অ্যাম্বিয়েন্ট লাইট সেন্সর" else "Ambient Light Sensor", if (language == AppLanguage.BANGLA) "পারিপার্শ্বিক আলোর তীব্রতা (Lux)" else "Live illumination lux level", Icons.Default.LightMode),
            HardwareTest("compass", if (language == AppLanguage.BANGLA) "কম্পাস ও ম্যাগনেটিক সেন্সর" else "Digital Compass", if (language == AppLanguage.BANGLA) "চৌম্বক দিক ও ডিগ্রি নির্দেশক" else "Real azimuth magnetic heading", Icons.Default.Explore)
        )
    }

    BackHandler(enabled = activeTestId != null) {
        activeTestId = null
    }

    if (activeTestId != null) {
        val currentTest = tests.firstOrNull { it.id == activeTestId }
        if (currentTest != null) {
            HardwareTestDetailHost(
                test = currentTest,
                language = language,
                onComplete = { result ->
                    testResults[currentTest.id] = result
                    activeTestId = null
                },
                onCancel = { activeTestId = null }
            )
            return
        }
    }

    BackHandler(onBack = onBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (language == AppLanguage.BANGLA) "হার্ডওয়্যার চেকআপ" else "Hardware Checkup",
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                val passed = testResults.values.count { it == TestState.PASS }
                val failed = testResults.values.count { it == TestState.FAILED }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (language == AppLanguage.BANGLA) "বাস্তব হার্ডওয়্যার পরীক্ষা" else "Hardware Diagnostics",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (language == AppLanguage.BANGLA) "আসল সেন্সর ও যন্ত্রাংশের সরাসরি পরীক্ষা" else "Select any test to verify actual hardware",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatusPill(label = "$passed Pass", color = GreenSuccess)
                            if (failed > 0) {
                                StatusPill(label = "$failed Fail", color = RedAccent)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            items(tests, key = { it.id }) { test ->
                val state = testResults[test.id] ?: TestState.UNTESTED
                HardwareTestRow(
                    test = test,
                    state = state,
                    language = language,
                    onClick = { activeTestId = test.id }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun HardwareTestRow(
    test: HardwareTest,
    state: TestState,
    language: AppLanguage,
    onClick: () -> Unit
) {
    val (statusLabel, statusColor, statusIcon) = when (state) {
        TestState.UNTESTED -> Triple(if (language == AppLanguage.BANGLA) "পরীক্ষা করুন" else "Start Test", MaterialTheme.colorScheme.onSurfaceVariant, Icons.Default.PlayArrow)
        TestState.PASS -> Triple(if (language == AppLanguage.BANGLA) "সফল" else "Passed", GreenSuccess, Icons.Default.Check)
        TestState.FAILED -> Triple(if (language == AppLanguage.BANGLA) "ব্যর্থ" else "Failed", RedAccent, Icons.Default.Close)
        TestState.UNSUPPORTED -> Triple(if (language == AppLanguage.BANGLA) "অনুপস্থিত" else "Unsupported", MaterialTheme.colorScheme.onSurfaceVariant, Icons.Default.QuestionMark)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
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
                    imageVector = test.icon,
                    contentDescription = null,
                    tint = if (state == TestState.PASS) GreenSuccess else RedAccent,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = test.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = test.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = statusColor.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = statusColor
                    )
                }
            }
        }
    }
}

@Composable
fun HardwareTestDetailHost(
    test: HardwareTest,
    language: AppLanguage,
    onComplete: (TestState) -> Unit,
    onCancel: () -> Unit
) {
    when (test.id) {
        "touch" -> TouchScreenTest(language, onComplete, onCancel)
        "multitouch" -> MultiTouchTest(language, onComplete, onCancel)
        "display" -> DisplayColorTest(language, onComplete, onCancel)
        "speaker" -> SpeakerTest(language, onComplete, onCancel)
        "mic" -> MicTest(language, onComplete, onCancel)
        "vibration" -> VibrationTest(language, onComplete, onCancel)
        "flashlight" -> FlashlightTest(language, onComplete, onCancel)
        "camera" -> CameraSpecsTest(language, onComplete, onCancel)
        "proximity" -> ProximitySensorTest(language, onComplete, onCancel)
        "accelerometer" -> AccelerometerTest(language, onComplete, onCancel)
        "gyroscope" -> GyroscopeTest(language, onComplete, onCancel)
        "light" -> LightSensorTest(language, onComplete, onCancel)
        "compass" -> CompassTest(language, onComplete, onCancel)
        else -> onCancel()
    }
}

// 1. TOUCHSCREEN TEST
@Composable
fun TouchScreenTest(language: AppLanguage, onComplete: (TestState) -> Unit, onCancel: () -> Unit) {
    val touchedPoints = remember { mutableStateListOf<Offset>() }
    var touchCount by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, _ ->
                        touchedPoints.add(change.position)
                        touchCount++
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw touched points as a glowing trail
            for (p in touchedPoints) {
                drawCircle(color = Color(0xFFFF3B30), radius = 24f, center = p)
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (language == AppLanguage.BANGLA) "স্ক্রিনে আঙুল দিয়ে ড্র্যাগ করুন" else "Draw all over the screen",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = if (language == AppLanguage.BANGLA) "স্পর্শ শনাক্ত হয়েছে: $touchCount টি পয়েন্ট" else "Touch points registered: $touchCount",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = { onComplete(TestState.FAILED) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text(if (language == AppLanguage.BANGLA) "ব্যর্থ" else "Failed")
            }
            Button(
                onClick = {
                    if (touchCount > 15) onComplete(TestState.PASS)
                    else onComplete(TestState.FAILED)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess)
            ) {
                Text(if (language == AppLanguage.BANGLA) "পাস (Pass)" else "Pass")
            }
        }
    }
}

// 2. MULTI-TOUCH TEST
@Composable
fun MultiTouchTest(language: AppLanguage, onComplete: (TestState) -> Unit, onCancel: () -> Unit) {
    var maxPointers by remember { mutableIntStateOf(0) }
    var currentPointers by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkSurface)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val count = event.changes.count { it.pressed }
                        currentPointers = count
                        if (count > maxPointers) {
                            maxPointers = count
                        }
                    }
                }
            }
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (language == AppLanguage.BANGLA) "একাধিক আঙুল একসাথে রাখুন" else "Touch with multiple fingers simultaneously",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceVariant)
                    .border(3.dp, RedAccent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$currentPointers",
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Bold,
                        color = RedAccent
                    )
                    Text(
                        text = if (language == AppLanguage.BANGLA) "সক্রিয় আঙুল" else "Fingers",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (language == AppLanguage.BANGLA) "সর্বোচ্চ শনাক্ত: $maxPointers টি আঙুল" else "Peak detected: $maxPointers fingers",
                color = GreenSuccess,
                fontWeight = FontWeight.SemiBold
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = { onComplete(TestState.FAILED) },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (language == AppLanguage.BANGLA) "ব্যর্থ" else "Failed")
            }
            Button(
                onClick = {
                    if (maxPointers >= 2) onComplete(TestState.PASS)
                    else onComplete(TestState.FAILED)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess)
            ) {
                Text(if (language == AppLanguage.BANGLA) "পাস (Pass)" else "Pass")
            }
        }
    }
}

// 3. DISPLAY COLOR TEST
@Composable
fun DisplayColorTest(language: AppLanguage, onComplete: (TestState) -> Unit, onCancel: () -> Unit) {
    val colors = listOf(Color.Red, Color.Green, Color.Blue, Color.White, Color.Black)
    var colorIndex by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors[colorIndex])
            .clickable {
                if (colorIndex < colors.lastIndex) {
                    colorIndex++
                }
            }
    ) {
        val textColor = if (colors[colorIndex] == Color.White) Color.Black else Color.White

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (language == AppLanguage.BANGLA) "স্ক্রিনে ট্যাপ করে রঙ পরিবর্তন করুন (${colorIndex + 1}/${colors.size})" else "Tap to cycle colors (${colorIndex + 1}/${colors.size})",
                color = textColor,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (language == AppLanguage.BANGLA) "কোনো ডেড বা নষ্ট পিক্সেল আছে কিনা লক্ষ্য করুন" else "Check for dead, stuck, or discolored pixels",
                color = textColor.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }

        if (colorIndex == colors.lastIndex) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { onComplete(TestState.FAILED) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = RedAccent)
                ) {
                    Text(if (language == AppLanguage.BANGLA) "ত্রুটি আছে" else "Dead Pixels Found")
                }
                Button(
                    onClick = { onComplete(TestState.PASS) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess)
                ) {
                    Text(if (language == AppLanguage.BANGLA) "সব ঠিক আছে (Pass)" else "Display OK")
                }
            }
        }
    }
}

// 4. SPEAKER TEST
@Composable
fun SpeakerTest(language: AppLanguage, onComplete: (TestState) -> Unit, onCancel: () -> Unit) {
    var isPlaying by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        onDispose {
            isPlaying = false
        }
    }

    fun playTone() {
        if (isPlaying) return
        isPlaying = true
        scope.launch(Dispatchers.Default) {
            try {
                val sampleRate = 44100
                val durationSec = 2
                val numSamples = sampleRate * durationSec
                val buffer = ShortArray(numSamples)
                val freq = 440.0 // 440Hz standard concert A

                for (i in 0 until numSamples) {
                    val angle = 2.0 * Math.PI * i / (sampleRate / freq)
                    buffer[i] = (sin(angle) * Short.MAX_VALUE * 0.7).toInt().toShort()
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .build()

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()
                delay(2000)
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                // Audio output error
            } finally {
                isPlaying = false
            }
        }
    }

    TestDialogScaffold(
        title = if (language == AppLanguage.BANGLA) "লাউডস্পিকার পরীক্ষা" else "Speaker Audio Test",
        instruction = if (language == AppLanguage.BANGLA) "'প্লে টোন' বাটনে ট্যাপ করে ৪৪০ হার্টজ অডিও শুনুন" else "Tap 'Play Sound' to output a 440 Hz test tone via phone speakers",
        language = language,
        onComplete = onComplete,
        onCancel = onCancel
    ) {
        Button(
            onClick = { playTone() },
            enabled = !isPlaying,
            colors = ButtonDefaults.buttonColors(containerColor = RedAccent)
        ) {
            Icon(Icons.Default.VolumeUp, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isPlaying) "Playing..." else (if (language == AppLanguage.BANGLA) "টোন প্লে করুন" else "Play Sound"))
        }
    }
}

// 5. MICROPHONE TEST
@Composable
fun MicTest(language: AppLanguage, onComplete: (TestState) -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted
    }

    var liveAmplitude by remember { mutableFloatStateOf(0f) }
    var isListening by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(hasPermission) {
        if (!hasPermission) {
            launcher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            isListening = true
            scope.launch(Dispatchers.IO) {
                try {
                    val sampleRate = 8000
                    val minBuf = AudioRecord.getMinBufferSize(
                        sampleRate,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT
                    )
                    val record = AudioRecord(
                        MediaRecorder.AudioSource.MIC,
                        sampleRate,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        minBuf.coerceAtLeast(1024)
                    )

                    if (record.state == AudioRecord.STATE_INITIALIZED) {
                        record.startRecording()
                        val buf = ShortArray(256)
                        while (isListening) {
                            val read = record.read(buf, 0, buf.size)
                            if (read > 0) {
                                var max = 0
                                for (i in 0 until read) {
                                    val absVal = kotlin.math.abs(buf[i].toInt())
                                    if (absVal > max) max = absVal
                                }
                                liveAmplitude = (max.toFloat() / Short.MAX_VALUE).coerceIn(0f, 1f)
                            }
                            delay(50)
                        }
                        record.stop()
                        record.release()
                    }
                } catch (e: Exception) {
                    // mic error
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { isListening = false }
    }

    TestDialogScaffold(
        title = if (language == AppLanguage.BANGLA) "মাইক্রোফোন ইনপুট পরীক্ষা" else "Microphone Input Test",
        instruction = if (language == AppLanguage.BANGLA) "মাইক্রোফোনে কথা বলুন বা ফুঁ দিন এবং লেভেল মিটার দেখুন" else "Speak or blow into the microphone to verify sound input",
        language = language,
        onComplete = onComplete,
        onCancel = onCancel
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceVariant)
                    .border(3.dp, RedAccent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = if (liveAmplitude > 0.1f) GreenSuccess else RedAccent,
                    modifier = Modifier.size((36 + (liveAmplitude * 30)).dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Amplitude: ${(liveAmplitude * 100).toInt()}%",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// 6. VIBRATION TEST
@Composable
fun VibrationTest(language: AppLanguage, onComplete: (TestState) -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    val hasVibrator = vibrator?.hasVibrator() == true

    fun triggerVibration() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 250, 150, 250), -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(500)
        }
    }

    TestDialogScaffold(
        title = if (language == AppLanguage.BANGLA) "ভাইব্রেশন টেস্ট" else "Vibration Motor Test",
        instruction = if (language == AppLanguage.BANGLA) "'ভাইব্রেট করুন' বাটনে চাপ দিন এবং ফোন কাঁপে কিনা দেখুন" else "Tap 'Vibrate' to test if the haptic vibration motor responds",
        language = language,
        onComplete = { result ->
            if (!hasVibrator) onComplete(TestState.UNSUPPORTED)
            else onComplete(result)
        },
        onCancel = onCancel
    ) {
        if (!hasVibrator) {
            Text(
                text = if (language == AppLanguage.BANGLA) "ডিভাইসে কোনো ভাইব্রেশন মোটর নেই" else "No physical vibrator detected on device",
                color = RedAccent
            )
        } else {
            Button(
                onClick = { triggerVibration() },
                colors = ButtonDefaults.buttonColors(containerColor = RedAccent)
            ) {
                Icon(Icons.Default.Vibration, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (language == AppLanguage.BANGLA) "ভাইব্রেট করুন" else "Vibrate Phone")
            }
        }
    }
}

// 7. FLASHLIGHT TEST
@Composable
fun FlashlightTest(language: AppLanguage, onComplete: (TestState) -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    val cameraManager = remember { context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager }
    val cameraId = remember {
        try {
            cameraManager?.cameraIdList?.firstOrNull { id ->
                val chars = cameraManager.getCameraCharacteristics(id)
                chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        } catch (e: Exception) {
            null
        }
    }

    var isTorchOn by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            if (isTorchOn && cameraId != null) {
                try {
                    cameraManager?.setTorchMode(cameraId, false)
                } catch (e: Exception) {}
            }
        }
    }

    fun toggleTorch() {
        if (cameraId == null) return
        try {
            val newState = !isTorchOn
            cameraManager?.setTorchMode(cameraId, newState)
            isTorchOn = newState
        } catch (e: Exception) {
            // torch error
        }
    }

    TestDialogScaffold(
        title = if (language == AppLanguage.BANGLA) "টর্চ / ফ্ল্যাশলাইট টেস্ট" else "Flashlight Torch Test",
        instruction = if (language == AppLanguage.BANGLA) "ক্যামেরা টর্চ অন করে আলো জ্বলছে কিনা পরীক্ষা করুন" else "Toggle torch mode to verify camera LED flash",
        language = language,
        onComplete = { result ->
            if (cameraId == null) onComplete(TestState.UNSUPPORTED)
            else onComplete(result)
        },
        onCancel = onCancel
    ) {
        if (cameraId == null) {
            Text(
                text = if (language == AppLanguage.BANGLA) "ডিভাইসে কোনো ফ্ল্যাশলাইট পাওয়া যায়নি" else "No camera flash unit available",
                color = RedAccent
            )
        } else {
            Button(
                onClick = { toggleTorch() },
                colors = ButtonDefaults.buttonColors(containerColor = if (isTorchOn) GreenSuccess else RedAccent)
            ) {
                Icon(Icons.Default.FlashlightOn, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isTorchOn) (if (language == AppLanguage.BANGLA) "টর্চ বন্ধ করুন" else "Turn Torch OFF") else (if (language == AppLanguage.BANGLA) "টর্চ চালু করুন" else "Turn Torch ON"))
            }
        }
    }
}

// 8. CAMERA SPECS TEST
@Composable
fun CameraSpecsTest(language: AppLanguage, onComplete: (TestState) -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    val cameraManager = remember { context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager }
    val cameraList = remember {
        try {
            cameraManager?.cameraIdList?.map { id ->
                val chars = cameraManager.getCameraCharacteristics(id)
                val facing = when (chars.get(CameraCharacteristics.LENS_FACING)) {
                    CameraCharacteristics.LENS_FACING_BACK -> "Back / Rear"
                    CameraCharacteristics.LENS_FACING_FRONT -> "Front / Selfie"
                    else -> "External"
                }
                val hasFlash = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                id to (facing to hasFlash)
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    TestDialogScaffold(
        title = if (language == AppLanguage.BANGLA) "ক্যামেরা হার্ডওয়্যার তথ্য" else "Camera Hardware Availability",
        instruction = if (language == AppLanguage.BANGLA) "অনবোর্ড ক্যামেরা লেন্স ও তাদের স্ট্যাটাস" else "Detected camera hardware sensors on device",
        language = language,
        onComplete = { result ->
            if (cameraList.isEmpty()) onComplete(TestState.UNSUPPORTED)
            else onComplete(result)
        },
        onCancel = onCancel
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (cameraList.isEmpty()) {
                Text(text = "No cameras detected", color = RedAccent)
            } else {
                cameraList.forEach { (id, info) ->
                    Text(
                        text = "• Camera $id: ${info.first} (Flash: ${if (info.second) "Yes" else "No"})",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// 9. PROXIMITY SENSOR TEST
@Composable
fun ProximitySensorTest(language: AppLanguage, onComplete: (TestState) -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager }
    val proxSensor = remember { sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY) }

    var distance by remember { mutableFloatStateOf(-1f) }
    var maxRange by remember { mutableFloatStateOf(0f) }

    DisposableEffect(Unit) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event != null && event.sensor.type == Sensor.TYPE_PROXIMITY) {
                    distance = event.values[0]
                    maxRange = event.sensor.maximumRange
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (proxSensor != null) {
            sensorManager?.registerListener(listener, proxSensor, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    TestDialogScaffold(
        title = if (language == AppLanguage.BANGLA) "প্রক্সিমিটি সেন্সর টেস্ট" else "Proximity Sensor Test",
        instruction = if (language == AppLanguage.BANGLA) "স্ক্রিনের উপরিভাগে হাত নিয়ে আসুন এবং সেন্সর 'Near' হয় কিনা দেখুন" else "Cover the top speaker/bezel with your hand to trigger NEAR detection",
        language = language,
        onComplete = { result ->
            if (proxSensor == null) onComplete(TestState.UNSUPPORTED)
            else onComplete(result)
        },
        onCancel = onCancel
    ) {
        if (proxSensor == null) {
            Text(text = "Proximity sensor not available on this device", color = RedAccent)
        } else {
            val isNear = distance in 0.0f..3.0f && distance < maxRange
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(if (isNear) GreenSuccess.copy(alpha = 0.2f) else DarkSurfaceVariant)
                        .border(3.dp, if (isNear) GreenSuccess else RedAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isNear) "NEAR" else "FAR",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isNear) GreenSuccess else RedAccent
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Distance: ${String.format(Locale.US, "%.1f", distance)} cm (Max: $maxRange cm)", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// 10. ACCELEROMETER TEST
@Composable
fun AccelerometerTest(language: AppLanguage, onComplete: (TestState) -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager }
    val sensor = remember { sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }

    var x by remember { mutableFloatStateOf(0f) }
    var y by remember { mutableFloatStateOf(0f) }
    var z by remember { mutableFloatStateOf(0f) }

    DisposableEffect(Unit) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event != null && event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    x = event.values[0]
                    y = event.values[1]
                    z = event.values[2]
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (sensor != null) {
            sensorManager?.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    TestDialogScaffold(
        title = if (language == AppLanguage.BANGLA) "অ্যাক্সেলেরোমিটার টেস্ট" else "Accelerometer Sensor",
        instruction = if (language == AppLanguage.BANGLA) "ফোনটি ডানে, বাঁয়ে ও ওপরে কাত করে অক্ষ পরিবর্তন দেখুন" else "Tilt and move your phone to observe X, Y, Z acceleration",
        language = language,
        onComplete = { result ->
            if (sensor == null) onComplete(TestState.UNSUPPORTED)
            else onComplete(result)
        },
        onCancel = onCancel
    ) {
        if (sensor == null) {
            Text(text = "Accelerometer not available", color = RedAccent)
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    SensorAxisBox("X", x)
                    SensorAxisBox("Y", y)
                    SensorAxisBox("Z", z)
                }
            }
        }
    }
}

// 11. GYROSCOPE TEST
@Composable
fun GyroscopeTest(language: AppLanguage, onComplete: (TestState) -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager }
    val sensor = remember { sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE) }

    var rx by remember { mutableFloatStateOf(0f) }
    var ry by remember { mutableFloatStateOf(0f) }
    var rz by remember { mutableFloatStateOf(0f) }

    DisposableEffect(Unit) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event != null && event.sensor.type == Sensor.TYPE_GYROSCOPE) {
                    rx = event.values[0]
                    ry = event.values[1]
                    rz = event.values[2]
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (sensor != null) {
            sensorManager?.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    TestDialogScaffold(
        title = if (language == AppLanguage.BANGLA) "জাইরোস্কোপ টেস্ট" else "Gyroscope Sensor",
        instruction = if (language == AppLanguage.BANGLA) "ফোনটি ঘোরালে রোটেশন স্পিড (rad/s) পরিবর্তিত হবে" else "Rotate the phone in hand to see angular speed (rad/s)",
        language = language,
        onComplete = { result ->
            if (sensor == null) onComplete(TestState.UNSUPPORTED)
            else onComplete(result)
        },
        onCancel = onCancel
    ) {
        if (sensor == null) {
            Text(text = "Gyroscope hardware not supported on this device", color = RedAccent)
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                SensorAxisBox("rX", rx)
                SensorAxisBox("rY", ry)
                SensorAxisBox("rZ", rz)
            }
        }
    }
}

// 12. LIGHT SENSOR TEST
@Composable
fun LightSensorTest(language: AppLanguage, onComplete: (TestState) -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager }
    val sensor = remember { sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT) }

    var lux by remember { mutableFloatStateOf(-1f) }

    DisposableEffect(Unit) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event != null && event.sensor.type == Sensor.TYPE_LIGHT) {
                    lux = event.values[0]
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (sensor != null) {
            sensorManager?.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    TestDialogScaffold(
        title = if (language == AppLanguage.BANGLA) "আলো সংবেদনশীলতা টেস্ট" else "Ambient Light Sensor",
        instruction = if (language == AppLanguage.BANGLA) "আলোতে আনুন বা সেন্সরে ছায়া ফেলুন" else "Move phone between light and shadow to test lux changes",
        language = language,
        onComplete = { result ->
            if (sensor == null) onComplete(TestState.UNSUPPORTED)
            else onComplete(result)
        },
        onCancel = onCancel
    ) {
        if (sensor == null) {
            Text(text = "Light sensor not supported on this device", color = RedAccent)
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${lux.toInt()} Lux",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = RedAccent
                )
                Text(
                    text = if (lux < 20) "Dim / Dark" else if (lux < 200) "Normal Room" else "Bright Light",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// 13. COMPASS TEST
@Composable
fun CompassTest(language: AppLanguage, onComplete: (TestState) -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager }
    val accelSensor = remember { sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
    val magSensor = remember { sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) }

    var azimuth by remember { mutableFloatStateOf(0f) }

    DisposableEffect(Unit) {
        var gravity: FloatArray? = null
        var geomagnetic: FloatArray? = null

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    gravity = event.values.clone()
                }
                if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    geomagnetic = event.values.clone()
                }
                if (gravity != null && geomagnetic != null) {
                    val r = FloatArray(9)
                    val i = FloatArray(9)
                    if (SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)) {
                        val orientation = FloatArray(3)
                        SensorManager.getOrientation(r, orientation)
                        val deg = Math.toDegrees(orientation[0].toDouble()).toFloat()
                        azimuth = (deg + 360) % 360
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (accelSensor != null && magSensor != null) {
            sensorManager?.registerListener(listener, accelSensor, SensorManager.SENSOR_DELAY_UI)
            sensorManager?.registerListener(listener, magSensor, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    TestDialogScaffold(
        title = if (language == AppLanguage.BANGLA) "ডিজিটাল কম্পাস টেস্ট" else "Digital Compass Test",
        instruction = if (language == AppLanguage.BANGLA) "ফোনটি অনুভূমিকভাবে ঘুরিয়ে উত্তর দিক দেখুন" else "Hold phone flat and rotate to verify azimuth degrees",
        language = language,
        onComplete = { result ->
            if (magSensor == null) onComplete(TestState.UNSUPPORTED)
            else onComplete(result)
        },
        onCancel = onCancel
    ) {
        if (magSensor == null) {
            Text(text = "Magnetic compass sensor not available", color = RedAccent)
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${azimuth.toInt()}°",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color = RedAccent
                )
                val dir = when {
                    azimuth in 337.5..360.0 || azimuth in 0.0..22.5 -> "North (N)"
                    azimuth in 22.5..67.5 -> "North-East (NE)"
                    azimuth in 67.5..112.5 -> "East (E)"
                    azimuth in 112.5..157.5 -> "South-East (SE)"
                    azimuth in 157.5..202.5 -> "South (S)"
                    azimuth in 202.5..247.5 -> "South-West (SW)"
                    azimuth in 247.5..292.5 -> "West (W)"
                    else -> "North-West (NW)"
                }
                Text(text = dir, color = GreenSuccess, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SensorAxisBox(axis: String, value: Float) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = DarkSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = axis, fontWeight = FontWeight.Bold, color = RedAccent)
            Text(text = String.format(Locale.US, "%.2f", value), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun TestDialogScaffold(
    title: String,
    instruction: String,
    language: AppLanguage,
    onComplete: (TestState) -> Unit,
    onCancel: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkSurface)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = instruction, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 12.dp))
            Spacer(modifier = Modifier.height(28.dp))
            content()
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = { onComplete(TestState.FAILED) },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (language == AppLanguage.BANGLA) "ব্যর্থ (Fail)" else "Failed")
            }
            Button(
                onClick = { onComplete(TestState.PASS) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess)
            ) {
                Text(if (language == AppLanguage.BANGLA) "সফল (Pass)" else "Pass")
            }
        }
    }
}
