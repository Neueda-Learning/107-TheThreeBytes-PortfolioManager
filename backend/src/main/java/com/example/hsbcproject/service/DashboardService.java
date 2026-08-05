package com.example.hsbcproject.service;

import com.example.hsbcproject.domain.AssetType;
import com.example.hsbcproject.dto.DashboardResponse;
import com.example.hsbcproject.dto.PerformanceItemResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final PerformanceService performanceService;

    public DashboardService(PerformanceService performanceService) {
        this.performanceService = performanceService;
    }

    public DashboardResponse getCombinedDashboard() {
        List<PerformanceItemResponse> holdings = performanceService.getAllPerformance();
        return buildResponse(holdings);
    }

    public DashboardResponse getDashboardByAssetType(AssetType assetType) {
        List<PerformanceItemResponse> holdings = performanceService.getAllPerformance().stream()
                .filter(h -> h.assetType() == assetType)
                .toList();
        return buildResponse(holdings);
    }

    private DashboardResponse buildResponse(List<PerformanceItemResponse> holdings) {
        BigDecimal totalCostBasis = BigDecimal.ZERO;
        BigDecimal totalCurrentValue = BigDecimal.ZERO;

        for (PerformanceItemResponse h : holdings) {
            totalCostBasis = totalCostBasis.add(h.costBasis());
            totalCurrentValue = totalCurrentValue.add(h.currentValue());
        }

        BigDecimal totalUnrealizedGain = totalCurrentValue.subtract(totalCostBasis);
        BigDecimal totalUnrealizedGainPct = totalCostBasis.compareTo(BigDecimal.ZERO) > 0
                ? totalUnrealizedGain.divide(totalCostBasis, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new DashboardResponse(
                holdings,
                holdings.size(),
                totalCostBasis.setScale(2, RoundingMode.HALF_UP),
                totalCurrentValue.setScale(2, RoundingMode.HALF_UP),
                totalUnrealizedGain.setScale(2, RoundingMode.HALF_UP),
                totalUnrealizedGainPct);
    }
}
