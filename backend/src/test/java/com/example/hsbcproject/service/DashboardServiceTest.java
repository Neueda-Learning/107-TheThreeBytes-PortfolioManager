package com.example.hsbcproject.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.hsbcproject.domain.AssetType;
import com.example.hsbcproject.domain.PortfolioItem;
import com.example.hsbcproject.dto.DashboardResponse;
import com.example.hsbcproject.dto.LivePriceResponse;
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
@DisplayName("DashboardService Tests")
class DashboardServiceTest {

    @Mock
    private PortfolioItemRepository portfolioItemRepository;

    @Mock
    private PriceService priceService;

    @InjectMocks
    private DashboardService service;

    private PortfolioItem testItem;

    @BeforeEach
    void setUp() {
        testItem = new PortfolioItem();
        testItem.setId(1L);
        testItem.setTicker("AAPL");
        testItem.setQuantity(10);
        testItem.setAssetType(AssetType.STOCK);
        testItem.setPurchasePrice(new BigDecimal("100.00"));
        testItem.setPurchaseDate(LocalDate.now());
        testItem.setName("Apple Inc.");
    }

    @Test
    @DisplayName("getCombinedDashboard_withItems_returnsDashboard")
    void testGetCombinedDashboard_WithItems_ReturnsDashboard() {
        List<PortfolioItem> items = Arrays.asList(testItem);
        when(portfolioItemRepository.findAll()).thenReturn(items);
        when(priceService.getPrice("AAPL")).thenReturn(
                new LivePriceResponse("AAPL", new BigDecimal("150.00"), null, null, null)
        );

        DashboardResponse result = service.getCombinedDashboard();

        assertNotNull(result);
        assertEquals(1, result.totalPositions());
        assertEquals(10, result.totalQuantity());
        assertEquals(new BigDecimal("1000.00"), result.totalCostBasis());
        assertEquals(new BigDecimal("1500.00"), result.estimatedTotalValue());
    }

    @Test
    @DisplayName("getCombinedDashboard_emptyPortfolio_returnsZeroValues")
    void testGetCombinedDashboard_EmptyPortfolio_ReturnsZeroValues() {
        when(portfolioItemRepository.findAll()).thenReturn(new ArrayList<>());

        DashboardResponse result = service.getCombinedDashboard();

        assertNotNull(result);
        assertEquals(0, result.totalPositions());
        assertEquals(0, result.totalQuantity());
        assertEquals(new BigDecimal("0.00"), result.totalCostBasis());
        assertEquals(new BigDecimal("0.00"), result.estimatedTotalValue());
    }

    @Test
    @DisplayName("getCombinedDashboard_profitablePosition_calculatesGainCorrectly")
    void testGetCombinedDashboard_ProfitablePosition_CalculatesGainCorrectly() {
        List<PortfolioItem> items = Arrays.asList(testItem);
        when(portfolioItemRepository.findAll()).thenReturn(items);
        when(priceService.getPrice("AAPL")).thenReturn(
                new LivePriceResponse("AAPL", new BigDecimal("150.00"), null, null, null)
        );

        DashboardResponse result = service.getCombinedDashboard();

        // Cost basis: 100 * 10 = 1000
        assertEquals(new BigDecimal("1000.00"), result.totalCostBasis());
        // Current value: 150 * 10 = 1500
        assertEquals(new BigDecimal("1500.00"), result.estimatedTotalValue());
        // Gain: 500
        assertEquals(new BigDecimal("500.00"), result.unrealizedGainLoss());
        // Gain %: 50%
        assertEquals(new BigDecimal("50.00"), result.unrealizedGainLossPct());
    }

    @Test
    @DisplayName("getCombinedDashboard_unprofitablePosition_calculatesLossCorrectly")
    void testGetCombinedDashboard_UnprofitablePosition_CalculatesLossCorrectly() {
        List<PortfolioItem> items = Arrays.asList(testItem);
        when(portfolioItemRepository.findAll()).thenReturn(items);
        when(priceService.getPrice("AAPL")).thenReturn(
                new LivePriceResponse("AAPL", new BigDecimal("80.00"), null, null, null)
        );

        DashboardResponse result = service.getCombinedDashboard();

        assertEquals(new BigDecimal("1000.00"), result.totalCostBasis());
        assertEquals(new BigDecimal("800.00"), result.estimatedTotalValue());
        assertEquals(new BigDecimal("-200.00"), result.unrealizedGainLoss());
        assertEquals(new BigDecimal("-20.00"), result.unrealizedGainLossPct());
    }

    @Test
    @DisplayName("getDashboardByAssetType_stock_returnsStockOnly")
    void testGetDashboardByAssetType_Stock_ReturnsStockOnly() {
        List<PortfolioItem> items = Arrays.asList(testItem);
        when(portfolioItemRepository.findAll()).thenReturn(items);
        when(priceService.getPrice("AAPL")).thenReturn(
                new LivePriceResponse("AAPL", new BigDecimal("150.00"), null, null, null)
        );

        DashboardResponse result = service.getDashboardByAssetType(AssetType.STOCK);

        assertNotNull(result);
        assertEquals(1, result.totalPositions());
    }

