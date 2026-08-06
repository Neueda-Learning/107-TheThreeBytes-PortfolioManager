package com.example.hsbcproject.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.hsbcproject.domain.AssetType;
import com.example.hsbcproject.dto.PerformanceItemResponse;
import com.example.hsbcproject.service.PerformanceService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class PerformanceControllerTest {

    @Test
    void getAllDelegatesToService() {
        PerformanceService service = mock(PerformanceService.class);
        when(service.getAllPerformance()).thenReturn(List.of(samplePerformance(1L)));
        PerformanceController controller = new PerformanceController(service);

        var result = controller.getAll();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).id());
    }

    @Test
    void getByIdDelegatesToService() {
        PerformanceService service = mock(PerformanceService.class);
        when(service.getPerformanceById(2L)).thenReturn(samplePerformance(2L));
        PerformanceController controller = new PerformanceController(service);

        assertEquals(2L, controller.getById(2L).id());
    }

    private static PerformanceItemResponse samplePerformance(Long id) {
        return new PerformanceItemResponse(
                id,
                "AAPL",
                AssetType.STOCK,
                new BigDecimal("2.00000000"),
                new BigDecimal("100.00"),
                new BigDecimal("120.00"),
                new BigDecimal("200.00"),
                new BigDecimal("240.00"),
                new BigDecimal("40.00"),
                new BigDecimal("20.00"),
                LocalDate.now().minusDays(10),
                10L);
    }
}

