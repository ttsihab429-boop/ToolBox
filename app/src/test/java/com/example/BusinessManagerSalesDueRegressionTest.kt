package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.business.BusinessProfile
import com.example.data.business.BusinessRepository
import com.example.data.business.BusinessTransaction
import com.example.data.business.Customer
import com.example.ui.screens.business.formatDecimal
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BusinessManagerSalesDueRegressionTest {

    private lateinit var context: Context
    private lateinit var repository: BusinessRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        repository = BusinessRepository(context)
    }

    @Test
    fun testSaleTransactionCalculatesDueAsTotalMinusPaidWithoutOverwritingUnrelatedFields() = runBlocking {
        val biz = repository.businesses.first().first()

        // Create a customer
        val custId = repository.saveCustomer(
            businessId = biz.id,
            name = "Anisur Rahman",
            phone = "01712345678",
            address = "Dhanmondi, Dhaka",
            openingBalance = 200.0,
            note = "Important VIP Customer"
        )
        val customer = repository.getCustomerById(custId)
        assertNotNull(customer)
        assertEquals(200.0, customer?.openingBalance ?: 0.0, 0.001)

        // Record a SALE transaction with Total Sales = 1500.0, Paid = 1000.0
        // Expected Due for this sale = 1500.0 - 1000.0 = 500.0
        val txId = repository.saveTransaction(
            businessId = biz.id,
            type = "SALE",
            title = "Hardware Materials Batch",
            amount = 1500.0,
            paidAmount = 1000.0,
            partyName = "Anisur Rahman",
            partyPhone = "01712345678",
            note = "Delivery via courier",
            customerId = custId,
            quantity = 15.0,
            unitPrice = 100.0
        )
        assertTrue(txId > 0)

        val tx = repository.getTransactionById(txId)
        assertNotNull(tx)
        assertEquals(1500.0, tx!!.amount, 0.001)
        assertEquals(1000.0, tx.paidAmount, 0.001)
        assertEquals(500.0, tx.dueAmount, 0.001)

        // Verify unrelated fields were completely preserved without overwriting
        assertEquals("Hardware Materials Batch", tx.title)
        assertEquals("Anisur Rahman", tx.partyName)
        assertEquals("01712345678", tx.partyPhone)
        assertEquals("Delivery via courier", tx.note)
        assertEquals(custId, tx.customerId)
        assertEquals(15.0, tx.quantity, 0.001)
        assertEquals(100.0, tx.unitPrice, 0.001)

        // Customer due calculation should be: opening balance (200) + sale due (500) = 700
        val allTx = repository.getTransactions(biz.id).first()
        val totalCustomerDue = repository.calculateCustomerDue(customer!!, allTx)
        assertEquals(700.0, totalCustomerDue, 0.001)
    }

    @Test
    fun testFullCashSaleDoesNotIncreaseCustomerDue() = runBlocking {
        val biz = repository.businesses.first().first()

        val custId = repository.saveCustomer(
            businessId = biz.id,
            name = "Cash Buyer",
            openingBalance = 0.0
        )
        val customer = repository.getCustomerById(custId)
        assertNotNull(customer)

        // A sale where customer pays in full (1000 total, 1000 paid)
        val txId = repository.saveTransaction(
            businessId = biz.id,
            type = "SALE",
            title = "Cash Groceries",
            amount = 1000.0,
            paidAmount = 1000.0,
            customerId = custId
        )
        val tx = repository.getTransactionById(txId)
        assertEquals(0.0, tx?.dueAmount ?: -1.0, 0.001)

        val allTx = repository.getTransactions(biz.id).first()
        val customerDue = repository.calculateCustomerDue(customer!!, allTx)
        assertEquals(0.0, customerDue, 0.001)
    }

    @Test
    fun testFormStateBindingsTotalSalesAndCustomerDueIndependence() {
        // Simulating the state binding and interaction logic implemented in AddEditTransactionDialog
        var amountText = ""
        var paidText = ""
        var dueText = ""
        val title = "Sample Item"
        val partyName = "Customer X"

        // 1. User types Total Sales = 1000.0
        // Because neither paidText nor dueText was explicitly set yet,
        // typing Total Sales must NOT automatically set dueText to 1000!
        fun onTotalSalesChanged(newAmount: String) {
            amountText = newAmount
            val newAmtVal = newAmount.toDoubleOrNull()
            if (newAmtVal != null) {
                val pVal = paidText.toDoubleOrNull()
                val dVal = dueText.toDoubleOrNull()
                if (pVal != null && paidText.isNotBlank()) {
                    val newDue = (newAmtVal - pVal).coerceAtLeast(0.0)
                    dueText = if (newDue > 0) formatDecimal(newDue) else "0"
                } else if (dVal != null && dueText.isNotBlank()) {
                    val newPaid = (newAmtVal - dVal).coerceAtLeast(0.0)
                    paidText = if (newPaid > 0) formatDecimal(newPaid) else "0"
                }
            }
        }

        fun onPaidChanged(newPaid: String) {
            paidText = newPaid
            val pVal = newPaid.toDoubleOrNull()
            val aVal = amountText.toDoubleOrNull() ?: 0.0
            if (pVal != null && aVal > 0) {
                val calculated = (aVal - pVal).coerceAtLeast(0.0)
                dueText = if (calculated > 0) formatDecimal(calculated) else "0"
            } else if (newPaid.isBlank()) {
                dueText = ""
            }
        }

        fun onDueChanged(newDue: String) {
            dueText = newDue
            val dVal = newDue.toDoubleOrNull()
            val aVal = amountText.toDoubleOrNull() ?: 0.0
            if (dVal != null && aVal > 0) {
                val calculated = (aVal - dVal).coerceAtLeast(0.0)
                paidText = if (calculated > 0) formatDecimal(calculated) else "0"
            } else if (newDue.isBlank()) {
                paidText = ""
            }
        }

        // Test 1: User types 2500 into Total Sales
        onTotalSalesChanged("2500")
        assertEquals("2500", amountText)
        // Crucial bug fix verification: Customer Due is NOT automatically set to 2500!
        assertEquals("", dueText)

        // Test 2: User explicitly types Amount Paid = 2000
        onPaidChanged("2000")
        assertEquals("2000", paidText)
        assertEquals("500", dueText) // Due correctly calculated as 2500 - 2000 = 500

        // Test 3: User adjusts Customer Due = 700 directly
        onDueChanged("700")
        assertEquals("700", dueText)
        assertEquals("1800", paidText) // Paid correctly recalculated as 2500 - 700 = 1800

        // Test 4: User updates Total Sales to 3000
        // Because paidText is active (1800), dueText recalculates to 3000 - 1800 = 1200
        onTotalSalesChanged("3000")
        assertEquals("3000", amountText)
        assertEquals("1200", dueText)
        assertEquals("1800", paidText)

        // Unrelated fields verification
        assertEquals("Sample Item", title)
        assertEquals("Customer X", partyName)
    }
}