    @Test
    @DisplayName("getDashboardByAssetType_bond_returnsBondOnly")
    void testGetDashboardByAssetType_Bond_ReturnsBondOnly() {
        PortfolioItem bondItem = new PortfolioItem();
        bondItem.setId(2L);
        bondItem.setTicker("BOND1");
        bondItem.setQuantity(10);
        bondItem.setAssetType(AssetType.BOND);
        bondItem.setPurchasePrice(new BigDecimal("100.00"));
        bondItem.setPurchaseDate(LocalDate.now());

        List<PortfolioItem> items = Arrays.asList(testItem, bondItem);
        when(portfolioItemRepository.findAll()).thenReturn(items);
        when(priceService.getPrice("BOND1")).thenReturn(
                new LivePriceResponse("BOND1", new BigDecimal("102.00"), null, null, null)
        );

        DashboardResponse result = service.getDashboardByAssetType(AssetType.BOND);

        assertNotNull(result);
        assertEquals(1, result.totalPositions());
    }

    @Test
    @DisplayName("buildDashboard_multipleItems_aggregatesCorrectly")
    void testBuildDashboard_MultipleItems_AggregatesCorrectly() {
        PortfolioItem item1 = new PortfolioItem();
        item1.setId(1L);
        item1.setTicker("AAPL");
        item1.setQuantity(10);
        item1.setAssetType(AssetType.STOCK);
        item1.setPurchasePrice(new BigDecimal("100.00"));
        item1.setPurchaseDate(LocalDate.now());

        PortfolioItem item2 = new PortfolioItem();
        item2.setId(2L);
        item2.setTicker("MSFT");
        item2.setQuantity(5);
        item2.setAssetType(AssetType.STOCK);
        item2.setPurchasePrice(new BigDecimal("200.00"));
        item2.setPurchaseDate(LocalDate.now());

        List<PortfolioItem> items = Arrays.asList(item1, item2);
        when(portfolioItemRepository.findAll()).thenReturn(items);
        when(priceService.getPrice("AAPL")).thenReturn(
                new LivePriceResponse("AAPL", new BigDecimal("150.00"), null, null, null)
        );
        when(priceService.getPrice("MSFT")).thenReturn(
                new LivePriceResponse("MSFT", new BigDecimal("250.00"), null, null, null)
        );

        DashboardResponse result = service.getCombinedDashboard();

        assertEquals(2, result.totalPositions());
        assertEquals(15, result.totalQuantity());
        // Cost basis: (100*10) + (200*5) = 1000 + 1000 = 2000
        assertEquals(new BigDecimal("2000.00"), result.totalCostBasis());
        // Current value: (150*10) + (250*5) = 1500 + 1250 = 2750
        assertEquals(new BigDecimal("2750.00"), result.estimatedTotalValue());
    }

    @Test
    @DisplayName("buildDashboard_groupsByAssetType_createsCorrectMap")
    void testBuildDashboard_GroupsByAssetType_CreatesCorrectMap() {
        PortfolioItem stock = new PortfolioItem();
        stock.setId(1L);
        stock.setTicker("AAPL");
        stock.setQuantity(10);
        stock.setAssetType(AssetType.STOCK);
        stock.setPurchasePrice(new BigDecimal("100.00"));
        stock.setPurchaseDate(LocalDate.now());

        PortfolioItem bond = new PortfolioItem();
        bond.setId(2L);
        bond.setTicker("BOND1");
        bond.setQuantity(5);
        bond.setAssetType(AssetType.BOND);
        bond.setPurchasePrice(new BigDecimal("100.00"));
        bond.setPurchaseDate(LocalDate.now());

        List<PortfolioItem> items = Arrays.asList(stock, bond);
        when(portfolioItemRepository.findAll()).thenReturn(items);
        when(priceService.getPrice("AAPL")).thenReturn(
                new LivePriceResponse("AAPL", new BigDecimal("150.00"), null, null, null)
        );
        when(priceService.getPrice("BOND1")).thenReturn(
                new LivePriceResponse("BOND1", new BigDecimal("105.00"), null, null, null)
        );

        DashboardResponse result = service.getCombinedDashboard();

        assertTrue(result.quantityByAssetType().containsKey("STOCK"));
        assertTrue(result.quantityByAssetType().containsKey("BOND"));
        assertEquals(10L, result.quantityByAssetType().get("STOCK"));
        assertEquals(5L, result.quantityByAssetType().get("BOND"));
    }

    @Test
    @DisplayName("buildDashboard_priceNotAvailable_usesPurchasePrice")
    void testBuildDashboard_PriceNotAvailable_UsesPurchasePrice() {
        List<PortfolioItem> items = Arrays.asList(testItem);
        when(portfolioItemRepository.findAll()).thenReturn(items);
        when(priceService.getPrice("AAPL")).thenReturn(
                new LivePriceResponse("AAPL", null, null, null, "Not found")
        );

        DashboardResponse result = service.getCombinedDashboard();

        // Should fall back to purchase price
        assertEquals(new BigDecimal("1000.00"), result.estimatedTotalValue());
        assertEquals(new BigDecimal("1000.00"), result.totalCostBasis());
    }

