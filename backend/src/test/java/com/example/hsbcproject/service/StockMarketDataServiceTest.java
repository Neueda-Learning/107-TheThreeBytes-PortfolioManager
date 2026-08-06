package com.example.hsbcproject.service;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.example.hsbcproject.dto.LivePriceResponse;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
@ExtendWith(MockitoExtension.class)
class StockMarketDataServiceTest {
    @Mock private PriceService priceService;
    @Mock private FinnhubClient finnhubClient;
    @InjectMocks private StockMarketDataService service;
    @Test
    void getStockQuoteUsesFallbackReferenceWhenProfileMissing() {
        when(finnhubClient.getCompanyProfile("AAPL")).thenReturn(Optional.empty());
        when(priceService.getPrice("AAPL", com.example.hsbcproject.domain.AssetType.STOCK))
                .thenReturn(new LivePriceResponse("AAPL", new BigDecimal("180.00"), BigDecimal.ONE, new BigDecimal("0.5"), null));
        var response = service.getStockQuote("aapl");
        assertEquals("AAPL", response.symbol());
        assertEquals("Apple Inc.", response.companyName());
        assertEquals("Technology", response.sector());
    }
    @Test
    void getStockQuoteRejectsBlankTicker() {
        assertThrows(IllegalArgumentException.class, () -> service.getStockQuote(" "));
    }
}
