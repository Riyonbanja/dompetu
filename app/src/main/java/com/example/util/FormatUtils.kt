package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object FormatUtils {
    private val localeId = Locale("id", "ID")
    private val rupiahFormat: NumberFormat = NumberFormat.getCurrencyInstance(localeId).apply {
        maximumFractionDigits = 0
    }

    private val numberFormat: NumberFormat = NumberFormat.getNumberInstance(localeId).apply {
        maximumFractionDigits = 0
    }

    fun formatRupiah(amount: Double): String {
        return try {
            val formatted = rupiahFormat.format(amount)
            // Replace "Rp" with "Rp " for standard Indonesian spacing
            if (formatted.startsWith("Rp") && !formatted.startsWith("Rp ")) {
                formatted.replaceFirst("Rp", "Rp ")
            } else {
                formatted
            }
        } catch (_: Exception) {
            "Rp " + numberFormat.format(amount)
        }
    }

    fun formatNumber(amount: Double): String {
        return numberFormat.format(amount)
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", localeId)
        return sdf.format(Date(timestamp))
    }

    fun formatDateWithDay(timestamp: Long): String {
        val sdf = SimpleDateFormat("EEEE, dd MMM yyyy", localeId)
        return sdf.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", localeId)
        return sdf.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", localeId)
        return sdf.format(Date(timestamp))
    }

    fun getMonthDisplay(monthString: String): String {
        // monthString is "yyyy-MM" e.g. "2026-10"
        return try {
            val parts = monthString.split("-")
            val year = parts[0]
            val month = parts[1].toInt()
            val monthNames = listOf(
                "Januari", "Februari", "Maret", "April", "Mei", "Juni",
                "Juli", "Agustus", "September", "Oktober", "November", "Desember"
            )
            if (month in 1..12) {
                "${monthNames[month - 1]} $year"
            } else {
                monthString
            }
        } catch (_: Exception) {
            monthString
        }
    }

    fun getCurrentMonthString(): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return sdf.format(Calendar.getInstance().time)
    }

    fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Calendar.getInstance().time)
    }

    fun getRelativeDateLabel(dateString: String): String {
        val today = getCurrentDateString()
        if (dateString == today) return "Hari Ini"

        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        if (dateString == yesterday) return "Kemarin"

        return try {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString)
            if (date != null) {
                SimpleDateFormat("EEEE, dd MMM yyyy", localeId).format(date)
            } else {
                dateString
            }
        } catch (_: Exception) {
            dateString
        }
    }
}
