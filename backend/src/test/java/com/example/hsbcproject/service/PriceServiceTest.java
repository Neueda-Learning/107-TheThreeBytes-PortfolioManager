package com.example.hsbcproject.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.hsbcproject.dto.LivePriceResponse;
import com.example.hsbcproject.service.DummyMarketDataStore.QuoteSnapshot;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("PriceService Tests")
class PriceServiceTest {

    @Mock
    private DummyMarketDataStore dummyMarketDataStore;

    @InjectMocks
    private PriceService service;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("getPrice_validTicker_returnsPriceWithData")
    void testGetPrice_ValidTicker_ReturnsPriceWithData() {
        QuoteSnapshot snapshot = new QuoteSnapshot(
                "stocks",
                "AAPL",
                new BigDecimal("150.00"),
                new BigDecimal("2.50"),
                new BigDecimal("1.69"),
                null,
                null
        );

        when(dummyMarketDataStore.getLatestQuote("AAPL")).thenReturn(Optional.of(snapshot));

        LivePriceResponse result = service.getPrice("AAPL");

        assertNotNull(result);
        assertEquals("AAPL", result.ticker());
        assertEquals(new BigDecimal("150.00"), result.currentPrice());
        assertEquals(new BigDecimal("2.50"), result.change());
        assertEquals(new BigDecimal("1.69"), result.changePercent());
        assertNull(result.errorMessage());
    }

    @Test
    @DisplayName("getPrice_invalidTicker_returnsErrorMessage")
    void testGetPrice_InvalidTicker_ReturnsErrorMessage() {
        when(dummyMarketDataStore.getLatestQuote("INVALID")).thenReturn(Optional.empty());

        LivePriceResponse result = service.getPrice("INVALID");

        assertNotNull(result);
        assertEquals("INVALID", result.ticker());
        assertNull(result.currentPrice());
        assertNull(result.change());
        assertNull(result.changePercent());
        assertNotNull(result.errorMessage());
        assertTrue(result.errorMessage().contains("not found"));
    }

    @Test
    @DisplayName("getPrice_nullTicker_normalizesAndReturnsError")
    void testGetPrice_NullTicker_NormalizesAndReturnsError() {
        when(dummyMarketDataStore.getLatestQuote("")).thenReturn(Optional.empty());

        LivePriceResponse result = service.getPrice(null);

        assertNotNull(result);
        assertEquals("", result.ticker());
        assertNull(result.currentPrice());
        assertNotNull(result.errorMessage());
    }

    @Test
    @DisplayName("getPrice_lowerCaseTicker_convertsToUpperCase")
    void testGetPrice_LowerCaseTicker_ConvertsToUpperCase() {
        QuoteSnapshot snapshot = new QuoteSnapshot(
                "stocks",
                "AAPL",
                new BigDecimal("150.00"),
                new BigDecimal("2.50"),
                new BigDecimal("1.69"),
                null,
                null
        );

        when(dummyMarketDataStore.getLatestQuote("AAPL")).thenReturn(Optional.of(snapshot));

        LivePriceResponse result = service.getPrice("aapl");

        assertEquals("AAPL", result.ticker());
        verify(dummyMarketDataStore, times(1)).getLatestQuote("AAPL");
    }

    @Test
    @DisplayName("getPrice_tickerWithWhitespace_trimsAndConverts")
    void testGetPrice_TickerWithWhitespace_TrimsAndConverts() {
        QuoteSnapshot snapshot = new QuoteSnapshot(
                "stocks",
                "AAPL",
                new BigDecimal("150.00"),
                new BigDecimal("2.50"),
                new BigDecimal("1.69"),
                null,
                null
        );

        when(dummyMarketDataStore.getLatestQuote("AAPL")).thenReturn(Optional.of(snapshot));

        LivePriceResponse result = service.getPrice("  aapl  ");

        assertEquals("AAPL", result.ticker());
        verify(dummyMarketDataStore, times(1)).getLatestQuote("AAPL");
    }

