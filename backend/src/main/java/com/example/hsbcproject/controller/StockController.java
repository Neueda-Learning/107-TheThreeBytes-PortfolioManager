package com.example.hsbcproject.controller;

import com.example.hsbcproject.dto.StockQuoteResponse;
import com.example.hsbcproject.service.StockMarketDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stocks")
@Tag(name = "Stocks", description = "Look up stock market data (company name, sector, live price)")
public class StockController {

    private final StockMarketDataService stockMarketDataService;

    public StockController(StockMarketDataService stockMarketDataService) {
        this.stockMarketDataService = stockMarketDataService;
    }

    @GetMapping("/{ticker}/price")
    @Operation(summary = "Get company name, sector and current price for a stock ticker (e.g. AAPL, MSFT)")
    public StockQuoteResponse getStockPrice(@PathVariable String ticker) {
        return stockMarketDataService.getStockQuote(ticker);
    }
}
