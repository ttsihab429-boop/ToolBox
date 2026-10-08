package com.example.ui.screens.qr

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.data.PreferencesManager
import com.example.localization.AppLanguage
import com.example.ui.components.PrimaryActionButton
import com.example.ui.components.SecondaryActionButton
import com.example.ui.components.ToolBoxTopBar
import com.example.ui.components.ToolInputField
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.RedAccent
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

enum class QrType { TEXT, URL, PHONE, EMAIL, WIFI }

@Composable
fun QrGeneratorScreen(
    language: AppLanguage,
    prefs: PreferencesManager,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isBn = language == AppLanguage.BANGLA
    var qrType by remember { mutableStateOf(QrType.TEXT) }

    var textInput by remember { mutableStateOf("https://google.com") }
    var phoneInput by remember { mutableStateOf("+8801646864645") }
    var emailInput by remember { mutableStateOf("contact@example.com") }
    var emailSubject by remember { mutableStateOf("Hello ToolBox") }
    var wifiSsid by remember { mutableStateOf("MyHomeWifi") }
    var wifiPassword by remember { mutableStateOf("password123") }
    var wifiEncryption by remember { mutableStateOf("WPA") }

    val payload = remember(qrType, textInput, phoneInput, emailInput, emailSubject, wifiSsid, wifiPassword, wifiEncryption) {
        when (qrType) {
            QrType.TEXT -> textInput
            QrType.URL -> if (textInput.startsWith("http://") || textInput.startsWith("https://")) textInput else "https://$textInput"
            QrType.PHONE -> "tel:$phoneInput"
            QrType.EMAIL -> "mailto:$emailInput?subject=${Uri.encode(emailSubject)}"
            QrType.WIFI -> "WIFI:S:$wifiSsid;T:$wifiEncryption;P:$wifiPassword;;"
        }
    }

    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isGenerating by remember { mutableStateOf(false) }

    // Generate QR off the main thread with debounce and prompt bitmap recycling
    LaunchedEffect(payload) {
        if (payload.isNotBlank()) {
            delay(200) // Debounce keystrokes so typing is smooth on weak phones
            isGenerating = true
            val bitmap = withContext(Dispatchers.Default) {
                generateQrBitmapOptimized(payload, 384)
            }
            val oldBitmap = qrBitmap
            qrBitmap = bitmap
            if (oldBitmap != null && oldBitmap != bitmap && !oldBitmap.isRecycled) {
                oldBitmap.recycle()
            }
            isGenerating = false
        } else {
            val oldBitmap = qrBitmap
            qrBitmap = null
            if (oldBitmap != null && !oldBitmap.isRecycled) {
                oldBitmap.recycle()
            }
        }
    }

    Scaffold(
        topBar = {
            ToolBoxTopBar(
                title = if (isBn) "কিউআর কোড জেনারেটর" else "QR Code Generator",
                onBackClick = onBackClick,
                isFavorite = prefs.isFavorite("qr_generator"),
                onFavoriteToggle = { prefs.toggleFavorite("qr_generator") }
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Type Selector Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    QrType.TEXT to (if (isBn) "টেক্সট" else "Text"),
                    QrType.URL to "URL",
                    QrType.PHONE to (if (isBn) "ফোন" else "Phone"),
                    QrType.EMAIL to (if (isBn) "ইমেইল" else "Email"),
                    QrType.WIFI to "Wi-Fi"
                ).forEach { (type, label) ->
                    FilterChip(
                        selected = qrType == type,
                        onClick = { qrType = type },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RedAccent,
                            containerColor = DarkSurfaceElevated
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Inputs based on type
            when (qrType) {
                QrType.TEXT, QrType.URL -> {
                    ToolInputField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        label = if (qrType == QrType.URL) "Website URL" else "Text Content",
                        singleLine = qrType == QrType.URL
                    )
                }
                QrType.PHONE -> {
                    ToolInputField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = if (isBn) "ফোন নম্বর" else "Phone Number"
                    )
                }
                QrType.EMAIL -> {
                    ToolInputField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = if (isBn) "ইমেইল ঠিকানা" else "Email Address"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ToolInputField(
                        value = emailSubject,
                        onValueChange = { emailSubject = it },
                        label = if (isBn) "বিষয় (Subject)" else "Email Subject"
                    )
                }
                QrType.WIFI -> {
                    ToolInputField(
                        value = wifiSsid,
                        onValueChange = { wifiSsid = it },
                        label = if (isBn) "নেটওয়ার্ক নাম (SSID)" else "Network Name (SSID)"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ToolInputField(
                        value = wifiPassword,
                        onValueChange = { wifiPassword = it },
                        label = if (isBn) "পাসওয়ার্ড" else "Password"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // QR Code Preview Box
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(androidx.compose.ui.graphics.Color.White)
                    .border(2.dp, RedAccent.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isGenerating && qrBitmap == null) {
                    CircularProgressIndicator(color = RedAccent)
                } else if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap!!.asImageBitmap(),
                        contentDescription = "Generated QR Code",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = if (isBn) "তথ্য লিখুন" else "Enter data",
                        color = androidx.compose.ui.graphics.Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save & Share Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PrimaryActionButton(
                    text = if (isBn) "গ্যালারিতে সেভ" else "Save QR",
                    onClick = {
                        qrBitmap?.let { bmp ->
                            coroutineScope.launch(Dispatchers.IO) {
                                saveQrToGalleryAsync(context, bmp)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    icon = {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White)
                    }
                )

                SecondaryActionButton(
                    text = if (isBn) "শেয়ার" else "Share QR",
                    onClick = {
                        qrBitmap?.let { bmp ->
                            coroutineScope.launch(Dispatchers.IO) {
                                shareQrBitmapAsync(context, bmp)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    icon = {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                    }
                )
            }
        }
    }
}

// Bulk pixel copy using setPixels (10x faster than setPixel per iteration)
fun generateQrBitmapOptimized(content: String, size: Int): Bitmap? {
    return try {
        val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
        val pixels = IntArray(size * size)
        val black = AndroidColor.BLACK
        val white = AndroidColor.WHITE
        var offset = 0
        for (y in 0 until size) {
            for (x in 0 until size) {
                pixels[offset++] = if (bitMatrix[x, y]) black else white
            }
        }
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        bitmap.setPixels(pixels, 0, size, 0, 0, size, size)
        bitmap
    } catch (_: Exception) {
        null
    }
}

suspend fun saveQrToGalleryAsync(context: Context, bitmap: Bitmap) {
    try {
        val filename = "ToolBox_QR_${System.currentTimeMillis()}.png"
        val fos: OutputStream?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ToolBox")
            }
            val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            fos = imageUri?.let { resolver.openOutputStream(it) }
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }
        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "QR saved to Pictures/ToolBox", Toast.LENGTH_SHORT).show()
        }
    } catch (_: Exception) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Failed to save QR code", Toast.LENGTH_SHORT).show()
        }
    }
}

suspend fun shareQrBitmapAsync(context: Context, bitmap: Bitmap) {
    try {
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val file = File(cachePath, "shared_qr.png")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()

        val contentUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        withContext(Dispatchers.Main) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share QR Code"))
        }
    } catch (_: Exception) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Could not share QR image", Toast.LENGTH_SHORT).show()
        }
    }
}
