package com.example.hsbcproject.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.hsbcproject.domain.AssetType;
import com.example.hsbcproject.domain.Transaction;
import com.example.hsbcproject.domain.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TransactionRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private TransactionRepository repository;

    @Test
    void findByTickerIgnoreCaseReturnsMatchingTransactions() {
        Transaction tx = new Transaction();
        tx.setTicker("AAPL");
        tx.setAssetType(AssetType.STOCK);
        tx.setTransactionType(TransactionType.BUY);
        tx.setQuantity(new BigDecimal("1.00000000"));
        tx.setPricePerUnit(new BigDecimal("100.00"));
        tx.setTransactionDate(LocalDate.now());
        repository.save(tx);

        assertEquals(1, repository.findByTickerIgnoreCase("aapl").size());
    }

    @Test
    void findByTransactionTypeReturnsMatchingTransactions() {
        Transaction buy = new Transaction();
        buy.setTicker("MSFT");
        buy.setAssetType(AssetType.STOCK);
        buy.setTransactionType(TransactionType.BUY);
        buy.setQuantity(new BigDecimal("2.00000000"));
        buy.setPricePerUnit(new BigDecimal("200.00"));
        buy.setTransactionDate(LocalDate.now());
        repository.save(buy);

        Transaction sell = new Transaction();
        sell.setTicker("MSFT");
        sell.setAssetType(AssetType.STOCK);
        sell.setTransactionType(TransactionType.SELL);
        sell.setQuantity(new BigDecimal("1.00000000"));
        sell.setPricePerUnit(new BigDecimal("210.00"));
        sell.setTransactionDate(LocalDate.now());
        repository.save(sell);

        assertEquals(1, repository.findByTransactionType(TransactionType.SELL).size());
    }
}

