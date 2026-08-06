package com.hsbc.portfoliomanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hsbc.portfoliomanager.data.model.*
import com.hsbc.portfoliomanager.data.repository.ApiResult
import com.hsbc.portfoliomanager.data.repository.PortfolioRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal

data class StockDetailUiState(
    val isLoading: Boolean = true,
    val ticker: String = "",

    // From /api/stocks/{ticker}/price — company name, sector
    val companyName: String? = null,
    val sector: String? = null,

    // From /api/prices/{ticker}?assetType=STOCK — live price + change
    val currentPrice: BigDecimal? = null,
    val change: BigDecimal? = null,
    val changePercent: BigDecimal? = null,

    // From /api/prices/history/{ticker}?assetType=STOCK&days=30 — candles
    val candles: List<DailyCandle> = emptyList(),
    val dataSource: String = "LOADING",   // "FINNHUB" | "DUMMY" | "LOADING"

    // The holding from the user's portfolio (passed in — no extra network call)
    val portfolioItem: PortfolioItem? = null,

    val error: String? = null
) {
    /** Computed: unrealised P&L = (livePrice - purchasePrice) × qty */
    val unrealisedPnl: BigDecimal?
        get() {
            val price = currentPrice ?: return null
            val item  = portfolioItem ?: return null
            return price.subtract(item.purchasePrice)
                .multiply(item.quantity)
        }

    val unrealisedPnlPct: BigDecimal?
        get() {
            val item = portfolioItem ?: return null
            val pnl  = unrealisedPnl ?: return null
            val cost = item.purchasePrice.multiply(item.quantity)
            if (cost == BigDecimal.ZERO) return BigDecimal.ZERO
            return pnl.divide(cost, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal(100))
        }

    val thirtyDayHigh: BigDecimal?
        get() = candles.maxOfOrNull { it.high }

    val thirtyDayLow: BigDecimal?
        get() = candles.minOfOrNull { it.low }

    val todayHigh: BigDecimal?
        get() = candles.lastOrNull()?.high

    val todayLow: BigDecimal?
        get() = candles.lastOrNull()?.low

    val todayOpen: BigDecimal?
        get() = candles.lastOrNull()?.open

    val previousClose: BigDecimal?
        get() = if (candles.size >= 2) candles[candles.size - 2].close else null
}

/**
 * ViewModel for the Stock Detail screen.
 *
 * Loads 3 things in parallel for a given ticker:
 *   1. /api/stocks/{ticker}/price        → company profile (name, sector)
 *   2. /api/prices/{ticker}?assetType    → live price + change + change%
 *   3. /api/prices/history/{ticker}      → 30-day daily OHLCV candles
 *
 * For STOCK: data source = Finnhub (via backend cache).
 * For BOND / CRYPTO: data source = backend dummy dataset.
 */
class StockDetailViewModel : ViewModel() {

    private val repository = PortfolioRepository()

    private val _uiState = MutableStateFlow(StockDetailUiState())
    val uiState: StateFlow<StockDetailUiState> = _uiState.asStateFlow()

    fun load(ticker: String, assetType: AssetType, portfolioItem: PortfolioItem?) {
        val assetTypeStr = assetType.name   // "STOCK", "BOND", "CRYPTO"

        viewModelScope.launch {
            _uiState.value = StockDetailUiState(
                isLoading     = true,
                ticker        = ticker,
                portfolioItem = portfolioItem
            )

            // ── Fire all 3 requests in parallel ──────────────────────────────
            val profileDeferred = async { repository.getStockQuote(ticker) }
            val priceDeferred   = async { repository.getLivePrice(ticker, assetTypeStr) }
            val historyDeferred = async { repository.getStockHistory(ticker, assetTypeStr, 30) }

            val profileResult  = profileDeferred.await()
            val priceResult    = priceDeferred.await()
            val historyResult  = historyDeferred.await()

            // ── Merge results ─────────────────────────────────────────────────
            val companyName = (profileResult as? ApiResult.Success)?.data?.companyName
            val sector      = (profileResult as? ApiResult.Success)?.data?.sector

            val livePriceData = (priceResult as? ApiResult.Success)?.data
            val candleData    = (historyResult as? ApiResult.Success)?.data

            val anyError = listOf(profileResult, priceResult, historyResult)
                .filterIsInstance<ApiResult.Error>()
                .firstOrNull()?.message

            _uiState.value = StockDetailUiState(
                isLoading      = false,
                ticker         = ticker,
                companyName    = companyName ?: portfolioItem?.name,
                sector         = sector ?: portfolioItem?.sector,
                currentPrice   = livePriceData?.currentPrice ?: portfolioItem?.purchasePrice,
                change         = livePriceData?.change,
                changePercent  = livePriceData?.changePercent,
                candles        = candleData?.candles ?: emptyList(),
                dataSource     = candleData?.source ?: "UNKNOWN",
                portfolioItem  = portfolioItem,
                error          = anyError
            )
        }
    }

    fun refresh(ticker: String, assetType: AssetType, portfolioItem: PortfolioItem?) {
        load(ticker, assetType, portfolioItem)
    }
}
