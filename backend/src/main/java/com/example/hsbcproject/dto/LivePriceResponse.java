package com.example.hsbcproject.dto;

import java.math.BigDecimal;

public record LivePriceResponse(
        String ticker,
        BigDecimal currentPrice,
        BigDecimal change,
        BigDecimal changePercent,
        String note) {
}
