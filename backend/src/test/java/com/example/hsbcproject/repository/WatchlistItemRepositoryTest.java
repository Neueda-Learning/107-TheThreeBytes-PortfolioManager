package com.example.hsbcproject.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.hsbcproject.domain.AssetType;
import com.example.hsbcproject.domain.WatchlistItem;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class WatchlistItemRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private WatchlistItemRepository repository;

    @Test
    void existsByTickerIgnoreCaseDetectsSavedItem() {
        WatchlistItem item = new WatchlistItem();
        item.setTicker("ETH");
        item.setAssetType(AssetType.CRYPTO);
        item.setAddedDate(LocalDate.now());
        repository.save(item);

        assertTrue(repository.existsByTickerIgnoreCase("eth"));
        assertFalse(repository.existsByTickerIgnoreCase("btc"));
    }
}

