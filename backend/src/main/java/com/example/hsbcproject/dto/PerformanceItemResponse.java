package com.example.hsbcproject.dto;

import com.example.hsbcproject.domain.AssetType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PerformanceItemResponse(
        Long id,
        String ticker,
        AssetType assetType,
        Integer quantity,
        BigDecimal purchasePrice,
        BigDecimal currentPrice,
        BigDecimal costBasis,
        BigDecimal currentValue,
        BigDecimal unrealizedGain,
        BigDecimal unrealizedGainPct,
        LocalDate purchaseDate,
        long holdingDays) {
}
