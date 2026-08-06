package com.hsbc.portfoliomanager.data.repository

import com.hsbc.portfoliomanager.PortfolioApplication
import com.hsbc.portfoliomanager.data.api.ApiClient
import com.hsbc.portfoliomanager.data.local.AppDatabase
import com.hsbc.portfoliomanager.data.local.PortfolioEntity
import com.hsbc.portfoliomanager.data.local.SyncStatus
import com.hsbc.portfoliomanager.data.model.*
import com.hsbc.portfoliomanager.data.worker.SyncWorker
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.math.BigDecimal

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

class PortfolioRepository {

    private val api = ApiClient.portfolioApi
    private val dao = AppDatabase.getDatabase(PortfolioApplication.appContext).portfolioDao()
    private val workManager = WorkManager.getInstance(PortfolioApplication.appContext)

    private fun enqueueSync() {
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>().setConstraints(constraints).build()
        workManager.enqueue(syncRequest)
    }

    private fun PortfolioEntity.toPortfolioItem() = PortfolioItem(
        id = id, ticker = ticker, quantity = quantity, assetType = assetType,
        purchasePrice = purchasePrice, purchaseDate = purchaseDate, name = name,
        sector = sector, issuer = issuer, interestRate = interestRate, maturityDate = maturityDate
    )

    private fun PortfolioItem.toEntity(status: SyncStatus = SyncStatus.SYNCED) = PortfolioEntity(
        id = id, ticker = ticker, quantity = quantity, assetType = assetType,
        purchasePrice = purchasePrice, purchaseDate = purchaseDate, name = name,
        sector = sector, issuer = issuer, interestRate = interestRate, maturityDate = maturityDate,
        syncStatus = status
    )

    // ── Portfolio Items (Offline-First) ──────────────────────────────────────

    fun getPortfolioFlow(): Flow<List<PortfolioItem>> {
        return dao.getPortfolioFlow().map { entities -> entities.map { it.toPortfolioItem() } }
    }

