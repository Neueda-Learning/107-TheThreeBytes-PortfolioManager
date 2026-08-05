
package com.example.hsbcproject.controller;

import com.example.hsbcproject.dto.CreateTransactionRequest;
import com.example.hsbcproject.dto.TransactionResponse;
import com.example.hsbcproject.service.TransactionService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transactions", description = "Record and query buy/sell transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    @Operation(summary = "List all transactions, optionally filter by ticker")
    public List<TransactionResponse> getAll(@RequestParam(required = false) String ticker) {
        if (ticker != null && !ticker.isBlank()) {
            return transactionService.findByTicker(ticker);
        }
        return transactionService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a transaction by id")
    public TransactionResponse getById(@PathVariable Long id) {
        return transactionService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Record a new buy or sell transaction")
    public ResponseEntity<TransactionResponse> create(@Valid @RequestBody CreateTransactionRequest request) {
        TransactionResponse response = transactionService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a transaction")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        transactionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

