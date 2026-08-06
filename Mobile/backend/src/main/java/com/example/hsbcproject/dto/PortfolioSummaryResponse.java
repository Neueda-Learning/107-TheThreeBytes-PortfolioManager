package com.example.hsbcproject.dto;

import java.math.BigDecimal;
import java.util.Map;

public record PortfolioSummaryResponse(
        long totalPositions,
        long totalQuantity,
        BigDecimal totalCostBasis,
        Map<String, Long> quantityByAssetType,
        Map<String, BigDecimal> costByAssetType) {
}

