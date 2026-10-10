package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.business.AppDatabase
import com.example.data.business.BusinessRepository
import com.example.data.business.sync.BusinessSyncManager
import com.example.data.business.sync.SyncResult
import com.example.data.business.sync.SyncStatusEnum
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BusinessManagerSyncTest {

    private lateinit var context: Context
    private lateinit var repository: BusinessRepository
    private lateinit var syncManager: BusinessSyncManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        repository = BusinessRepository(context)
        syncManager = repository.syncManager
    }

    @Test
    fun testOfflineSyncQueueOnCustomerAndTransaction() = runBlocking {
        val businesses = repository.businesses.first()
        val bizId = businesses.first().id

        val dao = AppDatabase.getDatabase(context).businessDao()

        // 1. Add Customer - Verify offline save and sync queue item creation
        val custId = repository.saveCustomer(
            businessId = bizId,
            name = "Mizanur Rahman",
            phone = "01911112233",
            openingBalance = 350.0
        )
        assertTrue(custId > 0)

        val custQueue = dao.getSyncItemForEntity("CUSTOMER", custId)
        assertNotNull(custQueue)
        assertEquals("UPSERT", custQueue?.action)
        assertEquals("cust_$custId", custQueue?.cloudId)
        assertEquals("PENDING", custQueue?.status)

        // 2. Add Sale Transaction - Verify transaction & sync queue item creation
        val txId = repository.saveTransaction(
            businessId = bizId,
            type = "SALE",
            title = "Hardware Item Sale",
            amount = 1500.0,
            paidAmount = 1000.0,
            customerId = custId
        )
        assertTrue(txId > 0)

        val txQueue = dao.getSyncItemForEntity("TRANSACTION", txId)
        assertNotNull(txQueue)
        assertEquals("UPSERT", txQueue?.action)
        assertEquals("tx_$txId", txQueue?.cloudId)

        // 3. Delete Transaction - Verify sync queue item reflects DELETE
        val tx = repository.getTransactionById(txId)
        assertNotNull(tx)
        repository.deleteTransaction(tx!!)

        val deletedTxQueue = dao.getSyncItemForEntity("TRANSACTION", txId)
        assertNotNull(deletedTxQueue)
        assertEquals("DELETE", deletedTxQueue?.action)
    }

    @Test
    fun testOfflineSyncQueueOnProductAndStockUpdates() = runBlocking {
        val businesses = repository.businesses.first()
        val bizId = businesses.first().id
        val dao = AppDatabase.getDatabase(context).businessDao()

        // 1. Create Product
        val prodId = repository.saveProduct(
            businessId = bizId,
            name = "LED Tube Light 20W",
            sku = "LED-20W",
            purchasePrice = 120.0,
            sellingPrice = 160.0,
            currentQuantity = 50.0
        )
        assertTrue(prodId > 0)

        val prodQueue = dao.getSyncItemForEntity("PRODUCT", prodId)
        assertNotNull(prodQueue)
        assertEquals("UPSERT", prodQueue?.action)
        assertEquals("prod_$prodId", prodQueue?.cloudId)

        // 2. Adjust Stock
        repository.adjustProductStock(prodId, -5.0)
        val updatedProduct = repository.getProductById(prodId)
        assertEquals(45.0, updatedProduct?.currentQuantity ?: 0.0, 0.01)

        val updatedQueue = dao.getSyncItemForEntity("PRODUCT", prodId)
        assertNotNull(updatedQueue)
        assertEquals("UPSERT", updatedQueue?.action)
    }

    @Test
    fun testFirebaseDiagnosticsDetectsIncompleteSetupAccurately() = runBlocking {
        // Without google-services.json configured in test/debug environment:
        val report = syncManager.checkFirebaseSetup()

        // It must NOT pretend setup is complete
        assertFalse(report.hasGoogleServicesConfig)
        assertTrue(report.missingRequirements.isNotEmpty())
        assertTrue(report.missingRequirements.any { it.contains("google-services.json", ignoreCase = true) })

        // Triggering sync must return IncompleteSetup and never crash
        val result = syncManager.triggerSync(isManual = true)
        assertTrue(result is SyncResult.IncompleteSetup)

        val syncState = syncManager.syncState.value
        assertEquals(SyncStatusEnum.SETUP_REQUIRED, syncState.status)
        assertNotNull(syncState.errorMessage)
    }

    @Test
    fun testLocalOfflineDataIntegrityPreserved() = runBlocking {
        val businesses = repository.businesses.first()
        val bizId = businesses.first().id

        // Create Supplier and check calculation
        val suppId = repository.saveSupplier(
            businessId = bizId,
            name = "Jamal Cables",
            companyName = "Jamal Electric Ltd",
            openingBalance = 5000.0
        )
        assertTrue(suppId > 0)

        val supplier = repository.getSupplierById(suppId)
        assertNotNull(supplier)

        val txs = repository.getTransactions(bizId).first()
        val payable = repository.calculateSupplierPayable(supplier!!, txs)
        assertEquals(5000.0, payable, 0.01)
    }
}
