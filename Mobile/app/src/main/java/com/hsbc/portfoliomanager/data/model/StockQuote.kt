package com.hsbc.portfoliomanager.data.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class StockQuote(
    @SerializedName("symbol")
    val symbol: String,

    @SerializedName("companyName")
    val companyName: String,

    @SerializedName("sector")
    val sector: String,

    @SerializedName("currentPrice")
    val currentPrice: BigDecimal,

    @SerializedName("errorMessage")
    val errorMessage: String? = null
)
