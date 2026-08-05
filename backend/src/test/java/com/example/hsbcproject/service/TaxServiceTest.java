package com.example.hsbcproject.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.hsbcproject.domain.AssetType;
import com.example.hsbcproject.domain.PortfolioItem;
import com.example.hsbcproject.dto.LivePriceResponse;
import com.example.hsbcproject.dto.TaxItemResponse;
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
@DisplayName("TaxService Tests")
class TaxServiceTest {

    @Mock
    private PortfolioItemRepository portfolioItemRepository;

    @Mock
    private PriceService priceService;

    @InjectMocks
    private TaxService service;

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
    @DisplayName("estimateTax_emptyPortfolio_returnsEmptyList")
    void testEstimateTax_EmptyPortfolio_ReturnsEmptyList() {
        when(portfolioItemRepository.findAll()).thenReturn(new ArrayList<>());

        List<TaxItemResponse> result = service.estimateTax();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("estimateTax_shortTermHolding_applies30PercentRate")
    void testEstimateTax_ShortTermHolding_Applies30PercentRate() {
        testItem.setPurchaseDate(LocalDate.now().minusDays(100)); // Less than 365 days
        List<PortfolioItem> items = Arrays.asList(testItem);
        when(portfolioItemRepository.findAll()).thenReturn(items);
        when(priceService.getPrice("AAPL")).thenReturn(
                new LivePriceResponse("AAPL", new BigDecimal("120.00"), null, null, null)
        );

        List<TaxItemResponse> result = service.estimateTax();

        assertNotNull(result);
        assertEquals(1, result.size());
        TaxItemResponse taxItem = result.get(0);
        assertEquals("SHORT_TERM", taxItem.taxCategory());
        assertEquals(new BigDecimal("0.30"), taxItem.taxRate());
    }

    @Test
    @DisplayName("estimateTax_longTermHolding_applies15PercentRate")
    void testEstimateTax_LongTermHolding_Applies15PercentRate() {
        testItem.setPurchaseDate(LocalDate.now().minusDays(500)); // More than 365 days
        List<PortfolioItem> items = Arrays.asList(testItem);
        when(portfolioItemRepository.findAll()).thenReturn(items);
        when(priceService.getPrice("AAPL")).thenReturn(
                new LivePriceResponse("AAPL", new BigDecimal("120.00"), null, null, null)
        );

        List<TaxItemResponse> result = service.estimateTax();

        assertNotNull(result);
        assertEquals(1, result.size());
        TaxItemResponse taxItem = result.get(0);
        assertEquals("LONG_TERM", taxItem.taxCategory());
        assertEquals(new BigDecimal("0.15"), taxItem.taxRate());
    }

    @Test
    @DisplayName("estimateTax_exactlyOneYear_isLongTerm")
    void testEstimateTax_ExactlyOneYear_IsLongTerm() {
        testItem.setPurchaseDate(LocalDate.now().minusDays(365));
        List<PortfolioItem> items = Arrays.asList(testItem);
        when(portfolioItemRepository.findAll()).thenReturn(items);
        when(priceService.getPrice("AAPL")).thenReturn(
                new LivePriceResponse("AAPL", new BigDecimal("120.00"), null, null, null)
        );

        List<TaxItemResponse> result = service.estimateTax();

        assertEquals("LONG_TERM", result.get(0).taxCategory());
    }

    @Test
    @DisplayName("estimateTax_profitablePosition_calculatesTax")
    void testEstimateTax_ProfitablePosition_CalculatesTax() {
        List<PortfolioItem> items = Arrays.asList(testItem);
        when(portfolioItemRepository.findAll()).thenReturn(items);
        when(priceService.getPrice("AAPL")).thenReturn(
                new LivePriceResponse("AAPL", new BigDecimal("150.00"), null, null, null)
        );

        List<TaxItemResponse> result = service.estimateTax();

        TaxItemResponse taxItem = result.get(0);
        // Cost basis: 100 * 10 = 1000
        assertEquals(new BigDecimal("1000.00"), taxItem.costBasis());
        // Current value: 150 * 10 = 1500
        assertEquals(new BigDecimal("1500.00"), taxItem.estimatedCurrentValue());
        // Estimated gain: 1500 - 1000 = 500
        assertEquals(new BigDecimal("500.00"), taxItem.estimatedGain());
        // Tax liability: 500 * 0.30 = 150 (short-term)
        assertTrue(taxItem.estimatedTaxLiability().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("estimateTax_lossPosition_noTaxLiability")
    void testEstimateTax_LossPosition_NoTaxLiability() {
        List<PortfolioItem> items = Arrays.asList(testItem);
        when(portfolioItemRepository.findAll()).thenReturn(items);
        when(priceService.getPrice("AAPL")).thenReturn(
                new LivePriceResponse("AAPL", new BigDecimal("80.00"), null, null, null)
        );

        List<TaxItemResponse> result = service.estimateTax();

        TaxItemResponse taxItem = result.get(0);
        // Current value: 80 * 10 = 800
        assertEquals(new BigDecimal("800.00"), taxItem.estimatedCurrentValue());
        // Estimated loss: 800 - 1000 = -200
        assertEquals(new BigDecimal("-200.00"), taxItem.estimatedGain());
        // No tax liability on losses
        assertEquals(BigDecimal.ZERO, taxItem.estimatedTaxLiability());
    }

    @Test
    @DisplayName("estimateTax_breakEven_noTaxLiability")
    void testEstimateTax_BreakEven_NoTaxLiability() {
        List<PortfolioItem> items = Arrays.asList(testItem);
        when(portfolioItemRepository.findAll()).thenReturn(items);
        when(priceService.getPrice("AAPL")).thenReturn(
                new LivePriceResponse("AAPL", new BigDecimal("100.00"), null, null, null)
        );

        List<TaxItemResponse> result = service.estimateTax();

        TaxItemResponse taxItem = result.get(0);
        assertEquals(new BigDecimal("0.00"), taxItem.estimatedGain());
        assertEquals(BigDecimal.ZERO, taxItem.estimatedTaxLiability());
    }

    @Test
    @DisplayName("estimateTax_shortTermTaxCalculation_isCorrect")
    void testEstimateTax_ShortTermTaxCalculation_IsCorrect() {
        testItem.setPurchaseDate(LocalDate.now().minusDays(100));
        List<PortfolioItem> items = Arrays.asList(testItem);
        when(portfolioItemRepository.findAll()).thenReturn(items);
        when(priceService.getPrice("AAPL")).thenReturn(
                new LivePriceResponse("AAPL", new BigDecimal("150.00"), null, null, null)
        );

        List<TaxItemResponse> result = service.estimateTax();

        TaxItemResponse taxItem = result.get(0);
        // Gain: 500, Tax: 500 * 0.30 = 150
        assertEquals(new BigDecimal("150.00"), taxItem.estimatedTaxLiability());
    }

    @Test
    @DisplayName("estimateTax_longTermTaxCalculation_isCorrect")
    void testEstimateTax_LongTermTaxCalculation_IsCorrect() {
        testItem.setPurchaseDate(LocalDate.now().minusDays(500));
        List<PortfolioItem> items = Arrays.asList(testItem);
        when(portfolioItemRepository.findAll()).thenReturn(items);
        when(priceService.getPrice("AAPL")).thenReturn(
                new LivePriceResponse("AAPL", new BigDecimal("150.00"), null, null, null)
        );

        List<TaxItemResponse> result = service.estimateTax();

        TaxItemResponse taxItem = result.get(0);
        // Gain: 500, Tax: 500 * 0.15 = 75
        assertEquals(new BigDecimal("75.00"), taxItem.estimatedTaxLiability());
    }

    @Test
    @DisplayName("estimateTax_priceNotAvailable_usesPurchasePrice")
    void testEstimateTax_PriceNotAvailable_UsesPurchasePrice() {
        List<PortfolioItem> items = Arrays.asList(testItem);
        when(portfolioItemRepository.findAll()).thenReturn(items);
        when(priceService.getPrice("AAPL")).thenReturn(
                new LivePriceResponse("AAPL", null, null, null, "Not found")
        );

        List<TaxItemResponse> result = service.estimateTax();

        TaxItemResponse taxItem = result.get(0);
        // Should use purchase price, resulting in zero gain
        assertEquals(new BigDecimal("1000.00"), taxItem.estimatedCurrentValue());
        assertEquals(new BigDecimal("0.00"), taxItem.estimatedGain());
        assertEquals(BigDecimal.ZERO, taxItem.estimatedTaxLiability());
    }

    @Test
    @DisplayName("estimateTax_holdingDaysCalculation_isCorrect")
    void testEstimateTax_HoldingDaysCalculation_IsCorrect() {
        testItem.setPurchaseDate(LocalDate.now().minusDays(30));
        List<PortfolioItem> items = Arrays.asList(testItem);
        when(portfolioItemRepository.findAll()).thenReturn(items);
        when(priceService.getPrice("AAPL")).thenReturn(
                new LivePriceResponse("AAPL", new BigDecimal("120.00"), null, null, null)
        );

        List<TaxItemResponse> result = service.estimateTax();

        assertEquals(30, result.get(0).holdingDays());
    }

    @Test
    @DisplayName("estimateTax_multipleItems_estimatesAll")
    void testEstimateTax_MultipleItems_EstimatesAll() {
        PortfolioItem item1 = new PortfolioItem();
        item1.setId(1L);
        item1.setTicker("AAPL");
        item1.setQuantity(10);
        item1.setAssetType(AssetType.STOCK);
        item1.setPurchasePrice(new BigDecimal("100.00"));
        item1.setPurchaseDate(LocalDate.now().minusDays(100)); // SHORT_TERM

        PortfolioItem item2 = new PortfolioItem();
        item2.setId(2L);
        item2.setTicker("MSFT");
        item2.setQuantity(5);
        item2.setAssetType(AssetType.STOCK);
        item2.setPurchasePrice(new BigDecimal("200.00"));
        item2.setPurchaseDate(LocalDate.now().minusDays(500)); // LONG_TERM

        when(portfolioItemRepository.findAll()).thenReturn(Arrays.asList(item1, item2));
        when(priceService.getPrice("AAPL")).thenReturn(
                new LivePriceResponse("AAPL", new BigDecimal("120.00"), null, null, null)
        );
        when(priceService.getPrice("MSFT")).thenReturn(
                new LivePriceResponse("MSFT", new BigDecimal("250.00"), null, null, null)
        );

        List<TaxItemResponse> result = service.estimateTax();

        assertEquals(2, result.size());
        assertEquals("SHORT_TERM", result.get(0).taxCategory());
        assertEquals("LONG_TERM", result.get(1).taxCategory());
    }

    @Test
    @DisplayName("estimateTax_largeGain_calculatesTaxCorrectly")
    void testEstimateTax_LargeGain_CalculatesTaxCorrectly() {
        testItem.setQuantity(1000);
        List<PortfolioItem> items = Arrays.asList(testItem);
        when(portfolioItemRepository.findAll()).thenReturn(items);
        when(priceService.getPrice("AAPL")).thenReturn(
                new LivePriceResponse("AAPL", new BigDecimal("200.00"), null, null, null)
        );

        List<TaxItemResponse> result = service.estimateTax();

        TaxItemResponse taxItem = result.get(0);
        // Cost basis: 100 * 1000 = 100,000
        assertEquals(new BigDecimal("100000.00"), taxItem.costBasis());
        // Current value: 200 * 1000 = 200,000
        assertEquals(new BigDecimal("200000.00"), taxItem.estimatedCurrentValue());
        // Gain: 100,000, Tax: 100,000 * 0.30 = 30,000
        assertEquals(new BigDecimal("30000.00"), taxItem.estimatedTaxLiability());
    }

    @Test
    @DisplayName("estimateTax_decimalPrice_calculatesPrecisely")
    void testEstimateTax_DecimalPrice_CalculatesPrecisely() {
        testItem.setPurchasePrice(new BigDecimal("99.99"));
        List<PortfolioItem> items = Arrays.asList(testItem);
        when(portfolioItemRepository.findAll()).thenReturn(items);
        when(priceService.getPrice("AAPL")).thenReturn(
                new LivePriceResponse("AAPL", new BigDecimal("150.25"), null, null, null)
        );

        List<TaxItemResponse> result = service.estimateTax();

        TaxItemResponse taxItem = result.get(0);
        assertEquals(new BigDecimal("999.90"), taxItem.costBasis());
        assertEquals(new BigDecimal("1502.50"), taxItem.estimatedCurrentValue());
    }

    @Test
    @DisplayName("estimateTax_zeroQuantity_handlesGracefully")
    void testEstimateTax_ZeroQuantity_HandlesGracefully() {
        testItem.setQuantity(0);
        List<PortfolioItem> items = Arrays.asList(testItem);
        when(portfolioItemRepository.findAll()).thenReturn(items);
        when(priceService.getPrice("AAPL")).thenReturn(
                new LivePriceResponse("AAPL", new BigDecimal("150.00"), null, null, null)
        );

        List<TaxItemResponse> result = service.estimateTax();

        TaxItemResponse taxItem = result.get(0);
        assertEquals(new BigDecimal("0.00"), taxItem.costBasis());
        assertEquals(new BigDecimal("0.00"), taxItem.estimatedCurrentValue());
        assertEquals(new BigDecimal("0.00"), taxItem.estimatedGain());
        assertEquals(BigDecimal.ZERO, taxItem.estimatedTaxLiability());
    }
}

