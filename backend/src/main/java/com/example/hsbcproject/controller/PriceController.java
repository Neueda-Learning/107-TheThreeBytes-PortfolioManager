package com.example.hsbcproject.controller;

import com.example.hsbcproject.dto.LivePriceResponse;
import com.example.hsbcproject.service.PriceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/prices")
@Tag(name = "Prices", description = "Fetch live daily price data from Yahoo Finance")
public class PriceController {

    private final PriceService priceService;

    public PriceController(PriceService priceService) {
        this.priceService = priceService;
    }

    @GetMapping("/{ticker}")
    @Operation(summary = "Get current price for a ticker (e.g. AAPL, TSLA, AMZN, C, FB)")
    public LivePriceResponse getPrice(@PathVariable String ticker) {
        return priceService.getPrice(ticker);
    }
}

