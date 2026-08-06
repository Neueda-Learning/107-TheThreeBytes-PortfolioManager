package com.example.hsbcproject.service;

import com.example.hsbcproject.domain.AssetType;
import com.example.hsbcproject.dto.LivePriceResponse;
import com.example.hsbcproject.dto.MarketInstrumentResponse;
import com.example.hsbcproject.dto.StockCandleResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

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

    private static final Map<AssetType, Map<String, String>> INSTRUMENT_NAMES = Map.of(
            AssetType.STOCK, Map.ofEntries(
                    Map.entry("AAPL", "Apple Inc."),
                    Map.entry("MSFT", "Microsoft Corporation"),
                    Map.entry("GOOGL", "Alphabet Inc."),
                    Map.entry("AMZN", "Amazon.com Inc."),
                    Map.entry("TSLA", "Tesla Inc."),
                    Map.entry("NVDA", "NVIDIA Corporation"),
                    Map.entry("JPM", "JPMorgan Chase & Co."),
                    Map.entry("V", "Visa Inc."),
                    Map.entry("DIS", "The Walt Disney Company"),
                    Map.entry("NFLX", "Netflix Inc."),
                    Map.entry("META", "Meta Platforms Inc."),
                    Map.entry("KO", "The Coca-Cola Company")),
            AssetType.BOND, Map.ofEntries(
                    Map.entry("UST2Y", "US Treasury 2-Year Bond"),
                    Map.entry("UST5Y", "US Treasury 5-Year Bond"),
                    Map.entry("UST10Y", "US Treasury 10-Year Bond"),
                    Map.entry("UST30Y", "US Treasury 30-Year Bond"),
                    Map.entry("LQD", "iShares iBoxx Investment Grade Corporate Bond ETF"),
                    Map.entry("HYG", "iShares iBoxx High Yield Corporate Bond ETF"),
                    Map.entry("TLT", "iShares 20+ Year Treasury Bond ETF"),
                    Map.entry("IEF", "iShares 7-10 Year Treasury Bond ETF"),
                    Map.entry("BND", "Vanguard Total Bond Market ETF"),
                    Map.entry("AGG", "iShares Core US Aggregate Bond ETF"),
                    Map.entry("MUB", "iShares National Muni Bond ETF"),
                    Map.entry("SHY", "iShares 1-3 Year Treasury Bond ETF")),
            AssetType.CRYPTO, Map.ofEntries(
                    Map.entry("BTC", "Bitcoin"),
                    Map.entry("ETH", "Ethereum"),
                    Map.entry("SOL", "Solana"),
                    Map.entry("ADA", "Cardano"),
                    Map.entry("XRP", "Ripple"),
                    Map.entry("DOT", "Polkadot"),
                    Map.entry("LINK", "Chainlink"),
                    Map.entry("LTC", "Litecoin"),
                    Map.entry("AVAX", "Avalanche"),
                    Map.entry("DOGE", "Dogecoin"),
                    Map.entry("BNB", "BNB"),
                    Map.entry("MATIC", "Polygon")));

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

    // ── Instrument Search ───────────────────────────────────────────────────────

    /**
     * Returns a searchable catalog of instruments for one asset type.
     * This keeps search metadata in the backend so UI components stay API-driven.
     */
    public List<MarketInstrumentResponse> searchInstruments(AssetType assetType, String query, int limit) {
        AssetType type = assetType == null ? AssetType.STOCK : assetType;
        Map<String, String> instruments = INSTRUMENT_NAMES.getOrDefault(type, Map.of());
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase();
        int cappedLimit = Math.max(1, Math.min(limit, 50));

        Stream<Map.Entry<String, String>> stream = instruments.entrySet().stream();
        if (!normalizedQuery.isEmpty()) {
            stream = stream.filter(entry -> {
                String haystack = (entry.getKey() + " " + entry.getValue()).toLowerCase();
                return haystack.contains(normalizedQuery);
            });
        }

        return stream
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .limit(cappedLimit)
                .map(entry -> new MarketInstrumentResponse(entry.getKey(), entry.getValue(), type, "USD"))
                .toList();
    }

    // ── Convenience: today's date as LocalDate ─────────────────────────────────
    private static LocalDate today() {
        return LocalDate.now();
    }
}
