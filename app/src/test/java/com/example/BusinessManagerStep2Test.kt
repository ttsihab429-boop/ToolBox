package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.business.BusinessRepository
import com.example.data.business.Customer
import com.example.data.business.ProductItem
import com.example.data.business.Supplier
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
class BusinessManagerStep2Test {

    private lateinit var context: Context
    private lateinit var repository: BusinessRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        repository = BusinessRepository(context)
    }

    @Test
    fun testCustomerCreationAndDueCalculation() = runBlocking {
        val businesses = repository.businesses.first()
        val bizId = businesses.first().id

        // 1. Add Customer with opening balance of 500
        val customerId = repository.saveCustomer(
            businessId = bizId,
            name = "Rahim Mia",
            phone = "01700000001",
            address = "Dhaka",
            openingBalance = 500.0,
            note = "Regular wholesale buyer"
        )
        assertTrue(customerId > 0)

        val customer = repository.getCustomerById(customerId)
        assertNotNull(customer)
        assertEquals("Rahim Mia", customer?.name)
        assertEquals(500.0, customer?.openingBalance ?: 0.0, 0.01)

        // 2. Add a Sale transaction of 1000 with 700 paid (300 due)
        repository.saveTransaction(
            businessId = bizId,
            type = "SALE",
            title = "Item Sale",
            amount = 1000.0,
            paidAmount = 700.0,
            partyName = "Rahim Mia",
            partyPhone = "01700000001",
            customerId = customerId
        )

        val transactionsAfterSale = repository.getTransactions(bizId).first()
        val dueAfterSale = repository.calculateCustomerDue(customer!!, transactionsAfterSale)
        // 500 opening due + 300 sale due = 800
        assertEquals(800.0, dueAfterSale, 0.01)

        // 3. Record a Customer Payment of 200
        repository.recordCustomerPayment(
            businessId = bizId,
            customerId = customerId,
            customerName = "Rahim Mia",
            customerPhone = "01700000001",
            amount = 200.0,
            note = "bKash payment"
        )

        val transactionsAfterPayment = repository.getTransactions(bizId).first()
        val dueAfterPayment = repository.calculateCustomerDue(customer, transactionsAfterPayment)
        // 800 - 200 = 600
        assertEquals(600.0, dueAfterPayment, 0.01)
    }

    @Test
    fun testSupplierCreationAndPayableCalculation() = runBlocking {
        val businesses = repository.businesses.first()
        val bizId = businesses.first().id

        // 1. Add Supplier with opening payable balance of 1000
        val supplierId = repository.saveSupplier(
            businessId = bizId,
            name = "Akbar Ali",
            companyName = "Akbar Traders",
            phone = "01800000002",
            openingBalance = 1000.0
        )
        assertTrue(supplierId > 0)

        val supplier = repository.getSupplierById(supplierId)
        assertNotNull(supplier)
        assertEquals("Akbar Ali", supplier?.name)

        // 2. Add a Purchase transaction of 2500 with 1500 paid (1000 due)
        repository.saveTransaction(
            businessId = bizId,
            type = "PURCHASE",
            title = "Raw Materials",
            amount = 2500.0,
            paidAmount = 1500.0,
            supplierId = supplierId
        )

        val transactionsAfterPurchase = repository.getTransactions(bizId).first()
        val payableAfterPurchase = repository.calculateSupplierPayable(supplier!!, transactionsAfterPurchase)
        // 1000 opening payable + 1000 purchase due = 2000
        assertEquals(2000.0, payableAfterPurchase, 0.01)

        // 3. Record Supplier Payment of 800
        repository.recordSupplierPayment(
            businessId = bizId,
            supplierId = supplierId,
            supplierName = "Akbar Ali",
            supplierPhone = "01800000002",
            amount = 800.0,
            note = "Bank Transfer"
        )

        val transactionsAfterPayment = repository.getTransactions(bizId).first()
        val payableAfterPayment = repository.calculateSupplierPayable(supplier, transactionsAfterPayment)
        // 2000 - 800 = 1200
        assertEquals(1200.0, payableAfterPayment, 0.01)
    }

    @Test
    fun testProductInventoryAndStockUpdates() = runBlocking {
        val businesses = repository.businesses.first()
        val bizId = businesses.first().id

        // 1. Create a Product Item with initial quantity of 20
        val productId = repository.saveProduct(
            businessId = bizId,
            name = "Mustard Oil 1L",
            sku = "MO-1001",
            category = "Grocery",
            purchasePrice = 180.0,
            sellingPrice = 220.0,
            currentQuantity = 20.0,
            unit = "bottle",
            lowStockThreshold = 5.0
        )
        assertTrue(productId > 0)

        var product = repository.getProductById(productId)
        assertNotNull(product)
        assertEquals(20.0, product?.currentQuantity ?: 0.0, 0.01)
        assertFalse(product!!.isLowStock)

        // 2. Record a SALE of 5 bottles
        val saleTxId = repository.saveTransaction(
            businessId = bizId,
            type = "SALE",
            title = "Mustard Oil 1L",
            amount = 1100.0,
            paidAmount = 1100.0,
            productId = productId,
            quantity = 5.0,
            unitPrice = 220.0
        )

        product = repository.getProductById(productId)
        // Stock should be 20 - 5 = 15
        assertEquals(15.0, product?.currentQuantity ?: 0.0, 0.01)

        // 3. Record a PURCHASE of 10 bottles
        repository.saveTransaction(
            businessId = bizId,
            type = "PURCHASE",
            title = "Mustard Oil 1L restock",
            amount = 1800.0,
            paidAmount = 1800.0,
            productId = productId,
            quantity = 10.0,
            unitPrice = 180.0
        )

        product = repository.getProductById(productId)
        // Stock should be 15 + 10 = 25
        assertEquals(25.0, product?.currentQuantity ?: 0.0, 0.01)

        // 4. Update the SALE transaction (change quantity from 5 to 8)
        repository.saveTransaction(
            id = saleTxId,
            businessId = bizId,
            type = "SALE",
            title = "Mustard Oil 1L",
            amount = 1760.0,
            paidAmount = 1760.0,
            productId = productId,
            quantity = 8.0,
            unitPrice = 220.0
        )

        product = repository.getProductById(productId)
        // Reverting old 5 (+5 -> 30), deducting new 8 (-8 -> 22)
        assertEquals(22.0, product?.currentQuantity ?: 0.0, 0.01)

        // 5. Delete the SALE transaction
        val saleTx = repository.getTransactionById(saleTxId)
        assertNotNull(saleTx)
        repository.deleteTransaction(saleTx!!)

        product = repository.getProductById(productId)
        // Reverting deleted sale of 8 (+8 -> 30)
        assertEquals(30.0, product?.currentQuantity ?: 0.0, 0.01)

        // 6. Test direct Stock Adjustment (-27 to trigger low stock)
        repository.adjustProductStock(productId, -27.0)
        product = repository.getProductById(productId)
        assertEquals(3.0, product?.currentQuantity ?: 0.0, 0.01)
        assertTrue(product!!.isLowStock)
        assertFalse(product!!.isOutOfStock)
    }
}
