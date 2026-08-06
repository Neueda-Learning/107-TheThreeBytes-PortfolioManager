package com.hsbc.portfoliomanager.data.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class PerformanceItem(
    @SerializedName("id")
    val id: Long,

    @SerializedName("ticker")
    val ticker: String,

    @SerializedName("assetType")
    val assetType: AssetType,

    @SerializedName("quantity")
    val quantity: BigDecimal,

    @SerializedName("purchasePrice")
    val purchasePrice: BigDecimal,

    @SerializedName("currentPrice")
    val currentPrice: BigDecimal,

    @SerializedName("costBasis")
    val costBasis: BigDecimal,

    @SerializedName("currentValue")
    val currentValue: BigDecimal,

    @SerializedName("unrealizedGain")
    val unrealizedGain: BigDecimal,

    @SerializedName("unrealizedGainPct")
    val unrealizedGainPct: BigDecimal,

    @SerializedName("purchaseDate")
    val purchaseDate: String,

    @SerializedName("holdingDays")
    val holdingDays: Long
)
