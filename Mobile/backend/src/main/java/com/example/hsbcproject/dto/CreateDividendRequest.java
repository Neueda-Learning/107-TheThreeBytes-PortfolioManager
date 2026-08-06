package com.example.hsbcproject.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateDividendRequest(
        @NotBlank(message = "ticker is required")
        @Pattern(regexp = "^[A-Za-z.]{1,10}$", message = "ticker must be letters/dot up to 10 chars")
        String ticker,

        @NotNull(message = "dividendPerShare is required")
        @DecimalMin(value = "0.01", message = "dividendPerShare must be greater than 0")
        BigDecimal dividendPerShare,

        @NotNull(message = "sharesHeld is required")
        @Min(value = 1, message = "sharesHeld must be at least 1")
        Integer sharesHeld,

        @NotNull(message = "dividendDate is required")
        @PastOrPresent(message = "dividendDate cannot be in the future")
        LocalDate dividendDate) {
}

