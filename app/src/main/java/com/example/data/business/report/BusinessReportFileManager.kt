package com.example.data.business.report

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

object BusinessReportFileManager {

    /**
     * Opens the native Android share sheet with FileProvider Uri.
     */
    fun shareReportFile(
        context: Context,
        file: File,
        mimeType: String,
        title: String
    ): Result<Unit> {
        return try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, title).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Saves or copies the report file into public Downloads directory (using MediaStore on Android 10+).
     * Returns the human-readable path or success confirmation.
     */
    fun saveReportToDownloads(
        context: Context,
        file: File,
        mimeType: String,
        displayName: String
    ): Result<String> {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/ToolBox_Reports")
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    ?: throw IllegalStateException("Could not create MediaStore entry")
                resolver.openOutputStream(uri)?.use { out ->
                    file.inputStream().use { input ->
                        input.copyTo(out)
                    }
                }
                Result.success("Downloads/ToolBox_Reports/$displayName")
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val targetDir = File(downloadsDir, "ToolBox_Reports").apply { mkdirs() }
                val targetFile = File(targetDir, displayName)
                file.copyTo(targetFile, overwrite = true)
                Result.success(targetFile.absolutePath)
            }
        } catch (e: Exception) {
            // Fallback to app external documents or files
            try {
                val fallbackDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                    ?: File(context.filesDir, "ToolBox_Reports")
                fallbackDir.mkdirs()
                val targetFile = File(fallbackDir, displayName)
                file.copyTo(targetFile, overwrite = true)
                Result.success(targetFile.absolutePath)
            } catch (ex: Exception) {
                Result.failure(ex)
            }
        }
    }
}
