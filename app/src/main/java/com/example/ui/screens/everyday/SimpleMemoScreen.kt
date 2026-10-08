package com.example.ui.screens.everyday

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.PreferencesManager
import com.example.localization.AppLanguage
import com.example.ui.components.ToolActions
import com.example.ui.components.ToolBoxTopBar
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.RedAccent

@Composable
fun SimpleMemoScreen(
    language: AppLanguage,
    prefs: PreferencesManager,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val isBn = language == AppLanguage.BANGLA
    val savedMemo by prefs.memoContent.collectAsState()
    var memoText by remember(savedMemo) { mutableStateOf(savedMemo) }

    Scaffold(
        topBar = {
            ToolBoxTopBar(
                title = if (isBn) "স্ক্র্যাচপ্যাড / মেমো" else "Instant Scratchpad",
                onBackClick = onBackClick,
                isFavorite = prefs.isFavorite("simple_memo"),
                onFavoriteToggle = { prefs.toggleFavorite("simple_memo") },
                actions = {
                    IconButton(
                        onClick = { ToolActions.copyToClipboard(context, memoText) },
                        modifier = Modifier.testTag("memo_copy_button")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(
                        onClick = { ToolActions.shareText(context, memoText) },
                        modifier = Modifier.testTag("memo_share_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(
                        onClick = {
                            memoText = ""
                            prefs.saveMemo("")
                        },
                        modifier = Modifier.testTag("memo_clear_button")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = if (isBn) "⚡ স্বয়ংক্রিয়ভাবে সেভ হয়। অ্যাপ বন্ধ করলেও লেখা মুছে যাবে না।"
                else "⚡ Auto-saves instantly. Persists even if you close the app.",
                style = MaterialTheme.typography.bodySmall,
                color = RedAccent
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = memoText,
                onValueChange = {
                    memoText = it
                    prefs.saveMemo(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("memo_text_field"),
                placeholder = {
                    Text(
                        if (isBn) "এখানে যেকোনো কিছু পেস্ট করুন বা লিখুন..."
                        else "Paste phone numbers, links, scratch notes here..."
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RedAccent,
                    unfocusedBorderColor = DarkBorder,
                    focusedContainerColor = DarkSurfaceVariant,
                    unfocusedContainerColor = DarkSurfaceVariant
                )
            )
        }
    }
}
