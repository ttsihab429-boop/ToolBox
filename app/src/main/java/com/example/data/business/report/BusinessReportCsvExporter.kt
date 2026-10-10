package com.example.data.business.report

import android.content.Context
import com.example.data.business.BusinessTransaction
import com.example.localization.AppLanguage
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BusinessReportCsvExporter {

    private fun escapeCsv(value: Any?): String {
        if (value == null) return ""
        val str = value.toString()
        return if (str.contains(",") || str.contains("\"") || str.contains("\n") || str.contains("\r")) {
            "\"" + str.replace("\"", "\"\"") + "\""
        } else {
            str
        }
    }

    /**
     * Exports a BusinessReportSummary into a CSV file with UTF-8 BOM encoding for complete Bangla & English support.
     */
    fun exportToCsv(
        context: Context,
        report: BusinessReportSummary,
        language: AppLanguage = AppLanguage.BANGLA,
        targetFile: File? = null
    ): Result<File> {
        return try {
            val outputDir = File(context.cacheDir, "reports").apply { mkdirs() }
            val sanitizedBizName = report.business.name.replace(Regex("[^a-zA-Z0-9_]"), "_").take(20)
            val fileName = "Report_${sanitizedBizName}_${report.startDate}_to_${report.endDate}_${System.currentTimeMillis()}.csv"
            val file = targetFile ?: File(outputDir, fileName)

            val isBangla = language == AppLanguage.BANGLA
            val currency = report.business.currency

            val genTimeStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(report.generatedAt))

            val sb = StringBuilder()

            // 1. Header Information
            sb.append(escapeCsv(if (isBangla) "ব্যবসার নাম" else "Business Name")).append(",")
                .append(escapeCsv(report.business.name)).append("\n")
            sb.append(escapeCsv(if (isBangla) "ব্যবসার ধরন" else "Business Type")).append(",")
                .append(escapeCsv(report.business.businessType)).append("\n")
            if (report.business.phone.isNotBlank()) {
                sb.append(escapeCsv(if (isBangla) "মোবাইল" else "Phone")).append(",")
                    .append(escapeCsv(report.business.phone)).append("\n")
            }
            sb.append(escapeCsv(if (isBangla) "রিপোর্টের সময়কাল" else "Report Period")).append(",")
                .append(escapeCsv(report.periodLabel)).append("\n")
            sb.append(escapeCsv(if (isBangla) "তারিখ সীমা" else "Date Range")).append(",")
                .append(escapeCsv("${report.startDate} to ${report.endDate}")).append("\n")
            sb.append(escapeCsv(if (isBangla) "তৈরির সময়" else "Generated At")).append(",")
                .append(escapeCsv(genTimeStr)).append("\n")
            sb.append(escapeCsv(if (isBangla) "মুদ্রা" else "Currency")).append(",")
                .append(escapeCsv(currency)).append("\n\n")

            // 2. Executive Summary Metrics
            sb.append(escapeCsv(if (isBangla) "সারসংক্ষেপ ও হিসাব খতিয়ান" else "Financial Summary")).append(",")
                .append(escapeCsv(if (isBangla) "পরিমাণ ($currency)" else "Amount ($currency)")).append("\n")

            sb.append(escapeCsv(if (isBangla) "মোট বিক্রি (Total Sales)" else "Total Sales")).append(",")
                .append(escapeCsv(String.format(Locale.US, "%.2f", report.totalSales))).append("\n")
            sb.append(escapeCsv(if (isBangla) "বিক্রি বাবদ নগদ জমা (Cash Received)" else "Sales Cash Received")).append(",")
                .append(escapeCsv(String.format(Locale.US, "%.2f", report.salesCashReceived))).append("\n")
            sb.append(escapeCsv(if (isBangla) "বিক্রিতে বকেয়া বাকি (Sales Due)" else "Sales Due Balance")).append(",")
                .append(escapeCsv(String.format(Locale.US, "%.2f", report.salesDueAmount))).append("\n")

            sb.append(escapeCsv(if (isBangla) "মোট ক্রয় (Total Purchases)" else "Total Purchases")).append(",")
                .append(escapeCsv(String.format(Locale.US, "%.2f", report.totalPurchases))).append("\n")
            sb.append(escapeCsv(if (isBangla) "ক্রয় বাবদ নগদ পরিশোধ (Purchases Paid)" else "Purchases Cash Paid")).append(",")
                .append(escapeCsv(String.format(Locale.US, "%.2f", report.purchasesCashPaid))).append("\n")
            sb.append(escapeCsv(if (isBangla) "ক্রয়ে বকেয়া দেনা (Purchases Due)" else "Purchases Due Balance")).append(",")
                .append(escapeCsv(String.format(Locale.US, "%.2f", report.purchasesDueAmount))).append("\n")

            sb.append(escapeCsv(if (isBangla) "মোট দোকান খরচ (Total Expenses)" else "Total Expenses")).append(",")
                .append(escapeCsv(String.format(Locale.US, "%.2f", report.totalExpenses))).append("\n")

            sb.append(escapeCsv(if (isBangla) "গ্রাহক বাকি আদায় (Customer Payments)" else "Customer Payments Received")).append(",")
                .append(escapeCsv(String.format(Locale.US, "%.2f", report.customerPaymentsReceived))).append("\n")
            sb.append(escapeCsv(if (isBangla) "মহাজন দেনা পরিশোধ (Supplier Payments)" else "Supplier Payments Made")).append(",")
                .append(escapeCsv(String.format(Locale.US, "%.2f", report.supplierPaymentsMade))).append("\n")

            sb.append(escapeCsv(if (isBangla) "মোট লাভ (Gross Profit)" else "Gross Profit")).append(",")
                .append(escapeCsv(String.format(Locale.US, "%.2f", report.grossProfit))).append("\n")
            sb.append(escapeCsv(if (isBangla) "নিট লাভ / ক্ষতি (Net Profit / Loss)" else "Net Profit / Loss")).append(",")
                .append(escapeCsv(String.format(Locale.US, "%.2f", report.netProfit))).append("\n")

            sb.append(escapeCsv(if (isBangla) "সর্বমোট গ্রাহক বকেয়া পাওনা (Total Customer Dues)" else "Total Customer Outstanding Dues")).append(",")
                .append(escapeCsv(String.format(Locale.US, "%.2f", report.totalCustomerOutstandingDues))).append("\n")
            sb.append(escapeCsv(if (isBangla) "সর্বমোট মহাজন দেনা বাকি (Total Supplier Payables)" else "Total Supplier Outstanding Payables")).append(",")
                .append(escapeCsv(String.format(Locale.US, "%.2f", report.totalSupplierOutstandingPayables))).append("\n")

            sb.append(escapeCsv(if (isBangla) "মোট পণ্যের আইটেম (Products in Inventory)" else "Inventory Items Count")).append(",")
                .append(escapeCsv(report.totalProductsCount)).append("\n")
            sb.append(escapeCsv(if (isBangla) "কম স্টক ও শেষ স্টক আইটেম" else "Low / Out of Stock Count")).append(",")
                .append(escapeCsv("${report.lowStockCount} (Out: ${report.outOfStockCount})")).append("\n")
            sb.append(escapeCsv(if (isBangla) "মজুদ পণ্যের মূল্যমান (Inventory Valuation)" else "Inventory Stock Valuation")).append(",")
                .append(escapeCsv(String.format(Locale.US, "%.2f", report.totalStockValuation))).append("\n")
            sb.append(escapeCsv(if (isBangla) "মোট লেনদেনের সংখ্যা (Total Transactions)" else "Total Transactions Count")).append(",")
                .append(escapeCsv(report.totalTransactionsCount)).append("\n\n")

            // 3. Detailed Transactions Table
            sb.append(escapeCsv(if (isBangla) "বিস্তারিত লেনদেন খতিয়ান" else "Transaction Ledger")).append("\n")
            sb.append(escapeCsv(if (isBangla) "তারিখ" else "Date")).append(",")
                .append(escapeCsv(if (isBangla) "ধরন" else "Type")).append(",")
                .append(escapeCsv(if (isBangla) "বিবরণ / শিরোনাম" else "Title / Description")).append(",")
                .append(escapeCsv(if (isBangla) "পক্ষ (কাস্টমার / মহাজন)" else "Party Name")).append(",")
                .append(escapeCsv(if (isBangla) "ফোন" else "Phone")).append(",")
                .append(escapeCsv(if (isBangla) "পরিমাণ" else "Quantity")).append(",")
                .append(escapeCsv(if (isBangla) "দর ($currency)" else "Unit Price ($currency)")).append(",")
                .append(escapeCsv(if (isBangla) "মোট টাকা ($currency)" else "Total Amount ($currency)")).append(",")
                .append(escapeCsv(if (isBangla) "জমা / পরিশোধ ($currency)" else "Paid Amount ($currency)")).append(",")
                .append(escapeCsv(if (isBangla) "বাকি ($currency)" else "Due Balance ($currency)")).append(",")
                .append(escapeCsv(if (isBangla) "নোট" else "Note")).append("\n")

            if (report.transactions.isEmpty()) {
                sb.append(escapeCsv(if (isBangla) "এই সময়কালে কোন লেনদেন নেই" else "No transactions recorded for this period")).append("\n")
            } else {
                for (tx in report.transactions) {
                    val typeLabel = when (tx.type) {
                        "SALE" -> if (isBangla) "বিক্রি" else "Sale"
                        "PURCHASE" -> if (isBangla) "ক্রয়" else "Purchase"
                        "EXPENSE" -> if (isBangla) "খরচ" else "Expense"
                        "CUSTOMER_PAYMENT" -> if (isBangla) "বাকি আদায়" else "Cust Payment"
                        "SUPPLIER_PAYMENT" -> if (isBangla) "মহাজন পরিশোধ" else "Supp Payment"
                        else -> tx.type
                    }

                    sb.append(escapeCsv(tx.dateString)).append(",")
                        .append(escapeCsv(typeLabel)).append(",")
                        .append(escapeCsv(tx.title)).append(",")
                        .append(escapeCsv(tx.partyName)).append(",")
                        .append(escapeCsv(tx.partyPhone)).append(",")
                        .append(escapeCsv(if (tx.quantity > 0) tx.quantity.toString() else "")).append(",")
                        .append(escapeCsv(if (tx.unitPrice > 0) String.format(Locale.US, "%.2f", tx.unitPrice) else "")).append(",")
                        .append(escapeCsv(String.format(Locale.US, "%.2f", tx.amount))).append(",")
                        .append(escapeCsv(String.format(Locale.US, "%.2f", tx.paidAmount))).append(",")
                        .append(escapeCsv(String.format(Locale.US, "%.2f", tx.dueAmount))).append(",")
                        .append(escapeCsv(tx.note)).append("\n")
                }
            }

            // Write with UTF-8 BOM (\uFEFF)
            FileOutputStream(file).use { fos ->
                // Write UTF-8 BOM
                fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
                OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                    writer.write(sb.toString())
                    writer.flush()
                }
            }

            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
