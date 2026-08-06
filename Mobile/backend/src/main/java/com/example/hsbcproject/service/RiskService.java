package com.example.hsbcproject.service;

import com.example.hsbcproject.domain.PortfolioItem;
import com.example.hsbcproject.dto.HoldingRiskDetail;
import com.example.hsbcproject.dto.RiskAnalysisResponse;
import com.example.hsbcproject.repository.PortfolioItemRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RiskService {

    private final PortfolioItemRepository portfolioItemRepository;

    public RiskService(PortfolioItemRepository portfolioItemRepository) {
        this.portfolioItemRepository = portfolioItemRepository;
    }

    public RiskAnalysisResponse analyzeRisk() {
        List<PortfolioItem> items = portfolioItemRepository.findAll();

        if (items.isEmpty()) {
            return new RiskAnalysisResponse(Map.of(), List.of(), BigDecimal.ZERO, "LOW");
        }

        BigDecimal totalCost = items.stream()
                .map(i -> i.getPurchasePrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Concentration by asset type (%)
        Map<String, BigDecimal> costByType = new HashMap<>();
        for (PortfolioItem item : items) {
            BigDecimal cost = item.getPurchasePrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            costByType.merge(item.getAssetType().name(), cost, BigDecimal::add);
        }
        Map<String, BigDecimal> concentrationByType = new HashMap<>();
        costByType.forEach((type, cost) -> {
            BigDecimal pct = cost.divide(totalCost, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
            concentrationByType.put(type, pct);
        });

        // Per-holding risk details
        List<HoldingRiskDetail> riskDetails = items.stream().map(item -> {
            long days = ChronoUnit.DAYS.between(item.getPurchaseDate(), LocalDate.now());
            String category = days < 30 ? "SHORT_TERM" : days < 365 ? "MEDIUM_TERM" : "LONG_TERM";
            BigDecimal cost = item.getPurchasePrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            BigDecimal concentration = cost.divide(totalCost, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
            return new HoldingRiskDetail(item.getTicker(), item.getAssetType(),
                    item.getPurchaseDate(), days, category, concentration);
        }).toList();

        // Diversification score: 0-100
        int assetTypeCount = concentrationByType.size();
        int tickerCount = Math.min(items.size(), 10);
        BigDecimal diversScore = BigDecimal.valueOf(Math.min(100, (assetTypeCount * 20) + (tickerCount * 8)));

        // Overall risk level based on max single concentration
        BigDecimal maxConcentration = concentrationByType.values().stream()
                .max(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);
        String riskLevel = maxConcentration.compareTo(BigDecimal.valueOf(70)) > 0 ? "HIGH"
                : maxConcentration.compareTo(BigDecimal.valueOf(40)) > 0 ? "MEDIUM" : "LOW";

        return new RiskAnalysisResponse(concentrationByType, riskDetails, diversScore, riskLevel);
    }
}

