package com.example.hsbcproject.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record RiskAnalysisResponse(
        Map<String, BigDecimal> concentrationByAssetType,
        List<HoldingRiskDetail> holdingRiskDetails,
        BigDecimal diversificationScore,
        String overallRiskLevel) {
}

