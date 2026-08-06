package com.example.hsbcproject.service;

import com.example.hsbcproject.dto.StockQuoteResponse;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Resolves market data (company name, sector/industry, live price) for stocks.
 *
 * Company profile: Finnhub /stock/profile2 (falls back to static map if unavailable).
 * Live price: delegated to PriceService → FinnhubClient.
 */
@Service
public class StockMarketDataService {

    /** Static fallback: company name and sector for well-known tickers */
    private record StockReference(String companyName, String sector) {}

    private static final Map<String, StockReference> FALLBACK_REFERENCE = Map.ofEntries(
            Map.entry("AAPL",  new StockReference("Apple Inc.", "Technology")),
            Map.entry("MSFT",  new StockReference("Microsoft Corporation", "Technology")),
            Map.entry("NVDA",  new StockReference("NVIDIA Corporation", "Technology")),
            Map.entry("AMZN",  new StockReference("Amazon.com Inc.", "Consumer Discretionary")),
            Map.entry("GOOGL", new StockReference("Alphabet Inc.", "Communication Services")),
            Map.entry("META",  new StockReference("Meta Platforms Inc.", "Communication Services")),
            Map.entry("TSLA",  new StockReference("Tesla Inc.", "Consumer Discretionary")),
            Map.entry("NFLX",  new StockReference("Netflix Inc.", "Communication Services")),
            Map.entry("JPM",   new StockReference("JPMorgan Chase & Co.", "Financials")),
            Map.entry("BAC",   new StockReference("Bank of America Corp.", "Financials")));

    private final PriceService priceService;
    private final FinnhubClient finnhubClient;

    public StockMarketDataService(PriceService priceService, FinnhubClient finnhubClient) {
        this.priceService = priceService;
        this.finnhubClient = finnhubClient;
    }

    public StockQuoteResponse getStockQuote(String ticker) {
        if (ticker == null || ticker.isBlank()) {
            throw new IllegalArgumentException("ticker must not be blank");
        }

        String symbol = ticker.trim().toUpperCase();

        // 1. Try Finnhub for company profile (live name + industry)
        var profile = finnhubClient.getCompanyProfile(symbol);
        String companyName = profile.map(FinnhubClient.CompanyProfile::name).orElse(null);
        String sector      = profile.map(FinnhubClient.CompanyProfile::industry).orElse(null);

        // 2. Fall back to static map if Finnhub profile not available
        if (companyName == null) {
            StockReference ref = FALLBACK_REFERENCE.get(symbol);
            if (ref != null) {
                companyName = ref.companyName();
                sector      = ref.sector();
            }
        }

        // 3. Get live price via PriceService (→ Finnhub quote with dummy fallback)
        var priceResponse = priceService.getPrice(symbol, com.example.hsbcproject.domain.AssetType.STOCK);

        return new StockQuoteResponse(
                symbol,
                companyName,
                priceResponse.currentPrice(),
                sector,
                priceResponse.errorMessage());
    }
}