    @Test
    @DisplayName("buildDashboard_zeroGainPercent_withZeroCostBasis")
    void testBuildDashboard_ZeroGainPercent_WithZeroCostBasis() {
        PortfolioItem item = new PortfolioItem();
        item.setId(1L);
        item.setTicker("AAPL");
        item.setQuantity(0);
        item.setAssetType(AssetType.STOCK);
        item.setPurchasePrice(new BigDecimal("100.00"));
        item.setPurchaseDate(LocalDate.now());

        List<PortfolioItem> items = Arrays.asList(item);
        when(portfolioItemRepository.findAll()).thenReturn(items);
        when(priceService.getPrice("AAPL")).thenReturn(
                new LivePriceResponse("AAPL", new BigDecimal("150.00"), null, null, null)
        );

        DashboardResponse result = service.getCombinedDashboard();

        assertEquals(new BigDecimal("0.00"), result.totalCostBasis());
        assertEquals(BigDecimal.ZERO, result.unrealizedGainLossPct());
    }

    @Test
    @DisplayName("getCombinedDashboard_breakEven_calculatesZeroGain")
    void testGetCombinedDashboard_BreakEven_CalculatesZeroGain() {
        List<PortfolioItem> items = Arrays.asList(testItem);
        when(portfolioItemRepository.findAll()).thenReturn(items);
        when(priceService.getPrice("AAPL")).thenReturn(
                new LivePriceResponse("AAPL", new BigDecimal("100.00"), null, null, null)
        );

        DashboardResponse result = service.getCombinedDashboard();

        assertEquals(new BigDecimal("1000.00"), result.totalCostBasis());
        assertEquals(new BigDecimal("1000.00"), result.estimatedTotalValue());
        assertEquals(new BigDecimal("0.00"), result.unrealizedGainLoss());
        assertEquals(new BigDecimal("0.00"), result.unrealizedGainLossPct());
    }

    @Test
    @DisplayName("buildDashboard_largePortfolio_performsOptimally")
    void testBuildDashboard_LargePortfolio_PerformsOptimally() {
        List<PortfolioItem> items = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            PortfolioItem item = new PortfolioItem();
            item.setId((long) i);
            item.setTicker("TICK" + i);
            item.setQuantity(10);
            item.setAssetType(i % 2 == 0 ? AssetType.STOCK : AssetType.BOND);
            item.setPurchasePrice(new BigDecimal("100.00"));
            item.setPurchaseDate(LocalDate.now());
            items.add(item);
        }

        when(portfolioItemRepository.findAll()).thenReturn(items);
        for (int i = 0; i < 10; i++) {
            when(priceService.getPrice("TICK" + i)).thenReturn(
                    new LivePriceResponse("TICK" + i, new BigDecimal("150.00"), null, null, null)
            );
        }

        DashboardResponse result = service.getCombinedDashboard();

        assertEquals(10, result.totalPositions());
        assertEquals(100, result.totalQuantity());
    }

    @Test
    @DisplayName("buildDashboard_decimalPrices_calculatesWithPrecision")
    void testBuildDashboard_DecimalPrices_CalculatesWithPrecision() {
        testItem.setPurchasePrice(new BigDecimal("99.99"));
        List<PortfolioItem> items = Arrays.asList(testItem);
        when(portfolioItemRepository.findAll()).thenReturn(items);
        when(priceService.getPrice("AAPL")).thenReturn(
                new LivePriceResponse("AAPL", new BigDecimal("150.25"), null, null, null)
        );

        DashboardResponse result = service.getCombinedDashboard();

        assertEquals(new BigDecimal("999.90"), result.totalCostBasis());
        assertEquals(new BigDecimal("1502.50"), result.estimatedTotalValue());
    }

    @Test
    @DisplayName("getCombinedDashboard_fetchesEachTicketOnce")
    void testGetCombinedDashboard_FetchesEachTicketOnce() {
        PortfolioItem item1 = new PortfolioItem();
        item1.setId(1L);
        item1.setTicker("AAPL");
        item1.setQuantity(10);
        item1.setAssetType(AssetType.STOCK);
        item1.setPurchasePrice(new BigDecimal("100.00"));
        item1.setPurchaseDate(LocalDate.now());

        PortfolioItem item2 = new PortfolioItem();
        item2.setId(2L);
        item2.setTicker("AAPL"); // Same ticker
        item2.setQuantity(5);
        item2.setAssetType(AssetType.STOCK);
        item2.setPurchasePrice(new BigDecimal("100.00"));
        item2.setPurchaseDate(LocalDate.now());

        List<PortfolioItem> items = Arrays.asList(item1, item2);
        when(portfolioItemRepository.findAll()).thenReturn(items);
        when(priceService.getPrice("AAPL")).thenReturn(
                new LivePriceResponse("AAPL", new BigDecimal("150.00"), null, null, null)
        );

        DashboardResponse result = service.getCombinedDashboard();

        // Should only fetch AAPL price once
        verify(priceService, times(1)).getPrice("AAPL");
    }
}

