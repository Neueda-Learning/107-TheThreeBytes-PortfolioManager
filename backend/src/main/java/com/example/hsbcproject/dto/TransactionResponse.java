package com.example.hsbcproject.dto;

import com.example.hsbcproject.domain.AssetType;
import com.example.hsbcproject.domain.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionResponse(
        Long id,
        String ticker,
        AssetType assetType,
        TransactionType transactionType,
        BigDecimal quantity,
        BigDecimal pricePerUnit,
        BigDecimal totalValue,
        LocalDate transactionDate,
        String notes) {
}

