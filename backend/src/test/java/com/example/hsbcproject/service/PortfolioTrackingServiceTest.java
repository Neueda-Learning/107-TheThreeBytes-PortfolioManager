package com.example.hsbcproject.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.hsbcproject.domain.PortfolioSnapshot;
import com.example.hsbcproject.domain.TrackingPeriod;
import com.example.hsbcproject.dto.DashboardResponse;
import com.example.hsbcproject.dto.PortfolioSnapshotResponse;
import com.example.hsbcproject.dto.PortfolioTrackingResponse;
import com.example.hsbcproject.repository.PortfolioSnapshotRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("PortfolioTrackingService Tests")
class PortfolioTrackingServiceTest {

    @Mock
    private PortfolioSnapshotRepository snapshotRepository;

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private PortfolioTrackingService service;

    private DashboardResponse testDashboard;
    private PortfolioSnapshot testSnapshot;

    @BeforeEach
    void setUp() {
        testDashboard = new DashboardResponse(
                5,
                100,
                new BigDecimal("10000.00"),
                new BigDecimal("12000.00"),
                new BigDecimal("2000.00"),
                new BigDecimal("20.00"),
                new HashMap<>(),
                new HashMap<>(),
                new ArrayList<>()
        );

        testSnapshot = new PortfolioSnapshot();
        testSnapshot.setId(1L);
        testSnapshot.setSnapshotDate(LocalDate.now());
        testSnapshot.setTotalValue(new BigDecimal("12000.00"));
        testSnapshot.setTotalCostBasis(new BigDecimal("10000.00"));
        testSnapshot.setTotalGainLoss(new BigDecimal("2000.00"));
        testSnapshot.setTotalGainLossPct(new BigDecimal("20.00"));
        testSnapshot.setTotalPositions(5L);
        testSnapshot.setTotalQuantity(100L);
    }

    @Test
    @DisplayName("createSnapshot_noExistingSnapshot_createsNew")
    void testCreateSnapshot_NoExistingSnapshot_CreatesNew() {
        when(snapshotRepository.findBySnapshotDate(LocalDate.now())).thenReturn(Optional.empty());
        when(dashboardService.getCombinedDashboard()).thenReturn(testDashboard);
        when(snapshotRepository.save(any(PortfolioSnapshot.class))).thenReturn(testSnapshot);

        PortfolioSnapshotResponse result = service.createSnapshot();

        assertNotNull(result);
        assertEquals(LocalDate.now(), result.snapshotDate());
        assertEquals(new BigDecimal("12000.00"), result.totalValue());

        ArgumentCaptor<PortfolioSnapshot> captor = ArgumentCaptor.forClass(PortfolioSnapshot.class);
        verify(snapshotRepository, times(1)).save(captor.capture());
        PortfolioSnapshot saved = captor.getValue();
        assertEquals(LocalDate.now(), saved.getSnapshotDate());
    }

    @Test
    @DisplayName("createSnapshot_existingSnapshot_returnsExisting")
    void testCreateSnapshot_ExistingSnapshot_ReturnsExisting() {
        when(snapshotRepository.findBySnapshotDate(LocalDate.now())).thenReturn(Optional.of(testSnapshot));

        PortfolioSnapshotResponse result = service.createSnapshot();

        assertNotNull(result);
        assertEquals(LocalDate.now(), result.snapshotDate());
        
        verify(snapshotRepository, never()).save(any());
        verify(dashboardService, never()).getCombinedDashboard();
    }

    @Test
    @DisplayName("getTracking_dailyPeriod_returns30Days")
    void testGetTracking_DailyPeriod_Returns30Days() {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(30);

        when(snapshotRepository.findBySnapshotDateBetweenOrderBySnapshotDateAsc(thirtyDaysAgo, today))
                .thenReturn(Arrays.asList(testSnapshot));

        PortfolioTrackingResponse result = service.getTracking(TrackingPeriod.DAILY);

        assertNotNull(result);
        assertEquals(TrackingPeriod.DAILY, result.period());
        assertEquals(1, result.snapshots().size());
    }

