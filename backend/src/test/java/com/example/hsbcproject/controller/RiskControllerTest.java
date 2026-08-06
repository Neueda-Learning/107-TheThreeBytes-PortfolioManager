package com.example.hsbcproject.controller;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.example.hsbcproject.service.RiskService;
import org.junit.jupiter.api.Test;
class RiskControllerTest {
    @Test
    void analyzeRiskDelegatesToService() {
        RiskService service = mock(RiskService.class);
        when(service.analyzeRisk()).thenReturn(new com.example.hsbcproject.dto.RiskAnalysisResponse(java.util.Map.of(), java.util.List.of(), java.math.BigDecimal.ZERO, "LOW"));
        RiskController controller = new RiskController(service);
        assertEquals("LOW", controller.analyzeRisk().overallRiskLevel());
    }
}
