package com.example.hsbcproject.service;

import com.example.hsbcproject.domain.AssetType;
import com.example.hsbcproject.dto.LivePriceResponse;
import com.example.hsbcproject.dto.StockCandleResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Central price resolution service.
 *
 * Routing strategy:
 *   STOCK  → Finnhub API (live data, cached) with fallback to dummy
 *   BOND   → DummyMarketDataStore only
 *   CRYPTO → DummyMarketDataStore only
 *   null / unknown → DummyMarketDataStore (backward-compatible)
 */
@Service
public class PriceService {

    private final DummyMarketDataStore dummyMarketDataStore;
    private final FinnhubClient finnhubClient;

    public PriceService(DummyMarketDataStore dummyMarketDataStore, FinnhubClient finnhubClient) {
        this.dummyMarketDataStore = dummyMarketDataStore;
        this.finnhubClient = finnhubClient;
    }

    // ── Live Quote ─────────────────────────────────────────────────────────────

    /**
     * Backward-compatible overload — uses dummy data.
     * Used by callers that don't know the asset type (e.g. PriceController /{ticker}).
     */
    public LivePriceResponse getPrice(String ticker) {
        return getPrice(ticker, null);
    }

    /**
     * Primary overload used by services that know the asset type.
     * STOCK → Finnhub (falls back to dummy if not found).
     * BOND / CRYPTO / null → dummy.
     */
    public LivePriceResponse getPrice(String ticker, AssetType assetType) {
        String normalizedTicker = ticker == null ? "" : ticker.trim().toUpperCase();

        if (assetType == AssetType.STOCK) {
            Optional<FinnhubClient.QuoteData> quote = finnhubClient.getQuote(normalizedTicker);
            if (quote.isPresent()) {
                FinnhubClient.QuoteData q = quote.get();
                return new LivePriceResponse(
                        normalizedTicker,
                        q.currentPrice(),
                        q.change(),
                        q.changePercent(),
                        null);
            }
            // Finnhub returned nothing → fall through to dummy
        }

        // BOND, CRYPTO, or unknown/fallback → use dummy dataset
        return dummyMarketDataStore.getLatestQuote(normalizedTicker)
                .map(snapshot -> new LivePriceResponse(
                        normalizedTicker,
                        snapshot.currentPrice(),
                        snapshot.change(),
                        snapshot.changePercent(),
                        null))
                .orElseGet(() -> new LivePriceResponse(
                        normalizedTicker,
                        null, null, null,
                        "Ticker '" + normalizedTicker + "' not found in Finnhub or local dummy dataset"));
    }

    // ── 30-Day OHLCV History ───────────────────────────────────────────────────

    /**
     * Returns N days of daily OHLCV candles for a ticker.
     *
     * STOCK  → Finnhub /stock/candle (falls back to dummy series if not found)
     * BOND / CRYPTO → dummy price series transformed to DailyCandle list
     */
    public StockCandleResponse getHistory(String ticker, AssetType assetType, int days) {
        String normalizedTicker = ticker == null ? "" : ticker.trim().toUpperCase();

        if (assetType == AssetType.STOCK) {
            Optional<StockCandleResponse> candles = finnhubClient.getCandles(normalizedTicker, days);
            if (candles.isPresent()) {
                return candles.get();
            }
            // Finnhub returned nothing → fall through to dummy
        }

        // BOND, CRYPTO, or fallback → build from dummy price series
        return dummyMarketDataStore.getSeriesByTicker(normalizedTicker)
                .map(series -> {
                    List<DummyMarketDataStore.PricePoint> prices = series.prices();
                    // Take the last `days` entries from the 30-day dummy set
                    int startIdx = Math.max(0, prices.size() - days);
                    List<StockCandleResponse.DailyCandle> candles = new ArrayList<>();
                    for (int i = startIdx; i < prices.size(); i++) {
                        DummyMarketDataStore.PricePoint p = prices.get(i);
                        // Dummy data has only close price → use it for all OHLC fields
                        candles.add(new StockCandleResponse.DailyCandle(
                                p.date(),
                                p.close(),          // open (same — no dummy open data)
                                p.close(),          // high
                                p.close(),          // low
                                p.close(),          // close
                                BigDecimal.ZERO     // volume (not in dummy data)
                        ));
                    }
                    return new StockCandleResponse(normalizedTicker, "DUMMY", candles);
                })
                .orElseGet(() -> new StockCandleResponse(normalizedTicker, "NOT_FOUND", List.of()));
    }

    // ── Convenience: today's date as LocalDate ─────────────────────────────────
    private static LocalDate today() {
        return LocalDate.now();
    }
}
