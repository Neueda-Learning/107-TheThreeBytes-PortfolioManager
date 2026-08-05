package com.example.hsbcproject.service;

import com.example.hsbcproject.dto.LivePriceResponse;
import org.springframework.stereotype.Service;

/**
 * Resolves live price data from the local dummy-market-data.json dataset.
 * Replaces the previous Yahoo Finance / fallback-API implementation.
 */
@Service
public class PriceService {

    private final DummyMarketDataStore dummyMarketDataStore;

    public PriceService(DummyMarketDataStore dummyMarketDataStore) {
        this.dummyMarketDataStore = dummyMarketDataStore;
    }

    public LivePriceResponse getPrice(String ticker) {
        String normalizedTicker = ticker == null ? "" : ticker.trim().toUpperCase();
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
                        "Ticker '" + normalizedTicker + "' not found in local dummy dataset"));
    }
}
