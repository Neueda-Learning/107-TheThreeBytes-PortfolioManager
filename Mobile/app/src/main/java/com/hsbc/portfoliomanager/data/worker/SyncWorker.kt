package com.hsbc.portfoliomanager.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.hsbc.portfoliomanager.data.api.ApiClient
import com.hsbc.portfoliomanager.data.local.AppDatabase
import com.hsbc.portfoliomanager.data.local.PortfolioEntity
import com.hsbc.portfoliomanager.data.local.SyncStatus
import com.hsbc.portfoliomanager.data.model.CreatePortfolioItemRequest
import com.hsbc.portfoliomanager.data.model.UpdatePortfolioItemRequest
import com.hsbc.portfoliomanager.data.model.PortfolioItem

class SyncWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val dao = database.portfolioDao()
        val api = ApiClient.portfolioApi

        try {
            // 1. Process local pending changes
            val pendingItems = dao.getPendingOperations()
            for (item in pendingItems) {
                try {
                    when (item.syncStatus) {
                        SyncStatus.PENDING_ADD -> {
                            val request = CreatePortfolioItemRequest(
                                ticker = item.ticker, quantity = item.quantity, assetType = item.assetType,
                                purchasePrice = item.purchasePrice, purchaseDate = item.purchaseDate,
                                name = item.name, sector = item.sector, issuer = item.issuer,
                                interestRate = item.interestRate, maturityDate = item.maturityDate
                            )
                            api.createPortfolioItem(request)
                            // Remove the local placeholder; the next API fetch will bring the real item
                            dao.deleteItem(item)
                        }
                        SyncStatus.PENDING_UPDATE -> {
                            val request = UpdatePortfolioItemRequest(
                                ticker = item.ticker, quantity = item.quantity, assetType = item.assetType,
                                purchasePrice = item.purchasePrice, purchaseDate = item.purchaseDate,
                                name = item.name, sector = item.sector, issuer = item.issuer,
                                interestRate = item.interestRate, maturityDate = item.maturityDate
                            )
                            api.updatePortfolioItem(item.id, request)
                            // Mark synced locally
                            dao.updateItem(item.copy(syncStatus = SyncStatus.SYNCED))
                        }
                        SyncStatus.PENDING_DELETE -> {
                            api.deletePortfolioItem(item.id)
                            dao.deleteItem(item)
                        }
                        SyncStatus.SYNCED -> { /* Should not happen based on query */ }
                    }
                } catch (e: Exception) {
                    // If a single operation fails (e.g. 500 error), continue trying others.
                    // If it's a network error, doWork() will eventually fail and retry.
                    e.printStackTrace()
                }
            }

            // 2. Fetch fresh data from backend to ensure DB is up to date
            val response = api.getPortfolioItems()
            if (response.isSuccessful && response.body() != null) {
                val serverItems = response.body()!!
                val newEntities = serverItems.map { dto ->
                PortfolioEntity(
                    id = dto.id,
                    ticker = dto.ticker,
                    quantity = dto.quantity,
                    assetType = dto.assetType,
                    purchasePrice = dto.purchasePrice,
                    purchaseDate = dto.purchaseDate,
                    name = dto.name,
                    sector = dto.sector,
                    issuer = dto.issuer,
                    interestRate = dto.interestRate,
                    maturityDate = dto.maturityDate,
                    syncStatus = SyncStatus.SYNCED
                )
            }
            
                // Delete all SYNCED items and replace with server source of truth
                // We keep pending items so we don't overwrite un-pushed offline changes
                val currentPending = dao.getPendingOperations()
                dao.deleteAll()
                dao.insertItems(newEntities)
                dao.insertItems(currentPending)
            }

            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }
}
