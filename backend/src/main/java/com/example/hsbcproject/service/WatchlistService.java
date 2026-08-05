package com.example.hsbcproject.service;

import com.example.hsbcproject.domain.WatchlistItem;
import com.example.hsbcproject.dto.CreateWatchlistItemRequest;
import com.example.hsbcproject.dto.WatchlistItemResponse;
import com.example.hsbcproject.exception.ResourceNotFoundException;
import com.example.hsbcproject.repository.WatchlistItemRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WatchlistService {

    private final WatchlistItemRepository watchlistItemRepository;

    public WatchlistService(WatchlistItemRepository watchlistItemRepository) {
        this.watchlistItemRepository = watchlistItemRepository;
    }

    @Transactional(readOnly = true)
    public List<WatchlistItemResponse> findAll() {
        return watchlistItemRepository.findAll().stream().map(this::toResponse).toList();
    }

    public WatchlistItemResponse add(CreateWatchlistItemRequest request) {
        WatchlistItem item = new WatchlistItem();
        item.setTicker(request.ticker().toUpperCase());
        item.setAssetType(request.assetType());
        item.setAddedDate(LocalDate.now());
        return toResponse(watchlistItemRepository.save(item));
    }

    public void remove(Long id) {
        WatchlistItem item = watchlistItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Watchlist item with id " + id + " not found"));
        watchlistItemRepository.delete(item);
    }

    @Transactional(readOnly = true)
    public boolean isWatched(String ticker) {
        return watchlistItemRepository.existsByTickerIgnoreCase(ticker);
    }

    private WatchlistItemResponse toResponse(WatchlistItem item) {
        return new WatchlistItemResponse(item.getId(), item.getTicker(), item.getAssetType(), item.getAddedDate());
    }
}

