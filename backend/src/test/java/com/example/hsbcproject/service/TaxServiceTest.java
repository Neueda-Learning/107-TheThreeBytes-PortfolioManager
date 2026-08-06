package com.example.hsbcproject.service;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.example.hsbcproject.dto.LivePriceResponse;
import com.example.hsbcproject.repository.PortfolioItemRepository;
import com.example.hsbcproject.support.TestDataFactory;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
@ExtendWith(MockitoExtension.class)
class TaxServiceTest {
    @Mock private PortfolioItemRepository portfolioItemRepository;
    @Mock private PriceService priceService;
    @InjectMocks private TaxService service;
    @Test
    void estimateTaxAppliesShortTermRateToPositiveGain() {
        when(portfolioItemRepository.findAll()).thenReturn(List.of(TestDataFactory.portfolioItem()));
        when(priceService.getPrice("AAPL", com.example.hsbcproject.domain.AssetType.STOCK))
                .thenReturn(new LivePriceResponse("AAPL", new BigDecimal("130.00"), BigDecimal.ONE, new BigDecimal("1.0"), null));
        var result = service.estimateTax();
        assertEquals(1, result.size());
        assertEquals("SHORT_TERM", result.get(0).taxCategory());
        assertEquals(new BigDecimal("90.00"), result.get(0).estimatedTaxLiability());
    }
    @Test
    void estimateTaxReturnsZeroWhenGainIsNegative() {
        when(portfolioItemRepository.findAll()).thenReturn(List.of(TestDataFactory.portfolioItem()));
        when(priceService.getPrice("AAPL", com.example.hsbcproject.domain.AssetType.STOCK))
                .thenReturn(new LivePriceResponse("AAPL", new BigDecimal("90.00"), BigDecimal.ONE.negate(), new BigDecimal("-1.0"), null));
        var result = service.estimateTax();
        assertEquals(0, BigDecimal.ZERO.compareTo(result.get(0).estimatedTaxLiability()));
    }
}
