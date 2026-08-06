package com.example.hsbcproject.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Public response DTO for 30-day (or N-day) OHLCV stock history.
 * Used by GET /api/prices/history/{ticker}
 */
public record StockCandleResponse(
        String ticker,
        String source,          // "FINNHUB" or "DUMMY"
        List<DailyCandle> candles) {

    public record DailyCandle(
            LocalDate date,
            BigDecimal open,
            BigDecimal high,
            BigDecimal low,
            BigDecimal close,
            BigDecimal volume) {
    }
}
