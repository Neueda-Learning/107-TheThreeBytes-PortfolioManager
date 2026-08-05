package com.example.hsbcproject.repository;

import com.example.hsbcproject.domain.PortfolioItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioItemRepository extends JpaRepository<PortfolioItem, Long> {
}

