package com.example.util

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.data.CategoryEntity
import com.example.data.TransactionEntity
import com.example.data.WalletConfigEntity
import java.io.OutputStream
import kotlin.math.max

/**
 * Generates a clean, multi-page A4 PDF financial statement of transaction history
 * using Android's native `android.graphics.pdf.PdfDocument`.
 */
object PdfReportGenerator {

    private const val PAGE_WIDTH = 595 // A4 width in points
    private const val PAGE_HEIGHT = 842 // A4 height in points
    private const val MARGIN = 32f

    fun writeHistoryPdf(
        outputStream: OutputStream,
        walletConfig: WalletConfigEntity,
        cashTotal: Int,
        categories: List<CategoryEntity>,
        transactions: List<TransactionEntity>,
        periodLabel: String
    ): Boolean {
        val document = PdfDocument()
        try {
            val catMap = categories.associateBy { it.id }
            val expenses = transactions.filter { it.type == "expense" }
            val inflows = transactions.filter { it.type == "inflow" }
            val totalExpense = expenses.sumOf { it.amount }
            val totalInflow = inflows.sumOf { it.amount }
            val cashExpense = expenses.filter { it.method == "cash" }.sumOf { it.amount }
            val bkashExpense = expenses.filter { it.method == "bkash" }.sumOf { it.amount }
            val dbblExpense = expenses.filter { it.method == "dbbl" }.sumOf { it.amount }

            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(15, 23, 42)
                textSize = 18f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(71, 85, 105)
                textSize = 10f
            }
            val headerBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(15, 23, 42)
                style = Paint.Style.FILL
            }
            val cardBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(241, 245, 249)
                style = Paint.Style.FILL
            }
            val cardStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(203, 213, 225)
                style = Paint.Style.STROKE
                strokeWidth = 1f
            }
            val tableHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(30, 41, 59)
                style = Paint.Style.FILL
            }
            val tableHeaderTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 9.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val cellTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(15, 23, 42)
                textSize = 9f
            }
            val cellBoldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(15, 23, 42)
                textSize = 9.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val expenseTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(220, 38, 38)
                textSize = 9.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val inflowTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(5, 150, 105)
                textSize = 9.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val rowAltPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(248, 250, 252)
                style = Paint.Style.FILL
            }
            val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(226, 232, 240)
                strokeWidth = 0.8f
            }

            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            var page = document.startPage(pageInfo)
            var canvas: Canvas = page.canvas
            var y = MARGIN

            // Draw Report Banner on Page 1
            val bannerRect = RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 62f)
            canvas.drawRoundRect(bannerRect, 10f, 10f, headerBgPaint)

            val whiteBold = Paint(titlePaint).apply { color = Color.WHITE; textSize = 16f }
            val cyanSub = Paint(subPaint).apply { color = Color.rgb(103, 232, 249); textSize = 10f }
            canvas.drawText(
                BanglaFormatters.tr(
                    "টাকাভল্ট ৪.০ — লেনদেনের ইতিহাস ও আর্থিক রিপোর্ট",
                    "TakaVault 4.0 — Transaction History & Financial Statement"
                ),
                MARGIN + 14f,
                y + 26f,
                whiteBold
            )
            val nowStr = BanglaFormatters.formatBanglaDateTime(System.currentTimeMillis())
            canvas.drawText(
                "${BanglaFormatters.tr("সময়কাল:", "Period:")} $periodLabel   |   ${BanglaFormatters.tr("তৈরির তারিখ:", "Generated:")} $nowStr",
                MARGIN + 14f,
                y + 46f,
                cyanSub
            )
            y += 74f

            // Summary Row 1: Wallet Balances & Period Overview
            val boxW = (PAGE_WIDTH - MARGIN * 2 - 18f) / 4f
            val summaryItems = listOf(
                BanglaFormatters.tr("মোট খরচ (এই সময়ে)", "Period Expense") to BanglaFormatters.formatBDT(totalExpense),
                BanglaFormatters.tr("মোট জমা (এই সময়ে)", "Period Inflow") to BanglaFormatters.formatBDT(totalInflow),
                BanglaFormatters.tr("বিকাশ / ব্যাংক খরচ", "bKash / DBBL") to "${BanglaFormatters.formatBDT(bkashExpense)} / ${BanglaFormatters.formatBDT(dbblExpense)}",
                BanglaFormatters.tr("নগদ খরচ / ভল্ট", "Cash Spend / Vault") to "${BanglaFormatters.formatBDT(cashExpense)} / ${BanglaFormatters.formatBDT(cashTotal)}"
            )
            summaryItems.forEachIndexed { idx, (label, value) ->
                val left = MARGIN + idx * (boxW + 6f)
                val rect = RectF(left, y, left + boxW, y + 46f)
                canvas.drawRoundRect(rect, 6f, 6f, cardBgPaint)
                canvas.drawRoundRect(rect, 6f, 6f, cardStrokePaint)
                canvas.drawText(label, left + 8f, y + 17f, subPaint)
                canvas.drawText(value, left + 8f, y + 35f, cellBoldPaint)
            }
            y += 58f

            // Table Header helper
            fun drawTableHeader(c: Canvas, topY: Float): Float {
                val rect = RectF(MARGIN, topY, PAGE_WIDTH - MARGIN, topY + 24f)
                c.drawRoundRect(rect, 4f, 4f, tableHeaderPaint)
                c.drawText(BanglaFormatters.tr("তারিখ ও সময়", "Date & Time"), MARGIN + 8f, topY + 15f, tableHeaderTextPaint)
                c.drawText(BanglaFormatters.tr("খাত", "Category"), MARGIN + 115f, topY + 15f, tableHeaderTextPaint)
                c.drawText(BanglaFormatters.tr("মাধ্যম", "Method"), MARGIN + 215f, topY + 15f, tableHeaderTextPaint)
                c.drawText(BanglaFormatters.tr("বিবরণ ও নোট হিসাব", "Memo & Note Breakdown"), MARGIN + 275f, topY + 15f, tableHeaderTextPaint)
                c.drawText(BanglaFormatters.tr("পরিমাণ (৳)", "Amount"), PAGE_WIDTH - MARGIN - 75f, topY + 15f, tableHeaderTextPaint)
                return topY + 26f
            }

            y = drawTableHeader(canvas, y)

            if (transactions.isEmpty()) {
                canvas.drawText(
                    BanglaFormatters.tr(
                        "নির্বাচিত সময়সীমার মধ্যে কোনো লেনদেন পাওয়া যায়নি।",
                        "No transactions found for the selected time period."
                    ),
                    MARGIN + 12f,
                    y + 28f,
                    subPaint
                )
            } else {
                transactions.forEachIndexed { index, tx ->
                    val hasCashNotes = tx.method == "cash" && (tx.tenderedNotes.isNotEmpty() || tx.receivedNotes.isNotEmpty())
                    val rowHeight = if (hasCashNotes) 34f else 22f

                    if (y + rowHeight > PAGE_HEIGHT - 44f) {
                        // Footer on current page
                        canvas.drawText(
                            "Page $pageNumber  •  TakaVault 4.0",
                            PAGE_WIDTH - MARGIN - 110f,
                            PAGE_HEIGHT - 16f,
                            subPaint
                        )
                        document.finishPage(page)
                        pageNumber++
                        pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                        page = document.startPage(pageInfo)
                        canvas = page.canvas
                        y = MARGIN
                        y = drawTableHeader(canvas, y)
                    }

                    if (index % 2 == 1) {
                        canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + rowHeight, rowAltPaint)
                    }

                    val dateStr = BanglaFormatters.formatBanglaDateTime(tx.timestamp)
                    val cat = catMap[tx.categoryId]
                    val catName = (cat?.name ?: if (tx.type == "inflow") BanglaFormatters.tr("তহবিল জমা", "Inflow") else BanglaFormatters.tr("বিবিধ", "Misc")).take(16)
                    val methodLabel = when (tx.method) {
                        "bkash" -> BanglaFormatters.tr("বিকাশ", "bKash")
                        "dbbl" -> BanglaFormatters.tr("ডিবিবিএল", "DBBL")
                        else -> BanglaFormatters.tr("নগদ", "Cash")
                    }
                    val memoText = tx.note.ifEmpty { catName }.take(34)
                    val isInflow = tx.type == "inflow"
                    val amtStr = "${if (isInflow) "+" else "-"}${BanglaFormatters.formatBDT(tx.amount)}"

                    canvas.drawText(dateStr, MARGIN + 8f, y + 14f, cellTextPaint)
                    canvas.drawText(catName, MARGIN + 115f, y + 14f, cellBoldPaint)
                    canvas.drawText(methodLabel, MARGIN + 215f, y + 14f, cellTextPaint)
                    canvas.drawText(memoText, MARGIN + 275f, y + 14f, cellTextPaint)
                    canvas.drawText(
                        amtStr,
                        PAGE_WIDTH - MARGIN - 75f,
                        y + 14f,
                        if (isInflow) inflowTextPaint else expenseTextPaint
                    )

                    if (hasCashNotes) {
                        val givenStr = BanglaFormatters.summarizeNotesInline(tx.tenderedNotes)
                        val gotStr = BanglaFormatters.summarizeNotesInline(tx.receivedNotes)
                        val notesLine = "${BanglaFormatters.tr("দেওয়া:", "Given:")} $givenStr  |  ${BanglaFormatters.tr("ফেরত:", "Change:")} $gotStr"
                        canvas.drawText(notesLine.take(54), MARGIN + 275f, y + 27f, subPaint)
                    }

                    canvas.drawLine(MARGIN, y + rowHeight, PAGE_WIDTH - MARGIN, y + rowHeight, dividerPaint)
                    y += max(rowHeight, 22f)
                }
            }

            // Final Page Footer
            canvas.drawText(
                "${BanglaFormatters.tr("মোট লেনদেন:", "Total Transactions:")} ${BanglaFormatters.toBanglaDigits(transactions.size)}   •   Page $pageNumber",
                MARGIN,
                PAGE_HEIGHT - 16f,
                subPaint
            )

            document.finishPage(page)
            document.writeTo(outputStream)
            outputStream.flush()
            return true
        } catch (_: Exception) {
            return false
        } finally {
            document.close()
        }
    }
}
