package com.example.hsbcproject.dto;

import java.math.BigDecimal;
import java.util.Map;

public record PortfolioSummaryResponse(
        long totalPositions,
        BigDecimal totalQuantity,
        BigDecimal totalCostBasis,
        Map<String, BigDecimal> quantityByAssetType,
        Map<String, BigDecimal> costByAssetType) {
}

