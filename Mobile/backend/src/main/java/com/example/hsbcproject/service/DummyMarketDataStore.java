package com.example.hsbcproject.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * Loads dummy-market-data.json at startup and serves price data for all 36 tickers
 * (12 stocks, 12 bonds, 12 crypto) — each with a 30-day close price series.
 * Bonds additionally carry lock-in period and annual interest rate.
 */
@Component
public class DummyMarketDataStore {

    private static final Logger log = LoggerFactory.getLogger(DummyMarketDataStore.class);

    private final Map<String, InstrumentSeries> byTicker   = new LinkedHashMap<>();
    private final Map<String, List<InstrumentSeries>> byCategory = new LinkedHashMap<>();

    public DummyMarketDataStore(ObjectMapper objectMapper) {
        try {
            ClassPathResource resource = new ClassPathResource("static/dummy-market-data.json");
            try (InputStream is = resource.getInputStream()) {
                JsonNode root = objectMapper.readTree(is);
                for (String category : List.of("stocks", "bonds", "crypto")) {
                    JsonNode arr = root.get(category);
                    if (arr == null || !arr.isArray()) continue;
                    for (JsonNode item : arr) {
                        String ticker = item.get("ticker").asText();

                        List<PricePoint> prices = new ArrayList<>();
                        for (JsonNode p : item.get("prices")) {
                            LocalDate date  = LocalDate.parse(p.get("date").asText());
                            BigDecimal close = p.get("close").decimalValue();
                            prices.add(new PricePoint(date, close));
                        }

                        BondTerms bondTerms = null;
                        JsonNode bt = item.get("bondTerms");
                        if (bt != null && !bt.isNull()) {
                            bondTerms = new BondTerms(
                                    bt.get("lockInMonths").asInt(),
                                    bt.get("annualInterestRate").decimalValue());
                        }

                        InstrumentSeries series = new InstrumentSeries(category, ticker, prices, bondTerms);
                        byTicker.put(ticker, series);
                        byCategory.computeIfAbsent(category, k -> new ArrayList<>()).add(series);
                    }
                }
            }
            log.info("DummyMarketDataStore loaded {} tickers from dummy-market-data.json", byTicker.size());
        } catch (Exception e) {
            log.error("Failed to load dummy-market-data.json: {}", e.getMessage(), e);
        }
    }

    /** Returns the latest quote snapshot (current price, change, change%) for a ticker. */
    public Optional<QuoteSnapshot> getLatestQuote(String ticker) {
        return getSeriesByTicker(ticker).filter(s -> !s.prices().isEmpty()).map(series -> {
            List<PricePoint> prices = series.prices();
            BigDecimal currentPrice = prices.get(prices.size() - 1).close();
            BigDecimal change = BigDecimal.ZERO;
            BigDecimal changePercent = BigDecimal.ZERO;

            if (prices.size() >= 2) {
                BigDecimal previous = prices.get(prices.size() - 2).close();
                change = currentPrice.subtract(previous).setScale(4, RoundingMode.HALF_UP);
                if (previous.compareTo(BigDecimal.ZERO) != 0) {
                    changePercent = change
                            .divide(previous, 6, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(2, RoundingMode.HALF_UP);
                }
            }
            return new QuoteSnapshot(series.category(), series.ticker(),
                    currentPrice, change, changePercent, prices, series.bondTerms());
        });
    }

    /** Returns the full 30-day series (+ bond terms) for one ticker. */
    public Optional<InstrumentSeries> getSeriesByTicker(String ticker) {
        if (ticker == null || ticker.isBlank()) return Optional.empty();
        return Optional.ofNullable(byTicker.get(ticker.trim().toUpperCase()));
    }

    /** Returns all series grouped by category: stocks, bonds, crypto. */
    public Map<String, List<InstrumentSeries>> getAllSeries() {
        return byCategory;
    }

    // ── inner records ──────────────────────────────────────────────────────────

    public record PricePoint(LocalDate date, BigDecimal close) {}

    public record BondTerms(int lockInMonths, BigDecimal annualInterestRate) {}

    public record InstrumentSeries(
            String category,
            String ticker,
            List<PricePoint> prices,
            BondTerms bondTerms) {}

    public record QuoteSnapshot(
            String category,
            String ticker,
            BigDecimal currentPrice,
            BigDecimal change,
            BigDecimal changePercent,
            List<PricePoint> prices,
            BondTerms bondTerms) {}
}

