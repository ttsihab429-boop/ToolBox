package com.example.data.business.report

import com.example.data.business.BusinessProfile
import com.example.data.business.BusinessTransaction

enum class ReportPeriodType {
    DAILY,
    MONTHLY,
    PREV_MONTH,
    CUSTOM
}

data class BusinessReportSummary(
    val business: BusinessProfile,
    val periodType: ReportPeriodType,
    val startDate: String,
    val endDate: String,
    val periodLabel: String,
    val generatedAt: Long = System.currentTimeMillis(),

    // Core Revenue & Cost Totals
    val totalSales: Double,
    val salesCashReceived: Double,
    val salesDueAmount: Double,

    val totalPurchases: Double,
    val purchasesCashPaid: Double,
    val purchasesDueAmount: Double,

    val totalExpenses: Double,

    val customerPaymentsReceived: Double,
    val supplierPaymentsMade: Double,

    // Profit Calculations
    val grossProfit: Double, // totalSales - totalPurchases
    val netProfit: Double,   // totalSales - (totalPurchases + totalExpenses)

    // Outstanding Balances
    val totalCustomerOutstandingDues: Double,
    val totalSupplierOutstandingPayables: Double,

    // Inventory Snapshot
    val totalProductsCount: Int,
    val lowStockCount: Int,
    val outOfStockCount: Int,
    val totalStockValuation: Double,

    // Transaction Breakdown Counts
    val totalTransactionsCount: Int,
    val salesCount: Int,
    val purchasesCount: Int,
    val expensesCount: Int,
    val customerPaymentsCount: Int,
    val supplierPaymentsCount: Int,

    // The individual transactions in this date range
    val transactions: List<BusinessTransaction>
)
