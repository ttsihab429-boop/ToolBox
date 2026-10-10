package com.example.data.business.sync

enum class SyncStatusEnum {
    IDLE,
    SYNCING,
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
    val report: FirebaseSetupReport = FirebaseSetupReport()
)
