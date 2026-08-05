package com.example.hsbcproject.dto;

import java.math.BigDecimal;

public record StockQuoteResponse(
        String symbol,
        String companyName,
        BigDecimal currentPrice,
        String sector,
        String errorMessage) {
}
