package com.example.hsbcproject.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(
        List<PerformanceItemResponse> holdings,
        long totalPositions,
        BigDecimal totalCostBasis,
        BigDecimal totalCurrentValue,
        BigDecimal totalUnrealizedGain,
        BigDecimal totalUnrealizedGainPct) {
}
