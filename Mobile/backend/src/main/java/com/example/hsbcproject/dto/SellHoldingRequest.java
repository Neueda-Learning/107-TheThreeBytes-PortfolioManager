package com.example.hsbcproject.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record SellHoldingRequest(
        @NotNull(message = "pricePerUnit is required")
        @DecimalMin(value = "0.01", message = "pricePerUnit must be greater than 0")
        BigDecimal pricePerUnit) {
}
