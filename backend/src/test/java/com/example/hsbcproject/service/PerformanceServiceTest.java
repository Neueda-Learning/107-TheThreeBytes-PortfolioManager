package com.example.hsbcproject.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.hsbcproject.domain.AssetType;
import com.example.hsbcproject.domain.PortfolioItem;
import com.example.hsbcproject.dto.LivePriceResponse;
import com.example.hsbcproject.dto.PerformanceItemResponse;
import com.example.hsbcproject.exception.ResourceNotFoundException;
import com.example.hsbcproject.repository.PortfolioItemRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("PerformanceService Tests")
class PerformanceServiceTest {

    @Mock
    private PortfolioItemRepository portfolioItemRepository;

    @Mock
    private PriceService priceService;

    @InjectMocks
    private PerformanceService service;

    private PortfolioItem testItem;

    @BeforeEach
    void setUp() {
        testItem = new PortfolioItem();
        testItem.setId(1L);
        testItem.setTicker("AAPL");
        testItem.setQuantity(10);
        testItem.setAssetType(AssetType.STOCK);
        testItem.setPurchasePrice(new BigDecimal("100.00"));
        testItem.setPurchaseDate(LocalDate.now().minusMonths(6));
        testItem.setName("Apple Inc.");
    }

    @Test
    @DisplayName("getAllPerformance_withItems_returnsPerformanceData")
    void testGetAllPerformance_WithItems_ReturnsPerformanceData() {
        List<PortfolioItem> items = Arrays.asList(testItem);
        when(portfolioItemRepository.findAll()).thenReturn(items);
        when(priceService.getPrice("AAPL")).thenReturn(
                new LivePriceResponse("AAPL", new BigDecimal("150.00"), 
                        new BigDecimal("2.50"), new BigDecimal("1.69"), null)
        );

        List<PerformanceItemResponse> result = service.getAllPerformance();

        assertNotNull(result);
        assertEquals(1, result.size());
        PerformanceItemResponse perf = result.get(0);
        assertEquals("AAPL", perf.ticker());
        assertEquals(new BigDecimal("150.00"), perf.currentPrice());
        
        verify(portfolioItemRepository, times(1)).findAll();
        verify(priceService, times(1)).getPrice("AAPL");
    }

    @Test
    @DisplayName("getAllPerformance_emptyPortfolio_returnsEmptyList")
    void testGetAllPerformance_EmptyPortfolio_ReturnsEmptyList() {
        when(portfolioItemRepository.findAll()).thenReturn(new ArrayList<>());

        List<PerformanceItemResponse> result = service.getAllPerformance();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(priceService, never()).getPrice(anyString());
    }

    @Test
    @DisplayName("getAllPerformance_priceNotAvailable_usesPurchasePrice")
    void testGetAllPerformance_PriceNotAvailable_UsesPurchasePrice() {
        List<PortfolioItem> items = Arrays.asList(testItem);
        when(portfolioItemRepository.findAll()).thenReturn(items);
        when(priceService.getPrice("AAPL")).thenReturn(
                new LivePriceResponse("AAPL", null, null, null, "Price not found")
        );

        List<PerformanceItemResponse> result = service.getAllPerformance();

        assertNotNull(result);
        assertEquals(1, result.size());
        PerformanceItemResponse perf = result.get(0);
        // Should use purchase price as fallback
        assertEquals(testItem.getPurchasePrice(), perf.currentPrice());
    }

