package com.example.hsbcproject.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.hsbcproject.domain.AssetType;
import com.example.hsbcproject.domain.PortfolioItem;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PortfolioItemRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private PortfolioItemRepository repository;

    @Test
    void saveAndFindByIdWorks() {
        PortfolioItem item = new PortfolioItem();
        item.setTicker("AAPL");
        item.setQuantity(new BigDecimal("1.00000000"));
        item.setAssetType(AssetType.STOCK);
        item.setPurchasePrice(new BigDecimal("100.00"));
        item.setPurchaseDate(LocalDate.now());

        PortfolioItem saved = repository.save(item);

        assertTrue(repository.findById(saved.getId()).isPresent());
        assertEquals("AAPL", repository.findById(saved.getId()).orElseThrow().getTicker());
    }
}

