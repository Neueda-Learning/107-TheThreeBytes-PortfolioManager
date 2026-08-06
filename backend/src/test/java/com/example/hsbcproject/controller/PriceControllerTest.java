package com.example.hsbcproject.controller;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.example.hsbcproject.dto.LivePriceResponse;
import com.example.hsbcproject.dto.StockCandleResponse;
import com.example.hsbcproject.service.DummyMarketDataStore;
import com.example.hsbcproject.service.PriceService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
class PriceControllerTest {
    @Test
    void getPriceDelegatesToService() {
        PriceService priceService = mock(PriceService.class);
        DummyMarketDataStore store = mock(DummyMarketDataStore.class);
        when(priceService.getPrice("AAPL", com.example.hsbcproject.domain.AssetType.STOCK))
                .thenReturn(new LivePriceResponse("AAPL", new BigDecimal("190.00"), BigDecimal.ONE, new BigDecimal("0.5"), null));
        PriceController controller = new PriceController(priceService, store);
        assertEquals("AAPL", controller.getPrice("AAPL", com.example.hsbcproject.domain.AssetType.STOCK).ticker());
    }
    @Test
    void getSeriesReturnsNotFoundWhenMissing() {
        PriceService priceService = mock(PriceService.class);
        DummyMarketDataStore store = mock(DummyMarketDataStore.class);
        when(store.getSeriesByTicker("MISS")).thenReturn(Optional.empty());
        PriceController controller = new PriceController(priceService, store);
        var response = controller.getSeries("MISS");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
