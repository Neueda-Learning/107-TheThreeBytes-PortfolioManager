package com.example.hsbcproject.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.hsbcproject.dto.LivePriceResponse;
import com.example.hsbcproject.dto.StockQuoteResponse;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockMarketDataService Tests")
class StockMarketDataServiceTest {

    @Mock
    private PriceService priceService;

    @InjectMocks
    private StockMarketDataService service;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("getStockQuote_validKnownTicker_returnsCompleteData")
    void testGetStockQuote_ValidKnownTicker_ReturnsCompleteData() {
        when(priceService.getPrice("AAPL")).thenReturn(
                new LivePriceResponse("AAPL", new BigDecimal("150.00"), 
                        new BigDecimal("2.50"), new BigDecimal("1.69"), null)
        );

        StockQuoteResponse result = service.getStockQuote("AAPL");

        assertNotNull(result);
        assertEquals("AAPL", result.symbol());
        assertEquals("Apple Inc.", result.companyName());
        assertEquals(new BigDecimal("150.00"), result.currentPrice());
        assertEquals("Technology", result.sector());
        assertNull(result.errorMessage());
    }

    @Test
    @DisplayName("getStockQuote_validUnknownTicker_returnsPriceWithoutMetadata")
    void testGetStockQuote_ValidUnknownTicker_ReturnsPriceWithoutMetadata() {
        when(priceService.getPrice("UNKN")).thenReturn(
                new LivePriceResponse("UNKN", new BigDecimal("50.00"), null, null, null)
        );

        StockQuoteResponse result = service.getStockQuote("UNKN");

        assertNotNull(result);
        assertEquals("UNKN", result.symbol());
        assertNull(result.companyName());
        assertEquals(new BigDecimal("50.00"), result.currentPrice());
        assertNull(result.sector());
    }

