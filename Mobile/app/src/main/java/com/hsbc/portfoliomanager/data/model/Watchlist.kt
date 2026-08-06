package com.hsbc.portfoliomanager.data.model

import com.google.gson.annotations.SerializedName

data class WatchlistItem(
    @SerializedName("id")
    val id: Long,

    @SerializedName("ticker")
    val ticker: String,

    @SerializedName("assetType")
    val assetType: AssetType,

    @SerializedName("addedDate")
    val addedDate: String
)

data class CreateWatchlistItemRequest(
    @SerializedName("ticker")
    val ticker: String,

    @SerializedName("assetType")
    val assetType: AssetType
)
