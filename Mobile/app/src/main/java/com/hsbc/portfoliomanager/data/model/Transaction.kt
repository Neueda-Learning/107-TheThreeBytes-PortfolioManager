package com.hsbc.portfoliomanager.data.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class TransactionResponse(
    @SerializedName("id")
    val id: Long,

    @SerializedName("ticker")
    val ticker: String,

    @SerializedName("assetType")
    val assetType: AssetType,

    @SerializedName("transactionType")
    val transactionType: TransactionType,

    @SerializedName("quantity")
    val quantity: BigDecimal,

    @SerializedName("pricePerUnit")
    val pricePerUnit: BigDecimal,

    @SerializedName("totalValue")
    val totalValue: BigDecimal,

    @SerializedName("transactionDate")
    val transactionDate: String,

    @SerializedName("notes")
    val notes: String? = null
)

data class CreateTransactionRequest(
    @SerializedName("ticker")
    val ticker: String,

    @SerializedName("assetType")
    val assetType: AssetType,

    @SerializedName("transactionType")
    val transactionType: TransactionType,

    @SerializedName("quantity")
    val quantity: BigDecimal,

    @SerializedName("pricePerUnit")
    val pricePerUnit: BigDecimal,

    @SerializedName("transactionDate")
    val transactionDate: String,

    @SerializedName("notes")
    val notes: String? = null
)
