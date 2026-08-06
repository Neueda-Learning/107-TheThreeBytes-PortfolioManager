package com.hsbc.portfoliomanager.data.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class HoldingRiskDetail(
    @SerializedName("ticker")
    val ticker: String,

    @SerializedName("assetType")
    val assetType: AssetType,

    @SerializedName("purchaseDate")
    val purchaseDate: String,

    @SerializedName("holdingDays")
    val holdingDays: Long,

    @SerializedName("holdingCategory")
    val holdingCategory: String,

    @SerializedName("portfolioConcentrationPct")
    val portfolioConcentrationPct: BigDecimal
)

data class RiskAnalysisResponse(
    @SerializedName("concentrationByAssetType")
    val concentrationByAssetType: Map<String, BigDecimal>,

    @SerializedName("holdingRiskDetails")
    val holdingRiskDetails: List<HoldingRiskDetail>,

    @SerializedName("diversificationScore")
    val diversificationScore: BigDecimal,

    @SerializedName("overallRiskLevel")
    val overallRiskLevel: String
)
