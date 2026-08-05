package com.example.hsbcproject.service;

import com.example.hsbcproject.domain.PortfolioSnapshot;
import com.example.hsbcproject.domain.TrackingPeriod;
import com.example.hsbcproject.dto.DashboardResponse;
import com.example.hsbcproject.dto.PortfolioSnapshotResponse;
import com.example.hsbcproject.dto.PortfolioTrackingResponse;
import com.example.hsbcproject.repository.PortfolioSnapshotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PortfolioTrackingService {

    private final PortfolioSnapshotRepository snapshotRepository;
    private final DashboardService dashboardService;

    public PortfolioTrackingService(PortfolioSnapshotRepository snapshotRepository,
                                    DashboardService dashboardService) {
        this.snapshotRepository = snapshotRepository;
        this.dashboardService = dashboardService;
    }

    /**
     * Create a snapshot of the current portfolio state
     */
    public PortfolioSnapshotResponse createSnapshot() {
        LocalDate today = LocalDate.now();
        
        // Check if snapshot already exists for today
        var existing = snapshotRepository.findBySnapshotDate(today);
        if (existing.isPresent()) {
            return toResponse(existing.get());
        }

        // Get current dashboard data
        DashboardResponse dashboard = dashboardService.getCombinedDashboard();

        // Create new snapshot
        PortfolioSnapshot snapshot = new PortfolioSnapshot();
        snapshot.setSnapshotDate(today);
        snapshot.setTotalValue(dashboard.estimatedTotalValue());
        snapshot.setTotalCostBasis(dashboard.totalCostBasis());
        snapshot.setTotalGainLoss(dashboard.unrealizedGainLoss());
        snapshot.setTotalGainLossPct(dashboard.unrealizedGainLossPct());
        snapshot.setTotalPositions(dashboard.totalPositions());
        snapshot.setTotalQuantity(dashboard.totalQuantity());

        PortfolioSnapshot saved = snapshotRepository.save(snapshot);
        return toResponse(saved);
    }

    /**
     * Get portfolio tracking data for a specific period
     */
    @Transactional(readOnly = true)
    public PortfolioTrackingResponse getTracking(TrackingPeriod period) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = calculateStartDate(endDate, period);

        List<PortfolioSnapshot> snapshots = snapshotRepository
                .findBySnapshotDateBetweenOrderBySnapshotDateAsc(startDate, endDate);

        // If no snapshots exist, create one for today
        if (snapshots.isEmpty()) {
            createSnapshot();
            snapshots = snapshotRepository
                    .findBySnapshotDateBetweenOrderBySnapshotDateAsc(startDate, endDate);
        }

        List<PortfolioSnapshotResponse> responses = snapshots.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        PortfolioTrackingResponse.PerformanceMetrics metrics = calculateMetrics(snapshots, period);

        return new PortfolioTrackingResponse(period, responses, metrics);
    }

    /**
     * Get all historical snapshots
     */
    @Transactional(readOnly = true)
    public List<PortfolioSnapshotResponse> getAllSnapshots() {
        return snapshotRepository.findAllOrderByDateDesc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Delete old snapshots (for cleanup)
     */
    public void deleteSnapshotsBefore(LocalDate date) {
        snapshotRepository.deleteBySnapshotDateBefore(date);
    }

    /**
     * Calculate start date based on period
     */
    private LocalDate calculateStartDate(LocalDate endDate, TrackingPeriod period) {
        return switch (period) {
            case DAILY -> endDate.minusDays(30);      // Last 30 days
            case WEEKLY -> endDate.minusWeeks(12);     // Last 12 weeks
            case MONTHLY -> endDate.minusMonths(12);   // Last 12 months
            case YEARLY -> endDate.minusYears(5);      // Last 5 years
        };
    }

    /**
     * Calculate performance metrics for the period
     */
    private PortfolioTrackingResponse.PerformanceMetrics calculateMetrics(
            List<PortfolioSnapshot> snapshots, TrackingPeriod period) {
        
        if (snapshots.isEmpty()) {
            return new PortfolioTrackingResponse.PerformanceMetrics(
                    getPeriodLabel(period),
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, null, null
            );
        }

        // Get first and last snapshots
        PortfolioSnapshot oldest = snapshots.get(0);
        PortfolioSnapshot newest = snapshots.get(snapshots.size() - 1);

        BigDecimal currentValue = newest.getTotalValue();
        BigDecimal previousValue = oldest.getTotalValue();
        BigDecimal periodChange = currentValue.subtract(previousValue);
        
        BigDecimal periodChangePct = BigDecimal.ZERO;
        if (previousValue.compareTo(BigDecimal.ZERO) > 0) {
            periodChangePct = periodChange.divide(previousValue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        // Find highest and lowest values
        PortfolioSnapshot highest = snapshots.stream()
                .max(Comparator.comparing(PortfolioSnapshot::getTotalValue))
                .orElse(newest);

        PortfolioSnapshot lowest = snapshots.stream()
                .min(Comparator.comparing(PortfolioSnapshot::getTotalValue))
                .orElse(newest);

        return new PortfolioTrackingResponse.PerformanceMetrics(
                getPeriodLabel(period),
                currentValue,
                previousValue,
                periodChange,
                periodChangePct,
                highest.getTotalValue(),
                lowest.getTotalValue(),
                highest.getSnapshotDate(),
                lowest.getSnapshotDate()
        );
    }

    private String getPeriodLabel(TrackingPeriod period) {
        return switch (period) {
            case DAILY -> "Last 30 Days";
            case WEEKLY -> "Last 12 Weeks";
            case MONTHLY -> "Last 12 Months";
            case YEARLY -> "Last 5 Years";
        };
    }

    private PortfolioSnapshotResponse toResponse(PortfolioSnapshot snapshot) {
        return new PortfolioSnapshotResponse(
                snapshot.getId(),
                snapshot.getSnapshotDate(),
                snapshot.getTotalValue(),
                snapshot.getTotalCostBasis(),
                snapshot.getTotalGainLoss(),
                snapshot.getTotalGainLossPct(),
                snapshot.getTotalPositions(),
                snapshot.getTotalQuantity()
        );
    }
}

