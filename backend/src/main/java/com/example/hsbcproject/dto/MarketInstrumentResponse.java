package com.example.hsbcproject.dto;

import com.example.hsbcproject.domain.AssetType;

public record MarketInstrumentResponse(
        String ticker,
        String name,
        AssetType assetType,
        String currency) {
}

