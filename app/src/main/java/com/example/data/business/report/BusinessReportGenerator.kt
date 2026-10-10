package com.example.data.business.report

import com.example.data.business.BusinessProfile
import com.example.data.business.BusinessRepository
import com.example.data.business.BusinessTransaction
import com.example.data.business.Customer
import com.example.data.business.ProductItem
import com.example.data.business.Supplier
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object BusinessReportGenerator {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    /**
     * Computes the date range [startDate, endDate] for the given period.
     */
    fun computeDateRange(
        periodType: ReportPeriodType,
        customStart: String? = null,
        customEnd: String? = null,
        referenceDate: Date = Date()
    ): Pair<String, String> {
        val calendar = Calendar.getInstance().apply { time = referenceDate }

        return when (periodType) {
            ReportPeriodType.DAILY -> {
                val todayStr = dateFormat.format(calendar.time)
                Pair(todayStr, todayStr)
            }
            ReportPeriodType.MONTHLY -> {
                val calStart = (calendar.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                }
                val calEnd = (calendar.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                }
                Pair(dateFormat.format(calStart.time), dateFormat.format(calEnd.time))
            }
            ReportPeriodType.PREV_MONTH -> {
                val calPrev = (calendar.clone() as Calendar).apply {
                    add(Calendar.MONTH, -1)
                }
                val calStart = (calPrev.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                }
                val calEnd = (calPrev.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                }
                Pair(dateFormat.format(calStart.time), dateFormat.format(calEnd.time))
            }
            ReportPeriodType.CUSTOM -> {
                val todayStr = dateFormat.format(calendar.time)
                val s = if (!customStart.isNullOrBlank()) customStart.trim() else todayStr
                val e = if (!customEnd.isNullOrBlank()) customEnd.trim() else s
                if (s <= e) Pair(s, e) else Pair(e, s)
            }
        }
    }

    /**
     * Builds a comprehensive BusinessReportSummary without altering existing calculations.
     */
    fun generateReport(
        business: BusinessProfile,
        periodType: ReportPeriodType,
        customStart: String? = null,
        customEnd: String? = null,
        allTransactions: List<BusinessTransaction>,
        customers: List<Customer> = emptyList(),
        suppliers: List<Supplier> = emptyList(),
        products: List<ProductItem> = emptyList(),
        repository: BusinessRepository? = null,
        periodLabel: String = ""
    ): BusinessReportSummary {
        val (startDate, endDate) = computeDateRange(periodType, customStart, customEnd)

        // Filter transactions strictly by date range
        val filteredTransactions = allTransactions.filter { tx ->
            val d = tx.dateString
            d in startDate..endDate
        }.sortedByDescending { it.timestamp }

        // Core Revenue & Expense Totals
        val salesList = filteredTransactions.filter { it.type == "SALE" }
        val purchaseList = filteredTransactions.filter { it.type == "PURCHASE" }
        val expenseList = filteredTransactions.filter { it.type == "EXPENSE" }
        val custPaymentsList = filteredTransactions.filter { it.type == "CUSTOMER_PAYMENT" }
        val suppPaymentsList = filteredTransactions.filter { it.type == "SUPPLIER_PAYMENT" }

        val totalSales = salesList.sumOf { it.amount }
        val salesCashReceived = salesList.sumOf { it.paidAmount }
        val salesDueAmount = salesList.sumOf { it.dueAmount }

        val totalPurchases = purchaseList.sumOf { it.amount }
        val purchasesCashPaid = purchaseList.sumOf { it.paidAmount }
        val purchasesDueAmount = purchaseList.sumOf { it.dueAmount }

        val totalExpenses = expenseList.sumOf { it.amount }

        val customerPaymentsReceived = custPaymentsList.sumOf { it.amount }
        val supplierPaymentsMade = suppPaymentsList.sumOf { it.amount }

        // Profit & Loss calculations (standard business ledger)
        val grossProfit = totalSales - totalPurchases
        val netProfit = totalSales - (totalPurchases + totalExpenses)

        // Outstanding Dues (use repository logic if available, else derive)
        val totalCustomerDues = if (customers.isNotEmpty() && repository != null) {
            customers.sumOf { repository.calculateCustomerDue(it, allTransactions) }
        } else {
            allTransactions.filter { it.type == "SALE" }.sumOf { it.dueAmount }
        }

        val totalSupplierPayables = if (suppliers.isNotEmpty() && repository != null) {
            suppliers.sumOf { repository.calculateSupplierPayable(it, allTransactions) }
        } else {
            allTransactions.filter { it.type == "PURCHASE" }.sumOf { it.dueAmount }
        }

        // Inventory snapshot
        val totalProductsCount = products.size
        val lowStockCount = products.count { it.isLowStock }
        val outOfStockCount = products.count { it.isOutOfStock }
        val totalStockValuation = products.sumOf { it.currentQuantity * it.purchasePrice }

        val label = if (periodLabel.isNotBlank()) {
            periodLabel
        } else {
            when (periodType) {
                ReportPeriodType.DAILY -> "Daily Report ($startDate)"
                ReportPeriodType.MONTHLY -> "Monthly Report ($startDate to $endDate)"
                ReportPeriodType.PREV_MONTH -> "Previous Month Report ($startDate to $endDate)"
                ReportPeriodType.CUSTOM -> "Custom Range Report ($startDate to $endDate)"
            }
        }

        return BusinessReportSummary(
            business = business,
            periodType = periodType,
            startDate = startDate,
            endDate = endDate,
            periodLabel = label,
            totalSales = totalSales,
            salesCashReceived = salesCashReceived,
            salesDueAmount = salesDueAmount,
            totalPurchases = totalPurchases,
            purchasesCashPaid = purchasesCashPaid,
            purchasesDueAmount = purchasesDueAmount,
            totalExpenses = totalExpenses,
            customerPaymentsReceived = customerPaymentsReceived,
            supplierPaymentsMade = supplierPaymentsMade,
            grossProfit = grossProfit,
            netProfit = netProfit,
            totalCustomerOutstandingDues = totalCustomerDues,
            totalSupplierOutstandingPayables = totalSupplierPayables,
            totalProductsCount = totalProductsCount,
            lowStockCount = lowStockCount,
            outOfStockCount = outOfStockCount,
            totalStockValuation = totalStockValuation,
            totalTransactionsCount = filteredTransactions.size,
            salesCount = salesList.size,
            purchasesCount = purchaseList.size,
            expensesCount = expenseList.size,
            customerPaymentsCount = custPaymentsList.size,
            supplierPaymentsCount = suppPaymentsList.size,
            transactions = filteredTransactions
        )
    }
}
