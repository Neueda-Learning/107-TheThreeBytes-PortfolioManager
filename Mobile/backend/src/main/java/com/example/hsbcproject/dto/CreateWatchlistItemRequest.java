package com.example.hsbcproject.dto;

import com.example.hsbcproject.domain.AssetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreateWatchlistItemRequest(
        @NotBlank(message = "ticker is required")
        @Pattern(regexp = "^[A-Za-z.]{1,10}$", message = "ticker must be letters/dot up to 10 chars")
        String ticker,

        @NotNull(message = "assetType is required")
        AssetType assetType) {
}

