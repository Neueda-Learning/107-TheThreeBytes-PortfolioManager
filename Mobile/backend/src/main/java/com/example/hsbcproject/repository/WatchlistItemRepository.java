package com.example.hsbcproject.repository;

import com.example.hsbcproject.domain.WatchlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WatchlistItemRepository extends JpaRepository<WatchlistItem, Long> {
    boolean existsByTickerIgnoreCase(String ticker);
}

