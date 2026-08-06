-- Portfolio Tracking Feature
-- Add portfolio_snapshots table for tracking portfolio value over time

CREATE TABLE IF NOT EXISTS portfolio_snapshots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    snapshot_date DATE NOT NULL UNIQUE,
    total_value DECIMAL(15, 2) NOT NULL,
    total_cost_basis DECIMAL(15, 2) NOT NULL,
    total_gain_loss DECIMAL(15, 2) NOT NULL,
    total_gain_loss_pct DECIMAL(10, 4) NOT NULL,
    total_positions BIGINT NOT NULL,
    total_quantity BIGINT NOT NULL,
    created_at DATE NOT NULL,
    INDEX idx_snapshot_date (snapshot_date),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add comment to table
ALTER TABLE portfolio_snapshots COMMENT = 'Stores daily snapshots of portfolio performance for tracking over time';

