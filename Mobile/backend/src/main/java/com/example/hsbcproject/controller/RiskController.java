package com.example.hsbcproject.controller;

import com.example.hsbcproject.dto.RiskAnalysisResponse;
import com.example.hsbcproject.service.RiskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/risk")
@Tag(name = "Risk", description = "Concentration, holding duration and diversification analysis")
public class RiskController {

    private final RiskService riskService;

    public RiskController(RiskService riskService) {
        this.riskService = riskService;
    }

    @GetMapping("/analysis")
    @Operation(summary = "Full risk analysis: concentration %, holding categories, diversification score")
    public RiskAnalysisResponse analyzeRisk() {
        return riskService.analyzeRisk();
    }
}

