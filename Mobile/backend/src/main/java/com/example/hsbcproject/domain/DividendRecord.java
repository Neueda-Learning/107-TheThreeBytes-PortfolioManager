package com.example.hsbcproject.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "dividend_records")
public class DividendRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticker", nullable = false, length = 10)
    private String ticker;

    @Column(name = "dividend_per_share", nullable = false, precision = 15, scale = 4)
    private BigDecimal dividendPerShare;

    @Column(name = "shares_held", nullable = false)
    private Integer sharesHeld;

    @Column(name = "total_dividend", nullable = false, precision = 15, scale = 4)
    private BigDecimal totalDividend;

    @Column(name = "dividend_date", nullable = false)
    private LocalDate dividendDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }
    public BigDecimal getDividendPerShare() { return dividendPerShare; }
    public void setDividendPerShare(BigDecimal dividendPerShare) { this.dividendPerShare = dividendPerShare; }
    public Integer getSharesHeld() { return sharesHeld; }
    public void setSharesHeld(Integer sharesHeld) { this.sharesHeld = sharesHeld; }
    public BigDecimal getTotalDividend() { return totalDividend; }
    public void setTotalDividend(BigDecimal totalDividend) { this.totalDividend = totalDividend; }
    public LocalDate getDividendDate() { return dividendDate; }
    public void setDividendDate(LocalDate dividendDate) { this.dividendDate = dividendDate; }
}

