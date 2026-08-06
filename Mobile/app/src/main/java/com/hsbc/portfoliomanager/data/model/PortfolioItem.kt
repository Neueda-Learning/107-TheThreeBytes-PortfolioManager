package com.hsbc.portfoliomanager.data.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class PortfolioItem(
    @SerializedName("id")
    val id: Long,

    @SerializedName("ticker")
    val ticker: String,

    @SerializedName("quantity")
    val quantity: BigDecimal,

    @SerializedName("assetType")
    val assetType: AssetType,

    @SerializedName("purchasePrice")
    val purchasePrice: BigDecimal,

    @SerializedName("purchaseDate")
    val purchaseDate: String,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("sector")
    val sector: String? = null,

    @SerializedName("issuer")
    val issuer: String? = null,

    @SerializedName("interestRate")
    val interestRate: BigDecimal? = null,

    @SerializedName("maturityDate")
    val maturityDate: String? = null
)

data class CreatePortfolioItemRequest(
    @SerializedName("ticker")
    val ticker: String,

    @SerializedName("quantity")
    val quantity: BigDecimal,

    @SerializedName("assetType")
    val assetType: AssetType,

    @SerializedName("purchasePrice")
    val purchasePrice: BigDecimal,

    @SerializedName("purchaseDate")
    val purchaseDate: String,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("sector")
    val sector: String? = null,

    @SerializedName("issuer")
    val issuer: String? = null,

    @SerializedName("interestRate")
    val interestRate: BigDecimal? = null,

    @SerializedName("maturityDate")
    val maturityDate: String? = null
)

data class UpdatePortfolioItemRequest(
    @SerializedName("ticker")
    val ticker: String,

    @SerializedName("quantity")
    val quantity: BigDecimal,

    @SerializedName("assetType")
    val assetType: AssetType,

    @SerializedName("purchasePrice")
    val purchasePrice: BigDecimal,

    @SerializedName("purchaseDate")
    val purchaseDate: String,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("sector")
    val sector: String? = null,

    @SerializedName("issuer")
    val issuer: String? = null,

    @SerializedName("interestRate")
    val interestRate: BigDecimal? = null,

    @SerializedName("maturityDate")
    val maturityDate: String? = null
)

data class SellHoldingRequest(
    @SerializedName("pricePerUnit")
    val pricePerUnit: BigDecimal,

    @SerializedName("quantity")
    val quantity: BigDecimal
)
