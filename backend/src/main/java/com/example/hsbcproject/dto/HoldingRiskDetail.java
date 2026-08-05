package com.example.hsbcproject.dto;

import com.example.hsbcproject.domain.AssetType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record HoldingRiskDetail(
        String ticker,
        AssetType assetType,
        LocalDate purchaseDate,
        long holdingDays,
        String holdingCategory,
        BigDecimal portfolioConcentrationPct) {
}

