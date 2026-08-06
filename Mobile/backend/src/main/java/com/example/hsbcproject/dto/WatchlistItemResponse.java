package com.example.hsbcproject.dto;

import com.example.hsbcproject.domain.AssetType;
import java.time.LocalDate;

public record WatchlistItemResponse(Long id, String ticker, AssetType assetType, LocalDate addedDate) {
}

