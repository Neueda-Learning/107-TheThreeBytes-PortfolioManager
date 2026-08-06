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
class DashboardServiceTest {
    @Mock private PortfolioItemRepository portfolioItemRepository;
    @Mock private PriceService priceService;
    @InjectMocks private DashboardService service;
    @Test
    void getCombinedDashboardCalculatesTotalsFromPrices() {
        when(portfolioItemRepository.findAll()).thenReturn(List.of(TestDataFactory.portfolioItem(), TestDataFactory.secondPortfolioItem()));
        when(priceService.getPrice("AAPL")).thenReturn(new LivePriceResponse("AAPL", new BigDecimal("110.00"), BigDecimal.ONE, new BigDecimal("1.0"), null));
        when(priceService.getPrice("MSFT")).thenReturn(new LivePriceResponse("MSFT", new BigDecimal("220.00"), BigDecimal.ONE, new BigDecimal("1.0"), null));
        var response = service.getCombinedDashboard();
        assertEquals(2, response.totalPositions());
        assertEquals(new BigDecimal("15.00000000"), response.totalQuantity());
        assertEquals(new BigDecimal("2000.00"), response.totalCostBasis());
        assertEquals(new BigDecimal("2200.00"), response.estimatedTotalValue());
        assertEquals(new BigDecimal("200.00"), response.unrealizedGainLoss());
    }
    @Test
    void getDashboardByAssetTypeFiltersHoldings() {
        when(portfolioItemRepository.findAll()).thenReturn(List.of(TestDataFactory.portfolioItem(), TestDataFactory.bondItem()));
        when(priceService.getPrice("AAPL")).thenReturn(new LivePriceResponse("AAPL", new BigDecimal("110.00"), BigDecimal.ONE, new BigDecimal("1.0"), null));
        var response = service.getDashboardByAssetType(com.example.hsbcproject.domain.AssetType.STOCK);
        assertEquals(1, response.totalPositions());
        assertEquals("AAPL", response.holdings().get(0).ticker());
    }
}
