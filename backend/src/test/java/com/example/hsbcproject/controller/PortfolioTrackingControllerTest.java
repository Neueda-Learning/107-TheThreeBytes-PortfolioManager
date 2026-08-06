package com.example.hsbcproject.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.hsbcproject.domain.TrackingPeriod;
import com.example.hsbcproject.dto.PortfolioSnapshotResponse;
import com.example.hsbcproject.dto.PortfolioTrackingResponse;
import com.example.hsbcproject.service.PortfolioTrackingService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class PortfolioTrackingControllerTest {

    @Test
    void createSnapshotReturnsOkAndBody() {
        PortfolioTrackingService service = mock(PortfolioTrackingService.class);
        PortfolioSnapshotResponse snapshot = snapshotResponse(11L);
        when(service.createSnapshot()).thenReturn(snapshot);
        PortfolioTrackingController controller = new PortfolioTrackingController(service);

        var response = controller.createSnapshot();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(11L, response.getBody().id());
    }

    @Test
    void getDailyTrackingDelegatesToService() {
        PortfolioTrackingService service = mock(PortfolioTrackingService.class);
        when(service.getTracking(TrackingPeriod.DAILY)).thenReturn(trackingResponse(TrackingPeriod.DAILY));
        PortfolioTrackingController controller = new PortfolioTrackingController(service);

        assertEquals(TrackingPeriod.DAILY, controller.getDailyTracking().period());
    }

    @Test
    void getWeeklyTrackingDelegatesToService() {
        PortfolioTrackingService service = mock(PortfolioTrackingService.class);
        when(service.getTracking(TrackingPeriod.WEEKLY)).thenReturn(trackingResponse(TrackingPeriod.WEEKLY));
        PortfolioTrackingController controller = new PortfolioTrackingController(service);

        assertEquals(TrackingPeriod.WEEKLY, controller.getWeeklyTracking().period());
    }

    @Test
    void getMonthlyTrackingDelegatesToService() {
        PortfolioTrackingService service = mock(PortfolioTrackingService.class);
        when(service.getTracking(TrackingPeriod.MONTHLY)).thenReturn(trackingResponse(TrackingPeriod.MONTHLY));
        PortfolioTrackingController controller = new PortfolioTrackingController(service);

        assertEquals(TrackingPeriod.MONTHLY, controller.getMonthlyTracking().period());
    }

    @Test
    void getYearlyTrackingDelegatesToService() {
        PortfolioTrackingService service = mock(PortfolioTrackingService.class);
        when(service.getTracking(TrackingPeriod.YEARLY)).thenReturn(trackingResponse(TrackingPeriod.YEARLY));
        PortfolioTrackingController controller = new PortfolioTrackingController(service);

        assertEquals(TrackingPeriod.YEARLY, controller.getYearlyTracking().period());
    }

    @Test
    void getTrackingByPeriodDelegatesToService() {
        PortfolioTrackingService service = mock(PortfolioTrackingService.class);
        when(service.getTracking(TrackingPeriod.MONTHLY)).thenReturn(trackingResponse(TrackingPeriod.MONTHLY));
        PortfolioTrackingController controller = new PortfolioTrackingController(service);

        assertEquals(TrackingPeriod.MONTHLY, controller.getTrackingByPeriod(TrackingPeriod.MONTHLY).period());
        verify(service).getTracking(TrackingPeriod.MONTHLY);
    }

    @Test
    void getAllSnapshotsDelegatesToService() {
        PortfolioTrackingService service = mock(PortfolioTrackingService.class);
        when(service.getAllSnapshots()).thenReturn(List.of(snapshotResponse(1L), snapshotResponse(2L)));
        PortfolioTrackingController controller = new PortfolioTrackingController(service);

        assertEquals(2, controller.getAllSnapshots().size());
    }

    private static PortfolioSnapshotResponse snapshotResponse(Long id) {
        return new PortfolioSnapshotResponse(
                id,
                LocalDate.now(),
                new BigDecimal("1000.00"),
                new BigDecimal("900.00"),
                new BigDecimal("100.00"),
                new BigDecimal("11.11"),
                2L,
                new BigDecimal("3.00000000"));
    }

    private static PortfolioTrackingResponse trackingResponse(TrackingPeriod period) {
        return new PortfolioTrackingResponse(
                period,
                List.of(),
                new PortfolioTrackingResponse.PerformanceMetrics(
                        "label",
                        new BigDecimal("1000.00"),
                        new BigDecimal("1100.00"),
                        new BigDecimal("100.00"),
                        new BigDecimal("10.00"),
                        new BigDecimal("900.00"),
                        new BigDecimal("1200.00"),
                        LocalDate.now().minusDays(10),
                        LocalDate.now()));
    }
}
