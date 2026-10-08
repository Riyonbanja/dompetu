package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.model.CategorySummary
import com.example.model.MonthlyFinanceSummary
import com.example.model.TransactionEntity
import java.io.File
import java.io.FileOutputStream

object PdfExporter {

    fun generateAndSharePdf(
        context: Context,
        summary: MonthlyFinanceSummary,
        transactions: List<TransactionEntity>
    ): File? {
        val file = createPdfFile(context, summary, transactions) ?: return null
        sharePdf(context, file)
        return file
    }

    fun createPdfFile(
        context: Context,
        summary: MonthlyFinanceSummary,
        transactions: List<TransactionEntity>
    ): File? {
        val doc = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842

        val titlePaint = Paint().apply {
            color = Color.rgb(15, 118, 110) // Emerald primary
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val subTitlePaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 10f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }

        val headerPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = Color.rgb(51, 65, 85)
            textSize = 9.5f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }

        val boldBodyPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val greenPaint = Paint().apply {
            color = Color.rgb(16, 185, 129)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val redPaint = Paint().apply {
            color = Color.rgb(239, 68, 68)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            strokeWidth = 1f
        }

        val bgPaint = Paint().apply {
            style = Paint.Style.FILL
        }

        var currentPageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
        var page = doc.startPage(pageInfo)
        var canvas = page.canvas

        var y = 40f

        // Top Brand Header Banner
        bgPaint.color = Color.rgb(240, 253, 250) // Soft teal tint
        canvas.drawRect(30f, y, (pageWidth - 30).toFloat(), y + 60f, bgPaint)

        // Accent border bar
        bgPaint.color = Color.rgb(15, 118, 110)
        canvas.drawRect(30f, y, 36f, y + 60f, bgPaint)

        canvas.drawText("DOMPETKU - LAPORAN KEUANGAN BULANAN", 48f, y + 25f, titlePaint)
        canvas.drawText(
            "Periode: ${summary.displayMonth}  |  Dicetak pada: ${FormatUtils.formatDateTime(System.currentTimeMillis())}",
            48f,
            y + 45f,
            subTitlePaint
        )

        y += 80f

        // Financial Summary Box Cards
        val boxWidth = (pageWidth - 60 - 20) / 3f
        val boxHeight = 55f

        // 1. Pemasukan Box
        drawSummaryCard(
            canvas, 30f, y, boxWidth, boxHeight,
            "Total Pemasukan", FormatUtils.formatRupiah(summary.totalIncome),
            Color.rgb(240, 253, 244), Color.rgb(22, 163, 74)
        )

        // 2. Pengeluaran Box
        drawSummaryCard(
            canvas, 30f + boxWidth + 10f, y, boxWidth, boxHeight,
            "Total Pengeluaran", FormatUtils.formatRupiah(summary.totalExpense),
            Color.rgb(254, 242, 242), Color.rgb(220, 38, 38)
        )

        // 3. Saldo Bersih Box
        val netColor = if (summary.netBalance >= 0) Color.rgb(15, 118, 110) else Color.rgb(220, 38, 38)
        val netBg = if (summary.netBalance >= 0) Color.rgb(240, 253, 250) else Color.rgb(254, 242, 242)
        drawSummaryCard(
            canvas, 30f + (boxWidth + 10f) * 2, y, boxWidth, boxHeight,
            "Sisa / Saldo Bersih", FormatUtils.formatRupiah(summary.netBalance),
            netBg, netColor
        )

        y += boxHeight + 25f

        // Top Expense Category Breakdown (mini table)
        if (summary.categoryBreakdown.isNotEmpty()) {
            canvas.drawText("Ringkasan Belanja per Kategori", 30f, y, headerPaint)
            y += 12f
            canvas.drawLine(30f, y, (pageWidth - 30).toFloat(), y, linePaint)
            y += 15f

            // Table headers
            canvas.drawText("Kategori", 35f, y, boldBodyPaint)
            canvas.drawText("Jumlah Transaksi", 220f, y, boldBodyPaint)
            canvas.drawText("Total Nominal", 360f, y, boldBodyPaint)
            canvas.drawText("% Pengeluaran", 470f, y, boldBodyPaint)

            y += 6f
            canvas.drawLine(30f, y, (pageWidth - 30).toFloat(), y, linePaint)
            y += 14f

            val totalExp = if (summary.totalExpense > 0) summary.totalExpense else 1.0
            summary.categoryBreakdown.take(6).forEach { cat ->
                val pct = (cat.totalAmount / totalExp) * 100.0
                canvas.drawText(cat.categoryName, 35f, y, bodyPaint)
                canvas.drawText("${cat.transactionCount} kali", 220f, y, bodyPaint)
                canvas.drawText(FormatUtils.formatRupiah(cat.totalAmount), 360f, y, boldBodyPaint)
                canvas.drawText(String.format(java.util.Locale.US, "%.1f%%", pct), 470f, y, bodyPaint)
                y += 15f
            }
            y += 15f
        }

        // Full Transactions List
        canvas.drawText("Rincian Transaksi (${transactions.size} Transaksi)", 30f, y, headerPaint)
        y += 12f
        canvas.drawLine(30f, y, (pageWidth - 30).toFloat(), y, linePaint)
        y += 15f

        // Table Header
        bgPaint.color = Color.rgb(241, 245, 249)
        canvas.drawRect(30f, y - 10f, (pageWidth - 30).toFloat(), y + 8f, bgPaint)

        canvas.drawText("No", 35f, y, boldBodyPaint)
        canvas.drawText("Tanggal", 60f, y, boldBodyPaint)
        canvas.drawText("Kategori", 130f, y, boldBodyPaint)
        canvas.drawText("Catatan / Keterangan", 230f, y, boldBodyPaint)
        canvas.drawText("Metode", 390f, y, boldBodyPaint)
        canvas.drawText("Nominal (Rp)", 460f, y, boldBodyPaint)

        y += 16f

        // Transactions rows
        transactions.forEachIndexed { index, tx ->
            if (y > pageHeight - 50) {
                // End current page & start new page
                doc.finishPage(page)
                currentPageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
                page = doc.startPage(pageInfo)
                canvas = page.canvas

                y = 40f
                canvas.drawText("Rincian Transaksi (Lanjutan) - Halaman $currentPageNumber", 30f, y, headerPaint)
                y += 12f
                canvas.drawLine(30f, y, (pageWidth - 30).toFloat(), y, linePaint)
                y += 15f

                // Table Header repeat
                bgPaint.color = Color.rgb(241, 245, 249)
                canvas.drawRect(30f, y - 10f, (pageWidth - 30).toFloat(), y + 8f, bgPaint)

                canvas.drawText("No", 35f, y, boldBodyPaint)
                canvas.drawText("Tanggal", 60f, y, boldBodyPaint)
                canvas.drawText("Kategori", 130f, y, boldBodyPaint)
                canvas.drawText("Catatan / Keterangan", 230f, y, boldBodyPaint)
                canvas.drawText("Metode", 390f, y, boldBodyPaint)
                canvas.drawText("Nominal (Rp)", 460f, y, boldBodyPaint)
                y += 16f
            }

            // Alternating row background
            if (index % 2 == 1) {
                bgPaint.color = Color.rgb(248, 250, 252)
                canvas.drawRect(30f, y - 9f, (pageWidth - 30).toFloat(), y + 7f, bgPaint)
            }

            canvas.drawText("${index + 1}", 35f, y, bodyPaint)
            canvas.drawText(FormatUtils.formatDate(tx.timestamp), 60f, y, bodyPaint)
            canvas.drawText(tx.categoryName.take(16), 130f, y, bodyPaint)

            val noteTruncated = if (tx.note.length > 25) tx.note.take(23) + "..." else tx.note
            canvas.drawText(noteTruncated.ifEmpty { "-" }, 230f, y, bodyPaint)
            canvas.drawText(tx.paymentMethod.take(12), 390f, y, bodyPaint)

            val isIncome = tx.type == "INCOME"
            val sign = if (isIncome) "+" else "-"
            val amountStr = "$sign ${FormatUtils.formatRupiah(tx.amount)}"
            val paintToUse = if (isIncome) greenPaint else redPaint
            canvas.drawText(amountStr, 460f, y, paintToUse)

            y += 16f
        }

        // Footer on last page
        y += 15f
        if (y < pageHeight - 30) {
            canvas.drawLine(30f, y, (pageWidth - 30).toFloat(), y, linePaint)
            y += 12f
            canvas.drawText(
                "Laporan digenerate otomatis oleh DompetKu • Solusi Cerdas Kelola Keuangan Pribadi",
                30f,
                y,
                subTitlePaint
            )
        }

        doc.finishPage(page)

        // Save to file
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) {
            reportsDir.mkdirs()
        }
        val file = File(reportsDir, "Laporan_DompetKu_${summary.monthString}.pdf")
        return try {
            val fos = FileOutputStream(file)
            doc.writeTo(fos)
            fos.close()
            doc.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            doc.close()
            null
        }
    }

    private fun drawSummaryCard(
        canvas: Canvas,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        title: String,
        amount: String,
        bgColor: Int,
        textColor: Int
    ) {
        val bgPaint = Paint().apply {
            color = bgColor
            style = Paint.Style.FILL
        }
        val borderPaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val textTitlePaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 8.5f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }
        val amountPaint = Paint().apply {
            color = textColor
            textSize = 10.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        canvas.drawRoundRect(x, y, x + width, y + height, 4f, 4f, bgPaint)
        canvas.drawRoundRect(x, y, x + width, y + height, 4f, 4f, borderPaint)

        canvas.drawText(title, x + 10f, y + 20f, textTitlePaint)
        canvas.drawText(amount, x + 10f, y + 40f, amountPaint)
    }

    fun sharePdf(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Laporan Keuangan DompetKu - ${file.name}")
            putExtra(
                Intent.EXTRA_TEXT,
                "Berikut adalah laporan keuangan bulanan yang diekspor dari aplikasi DompetKu."
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Bagikan Laporan PDF")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
