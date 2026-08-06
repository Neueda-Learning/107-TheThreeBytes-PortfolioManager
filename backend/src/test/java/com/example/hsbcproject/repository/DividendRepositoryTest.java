package com.example.hsbcproject.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.hsbcproject.domain.DividendRecord;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class DividendRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private DividendRepository repository;

    @Test
    void findByTickerIgnoreCaseReturnsMatchingRecords() {
        DividendRecord record = new DividendRecord();
        record.setTicker("MSFT");
        record.setDividendPerShare(new BigDecimal("0.50"));
        record.setSharesHeld(10);
        record.setTotalDividend(new BigDecimal("5.00"));
        record.setDividendDate(LocalDate.now());
        repository.save(record);

        assertEquals(1, repository.findByTickerIgnoreCase("msft").size());
    }
}

