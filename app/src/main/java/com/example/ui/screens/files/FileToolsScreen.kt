package com.example.ui.screens.files

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.data.PreferencesManager
import com.example.localization.AppLanguage
import com.example.ui.components.PrimaryActionButton
import com.example.ui.components.ToolBoxTopBar
import com.example.ui.components.ToolInputField
import com.example.ui.components.ToolResultCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.RedAccent
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun FileToolsScreen(
    initialToolId: String,
    language: AppLanguage,
    prefs: PreferencesManager,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val isBn = language == AppLanguage.BANGLA
    var activeTool by remember { mutableStateOf(initialToolId) }

    Scaffold(
        topBar = {
            ToolBoxTopBar(
                title = when (activeTool) {
                    "file_name_gen" -> if (isBn) "ফাইল নাম জেনারেটর" else "File Name Generator"
                    "file_info" -> if (isBn) "ফাইল বিবরণী ও তথ্য" else "File Inspector"
                    else -> "File Utility"
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
                    "file_name_gen" to (if (isBn) "নাম জেনারেটর" else "Name Generator"),
                    "file_info" to (if (isBn) "ফাইল বিবরণী" else "File Inspector")
                ).forEach { (id, label) ->
                    FilterChip(
                        selected = activeTool == id,
                        onClick = { activeTool = id },
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
                "file_name_gen" -> {
                    var baseTitle by remember { mutableStateOf("My Project Report") }
                    var extension by remember { mutableStateOf("pdf") }
                    var formatStyle by remember { mutableStateOf("timestamp") } // timestamp, slug, uuid

                    val generatedName by remember(baseTitle, extension, formatStyle) {
                        derivedStateOf {
                            val sanitizedTitle = baseTitle.trim().lowercase()
                                .replace("[^a-zA-Z0-9]+".toRegex(), "_")
                                .trim('_')
                            val ext = extension.trim().removePrefix(".")

                            when (formatStyle) {
                                "timestamp" -> {
                                    val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                                    "${sanitizedTitle}_$ts.$ext"
                                }
                                "slug" -> {
                                    val slug = baseTitle.trim().lowercase().replace("[^a-zA-Z0-9]+".toRegex(), "-").trim('-')
                                    "$slug.$ext"
                                }
                                "uuid" -> {
                                    val uid = UUID.randomUUID().toString().take(8)
                                    "${sanitizedTitle}_$uid.$ext"
                                }
                                else -> "$sanitizedTitle.$ext"
                            }
                        }
                    }

                    ToolInputField(
                        value = baseTitle,
                        onValueChange = { baseTitle = it },
                        label = if (isBn) "ফাইলের বিষয় / শিরোনাম" else "Base Subject / Title"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ToolInputField(
                        value = extension,
                        onValueChange = { extension = it },
                        label = if (isBn) "এক্সটেনশন (e.g. pdf, png, docx)" else "File Extension (e.g. pdf, png, docx)"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            "timestamp" to "Date_Time",
                            "slug" to "Kebab-slug",
                            "uuid" to "Random-ID"
                        ).forEach { (st, label) ->
                            FilterChip(
                                selected = formatStyle == st,
                                onClick = { formatStyle = st },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RedAccent)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    ToolResultCard(
                        resultValue = generatedName,
                        title = if (isBn) "নিরাপদ ফাইলের নাম" else "Sanitized File Name"
                    )
                }

                "file_info" -> {
                    var selectedUri by remember { mutableStateOf<Uri?>(null) }
                    var fileDetails by remember { mutableStateOf<String?>(null) }

                    val filePickerLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                    ) { uri: Uri? ->
                        selectedUri = uri
                        if (uri != null) {
                            fileDetails = queryFileInfo(context, uri)
                        }
                    }

                    PrimaryActionButton(
                        text = if (isBn) "যেকোনো ফাইল নির্বাচন করুন" else "Pick File to Inspect",
                        onClick = { filePickerLauncher.launch("*/*") },
                        icon = { Icon(Icons.Default.FileOpen, contentDescription = null) }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (fileDetails != null) {
                        ToolResultCard(
                            resultValue = selectedUri?.lastPathSegment.orEmpty(),
                            title = if (isBn) "ফাইলের তথ্য ও বৈশিষ্ট্য" else "File Metadata & Inspection",
                            subtitle = fileDetails
                        )
                    } else {
                        Text(
                            text = if (isBn) "ফাইলের সাইজ, ফরম্যাট, এবং পাথ দেখার জন্য ফাইল নির্বাচন করুন।"
                            else "Select any file to inspect exact bytes, KB, MIME type, and URI details.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

fun queryFileInfo(context: Context, uri: Uri): String {
    return try {
        val returnCursor = context.contentResolver.query(uri, null, null, null, null)
        val nameIndex = returnCursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME) ?: -1
        val sizeIndex = returnCursor?.getColumnIndex(OpenableColumns.SIZE) ?: -1

        returnCursor?.moveToFirst()
        val name = if (nameIndex != -1) returnCursor?.getString(nameIndex) else "Unknown"
        val sizeBytes = if (sizeIndex != -1) returnCursor?.getLong(sizeIndex) ?: 0L else 0L
        returnCursor?.close()

        val mimeType = context.contentResolver.getType(uri) ?: "Unknown MIME"
        val sizeKb = sizeBytes / 1024.0
        val sizeMb = sizeKb / 1024.0
        val df = DecimalFormat("#,##0.00")

        "Name: $name\n" +
        "MIME Type: $mimeType\n" +
        "Size: ${df.format(sizeKb)} KB (${df.format(sizeMb)} MB / $sizeBytes bytes)\n" +
        "Scheme: ${uri.scheme} • Authority: ${uri.authority}"
    } catch (_: Exception) {
        "URI: $uri"
    }
}
