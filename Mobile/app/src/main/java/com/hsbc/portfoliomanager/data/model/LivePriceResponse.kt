package com.hsbc.portfoliomanager.data.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

/** Mirrors backend LivePriceResponse — returned by GET /api/prices/{ticker}?assetType=STOCK */
data class LivePriceResponse(
    @SerializedName("ticker")
    val ticker: String,

    /** Live current price — null if ticker not found */
    @SerializedName("currentPrice")
    val currentPrice: BigDecimal?,

    /** Day change (absolute) */
    @SerializedName("change")
    val change: BigDecimal?,

    /** Day change percentage */
    @SerializedName("changePercent")
    val changePercent: BigDecimal?,

    /** Non-null only on error (ticker not found / Finnhub down) */
    @SerializedName("errorMessage")
    val errorMessage: String? = null
)
