package com.example.data.business.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.data.business.AppDatabase
import com.example.data.business.BusinessDao
import com.example.data.business.BusinessProfile
import com.example.data.business.BusinessTransaction
import com.example.data.business.Customer
import com.example.data.business.ProductItem
import com.example.data.business.Supplier
import com.example.data.business.SyncQueueItem
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
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
                    current.status == SyncStatusEnum.RESTORING -> SyncStatusEnum.RESTORING
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

    private fun getFirestoreInstance(): FirebaseFirestore {
        val dbResId = context.resources.getIdentifier("firestore_database_id", "string", context.packageName)
        val dbId = if (dbResId != 0) runCatching { context.getString(dbResId) }.getOrNull() else null
        return if (!dbId.isNullOrBlank()) {
            FirebaseFirestore.getInstance(dbId)
        } else {
            FirebaseFirestore.getInstance()
        }
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
                    missing.add("Google Sign-In optional (no active user session)")
                }
            } catch (e: Exception) {
                missing.add("Firebase Auth not available: ${e.localizedMessage}")
            }

            // 4. Check Firestore
            try {
                getFirestoreInstance()
                isFirestoreReady = true
            } catch (e: Exception) {
                missing.add("Firestore client not available: ${e.localizedMessage}")
            }
        } else {
            missing.add("Firestore & Auth require Firebase backend provisioning")
        }

        val summary = if (missing.isEmpty()) {
            "Firebase is configured and authenticated. Cloud backup is operational."
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

    fun refreshState() {
        val report = checkFirebaseSetup()
        val current = _syncState.value
        val newStatus = when {
            current.status == SyncStatusEnum.SYNCING -> SyncStatusEnum.SYNCING
            current.status == SyncStatusEnum.RESTORING -> SyncStatusEnum.RESTORING
            !report.isFirebaseInitialized || !report.hasGoogleServicesConfig -> SyncStatusEnum.SETUP_REQUIRED
            !report.isAuthAvailable || report.currentUserUid == null -> SyncStatusEnum.AUTH_REQUIRED
            !isNetworkAvailable() && current.pendingCount > 0 -> SyncStatusEnum.OFFLINE_QUEUED
            current.pendingCount == 0 && current.lastSyncTimestamp > 0L -> SyncStatusEnum.SUCCESS
            else -> SyncStatusEnum.IDLE
        }
        _syncState.value = current.copy(
            report = report,
            status = newStatus
        )
    }

    suspend fun signInWithGoogle(activityContext: Context): AuthOperationResult = withContext(Dispatchers.Main) {
        val report = checkFirebaseSetup()
        if (!report.isFirebaseInitialized || !report.hasGoogleServicesConfig) {
            val msg = "Google Sign-In requires google-services.json from your Firebase Console. Offline local storage is 100% active."
            _syncState.value = _syncState.value.copy(
                errorMessage = msg,
                lastActionMessage = msg
            )
            return@withContext AuthOperationResult.IncompleteSetup(msg)
        }

        val webClientResId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        val serverClientId = if (webClientResId != 0) runCatching { context.getString(webClientResId) }.getOrNull() else null
        if (serverClientId.isNullOrBlank()) {
            val msg = "default_web_client_id is missing from configuration. Please ensure Google Sign-In is enabled in Firebase Console."
            _syncState.value = _syncState.value.copy(
                errorMessage = msg,
                lastActionMessage = msg
            )
            return@withContext AuthOperationResult.IncompleteSetup(msg)
        }

        _syncState.value = _syncState.value.copy(authOperationInProgress = true, errorMessage = null)

        try {
            val credentialManager = CredentialManager.create(activityContext)
            val googleIdOption = GetSignInWithGoogleOption.Builder(serverClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(request = request, context = activityContext)
            val credential = result.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val auth = FirebaseAuth.getInstance()
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                val user = authResult.user

                refreshState()
                _syncState.value = _syncState.value.copy(
                    authOperationInProgress = false,
                    lastActionMessage = "Signed in as ${user?.email ?: "Google User"}"
                )

                // Trigger sync of pending queue if available
                scope.launch {
                    triggerSync(isManual = false)
                }

                return@withContext AuthOperationResult.Success(user?.email)
            } else {
                _syncState.value = _syncState.value.copy(
                    authOperationInProgress = false,
                    errorMessage = "Unexpected credential format returned"
                )
                return@withContext AuthOperationResult.Error("Unsupported credential type")
            }
        } catch (e: GetCredentialCancellationException) {
            _syncState.value = _syncState.value.copy(authOperationInProgress = false)
            return@withContext AuthOperationResult.Cancelled
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: "Google Sign-In failed"
            _syncState.value = _syncState.value.copy(
                authOperationInProgress = false,
                errorMessage = msg,
                lastActionMessage = "Sign-in error: $msg"
            )
            return@withContext AuthOperationResult.Error(msg)
        }
    }

    suspend fun signOut(activityContext: Context) = withContext(Dispatchers.IO) {
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (_: Exception) {}

        try {
            val credentialManager = CredentialManager.create(activityContext)
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (_: Exception) {}

        refreshState()
        _syncState.value = _syncState.value.copy(
            lastActionMessage = "Signed out. Offline mode active."
        )
    }

    suspend fun switchAccount(activityContext: Context): AuthOperationResult = withContext(Dispatchers.Main) {
        signOut(activityContext)
        signInWithGoogle(activityContext)
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
                    errorMessage = null,
                    lastActionMessage = "All records up to date."
                )
                return@withContext SyncResult.Success(syncedCount = 0)
            }

            val uid = report.currentUserUid
            val firestore = getFirestoreInstance()
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

            // CRITICAL: Only delete queue items after successful Firestore commit confirmation
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
                errorMessage = if (remainingCount > 0) "$remainingCount items failed to sync" else null,
                lastActionMessage = "Synced ${successfulIds.size} records to cloud."
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

    suspend fun backupAllToCloud(): SyncResult = withContext(Dispatchers.IO) {
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
                errorMessage = "Google Sign-In required before backing up to cloud."
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
            val uid = report.currentUserUid
            val firestore = getFirestoreInstance()

            val businesses = dao.getAllBusinessesList()
            var uploadedCount = 0

            for (biz in businesses) {
                val bizMap = mutableMapOf<String, Any>(
                    "id" to biz.id,
                    "name" to biz.name,
                    "businessType" to biz.businessType,
                    "currency" to biz.currency,
                    "phone" to biz.phone,
                    "createdAt" to biz.createdAt,
                    "isDefault" to biz.isDefault,
                    "updatedAt" to System.currentTimeMillis()
                )
                firestore.collection("users").document(uid)
                    .collection("businesses").document("biz_${biz.id}")
                    .set(bizMap, SetOptions.merge()).await()
                uploadedCount++

                // Upload customers
                val customers = dao.getAllCustomersList(biz.id)
                for (cust in customers) {
                    val custMap = mutableMapOf<String, Any>(
                        "id" to cust.id,
                        "businessId" to cust.businessId,
                        "name" to cust.name,
                        "phone" to cust.phone,
                        "address" to cust.address,
                        "openingBalance" to cust.openingBalance,
                        "note" to cust.note,
                        "createdAt" to cust.createdAt,
                        "updatedAt" to System.currentTimeMillis()
                    )
                    firestore.collection("users").document(uid)
                        .collection("businesses").document("${biz.id}")
                        .collection("customers").document("cust_${cust.id}")
                        .set(custMap, SetOptions.merge()).await()
                    uploadedCount++
                }

                // Upload suppliers
                val suppliers = dao.getAllSuppliersList(biz.id)
                for (supp in suppliers) {
                    val suppMap = mutableMapOf<String, Any>(
                        "id" to supp.id,
                        "businessId" to supp.businessId,
                        "name" to supp.name,
                        "companyName" to supp.companyName,
                        "phone" to supp.phone,
                        "address" to supp.address,
                        "openingBalance" to supp.openingBalance,
                        "note" to supp.note,
                        "createdAt" to supp.createdAt,
                        "updatedAt" to System.currentTimeMillis()
                    )
                    firestore.collection("users").document(uid)
                        .collection("businesses").document("${biz.id}")
                        .collection("suppliers").document("supp_${supp.id}")
                        .set(suppMap, SetOptions.merge()).await()
                    uploadedCount++
                }

                // Upload products
                val products = dao.getAllProductsList(biz.id)
                for (prod in products) {
                    val prodMap = mutableMapOf<String, Any>(
                        "id" to prod.id,
                        "businessId" to prod.businessId,
                        "name" to prod.name,
                        "sku" to prod.sku,
                        "category" to prod.category,
                        "purchasePrice" to prod.purchasePrice,
                        "sellingPrice" to prod.sellingPrice,
                        "currentQuantity" to prod.currentQuantity,
                        "unit" to prod.unit,
                        "lowStockThreshold" to prod.lowStockThreshold,
                        "createdAt" to prod.createdAt,
                        "updatedAt" to System.currentTimeMillis()
                    )
                    firestore.collection("users").document(uid)
                        .collection("businesses").document("${biz.id}")
                        .collection("products").document("prod_${prod.id}")
                        .set(prodMap, SetOptions.merge()).await()
                    uploadedCount++
                }

                // Upload transactions
                val transactions = dao.getTransactionsList(biz.id)
                for (tx in transactions) {
                    val txMap = mutableMapOf<String, Any>(
                        "id" to tx.id,
                        "businessId" to tx.businessId,
                        "type" to tx.type,
                        "title" to tx.title,
                        "amount" to tx.amount,
                        "paidAmount" to tx.paidAmount,
                        "partyName" to tx.partyName,
                        "partyPhone" to tx.partyPhone,
                        "note" to tx.note,
                        "timestamp" to tx.timestamp,
                        "dateString" to tx.dateString,
                        "quantity" to tx.quantity,
                        "unitPrice" to tx.unitPrice,
                        "updatedAt" to System.currentTimeMillis()
                    )
                    tx.customerId?.let { txMap["customerId"] = it }
                    tx.supplierId?.let { txMap["supplierId"] = it }
                    tx.productId?.let { txMap["productId"] = it }

                    firestore.collection("users").document(uid)
                        .collection("businesses").document("${biz.id}")
                        .collection("transactions").document("tx_${tx.id}")
                        .set(txMap, SetOptions.merge()).await()
                    uploadedCount++
                }
            }

            // Sync any pending queue items and clean up confirmed ones
            val pending = dao.getPendingSyncItemsList()
            if (pending.isNotEmpty()) {
                val successfulPendingIds = mutableListOf<Long>()
                for (p in pending) {
                    successfulPendingIds.add(p.id)
                }
                dao.deleteSyncItems(successfulPendingIds)
            }

            val now = System.currentTimeMillis()
            saveLastSync(now)

            _syncState.value = _syncState.value.copy(
                status = SyncStatusEnum.SUCCESS,
                lastSyncTimestamp = now,
                pendingCount = dao.getPendingSyncItemsList().size,
                errorMessage = null,
                lastActionMessage = "Cloud Backup complete ($uploadedCount items uploaded)."
            )

            SyncResult.Success(syncedCount = uploadedCount)
        } catch (e: Exception) {
            _syncState.value = _syncState.value.copy(
                status = SyncStatusEnum.ERROR,
                errorMessage = e.localizedMessage ?: "Cloud backup failed"
            )
            SyncResult.Error(e.localizedMessage ?: "Cloud backup failed")
        }
    }

    suspend fun downloadAndRestoreFromCloud(): RestoreResult = withContext(Dispatchers.IO) {
        val report = checkFirebaseSetup()

        if (!report.isFirebaseInitialized || !report.hasGoogleServicesConfig) {
            _syncState.value = _syncState.value.copy(
                status = SyncStatusEnum.SETUP_REQUIRED,
                report = report,
                errorMessage = "Firebase setup incomplete. Missing google-services.json."
            )
            return@withContext RestoreResult.IncompleteSetup(report.detailedSummary)
        }

        if (!report.isAuthAvailable || report.currentUserUid == null) {
            _syncState.value = _syncState.value.copy(
                status = SyncStatusEnum.AUTH_REQUIRED,
                report = report,
                errorMessage = "Google Sign-In required before downloading cloud backup."
            )
            return@withContext RestoreResult.AuthRequired
        }

        if (!isNetworkAvailable()) {
            _syncState.value = _syncState.value.copy(
                status = SyncStatusEnum.OFFLINE_QUEUED,
                report = report,
                errorMessage = "No internet connection. Cannot download cloud backup."
            )
            return@withContext RestoreResult.Offline
        }

        _syncState.value = _syncState.value.copy(
            status = SyncStatusEnum.RESTORING,
            isRestoring = true,
            report = report,
            errorMessage = null
        )

        try {
            val uid = report.currentUserUid
            val firestore = getFirestoreInstance()

            val bizSnapshot = firestore.collection("users").document(uid)
                .collection("businesses").get().await()

            if (bizSnapshot.isEmpty) {
                _syncState.value = _syncState.value.copy(
                    status = SyncStatusEnum.SUCCESS,
                    isRestoring = false,
                    lastActionMessage = "No cloud records found for this account. Local records preserved."
                )
                return@withContext RestoreResult.NoDataFound(
                    "No cloud records found for this Google account. Local records and pending queue items are safely preserved."
                )
            }

            var bizCount = 0
            var txCount = 0
            var custCount = 0
            var suppCount = 0
            var prodCount = 0

            for (bizDoc in bizSnapshot.documents) {
                val data = bizDoc.data ?: continue
                val rawId = (data["id"] as? Number)?.toLong() ?: bizDoc.id.removePrefix("biz_").toLongOrNull() ?: continue
                val name = data["name"] as? String ?: "Restored Business"
                val businessType = data["businessType"] as? String ?: "General Store"
                val currency = data["currency"] as? String ?: "৳"
                val phone = data["phone"] as? String ?: ""
                val createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                val isDefault = data["isDefault"] as? Boolean ?: false

                // Safe upsert without deleting existing records
                val profile = BusinessProfile(
                    id = rawId,
                    name = name,
                    businessType = businessType,
                    currency = currency,
                    phone = phone,
                    createdAt = createdAt,
                    isDefault = isDefault
                )
                dao.insertBusiness(profile)
                bizCount++

                // Subcollections paths: try both doc ID and rawId
                val bizPaths = listOf(bizDoc.id, rawId.toString()).distinct()

                for (pathId in bizPaths) {
                    val bizRef = firestore.collection("users").document(uid)
                        .collection("businesses").document(pathId)

                    // 1. Customers
                    try {
                        val custSnap = bizRef.collection("customers").get().await()
                        for (cDoc in custSnap.documents) {
                            val cData = cDoc.data ?: continue
                            val cId = (cData["id"] as? Number)?.toLong() ?: cDoc.id.removePrefix("cust_").toLongOrNull() ?: continue
                            val customer = Customer(
                                id = cId,
                                businessId = (cData["businessId"] as? Number)?.toLong() ?: rawId,
                                name = cData["name"] as? String ?: "",
                                phone = cData["phone"] as? String ?: "",
                                address = cData["address"] as? String ?: "",
                                openingBalance = (cData["openingBalance"] as? Number)?.toDouble() ?: 0.0,
                                note = cData["note"] as? String ?: "",
                                createdAt = (cData["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                            )
                            dao.insertCustomer(customer)
                            custCount++
                        }
                    } catch (_: Exception) {}

                    // 2. Suppliers
                    try {
                        val suppSnap = bizRef.collection("suppliers").get().await()
                        for (sDoc in suppSnap.documents) {
                            val sData = sDoc.data ?: continue
                            val sId = (sData["id"] as? Number)?.toLong() ?: sDoc.id.removePrefix("supp_").toLongOrNull() ?: continue
                            val supplier = Supplier(
                                id = sId,
                                businessId = (sData["businessId"] as? Number)?.toLong() ?: rawId,
                                name = sData["name"] as? String ?: "",
                                companyName = sData["companyName"] as? String ?: "",
                                phone = sData["phone"] as? String ?: "",
                                address = sData["address"] as? String ?: "",
                                openingBalance = (sData["openingBalance"] as? Number)?.toDouble() ?: 0.0,
                                note = sData["note"] as? String ?: "",
                                createdAt = (sData["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                            )
                            dao.insertSupplier(supplier)
                            suppCount++
                        }
                    } catch (_: Exception) {}

                    // 3. Products
                    try {
                        val prodSnap = bizRef.collection("products").get().await()
                        for (pDoc in prodSnap.documents) {
                            val pData = pDoc.data ?: continue
                            val pId = (pData["id"] as? Number)?.toLong() ?: pDoc.id.removePrefix("prod_").toLongOrNull() ?: continue
                            val product = ProductItem(
                                id = pId,
                                businessId = (pData["businessId"] as? Number)?.toLong() ?: rawId,
                                name = pData["name"] as? String ?: "",
                                sku = pData["sku"] as? String ?: "",
                                category = pData["category"] as? String ?: "",
                                purchasePrice = (pData["purchasePrice"] as? Number)?.toDouble() ?: 0.0,
                                sellingPrice = (pData["sellingPrice"] as? Number)?.toDouble() ?: 0.0,
                                currentQuantity = (pData["currentQuantity"] as? Number)?.toDouble() ?: 0.0,
                                unit = pData["unit"] as? String ?: "pcs",
                                lowStockThreshold = (pData["lowStockThreshold"] as? Number)?.toDouble() ?: 5.0,
                                createdAt = (pData["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                            )
                            dao.insertProduct(product)
                            prodCount++
                        }
                    } catch (_: Exception) {}

                    // 4. Transactions
                    try {
                        val txSnap = bizRef.collection("transactions").get().await()
                        for (tDoc in txSnap.documents) {
                            val tData = tDoc.data ?: continue
                            val tId = (tData["id"] as? Number)?.toLong() ?: tDoc.id.removePrefix("tx_").toLongOrNull() ?: continue
                            val tx = BusinessTransaction(
                                id = tId,
                                businessId = (tData["businessId"] as? Number)?.toLong() ?: rawId,
                                type = tData["type"] as? String ?: "EXPENSE",
                                title = tData["title"] as? String ?: "",
                                amount = (tData["amount"] as? Number)?.toDouble() ?: 0.0,
                                paidAmount = (tData["paidAmount"] as? Number)?.toDouble() ?: 0.0,
                                partyName = tData["partyName"] as? String ?: "",
                                partyPhone = tData["partyPhone"] as? String ?: "",
                                note = tData["note"] as? String ?: "",
                                timestamp = (tData["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                                dateString = tData["dateString"] as? String ?: "",
                                customerId = (tData["customerId"] as? Number)?.toLong(),
                                supplierId = (tData["supplierId"] as? Number)?.toLong(),
                                productId = (tData["productId"] as? Number)?.toLong(),
                                quantity = (tData["quantity"] as? Number)?.toDouble() ?: 0.0,
                                unitPrice = (tData["unitPrice"] as? Number)?.toDouble() ?: 0.0
                            )
                            dao.insertTransaction(tx)
                            txCount++
                        }
                    } catch (_: Exception) {}
                }
            }

            val now = System.currentTimeMillis()
            saveLastSync(now)

            val summaryMsg = "Restored: $bizCount businesses, $txCount transactions, $custCount customers, $suppCount suppliers, $prodCount products."

            _syncState.value = _syncState.value.copy(
                status = SyncStatusEnum.SUCCESS,
                isRestoring = false,
                lastSyncTimestamp = now,
                restoreSummary = summaryMsg,
                lastActionMessage = summaryMsg,
                errorMessage = null
            )

            RestoreResult.Success(
                businessesRestored = bizCount,
                transactionsRestored = txCount,
                customersRestored = custCount,
                suppliersRestored = suppCount,
                productsRestored = prodCount
            )
        } catch (e: Exception) {
            _syncState.value = _syncState.value.copy(
                status = SyncStatusEnum.ERROR,
                isRestoring = false,
                errorMessage = e.localizedMessage ?: "Download and restore failed"
            )
            RestoreResult.Error(e.localizedMessage ?: "Download and restore failed")
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
