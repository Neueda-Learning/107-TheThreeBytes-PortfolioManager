package com.example.hsbcproject.service;

import com.example.hsbcproject.domain.DividendRecord;
import com.example.hsbcproject.dto.CreateDividendRequest;
import com.example.hsbcproject.dto.DividendResponse;
import com.example.hsbcproject.exception.ResourceNotFoundException;
import com.example.hsbcproject.repository.DividendRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DividendService {

    private final DividendRepository dividendRepository;

    public DividendService(DividendRepository dividendRepository) {
        this.dividendRepository = dividendRepository;
    }

    @Transactional(readOnly = true)
    public List<DividendResponse> findAll() {
        return dividendRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public DividendResponse findById(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    public List<DividendResponse> findByTicker(String ticker) {
        return dividendRepository.findByTickerIgnoreCase(ticker).stream().map(this::toResponse).toList();
    }

    public DividendResponse create(CreateDividendRequest request) {
        DividendRecord record = new DividendRecord();
        record.setTicker(request.ticker().toUpperCase());
        record.setDividendPerShare(request.dividendPerShare());
        record.setSharesHeld(request.sharesHeld());
        record.setTotalDividend(request.dividendPerShare().multiply(BigDecimal.valueOf(request.sharesHeld())));
        record.setDividendDate(request.dividendDate());
        return toResponse(dividendRepository.save(record));
    }

    public void delete(Long id) {
        DividendRecord record = getEntity(id);
        dividendRepository.delete(record);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalDividendsReceived() {
        return dividendRepository.findAll().stream()
                .map(DividendRecord::getTotalDividend)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private DividendRecord getEntity(Long id) {
        return dividendRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dividend record with id " + id + " not found"));
    }

    private DividendResponse toResponse(DividendRecord record) {
        return new DividendResponse(record.getId(), record.getTicker(),
                record.getDividendPerShare(), record.getSharesHeld(),
                record.getTotalDividend(), record.getDividendDate());
    }
}

