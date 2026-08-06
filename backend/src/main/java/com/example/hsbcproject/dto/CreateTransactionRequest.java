package com.example.hsbcproject.dto;

import com.example.hsbcproject.domain.AssetType;
import com.example.hsbcproject.domain.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateTransactionRequest(
        @NotBlank(message = "ticker is required")
        @Pattern(regexp = "^[A-Za-z.]{1,10}$", message = "ticker must be letters/dot up to 10 chars")
        String ticker,

        @NotNull(message = "assetType is required")
        AssetType assetType,

        @NotNull(message = "transactionType is required")
        TransactionType transactionType,

        @NotNull(message = "quantity is required")
        @DecimalMin(value = "0.00000001", message = "quantity must be at least 1")
        BigDecimal quantity,

        @NotNull(message = "pricePerUnit is required")
        @DecimalMin(value = "0.01", message = "pricePerUnit must be greater than 0")
        BigDecimal pricePerUnit,

        @NotNull(message = "transactionDate is required")
        @PastOrPresent(message = "transactionDate cannot be in the future")
        LocalDate transactionDate,

        String notes) {
}

