package com.hsbc.portfoliomanager.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.hsbc.portfoliomanager.data.model.AssetType
import java.math.BigDecimal

@Entity(tableName = "portfolio_items")
data class PortfolioEntity(
    @PrimaryKey val id: Long, // Use the server's ID, or generate locally for pending items
    val ticker: String,
    val quantity: BigDecimal,
    val assetType: AssetType,
    val purchasePrice: BigDecimal,
    val purchaseDate: String,
    val name: String?,
    val sector: String?,
    val issuer: String?,
    val interestRate: BigDecimal?,
    val maturityDate: String?,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)

enum class SyncStatus {
    SYNCED, PENDING_ADD, PENDING_UPDATE, PENDING_DELETE
}
