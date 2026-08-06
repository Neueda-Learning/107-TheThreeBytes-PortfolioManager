package com.hsbc.portfoliomanager.data.model

import com.google.gson.annotations.SerializedName

enum class AssetType {
    @SerializedName("STOCK")
    STOCK,
    
    @SerializedName("BOND")
    BOND,
    
    @SerializedName("CRYPTO")
    CRYPTO
}

fun AssetType.displayName(): String = when (this) {
    AssetType.STOCK -> "Stock"
    AssetType.BOND -> "Bond"
    AssetType.CRYPTO -> "Crypto"
}