    @Test
    @DisplayName("getTracking_weeklyPeriod_returns12Weeks")
    void testGetTracking_WeeklyPeriod_Returns12Weeks() {
        LocalDate today = LocalDate.now();
        LocalDate twelveWeeksAgo = today.minusWeeks(12);

        when(snapshotRepository.findBySnapshotDateBetweenOrderBySnapshotDateAsc(twelveWeeksAgo, today))
                .thenReturn(Arrays.asList(testSnapshot));

        PortfolioTrackingResponse result = service.getTracking(TrackingPeriod.WEEKLY);

        assertNotNull(result);
        assertEquals(TrackingPeriod.WEEKLY, result.period());
    }

    @Test
    @DisplayName("getTracking_monthlyPeriod_returns12Months")
    void testGetTracking_MonthlyPeriod_Returns12Months() {
        LocalDate today = LocalDate.now();
        LocalDate twelveMonthsAgo = today.minusMonths(12);

        when(snapshotRepository.findBySnapshotDateBetweenOrderBySnapshotDateAsc(twelveMonthsAgo, today))
                .thenReturn(Arrays.asList(testSnapshot));

        PortfolioTrackingResponse result = service.getTracking(TrackingPeriod.MONTHLY);

        assertNotNull(result);
        assertEquals(TrackingPeriod.MONTHLY, result.period());
    }

    @Test
    @DisplayName("getTracking_yearlyPeriod_returns5Years")
    void testGetTracking_YearlyPeriod_Returns5Years() {
        LocalDate today = LocalDate.now();
        LocalDate fiveYearsAgo = today.minusYears(5);

        when(snapshotRepository.findBySnapshotDateBetweenOrderBySnapshotDateAsc(fiveYearsAgo, today))
                .thenReturn(Arrays.asList(testSnapshot));

        PortfolioTrackingResponse result = service.getTracking(TrackingPeriod.YEARLY);

        assertNotNull(result);
        assertEquals(TrackingPeriod.YEARLY, result.period());
    }

    @Test
    @DisplayName("getTracking_noSnapshots_createsNewSnapshot")
    void testGetTracking_NoSnapshots_CreatesNewSnapshot() {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(30);

        when(snapshotRepository.findBySnapshotDateBetweenOrderBySnapshotDateAsc(thirtyDaysAgo, today))
                .thenReturn(new ArrayList<>())
                .thenReturn(Arrays.asList(testSnapshot));
        when(dashboardService.getCombinedDashboard()).thenReturn(testDashboard);
        when(snapshotRepository.findBySnapshotDate(today)).thenReturn(Optional.empty());
        when(snapshotRepository.save(any(PortfolioSnapshot.class))).thenReturn(testSnapshot);

        PortfolioTrackingResponse result = service.getTracking(TrackingPeriod.DAILY);

        assertNotNull(result);
        verify(snapshotRepository, times(1)).save(any(PortfolioSnapshot.class));
    }

    @Test
    @DisplayName("getTracking_performanceMetricsCalculation_isCorrect")
    void testGetTracking_PerformanceMetricsCalculation_IsCorrect() {
        PortfolioSnapshot snap1 = new PortfolioSnapshot();
        snap1.setId(1L);
        snap1.setSnapshotDate(LocalDate.now().minusDays(5));
        snap1.setTotalValue(new BigDecimal("10000.00"));

        PortfolioSnapshot snap2 = new PortfolioSnapshot();
        snap2.setId(2L);
        snap2.setSnapshotDate(LocalDate.now());
        snap2.setTotalValue(new BigDecimal("12000.00"));

        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(30);

        when(snapshotRepository.findBySnapshotDateBetweenOrderBySnapshotDateAsc(thirtyDaysAgo, today))
                .thenReturn(Arrays.asList(snap1, snap2));

        PortfolioTrackingResponse result = service.getTracking(TrackingPeriod.DAILY);

        PortfolioTrackingResponse.PerformanceMetrics metrics = result.metrics();
        assertNotNull(metrics);
        assertEquals(new BigDecimal("12000.00"), metrics.currentValue());
        assertEquals(new BigDecimal("10000.00"), metrics.previousValue());
        assertEquals(new BigDecimal("2000.00"), metrics.periodChange());
    }

