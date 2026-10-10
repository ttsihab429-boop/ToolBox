package com.example.data.business

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessDao {
    // --- Profiles ---
    @Query("SELECT * FROM business_profiles ORDER BY id ASC")
    fun getAllBusinesses(): Flow<List<BusinessProfile>>

    @Query("SELECT * FROM business_profiles WHERE id = :id LIMIT 1")
    suspend fun getBusinessById(id: Long): BusinessProfile?

    @Query("SELECT COUNT(*) FROM business_profiles")
    suspend fun getBusinessCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusiness(business: BusinessProfile): Long

    @Update
    suspend fun updateBusiness(business: BusinessProfile)

    @Query("DELETE FROM business_profiles WHERE id = :id")
    suspend fun deleteBusiness(id: Long)

    // --- Transactions ---
    @Query("SELECT * FROM business_transactions WHERE businessId = :businessId ORDER BY timestamp DESC")
    fun getTransactions(businessId: Long): Flow<List<BusinessTransaction>>

    @Query("SELECT * FROM business_transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): BusinessTransaction?

    @Query("SELECT * FROM business_transactions WHERE businessId = :businessId AND type = :type ORDER BY timestamp DESC")
    fun getTransactionsByType(businessId: Long, type: String): Flow<List<BusinessTransaction>>

    @Query("SELECT * FROM business_transactions WHERE businessId = :businessId AND customerId = :customerId ORDER BY timestamp DESC")
    fun getTransactionsByCustomer(businessId: Long, customerId: Long): Flow<List<BusinessTransaction>>

    @Query("SELECT * FROM business_transactions WHERE businessId = :businessId AND supplierId = :supplierId ORDER BY timestamp DESC")
    fun getTransactionsBySupplier(businessId: Long, supplierId: Long): Flow<List<BusinessTransaction>>

    @Query("SELECT * FROM business_transactions WHERE businessId = :businessId AND productId = :productId ORDER BY timestamp DESC")
    fun getTransactionsByProduct(businessId: Long, productId: Long): Flow<List<BusinessTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: BusinessTransaction): Long

    @Update
    suspend fun updateTransaction(transaction: BusinessTransaction)

    @Delete
    suspend fun deleteTransaction(transaction: BusinessTransaction)

    @Query("DELETE FROM business_transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("DELETE FROM business_transactions WHERE businessId = :businessId")
    suspend fun deleteAllTransactionsForBusiness(businessId: Long)

    // --- Customers ---
    @Query("SELECT * FROM business_customers WHERE businessId = :businessId ORDER BY name ASC")
    fun getAllCustomers(businessId: Long): Flow<List<Customer>>

    @Query("SELECT * FROM business_customers WHERE id = :id LIMIT 1")
    suspend fun getCustomerById(id: Long): Customer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    @Query("DELETE FROM business_customers WHERE id = :id")
    suspend fun deleteCustomerById(id: Long)

    @Query("DELETE FROM business_customers WHERE businessId = :businessId")
    suspend fun deleteAllCustomersForBusiness(businessId: Long)

    // --- Suppliers ---
    @Query("SELECT * FROM business_suppliers WHERE businessId = :businessId ORDER BY name ASC")
    fun getAllSuppliers(businessId: Long): Flow<List<Supplier>>

    @Query("SELECT * FROM business_suppliers WHERE id = :id LIMIT 1")
    suspend fun getSupplierById(id: Long): Supplier?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: Supplier): Long

    @Update
    suspend fun updateSupplier(supplier: Supplier)

    @Delete
    suspend fun deleteSupplier(supplier: Supplier)

    @Query("DELETE FROM business_suppliers WHERE id = :id")
    suspend fun deleteSupplierById(id: Long)

    @Query("DELETE FROM business_suppliers WHERE businessId = :businessId")
    suspend fun deleteAllSuppliersForBusiness(businessId: Long)

    // --- Products (Inventory) ---
    @Query("SELECT * FROM business_products WHERE businessId = :businessId ORDER BY name ASC")
    fun getAllProducts(businessId: Long): Flow<List<ProductItem>>

    @Query("SELECT * FROM business_products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): ProductItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductItem): Long

    @Update
    suspend fun updateProduct(product: ProductItem)

    @Delete
    suspend fun deleteProduct(product: ProductItem)

    @Query("DELETE FROM business_products WHERE id = :id")
    suspend fun deleteProductById(id: Long)

    @Query("UPDATE business_products SET currentQuantity = :newQuantity WHERE id = :id")
    suspend fun updateProductQuantity(id: Long, newQuantity: Double)

    @Query("DELETE FROM business_products WHERE businessId = :businessId")
    suspend fun deleteAllProductsForBusiness(businessId: Long)

    // --- Sync Queue ---
    @Query("SELECT * FROM business_sync_queue ORDER BY timestamp ASC")
    fun getAllSyncItems(): Flow<List<SyncQueueItem>>

    @Query("SELECT COUNT(*) FROM business_sync_queue WHERE status = 'PENDING'")
    fun getPendingSyncCount(): Flow<Int>

    @Query("SELECT * FROM business_sync_queue WHERE status = 'PENDING' ORDER BY timestamp ASC")
    suspend fun getPendingSyncItemsList(): List<SyncQueueItem>

    @Query("SELECT * FROM business_sync_queue WHERE entityType = :entityType AND entityId = :entityId LIMIT 1")
    suspend fun getSyncItemForEntity(entityType: String, entityId: Long): SyncQueueItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncItem(item: SyncQueueItem): Long

    @Update
    suspend fun updateSyncItem(item: SyncQueueItem)

    @Query("DELETE FROM business_sync_queue WHERE id = :id")
    suspend fun deleteSyncItem(id: Long)

    @Query("DELETE FROM business_sync_queue WHERE id IN (:ids)")
    suspend fun deleteSyncItems(ids: List<Long>)

    @Query("DELETE FROM business_sync_queue WHERE businessId = :businessId")
    suspend fun clearQueueForBusiness(businessId: Long)

    @Query("UPDATE business_sync_queue SET status = :status, lastError = :error, retryCount = retryCount + 1 WHERE id = :id")
    suspend fun markSyncFailed(id: Long, status: String, error: String)
}
