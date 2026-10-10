package com.example.data.business.report

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.data.business.BusinessTransaction
import com.example.localization.AppLanguage
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ceil

object BusinessReportJpgExporter {

    private const val IMAGE_WIDTH = 1080
    private const val MARGIN = 40f
    private const val CONTENT_WIDTH = IMAGE_WIDTH - (MARGIN * 2) // 1000f
    private const val MAX_PAGE_HEIGHT = 1920
    private const val FOOTER_HEIGHT = 64f
    private const val ROW_HEIGHT = 46f

    /**
     * Exports a BusinessReportSummary into one or more high-resolution JPG images.
     * Features ToolBox's sleek dark UI, full Bangla & English typography,
     * comprehensive financial metrics, dues, inventory, and paginated transaction ledger.
     */
    fun exportToJpg(
        context: Context,
        report: BusinessReportSummary,
        language: AppLanguage = AppLanguage.BANGLA,
        targetDir: File? = null
    ): Result<List<File>> {
        val outputDir = targetDir ?: File(context.cacheDir, "reports").apply { mkdirs() }
        val sanitizedBizName = report.business.name.replace(Regex("[^a-zA-Z0-9_]"), "_").take(20)
        val timestamp = System.currentTimeMillis()
        val baseFileName = "Report_${sanitizedBizName}_${report.startDate}_to_${report.endDate}_$timestamp"

        val generatedFiles = mutableListOf<File>()

        return try {
            val isBangla = language == AppLanguage.BANGLA
            val currency = report.business.currency

            // Load fonts
            val bengaliTypeface = try {
                ResourcesCompat.getFont(context, R.font.noto_sans_bengali) ?: Typeface.create("sans-serif", Typeface.NORMAL)
            } catch (_: Exception) {
                Typeface.create("sans-serif", Typeface.NORMAL)
            }

            val boldTypeface = try {
                Typeface.create(bengaliTypeface, Typeface.BOLD)
            } catch (_: Exception) {
                Typeface.DEFAULT_BOLD
            }

            // Paints
            val textPaint = Paint().apply {
                isAntiAlias = true
                typeface = bengaliTypeface
                color = Color.parseColor("#E0E0E0")
            }

            val boldPaint = Paint().apply {
                isAntiAlias = true
                typeface = boldTypeface
                color = Color.WHITE
            }

            val fillPaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.FILL
            }

            val strokePaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeWidth = 1f
                color = Color.parseColor("#2D3039")
            }

