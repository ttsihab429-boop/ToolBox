package com.example.ui.screens.files

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.data.PreferencesManager
import com.example.localization.AppLanguage
import com.example.ui.components.PrimaryActionButton
import com.example.ui.components.SecondaryActionButton
import com.example.ui.components.ToolBoxTopBar
import com.example.ui.components.ToolInputField
import com.example.ui.components.ToolResultCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.RedAccent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@Composable
fun PdfToolsScreen(
    initialToolId: String,
    language: AppLanguage,
    prefs: PreferencesManager,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isBn = language == AppLanguage.BANGLA
    var activeTool by remember { mutableStateOf(initialToolId) }

    var lastGeneratedPdfFile by remember { mutableStateOf<File?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isGenerating by remember { mutableStateOf(false) }

    // Text to PDF state
    var docTitle by remember { mutableStateOf("ToolBox Document") }
    var docContent by remember {
        mutableStateOf("This PDF was created with ToolBox Android App.\n\nAll Your Everyday Tools, In One Place.\nOffline, fast, and secure.")
    }

    // Single Image to PDF
    var singleImageUri by remember { mutableStateOf<Uri?>(null) }
    val singleImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        singleImageUri = uri
    }

    // Multi Image to PDF
    val multiImageUris = remember { mutableStateListOf<Uri>() }
    val multiImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris: List<Uri> ->
        multiImageUris.clear()
        multiImageUris.addAll(uris)
    }

    Scaffold(
        topBar = {
            ToolBoxTopBar(
                title = when (activeTool) {
                    "text_to_pdf" -> if (isBn) "টেক্সট থেকে পিডিএফ" else "Text to PDF"
                    "image_to_pdf" -> if (isBn) "ছবি থেকে পিডিএফ" else "Image to PDF"
                    "multi_image_to_pdf" -> if (isBn) "একাধিক ছবি থেকে পিডিএফ" else "Multiple Images to PDF"
                    else -> "PDF Utility"
                },
                onBackClick = onBackClick,
                isFavorite = prefs.isFavorite(activeTool),
                onFavoriteToggle = { prefs.toggleFavorite(activeTool) }
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "text_to_pdf" to (if (isBn) "টেক্সট → PDF" else "Text → PDF"),
                    "image_to_pdf" to (if (isBn) "ছবি → PDF" else "Image → PDF"),
                    "multi_image_to_pdf" to (if (isBn) "অ্যালবাম → PDF" else "Multi-Image → PDF")
                ).forEach { (id, label) ->
                    FilterChip(
                        selected = activeTool == id,
                        onClick = {
                            activeTool = id
                            lastGeneratedPdfFile = null
                            statusMessage = null
                        },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RedAccent,
                            containerColor = DarkSurfaceElevated
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (activeTool) {
                "text_to_pdf" -> {
                    ToolInputField(
                        value = docTitle,
                        onValueChange = { docTitle = it },
                        label = if (isBn) "নথির শিরোনাম" else "Document Title"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ToolInputField(
                        value = docContent,
                        onValueChange = { docContent = it },
                        label = if (isBn) "নথির বিস্তারিত লেখা" else "Document Body Text",
                        singleLine = false,
                        maxLines = 8
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    PrimaryActionButton(
                        text = if (isBn) "পিডিএফ ফাইল তৈরি করুন" else "Generate PDF Document",
                        enabled = !isGenerating,
                        onClick = {
                            isGenerating = true
                            coroutineScope.launch {
                                val file = withContext(Dispatchers.IO) {
                                    createTextPdf(context, docTitle, docContent)
                                }
                                isGenerating = false
                                if (file != null) {
                                    lastGeneratedPdfFile = file
                                    statusMessage = "PDF generated: ${file.name} (${file.length() / 1024} KB)"
                                    Toast.makeText(context, "PDF successfully generated!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        icon = { Icon(Icons.Default.Description, contentDescription = null) }
                    )
                }

                "image_to_pdf" -> {
                    Text(
                        text = if (singleImageUri != null) "Selected Image: ${singleImageUri?.lastPathSegment}"
                        else if (isBn) "কোনো ছবি নির্বাচন করা হয়নি" else "No photo selected yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    SecondaryActionButton(
                        text = if (isBn) "গ্যালারি থেকে ছবি নির্বাচন করুন" else "Select Photo from Gallery",
                        onClick = {
                            singleImagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        icon = { Icon(Icons.Default.Image, contentDescription = null) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PrimaryActionButton(
                        text = if (isBn) "ছবি থেকে পিডিএফ বানান" else "Convert to PDF",
                        enabled = singleImageUri != null && !isGenerating,
                        onClick = {
                            singleImageUri?.let { uri ->
                                isGenerating = true
                                coroutineScope.launch {
                                    val file = withContext(Dispatchers.IO) {
                                        createImagesPdfOptimized(context, listOf(uri))
                                    }
                                    isGenerating = false
                                    if (file != null) {
                                        lastGeneratedPdfFile = file
                                        statusMessage = "PDF generated: ${file.name} (${file.length() / 1024} KB)"
                                        Toast.makeText(context, "PDF generated successfully!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    )
                }

                "multi_image_to_pdf" -> {
                    Text(
                        text = "${multiImageUris.size} ${if (isBn) "টি ছবি নির্বাচিত হয়েছে" else "photos selected"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    SecondaryActionButton(
                        text = if (isBn) "একাধিক ছবি নির্বাচন করুন (সর্বোচ্চ ১০টি)" else "Select Multiple Photos (Up to 10)",
                        onClick = {
                            multiImagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = null) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PrimaryActionButton(
                        text = if (isBn) "সব ছবি একত্র করে পিডিএফ তৈরি করুন" else "Merge into Multi-Page PDF",
                        enabled = multiImageUris.isNotEmpty() && !isGenerating,
                        onClick = {
                            isGenerating = true
                            coroutineScope.launch {
                                val file = withContext(Dispatchers.IO) {
                                    createImagesPdfOptimized(context, multiImageUris.toList())
                                }
                                isGenerating = false
                                if (file != null) {
                                    lastGeneratedPdfFile = file
                                    statusMessage = "Multi-page PDF generated: ${file.name} (${file.length() / 1024} KB)"
                                    Toast.makeText(context, "Multi-page PDF generated!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
            }

            if (isGenerating) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(color = RedAccent)
                    Spacer(modifier = Modifier.padding(start = 12.dp))
                    Text(
                        text = if (isBn) "পিডিএফ তৈরি হচ্ছে..." else "Creating PDF...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (lastGeneratedPdfFile != null) {
                Spacer(modifier = Modifier.height(20.dp))

                ToolResultCard(
                    resultValue = lastGeneratedPdfFile!!.name,
                    title = if (isBn) "পিডিএফ প্রস্তুত" else "PDF Document Ready",
                    subtitle = statusMessage,
                    onShare = {
                        sharePdfFile(context, lastGeneratedPdfFile!!)
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                PrimaryActionButton(
                    text = if (isBn) "পিডিএফ ফাইল শেয়ার করুন" else "Share Generated PDF",
                    onClick = { sharePdfFile(context, lastGeneratedPdfFile!!) },
                    icon = { Icon(Icons.Default.Share, contentDescription = null) }
                )
            }
        }
    }
}

fun createTextPdf(context: Context, title: String, content: String): File? {
    return try {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size in points
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            textSize = 22f
            isFakeBoldText = true
            color = android.graphics.Color.BLACK
        }
        val bodyPaint = Paint().apply {
            textSize = 13f
            color = android.graphics.Color.DKGRAY
        }

        canvas.drawText(title, 40f, 60f, titlePaint)

        var y = 100f
        content.lines().forEach { line ->
            if (line.length > 70) {
                val chunks = line.chunked(70)
                chunks.forEach { chunk ->
                    canvas.drawText(chunk, 40f, y, bodyPaint)
                    y += 18f
                }
            } else {
                canvas.drawText(line, 40f, y, bodyPaint)
                y += 18f
            }
        }

        pdfDocument.finishPage(page)

        val outputDir = File(context.cacheDir, "pdf")
        outputDir.mkdirs()
        val file = File(outputDir, "ToolBox_Doc_${System.currentTimeMillis()}.pdf")
        val outputStream = FileOutputStream(file)
        pdfDocument.writeTo(outputStream)
        pdfDocument.close()
        outputStream.close()
        file
    } catch (_: Exception) {
        null
    }
}

// Memory-optimized PDF image rendering with inSampleSize and immediate recycling
fun createImagesPdfOptimized(context: Context, uris: List<Uri>): File? {
    return try {
        val pdfDocument = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842

        uris.forEachIndexed { index, uri ->
            // Subsample image to target page dimension
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            var stream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(stream, null, bounds)
            stream?.close()

            var inSampleSize = 1
            if (bounds.outHeight > pageHeight || bounds.outWidth > pageWidth) {
                val halfHeight = bounds.outHeight / 2
                val halfWidth = bounds.outWidth / 2
                while ((halfHeight / inSampleSize) >= pageHeight && (halfWidth / inSampleSize) >= pageWidth) {
                    inSampleSize *= 2
                }
            }

            val opts = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.RGB_565 // Half memory footprint
            }

            stream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(stream, null, opts)
            stream?.close()

            if (bitmap != null) {
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas

                val scale = Math.min(
                    (pageWidth - 60).toFloat() / bitmap.width.toFloat(),
                    (pageHeight - 60).toFloat() / bitmap.height.toFloat()
                )
                val scaledWidth = bitmap.width * scale
                val scaledHeight = bitmap.height * scale
                val left = (pageWidth - scaledWidth) / 2
                val top = (pageHeight - scaledHeight) / 2

                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth.toInt().coerceAtLeast(1), scaledHeight.toInt().coerceAtLeast(1), true)
                canvas.drawBitmap(scaledBitmap, left, top, null)
                pdfDocument.finishPage(page)

                if (scaledBitmap != bitmap) {
                    scaledBitmap.recycle()
                }
                bitmap.recycle() // Promptly free native bitmap RAM
            }
        }

        val outputDir = File(context.cacheDir, "pdf")
        outputDir.mkdirs()
        val file = File(outputDir, "ToolBox_Images_${System.currentTimeMillis()}.pdf")
        val outputStream = FileOutputStream(file)
        pdfDocument.writeTo(outputStream)
        pdfDocument.close()
        outputStream.close()
        file
    } catch (_: Exception) {
        null
    }
}

fun sharePdfFile(context: Context, file: File) {
    try {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share PDF Document"))
    } catch (_: Exception) {
        Toast.makeText(context, "Failed to share PDF", Toast.LENGTH_SHORT).show()
    }
}
