package com.example.data.business

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "business_profiles")
data class BusinessProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val businessType: String = "General Store",
    val currency: String = "৳",
    val phone: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isDefault: Boolean = false
)

@Entity(
    tableName = "business_transactions",
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["type"]),
        Index(value = ["customerId"]),
        Index(value = ["supplierId"]),
        Index(value = ["productId"])
    ]
)
data class BusinessTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long,
    val type: String, // "SALE", "PURCHASE", "EXPENSE", "CUSTOMER_PAYMENT", "SUPPLIER_PAYMENT"
    val title: String,
    val amount: Double,
    val paidAmount: Double,
    val partyName: String = "",
    val partyPhone: String = "",
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String, // YYYY-MM-DD
    val customerId: Long? = null,
    val supplierId: Long? = null,
    val productId: Long? = null,
    val quantity: Double = 0.0,
    val unitPrice: Double = 0.0
) {
    val dueAmount: Double
        get() = when (type) {
            "SALE", "PURCHASE" -> (amount - paidAmount).coerceAtLeast(0.0)
            else -> 0.0
        }
}

@Entity(
    tableName = "business_customers",
    indices = [Index(value = ["businessId"])]
)
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long,
    val name: String,
    val phone: String = "",
    val address: String = "",
    val openingBalance: Double = 0.0, // Initial due amount
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "business_suppliers",
    indices = [Index(value = ["businessId"])]
)
data class Supplier(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long,
    val name: String,
    val companyName: String = "",
    val phone: String = "",
    val address: String = "",
    val openingBalance: Double = 0.0, // Initial payable amount
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "business_products",
    indices = [Index(value = ["businessId"])]
)
data class ProductItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long,
    val name: String,
    val sku: String = "",
    val category: String = "",
    val purchasePrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val currentQuantity: Double = 0.0,
    val unit: String = "pcs",
    val lowStockThreshold: Double = 5.0,
    val createdAt: Long = System.currentTimeMillis()
) {
    val isLowStock: Boolean
        get() = currentQuantity <= lowStockThreshold

    val isOutOfStock: Boolean
        get() = currentQuantity <= 0.0
}

@Entity(
    tableName = "business_sync_queue",
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["status"]),
        Index(value = ["entityType", "entityId"])
    ]
)
data class SyncQueueItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long,
    val entityType: String, // "PROFILE", "TRANSACTION", "CUSTOMER", "SUPPLIER", "PRODUCT"
    val entityId: Long,
    val cloudId: String, // Deterministic ID to ensure idempotent upsert in Firestore
    val action: String, // "UPSERT", "DELETE"
    val payloadJson: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val retryCount: Int = 0,
    val lastError: String? = null,
    val status: String = "PENDING" // "PENDING", "SYNCING", "FAILED"
)

