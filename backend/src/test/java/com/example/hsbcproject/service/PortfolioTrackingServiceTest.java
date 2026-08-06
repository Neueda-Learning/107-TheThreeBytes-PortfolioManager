package com.example.hsbcproject.service;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.example.hsbcproject.domain.TrackingPeriod;
import com.example.hsbcproject.dto.DashboardResponse;
import com.example.hsbcproject.repository.PortfolioSnapshotRepository;
import com.example.hsbcproject.support.TestDataFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
@ExtendWith(MockitoExtension.class)
class PortfolioTrackingServiceTest {
    @Mock private PortfolioSnapshotRepository snapshotRepository;
    @Mock private DashboardService dashboardService;
    @InjectMocks private PortfolioTrackingService service;
    @Test
    void createSnapshotReturnsExistingSnapshotForToday() {
        var existing = TestDataFactory.snapshot(LocalDate.now(), new BigDecimal("1200.00"));
        when(snapshotRepository.findBySnapshotDate(LocalDate.now())).thenReturn(Optional.of(existing));
        var response = service.createSnapshot();
        assertEquals(existing.getSnapshotDate(), response.snapshotDate());
        verify(snapshotRepository, never()).save(any());
    }
    @Test
    void getTrackingReturnsMetricsFromSnapshots() {
        when(snapshotRepository.findBySnapshotDateBetweenOrderBySnapshotDateAsc(any(), any()))
                .thenReturn(TestDataFactory.snapshots());
        var response = service.getTracking(TrackingPeriod.DAILY);
        assertEquals(TrackingPeriod.DAILY, response.period());
        assertEquals(3, response.snapshots().size());
        assertEquals(new BigDecimal("150.00"), response.metrics().periodChange());
    }
    @Test
    void createSnapshotPersistsDashboardTotalsWhenMissing() {
        DashboardResponse dashboard = new DashboardResponse(
                2,
                new BigDecimal("15.00000000"),
                new BigDecimal("2000.00"),
                new BigDecimal("2300.00"),
                new BigDecimal("300.00"),
                new BigDecimal("15.00"),
                java.util.Map.of("STOCK", new BigDecimal("15.00000000")),
                java.util.Map.of("STOCK", new BigDecimal("2000.00")),
                List.of());
        when(snapshotRepository.findBySnapshotDate(LocalDate.now())).thenReturn(Optional.empty());
        when(dashboardService.getCombinedDashboard()).thenReturn(dashboard);
        when(snapshotRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var response = service.createSnapshot();
        assertEquals(new BigDecimal("2300.00"), response.totalValue());
    }
}
