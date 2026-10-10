package com.example.ui.screens.business

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.business.sync.BusinessSyncState
import com.example.data.business.sync.SyncStatusEnum
import com.example.localization.AppLanguage
import com.example.localization.Strings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CloudSyncCard(
    syncState: BusinessSyncState,
    lang: AppLanguage,
    onSyncNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by FindRememberedDialogState()

    val isSyncing = syncState.status == SyncStatusEnum.SYNCING

    val (statusColor, statusIcon, statusLabel) = when (syncState.status) {
        SyncStatusEnum.SUCCESS -> Triple(
            Color(0xFF10B981),
            Icons.Default.CloudDone,
            Strings.bizStatusSynced(lang)
        )
        SyncStatusEnum.SYNCING -> Triple(
            Color(0xFF3B82F6),
            Icons.Default.CloudSync,
            Strings.bizStatusSyncing(lang)
        )
        SyncStatusEnum.OFFLINE_QUEUED -> Triple(
            Color(0xFFF59E0B),
            Icons.Default.CloudOff,
            "${Strings.bizStatusOffline(lang)} (${syncState.pendingCount})"
        )
        SyncStatusEnum.SETUP_REQUIRED -> Triple(
            Color(0xFFEAB308),
            Icons.Default.Warning,
            Strings.bizStatusSetupRequired(lang)
        )
        SyncStatusEnum.AUTH_REQUIRED -> Triple(
            Color(0xFFF97316),
            Icons.Default.CloudQueue,
            Strings.bizStatusAuthRequired(lang)
        )
        SyncStatusEnum.ERROR -> Triple(
            Color(0xFFEF4444),
            Icons.Default.Warning,
            "Error (${syncState.pendingCount} queued)"
        )
        SyncStatusEnum.IDLE -> Triple(
            if (syncState.lastSyncTimestamp > 0) Color(0xFF10B981) else Color(0xFF94A3B8),
            Icons.Default.CloudQueue,
            if (syncState.lastSyncTimestamp > 0) Strings.bizStatusSynced(lang) else Strings.bizNeverSynced(lang)
        )
    }

    val formattedLastSync = if (syncState.lastSyncTimestamp > 0) {
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        sdf.format(Date(syncState.lastSyncTimestamp))
    } else {
        Strings.bizNeverSynced(lang)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { showDialog = true }
            .testTag("cloud_sync_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF181C24)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(statusColor.copy(alpha = 0.35f))
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSyncing) {
                        val infiniteTransition = rememberInfiniteTransition(label = "rotation")
                        val angle by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1200, easing = LinearEasing)
                            ),
                            label = "spin"
                        )
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Syncing",
                            tint = statusColor,
                            modifier = Modifier
                                .size(22.dp)
                                .rotate(angle)
                        )
                    } else {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = statusLabel,
                            tint = statusColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = Strings.bizCloudSync(lang),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFE2E8F0)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(statusColor.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = statusLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${Strings.bizLastSynced(lang)} $formattedLastSync",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onSyncNow,
                    enabled = !isSyncing,
                    modifier = Modifier.testTag("sync_now_button")
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFF38BDF8),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = Strings.bizSyncNow(lang),
                            tint = Color(0xFF38BDF8)
                        )
                    }
                }

                IconButton(
                    onClick = { showDialog = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Diagnostics",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    if (showDialog) {
        SyncDiagnosticsDialog(
            syncState = syncState,
            lang = lang,
            onDismiss = { showDialog = false },
            onSyncNow = {
                onSyncNow()
            }
        )
    }
}

@Composable
private fun FindRememberedDialogState() = remember { mutableStateOf(false) }

@Composable
fun SyncDiagnosticsDialog(
    syncState: BusinessSyncState,
    lang: AppLanguage,
    onDismiss: () -> Unit,
    onSyncNow: () -> Unit
) {
    val report = syncState.report

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CloudSync,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = Strings.bizSyncDetails(lang),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Section 1: Offline Protection
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F2E22))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "🛡️ 100% Offline-First Data Storage",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF34D399)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "All sales, purchases, customers, and stock are saved securely in your local on-device SQLite database. No data will ever be lost without internet.",
                            fontSize = 11.sp,
                            color = Color(0xFFA7F3D0)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section 2: Sync Queue Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Pending Queue Items:",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = "${syncState.pendingCount} records",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (syncState.pendingCount > 0) Color(0xFFF59E0B) else Color(0xFF10B981)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Section 3: Diagnostic Checklist
                Text(
                    text = "Firebase Cloud Diagnostics:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFCBD5E1)
                )
                Spacer(modifier = Modifier.height(4.dp))

                DiagnosticItem(
                    label = "google-services.json Config",
                    isPassed = report.hasGoogleServicesConfig,
                    missingHint = "Not present in project"
                )
                DiagnosticItem(
                    label = "Firebase App Initialized",
                    isPassed = report.isFirebaseInitialized,
                    missingHint = "Requires backend provisioning"
                )
                DiagnosticItem(
                    label = "Cloud Firestore DB",
                    isPassed = report.isFirestoreAvailable,
                    missingHint = "Firestore not connected"
                )
                DiagnosticItem(
                    label = "Firebase Auth / Sign-In",
                    isPassed = report.currentUserUid != null,
                    missingHint = if (report.isAuthAvailable) "Sign-in required" else "Auth not ready"
                )

                if (report.missingRequirements.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF2D2310))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "⚠️ Missing Setup Requirements:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFBBF24)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            report.missingRequirements.forEach { req ->
                                Text(
                                    text = "• $req",
                                    fontSize = 10.sp,
                                    color = Color(0xFFFDE68A)
                                )
                            }
                        }
                    }
                }

                if (!syncState.errorMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Notice: ${syncState.errorMessage}",
                        fontSize = 10.sp,
                        color = Color(0xFFF87171)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSyncNow,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                modifier = Modifier.testTag("dialog_sync_button")
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = Strings.bizSyncNow(lang))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(text = "Close")
            }
        },
        containerColor = Color(0xFF1E222A),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun DiagnosticItem(
    label: String,
    isPassed: Boolean,
    missingHint: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF94A3B8)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (isPassed) "✅ Ready" else "❌ $missingHint",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (isPassed) Color(0xFF34D399) else Color(0xFFF87171)
            )
        }
    }
}
