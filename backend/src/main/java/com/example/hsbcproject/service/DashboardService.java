package com.example.hsbcproject.service;

import com.example.hsbcproject.domain.AssetType;
import com.example.hsbcproject.domain.PortfolioItem;
import com.example.hsbcproject.dto.DashboardResponse;
import com.example.hsbcproject.dto.PortfolioItemResponse;
import com.example.hsbcproject.repository.PortfolioItemRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final PortfolioItemRepository portfolioItemRepository;
    private final PriceService priceService;

    public DashboardService(PortfolioItemRepository portfolioItemRepository, PriceService priceService) {
        this.portfolioItemRepository = portfolioItemRepository;
        this.priceService = priceService;
    }

    public DashboardResponse getCombinedDashboard() {
        return buildDashboard(portfolioItemRepository.findAll());
    }

    public DashboardResponse getDashboardByAssetType(AssetType assetType) {
        List<PortfolioItem> items = portfolioItemRepository.findAll().stream()
                .filter(i -> i.getAssetType() == assetType)
                .toList();
        return buildDashboard(items);
    }

    private DashboardResponse buildDashboard(List<PortfolioItem> items) {
        // Fetch live prices once per unique ticker to avoid N redundant HTTP calls
        Map<String, BigDecimal> livePriceByTicker = items.stream()
                .map(PortfolioItem::getTicker)
                .distinct()
                .map(ticker -> new java.util.AbstractMap.SimpleEntry<>(ticker, priceService.getPrice(ticker).currentPrice()))
                .filter(e -> e.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        BigDecimal totalCostBasis = BigDecimal.ZERO;
        BigDecimal totalCurrentValue = BigDecimal.ZERO;
        Map<String, BigDecimal> quantityByType = new HashMap<>();
        Map<String, BigDecimal> costByType = new HashMap<>();
        BigDecimal totalQuantity = BigDecimal.ZERO;

        for (PortfolioItem item : items) {
            BigDecimal cost = item.getPurchasePrice().multiply(item.getQuantity());
            totalCostBasis = totalCostBasis.add(cost);
            totalQuantity = totalQuantity.add(item.getQuantity());

            String type = item.getAssetType().name();
            quantityByType.merge(type, item.getQuantity(), BigDecimal::add);
            costByType.merge(type, cost, BigDecimal::add);

            BigDecimal livePrice = livePriceByTicker.get(item.getTicker());
            BigDecimal currentPrice = livePrice != null ? livePrice : item.getPurchasePrice();
            totalCurrentValue = totalCurrentValue.add(currentPrice.multiply(item.getQuantity()));
        }

        BigDecimal gainLoss = totalCurrentValue.subtract(totalCostBasis);
        BigDecimal gainLossPct = totalCostBasis.compareTo(BigDecimal.ZERO) > 0
                ? gainLoss.divide(totalCostBasis, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        List<PortfolioItemResponse> responses = items.stream()
                .map(i -> new PortfolioItemResponse(i.getId(), i.getTicker(), i.getQuantity(),
                        i.getAssetType(), i.getPurchasePrice(), i.getPurchaseDate(),
                        i.getName(), i.getSector(), i.getIssuer(), i.getInterestRate(), i.getMaturityDate()))
                .toList();

        return new DashboardResponse(items.size(), totalQuantity,
                totalCostBasis.setScale(2, RoundingMode.HALF_UP),
                totalCurrentValue.setScale(2, RoundingMode.HALF_UP),
                gainLoss.setScale(2, RoundingMode.HALF_UP),
                gainLossPct, quantityByType, costByType, responses);
    }
}

