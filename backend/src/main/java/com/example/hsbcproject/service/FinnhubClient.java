package com.example.hsbcproject.service;

import com.example.hsbcproject.dto.StockCandleResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HTTP client for the Finnhub REST API.
 *
 * Used ONLY for STOCK tickers. BOND and CRYPTO continue to use DummyMarketDataStore.
 *
 * Two endpoints:
 *   1. /api/v1/quote             → current price, change, change%
 *   2. /api/v1/stock/candle      → 30-day daily OHLCV for charting
 *
 * Both results are cached to avoid hammering the free-plan rate limit (60 req/min):
 *   - Quote cache  : 60 seconds (configurable via finnhub.cache.quote-ttl-seconds)
 *   - Candle cache : 24 hours   (configurable via finnhub.cache.candle-ttl-hours)
 */
@Component
public class FinnhubClient {

    private static final Logger log = LoggerFactory.getLogger(FinnhubClient.class);
    @Value("${finnhub.api.key:}")
    private String apiKey;

    @Value("${finnhub.cache.quote-ttl-seconds:60}")
    private long quoteTtlSeconds;

    @Value("${finnhub.cache.candle-ttl-hours:24}")
    private long candleTtlHours;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    // ── simple in-memory TTL caches ────────────────────────────────────────────
    private final ConcurrentHashMap<String, CachedEntry<QuoteData>> quoteCache  = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CachedEntry<StockCandleResponse>> candleCache = new ConcurrentHashMap<>();

    public FinnhubClient(ObjectMapper objectMapper,
                         @Value("${finnhub.api.base-url:https://finnhub.io/api/v1}") String baseUrl) {
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Accept", "application/json")
                .build();
    }

    // ── Public API ─────────────────────────────────────────────────────────────

    /**
     * Fetch the latest stock quote (current price, day change, change%).
     * Returns empty if ticker not found or any error occurs.
     */
    public Optional<QuoteData> getQuote(String ticker) {
        if (apiKey == null || apiKey.isBlank()) {
            return Optional.empty();
        }
        String key = ticker.toUpperCase();

        // Check cache first
        CachedEntry<QuoteData> cached = quoteCache.get(key);
        if (cached != null && !cached.isExpired(quoteTtlSeconds * 1_000L)) {
            log.debug("Quote cache hit for {}", key);
            return Optional.of(cached.value());
        }

        try {
            String json = restClient.get()
                    .uri("/quote?symbol={ticker}&token={token}", key, apiKey)
                    .retrieve()
                    .body(String.class);

            JsonNode node = objectMapper.readTree(json);

            // Finnhub returns c=0 when ticker is unknown
            BigDecimal currentPrice = node.path("c").decimalValue();
            if (currentPrice.compareTo(BigDecimal.ZERO) == 0) {
                log.warn("Finnhub returned zero price for ticker '{}' — treating as not found", key);
                return Optional.empty();
            }

            QuoteData quote = new QuoteData(
                    currentPrice,
                    node.path("d").decimalValue(),
                    node.path("dp").decimalValue(),
                    node.path("h").decimalValue(),
                    node.path("l").decimalValue(),
                    node.path("o").decimalValue(),
                    node.path("pc").decimalValue()
            );

            quoteCache.put(key, new CachedEntry<>(quote));
            log.info("Finnhub quote fetched for {}: price={}", key, currentPrice);
            return Optional.of(quote);

        } catch (Exception e) {
            log.error("Finnhub quote fetch failed for ticker '{}': {}", key, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Fetch the last {@code days} calendar days of daily OHLCV candles.
     * Returns empty if ticker not found or any error occurs.
     */
    public Optional<StockCandleResponse> getCandles(String ticker, int days) {
        if (apiKey == null || apiKey.isBlank()) {
            return Optional.empty();
        }
        String key = ticker.toUpperCase();

        // Check cache first
        CachedEntry<StockCandleResponse> cached = candleCache.get(key);
        if (cached != null && !cached.isExpired(candleTtlHours * 3_600_000L)) {
            log.debug("Candle cache hit for {}", key);
            return Optional.of(cached.value());
        }

        try {
            long to   = Instant.now().getEpochSecond();
            long from = Instant.now().minusSeconds((long) days * 24 * 3600).getEpochSecond();

            String json = restClient.get()
                    .uri("/stock/candle?symbol={ticker}&resolution=D&from={from}&to={to}&token={token}",
                            key, from, to, apiKey)
                    .retrieve()
                    .body(String.class);

            JsonNode node = objectMapper.readTree(json);

            // "no_data" means weekend/holiday gap or unknown ticker
            if (!"ok".equals(node.path("s").asText())) {
                log.warn("Finnhub returned no_data for candle '{}' ", key);
                return Optional.empty();
            }

            JsonNode timestamps = node.path("t");
            JsonNode opens      = node.path("o");
            JsonNode highs      = node.path("h");
            JsonNode lows       = node.path("l");
            JsonNode closes     = node.path("c");
            JsonNode volumes    = node.path("v");

            List<StockCandleResponse.DailyCandle> candles = new ArrayList<>();
            for (int i = 0; i < timestamps.size(); i++) {
                LocalDate date = Instant.ofEpochSecond(timestamps.get(i).asLong())
                        .atZone(ZoneId.of("UTC"))
                        .toLocalDate();

                candles.add(new StockCandleResponse.DailyCandle(
                        date,
                        opens.get(i).decimalValue(),
                        highs.get(i).decimalValue(),
                        lows.get(i).decimalValue(),
                        closes.get(i).decimalValue(),
                        volumes.get(i).decimalValue()
                ));
            }

            StockCandleResponse response = new StockCandleResponse(key, "FINNHUB", candles);
            candleCache.put(key, new CachedEntry<>(response));
            log.info("Finnhub candles fetched for {}: {} days", key, candles.size());
            return Optional.of(response);

        } catch (Exception e) {
            log.error("Finnhub candle fetch failed for ticker '{}': {}", key, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Fetch company profile (name, sector/industry) for a stock ticker.
     * Returns empty if not found.
     */
    public Optional<CompanyProfile> getCompanyProfile(String ticker) {
        if (apiKey == null || apiKey.isBlank()) {
            return Optional.empty();
        }
        String key = ticker.toUpperCase();
        try {
            String json = restClient.get()
                    .uri("/stock/profile2?symbol={ticker}&token={token}", key, apiKey)
                    .retrieve()
                    .body(String.class);

            JsonNode node = objectMapper.readTree(json);
            String name = node.path("name").asText(null);
            if (name == null || name.isBlank()) {
                return Optional.empty();
            }

            return Optional.of(new CompanyProfile(
                    name,
                    node.path("finnhubIndustry").asText(null)
            ));

        } catch (Exception e) {
            log.error("Finnhub profile fetch failed for ticker '{}': {}", key, e.getMessage());
            return Optional.empty();
        }
    }

    // ── Inner records ──────────────────────────────────────────────────────────

    /** Current price snapshot from /quote */
    public record QuoteData(
            BigDecimal currentPrice,
            BigDecimal change,
            BigDecimal changePercent,
            BigDecimal dayHigh,
            BigDecimal dayLow,
            BigDecimal open,
            BigDecimal previousClose) {
    }

    /** Company name and industry from /stock/profile2 */
    public record CompanyProfile(String name, String industry) {}

    /** Generic TTL wrapper */
    private record CachedEntry<T>(T value, long cachedAtMs) {
        CachedEntry(T value) {
            this(value, System.currentTimeMillis());
        }

        boolean isExpired(long ttlMs) {
            return System.currentTimeMillis() - cachedAtMs > ttlMs;
        }
    }
}
