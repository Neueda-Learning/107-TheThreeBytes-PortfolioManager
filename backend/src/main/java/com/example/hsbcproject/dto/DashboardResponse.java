package com.example.hsbcproject.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record DashboardResponse(
        long totalPositions,
        BigDecimal totalQuantity,
        BigDecimal totalCostBasis,
        BigDecimal estimatedTotalValue,
        BigDecimal unrealizedGainLoss,
        BigDecimal unrealizedGainLossPct,
        Map<String, BigDecimal> quantityByAssetType,
        Map<String, BigDecimal> costByAssetType,
        List<PortfolioItemResponse> holdings) {
}