    @Test
    @DisplayName("getStockQuote_nullTicker_throwsIllegalArgument")
    void testGetStockQuote_NullTicker_ThrowsIllegalArgument() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getStockQuote(null)
        );

        verify(priceService, never()).getPrice(anyString());
    }

    @Test
    @DisplayName("getStockQuote_blankTicker_throwsIllegalArgument")
    void testGetStockQuote_BlankTicker_ThrowsIllegalArgument() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getStockQuote("   ")
        );

        verify(priceService, never()).getPrice(anyString());
    }

    @Test
    @DisplayName("getStockQuote_emptyTicker_throwsIllegalArgument")
    void testGetStockQuote_EmptyTicker_ThrowsIllegalArgument() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getStockQuote("")
        );

        verify(priceService, never()).getPrice(anyString());
    }

    @Test
    @DisplayName("getStockQuote_lowerCaseTicker_convertsToUpperCase")
    void testGetStockQuote_LowerCaseTicker_ConvertsToUpperCase() {
        when(priceService.getPrice("AAPL")).thenReturn(
                new LivePriceResponse("AAPL", new BigDecimal("150.00"), null, null, null)
        );

        StockQuoteResponse result = service.getStockQuote("aapl");

        assertEquals("AAPL", result.symbol());
        assertEquals("Apple Inc.", result.companyName());
        verify(priceService, times(1)).getPrice("AAPL");
    }

    @Test
    @DisplayName("getStockQuote_mixedCaseTicker_convertsToUpperCase")
    void testGetStockQuote_MixedCaseTicker_ConvertsToUpperCase() {
        when(priceService.getPrice("AAPL")).thenReturn(
                new LivePriceResponse("AAPL", new BigDecimal("150.00"), null, null, null)
        );

        StockQuoteResponse result = service.getStockQuote("AaPl");

        assertEquals("AAPL", result.symbol());
        verify(priceService, times(1)).getPrice("AAPL");
    }

    @Test
    @DisplayName("getStockQuote_tickerWithWhitespace_trims")
    void testGetStockQuote_TickerWithWhitespace_Trims() {
        when(priceService.getPrice("AAPL")).thenReturn(
                new LivePriceResponse("AAPL", new BigDecimal("150.00"), null, null, null)
        );

        StockQuoteResponse result = service.getStockQuote("  AAPL  ");

        assertEquals("AAPL", result.symbol());
        verify(priceService, times(1)).getPrice("AAPL");
    }

    @Test
    @DisplayName("getStockQuote_msft_returnsMicrosoftData")
    void testGetStockQuote_Msft_ReturnsMicrosoftData() {
        when(priceService.getPrice("MSFT")).thenReturn(
                new LivePriceResponse("MSFT", new BigDecimal("380.00"), null, null, null)
        );

        StockQuoteResponse result = service.getStockQuote("MSFT");

        assertEquals("Microsoft Corporation", result.companyName());
        assertEquals("Technology", result.sector());
    }

    @Test
    @DisplayName("getStockQuote_nvda_returnsNvidiaData")
    void testGetStockQuote_Nvda_ReturnsNvidiaData() {
        when(priceService.getPrice("NVDA")).thenReturn(
                new LivePriceResponse("NVDA", new BigDecimal("875.00"), null, null, null)
        );

        StockQuoteResponse result = service.getStockQuote("NVDA");

        assertEquals("NVIDIA Corporation", result.companyName());
        assertEquals("Technology", result.sector());
    }

    @Test
    @DisplayName("getStockQuote_amzn_returnsAmazonData")
    void testGetStockQuote_Amzn_ReturnsAmazonData() {
        when(priceService.getPrice("AMZN")).thenReturn(
                new LivePriceResponse("AMZN", new BigDecimal("175.00"), null, null, null)
        );

        StockQuoteResponse result = service.getStockQuote("AMZN");

        assertEquals("Amazon.com Inc.", result.companyName());
        assertEquals("Consumer Discretionary", result.sector());
    }

    @Test
    @DisplayName("getStockQuote_googl_returnsAlphabetData")
    void testGetStockQuote_Googl_ReturnsAlphabetData() {
        when(priceService.getPrice("GOOGL")).thenReturn(
                new LivePriceResponse("GOOGL", new BigDecimal("140.00"), null, null, null)
        );

        StockQuoteResponse result = service.getStockQuote("GOOGL");

        assertEquals("Alphabet Inc.", result.companyName());
        assertEquals("Communication Services", result.sector());
    }

    @Test
    @DisplayName("getStockQuote_meta_returnsMetaData")
    void testGetStockQuote_Meta_ReturnsMetaData() {
        when(priceService.getPrice("META")).thenReturn(
                new LivePriceResponse("META", new BigDecimal("485.00"), null, null, null)
        );

        StockQuoteResponse result = service.getStockQuote("META");

        assertEquals("Meta Platforms Inc.", result.companyName());
        assertEquals("Communication Services", result.sector());
    }

    @Test
    @DisplayName("getStockQuote_tsla_returnsTeslaData")
    void testGetStockQuote_Tsla_ReturnsTeslaData() {
        when(priceService.getPrice("TSLA")).thenReturn(
                new LivePriceResponse("TSLA", new BigDecimal("245.00"), null, null, null)
        );

        StockQuoteResponse result = service.getStockQuote("TSLA");

        assertEquals("Tesla Inc.", result.companyName());
        assertEquals("Consumer Discretionary", result.sector());
    }

    @Test
    @DisplayName("getStockQuote_nflx_returnsNetflixData")
    void testGetStockQuote_Nflx_ReturnsNetflixData() {
        when(priceService.getPrice("NFLX")).thenReturn(
                new LivePriceResponse("NFLX", new BigDecimal("450.00"), null, null, null)
        );

        StockQuoteResponse result = service.getStockQuote("NFLX");

        assertEquals("Netflix Inc.", result.companyName());
        assertEquals("Communication Services", result.sector());
    }

    @Test
    @DisplayName("getStockQuote_jpm_returnsJPMorganData")
    void testGetStockQuote_Jpm_ReturnsJPMorganData() {
        when(priceService.getPrice("JPM")).thenReturn(
                new LivePriceResponse("JPM", new BigDecimal("195.00"), null, null, null)
        );

        StockQuoteResponse result = service.getStockQuote("JPM");

        assertEquals("JPMorgan Chase & Co.", result.companyName());
        assertEquals("Financials", result.sector());
    }

    @Test
    @DisplayName("getStockQuote_bac_returnsBankOfAmericaData")
    void testGetStockQuote_Bac_ReturnsBankOfAmericaData() {
        when(priceService.getPrice("BAC")).thenReturn(
                new LivePriceResponse("BAC", new BigDecimal("32.00"), null, null, null)
        );

        StockQuoteResponse result = service.getStockQuote("BAC");

        assertEquals("Bank of America Corp.", result.companyName());
        assertEquals("Financials", result.sector());
    }

    @Test
    @DisplayName("getStockQuote_priceServiceError_includersErrorMessage")
    void testGetStockQuote_PriceServiceError_IncludesErrorMessage() {
        when(priceService.getPrice("INVALID")).thenReturn(
                new LivePriceResponse("INVALID", null, null, null, "Ticker not found")
        );

        StockQuoteResponse result = service.getStockQuote("INVALID");

        assertNull(result.currentPrice());
        assertEquals("Ticker not found", result.errorMessage());
    }

    @Test
    @DisplayName("getStockQuote_zeroPrice_returnsZeroPrice")
    void testGetStockQuote_ZeroPrice_ReturnsZeroPrice() {
        when(priceService.getPrice("AAPL")).thenReturn(
                new LivePriceResponse("AAPL", BigDecimal.ZERO, null, null, null)
        );

        StockQuoteResponse result = service.getStockQuote("AAPL");

        assertEquals(BigDecimal.ZERO, result.currentPrice());
    }

    @Test
    @DisplayName("getStockQuote_largePrice_returnsCorrectly")
    void testGetStockQuote_LargePrice_ReturnsCorrectly() {
        BigDecimal largePrice = new BigDecimal("9999.99");
        when(priceService.getPrice("AAPL")).thenReturn(
                new LivePriceResponse("AAPL", largePrice, null, null, null)
        );

        StockQuoteResponse result = service.getStockQuote("AAPL");

        assertEquals(largePrice, result.currentPrice());
    }

    @Test
    @DisplayName("getStockQuote_multipleCallsSameTicker_callsPriceServiceEachTime")
    void testGetStockQuote_MultipleCallsSameTicker_CallsPriceServiceEachTime() {
        when(priceService.getPrice("AAPL")).thenReturn(
                new LivePriceResponse("AAPL", new BigDecimal("150.00"), null, null, null),
                new LivePriceResponse("AAPL", new BigDecimal("155.00"), null, null, null)
        );

        service.getStockQuote("AAPL");
        service.getStockQuote("AAPL");

        verify(priceService, times(2)).getPrice("AAPL");
    }
}

