package com.hsbc.portfoliomanager.data.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class TaxItem(
    @SerializedName("ticker")
    val ticker: String,

    @SerializedName("assetType")
    val assetType: AssetType,

    @SerializedName("purchaseDate")
    val purchaseDate: String,

    @SerializedName("holdingDays")
    val holdingDays: Long,

    @SerializedName("taxCategory")
    val taxCategory: String,

    @SerializedName("costBasis")
    val costBasis: BigDecimal,

    @SerializedName("estimatedCurrentValue")
    val estimatedCurrentValue: BigDecimal,

    @SerializedName("estimatedGain")
    val estimatedGain: BigDecimal,

    @SerializedName("taxRate")
    val taxRate: BigDecimal,

    @SerializedName("estimatedTaxLiability")
    val estimatedTaxLiability: BigDecimal
)