    @Test
    @DisplayName("getPerformanceById_validId_returnsPerformance")
    void testGetPerformanceById_ValidId_ReturnsPerformance() {
        when(portfolioItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(priceService.getPrice("AAPL")).thenReturn(
                new LivePriceResponse("AAPL", new BigDecimal("150.00"),
                        new BigDecimal("2.50"), new BigDecimal("1.69"), null)
        );

        PerformanceItemResponse result = service.getPerformanceById(1L);

        assertNotNull(result);
        assertEquals("AAPL", result.ticker());
        assertEquals(new BigDecimal("150.00"), result.currentPrice());
    }

    @Test
    @DisplayName("getPerformanceById_invalidId_throwsResourceNotFoundException")
    void testGetPerformanceById_InvalidId_ThrowsResourceNotFoundException() {
        when(portfolioItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.getPerformanceById(999L)
        );

        verify(priceService, never()).getPrice(anyString());
    }

    @Test
    @DisplayName("buildPerformance_profitablePosition_calculatesCorrectGain")
    void testBuildPerformance_ProfitablePosition_CalculatesCorrectGain() {
        when(portfolioItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(priceService.getPrice("AAPL")).thenReturn(
                new LivePriceResponse("AAPL", new BigDecimal("150.00"), null, null, null)
        );

        PerformanceItemResponse result = service.getPerformanceById(1L);

        // Cost basis: 100 * 10 = 1000
        assertEquals(new BigDecimal("1000.00"), result.costBasis());
        // Current value: 150 * 10 = 1500
        assertEquals(new BigDecimal("1500.00"), result.currentValue());
        // Unrealized gain: 1500 - 1000 = 500
        assertEquals(new BigDecimal("500.00"), result.unrealizedGain());
        // Gain %: 500 / 1000 * 100 = 50%
        assertEquals(new BigDecimal("50.00"), result.unrealizedGainPct());
    }

    @Test
    @DisplayName("buildPerformance_unprofitablePosition_calculatesNegativeGain")
    void testBuildPerformance_UnprofitablePosition_CalculatesNegativeGain() {
        when(portfolioItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(priceService.getPrice("AAPL")).thenReturn(
                new LivePriceResponse("AAPL", new BigDecimal("80.00"), null, null, null)
        );

        PerformanceItemResponse result = service.getPerformanceById(1L);

        // Cost basis: 100 * 10 = 1000
        assertEquals(new BigDecimal("1000.00"), result.costBasis());
        // Current value: 80 * 10 = 800
        assertEquals(new BigDecimal("800.00"), result.currentValue());
        // Unrealized loss: 800 - 1000 = -200
        assertEquals(new BigDecimal("-200.00"), result.unrealizedGain());
        // Loss %: -200 / 1000 * 100 = -20%
        assertEquals(new BigDecimal("-20.00"), result.unrealizedGainPct());
    }

    @Test
    @DisplayName("buildPerformance_breakEven_calculatesZeroGain")
    void testBuildPerformance_BreakEven_CalculatesZeroGain() {
        when(portfolioItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(priceService.getPrice("AAPL")).thenReturn(
                new LivePriceResponse("AAPL", new BigDecimal("100.00"), null, null, null)
        );

        PerformanceItemResponse result = service.getPerformanceById(1L);

        assertEquals(new BigDecimal("1000.00"), result.costBasis());
        assertEquals(new BigDecimal("1000.00"), result.currentValue());
        assertEquals(new BigDecimal("0.00"), result.unrealizedGain());
        assertEquals(new BigDecimal("0.00"), result.unrealizedGainPct());
    }

    @Test
    @DisplayName("buildPerformance_holdingDaysCalculation_isCorrect")
    void testBuildPerformance_HoldingDaysCalculation_IsCorrect() {
        testItem.setPurchaseDate(LocalDate.now().minusDays(30));
        when(portfolioItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(priceService.getPrice("AAPL")).thenReturn(
                new LivePriceResponse("AAPL", new BigDecimal("150.00"), null, null, null)
        );

        PerformanceItemResponse result = service.getPerformanceById(1L);

        assertEquals(30, result.holdingDays());
    }

    @Test
    @DisplayName("buildPerformance_recentPurchase_showsSmallHoldingDays")
    void testBuildPerformance_RecentPurchase_ShowsSmallHoldingDays() {
        testItem.setPurchaseDate(LocalDate.now().minusDays(1));
        when(portfolioItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(priceService.getPrice("AAPL")).thenReturn(
                new LivePriceResponse("AAPL", new BigDecimal("150.00"), null, null, null)
        );

        PerformanceItemResponse result = service.getPerformanceById(1L);

        assertEquals(1, result.holdingDays());
    }

    @Test
    @DisplayName("buildPerformance_singleShare_calculatesCorrectly")
    void testBuildPerformance_SingleShare_CalculatesCorrectly() {
        testItem.setQuantity(1);
        when(portfolioItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(priceService.getPrice("AAPL")).thenReturn(
                new LivePriceResponse("AAPL", new BigDecimal("200.00"), null, null, null)
        );

        PerformanceItemResponse result = service.getPerformanceById(1L);

        assertEquals(new BigDecimal("100.00"), result.costBasis());
        assertEquals(new BigDecimal("200.00"), result.currentValue());
        assertEquals(new BigDecimal("100.00"), result.unrealizedGain());
    }

    @Test
    @DisplayName("buildPerformance_largeQuantity_handlesCorrectly")
    void testBuildPerformance_LargeQuantity_HandlesCorrectly() {
        testItem.setQuantity(10000);
        when(portfolioItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(priceService.getPrice("AAPL")).thenReturn(
                new LivePriceResponse("AAPL", new BigDecimal("150.00"), null, null, null)
        );

        PerformanceItemResponse result = service.getPerformanceById(1L);

        // Cost basis: 100 * 10000 = 1,000,000
        assertEquals(new BigDecimal("1000000.00"), result.costBasis());
        // Current value: 150 * 10000 = 1,500,000
        assertEquals(new BigDecimal("1500000.00"), result.currentValue());
    }

    @Test
    @DisplayName("buildPerformance_decimalPrice_calculatesWithPrecision")
    void testBuildPerformance_DecimalPrice_CalculatesWithPrecision() {
        testItem.setPurchasePrice(new BigDecimal("99.99"));
        when(portfolioItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(priceService.getPrice("AAPL")).thenReturn(
                new LivePriceResponse("AAPL", new BigDecimal("150.25"), null, null, null)
        );

        PerformanceItemResponse result = service.getPerformanceById(1L);

        assertEquals(new BigDecimal("999.90"), result.costBasis());
        assertEquals(new BigDecimal("1502.50"), result.currentValue());
    }

    @Test
    @DisplayName("getAllPerformance_multipleItems_returnAllWithCorrectCalculations")
    void testGetAllPerformance_MultipleItems_ReturnAllWithCorrectCalculations() {
        PortfolioItem item1 = new PortfolioItem();
        item1.setId(1L);
        item1.setTicker("AAPL");
        item1.setQuantity(10);
        item1.setAssetType(AssetType.STOCK);
        item1.setPurchasePrice(new BigDecimal("100.00"));
        item1.setPurchaseDate(LocalDate.now().minusMonths(6));

        PortfolioItem item2 = new PortfolioItem();
        item2.setId(2L);
        item2.setTicker("MSFT");
        item2.setQuantity(5);
        item2.setAssetType(AssetType.STOCK);
        item2.setPurchasePrice(new BigDecimal("200.00"));
        item2.setPurchaseDate(LocalDate.now().minusMonths(3));

        when(portfolioItemRepository.findAll()).thenReturn(Arrays.asList(item1, item2));
        when(priceService.getPrice("AAPL")).thenReturn(
                new LivePriceResponse("AAPL", new BigDecimal("150.00"), null, null, null)
        );
        when(priceService.getPrice("MSFT")).thenReturn(
                new LivePriceResponse("MSFT", new BigDecimal("250.00"), null, null, null)
        );

        List<PerformanceItemResponse> result = service.getAllPerformance();

        assertEquals(2, result.size());
        assertEquals("AAPL", result.get(0).ticker());
        assertEquals("MSFT", result.get(1).ticker());
    }

    @Test
    @DisplayName("buildPerformance_zeroGainPercent_withZeroCostBasis")
    void testBuildPerformance_ZeroGainPercent_WithZeroCostBasis() {
        testItem.setQuantity(0);
        testItem.setPurchasePrice(new BigDecimal("100.00"));
        when(portfolioItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(priceService.getPrice("AAPL")).thenReturn(
                new LivePriceResponse("AAPL", new BigDecimal("150.00"), null, null, null)
        );

        PerformanceItemResponse result = service.getPerformanceById(1L);

        assertEquals(new BigDecimal("0.00"), result.costBasis());
        assertEquals(new BigDecimal("0.00"), result.currentValue());
        assertEquals(BigDecimal.ZERO, result.unrealizedGainPct());
    }
}

