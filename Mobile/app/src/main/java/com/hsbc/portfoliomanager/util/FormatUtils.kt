package com.hsbc.portfoliomanager.util

import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

object FormatUtils {
    
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    private val percentFormat = NumberFormat.getPercentInstance(Locale.US).apply {
        maximumFractionDigits = 2
    }
    
    fun formatCurrency(value: BigDecimal): String {
        return currencyFormat.format(value)
    }
    
    fun formatCurrency(value: Double): String {
        return currencyFormat.format(value)
    }
    
    fun formatPercent(value: BigDecimal): String {
        return String.format("%.2f%%", value)
    }
    
    fun formatPercent(value: Double): String {
        return String.format("%.2f%%", value)
    }
    
    fun formatDate(dateString: String): String {
        return try {
            val date = LocalDate.parse(dateString)
            date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
        } catch (e: Exception) {
            dateString
        }
    }
    
    fun formatNumber(value: Long): String {
        return NumberFormat.getNumberInstance(Locale.US).format(value)
    }
    
    fun formatNumber(value: Int): String {
        return NumberFormat.getNumberInstance(Locale.US).format(value)
    }
    
    fun getCurrentDate(): String {
        return LocalDate.now().toString()
    }
    
    fun isValidDate(dateString: String): Boolean {
        return try {
            LocalDate.parse(dateString)
            true
        } catch (e: Exception) {
            false
        }
    }
}

