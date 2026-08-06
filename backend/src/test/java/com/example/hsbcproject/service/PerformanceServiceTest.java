package com.example.hsbcproject.service;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.example.hsbcproject.dto.LivePriceResponse;
import com.example.hsbcproject.exception.ResourceNotFoundException;
import com.example.hsbcproject.repository.PortfolioItemRepository;
import com.example.hsbcproject.support.TestDataFactory;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
@ExtendWith(MockitoExtension.class)
class PerformanceServiceTest {
    @Mock private PortfolioItemRepository portfolioItemRepository;
    @Mock private PriceService priceService;
    @InjectMocks private PerformanceService service;
    @Test
    void getAllPerformanceUsesLivePrices() {
        when(portfolioItemRepository.findAll()).thenReturn(List.of(TestDataFactory.portfolioItem()));
        when(priceService.getPrice("AAPL", com.example.hsbcproject.domain.AssetType.STOCK))
                .thenReturn(new LivePriceResponse("AAPL", new BigDecimal("120.00"), BigDecimal.TEN, new BigDecimal("10.0"), null));
        var result = service.getAllPerformance();
        assertEquals(1, result.size());
        assertEquals(new BigDecimal("1200.00"), result.get(0).currentValue());
        assertEquals(new BigDecimal("200.00"), result.get(0).unrealizedGain());
    }
    @Test
    void getPerformanceByIdMissingHoldingThrowsNotFound() {
        when(portfolioItemRepository.findById(44L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getPerformanceById(44L));
    }
}
