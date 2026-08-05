package com.example.hsbcproject.controller;

import com.example.hsbcproject.dto.PerformanceItemResponse;
import com.example.hsbcproject.service.PerformanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/performance")
@Tag(name = "Performance", description = "Unrealised P&L and % gain/loss per holding")
public class PerformanceController {

    private final PerformanceService performanceService;

    public PerformanceController(PerformanceService performanceService) {
        this.performanceService = performanceService;
    }

    @GetMapping
    @Operation(summary = "Performance analytics for all holdings")
    public List<PerformanceItemResponse> getAll() {
        return performanceService.getAllPerformance();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Performance analytics for a single holding by id")
    public PerformanceItemResponse getById(@PathVariable Long id) {
        return performanceService.getPerformanceById(id);
    }
}

