package com.example.data.business.sync

enum class SyncStatusEnum {
    IDLE,
    SYNCING,
    RESTORING,
    SUCCESS,
    OFFLINE_QUEUED,
    SETUP_REQUIRED,
    AUTH_REQUIRED,
    ERROR
}

data class FirebaseSetupReport(
    val hasGoogleServicesConfig: Boolean = false,
    val isFirebaseInitialized: Boolean = false,
    val isFirestoreAvailable: Boolean = false,
    val isAuthAvailable: Boolean = false,
    val currentUserUid: String? = null,
    val currentUserEmail: String? = null,
    val missingRequirements: List<String> = emptyList(),
    val detailedSummary: String = ""
)

data class BusinessSyncState(
    val status: SyncStatusEnum = SyncStatusEnum.IDLE,
    val lastSyncTimestamp: Long = 0L,
    val pendingCount: Int = 0,
    val errorMessage: String? = null,
    val report: FirebaseSetupReport = FirebaseSetupReport(),
    val isRestoring: Boolean = false,
    val restoreSummary: String? = null,
    val authOperationInProgress: Boolean = false,
    val lastActionMessage: String? = null
)

sealed interface SyncResult {
    data class Success(val syncedCount: Int) : SyncResult
    data class IncompleteSetup(val diagnosticMessage: String) : SyncResult
    data object AuthRequired : SyncResult
    data object Offline : SyncResult
    data class Error(val message: String) : SyncResult
}

sealed interface RestoreResult {
    data class Success(
        val businessesRestored: Int,
        val transactionsRestored: Int,
        val customersRestored: Int,
        val suppliersRestored: Int,
        val productsRestored: Int
    ) : RestoreResult
    data class NoDataFound(val message: String) : RestoreResult
    data class IncompleteSetup(val diagnosticMessage: String) : RestoreResult
    data object AuthRequired : RestoreResult
    data object Offline : RestoreResult
    data class Error(val message: String) : RestoreResult
}

sealed interface AuthOperationResult {
    data class Success(val email: String?) : AuthOperationResult
    data object Cancelled : AuthOperationResult
    data class IncompleteSetup(val message: String) : AuthOperationResult
    data class Error(val message: String) : AuthOperationResult
}

