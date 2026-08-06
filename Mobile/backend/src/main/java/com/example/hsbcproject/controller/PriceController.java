package com.example.hsbcproject.controller;

import com.example.hsbcproject.domain.AssetType;
import com.example.hsbcproject.dto.LivePriceResponse;
import com.example.hsbcproject.dto.StockCandleResponse;
import com.example.hsbcproject.service.DummyMarketDataStore;
import com.example.hsbcproject.service.PriceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/prices")
@Tag(name = "Prices", description = "Live price data (Finnhub for stocks; local dummy dataset for bonds & crypto)")
public class PriceController {

    private final PriceService priceService;
    private final DummyMarketDataStore dummyMarketDataStore;

    public PriceController(PriceService priceService, DummyMarketDataStore dummyMarketDataStore) {
        this.priceService = priceService;
        this.dummyMarketDataStore = dummyMarketDataStore;
    }

    @GetMapping("/{ticker}")
    @Operation(summary = "Latest price, day-change and change% for a ticker. "
            + "STOCK tickers are fetched from Finnhub (live); BOND/CRYPTO from local dummy data.")
    public LivePriceResponse getPrice(@PathVariable String ticker,
                                      @RequestParam(required = false) AssetType assetType) {
        return priceService.getPrice(ticker, assetType);
    }

    @GetMapping("/history/{ticker}")
    @Operation(summary = "30-day daily OHLCV candle history for a ticker. "
            + "STOCK → Finnhub candles; BOND/CRYPTO → local dummy price series.")
    public StockCandleResponse getHistory(
            @PathVariable String ticker,
            @RequestParam(required = false) AssetType assetType,
            @RequestParam(defaultValue = "30") int days) {
        return priceService.getHistory(ticker, assetType, days);
    }

    @GetMapping("/series/{ticker}")
    @Operation(summary = "Full 30-day price series from local dummy dataset (all asset types). "
            + "For stocks, prefer /history/{ticker}?assetType=STOCK which uses live Finnhub data.")
    public ResponseEntity<DummyMarketDataStore.InstrumentSeries> getSeries(@PathVariable String ticker) {
        return dummyMarketDataStore.getSeriesByTicker(ticker)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/dummy-data")
    @Operation(summary = "All 30-day dummy data grouped by category: stocks, bonds, crypto (12 tickers each)")
    public Map<String, List<DummyMarketDataStore.InstrumentSeries>> getAllDummyData() {
        return dummyMarketDataStore.getAllSeries();
    }
}
