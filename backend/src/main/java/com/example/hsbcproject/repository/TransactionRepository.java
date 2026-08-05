package com.example.hsbcproject.repository;

import com.example.hsbcproject.domain.Transaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByTickerIgnoreCase(String ticker);
}
