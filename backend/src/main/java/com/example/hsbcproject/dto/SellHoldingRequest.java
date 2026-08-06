package com.example.hsbcproject.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record SellHoldingRequest(
        @NotNull(message = "pricePerUnit is required")
        @DecimalMin(value = "0.01", message = "pricePerUnit must be greater than 0")
        BigDecimal pricePerUnit,
        
        @NotNull(message = "quantity is required")
        @DecimalMin(value = "0.00000001", message = "quantity must be greater than 0")
        BigDecimal quantity) {
}
