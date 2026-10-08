package com.example.ui.screens.diagnostics

import android.content.Context
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.MultiFormatWriter
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

data class ParsedWifiInfo(
    val ssid: String,
    val password: String,
    val security: String,
    val isHidden: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiQrScreen(
    language: AppLanguage,
    prefs: PreferencesManager,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (language == AppLanguage.BANGLA) "ওয়াইফাই কিউআর টুলস" else "Wi-Fi QR Tools",
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
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkSurface,
                contentColor = RedAccent
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(if (language == AppLanguage.BANGLA) "কিউআর তৈরি" else "Generate QR", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.QrCode, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(if (language == AppLanguage.BANGLA) "কিউআর স্ক্যান" else "Scan / Read", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) }
                )
            }

            if (selectedTab == 0) {
                WifiQrGeneratorContent(language, prefs)
            } else {
                WifiQrScannerContent(language, prefs)
            }
        }
    }
}

@Composable
fun WifiQrGeneratorContent(
    language: AppLanguage,
    prefs: PreferencesManager
) {
    val context = LocalContext.current
    var ssid by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var securityType by remember { mutableStateOf("WPA") } // WPA, WEP, nopass
    var isHidden by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isGenerating by remember { mutableStateOf(false) }

    val wifiPayload = remember(ssid, password, securityType, isHidden) {
        if (ssid.isBlank()) ""
        else {
            val sec = if (securityType == "nopass") "nopass" else securityType
            "WIFI:T:$sec;S:$ssid;P:$password;H:${if (isHidden) "true" else "false"};;"
        }
    }

    LaunchedEffect(wifiPayload) {
        if (wifiPayload.isNotBlank()) {
            delay(150)
            isGenerating = true
            val bitmap = withContext(Dispatchers.Default) {
                try {
                    val bitMatrix = MultiFormatWriter().encode(
                        wifiPayload,
                        BarcodeFormat.QR_CODE,
                        512,
                        512
                    )
                    val width = bitMatrix.width
                    val height = bitMatrix.height
                    val pixels = IntArray(width * height)
                    for (y in 0 until height) {
                        val offset = y * width
                        for (x in 0 until width) {
                            pixels[offset + x] = if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                        }
                    }
                    Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
                } catch (e: Exception) {
                    null
                }
            }
            qrBitmap = bitmap
            isGenerating = false
        } else {
            qrBitmap = null
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = if (language == AppLanguage.BANGLA) "ওয়াইফাই শেয়ারিং কিউআর কোড বানান" else "Create Instant Wi-Fi Sharing QR Card",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (language == AppLanguage.BANGLA) "যেকোনো স্মার্টফোন ক্যামেরা দিয়ে স্ক্যান করলেই সরাসরি ওয়াইফাই কানেক্ট হবে।" else "Anyone scanning this QR code connects immediately without typing passwords.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // SSID Field
        item {
            OutlinedTextField(
                value = ssid,
                onValueChange = { ssid = it },
                label = { Text(if (language == AppLanguage.BANGLA) "ওয়াইফাই নেটওয়ার্কের নাম (SSID)" else "Network Name (SSID)") },
                leadingIcon = { Icon(Icons.Default.Wifi, contentDescription = null, tint = RedAccent) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RedAccent,
                    unfocusedBorderColor = DarkBorder,
                    focusedContainerColor = DarkSurfaceVariant,
                    unfocusedContainerColor = DarkSurfaceVariant
                )
            )
        }

        // Password Field (if not open)
        if (securityType != "nopass") {
            item {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(if (language == AppLanguage.BANGLA) "ওয়াইফাই পাসওয়ার্ড" else "Wi-Fi Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = RedAccent) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle password",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RedAccent,
                        unfocusedBorderColor = DarkBorder,
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant
                    )
                )
            }
        }

        // Security Type Chips
        item {
            Text(
                text = if (language == AppLanguage.BANGLA) "নিরাপত্তা ব্যবস্থা (Security)" else "Security Protocol",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("WPA" to "WPA / WPA2 / WPA3", "WEP" to "WEP", "nopass" to "Open (No Password)").forEach { (sec, label) ->
                    FilterChip(
                        selected = securityType == sec,
                        onClick = { securityType = sec },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RedAccent,
                            containerColor = DarkSurfaceElevated
                        )
                    )
                }
            }
        }

        // Hidden Network Toggle
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { isHidden = !isHidden }
            ) {
                Checkbox(
                    checked = isHidden,
                    onCheckedChange = { isHidden = it },
                    colors = CheckboxDefaults.colors(checkedColor = RedAccent)
                )
                Text(
                    text = if (language == AppLanguage.BANGLA) "লুকানো নেটওয়ার্ক (Hidden SSID)" else "Hidden Network (Don't broadcast SSID)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Generated QR Card
        if (qrBitmap != null) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Image(
                                bitmap = qrBitmap!!.asImageBitmap(),
                                contentDescription = "Wi-Fi QR Code",
                                modifier = Modifier
                                    .size(230.dp)
                                    .padding(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = ssid,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Security: $securityType • ${if (password.isNotEmpty()) "Protected" else "Open"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = {
                                    ToolActions.copyToClipboard(context, wifiPayload)
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(Strings.copy(language))
                            }

                            Button(
                                onClick = {
                                    ToolActions.shareText(context, "Wi-Fi Network: $ssid\nPassword: $password\nPayload: $wifiPayload")
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RedAccent)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(Strings.share(language))
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

@Composable
fun WifiQrScannerContent(
    language: AppLanguage,
    prefs: PreferencesManager
) {
    val context = LocalContext.current
    var scannedResult by remember { mutableStateOf<ParsedWifiInfo?>(null) }
    var scanError by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (bitmap != null) {
                    val width = bitmap.width
                    val height = bitmap.height
                    val pixels = IntArray(width * height)
                    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
                    bitmap.recycle()

                    val source = RGBLuminanceSource(width, height, pixels)
                    val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
                    val result = MultiFormatReader().decode(binaryBitmap)

                    val text = result.text
                    if (text.startsWith("WIFI:", ignoreCase = true)) {
                        scannedResult = parseWifiPayload(text)
                        scanError = null
                    } else {
                        scanError = if (language == AppLanguage.BANGLA) "ছবিটিতে কোনো ওয়াইফাই কিউআর কোড পাওয়া যায়নি।" else "Scanned QR code is not a Wi-Fi configuration card."
                    }
                }
            } catch (e: Exception) {
                scanError = if (language == AppLanguage.BANGLA) "কিউআর কোড পড়া যায়নি।" else "Could not decode QR image. Please try a clearer photo."
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = if (language == AppLanguage.BANGLA) "ওয়াইফাই কিউআর রিডার ও বিশ্লেষক" else "Wi-Fi QR Code Inspector",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (language == AppLanguage.BANGLA) "গ্যালারি থেকে ওয়াইফাই কিউআর সিলেক্ট করে নেটওয়ার্ক নাম ও পাসওয়ার্ড বের করুন।" else "Select a Wi-Fi QR code photo to reveal network credentials and password safely.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Button(
                onClick = { photoPickerLauncher.launch("image/*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RedAccent)
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (language == AppLanguage.BANGLA) "গ্যালারি থেকে কিউআর ছবি বেছে নিন" else "Pick QR Code Image from Gallery",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (scanError != null) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = RedAccent.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RedAccent)
                ) {
                    Text(
                        text = scanError!!,
                        color = RedAccent,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }

        if (scannedResult != null) {
            val res = scannedResult!!
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(GreenSuccess.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Wifi, contentDescription = null, tint = GreenSuccess, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (language == AppLanguage.BANGLA) "শনাক্তকৃত ওয়াইফাই" else "Detected Wi-Fi Network",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Security: ${res.security}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GreenSuccess
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // SSID row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Network Name (SSID)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = res.ssid, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                            }
                            IconButton(onClick = { ToolActions.copyToClipboard(context, res.ssid) }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy SSID", tint = RedAccent)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Password row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Password", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = if (res.password.isNotBlank()) res.password else "(Open Network)", fontWeight = FontWeight.Bold, color = if (res.password.isNotBlank()) RedAccent else GreenSuccess, fontSize = 16.sp)
                            }
                            if (res.password.isNotBlank()) {
                                IconButton(onClick = { ToolActions.copyToClipboard(context, res.password) }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy Password", tint = RedAccent)
                                }
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

fun parseWifiPayload(raw: String): ParsedWifiInfo {
    // Format: WIFI:T:WPA;S:MySSID;P:MyPassword;H:false;;
    var ssid = ""
    var password = ""
    var security = "WPA"
    var isHidden = false

    val content = raw.removePrefix("WIFI:").removePrefix("wifi:").removeSuffix(";;")
    val tokens = content.split(";")
    for (t in tokens) {
        if (t.startsWith("S:", ignoreCase = true)) {
            ssid = t.substring(2)
        } else if (t.startsWith("P:", ignoreCase = true)) {
            password = t.substring(2)
        } else if (t.startsWith("T:", ignoreCase = true)) {
            security = t.substring(2)
        } else if (t.startsWith("H:", ignoreCase = true)) {
            isHidden = t.substring(2).equals("true", ignoreCase = true)
        }
    }

    return ParsedWifiInfo(ssid, password, security, isHidden)
}
