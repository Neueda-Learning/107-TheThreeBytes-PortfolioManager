package com.example.hsbcproject.service;

import com.example.hsbcproject.domain.PortfolioItem;
import com.example.hsbcproject.dto.LivePriceResponse;
import com.example.hsbcproject.dto.TaxItemResponse;
import com.example.hsbcproject.repository.PortfolioItemRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Estimates tax liability on current holdings.
 * SHORT_TERM: held < 365 days → 30% rate (income tax estimate)
 * LONG_TERM:  held ≥ 365 days → 15% rate (capital gains estimate)
 * Note: These are indicative rates — users should consult a tax advisor.
 */
@Service
@Transactional(readOnly = true)
public class TaxService {

    private static final BigDecimal SHORT_TERM_RATE = new BigDecimal("0.30");
    private static final BigDecimal LONG_TERM_RATE  = new BigDecimal("0.15");
    private static final long LONG_TERM_THRESHOLD_DAYS = 365;

    private final PortfolioItemRepository portfolioItemRepository;
    private final PriceService priceService;

    public TaxService(PortfolioItemRepository portfolioItemRepository, PriceService priceService) {
        this.portfolioItemRepository = portfolioItemRepository;
        this.priceService = priceService;
    }

    public List<TaxItemResponse> estimateTax() {
        return portfolioItemRepository.findAll().stream()
                .map(this::buildTaxItem)
                .toList();
    }

    private TaxItemResponse buildTaxItem(PortfolioItem item) {
        long holdingDays = ChronoUnit.DAYS.between(item.getPurchaseDate(), LocalDate.now());
        boolean isLongTerm = holdingDays >= LONG_TERM_THRESHOLD_DAYS;
        String taxCategory = isLongTerm ? "LONG_TERM" : "SHORT_TERM";
        BigDecimal taxRate = isLongTerm ? LONG_TERM_RATE : SHORT_TERM_RATE;

        LivePriceResponse priceData = priceService.getPrice(item.getTicker(), item.getAssetType());
        BigDecimal currentPrice = priceData.currentPrice() != null
                ? priceData.currentPrice() : item.getPurchasePrice();

        BigDecimal costBasis = item.getPurchasePrice().multiply(item.getQuantity());
        BigDecimal currentValue = currentPrice.multiply(item.getQuantity());
        BigDecimal estimatedGain = currentValue.subtract(costBasis);

        BigDecimal taxLiability = estimatedGain.compareTo(BigDecimal.ZERO) > 0
                ? estimatedGain.multiply(taxRate).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new TaxItemResponse(item.getTicker(), item.getAssetType(), item.getPurchaseDate(),
                holdingDays, taxCategory,
                costBasis.setScale(2, RoundingMode.HALF_UP),
                currentValue.setScale(2, RoundingMode.HALF_UP),
                estimatedGain.setScale(2, RoundingMode.HALF_UP),
                taxRate, taxLiability);
    }
}

