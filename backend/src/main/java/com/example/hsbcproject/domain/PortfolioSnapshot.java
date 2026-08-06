package com.example.hsbcproject.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "portfolio_snapshots")
public class PortfolioSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "total_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalValue;

    @Column(name = "total_cost_basis", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalCostBasis;

    @Column(name = "total_gain_loss", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalGainLoss;

    @Column(name = "total_gain_loss_pct", nullable = false, precision = 10, scale = 4)
    private BigDecimal totalGainLossPct;

    @Column(name = "total_positions", nullable = false)
    private Long totalPositions;

    @Column(name = "total_quantity", precision = 19, scale = 8)
    private BigDecimal totalQuantity;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    public PortfolioSnapshot() {
        this.createdAt = LocalDate.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getSnapshotDate() {
        return snapshotDate;
    }

    public void setSnapshotDate(LocalDate snapshotDate) {
        this.snapshotDate = snapshotDate;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    public BigDecimal getTotalCostBasis() {
        return totalCostBasis;
    }

    public void setTotalCostBasis(BigDecimal totalCostBasis) {
        this.totalCostBasis = totalCostBasis;
    }

    public BigDecimal getTotalGainLoss() {
        return totalGainLoss;
    }

    public void setTotalGainLoss(BigDecimal totalGainLoss) {
        this.totalGainLoss = totalGainLoss;
    }

    public BigDecimal getTotalGainLossPct() {
        return totalGainLossPct;
    }

    public void setTotalGainLossPct(BigDecimal totalGainLossPct) {
        this.totalGainLossPct = totalGainLossPct;
    }

    public Long getTotalPositions() {
        return totalPositions;
    }

    public void setTotalPositions(Long totalPositions) {
        this.totalPositions = totalPositions;
    }

    public BigDecimal getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(BigDecimal totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }
}

