package com.example.hsbcproject.controller;

import com.example.hsbcproject.domain.AssetType;
import com.example.hsbcproject.dto.DashboardResponse;
import com.example.hsbcproject.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Combined and per-asset-type portfolio snapshots")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    @Operation(summary = "Combined dashboard — all holdings with P&L and live value estimate")
    public DashboardResponse getCombined() {
        return dashboardService.getCombinedDashboard();
    }

    @GetMapping("/{assetType}")
    @Operation(summary = "Dashboard filtered by asset type (STOCK, BOND, CRYPTO)")
    public DashboardResponse getByAssetType(@PathVariable AssetType assetType) {
        return dashboardService.getDashboardByAssetType(assetType);
    }
}

