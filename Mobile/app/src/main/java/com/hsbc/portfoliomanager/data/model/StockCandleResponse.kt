package com.hsbc.portfoliomanager.data.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

/** Mirrors backend StockCandleResponse — returned by GET /api/prices/history/{ticker} */
data class StockCandleResponse(
    @SerializedName("ticker")
    val ticker: String,

    /** "FINNHUB" for stocks, "DUMMY" for bonds/crypto */
    @SerializedName("source")
    val source: String,

    @SerializedName("candles")
    val candles: List<DailyCandle>
)

data class DailyCandle(
    @SerializedName("date")
    val date: String,   // "YYYY-MM-DD"

    @SerializedName("open")
    val open: BigDecimal,

    @SerializedName("high")
    val high: BigDecimal,

    @SerializedName("low")
    val low: BigDecimal,

    @SerializedName("close")
    val close: BigDecimal,

    @SerializedName("volume")
    val volume: BigDecimal
)
