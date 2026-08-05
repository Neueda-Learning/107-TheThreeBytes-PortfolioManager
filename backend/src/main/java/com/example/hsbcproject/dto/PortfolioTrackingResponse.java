package com.example.hsbcproject.dto;

import com.example.hsbcproject.domain.TrackingPeriod;

import java.util.List;

public record PortfolioTrackingResponse(
        TrackingPeriod period,
        List<PortfolioSnapshotResponse> snapshots,
        PerformanceMetrics metrics
) {
    public record PerformanceMetrics(
            String periodLabel,
            java.math.BigDecimal currentValue,
            java.math.BigDecimal previousValue,
            java.math.BigDecimal periodChange,
            java.math.BigDecimal periodChangePct,
            java.math.BigDecimal highestValue,
            java.math.BigDecimal lowestValue,
            java.time.LocalDate highestDate,
            java.time.LocalDate lowestDate
    ) {
    }
}

