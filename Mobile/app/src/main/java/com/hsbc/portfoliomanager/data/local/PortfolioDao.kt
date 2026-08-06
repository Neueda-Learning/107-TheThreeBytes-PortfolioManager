package com.hsbc.portfoliomanager.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PortfolioDao {
    @Query("SELECT * FROM portfolio_items WHERE syncStatus != 'PENDING_DELETE' ORDER BY purchaseDate DESC")
    fun getPortfolioFlow(): Flow<List<PortfolioEntity>>

    @Query("SELECT * FROM portfolio_items")
    suspend fun getAllItems(): List<PortfolioEntity>

    @Query("SELECT * FROM portfolio_items WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingOperations(): List<PortfolioEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<PortfolioEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: PortfolioEntity): Long

    @Update
    suspend fun updateItem(item: PortfolioEntity)

    @Delete
    suspend fun deleteItem(item: PortfolioEntity)

    @Query("DELETE FROM portfolio_items")
    suspend fun deleteAll()

    @Query("DELETE FROM portfolio_items WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("SELECT MIN(id) FROM portfolio_items")
    suspend fun getMinId(): Long?
}
