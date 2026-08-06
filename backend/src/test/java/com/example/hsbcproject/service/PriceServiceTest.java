package com.example.hsbcproject.service;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.example.hsbcproject.domain.AssetType;
import com.example.hsbcproject.dto.StockCandleResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
@ExtendWith(MockitoExtension.class)
class PriceServiceTest {
    @Mock private DummyMarketDataStore dummyMarketDataStore;
    @Mock private FinnhubClient finnhubClient;
    @InjectMocks private PriceService service;
    @Test
    void getPriceUsesFinnhubForStockWhenAvailable() {
        when(finnhubClient.getQuote("AAPL")).thenReturn(Optional.of(
                new FinnhubClient.QuoteData(new BigDecimal("150.00"), new BigDecimal("2.50"), new BigDecimal("1.69"), null, null, null, null)));
        var response = service.getPrice("aapl", AssetType.STOCK);
        assertEquals(new BigDecimal("150.00"), response.currentPrice());
        assertNull(response.errorMessage());
    }
    @Test
    void getPriceFallsBackToDummyData() {
        when(dummyMarketDataStore.getLatestQuote("BTC")).thenReturn(Optional.of(
                new DummyMarketDataStore.QuoteSnapshot("crypto", "BTC", new BigDecimal("60000.00"), BigDecimal.TEN, new BigDecimal("0.02"), List.of(), null)));
        var response = service.getPrice("btc", AssetType.CRYPTO);
        assertEquals(new BigDecimal("60000.00"), response.currentPrice());
    }
    @Test
    void getHistoryBuildsDummyCandlesWhenNoFinnhubData() {
        DummyMarketDataStore.InstrumentSeries series = new DummyMarketDataStore.InstrumentSeries(
                "crypto",
                "ETH",
                List.of(
                        new DummyMarketDataStore.PricePoint(LocalDate.now().minusDays(1), new BigDecimal("3000.00")),
                        new DummyMarketDataStore.PricePoint(LocalDate.now(), new BigDecimal("3100.00"))),
                null);
        when(dummyMarketDataStore.getSeriesByTicker("ETH")).thenReturn(Optional.of(series));
        StockCandleResponse response = service.getHistory("eth", AssetType.CRYPTO, 2);
        assertEquals("DUMMY", response.source());
        assertEquals(2, response.candles().size());
        assertEquals(new BigDecimal("3100.00"), response.candles().get(1).close());
    }
}
