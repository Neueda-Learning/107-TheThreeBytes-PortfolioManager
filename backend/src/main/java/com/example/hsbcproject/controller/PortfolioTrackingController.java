package com.example.hsbcproject.controller;

import com.example.hsbcproject.domain.TrackingPeriod;
import com.example.hsbcproject.dto.PortfolioSnapshotResponse;
import com.example.hsbcproject.dto.PortfolioTrackingResponse;
import com.example.hsbcproject.service.PortfolioTrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolio-tracking")
@Tag(name = "Portfolio Tracking", description = "Track portfolio performance over time")
public class PortfolioTrackingController {

    private final PortfolioTrackingService trackingService;

    public PortfolioTrackingController(PortfolioTrackingService trackingService) {
        this.trackingService = trackingService;
    }

    @PostMapping("/snapshot")
    @Operation(summary = "Create a snapshot of current portfolio state")
    public ResponseEntity<PortfolioSnapshotResponse> createSnapshot() {
        PortfolioSnapshotResponse snapshot = trackingService.createSnapshot();
        return ResponseEntity.ok(snapshot);
    }

    @GetMapping("/daily")
    @Operation(summary = "Get daily tracking data (last 30 days)")
    public PortfolioTrackingResponse getDailyTracking() {
        return trackingService.getTracking(TrackingPeriod.DAILY);
    }

    @GetMapping("/weekly")
    @Operation(summary = "Get weekly tracking data (last 12 weeks)")
    public PortfolioTrackingResponse getWeeklyTracking() {
        return trackingService.getTracking(TrackingPeriod.WEEKLY);
    }

    @GetMapping("/monthly")
    @Operation(summary = "Get monthly tracking data (last 12 months)")
    public PortfolioTrackingResponse getMonthlyTracking() {
        return trackingService.getTracking(TrackingPeriod.MONTHLY);
    }

    @GetMapping("/yearly")
    @Operation(summary = "Get yearly tracking data (last 5 years)")
    public PortfolioTrackingResponse getYearlyTracking() {
        return trackingService.getTracking(TrackingPeriod.YEARLY);
    }

    @GetMapping("/period/{period}")
    @Operation(summary = "Get tracking data for a specific period")
    public PortfolioTrackingResponse getTrackingByPeriod(@PathVariable TrackingPeriod period) {
        return trackingService.getTracking(period);
    }

    @GetMapping("/snapshots")
    @Operation(summary = "Get all historical snapshots")
    public List<PortfolioSnapshotResponse> getAllSnapshots() {
        return trackingService.getAllSnapshots();
    }
}

