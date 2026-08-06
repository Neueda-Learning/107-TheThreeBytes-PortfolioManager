package com.example.hsbcproject.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.hsbcproject.domain.PortfolioSnapshot;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PortfolioSnapshotRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private PortfolioSnapshotRepository repository;

    private PortfolioSnapshot snapshot(LocalDate date, BigDecimal value) {
        PortfolioSnapshot snapshot = new PortfolioSnapshot();
        snapshot.setSnapshotDate(date);
        snapshot.setTotalValue(value);
        snapshot.setTotalCostBasis(new BigDecimal("1000.00"));
        snapshot.setTotalGainLoss(value.subtract(new BigDecimal("1000.00")));
        snapshot.setTotalGainLossPct(new BigDecimal("10.00"));
        snapshot.setTotalPositions(2L);
        snapshot.setTotalQuantity(new BigDecimal("15.00000000"));
        snapshot.setCreatedAt(date);
        return snapshot;
    }

    @Test
    void findBySnapshotDateReturnsSavedSnapshot() {
        LocalDate date = LocalDate.now();
        repository.save(snapshot(date, new BigDecimal("1200.00")));

        assertTrue(repository.findBySnapshotDate(date).isPresent());
    }

    @Test
    void findBySnapshotDateBetweenOrderBySnapshotDateAscReturnsOrderedResults() {
        LocalDate today = LocalDate.now();
        repository.save(snapshot(today.minusDays(2), new BigDecimal("1100.00")));
        repository.save(snapshot(today.minusDays(1), new BigDecimal("1150.00")));
        repository.save(snapshot(today, new BigDecimal("1200.00")));

        List<PortfolioSnapshot> results = repository.findBySnapshotDateBetweenOrderBySnapshotDateAsc(today.minusDays(2), today);

        assertEquals(3, results.size());
        assertEquals(today.minusDays(2), results.get(0).getSnapshotDate());
        assertEquals(today, results.get(2).getSnapshotDate());
    }

    @Test
    void findSnapshotsSinceReturnsOnlyRecentSnapshots() {
        LocalDate today = LocalDate.now();
        repository.save(snapshot(today.minusDays(5), new BigDecimal("1000.00")));
        repository.save(snapshot(today.minusDays(1), new BigDecimal("1200.00")));

        List<PortfolioSnapshot> results = repository.findSnapshotsSince(today.minusDays(2));

        assertEquals(1, results.size());
        assertEquals(today.minusDays(1), results.get(0).getSnapshotDate());
    }

    @Test
    void findAllOrderByDateDescReturnsDescendingOrder() {
        LocalDate today = LocalDate.now();
        repository.save(snapshot(today.minusDays(2), new BigDecimal("1000.00")));
        repository.save(snapshot(today, new BigDecimal("1200.00")));

        List<PortfolioSnapshot> results = repository.findAllOrderByDateDesc();

        assertFalse(results.isEmpty());
        assertEquals(today, results.get(0).getSnapshotDate());
    }

    @Test
    void deleteBySnapshotDateBeforeRemovesOlderSnapshots() {
        LocalDate today = LocalDate.now();
        repository.save(snapshot(today.minusDays(10), new BigDecimal("900.00")));
        repository.save(snapshot(today.minusDays(1), new BigDecimal("1200.00")));

        repository.deleteBySnapshotDateBefore(today.minusDays(5));

        assertTrue(repository.findBySnapshotDate(today.minusDays(10)).isEmpty());
        assertTrue(repository.findBySnapshotDate(today.minusDays(1)).isPresent());
    }
}