    @Test
    @DisplayName("getAllSnapshots_returnsAllOrderedByDate")
    void testGetAllSnapshots_ReturnsAllOrderedByDate() {
        PortfolioSnapshot snap1 = new PortfolioSnapshot();
        snap1.setId(1L);
        snap1.setSnapshotDate(LocalDate.now().minusDays(2));

        PortfolioSnapshot snap2 = new PortfolioSnapshot();
        snap2.setId(2L);
        snap2.setSnapshotDate(LocalDate.now());

        when(snapshotRepository.findAllOrderByDateDesc()).thenReturn(Arrays.asList(snap2, snap1));

        List<PortfolioSnapshotResponse> result = service.getAllSnapshots();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("deleteSnapshotsBefore_deletesOldSnapshots")
    void testDeleteSnapshotsBefore_DeletesOldSnapshots() {
        LocalDate cutoffDate = LocalDate.now().minusDays(90);

        service.deleteSnapshotsBefore(cutoffDate);

        verify(snapshotRepository, times(1)).deleteBySnapshotDateBefore(cutoffDate);
    }

    @Test
    @DisplayName("getTracking_singleSnapshot_calculatesMetricsCorrectly")
    void testGetTracking_SingleSnapshot_CalculatesMetricsCorrectly() {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(30);

        when(snapshotRepository.findBySnapshotDateBetweenOrderBySnapshotDateAsc(thirtyDaysAgo, today))
                .thenReturn(Arrays.asList(testSnapshot));

        PortfolioTrackingResponse result = service.getTracking(TrackingPeriod.DAILY);

        PortfolioTrackingResponse.PerformanceMetrics metrics = result.metrics();
        // With single snapshot, previous value equals current value
        assertEquals(new BigDecimal("12000.00"), metrics.currentValue());
        assertEquals(metrics.currentValue(), metrics.previousValue());
    }

    @Test
    @DisplayName("getTracking_negativePeriodChange_calculatesCorrectly")
    void testGetTracking_NegativePeriodChange_CalculatesCorrectly() {
        PortfolioSnapshot snap1 = new PortfolioSnapshot();
        snap1.setId(1L);
        snap1.setSnapshotDate(LocalDate.now().minusDays(5));
        snap1.setTotalValue(new BigDecimal("15000.00"));

        PortfolioSnapshot snap2 = new PortfolioSnapshot();
        snap2.setId(2L);
        snap2.setSnapshotDate(LocalDate.now());
        snap2.setTotalValue(new BigDecimal("12000.00"));

        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(30);

        when(snapshotRepository.findBySnapshotDateBetweenOrderBySnapshotDateAsc(thirtyDaysAgo, today))
                .thenReturn(Arrays.asList(snap1, snap2));

        PortfolioTrackingResponse result = service.getTracking(TrackingPeriod.DAILY);

        PortfolioTrackingResponse.PerformanceMetrics metrics = result.metrics();
        assertEquals(new BigDecimal("-3000.00"), metrics.periodChange());
    }

    @Test
    @DisplayName("getTracking_highLowTracking_isCorrect")
    void testGetTracking_HighLowTracking_IsCorrect() {
        PortfolioSnapshot snap1 = new PortfolioSnapshot();
        snap1.setId(1L);
        snap1.setSnapshotDate(LocalDate.now().minusDays(5));
        snap1.setTotalValue(new BigDecimal("10000.00"));

        PortfolioSnapshot snap2 = new PortfolioSnapshot();
        snap2.setId(2L);
        snap2.setSnapshotDate(LocalDate.now().minusDays(3));
        snap2.setTotalValue(new BigDecimal("15000.00")); // High

        PortfolioSnapshot snap3 = new PortfolioSnapshot();
        snap3.setId(3L);
        snap3.setSnapshotDate(LocalDate.now());
        snap3.setTotalValue(new BigDecimal("12000.00"));

        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(30);

        when(snapshotRepository.findBySnapshotDateBetweenOrderBySnapshotDateAsc(thirtyDaysAgo, today))
                .thenReturn(Arrays.asList(snap1, snap2, snap3));

        PortfolioTrackingResponse result = service.getTracking(TrackingPeriod.DAILY);

        PortfolioTrackingResponse.PerformanceMetrics metrics = result.metrics();
        assertEquals(new BigDecimal("15000.00"), metrics.highestValue());
        assertEquals(new BigDecimal("10000.00"), metrics.lowestValue());
    }

    @Test
    @DisplayName("getTracking_periodChangePercentage_calculatesCorrectly")
    void testGetTracking_PeriodChangePercentage_CalculatesCorrectly() {
        PortfolioSnapshot snap1 = new PortfolioSnapshot();
        snap1.setId(1L);
        snap1.setSnapshotDate(LocalDate.now().minusDays(5));
        snap1.setTotalValue(new BigDecimal("10000.00"));

        PortfolioSnapshot snap2 = new PortfolioSnapshot();
        snap2.setId(2L);
        snap2.setSnapshotDate(LocalDate.now());
        snap2.setTotalValue(new BigDecimal("12000.00"));

        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(30);

        when(snapshotRepository.findBySnapshotDateBetweenOrderBySnapshotDateAsc(thirtyDaysAgo, today))
                .thenReturn(Arrays.asList(snap1, snap2));

        PortfolioTrackingResponse result = service.getTracking(TrackingPeriod.DAILY);

        PortfolioTrackingResponse.PerformanceMetrics metrics = result.metrics();
        // (2000 / 10000) * 100 = 20%
        assertEquals(new BigDecimal("20.0000"), metrics.periodChangePct());
    }

    @Test
    @DisplayName("createSnapshot_populatesAllFields")
    void testCreateSnapshot_PopulatesAllFields() {
        when(snapshotRepository.findBySnapshotDate(LocalDate.now())).thenReturn(Optional.empty());
        when(dashboardService.getCombinedDashboard()).thenReturn(testDashboard);
        when(snapshotRepository.save(any(PortfolioSnapshot.class))).thenReturn(testSnapshot);

        service.createSnapshot();

        ArgumentCaptor<PortfolioSnapshot> captor = ArgumentCaptor.forClass(PortfolioSnapshot.class);
        verify(snapshotRepository, times(1)).save(captor.capture());
        PortfolioSnapshot saved = captor.getValue();
        
        assertEquals(LocalDate.now(), saved.getSnapshotDate());
        assertEquals(new BigDecimal("12000.00"), saved.getTotalValue());
        assertEquals(new BigDecimal("10000.00"), saved.getTotalCostBasis());
        assertEquals(new BigDecimal("2000.00"), saved.getTotalGainLoss());
        assertEquals(new BigDecimal("20.00"), saved.getTotalGainLossPct());
        assertEquals(5, saved.getTotalPositions());
        assertEquals(100L, saved.getTotalQuantity());
    }

    @Test
    @DisplayName("getAllSnapshots_emptyRepository_returnsEmptyList")
    void testGetAllSnapshots_EmptyRepository_ReturnsEmptyList() {
        when(snapshotRepository.findAllOrderByDateDesc()).thenReturn(new ArrayList<>());

        List<PortfolioSnapshotResponse> result = service.getAllSnapshots();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}

