package com.example.hsbcproject.service;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.example.hsbcproject.repository.PortfolioItemRepository;
import com.example.hsbcproject.support.TestDataFactory;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
@ExtendWith(MockitoExtension.class)
class RiskServiceTest {
    @Mock private PortfolioItemRepository portfolioItemRepository;
    @InjectMocks private RiskService service;
    @Test
    void analyzeRiskReturnsLowForEmptyPortfolio() {
        when(portfolioItemRepository.findAll()).thenReturn(List.of());
        var result = service.analyzeRisk();
        assertEquals("LOW", result.overallRiskLevel());
        assertTrue(result.concentrationByAssetType().isEmpty());
    }
    @Test
    void analyzeRiskCalculatesConcentrationForHoldings() {
        when(portfolioItemRepository.findAll()).thenReturn(List.of(TestDataFactory.portfolioItem(), TestDataFactory.bondItem()));
        var result = service.analyzeRisk();
        assertFalse(result.concentrationByAssetType().isEmpty());
        assertEquals(2, result.holdingRiskDetails().size());
        assertNotNull(result.diversificationScore());
    }
}
