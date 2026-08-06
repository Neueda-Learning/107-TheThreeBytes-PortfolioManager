package com.hsbc.portfoliomanager.data.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class DividendResponse(
    @SerializedName("id")
    val id: Long,

    @SerializedName("ticker")
    val ticker: String,

    @SerializedName("dividendPerShare")
    val dividendPerShare: BigDecimal,

    @SerializedName("sharesHeld")
    val sharesHeld: Int,

    @SerializedName("totalDividend")
    val totalDividend: BigDecimal,

    @SerializedName("dividendDate")
    val dividendDate: String
)

data class CreateDividendRequest(
    @SerializedName("ticker")
    val ticker: String,

    @SerializedName("dividendPerShare")
    val dividendPerShare: BigDecimal,

    @SerializedName("sharesHeld")
    val sharesHeld: Int,

    @SerializedName("dividendDate")
    val dividendDate: String
)
