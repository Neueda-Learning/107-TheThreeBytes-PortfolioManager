package com.hsbc.portfoliomanager.data.model

import com.google.gson.annotations.SerializedName

enum class TransactionType {
    @SerializedName("BUY")
    BUY,

    @SerializedName("SELL")
    SELL
}
