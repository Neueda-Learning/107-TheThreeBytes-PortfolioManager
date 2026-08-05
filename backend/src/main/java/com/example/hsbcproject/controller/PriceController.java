package com.example.hsbcproject.controller;

import com.example.hsbcproject.dto.LivePriceResponse;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/prices")
@Tag(name = "Prices", description = "Price data served from local 30-day dummy dataset (stocks, bonds, crypto)")
public class PriceController {

    private final PriceService priceService;
    private final DummyMarketDataStore dummyMarketDataStore;

    public PriceController(PriceService priceService, DummyMarketDataStore dummyMarketDataStore) {
        this.priceService = priceService;
        this.dummyMarketDataStore = dummyMarketDataStore;
    }

    @GetMapping("/{ticker}")
    @Operation(summary = "Latest price, day-change and change% for a ticker (e.g. AAPL, UST2Y, BTC)")
    public LivePriceResponse getPrice(@PathVariable String ticker) {
        return priceService.getPrice(ticker);
    }

    @GetMapping("/series/{ticker}")
    @Operation(summary = "Full 30-day price series for one ticker; bonds also return lockInMonths and annualInterestRate")
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

