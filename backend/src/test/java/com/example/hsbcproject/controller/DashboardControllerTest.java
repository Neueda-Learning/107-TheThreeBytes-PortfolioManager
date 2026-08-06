package com.example.hsbcproject.controller;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.example.hsbcproject.domain.AssetType;
import com.example.hsbcproject.dto.DashboardResponse;
import com.example.hsbcproject.service.DashboardService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
class DashboardControllerTest {
    @Test
    void getCombinedDelegatesToService() {
        DashboardService service = mock(DashboardService.class);
        when(service.getCombinedDashboard()).thenReturn(new DashboardResponse(1, new BigDecimal("1.00000000"), new BigDecimal("100.00"), new BigDecimal("110.00"), new BigDecimal("10.00"), new BigDecimal("10.00"), Map.of("STOCK", new BigDecimal("1.00000000")), Map.of("STOCK", new BigDecimal("100.00")), List.of()));
        DashboardController controller = new DashboardController(service);
        assertEquals(new BigDecimal("110.00"), controller.getCombined().estimatedTotalValue());
        verify(service).getCombinedDashboard();
    }
    @Test
    void getByAssetTypeDelegatesToService() {
        DashboardService service = mock(DashboardService.class);
        when(service.getDashboardByAssetType(AssetType.STOCK)).thenReturn(new DashboardResponse(1, BigDecimal.ONE, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO, Map.of(), Map.of(), List.of()));
        DashboardController controller = new DashboardController(service);
        controller.getByAssetType(AssetType.STOCK);
        verify(service).getDashboardByAssetType(AssetType.STOCK);
    }
}
