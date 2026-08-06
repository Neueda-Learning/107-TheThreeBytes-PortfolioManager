package com.example.hsbcproject.dto;

import com.example.hsbcproject.domain.AssetType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdatePortfolioItemRequest(
        @NotBlank(message = "ticker is required")
        @Pattern(regexp = "^[A-Za-z.]{1,10}$", message = "ticker must be letters/dot up to 10 chars")
        String ticker,

        @NotNull(message = "quantity is required")
        @DecimalMin(value = "0.00000001", message = "quantity must be at least 1")
        BigDecimal quantity,

        @NotNull(message = "assetType is required")
        AssetType assetType,

        @NotNull(message = "purchasePrice is required")
        @DecimalMin(value = "0.01", message = "purchasePrice must be greater than 0")
        BigDecimal purchasePrice,

        @NotNull(message = "purchaseDate is required")
        @PastOrPresent(message = "purchaseDate cannot be in the future")
        LocalDate purchaseDate,

        @Size(max = 200, message = "name must be at most 200 characters")
        String name,

        @Size(max = 100, message = "sector must be at most 100 characters")
        String sector,

        @Size(max = 200, message = "issuer must be at most 200 characters")
        String issuer,

        @DecimalMin(value = "0.0", message = "interestRate cannot be negative")
        @DecimalMax(value = "100.0", message = "interestRate cannot exceed 100")
        BigDecimal interestRate,

        LocalDate maturityDate) {
}

