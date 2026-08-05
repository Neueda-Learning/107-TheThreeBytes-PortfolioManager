package com.example.hsbcproject.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DividendResponse(
        Long id,
        String ticker,
        BigDecimal dividendPerShare,
        Integer sharesHeld,
        BigDecimal totalDividend,
        LocalDate dividendDate) {
}

