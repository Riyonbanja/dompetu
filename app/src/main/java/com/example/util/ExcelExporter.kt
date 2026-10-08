package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.model.MonthlyFinanceSummary
import com.example.model.TransactionEntity
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

object ExcelExporter {

    fun generateAndShareExcel(
        context: Context,
        summary: MonthlyFinanceSummary,
        transactions: List<TransactionEntity>
    ): File? {
        val file = createExcelFile(context, summary, transactions) ?: return null
        shareExcel(context, file)
        return file
    }

    fun createExcelFile(
        context: Context,
        summary: MonthlyFinanceSummary,
        transactions: List<TransactionEntity>
    ): File? {
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) {
            reportsDir.mkdirs()
        }

        val file = File(reportsDir, "Laporan_DompetKu_${summary.monthString}.csv")

        return try {
            val fos = FileOutputStream(file)
            // Write UTF-8 BOM so Microsoft Excel correctly recognises UTF-8 encoding
            fos.write(0xEF)
            fos.write(0xBB)
            fos.write(0xBF)

            val writer = OutputStreamWriter(fos, StandardCharsets.UTF_8)

            fun escapeCsv(value: String): String {
                val clean = value.replace("\"", "\"\"")
                return if (clean.contains(",") || clean.contains("\"") || clean.contains("\n") || clean.contains(";")) {
                    "\"$clean\""
                } else {
                    clean
                }
            }

            // Title and Info
            writer.append("LAPORAN KEUANGAN BULANAN - DOMPETKU\n")
            writer.append("Periode,${escapeCsv(summary.displayMonth)}\n")
            writer.append("Tanggal Ekspor,${escapeCsv(FormatUtils.formatDateTime(System.currentTimeMillis()))}\n")
            writer.append("\n")

            // Ringkasan Finansial
            writer.append("RINGKASAN FINANSIAL\n")
            writer.append("Total Pemasukan,${summary.totalIncome.toLong()},${escapeCsv(FormatUtils.formatRupiah(summary.totalIncome))}\n")
            writer.append("Total Pengeluaran,${summary.totalExpense.toLong()},${escapeCsv(FormatUtils.formatRupiah(summary.totalExpense))}\n")
            writer.append("Saldo Bersih,${summary.netBalance.toLong()},${escapeCsv(FormatUtils.formatRupiah(summary.netBalance))}\n")
            writer.append("\n")

            // Ringkasan Kategori Belanja
            if (summary.categoryBreakdown.isNotEmpty()) {
                writer.append("RINGKASAN PENGELUARAN PER KATEGORI\n")
                writer.append("Kategori,Jumlah Transaksi,Total Nominal (Rp),Persentase (%)\n")
                val totalExp = if (summary.totalExpense > 0) summary.totalExpense else 1.0
                summary.categoryBreakdown.forEach { cat ->
                    val pct = (cat.totalAmount / totalExp) * 100.0
                    val pctStr = String.format(java.util.Locale.US, "%.1f%%", pct)
                    writer.append("${escapeCsv(cat.categoryName)},${cat.transactionCount},${cat.totalAmount.toLong()},$pctStr\n")
                }
                writer.append("\n")
            }

            // Detail Transaksi
            writer.append("RINCIAN TRANSAKSI\n")
            writer.append("No,Tanggal,Waktu,Tipe Transaksi,Kategori,Catatan / Keterangan,Metode Pembayaran,Nominal,Format Rupiah\n")

            transactions.forEachIndexed { index, tx ->
                val dateStr = FormatUtils.formatDate(tx.timestamp)
                val timeStr = FormatUtils.formatTime(tx.timestamp)
                val typeStr = if (tx.type == "INCOME") "Pemasukan" else "Pengeluaran"
                val amountVal = if (tx.type == "INCOME") tx.amount.toLong() else -tx.amount.toLong()
                val rupiahStr = FormatUtils.formatRupiah(tx.amount)

                writer.append("${index + 1},")
                writer.append("${escapeCsv(dateStr)},")
                writer.append("${escapeCsv(timeStr)},")
                writer.append("${escapeCsv(typeStr)},")
                writer.append("${escapeCsv(tx.categoryName)},")
                writer.append("${escapeCsv(tx.note.ifEmpty { "-" })},")
                writer.append("${escapeCsv(tx.paymentMethod)},")
                writer.append("$amountVal,")
                writer.append("${escapeCsv(rupiahStr)}\n")
            }

            writer.flush()
            writer.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun shareExcel(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Laporan Excel DompetKu - ${file.name}")
            putExtra(
                Intent.EXTRA_TEXT,
                "Berikut adalah file laporan Excel/Spreadsheet keuangan yang diekspor dari aplikasi DompetKu."
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Buka atau Bagikan File Excel/CSV")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
