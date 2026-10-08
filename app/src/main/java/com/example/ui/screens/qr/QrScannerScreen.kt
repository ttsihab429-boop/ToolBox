package com.example.ui.screens.qr

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.PreferencesManager
import com.example.localization.AppLanguage
import com.example.ui.components.PrimaryActionButton
import com.example.ui.components.SecondaryActionButton
import com.example.ui.components.ToolActions
import com.example.ui.components.ToolBoxTopBar
import com.example.ui.components.ToolResultCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.RedAccent
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer

@Composable
fun QrScannerScreen(
    language: AppLanguage,
    prefs: PreferencesManager,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val isBn = language == AppLanguage.BANGLA
    var scannedResult by remember { mutableStateOf("") }
    var scanFormat by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            decodeImageUri(context, uri) { result, format ->
                if (result != null) {
                    scannedResult = result
                    scanFormat = format ?: "QR_CODE"
                    errorMessage = null
                    ToolActions.triggerHaptic(context, true)
                } else {
                    errorMessage = if (isBn) "ছবিতে কোনো কিউআর বা বারকোড পাওয়া যায়নি।" else "No QR or barcode detected in image."
                }
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, if (isBn) "ক্যামেরা প্রস্তুত! ছবির জন্য গ্যালারি অথবা ক্যামেরা ব্যবহার করুন।" else "Camera granted! Pick image to scan.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, if (isBn) "ক্যামেরা পারমিশন পাওয়া যায়নি" else "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            ToolBoxTopBar(
                title = if (isBn) "কিউআর ও বারকোড স্ক্যানার" else "QR & Barcode Scanner",
                onBackClick = onBackClick,
                isFavorite = prefs.isFavorite("qr_scanner"),
                onFavoriteToggle = { prefs.toggleFavorite("qr_scanner") }
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
            Spacer(modifier = Modifier.height(16.dp))

            // Scanner graphic card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurface)
                    .border(2.dp, RedAccent.copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scanner",
                        tint = RedAccent,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isBn) "যেকোনো ছবি থেকে কিউআর ও বারকোড স্ক্যান করুন" else "Scan QR codes and barcodes from images",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Select Image Button
            PrimaryActionButton(
                text = if (isBn) "গ্যালারি থেকে ছবি স্ক্যান করুন" else "Pick Image from Gallery",
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                icon = {
                    Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            SecondaryActionButton(
                text = if (isBn) "ক্যামেরা পারমিশন চেক" else "Check Camera Permission",
                onClick = {
                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                }
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (scannedResult.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))

                ToolResultCard(
                    resultValue = scannedResult,
                    title = if (isBn) "স্ক্যানকৃত ফলাফল ($scanFormat)" else "Scanned Result ($scanFormat)",
                    subtitle = "Format: $scanFormat"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action shortcuts (Open URL, Call Phone, Share)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (scannedResult.startsWith("http://") || scannedResult.startsWith("https://")) {
                        IconButton(
                            onClick = {
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(scannedResult))
                                context.startActivity(browserIntent)
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurfaceElevated)
                        ) {
                            Icon(Icons.Default.OpenInBrowser, contentDescription = "Open URL", tint = RedAccent)
                        }
                    }

                    if (scannedResult.startsWith("tel:") || scannedResult.matches(Regex("^[+0-9\\-\\s]+$"))) {
                        IconButton(
                            onClick = {
                                val telUri = if (scannedResult.startsWith("tel:")) scannedResult else "tel:$scannedResult"
                                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse(telUri))
                                context.startActivity(dialIntent)
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurfaceElevated)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Call Number", tint = RedAccent)
                        }
                    }

                    IconButton(
                        onClick = { ToolActions.copyToClipboard(context, scannedResult) },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceElevated)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.onSurface)
                    }

                    IconButton(
                        onClick = { ToolActions.shareText(context, scannedResult) },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceElevated)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

fun decodeImageUri(context: Context, uri: Uri, onResult: (String?, String?) -> Unit) {
    try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        if (bitmap == null) {
            onResult(null, null)
            return
        }

        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val source = RGBLuminanceSource(width, height, pixels)
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

        val reader = MultiFormatReader()
        val result = reader.decode(binaryBitmap)

        onResult(result.text, result.barcodeFormat?.name)
    } catch (_: Exception) {
        onResult(null, null)
    }
}
