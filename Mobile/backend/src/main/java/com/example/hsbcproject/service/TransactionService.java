package com.example.hsbcproject.service;

import com.example.hsbcproject.domain.Transaction;
import com.example.hsbcproject.dto.CreateTransactionRequest;
import com.example.hsbcproject.dto.TransactionResponse;
import com.example.hsbcproject.exception.ResourceNotFoundException;
import com.example.hsbcproject.repository.TransactionRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> findAll() {
        return transactionRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TransactionResponse findById(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> findByTicker(String ticker) {
        return transactionRepository.findByTickerIgnoreCase(ticker).stream().map(this::toResponse).toList();
    }

    public TransactionResponse create(CreateTransactionRequest request) {
        Transaction tx = new Transaction();
        tx.setTicker(request.ticker().toUpperCase());
        tx.setAssetType(request.assetType());
        tx.setTransactionType(request.transactionType());
        tx.setQuantity(request.quantity());
        tx.setPricePerUnit(request.pricePerUnit());
        tx.setTransactionDate(request.transactionDate());
        tx.setNotes(request.notes());
        return toResponse(transactionRepository.save(tx));
    }

    public void delete(Long id) {
        Transaction tx = getEntity(id);
        transactionRepository.delete(tx);
    }

    private Transaction getEntity(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction with id " + id + " not found"));
    }

    private TransactionResponse toResponse(Transaction tx) {
        BigDecimal total = tx.getPricePerUnit().multiply(BigDecimal.valueOf(tx.getQuantity()));
        return new TransactionResponse(tx.getId(), tx.getTicker(), tx.getAssetType(),
                tx.getTransactionType(), tx.getQuantity(), tx.getPricePerUnit(),
                total, tx.getTransactionDate(), tx.getNotes());
    }
}

