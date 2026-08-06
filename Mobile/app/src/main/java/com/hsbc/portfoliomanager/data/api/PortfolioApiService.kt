package com.hsbc.portfoliomanager.data.api

import com.hsbc.portfoliomanager.data.model.*
import retrofit2.Response
import retrofit2.http.*
import java.math.BigDecimal

interface PortfolioApiService {

    // ── Portfolio Items (/api/portfolio-items) ──────────────────────────────

    @GET("portfolio-items")
    suspend fun getPortfolioItems(): Response<List<PortfolioItem>>

    @GET("portfolio-items/{id}")
    suspend fun getPortfolioItem(@Path("id") id: Long): Response<PortfolioItem>

    @POST("portfolio-items")
    suspend fun createPortfolioItem(@Body request: CreatePortfolioItemRequest): Response<PortfolioItem>

    @PUT("portfolio-items/{id}")
    suspend fun updatePortfolioItem(
        @Path("id") id: Long,
        @Body request: UpdatePortfolioItemRequest
    ): Response<PortfolioItem>

    @DELETE("portfolio-items/{id}")
    suspend fun deletePortfolioItem(@Path("id") id: Long): Response<Unit>

    /** Sell all units of a holding and log a SELL transaction */
    @POST("portfolio-items/{id}/sell")
    suspend fun sellPortfolioItem(
        @Path("id") id: Long,
        @Body request: SellHoldingRequest
    ): Response<TransactionResponse>

    /** Returns totalPositions, totalQuantity, quantityByAssetType */
    @GET("portfolio-items/summary")
    suspend fun getPortfolioSummary(): Response<PortfolioSummary>

    // ── Dashboard (/api/dashboard) ──────────────────────────────────────────

    @GET("dashboard")
    suspend fun getDashboard(): Response<DashboardResponse>

    @GET("dashboard/{assetType}")
    suspend fun getDashboardByAssetType(
        @Path("assetType") assetType: AssetType
    ): Response<DashboardResponse>

    // ── Performance (/api/performance) ─────────────────────────────────────

    @GET("performance")
    suspend fun getPerformance(): Response<List<PerformanceItem>>

    @GET("performance/{id}")
    suspend fun getPerformanceById(@Path("id") id: Long): Response<PerformanceItem>

    // ── Risk (/api/risk) ────────────────────────────────────────────────────

    @GET("risk/analysis")
    suspend fun getRiskAnalysis(): Response<RiskAnalysisResponse>

    // ── Transactions (/api/transactions) ────────────────────────────────────

    @GET("transactions")
    suspend fun getTransactions(@Query("ticker") ticker: String? = null): Response<List<TransactionResponse>>

    @GET("transactions/{id}")
    suspend fun getTransaction(@Path("id") id: Long): Response<TransactionResponse>

    @POST("transactions")
    suspend fun createTransaction(@Body request: CreateTransactionRequest): Response<TransactionResponse>

    @DELETE("transactions/{id}")
    suspend fun deleteTransaction(@Path("id") id: Long): Response<Unit>

    // ── Dividends (/api/dividends) ──────────────────────────────────────────

    @GET("dividends")
    suspend fun getDividends(@Query("ticker") ticker: String? = null): Response<List<DividendResponse>>

    @GET("dividends/{id}")
    suspend fun getDividend(@Path("id") id: Long): Response<DividendResponse>

    @POST("dividends")
    suspend fun createDividend(@Body request: CreateDividendRequest): Response<DividendResponse>

    @DELETE("dividends/{id}")
    suspend fun deleteDividend(@Path("id") id: Long): Response<Unit>

    /** Returns {"totalDividendsReceived": BigDecimal} */
    @GET("dividends/total")
    suspend fun getTotalDividends(): Response<Map<String, BigDecimal>>

    // ── Watchlist (/api/watchlist) ───────────────────────────────────────────

    @GET("watchlist")
    suspend fun getWatchlist(): Response<List<WatchlistItem>>

    @POST("watchlist")
    suspend fun addToWatchlist(@Body request: CreateWatchlistItemRequest): Response<WatchlistItem>

    @DELETE("watchlist/{id}")
    suspend fun removeFromWatchlist(@Path("id") id: Long): Response<Unit>

    // ── Tax (/api/tax) ───────────────────────────────────────────────────────

    @GET("tax/estimate")
    suspend fun getTaxEstimate(): Response<List<TaxItem>>

    // ── Stocks (/api/stocks) ─────────────────────────────────────────────────
    /** Company name + sector from /api/stocks/{ticker}/price */
    @GET("stocks/{ticker}/price")
    suspend fun getStockQuote(@Path("ticker") ticker: String): Response<StockQuote>

    // ── Live Prices (/api/prices) ─────────────────────────────────────────────
    /**
     * Live price, day-change and change% for any ticker.
     * STOCK → Finnhub (live). BOND / CRYPTO → dummy dataset.
     * Pass assetType so the backend routes correctly.
     */
    @GET("prices/{ticker}")
    suspend fun getLivePrice(
        @Path("ticker") ticker: String,
        @Query("assetType") assetType: String
    ): Response<LivePriceResponse>

    /**
     * 30-day daily OHLCV candles for a ticker.
     * STOCK → Finnhub /stock/candle (cached 24h). BOND/CRYPTO → dummy series.
     */
    @GET("prices/history/{ticker}")
    suspend fun getStockHistory(
        @Path("ticker") ticker: String,
        @Query("assetType") assetType: String,
        @Query("days") days: Int = 30
    ): Response<StockCandleResponse>
}
