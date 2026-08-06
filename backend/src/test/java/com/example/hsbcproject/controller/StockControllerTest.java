package com.example.hsbcproject.controller;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.example.hsbcproject.dto.StockQuoteResponse;
import com.example.hsbcproject.service.StockMarketDataService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
class StockControllerTest {
    @Test
    void getStockPriceDelegatesToService() {
        StockMarketDataService service = mock(StockMarketDataService.class);
        when(service.getStockQuote("AAPL")).thenReturn(new StockQuoteResponse("AAPL", "Apple", new BigDecimal("100.00"), "Technology", null));
        StockController controller = new StockController(service);
        assertEquals("Apple", controller.getStockPrice("AAPL").companyName());
    }
}