    suspend fun getPortfolioItems(): ApiResult<List<PortfolioItem>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getPortfolioItems()
            if (response.isSuccessful && response.body() != null) {
                val serverItems = response.body()!!
                val currentPending = dao.getPendingOperations()
                dao.deleteAll()
                dao.insertItems(serverItems.map { it.toEntity() })
                dao.insertItems(currentPending)
                ApiResult.Success(serverItems)
            } else {
                // If API fails, return local data as success to keep UI working offline
                ApiResult.Success(dao.getAllItems().map { it.toPortfolioItem() })
            }
        } catch (e: Exception) {
            // Network error -> Return local data
            ApiResult.Success(dao.getAllItems().map { it.toPortfolioItem() })
        }
    }

    suspend fun getPortfolioItem(id: Long): ApiResult<PortfolioItem> = withContext(Dispatchers.IO) {
        try {
            val response = api.getPortfolioItem(id)
            if (response.isSuccessful && response.body() != null)
                ApiResult.Success(response.body()!!)
            else
                ApiResult.Error(response.message() ?: "Failed to fetch portfolio item")
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error occurred")
        }
    }

    suspend fun createPortfolioItem(request: CreatePortfolioItemRequest): ApiResult<PortfolioItem> =
        withContext(Dispatchers.IO) {
            try {
                val minId = dao.getMinId() ?: 0L
                val newId = if (minId < 0) minId - 1 else -1L
                val entity = PortfolioEntity(
                    id = newId, ticker = request.ticker, quantity = request.quantity, assetType = request.assetType,
                    purchasePrice = request.purchasePrice, purchaseDate = request.purchaseDate, name = request.name,
                    sector = request.sector, issuer = request.issuer, interestRate = request.interestRate,
                    maturityDate = request.maturityDate, syncStatus = SyncStatus.PENDING_ADD
                )
                dao.insertItem(entity)
                enqueueSync()
                ApiResult.Success(entity.toPortfolioItem())
            } catch (e: Exception) {
                ApiResult.Error(e.message ?: "Database error occurred")
            }
        }

    suspend fun updatePortfolioItem(id: Long, request: UpdatePortfolioItemRequest): ApiResult<PortfolioItem> =
        withContext(Dispatchers.IO) {
            try {
                val entity = PortfolioEntity(
                    id = id, ticker = request.ticker, quantity = request.quantity, assetType = request.assetType,
                    purchasePrice = request.purchasePrice, purchaseDate = request.purchaseDate, name = request.name,
                    sector = request.sector, issuer = request.issuer, interestRate = request.interestRate,
                    maturityDate = request.maturityDate, syncStatus = SyncStatus.PENDING_UPDATE
                )
                dao.updateItem(entity)
                enqueueSync()
                ApiResult.Success(entity.toPortfolioItem())
            } catch (e: Exception) {
                ApiResult.Error(e.message ?: "Database error occurred")
            }
        }

    suspend fun deletePortfolioItem(id: Long): ApiResult<Unit> = withContext(Dispatchers.IO) {
        try {
            dao.deleteById(id)
            // To sync, we'd need a dummy entity for PENDING_DELETE.
            val marker = PortfolioEntity(
                id = id, ticker = "DEL", quantity = BigDecimal.ZERO, assetType = AssetType.STOCK,
                purchasePrice = BigDecimal.ZERO, purchaseDate = "1970-01-01", name = null,
                sector = null, issuer = null, interestRate = null, maturityDate = null,
                syncStatus = SyncStatus.PENDING_DELETE
            )
            dao.insertItem(marker)
            enqueueSync()
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Database error occurred")
        }
    }

    suspend fun sellPortfolioItem(id: Long, request: SellHoldingRequest): ApiResult<TransactionResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.sellPortfolioItem(id, request)
                if (response.isSuccessful && response.body() != null) {
                    ApiResult.Success(response.body()!!)
                } else {
                    var errorMessage = response.message() ?: "Failed to sell holding"
                    try {
                        val errorBody = response.errorBody()?.string()
                        if (errorBody != null) {
                            val jsonObject = com.google.gson.JsonParser.parseString(errorBody).asJsonObject
                            if (jsonObject.has("details") && jsonObject.getAsJsonArray("details").size() > 0) {
                                errorMessage = jsonObject.getAsJsonArray("details").get(0).asString
                            } else if (jsonObject.has("error")) {
                                errorMessage = jsonObject.get("error").asString
                            }
                        }
                    } catch (e: Exception) {
                        // Fallback to response.message()
                    }
                    ApiResult.Error(errorMessage)
                }
            } catch (e: Exception) {
                ApiResult.Error(e.message ?: "Network error occurred")
            }
        }

    suspend fun getPortfolioSummary(): ApiResult<PortfolioSummary> = withContext(Dispatchers.IO) {
        try {
            val response = api.getPortfolioSummary()
            if (response.isSuccessful && response.body() != null)
                ApiResult.Success(response.body()!!)
            else
                ApiResult.Error(response.message() ?: "Failed to fetch portfolio summary")
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error occurred")
        }
    }

    // ── Dashboard ────────────────────────────────────────────────────────────

    suspend fun getDashboard(): ApiResult<DashboardResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.getDashboard()
            if (response.isSuccessful && response.body() != null)
                ApiResult.Success(response.body()!!)
            else
                ApiResult.Error(response.message() ?: "Failed to fetch dashboard")
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error occurred")
        }
    }

    suspend fun getDashboardByAssetType(assetType: AssetType): ApiResult<DashboardResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getDashboardByAssetType(assetType)
                if (response.isSuccessful && response.body() != null)
                    ApiResult.Success(response.body()!!)
                else
                    ApiResult.Error(response.message() ?: "Failed to fetch dashboard")
            } catch (e: Exception) {
                ApiResult.Error(e.message ?: "Network error occurred")
            }
        }

    suspend fun getLocalDashboard(): DashboardResponse = withContext(Dispatchers.IO) {
        val items = dao.getAllItems().map { it.toPortfolioItem() }
        buildLocalDashboard(items)
    }

    suspend fun getLocalDashboardByAssetType(assetType: AssetType): DashboardResponse = withContext(Dispatchers.IO) {
        val items = dao.getAllItems().map { it.toPortfolioItem() }.filter { it.assetType == assetType }
        buildLocalDashboard(items)
    }

    private fun buildLocalDashboard(items: List<PortfolioItem>): DashboardResponse {
        val totalPositions = items.size.toLong()
        val totalQuantity = items.map { it.quantity }.fold(BigDecimal.ZERO, BigDecimal::add)
        
        var totalCostBasis = java.math.BigDecimal.ZERO
        var estimatedTotalValue = java.math.BigDecimal.ZERO
        
        val qtyByAsset = mutableMapOf<String, BigDecimal>()
        val costByAsset = mutableMapOf<String, java.math.BigDecimal>()
        
        for (item in items) {
            val cost = item.purchasePrice.multiply(item.quantity)
            totalCostBasis = totalCostBasis.add(cost)
            estimatedTotalValue = estimatedTotalValue.add(cost)
            
            val type = item.assetType.name
            qtyByAsset[type] = (qtyByAsset[type] ?: BigDecimal.ZERO).add(item.quantity)
            costByAsset[type] = (costByAsset[type] ?: java.math.BigDecimal.ZERO).add(cost)
        }
        
        return DashboardResponse(
            totalPositions = totalPositions,
            totalQuantity = totalQuantity,
            totalCostBasis = totalCostBasis,
            estimatedTotalValue = estimatedTotalValue,
            unrealizedGainLoss = java.math.BigDecimal.ZERO,
            unrealizedGainLossPct = java.math.BigDecimal.ZERO,
            quantityByAssetType = qtyByAsset,
            costByAssetType = costByAsset,
            holdings = items
        )
    }

    // ── Performance ──────────────────────────────────────────────────────────

    suspend fun getPerformance(): ApiResult<List<PerformanceItem>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getPerformance()
            if (response.isSuccessful && response.body() != null)
                ApiResult.Success(response.body()!!)
            else
                ApiResult.Error(response.message() ?: "Failed to fetch performance data")
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error occurred")
        }
    }

    suspend fun getPerformanceById(id: Long): ApiResult<PerformanceItem> = withContext(Dispatchers.IO) {
        try {
            val response = api.getPerformanceById(id)
            if (response.isSuccessful && response.body() != null)
                ApiResult.Success(response.body()!!)
            else
                ApiResult.Error(response.message() ?: "Failed to fetch performance item")
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error occurred")
        }
    }

    // ── Risk ─────────────────────────────────────────────────────────────────

    suspend fun getRiskAnalysis(): ApiResult<RiskAnalysisResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.getRiskAnalysis()
            if (response.isSuccessful && response.body() != null)
                ApiResult.Success(response.body()!!)
            else
                ApiResult.Error(response.message() ?: "Failed to fetch risk analysis")
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error occurred")
        }
    }

    // ── Transactions ─────────────────────────────────────────────────────────

    suspend fun getTransactions(ticker: String? = null): ApiResult<List<TransactionResponse>> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getTransactions(ticker)
                if (response.isSuccessful && response.body() != null)
                    ApiResult.Success(response.body()!!)
                else
                    ApiResult.Error(response.message() ?: "Failed to fetch transactions")
            } catch (e: Exception) {
                ApiResult.Error(e.message ?: "Network error occurred")
            }
        }

    suspend fun createTransaction(request: CreateTransactionRequest): ApiResult<TransactionResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.createTransaction(request)
                if (response.isSuccessful && response.body() != null)
                    ApiResult.Success(response.body()!!)
                else
                    ApiResult.Error(response.message() ?: "Failed to create transaction")
            } catch (e: Exception) {
                ApiResult.Error(e.message ?: "Network error occurred")
            }
        }

    suspend fun deleteTransaction(id: Long): ApiResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteTransaction(id)
            if (response.isSuccessful)
                ApiResult.Success(Unit)
            else
                ApiResult.Error(response.message() ?: "Failed to delete transaction")
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error occurred")
        }
    }

    // ── Dividends ────────────────────────────────────────────────────────────

    suspend fun getDividends(ticker: String? = null): ApiResult<List<DividendResponse>> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getDividends(ticker)
                if (response.isSuccessful && response.body() != null)
                    ApiResult.Success(response.body()!!)
                else
                    ApiResult.Error(response.message() ?: "Failed to fetch dividends")
            } catch (e: Exception) {
                ApiResult.Error(e.message ?: "Network error occurred")
            }
        }

    suspend fun createDividend(request: CreateDividendRequest): ApiResult<DividendResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.createDividend(request)
                if (response.isSuccessful && response.body() != null)
                    ApiResult.Success(response.body()!!)
                else
                    ApiResult.Error(response.message() ?: "Failed to create dividend")
            } catch (e: Exception) {
                ApiResult.Error(e.message ?: "Network error occurred")
            }
        }

    suspend fun deleteDividend(id: Long): ApiResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteDividend(id)
            if (response.isSuccessful)
                ApiResult.Success(Unit)
            else
                ApiResult.Error(response.message() ?: "Failed to delete dividend")
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error occurred")
        }
    }

    suspend fun getTotalDividends(): ApiResult<BigDecimal> = withContext(Dispatchers.IO) {
        try {
            val response = api.getTotalDividends()
            if (response.isSuccessful && response.body() != null)
                ApiResult.Success(response.body()!!["totalDividendsReceived"] ?: BigDecimal.ZERO)
            else
                ApiResult.Error(response.message() ?: "Failed to fetch total dividends")
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error occurred")
        }
    }

    // ── Watchlist ─────────────────────────────────────────────────────────────

    suspend fun getWatchlist(): ApiResult<List<WatchlistItem>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getWatchlist()
            if (response.isSuccessful && response.body() != null)
                ApiResult.Success(response.body()!!)
            else
                ApiResult.Error(response.message() ?: "Failed to fetch watchlist")
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error occurred")
        }
    }

    suspend fun addToWatchlist(request: CreateWatchlistItemRequest): ApiResult<WatchlistItem> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.addToWatchlist(request)
                if (response.isSuccessful && response.body() != null)
                    ApiResult.Success(response.body()!!)
                else
                    ApiResult.Error(response.message() ?: "Failed to add to watchlist")
            } catch (e: Exception) {
                ApiResult.Error(e.message ?: "Network error occurred")
            }
        }

    suspend fun removeFromWatchlist(id: Long): ApiResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.removeFromWatchlist(id)
            if (response.isSuccessful)
                ApiResult.Success(Unit)
            else
                ApiResult.Error(response.message() ?: "Failed to remove from watchlist")
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error occurred")
        }
    }

    // ── Tax ───────────────────────────────────────────────────────────────────

    suspend fun getTaxEstimate(): ApiResult<List<TaxItem>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getTaxEstimate()
            if (response.isSuccessful && response.body() != null)
                ApiResult.Success(response.body()!!)
            else
                ApiResult.Error(response.message() ?: "Failed to fetch tax estimate")
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error occurred")
        }
    }

    // ── Stocks ────────────────────────────────────────────────────────────────

    suspend fun getStockQuote(ticker: String): ApiResult<StockQuote> = withContext(Dispatchers.IO) {
        try {
            val response = api.getStockQuote(ticker)
            if (response.isSuccessful && response.body() != null)
                ApiResult.Success(response.body()!!)
            else
                ApiResult.Error(response.message() ?: "Failed to fetch stock quote")
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error occurred")
        }
    }

    /**
     * Fetches live price with change & change% from backend.
     * STOCK → Finnhub (live, cached 60s on backend).
     * BOND/CRYPTO → dummy market data.
     */
    suspend fun getLivePrice(ticker: String, assetType: String): ApiResult<LivePriceResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getLivePrice(ticker, assetType)
                if (response.isSuccessful && response.body() != null)
                    ApiResult.Success(response.body()!!)
                else
                    ApiResult.Error(response.message() ?: "Failed to fetch live price")
            } catch (e: Exception) {
                ApiResult.Error(e.message ?: "Network error occurred")
            }
        }

    /**
     * Fetches 30-day (or N-day) daily OHLCV candles from backend.
     * STOCK → Finnhub /stock/candle (cached 24h on backend).
     * BOND/CRYPTO → transformed dummy price series.
     */
    suspend fun getStockHistory(
        ticker: String,
        assetType: String,
        days: Int = 30
    ): ApiResult<StockCandleResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.getStockHistory(ticker, assetType, days)
            if (response.isSuccessful && response.body() != null)
                ApiResult.Success(response.body()!!)
            else
                ApiResult.Error(response.message() ?: "Failed to fetch stock history")
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error occurred")
        }
    }
}
