package com.example.hsbcproject.repository;

import com.example.hsbcproject.domain.PortfolioSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioSnapshotRepository extends JpaRepository<PortfolioSnapshot, Long> {

    Optional<PortfolioSnapshot> findBySnapshotDate(LocalDate snapshotDate);

    List<PortfolioSnapshot> findBySnapshotDateBetweenOrderBySnapshotDateAsc(LocalDate startDate, LocalDate endDate);

    @Query("SELECT p FROM PortfolioSnapshot p WHERE p.snapshotDate >= :startDate ORDER BY p.snapshotDate ASC")
    List<PortfolioSnapshot> findSnapshotsSince(@Param("startDate") LocalDate startDate);

    @Query("SELECT p FROM PortfolioSnapshot p ORDER BY p.snapshotDate DESC")
    List<PortfolioSnapshot> findAllOrderByDateDesc();

    void deleteBySnapshotDateBefore(LocalDate date);
}

