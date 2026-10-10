package com.example.data.business.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.example.data.business.AppDatabase
import com.example.data.business.BusinessDao
import com.example.data.business.SyncQueueItem
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject

class BusinessSyncManager(private val context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val dao: BusinessDao = db.businessDao()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val prefs = context.getSharedPreferences("toolbox_business_sync", Context.MODE_PRIVATE)

    private val _syncState = MutableStateFlow(
        BusinessSyncState(
            lastSyncTimestamp = prefs.getLong(KEY_LAST_SYNC, 0L),
            report = checkFirebaseSetup()
        )
    )
    val syncState: StateFlow<BusinessSyncState> = _syncState.asStateFlow()

    init {
        // Collect pending sync items count to update state automatically
        scope.launch {
            dao.getPendingSyncCount().collectLatest { count ->
                val current = _syncState.value
                val newStatus = when {
                    current.status == SyncStatusEnum.SYNCING -> SyncStatusEnum.SYNCING
                    !current.report.isFirebaseInitialized || !current.report.hasGoogleServicesConfig -> SyncStatusEnum.SETUP_REQUIRED
                    !current.report.isAuthAvailable || current.report.currentUserUid == null -> SyncStatusEnum.AUTH_REQUIRED
                    !isNetworkAvailable() && count > 0 -> SyncStatusEnum.OFFLINE_QUEUED
                    count == 0 && current.lastSyncTimestamp > 0L -> SyncStatusEnum.SUCCESS
                    else -> current.status
                }
                _syncState.value = current.copy(
                    pendingCount = count,
                    status = newStatus
                )
            }
        }

        // Register network callback for automatic sync when internet returns
        registerNetworkCallback()
    }

    private fun registerNetworkCallback() {
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            if (cm != null) {
                val request = NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build()
                cm.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        scope.launch {
                            // Automatically trigger sync when internet returns
                            triggerSync(isManual = false)
                        }
                    }

                    override fun onLost(network: Network) {
                        val current = _syncState.value
                        if (current.pendingCount > 0 && current.status != SyncStatusEnum.SETUP_REQUIRED) {
                            _syncState.value = current.copy(status = SyncStatusEnum.OFFLINE_QUEUED)
                        }
                    }
                })
            }
        } catch (_: Exception) {
            // Safe fallback if network callback registration fails on specific restrictions
        }
    }

    fun isNetworkAvailable(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun checkFirebaseSetup(): FirebaseSetupReport {
        val missing = mutableListOf<String>()
        var hasGoogleServices = false
        var isInitialized = false
        var isFirestoreReady = false
        var isAuthReady = false
        var userUid: String? = null
        var userEmail: String? = null

        // 1. Check Google Services config resources (from google-services.json)
        val resId = context.resources.getIdentifier("google_app_id", "string", context.packageName)
        if (resId != 0) {
            val appId = runCatching { context.getString(resId) }.getOrNull()
            if (!appId.isNullOrBlank()) {
                hasGoogleServices = true
            }
        }
        if (!hasGoogleServices) {
            missing.add("google-services.json not found in project (google_app_id missing)")
        }

        // 2. Check FirebaseApp initialization
        try {
            val apps = FirebaseApp.getApps(context)
            if (apps.isNotEmpty()) {
                isInitialized = true
            } else {
                val app = FirebaseApp.initializeApp(context)
                if (app != null) {
                    isInitialized = true
                } else {
                    missing.add("FirebaseApp could not be initialized")
                }
            }
        } catch (e: Exception) {
            missing.add("Firebase initialization: ${e.localizedMessage ?: "Not initialized"}")
        }

        // 3. Check Firebase Auth
        if (isInitialized) {
            try {
                val auth = FirebaseAuth.getInstance()
                isAuthReady = true
                val user = auth.currentUser
                if (user != null) {
                    userUid = user.uid
                    userEmail = user.email
                } else {
                    missing.add("Google Sign-In required (no active user session)")
                }
            } catch (e: Exception) {
                missing.add("Firebase Auth not available: ${e.localizedMessage}")
            }

            // 4. Check Firestore
            try {
                FirebaseFirestore.getInstance()
                isFirestoreReady = true
            } catch (e: Exception) {
                missing.add("Firestore client not available: ${e.localizedMessage}")
            }
        } else {
            missing.add("Firestore & Auth require Firebase backend provisioning")
        }

        val summary = if (missing.isEmpty()) {
            "Firebase is configured and authenticated. Online cloud backup is operational."
        } else {
            buildString {
                append("Firebase Setup Incomplete:\n")
                missing.forEach { append("• $it\n") }
                append("\nLocal storage is 100% active and offline-ready. All transactions, customers, suppliers, and stock records are securely preserved in the on-device SQLite/Room database.")
            }
        }

        return FirebaseSetupReport(
            hasGoogleServicesConfig = hasGoogleServices,
            isFirebaseInitialized = isInitialized,
            isFirestoreAvailable = isFirestoreReady,
            isAuthAvailable = isAuthReady,
            currentUserUid = userUid,
            currentUserEmail = userEmail,
            missingRequirements = missing,
            detailedSummary = summary
        )
    }

    suspend fun triggerSync(isManual: Boolean = true): SyncResult = withContext(Dispatchers.IO) {
        val report = checkFirebaseSetup()

        if (!report.isFirebaseInitialized || !report.hasGoogleServicesConfig) {
            _syncState.value = _syncState.value.copy(
                status = SyncStatusEnum.SETUP_REQUIRED,
                report = report,
                errorMessage = "Firebase setup incomplete. Missing google-services.json."
            )
            return@withContext SyncResult.IncompleteSetup(report.detailedSummary)
        }

        if (!report.isAuthAvailable || report.currentUserUid == null) {
            _syncState.value = _syncState.value.copy(
                status = SyncStatusEnum.AUTH_REQUIRED,
                report = report,
                errorMessage = "Google Sign-In required to isolate and secure business cloud data."
            )
            return@withContext SyncResult.AuthRequired
        }

        if (!isNetworkAvailable()) {
            _syncState.value = _syncState.value.copy(
                status = SyncStatusEnum.OFFLINE_QUEUED,
                report = report,
                errorMessage = "No internet connection. Changes queued locally."
            )
            return@withContext SyncResult.Offline
        }

        _syncState.value = _syncState.value.copy(
            status = SyncStatusEnum.SYNCING,
            report = report,
            errorMessage = null
        )

        try {
            val pendingItems = dao.getPendingSyncItemsList()
            if (pendingItems.isEmpty()) {
                val now = System.currentTimeMillis()
                saveLastSync(now)
                _syncState.value = _syncState.value.copy(
                    status = SyncStatusEnum.SUCCESS,
                    lastSyncTimestamp = now,
                    errorMessage = null
                )
                return@withContext SyncResult.Success(syncedCount = 0)
            }

            val uid = report.currentUserUid
            val firestore = FirebaseFirestore.getInstance()
            val successfulIds = mutableListOf<Long>()

            for (item in pendingItems) {
                try {
                    val docRef = when (item.entityType) {
                        "PROFILE" -> firestore.collection("users").document(uid)
                            .collection("businesses").document(item.cloudId)
                        "TRANSACTION" -> firestore.collection("users").document(uid)
                            .collection("businesses").document("${item.businessId}")
                            .collection("transactions").document(item.cloudId)
                        "CUSTOMER" -> firestore.collection("users").document(uid)
                            .collection("businesses").document("${item.businessId}")
                            .collection("customers").document(item.cloudId)
                        "SUPPLIER" -> firestore.collection("users").document(uid)
                            .collection("businesses").document("${item.businessId}")
                            .collection("suppliers").document(item.cloudId)
                        "PRODUCT" -> firestore.collection("users").document(uid)
                            .collection("businesses").document("${item.businessId}")
                            .collection("products").document(item.cloudId)
                        else -> null
                    }

                    if (docRef != null) {
                        if (item.action == "DELETE") {
                            docRef.delete().await()
                        } else {
                            val map = jsonToMap(item.payloadJson)
                            map["updatedAt"] = System.currentTimeMillis()
                            // SetOptions.merge() prevents duplicates and ensures idempotency on retries
                            docRef.set(map, SetOptions.merge()).await()
                        }
                        successfulIds.add(item.id)
                    }
                } catch (itemErr: Exception) {
                    dao.markSyncFailed(item.id, "FAILED", itemErr.localizedMessage ?: "Unknown error")
                }
            }

            if (successfulIds.isNotEmpty()) {
                dao.deleteSyncItems(successfulIds)
            }

            val remainingCount = dao.getPendingSyncItemsList().size
            val now = System.currentTimeMillis()
            saveLastSync(now)

            _syncState.value = _syncState.value.copy(
                status = if (remainingCount == 0) SyncStatusEnum.SUCCESS else SyncStatusEnum.ERROR,
                lastSyncTimestamp = now,
                pendingCount = remainingCount,
                errorMessage = if (remainingCount > 0) "$remainingCount items failed to sync" else null
            )

            SyncResult.Success(syncedCount = successfulIds.size)
        } catch (e: Exception) {
            _syncState.value = _syncState.value.copy(
                status = SyncStatusEnum.ERROR,
                errorMessage = e.localizedMessage ?: "Sync error occurred"
            )
            SyncResult.Error(e.localizedMessage ?: "Unknown sync failure")
        }
    }

    private fun saveLastSync(timestamp: Long) {
        prefs.edit().putLong(KEY_LAST_SYNC, timestamp).apply()
    }

    private fun jsonToMap(jsonString: String): MutableMap<String, Any> {
        val map = mutableMapOf<String, Any>()
        if (jsonString.isBlank()) return map
        return try {
            val json = JSONObject(jsonString)
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val value = json.get(key)
                if (value != JSONObject.NULL) {
                    map[key] = value
                }
            }
            map
        } catch (_: Exception) {
            map
        }
    }

    companion object {
        private const val KEY_LAST_SYNC = "last_sync_timestamp"
    }
}

sealed interface SyncResult {
    data class Success(val syncedCount: Int) : SyncResult
    data class IncompleteSetup(val diagnosticMessage: String) : SyncResult
    data object AuthRequired : SyncResult
    data object Offline : SyncResult
    data class Error(val message: String) : SyncResult
}