            val genDateStr = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date(report.generatedAt))
            val transactions = report.transactions

            // Calculate estimated total pages
            val totalPages = calculateTotalPages(transactions.size)

            var currentPage = 1
            var txIndex = 0

            // --- PAGE 1 GENERATION ---
            val page1EstimatedHeight = if (totalPages == 1) {
                val neededHeight = (660f + (transactions.size.coerceAtLeast(1) * ROW_HEIGHT) + FOOTER_HEIGHT + 40f).toInt()
                neededHeight.coerceIn(980, MAX_PAGE_HEIGHT)
            } else {
                MAX_PAGE_HEIGHT
            }

            var currentBitmap = Bitmap.createBitmap(IMAGE_WIDTH, page1EstimatedHeight, Bitmap.Config.ARGB_8888)
            var currentCanvas = Canvas(currentBitmap)
            currentCanvas.drawColor(Color.parseColor("#121316")) // Main Dark Background

            var currentY = MARGIN

            // 1. Header Banner Card
            val headerHeight = 136f
            fillPaint.color = Color.parseColor("#1E2026")
            currentCanvas.drawRoundRect(RectF(MARGIN, currentY, MARGIN + CONTENT_WIDTH, currentY + headerHeight), 16f, 16f, fillPaint)
            strokePaint.color = Color.parseColor("#2D3039")
            currentCanvas.drawRoundRect(RectF(MARGIN, currentY, MARGIN + CONTENT_WIDTH, currentY + headerHeight), 16f, 16f, strokePaint)

            // Red vertical accent strip
            fillPaint.color = Color.parseColor("#E53935")
            currentCanvas.drawRoundRect(RectF(MARGIN, currentY, MARGIN + 8f, currentY + headerHeight), 4f, 4f, fillPaint)

            // Header left side text
            boldPaint.color = Color.parseColor("#FF5252")
            boldPaint.textSize = 17f
            currentCanvas.drawText("ToolBox Business Manager • ${if (isBangla) "ব্যবসা রিপোর্ট" else "Performance Report"}", MARGIN + 22f, currentY + 30f, boldPaint)

            boldPaint.color = Color.WHITE
            boldPaint.textSize = 28f
            currentCanvas.drawText(report.business.name.take(32), MARGIN + 22f, currentY + 68f, boldPaint)

            textPaint.color = Color.parseColor("#B0BEC5")
            textPaint.textSize = 18f
            val bizSub = "${report.business.businessType}${if (report.business.phone.isNotBlank()) " • " + report.business.phone else ""}"
            currentCanvas.drawText(bizSub.take(45), MARGIN + 22f, currentY + 104f, textPaint)

            // Header right side text
            boldPaint.textAlign = Paint.Align.RIGHT
            boldPaint.color = Color.parseColor("#FFD54F")
            boldPaint.textSize = 21f
            currentCanvas.drawText(report.periodLabel.take(24), MARGIN + CONTENT_WIDTH - 18f, currentY + 34f, boldPaint)

            textPaint.textAlign = Paint.Align.RIGHT
            textPaint.textSize = 17f
            textPaint.color = Color.parseColor("#CFD8DC")
            currentCanvas.drawText("${if (isBangla) "সময়কাল:" else "Dates:"} ${report.startDate} to ${report.endDate}", MARGIN + CONTENT_WIDTH - 18f, currentY + 68f, textPaint)

            textPaint.textSize = 15f
            textPaint.color = Color.parseColor("#90A4AE")
            currentCanvas.drawText("${if (isBangla) "তৈরি:" else "Generated:"} $genDateStr", MARGIN + CONTENT_WIDTH - 18f, currentY + 102f, textPaint)

            boldPaint.textAlign = Paint.Align.LEFT
            textPaint.textAlign = Paint.Align.LEFT

            currentY += headerHeight + 14f

            // 2. Net Profit / Loss Highlight Card
            val isProfit = report.netProfit >= 0
            val netCardHeight = 92f
            fillPaint.color = if (isProfit) Color.parseColor("#18331F") else Color.parseColor("#381B1B")
            val netRect = RectF(MARGIN, currentY, MARGIN + CONTENT_WIDTH, currentY + netCardHeight)
            currentCanvas.drawRoundRect(netRect, 14f, 14f, fillPaint)

            strokePaint.color = if (isProfit) Color.parseColor("#2E7D32") else Color.parseColor("#C62828")
            currentCanvas.drawRoundRect(netRect, 14f, 14f, strokePaint)

            fillPaint.color = if (isProfit) Color.parseColor("#4CAF50") else Color.parseColor("#E53935")
            currentCanvas.drawRoundRect(RectF(MARGIN, currentY, MARGIN + 6f, currentY + netCardHeight), 3f, 3f, fillPaint)

            boldPaint.color = if (isProfit) Color.parseColor("#81C784") else Color.parseColor("#E57373")
            boldPaint.textSize = 21f
            val netTitle = if (isProfit) {
                if (isBangla) "নিট লাভ (Net Profit):" else "Net Profit:"
            } else {
                if (isBangla) "নিট ক্ষতি (Net Loss):" else "Net Loss:"
            }
            currentCanvas.drawText(netTitle, MARGIN + 20f, currentY + 38f, boldPaint)

            textPaint.color = if (isProfit) Color.parseColor("#A5D6A7") else Color.parseColor("#EF9A9A")
            textPaint.textSize = 15f
            val netSub = if (isProfit) {
                if (isBangla) "বিক্রি ও আয় থেকে ক্রয় ও খরচ বাদ দেওয়ার পর প্রকৃত লাভ" else "Net earnings after deducting all purchases & expenses"
            } else {
                if (isBangla) "ক্রয় ও মোট ব্যয়ের তুলনায় বিক্রয় আয়ের ঘাটতি" else "Total purchases and expenses exceeded sales revenue"
            }
            currentCanvas.drawText(netSub, MARGIN + 20f, currentY + 68f, textPaint)

            boldPaint.textAlign = Paint.Align.RIGHT
            boldPaint.color = if (isProfit) Color.parseColor("#4CAF50") else Color.parseColor("#EF5350")
            boldPaint.textSize = 28f
            val formattedNet = "$currency ${formatMoney(Math.abs(report.netProfit))}"
            currentCanvas.drawText(formattedNet, MARGIN + CONTENT_WIDTH - 20f, currentY + 54f, boldPaint)
            boldPaint.textAlign = Paint.Align.LEFT

            currentY += netCardHeight + 14f

            // 3. Key Financial Metrics Grid (3 cols x 2 rows)
            val cardW = (CONTENT_WIDTH - 24f) / 3f
            val cardH = 82f

            val metrics = listOf(
                Triple(if (isBangla) "মোট বিক্রি (Sales)" else "Total Sales", "$currency ${formatMoney(report.totalSales)}", Color.parseColor("#4CAF50")),
                Triple(if (isBangla) "মোট ক্রয় (Purchases)" else "Purchases", "$currency ${formatMoney(report.totalPurchases)}", Color.parseColor("#FF9800")),
                Triple(if (isBangla) "মোট খরচ (Expenses)" else "Expenses", "$currency ${formatMoney(report.totalExpenses)}", Color.parseColor("#F44336")),
                Triple(if (isBangla) "কাস্টমার বাকি (Customer Due)" else "Customer Dues", "$currency ${formatMoney(report.totalCustomerOutstandingDues)}", Color.parseColor("#EF5350")),
                Triple(if (isBangla) "মহাজন দেনা (Supplier Payable)" else "Supplier Payables", "$currency ${formatMoney(report.totalSupplierOutstandingPayables)}", Color.parseColor("#FFA726")),
                Triple(if (isBangla) "নগদ বিক্রি আদায় (Cash In)" else "Sales Cash In", "$currency ${formatMoney(report.salesCashReceived)}", Color.parseColor("#42A5F5"))
            )

            for (i in metrics.indices) {
                val col = i % 3
                val row = i / 3
                val rx = MARGIN + (col * (cardW + 12f))
                val ry = currentY + (row * (cardH + 10f))

                fillPaint.color = Color.parseColor("#1E2026")
                currentCanvas.drawRoundRect(RectF(rx, ry, rx + cardW, ry + cardH), 12f, 12f, fillPaint)
                strokePaint.color = Color.parseColor("#2D3039")
                currentCanvas.drawRoundRect(RectF(rx, ry, rx + cardW, ry + cardH), 12f, 12f, strokePaint)

                textPaint.color = Color.parseColor("#9E9E9E")
                textPaint.textSize = 15f
                currentCanvas.drawText(metrics[i].first, rx + 14f, ry + 28f, textPaint)

                boldPaint.color = metrics[i].third
                boldPaint.textSize = 20f
                currentCanvas.drawText(metrics[i].second, rx + 14f, ry + 60f, boldPaint)
            }

            currentY += (2 * (cardH + 10f)) + 12f

            // 4. Secondary Strips (Gross Profit, Inventory, Cash Flow)
            val stripHeight = 44f
            fillPaint.color = Color.parseColor("#1B1D24")
            strokePaint.color = Color.parseColor("#2D3039")

            // Strip A: Gross Profit & Inventory
            val stripARect = RectF(MARGIN, currentY, MARGIN + CONTENT_WIDTH, currentY + stripHeight)
            currentCanvas.drawRoundRect(stripARect, 10f, 10f, fillPaint)
            currentCanvas.drawRoundRect(stripARect, 10f, 10f, strokePaint)

            boldPaint.color = Color.parseColor("#81C784")
            boldPaint.textSize = 16f
            currentCanvas.drawText("${if (isBangla) "মোট লাভ (Gross Profit):" else "Gross Profit:"} $currency ${formatMoney(report.grossProfit)}", MARGIN + 16f, currentY + 28f, boldPaint)

            textPaint.textAlign = Paint.Align.RIGHT
            textPaint.color = Color.parseColor("#CFD8DC")
            textPaint.textSize = 15f
            val invText = "${if (isBangla) "ইনভেন্টরি পণ্য:" else "Inventory:"} ${report.totalProductsCount} • ${if (isBangla) "মজুদ মূল্য:" else "Stock Value:"} $currency ${formatMoney(report.totalStockValuation)}"
            currentCanvas.drawText(invText, MARGIN + CONTENT_WIDTH - 16f, currentY + 28f, textPaint)
            textPaint.textAlign = Paint.Align.LEFT

            currentY += stripHeight + 8f

            // Strip B: Customer Payments In & Supplier Payments Out
            val stripBRect = RectF(MARGIN, currentY, MARGIN + CONTENT_WIDTH, currentY + stripHeight)
            currentCanvas.drawRoundRect(stripBRect, 10f, 10f, fillPaint)
            currentCanvas.drawRoundRect(stripBRect, 10f, 10f, strokePaint)

            textPaint.color = Color.parseColor("#90CAF9")
            textPaint.textSize = 15f
            currentCanvas.drawText("${if (isBangla) "গ্রাহক বাকি আদায়:" else "Customer Payments In:"} $currency ${formatMoney(report.customerPaymentsReceived)}", MARGIN + 16f, currentY + 28f, textPaint)

            textPaint.textAlign = Paint.Align.RIGHT
            textPaint.color = Color.parseColor("#CE93D8")
            currentCanvas.drawText("${if (isBangla) "মহাজন দেনা পরিশোধ:" else "Supplier Payments Out:"} $currency ${formatMoney(report.supplierPaymentsMade)}", MARGIN + CONTENT_WIDTH - 16f, currentY + 28f, textPaint)
            textPaint.textAlign = Paint.Align.LEFT

            currentY += stripHeight + 16f

            // 5. Transaction Ledger Section Title
            boldPaint.color = Color.WHITE
            boldPaint.textSize = 21f
            val ledgerTitle = if (isBangla) "বিস্তারিত লেনদেন খতিয়ান (${transactions.size} টি)" else "Transaction Ledger (${transactions.size} entries)"
            currentCanvas.drawText(ledgerTitle, MARGIN, currentY, boldPaint)

            currentY += 12f

            // Table Header Bar
            drawTableHeader(currentCanvas, currentY, currency, isBangla, boldPaint, fillPaint)
            currentY += 44f

            // Table Content
            if (transactions.isEmpty()) {
                currentY += 8f
                fillPaint.color = Color.parseColor("#1A1B20")
                val emptyRect = RectF(MARGIN, currentY, MARGIN + CONTENT_WIDTH, currentY + 70f)
                currentCanvas.drawRoundRect(emptyRect, 12f, 12f, fillPaint)
                strokePaint.color = Color.parseColor("#2D3039")
                currentCanvas.drawRoundRect(emptyRect, 12f, 12f, strokePaint)

                textPaint.color = Color.parseColor("#9E9E9E")
                textPaint.textSize = 17f
                textPaint.textAlign = Paint.Align.CENTER
                val emptyMsg = if (isBangla) "এই নির্বাচিত সময়কালে কোনো লেনদেন রেকর্ড নেই।" else "No transactions recorded for this period."
                currentCanvas.drawText(emptyMsg, MARGIN + (CONTENT_WIDTH / 2f), currentY + 40f, textPaint)
                textPaint.textAlign = Paint.Align.LEFT

                currentY += 70f
            } else {
                while (txIndex < transactions.size) {
                    val tx = transactions[txIndex]

                    // Check if current row fits on current page
                    val availableSpace = if (currentPage == 1) page1EstimatedHeight else MAX_PAGE_HEIGHT
                    if (currentY + ROW_HEIGHT + FOOTER_HEIGHT + 20f > availableSpace) {
                        // Finish current page
                        drawFooter(currentCanvas, currentPage, totalPages, genDateStr, isBangla, textPaint, strokePaint, availableSpace.toFloat())
                        val pageFile = saveBitmapToJpg(currentBitmap, outputDir, baseFileName, currentPage, totalPages)
                        generatedFiles.add(pageFile)
                        currentBitmap.recycle()

                        // Prepare next page
                        currentPage++
                        currentBitmap = Bitmap.createBitmap(IMAGE_WIDTH, MAX_PAGE_HEIGHT, Bitmap.Config.ARGB_8888)
                        currentCanvas = Canvas(currentBitmap)
                        currentCanvas.drawColor(Color.parseColor("#121316"))

                        currentY = MARGIN

                        // Draw compact page header on subsequent pages
                        drawSubsequentPageHeader(currentCanvas, currentY, report, currentPage, totalPages, isBangla, boldPaint, textPaint, fillPaint, strokePaint)
                        currentY += 88f

                        // Draw Table Header
                        drawTableHeader(currentCanvas, currentY, currency, isBangla, boldPaint, fillPaint)
                        currentY += 44f
                    }

                    // Draw Transaction Row
                    drawTransactionRow(currentCanvas, currentY, tx, txIndex, isBangla, boldPaint, textPaint, fillPaint, strokePaint)
                    currentY += ROW_HEIGHT
                    txIndex++
                }
            }

            // Finish the final page
            val finalAvailableSpace = if (currentPage == 1) page1EstimatedHeight else MAX_PAGE_HEIGHT
            drawFooter(currentCanvas, currentPage, totalPages, genDateStr, isBangla, textPaint, strokePaint, finalAvailableSpace.toFloat())
            val finalPageFile = saveBitmapToJpg(currentBitmap, outputDir, baseFileName, currentPage, totalPages)
            generatedFiles.add(finalPageFile)
            currentBitmap.recycle()

            Result.success(generatedFiles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun drawTableHeader(
        canvas: Canvas,
        y: Float,
        currency: String,
        isBangla: Boolean,
        boldPaint: Paint,
        fillPaint: Paint
    ) {
        val tableHeaderHeight = 44f
        fillPaint.color = Color.parseColor("#262933")
        canvas.drawRoundRect(RectF(MARGIN, y, MARGIN + CONTENT_WIDTH, y + tableHeaderHeight), 8f, 8f, fillPaint)

        boldPaint.color = Color.parseColor("#CFD8DC")
        boldPaint.textSize = 15f

        val colDateX = MARGIN + 14f
        val colTypeX = MARGIN + 145f
        val colTitleX = MARGIN + 275f
        val colPartyX = MARGIN + 535f
        val colAmountX = MARGIN + CONTENT_WIDTH - 225f
        val colPaidX = MARGIN + CONTENT_WIDTH - 115f
        val colDueX = MARGIN + CONTENT_WIDTH - 14f

        canvas.drawText(if (isBangla) "তারিখ" else "Date", colDateX, y + 28f, boldPaint)
        canvas.drawText(if (isBangla) "ধরন" else "Type", colTypeX, y + 28f, boldPaint)
        canvas.drawText(if (isBangla) "বিবরণ" else "Description", colTitleX, y + 28f, boldPaint)
        canvas.drawText(if (isBangla) "পক্ষ" else "Party", colPartyX, y + 28f, boldPaint)

        boldPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText(if (isBangla) "টাকা ($currency)" else "Amount", colAmountX, y + 28f, boldPaint)
        canvas.drawText(if (isBangla) "জমা" else "Paid", colPaidX, y + 28f, boldPaint)
        canvas.drawText(if (isBangla) "বাকি" else "Due", colDueX, y + 28f, boldPaint)
        boldPaint.textAlign = Paint.Align.LEFT
    }

    private fun drawTransactionRow(
        canvas: Canvas,
        y: Float,
        tx: BusinessTransaction,
        index: Int,
        isBangla: Boolean,
        boldPaint: Paint,
        textPaint: Paint,
        fillPaint: Paint,
        strokePaint: Paint
    ) {
        // Zebra striping
        fillPaint.color = if (index % 2 == 0) Color.parseColor("#16171D") else Color.parseColor("#1C1E25")
        canvas.drawRect(RectF(MARGIN, y, MARGIN + CONTENT_WIDTH, y + ROW_HEIGHT), fillPaint)

        // Bottom border
        strokePaint.color = Color.parseColor("#262832")
        canvas.drawLine(MARGIN, y + ROW_HEIGHT, MARGIN + CONTENT_WIDTH, y + ROW_HEIGHT, strokePaint)

        val colDateX = MARGIN + 14f
        val colTypeX = MARGIN + 145f
        val colTitleX = MARGIN + 275f
        val colPartyX = MARGIN + 535f
        val colAmountX = MARGIN + CONTENT_WIDTH - 225f
        val colPaidX = MARGIN + CONTENT_WIDTH - 115f
        val colDueX = MARGIN + CONTENT_WIDTH - 14f

        // Date
        textPaint.textSize = 15f
        textPaint.color = Color.parseColor("#B0BEC5")
        canvas.drawText(tx.dateString, colDateX, y + 29f, textPaint)

        // Type badge
        val (typeLabel, typeColor, badgeBg) = when (tx.type) {
            "SALE" -> Triple(if (isBangla) "বিক্রি" else "Sale", Color.parseColor("#4CAF50"), Color.parseColor("#1B3822"))
            "PURCHASE" -> Triple(if (isBangla) "ক্রয়" else "Purchase", Color.parseColor("#FF9800"), Color.parseColor("#382A18"))
            "EXPENSE" -> Triple(if (isBangla) "খরচ" else "Expense", Color.parseColor("#EF5350"), Color.parseColor("#381B1B"))
            "CUSTOMER_PAYMENT" -> Triple(if (isBangla) "আদায়" else "Payment", Color.parseColor("#42A5F5"), Color.parseColor("#182938"))
            "SUPPLIER_PAYMENT" -> Triple(if (isBangla) "পরিশোধ" else "Payment", Color.parseColor("#AB47BC"), Color.parseColor("#2E1838"))
            else -> Triple(tx.type, Color.parseColor("#9E9E9E"), Color.parseColor("#262933"))
        }

        fillPaint.color = badgeBg
        canvas.drawRoundRect(RectF(colTypeX, y + 10f, colTypeX + 110f, y + 36f), 6f, 6f, fillPaint)
        boldPaint.color = typeColor
        boldPaint.textSize = 14f
        canvas.drawText(typeLabel, colTypeX + 12f, y + 28f, boldPaint)

        // Description / Title
        textPaint.color = Color.WHITE
        textPaint.textSize = 15.5f
        canvas.drawText(tx.title.take(24), colTitleX, y + 29f, textPaint)

        // Party
        textPaint.color = Color.parseColor("#90A4AE")
        textPaint.textSize = 14.5f
        canvas.drawText(tx.partyName.take(18), colPartyX, y + 29f, textPaint)

        // Amounts (right-aligned)
        boldPaint.textAlign = Paint.Align.RIGHT
        boldPaint.color = typeColor
        boldPaint.textSize = 16f
        canvas.drawText(formatMoney(tx.amount), colAmountX, y + 29f, boldPaint)

        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.color = Color.parseColor("#CFD8DC")
        textPaint.textSize = 15f
        canvas.drawText(formatMoney(tx.paidAmount), colPaidX, y + 29f, textPaint)

        if (tx.dueAmount > 0) {
            boldPaint.color = Color.parseColor("#EF5350")
            boldPaint.textSize = 15f
            canvas.drawText(formatMoney(tx.dueAmount), colDueX, y + 29f, boldPaint)
        } else {
            textPaint.color = Color.parseColor("#757575")
            canvas.drawText("-", colDueX, y + 29f, textPaint)
        }

        boldPaint.textAlign = Paint.Align.LEFT
        textPaint.textAlign = Paint.Align.LEFT
    }

    private fun drawSubsequentPageHeader(
        canvas: Canvas,
        y: Float,
        report: BusinessReportSummary,
        page: Int,
        totalPages: Int,
        isBangla: Boolean,
        boldPaint: Paint,
        textPaint: Paint,
        fillPaint: Paint,
        strokePaint: Paint
    ) {
        val hHeight = 74f
        fillPaint.color = Color.parseColor("#1E2026")
        canvas.drawRoundRect(RectF(MARGIN, y, MARGIN + CONTENT_WIDTH, y + hHeight), 12f, 12f, fillPaint)
        strokePaint.color = Color.parseColor("#2D3039")
        canvas.drawRoundRect(RectF(MARGIN, y, MARGIN + CONTENT_WIDTH, y + hHeight), 12f, 12f, strokePaint)

        fillPaint.color = Color.parseColor("#E53935")
        canvas.drawRoundRect(RectF(MARGIN, y, MARGIN + 6f, y + hHeight), 3f, 3f, fillPaint)

        boldPaint.color = Color.WHITE
        boldPaint.textSize = 21f
        canvas.drawText("${report.business.name} • ${report.periodLabel}", MARGIN + 18f, y + 34f, boldPaint)

        textPaint.color = Color.parseColor("#B0BEC5")
        textPaint.textSize = 15f
        canvas.drawText("${report.startDate} to ${report.endDate}", MARGIN + 18f, y + 58f, textPaint)

        boldPaint.textAlign = Paint.Align.RIGHT
        boldPaint.color = Color.parseColor("#FFD54F")
        boldPaint.textSize = 18f
        val pageStr = if (isBangla) "পৃষ্ঠা $page / $totalPages" else "Page $page of $totalPages"
        canvas.drawText(pageStr, MARGIN + CONTENT_WIDTH - 18f, y + 44f, boldPaint)
        boldPaint.textAlign = Paint.Align.LEFT
    }

    private fun drawFooter(
        canvas: Canvas,
        page: Int,
        totalPages: Int,
        genDateStr: String,
        isBangla: Boolean,
        textPaint: Paint,
        strokePaint: Paint,
        canvasHeight: Float
    ) {
        val footerY = canvasHeight - MARGIN - 16f
        strokePaint.color = Color.parseColor("#2D3039")
        canvas.drawLine(MARGIN, footerY - 20f, MARGIN + CONTENT_WIDTH, footerY - 20f, strokePaint)

        textPaint.color = Color.parseColor("#78909C")
        textPaint.textSize = 14f
        val brand = if (isBangla) "ToolBox Business Manager • নিরাপদ ব্যবসায়িক খতিয়ান" else "ToolBox Business Manager • Verified Performance Ledger"
        canvas.drawText(brand, MARGIN, footerY, textPaint)

        textPaint.textAlign = Paint.Align.RIGHT
        val pageStr = if (isBangla) "পৃষ্ঠা $page / $totalPages" else "Page $page of $totalPages"
        canvas.drawText(pageStr, MARGIN + CONTENT_WIDTH, footerY, textPaint)
        textPaint.textAlign = Paint.Align.LEFT
    }

    private fun saveBitmapToJpg(
        bitmap: Bitmap,
        dir: File,
        baseName: String,
        pageIndex: Int,
        totalPages: Int
    ): File {
        val fileName = if (totalPages == 1) {
            "$baseName.jpg"
        } else {
            "${baseName}_page_$pageIndex.jpg"
        }
        val file = File(dir, fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }
        return file
    }

    private fun calculateTotalPages(txCount: Int): Int {
        if (txCount <= 18) return 1
        val remaining = txCount - 18
        val subsequentPageCapacity = 28
        return 1 + ceil(remaining.toDouble() / subsequentPageCapacity).toInt()
    }

    private fun formatMoney(amount: Double): String {
        return String.format(Locale.US, "%,.2f", amount)
    }
}
