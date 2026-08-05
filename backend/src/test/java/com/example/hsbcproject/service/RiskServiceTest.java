package com.example.hsbcproject.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.hsbcproject.domain.AssetType;
import com.example.hsbcproject.domain.PortfolioItem;
import com.example.hsbcproject.dto.HoldingRiskDetail;
import com.example.hsbcproject.dto.RiskAnalysisResponse;
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
@DisplayName("RiskService Tests")
class RiskServiceTest {

    @Mock
    private PortfolioItemRepository portfolioItemRepository;

    @InjectMocks
    private RiskService service;

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
    }

    @Test
    @DisplayName("analyzeRisk_emptyPortfolio_returnsLowRisk")
    void testAnalyzeRisk_EmptyPortfolio_ReturnsLowRisk() {
        when(portfolioItemRepository.findAll()).thenReturn(new ArrayList<>());

        RiskAnalysisResponse result = service.analyzeRisk();

        assertNotNull(result);
        assertTrue(result.concentrationByAssetType().isEmpty());
        assertTrue(result.holdingRiskDetails().isEmpty());
        assertEquals(BigDecimal.ZERO, result.diversificationScore());
        assertEquals("LOW", result.overallRiskLevel());
    }

    @Test
    @DisplayName("analyzeRisk_singleItem_calculatesConcentration")
    void testAnalyzeRisk_SingleItem_CalculatesConcentration() {
        List<PortfolioItem> items = Arrays.asList(testItem);
        when(portfolioItemRepository.findAll()).thenReturn(items);

        RiskAnalysisResponse result = service.analyzeRisk();

        assertNotNull(result);
        assertEquals(1, result.concentrationByAssetType().size());
        assertTrue(result.concentrationByAssetType().containsKey("STOCK"));
        // Single stock should be 100% concentration
        assertEquals(new BigDecimal("100.00"), result.concentrationByAssetType().get("STOCK"));
        assertEquals("HIGH", result.overallRiskLevel());
    }

    @Test
    @DisplayName("analyzeRisk_diversifiedPortfolio_returnsLowRisk")
    void testAnalyzeRisk_DiversifiedPortfolio_ReturnsLowRisk() {
        PortfolioItem stock = new PortfolioItem();
        stock.setId(1L);
        stock.setTicker("AAPL");
        stock.setQuantity(10);
        stock.setAssetType(AssetType.STOCK);
        stock.setPurchasePrice(new BigDecimal("100.00"));
        stock.setPurchaseDate(LocalDate.now().minusMonths(6));

        PortfolioItem bond = new PortfolioItem();
        bond.setId(2L);
        bond.setTicker("BOND1");
        bond.setQuantity(10);
        bond.setAssetType(AssetType.BOND);
        bond.setPurchasePrice(new BigDecimal("100.00"));
        bond.setPurchaseDate(LocalDate.now().minusMonths(6));

        PortfolioItem crypto = new PortfolioItem();
        crypto.setId(3L);
        crypto.setTicker("BTC");
        crypto.setQuantity(10);
        crypto.setAssetType(AssetType.CRYPTO);
        crypto.setPurchasePrice(new BigDecimal("100.00"));
        crypto.setPurchaseDate(LocalDate.now().minusMonths(6));

        when(portfolioItemRepository.findAll()).thenReturn(Arrays.asList(stock, bond, crypto));

        RiskAnalysisResponse result = service.analyzeRisk();

        assertNotNull(result);
        assertEquals(3, result.concentrationByAssetType().size());
        // Each should be 33.33% concentration
        assertTrue(result.concentrationByAssetType().values().stream()
                .allMatch(v -> v.compareTo(new BigDecimal("33.00")) > 0 && 
                         v.compareTo(new BigDecimal("34.00")) < 0));
        assertEquals("LOW", result.overallRiskLevel());
    }

    @Test
    @DisplayName("analyzeRisk_mediumConcentration_returnsMediumRisk")
    void testAnalyzeRisk_MediumConcentration_ReturnsMediumRisk() {
        PortfolioItem item1 = new PortfolioItem();
        item1.setId(1L);
        item1.setTicker("AAPL");
        item1.setQuantity(50);
        item1.setAssetType(AssetType.STOCK);
        item1.setPurchasePrice(new BigDecimal("100.00"));
        item1.setPurchaseDate(LocalDate.now().minusMonths(6));

        PortfolioItem item2 = new PortfolioItem();
        item2.setId(2L);
        item2.setTicker("BOND1");
        item2.setQuantity(50);
        item2.setAssetType(AssetType.BOND);
        item2.setPurchasePrice(new BigDecimal("100.00"));
        item2.setPurchaseDate(LocalDate.now().minusMonths(6));

        when(portfolioItemRepository.findAll()).thenReturn(Arrays.asList(item1, item2));

        RiskAnalysisResponse result = service.analyzeRisk();

        assertNotNull(result);
        // Each asset type is 50% of total
        assertEquals(new BigDecimal("50.00"), result.concentrationByAssetType().get("STOCK"));
        assertEquals(new BigDecimal("50.00"), result.concentrationByAssetType().get("BOND"));
        assertEquals("MEDIUM", result.overallRiskLevel());
    }

    @Test
    @DisplayName("analyzeRisk_holdingDaysClassification_isCorrect")
    void testAnalyzeRisk_HoldingDaysClassification_IsCorrect() {
        PortfolioItem shortTerm = new PortfolioItem();
        shortTerm.setId(1L);
        shortTerm.setTicker("AAPL");
        shortTerm.setQuantity(10);
        shortTerm.setAssetType(AssetType.STOCK);
        shortTerm.setPurchasePrice(new BigDecimal("100.00"));
        shortTerm.setPurchaseDate(LocalDate.now().minusDays(15)); // 15 days - SHORT_TERM

        PortfolioItem mediumTerm = new PortfolioItem();
        mediumTerm.setId(2L);
        mediumTerm.setTicker("MSFT");
        mediumTerm.setQuantity(10);
        mediumTerm.setAssetType(AssetType.STOCK);
        mediumTerm.setPurchasePrice(new BigDecimal("100.00"));
        mediumTerm.setPurchaseDate(LocalDate.now().minusDays(100)); // 100 days - MEDIUM_TERM

        PortfolioItem longTerm = new PortfolioItem();
        longTerm.setId(3L);
        longTerm.setTicker("GOOGL");
        longTerm.setQuantity(10);
        longTerm.setAssetType(AssetType.STOCK);
        longTerm.setPurchasePrice(new BigDecimal("100.00"));
        longTerm.setPurchaseDate(LocalDate.now().minusDays(500)); // 500 days - LONG_TERM

        when(portfolioItemRepository.findAll()).thenReturn(Arrays.asList(shortTerm, mediumTerm, longTerm));

        RiskAnalysisResponse result = service.analyzeRisk();

        assertEquals(3, result.holdingRiskDetails().size());
        assertEquals("SHORT_TERM", result.holdingRiskDetails().get(0).holdingCategory());
        assertEquals("MEDIUM_TERM", result.holdingRiskDetails().get(1).holdingCategory());
        assertEquals("LONG_TERM", result.holdingRiskDetails().get(2).holdingCategory());
    }

    @Test
    @DisplayName("analyzeRisk_riskDetailIncludesAllFields")
    void testAnalyzeRisk_RiskDetailIncludesAllFields() {
        List<PortfolioItem> items = Arrays.asList(testItem);
        when(portfolioItemRepository.findAll()).thenReturn(items);

        RiskAnalysisResponse result = service.analyzeRisk();

        HoldingRiskDetail detail = result.holdingRiskDetails().get(0);
        assertNotNull(detail);
        assertEquals("AAPL", detail.ticker());
        assertEquals(AssetType.STOCK, detail.assetType());
        assertNotNull(detail.purchaseDate());
        assertTrue(detail.holdingDays() > 0);
        assertNotNull(detail.holdingCategory());
        assertNotNull(detail.portfolioConcentrationPct());
    }

    @Test
    @DisplayName("analyzeRisk_diversificationScore_calculation")
    void testAnalyzeRisk_DiversificationScore_Calculation() {
        List<PortfolioItem> items = Arrays.asList(testItem);
        when(portfolioItemRepository.findAll()).thenReturn(items);

        RiskAnalysisResponse result = service.analyzeRisk();

        // Single asset type (1 * 20) + single ticker (min(1, 10) * 8) = 20 + 8 = 28
        assertEquals(new BigDecimal("28"), result.diversificationScore());
    }

    @Test
    @DisplayName("analyzeRisk_multipleTickersInSameType_affectsDiversification")
    void testAnalyzeRisk_MultipleTickersInSameType_AffectsDiversification() {
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
        item2.setQuantity(10);
        item2.setAssetType(AssetType.STOCK);
        item2.setPurchasePrice(new BigDecimal("100.00"));
        item2.setPurchaseDate(LocalDate.now().minusMonths(6));

        when(portfolioItemRepository.findAll()).thenReturn(Arrays.asList(item1, item2));

        RiskAnalysisResponse result = service.analyzeRisk();

        // 1 asset type (1 * 20) + 2 tickers (2 * 8) = 20 + 16 = 36
        assertEquals(new BigDecimal("36"), result.diversificationScore());
    }

    @Test
    @DisplayName("analyzeRisk_manyTickers_diversificationScoreCapped")
    void testAnalyzeRisk_ManyTickers_DiversificationScoreCapped() {
        List<PortfolioItem> items = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            PortfolioItem item = new PortfolioItem();
            item.setId((long) i);
            item.setTicker("TICK" + i);
            item.setQuantity(10);
            item.setAssetType(AssetType.STOCK);
            item.setPurchasePrice(new BigDecimal("100.00"));
            item.setPurchaseDate(LocalDate.now().minusMonths(6));
            items.add(item);
        }

        when(portfolioItemRepository.findAll()).thenReturn(items);

        RiskAnalysisResponse result = service.analyzeRisk();

        // 1 asset type (1 * 20) + min(15, 10) tickers (10 * 8) = 20 + 80 = 100
        assertEquals(new BigDecimal("100"), result.diversificationScore());
    }

    @Test
    @DisplayName("analyzeRisk_highConcentration_returnsHighRisk")
    void testAnalyzeRisk_HighConcentration_ReturnsHighRisk() {
        PortfolioItem largePosition = new PortfolioItem();
        largePosition.setId(1L);
        largePosition.setTicker("AAPL");
        largePosition.setQuantity(1000);
        largePosition.setAssetType(AssetType.STOCK);
        largePosition.setPurchasePrice(new BigDecimal("100.00"));
        largePosition.setPurchaseDate(LocalDate.now().minusMonths(6));

        PortfolioItem smallPosition = new PortfolioItem();
        smallPosition.setId(2L);
        smallPosition.setTicker("MSFT");
        smallPosition.setQuantity(1);
        smallPosition.setAssetType(AssetType.STOCK);
        smallPosition.setPurchasePrice(new BigDecimal("100.00"));
        smallPosition.setPurchaseDate(LocalDate.now().minusMonths(6));

        when(portfolioItemRepository.findAll()).thenReturn(Arrays.asList(largePosition, smallPosition));

        RiskAnalysisResponse result = service.analyzeRisk();

        // AAPL is ~99.9% of portfolio
        assertTrue(result.concentrationByAssetType().get("STOCK").compareTo(new BigDecimal("70")) > 0);
        assertEquals("HIGH", result.overallRiskLevel());
    }

    @Test
    @DisplayName("analyzeRisk_multipleAssetTypes_calculatesSeparateConcentrations")
    void testAnalyzeRisk_MultipleAssetTypes_CalculatesSeparateConcentrations() {
        PortfolioItem stock = new PortfolioItem();
        stock.setId(1L);
        stock.setTicker("AAPL");
        stock.setQuantity(10);
        stock.setAssetType(AssetType.STOCK);
        stock.setPurchasePrice(new BigDecimal("100.00"));
        stock.setPurchaseDate(LocalDate.now().minusMonths(6));

        PortfolioItem bond = new PortfolioItem();
        bond.setId(2L);
        bond.setTicker("BOND1");
        bond.setQuantity(20);
        bond.setAssetType(AssetType.BOND);
        bond.setPurchasePrice(new BigDecimal("100.00"));
        bond.setPurchaseDate(LocalDate.now().minusMonths(6));

        when(portfolioItemRepository.findAll()).thenReturn(Arrays.asList(stock, bond));

        RiskAnalysisResponse result = service.analyzeRisk();

        assertEquals(2, result.concentrationByAssetType().size());
        assertTrue(result.concentrationByAssetType().get("STOCK").compareTo(new BigDecimal("33")) > 0);
        assertTrue(result.concentrationByAssetType().get("BOND").compareTo(new BigDecimal("66")) > 0);
    }
}

