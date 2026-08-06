package com.hsbc.portfoliomanager.data.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class DashboardResponse(
    @SerializedName("totalPositions")
    val totalPositions: Long,

    @SerializedName("totalQuantity")
    val totalQuantity: BigDecimal,

    @SerializedName("totalCostBasis")
    val totalCostBasis: BigDecimal,

    @SerializedName("estimatedTotalValue")
    val estimatedTotalValue: BigDecimal,

    @SerializedName("unrealizedGainLoss")
    val unrealizedGainLoss: BigDecimal,

    @SerializedName("unrealizedGainLossPct")
    val unrealizedGainLossPct: BigDecimal,

    @SerializedName("quantityByAssetType")
    val quantityByAssetType: Map<String, BigDecimal>,

    @SerializedName("costByAssetType")
    val costByAssetType: Map<String, BigDecimal>,

    @SerializedName("holdings")
    val holdings: List<PortfolioItem>
)

// Fixed: backend PortfolioSummaryResponse has only totalPositions, totalQuantity, quantityByAssetType
data class PortfolioSummary(
    @SerializedName("totalPositions")
    val totalPositions: Long,

    @SerializedName("totalQuantity")
    val totalQuantity: BigDecimal,

    @SerializedName("totalCostBasis")
    val totalCostBasis: BigDecimal,

    @SerializedName("quantityByAssetType")
    val quantityByAssetType: Map<String, BigDecimal>,

    @SerializedName("costByAssetType")
    val costByAssetType: Map<String, BigDecimal>
)
