package com.example.hsbcproject.service;

import com.example.hsbcproject.dto.LivePriceResponse;
import com.example.hsbcproject.dto.StockQuoteResponse;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Resolves market data (company name, sector, live price) for stocks.
 * Kept separate from portfolio persistence so bond/crypto market data can be added independently later.
 */
@Service
public class StockMarketDataService {

    private record StockReference(String companyName, String sector) {}

    // Static reference metadata (company name / sector) - the price API has no such metadata endpoint.
    private static final Map<String, StockReference> STOCK_REFERENCE_DATA = Map.ofEntries(
            Map.entry("AAPL", new StockReference("Apple Inc.", "Technology")),
            Map.entry("MSFT", new StockReference("Microsoft Corporation", "Technology")),
            Map.entry("NVDA", new StockReference("NVIDIA Corporation", "Technology")),
            Map.entry("AMZN", new StockReference("Amazon.com Inc.", "Consumer Discretionary")),
            Map.entry("GOOGL", new StockReference("Alphabet Inc.", "Communication Services")),
            Map.entry("META", new StockReference("Meta Platforms Inc.", "Communication Services")),
            Map.entry("TSLA", new StockReference("Tesla Inc.", "Consumer Discretionary")),
            Map.entry("NFLX", new StockReference("Netflix Inc.", "Communication Services")),
            Map.entry("JPM", new StockReference("JPMorgan Chase & Co.", "Financials")),
            Map.entry("BAC", new StockReference("Bank of America Corp.", "Financials")));

    private final PriceService priceService;

    public StockMarketDataService(PriceService priceService) {
        this.priceService = priceService;
    }

    public StockQuoteResponse getStockQuote(String ticker) {
        if (ticker == null || ticker.isBlank()) {
            throw new IllegalArgumentException("ticker must not be blank");
        }

        String symbol = ticker.trim().toUpperCase();
        StockReference reference = STOCK_REFERENCE_DATA.get(symbol);
        LivePriceResponse priceResponse = priceService.getPrice(symbol);

        return new StockQuoteResponse(
                symbol,
                reference != null ? reference.companyName() : null,
                priceResponse.currentPrice(),
                reference != null ? reference.sector() : null,
                priceResponse.errorMessage());
    }
}
