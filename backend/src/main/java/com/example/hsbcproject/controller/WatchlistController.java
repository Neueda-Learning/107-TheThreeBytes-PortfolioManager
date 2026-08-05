package com.example.hsbcproject.controller;

import com.example.hsbcproject.dto.CreateWatchlistItemRequest;
import com.example.hsbcproject.dto.WatchlistItemResponse;
import com.example.hsbcproject.service.WatchlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/watchlist")
@Tag(name = "Watchlist", description = "Track assets you are watching")
public class WatchlistController {

    private final WatchlistService watchlistService;

    public WatchlistController(WatchlistService watchlistService) {
        this.watchlistService = watchlistService;
    }

    @GetMapping
    @Operation(summary = "Get all watchlist items")
    public List<WatchlistItemResponse> getAll() {
        return watchlistService.findAll();
    }

    @PostMapping
    @Operation(summary = "Add a ticker to the watchlist")
    public ResponseEntity<WatchlistItemResponse> add(@Valid @RequestBody CreateWatchlistItemRequest request) {
        WatchlistItemResponse response = watchlistService.add(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove a ticker from the watchlist")
    public ResponseEntity<Void> remove(@PathVariable Long id) {
        watchlistService.remove(id);
        return ResponseEntity.noContent().build();
    }
}

