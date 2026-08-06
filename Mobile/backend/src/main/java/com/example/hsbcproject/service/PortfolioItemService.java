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

    public PortfolioItemService(PortfolioItemRepository portfolioItemRepository,
                                TransactionRepository transactionRepository) {
        this.portfolioItemRepository = portfolioItemRepository;
        this.transactionRepository = transactionRepository;
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
        item.setInterestRate(request.interestRate());
        item.setMaturityDate(request.maturityDate());
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
        item.setInterestRate(request.interestRate());
        item.setMaturityDate(request.maturityDate());
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

    public TransactionResponse sell(Long id, BigDecimal pricePerUnit) {
        PortfolioItem item = getEntity(id);
        Transaction tx = logTransaction(item.getTicker(), item.getAssetType(), TransactionType.SELL,
                item.getQuantity(), pricePerUnit, LocalDate.now());
        portfolioItemRepository.delete(item);
        BigDecimal total = tx.getPricePerUnit().multiply(BigDecimal.valueOf(tx.getQuantity()));
        return new TransactionResponse(tx.getId(), tx.getTicker(), tx.getAssetType(),
                tx.getTransactionType(), tx.getQuantity(), tx.getPricePerUnit(),
                total, tx.getTransactionDate(), tx.getNotes());
    }

    private Transaction logTransaction(String ticker, AssetType assetType,
                                       TransactionType type, Integer quantity,
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
        Map<String, Long> quantityByType = new HashMap<>();
        Map<String, BigDecimal> costByType = new HashMap<>();
        long totalQuantity = 0;
        BigDecimal totalCostBasis = BigDecimal.ZERO;

        for (PortfolioItem item : items) {
            totalQuantity += item.getQuantity();
            BigDecimal itemCost = item.getPurchasePrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalCostBasis = totalCostBasis.add(itemCost);
            String key = item.getAssetType().name();
            quantityByType.merge(key, (long) item.getQuantity(), Long::sum);
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