    @Test
    @DisplayName("getPrice_zeroPrice_returnsPrice")
    void testGetPrice_ZeroPrice_ReturnsPrice() {
        QuoteSnapshot snapshot = new QuoteSnapshot(
                "stocks",
                "AAPL",
                BigDecimal.ZERO,
                new BigDecimal("-150.00"),
                new BigDecimal("-100.00"),
                null,
                null
        );

        when(dummyMarketDataStore.getLatestQuote("AAPL")).thenReturn(Optional.of(snapshot));

        LivePriceResponse result = service.getPrice("AAPL");

        assertEquals(BigDecimal.ZERO, result.currentPrice());
    }

    @Test
    @DisplayName("getPrice_negativeChange_returnsCorrectValues")
    void testGetPrice_NegativeChange_ReturnsCorrectValues() {
        QuoteSnapshot snapshot = new QuoteSnapshot(
                "stocks",
                "AAPL",
                new BigDecimal("140.00"),
                new BigDecimal("-10.00"),
                new BigDecimal("-6.67"),
                null,
                null
        );

        when(dummyMarketDataStore.getLatestQuote("AAPL")).thenReturn(Optional.of(snapshot));

        LivePriceResponse result = service.getPrice("AAPL");

        assertEquals(new BigDecimal("140.00"), result.currentPrice());
        assertEquals(new BigDecimal("-10.00"), result.change());
        assertEquals(new BigDecimal("-6.67"), result.changePercent());
    }

    @Test
    @DisplayName("getPrice_largePrice_returnsCorrectly")
    void testGetPrice_LargePrice_ReturnsCorrectly() {
        QuoteSnapshot snapshot = new QuoteSnapshot(
                "stocks",
                "AAPL",
                new BigDecimal("999999.9999"),
                new BigDecimal("100.00"),
                new BigDecimal("0.01"),
                null,
                null
        );

        when(dummyMarketDataStore.getLatestQuote("AAPL")).thenReturn(Optional.of(snapshot));

        LivePriceResponse result = service.getPrice("AAPL");

        assertEquals(new BigDecimal("999999.9999"), result.currentPrice());
    }

    @Test
    @DisplayName("getPrice_differentAssetCategories_returnsData")
    void testGetPrice_DifferentAssetCategories_ReturnsData() {
        QuoteSnapshot stockSnapshot = new QuoteSnapshot(
                "stocks",
                "AAPL",
                new BigDecimal("150.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                null,
                null
        );

        QuoteSnapshot cryptoSnapshot = new QuoteSnapshot(
                "crypto",
                "BTC",
                new BigDecimal("40000.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                null,
                null
        );

        when(dummyMarketDataStore.getLatestQuote("AAPL")).thenReturn(Optional.of(stockSnapshot));
        when(dummyMarketDataStore.getLatestQuote("BTC")).thenReturn(Optional.of(cryptoSnapshot));

        LivePriceResponse stockResult = service.getPrice("AAPL");
        LivePriceResponse cryptoResult = service.getPrice("BTC");

        assertEquals("AAPL", stockResult.ticker());
        assertEquals("BTC", cryptoResult.ticker());
    }

    @Test
    @DisplayName("getPrice_emptyTicker_normalizesToEmpty")
    void testGetPrice_EmptyTicker_NormalizesToEmpty() {
        when(dummyMarketDataStore.getLatestQuote("")).thenReturn(Optional.empty());

        LivePriceResponse result = service.getPrice("");

        assertEquals("", result.ticker());
        assertNull(result.currentPrice());
    }

    @Test
    @DisplayName("getPrice_mixedCaseWithSpecialCharacters_normalizes")
    void testGetPrice_MixedCaseWithSpecialCharacters_Normalizes() {
        QuoteSnapshot snapshot = new QuoteSnapshot(
                "stocks",
                "AAPL",
                new BigDecimal("150.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                null,
                null
        );

        when(dummyMarketDataStore.getLatestQuote("AAPL")).thenReturn(Optional.of(snapshot));

        LivePriceResponse result = service.getPrice("AaPl");

        assertEquals("AAPL", result.ticker());
        assertEquals(new BigDecimal("150.00"), result.currentPrice());
    }
}

