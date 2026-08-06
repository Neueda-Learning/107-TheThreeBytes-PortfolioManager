package com.example.hsbcproject.repository;

import com.example.hsbcproject.domain.DividendRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DividendRepository extends JpaRepository<DividendRecord, Long> {
    List<DividendRecord> findByTickerIgnoreCase(String ticker);
}

