package com.example.hsbcproject.dto;

import com.example.hsbcproject.domain.AssetType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TaxItemResponse(
        String ticker,
        AssetType assetType,
        LocalDate purchaseDate,
        long holdingDays,
        String taxCategory,
        BigDecimal costBasis,
        BigDecimal estimatedCurrentValue,
        BigDecimal estimatedGain,
        BigDecimal taxRate,
        BigDecimal estimatedTaxLiability) {
}

