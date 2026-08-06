package com.example.hsbcproject.controller;

import com.example.hsbcproject.dto.CreatePortfolioItemRequest;
import com.example.hsbcproject.dto.PortfolioItemResponse;
import com.example.hsbcproject.dto.PortfolioSummaryResponse;
import com.example.hsbcproject.dto.SellHoldingRequest;
import com.example.hsbcproject.dto.TransactionResponse;
import com.example.hsbcproject.dto.UpdatePortfolioItemRequest;
import com.example.hsbcproject.service.PortfolioItemService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/portfolio-items")
@Tag(name = "Portfolio Items", description = "Manage holdings in the portfolio")
public class PortfolioItemController {

    private final PortfolioItemService portfolioItemService;

    public PortfolioItemController(PortfolioItemService portfolioItemService) {
        this.portfolioItemService = portfolioItemService;
    }

    @GetMapping
    @Operation(summary = "List portfolio items")
    public List<PortfolioItemResponse> getAll() {
        return portfolioItemService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a portfolio item by id")
    public PortfolioItemResponse getById(@PathVariable Long id) {
        return portfolioItemService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Create a portfolio item")
    public ResponseEntity<PortfolioItemResponse> create(@Valid @RequestBody CreatePortfolioItemRequest request) {
        PortfolioItemResponse response = portfolioItemService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a portfolio item")
    public PortfolioItemResponse update(@PathVariable Long id, @Valid @RequestBody UpdatePortfolioItemRequest request) {
        return portfolioItemService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a portfolio item")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        portfolioItemService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/sell")
    @Operation(summary = "Sell all units of a holding and log a SELL transaction")
    public TransactionResponse sell(@PathVariable Long id, @Valid @RequestBody SellHoldingRequest request) {
        return portfolioItemService.sell(id, request.pricePerUnit());
    }

    @GetMapping("/summary")
    @Operation(summary = "Get summary totals for the portfolio")
    public PortfolioSummaryResponse summary() {
        return portfolioItemService.getSummary();
    }
}

