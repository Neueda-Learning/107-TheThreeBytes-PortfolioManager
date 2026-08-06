package com.example.hsbcproject.service;

import com.example.hsbcproject.domain.AssetType;
import com.example.hsbcproject.domain.PortfolioItem;
import com.example.hsbcproject.domain.Transaction;
import com.example.hsbcproject.domain.TransactionType;
import com.example.hsbcproject.dto.CreatePortfolioItemRequest;
import com.example.hsbcproject.dto.PortfolioItemResponse;
import com.example.hsbcproject.dto.PortfolioSummaryResponse;
import com.example.hsbcproject.dto.TransactionResponse;
import com.example.hsbcproject.dto.UpdatePortfolioItemRequest;
import com.example.hsbcproject.exception.ResourceNotFoundException;
import com.example.hsbcproject.repository.PortfolioItemRepository;
import com.example.hsbcproject.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PortfolioItemService {

    private final PortfolioItemRepository portfolioItemRepository;
    private final TransactionRepository transactionRepository;
    private final DummyMarketDataStore dummyMarketDataStore;

    public PortfolioItemService(PortfolioItemRepository portfolioItemRepository,
                                TransactionRepository transactionRepository,
                                DummyMarketDataStore dummyMarketDataStore) {
        this.portfolioItemRepository = portfolioItemRepository;
        this.transactionRepository = transactionRepository;
        this.dummyMarketDataStore = dummyMarketDataStore;
    }

    @Transactional(readOnly = true)
    public List<PortfolioItemResponse> findAll() {
        return portfolioItemRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PortfolioItemResponse findById(Long id) {
        return toResponse(getEntity(id));
    }

    public PortfolioItemResponse create(CreatePortfolioItemRequest request) {
        validateAssetSpecificFields(request.assetType(), request.maturityDate(), request.purchaseDate());

        PortfolioItem item = new PortfolioItem();
        item.setTicker(request.ticker().toUpperCase());
        item.setQuantity(request.quantity());
        item.setAssetType(request.assetType());
        item.setPurchasePrice(request.purchasePrice());
        item.setPurchaseDate(request.purchaseDate());
        item.setName(request.name());
        item.setSector(request.sector());
        item.setIssuer(request.issuer());
        item.setMaturityDate(request.maturityDate());
        
        if (request.assetType() == AssetType.BOND) {
            dummyMarketDataStore.getSeriesByTicker(item.getTicker()).ifPresent(series -> {
                if (series.bondTerms() != null) {
                    item.setInterestRate(series.bondTerms().annualInterestRate());
                }
            });
        } else {
            item.setInterestRate(request.interestRate());
        }

        PortfolioItemResponse saved = toResponse(portfolioItemRepository.save(item));
        logTransaction(item.getTicker(), item.getAssetType(), TransactionType.BUY,
                item.getQuantity(), item.getPurchasePrice(), item.getPurchaseDate());
        return saved;
    }

    public PortfolioItemResponse update(Long id, UpdatePortfolioItemRequest request) {
        validateAssetSpecificFields(request.assetType(), request.maturityDate(), request.purchaseDate());

        PortfolioItem item = getEntity(id);
        item.setTicker(request.ticker().toUpperCase());
        item.setQuantity(request.quantity());
        item.setAssetType(request.assetType());
        item.setPurchasePrice(request.purchasePrice());
        item.setPurchaseDate(request.purchaseDate());
        item.setName(request.name());
        item.setSector(request.sector());
        item.setIssuer(request.issuer());
        item.setMaturityDate(request.maturityDate());
        
        if (request.assetType() == AssetType.BOND) {
            dummyMarketDataStore.getSeriesByTicker(item.getTicker()).ifPresent(series -> {
                if (series.bondTerms() != null) {
                    item.setInterestRate(series.bondTerms().annualInterestRate());
                }
            });
        } else {
            item.setInterestRate(request.interestRate());
        }

        return toResponse(portfolioItemRepository.save(item));
    }

    private void validateAssetSpecificFields(AssetType assetType, LocalDate maturityDate, LocalDate purchaseDate) {
        if (maturityDate != null && !maturityDate.isAfter(purchaseDate)) {
            throw new IllegalArgumentException("maturityDate must be after purchaseDate");
        }
    }

    public void delete(Long id) {
        PortfolioItem item = getEntity(id);
        portfolioItemRepository.delete(item);
    }

    public TransactionResponse sell(Long id, BigDecimal pricePerUnit, BigDecimal quantityToSell) {
        PortfolioItem item = getEntity(id);

        if (quantityToSell == null || quantityToSell.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity to sell must be greater than zero");
        }
        if (quantityToSell.compareTo(item.getQuantity()) > 0) {
            throw new IllegalArgumentException("Cannot sell more than the current holding quantity");
        }
        
        if (item.getAssetType() == AssetType.BOND) {
            dummyMarketDataStore.getSeriesByTicker(item.getTicker()).ifPresent(series -> {
                if (series.bondTerms() != null) {
                    int lockInMonths = series.bondTerms().lockInMonths();
                    LocalDate lockInEndDate = item.getPurchaseDate().plusMonths(lockInMonths);
                    if (LocalDate.now().isBefore(lockInEndDate)) {
                        throw new IllegalArgumentException("Bond cannot be sold during its lock-in period. Locked until " + lockInEndDate);
                    }
                }
            });
        }

        Transaction tx = logTransaction(item.getTicker(), item.getAssetType(), TransactionType.SELL,
                quantityToSell, pricePerUnit, LocalDate.now());
        
        if (quantityToSell.compareTo(item.getQuantity()) == 0) {
            portfolioItemRepository.delete(item);
        } else {
            item.setQuantity(item.getQuantity().subtract(quantityToSell));
            portfolioItemRepository.save(item);
        }

        BigDecimal total = tx.getPricePerUnit().multiply(tx.getQuantity());
        return new TransactionResponse(tx.getId(), tx.getTicker(), tx.getAssetType(),
                tx.getTransactionType(), tx.getQuantity(), tx.getPricePerUnit(),
                total, tx.getTransactionDate(), tx.getNotes());
    }

    private Transaction logTransaction(String ticker, AssetType assetType,
                                       TransactionType type, BigDecimal quantity,
                                       BigDecimal pricePerUnit, LocalDate date) {
        Transaction tx = new Transaction();
        tx.setTicker(ticker);
        tx.setAssetType(assetType);
        tx.setTransactionType(type);
        tx.setQuantity(quantity);
        tx.setPricePerUnit(pricePerUnit);
        tx.setTransactionDate(date);
        return transactionRepository.save(tx);
    }

    @Transactional(readOnly = true)
    public PortfolioSummaryResponse getSummary() {
        List<PortfolioItem> items = portfolioItemRepository.findAll();
        Map<String, BigDecimal> quantityByType = new HashMap<>();
        Map<String, BigDecimal> costByType = new HashMap<>();
        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal totalCostBasis = BigDecimal.ZERO;

        for (PortfolioItem item : items) {
            totalQuantity = totalQuantity.add(item.getQuantity());
            BigDecimal itemCost = item.getPurchasePrice().multiply(item.getQuantity());
            totalCostBasis = totalCostBasis.add(itemCost);
            String key = item.getAssetType().name();
            quantityByType.merge(key, item.getQuantity(), BigDecimal::add);
            costByType.merge(key, itemCost, BigDecimal::add);
        }

        return new PortfolioSummaryResponse(items.size(), totalQuantity, totalCostBasis, quantityByType, costByType);
    }

    public PortfolioItem getEntity(Long id) {
        return portfolioItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio item with id " + id + " was not found"));
    }

    private PortfolioItemResponse toResponse(PortfolioItem item) {
        return new PortfolioItemResponse(item.getId(), item.getTicker(), item.getQuantity(),
                item.getAssetType(), item.getPurchasePrice(), item.getPurchaseDate(),
                item.getName(), item.getSector(), item.getIssuer(), item.getInterestRate(), item.getMaturityDate());
    }
}
