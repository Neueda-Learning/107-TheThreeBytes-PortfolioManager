package com.hsbc.portfoliomanager.util

object ValidationUtils {
    
    fun isValidTicker(ticker: String): Boolean {
        return ticker.matches(Regex("^[A-Za-z.]{1,10}$"))
    }
    
    fun isValidQuantity(quantity: String): Boolean {
        return try {
            val value = quantity.toInt()
            value > 0
        } catch (e: Exception) {
            false
        }
    }
    
    fun isValidPrice(price: String): Boolean {
        return try {
            val value = price.toBigDecimal()
            value > java.math.BigDecimal.ZERO
        } catch (e: Exception) {
            false
        }
    }
    
    fun isValidDate(date: String): Boolean {
        return try {
            java.time.LocalDate.parse(date)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun getTickerError(ticker: String): String? {
        return when {
            ticker.isEmpty() -> "Ticker is required"
            !isValidTicker(ticker) -> "Ticker must be letters/dot, up to 10 characters"
            else -> null
        }
    }
    
    fun getQuantityError(quantity: String): String? {
        return when {
            quantity.isEmpty() -> "Quantity is required"
            !isValidQuantity(quantity) -> "Quantity must be a positive number"
            else -> null
        }
    }
    
    fun getPriceError(price: String): String? {
        return when {
            price.isEmpty() -> "Price is required"
            !isValidPrice(price) -> "Price must be greater than 0"
            else -> null
        }
    }
    
    fun getDateError(date: String): String? {
        return when {
            date.isEmpty() -> "Date is required"
            !isValidDate(date) -> "Date must be in format YYYY-MM-DD"
            else -> null
        }
    }
}

