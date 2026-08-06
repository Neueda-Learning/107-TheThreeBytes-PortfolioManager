package com.example.hsbcproject.service;

import com.example.hsbcproject.domain.PortfolioItem;
import com.example.hsbcproject.dto.LivePriceResponse;
import com.example.hsbcproject.dto.PerformanceItemResponse;
import com.example.hsbcproject.repository.PortfolioItemRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PerformanceService {

    private final PortfolioItemRepository portfolioItemRepository;
    private final PriceService priceService;

    public PerformanceService(PortfolioItemRepository portfolioItemRepository, PriceService priceService) {
        this.portfolioItemRepository = portfolioItemRepository;
        this.priceService = priceService;
    }

    public List<PerformanceItemResponse> getAllPerformance() {
        return portfolioItemRepository.findAll().stream()
                .map(this::buildPerformance)
                .toList();
    }

    public PerformanceItemResponse getPerformanceById(Long id) {
        PortfolioItem item = portfolioItemRepository.findById(id)
                .orElseThrow(() -> new com.example.hsbcproject.exception.ResourceNotFoundException(
                        "Portfolio item with id " + id + " not found"));
        return buildPerformance(item);
    }

    private PerformanceItemResponse buildPerformance(PortfolioItem item) {
        LivePriceResponse priceData = priceService.getPrice(item.getTicker(), item.getAssetType());
        BigDecimal currentPrice = priceData.currentPrice() != null
                ? priceData.currentPrice() : item.getPurchasePrice();

        BigDecimal costBasis = item.getPurchasePrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        BigDecimal currentValue = currentPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
        BigDecimal unrealizedGain = currentValue.subtract(costBasis);
        BigDecimal unrealizedGainPct = costBasis.compareTo(BigDecimal.ZERO) > 0
                ? unrealizedGain.divide(costBasis, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        long holdingDays = ChronoUnit.DAYS.between(item.getPurchaseDate(), LocalDate.now());

        return new PerformanceItemResponse(item.getId(), item.getTicker(), item.getAssetType(),
                item.getQuantity(), item.getPurchasePrice(), currentPrice,
                costBasis.setScale(2, RoundingMode.HALF_UP),
                currentValue.setScale(2, RoundingMode.HALF_UP),
                unrealizedGain.setScale(2, RoundingMode.HALF_UP),
                unrealizedGainPct, item.getPurchaseDate(), holdingDays);
    }
}

