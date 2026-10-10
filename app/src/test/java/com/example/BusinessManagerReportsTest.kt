package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.business.BusinessProfile
import com.example.data.business.BusinessRepository
import com.example.data.business.BusinessTransaction
import com.example.data.business.Customer
import com.example.data.business.ProductItem
import com.example.data.business.Supplier
import com.example.data.business.report.BusinessReportCsvExporter
import com.example.data.business.report.BusinessReportGenerator
import com.example.data.business.report.BusinessReportPdfExporter
import com.example.data.business.report.ReportPeriodType
import com.example.localization.AppLanguage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BusinessManagerReportsTest {

    private lateinit var context: Context
    private lateinit var repository: BusinessRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        repository = BusinessRepository(context)
    }

    @Test
    fun testReportCalculationAccurateTotals() = runBlocking {
        val biz = repository.businesses.first().first()

        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        val tx1 = BusinessTransaction(
            id = 1,
            businessId = biz.id,
            type = "SALE",
            title = "Rice Bag Sale",
            amount = 1200.0,
            paidAmount = 800.0, // due 400
            dateString = todayStr
        )
        val tx2 = BusinessTransaction(
            id = 2,
            businessId = biz.id,
            type = "SALE",
            title = "Oil Bottle Sale",
            amount = 500.0,
            paidAmount = 500.0, // due 0
            dateString = todayStr
        )
        val tx3 = BusinessTransaction(
            id = 3,
            businessId = biz.id,
            type = "PURCHASE",
            title = "Stock Purchase",
            amount = 900.0,
            paidAmount = 600.0, // due 300
            dateString = todayStr
        )
        val tx4 = BusinessTransaction(
            id = 4,
            businessId = biz.id,
            type = "EXPENSE",
            title = "Electricity Bill",
            amount = 250.0,
            paidAmount = 250.0,
            dateString = todayStr
        )
        val tx5 = BusinessTransaction(
            id = 5,
            businessId = biz.id,
            type = "CUSTOMER_PAYMENT",
            title = "Due recovery",
            amount = 200.0,
            paidAmount = 200.0,
            dateString = todayStr
        )

        val txList = listOf(tx1, tx2, tx3, tx4, tx5)
        val customers = listOf(Customer(id = 1, businessId = biz.id, name = "Kamal", openingBalance = 100.0))
        val suppliers = listOf(Supplier(id = 1, businessId = biz.id, name = "Akbar Traders", openingBalance = 150.0))
        val products = listOf(
            ProductItem(id = 1, businessId = biz.id, name = "Rice", purchasePrice = 50.0, sellingPrice = 60.0, currentQuantity = 20.0, lowStockThreshold = 5.0)
        )

        val report = BusinessReportGenerator.generateReport(
            business = biz,
            periodType = ReportPeriodType.DAILY,
            allTransactions = txList,
            customers = customers,
            suppliers = suppliers,
            products = products,
            repository = repository
        )

        // Verifications
        assertEquals(1700.0, report.totalSales, 0.001)
        assertEquals(1300.0, report.salesCashReceived, 0.001)
        assertEquals(400.0, report.salesDueAmount, 0.001)

        assertEquals(900.0, report.totalPurchases, 0.001)
        assertEquals(600.0, report.purchasesCashPaid, 0.001)
        assertEquals(300.0, report.purchasesDueAmount, 0.001)

        assertEquals(250.0, report.totalExpenses, 0.001)
        assertEquals(200.0, report.customerPaymentsReceived, 0.001)

        // Gross profit = 1700 - 900 = 800
        assertEquals(800.0, report.grossProfit, 0.001)
        // Net profit = 1700 - (900 + 250) = 550
        assertEquals(550.0, report.netProfit, 0.001)

        // Inventory valuation = 20 * 50 = 1000
        assertEquals(1000.0, report.totalStockValuation, 0.001)
        assertEquals(1, report.totalProductsCount)
        assertEquals(0, report.lowStockCount)

        assertEquals(5, report.totalTransactionsCount)
        assertEquals(2, report.salesCount)
        assertEquals(1, report.purchasesCount)
        assertEquals(1, report.expensesCount)
        assertEquals(1, report.customerPaymentsCount)
    }

    @Test
    fun testDateRangeFilteringDailyMonthlyAndCustom() {
        val biz = BusinessProfile(id = 1, name = "Test Shop")

        val cal = Calendar.getInstance()
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        val todayStr = fmt.format(cal.time)

        cal.add(Calendar.MONTH, -1)
        val lastMonthStr = fmt.format(cal.time)

        val txToday = BusinessTransaction(id = 1, businessId = 1, type = "SALE", title = "Today sale", amount = 100.0, paidAmount = 100.0, dateString = todayStr)
        val txLastMonth = BusinessTransaction(id = 2, businessId = 1, type = "SALE", title = "Last month sale", amount = 200.0, paidAmount = 200.0, dateString = lastMonthStr)

        val allTx = listOf(txToday, txLastMonth)

        // Daily report: only today
        val dailyReport = BusinessReportGenerator.generateReport(
            business = biz,
            periodType = ReportPeriodType.DAILY,
            allTransactions = allTx
        )
        assertEquals(1, dailyReport.transactions.size)
        assertEquals(100.0, dailyReport.totalSales, 0.001)

        // Previous month report: only last month
        val prevMonthReport = BusinessReportGenerator.generateReport(
            business = biz,
            periodType = ReportPeriodType.PREV_MONTH,
            allTransactions = allTx
        )
        assertEquals(1, prevMonthReport.transactions.size)
        assertEquals(200.0, prevMonthReport.totalSales, 0.001)

        // Custom range covering both
        val customReport = BusinessReportGenerator.generateReport(
            business = biz,
            periodType = ReportPeriodType.CUSTOM,
            customStart = "2020-01-01",
            customEnd = "2030-12-31",
            allTransactions = allTx
        )
        assertEquals(2, customReport.transactions.size)
        assertEquals(300.0, customReport.totalSales, 0.001)
    }

    @Test
    fun testCsvExportGeneratesValidUtf8BomAndContent() {
        val biz = BusinessProfile(id = 1, name = "মেসার্স রহিম ট্রেডার্স (Rahim Traders)", currency = "৳")
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        val tx = BusinessTransaction(
            id = 1,
            businessId = 1,
            type = "SALE",
            title = "সয়াবিন তেল (Soybean Oil)",
            partyName = "করিম মিয়া (Karim)",
            amount = 850.0,
            paidAmount = 500.0,
            dateString = todayStr
        )

        val report = BusinessReportGenerator.generateReport(
            business = biz,
            periodType = ReportPeriodType.DAILY,
            allTransactions = listOf(tx)
        )

        val result = BusinessReportCsvExporter.exportToCsv(
            context = context,
            report = report,
            language = AppLanguage.BANGLA
        )

        assertTrue(result.isSuccess)
        val file = result.getOrNull()
        assertNotNull(file)
        assertTrue(file!!.exists())
        assertTrue(file.length() > 0)

        // Verify UTF-8 BOM bytes (0xEF, 0xBB, 0xBF)
        FileInputStream(file).use { fis ->
            val bom = ByteArray(3)
            val read = fis.read(bom)
            assertEquals(3, read)
            assertEquals(0xEF.toByte(), bom[0])
            assertEquals(0xBB.toByte(), bom[1])
            assertEquals(0xBF.toByte(), bom[2])
        }

        // Verify content contains Bangla text and numbers
        val content = file.readText(Charsets.UTF_8)
        assertTrue(content.contains("রহিম ট্রেডার্স"))
        assertTrue(content.contains("সয়াবিন তেল"))
        assertTrue(content.contains("850.00"))
        assertTrue(content.contains("500.00"))
        assertTrue(content.contains("350.00"))
    }

    @Test
    fun testPdfExportGeneratesValidDocument() {
        val biz = BusinessProfile(id = 1, name = "Modern Pharmacy (মডার্ন ফার্মেসি)", businessType = "Pharmacy", currency = "৳")
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        val tx1 = BusinessTransaction(
            id = 1,
            businessId = 1,
            type = "SALE",
            title = "Napa Extra 50 strips",
            partyName = "Dr. Hasan",
            amount = 1250.0,
            paidAmount = 1250.0,
            dateString = todayStr
        )
        val tx2 = BusinessTransaction(
            id = 2,
            businessId = 1,
            type = "PURCHASE",
            title = "Square Pharma Invoices",
            partyName = "Square Pharmaceuticals",
            amount = 3500.0,
            paidAmount = 2000.0,
            dateString = todayStr
        )

        val report = BusinessReportGenerator.generateReport(
            business = biz,
            periodType = ReportPeriodType.DAILY,
            allTransactions = listOf(tx1, tx2)
        )

        val result = BusinessReportPdfExporter.exportToPdf(
            context = context,
            report = report,
            language = AppLanguage.BANGLA
        )

        if (result.isFailure) {
            result.exceptionOrNull()?.printStackTrace()
            System.err.println("PDF EXPORT ERROR: " + result.exceptionOrNull()?.message)
        }

        assertTrue(result.isSuccess)
        val file = result.getOrNull()
        assertNotNull(file)
        assertTrue(file!!.exists())
        assertTrue(file.length() > 500)

        // Verify %PDF header magic bytes
        FileInputStream(file).use { fis ->
            val header = ByteArray(4)
            fis.read(header)
            assertEquals("%PDF", String(header))
        }
    }

    @Test
    fun testEmptyReportGracefulHandling() {
        val biz = BusinessProfile(id = 2, name = "Empty Enterprise")

        val emptyReport = BusinessReportGenerator.generateReport(
            business = biz,
            periodType = ReportPeriodType.DAILY,
            allTransactions = emptyList()
        )

        assertEquals(0.0, emptyReport.totalSales, 0.0)
        assertEquals(0.0, emptyReport.totalPurchases, 0.0)
        assertEquals(0.0, emptyReport.netProfit, 0.0)
        assertEquals(0, emptyReport.totalTransactionsCount)

        // CSV export with zero transactions
        val csvResult = BusinessReportCsvExporter.exportToCsv(context, emptyReport, AppLanguage.BANGLA)
        assertTrue(csvResult.isSuccess)
        val csvFile = csvResult.getOrNull()!!
        assertTrue(csvFile.exists())
        val csvContent = csvFile.readText(Charsets.UTF_8)
        assertTrue(csvContent.contains("Empty Enterprise"))
        assertTrue(csvContent.contains("এই সময়কালে কোন লেনদেন নেই") || csvContent.contains("No transactions recorded"))

        // PDF export with zero transactions
        val pdfResult = BusinessReportPdfExporter.exportToPdf(context, emptyReport, AppLanguage.BANGLA)
        assertTrue(pdfResult.isSuccess)
        val pdfFile = pdfResult.getOrNull()!!
        assertTrue(pdfFile.exists())
        assertTrue(pdfFile.length() > 200)
    }

    @Test
    fun testLargeReportPdfPagination() {
        val biz = BusinessProfile(id = 3, name = "Super Wholesale Market")
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        val largeList = (1..60).map { i ->
            BusinessTransaction(
                id = i.toLong(),
                businessId = 3,
                type = if (i % 2 == 0) "SALE" else "PURCHASE",
                title = "Item Bulk $i",
                partyName = "Party $i",
                amount = 100.0 * i,
                paidAmount = 80.0 * i,
                dateString = todayStr
            )
        }

        val report = BusinessReportGenerator.generateReport(
            business = biz,
            periodType = ReportPeriodType.DAILY,
            allTransactions = largeList
        )

        val pdfResult = BusinessReportPdfExporter.exportToPdf(context, report, AppLanguage.ENGLISH)
        assertTrue(pdfResult.isSuccess)
        val pdfFile = pdfResult.getOrNull()!!
        assertTrue(pdfFile.exists())
        assertTrue(pdfFile.length() > 1000)
    }
}
