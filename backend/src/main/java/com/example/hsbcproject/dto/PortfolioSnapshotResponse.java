package com.example.hsbcproject.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PortfolioSnapshotResponse(
        Long id,
        LocalDate snapshotDate,
        BigDecimal totalValue,
        BigDecimal totalCostBasis,
        BigDecimal totalGainLoss,
        BigDecimal totalGainLossPct,
        Long totalPositions,
        BigDecimal totalQuantity
) {
}

