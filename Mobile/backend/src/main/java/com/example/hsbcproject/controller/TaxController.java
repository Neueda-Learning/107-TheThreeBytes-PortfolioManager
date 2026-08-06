package com.example.hsbcproject.controller;

import com.example.hsbcproject.dto.TaxItemResponse;
import com.example.hsbcproject.service.TaxService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tax")
@Tag(name = "Tax", description = "Estimate short-term and long-term capital gains tax on holdings")
public class TaxController {

    private final TaxService taxService;

    public TaxController(TaxService taxService) {
        this.taxService = taxService;
    }

    @GetMapping("/estimate")
    @Operation(summary = "Estimated tax liability per holding (SHORT_TERM <1yr @30%, LONG_TERM ≥1yr @15%)")
    public List<TaxItemResponse> estimateTax() {
        return taxService.estimateTax();
    }
}

