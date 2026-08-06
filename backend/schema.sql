-- ============================================================
--  HSBC Portfolio Project – MySQL Schema
--  Database: portfolio_db
--  Run this in MySQL Workbench (File > Open SQL Script, then ⚡)
-- ============================================================

-- 1. Create & select the database
CREATE DATABASE IF NOT EXISTS portfolio_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE portfolio_db;

-- ============================================================
-- 2. portfolio_items
--    Represents a holding in the user's portfolio.
-- ============================================================
CREATE TABLE IF NOT EXISTS portfolio_items (
    id            BIGINT          NOT NULL AUTO_INCREMENT,
    ticker        VARCHAR(10)     NOT NULL,
    quantity      INT             NOT NULL,
    asset_type    ENUM('STOCK','BOND','CRYPTO') NOT NULL,
    purchase_price DECIMAL(15,4)  NOT NULL,
    purchase_date  DATE           NOT NULL,
    name           VARCHAR(200)   NULL,
    sector         VARCHAR(100)   NULL,
    issuer         VARCHAR(200)   NULL,
    interest_rate  DECIMAL(6,3)   NULL,
    maturity_date  DATE           NULL,

    PRIMARY KEY (id),
    INDEX idx_pi_ticker (ticker),
    INDEX idx_pi_asset_type (asset_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 3. transactions
--    Records every BUY / SELL event.
-- ============================================================
CREATE TABLE IF NOT EXISTS transactions (
    id               BIGINT         NOT NULL AUTO_INCREMENT,
    ticker           VARCHAR(10)    NOT NULL,
    asset_type       ENUM('STOCK','BOND','CRYPTO') NOT NULL,
    transaction_type ENUM('BUY','SELL')            NOT NULL,
    quantity         INT            NOT NULL,
    price_per_unit   DECIMAL(15,4)  NOT NULL,
    transaction_date DATE           NOT NULL,
    notes            VARCHAR(500)   NULL,

    PRIMARY KEY (id),
    INDEX idx_tx_ticker (ticker),
    INDEX idx_tx_date   (transaction_date),
    INDEX idx_tx_type   (transaction_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 4. dividend_records
--    Tracks dividend payments received per ticker.
-- ============================================================
CREATE TABLE IF NOT EXISTS dividend_records (
    id                 BIGINT        NOT NULL AUTO_INCREMENT,
    ticker             VARCHAR(10)   NOT NULL,
    dividend_per_share DECIMAL(15,4) NOT NULL,
    shares_held        INT           NOT NULL,
    total_dividend     DECIMAL(15,4) NOT NULL,
    dividend_date      DATE          NOT NULL,

    PRIMARY KEY (id),
    INDEX idx_div_ticker (ticker),
    INDEX idx_div_date   (dividend_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 5. watchlist_items
--    Tickers the user is monitoring (not yet in portfolio).
-- ============================================================
CREATE TABLE IF NOT EXISTS watchlist_items (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    ticker     VARCHAR(10) NOT NULL,
    asset_type ENUM('STOCK','BOND','CRYPTO') NOT NULL,
    added_date DATE        NOT NULL,

    PRIMARY KEY (id),
    UNIQUE INDEX uq_watchlist_ticker (ticker),
    INDEX idx_wl_asset_type (asset_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 6. portfolio_snapshots
--    Daily point-in-time portfolio value snapshots.
-- ============================================================
CREATE TABLE IF NOT EXISTS portfolio_snapshots (
    id                  BIGINT         NOT NULL AUTO_INCREMENT,
    snapshot_date       DATE           NOT NULL,
    total_value         DECIMAL(15,2)  NOT NULL,
    total_cost_basis    DECIMAL(15,2)  NOT NULL,
    total_gain_loss     DECIMAL(15,2)  NOT NULL,
    total_gain_loss_pct DECIMAL(10,4)  NOT NULL,
    total_positions     BIGINT         NOT NULL,
    total_quantity      BIGINT         NOT NULL,
    created_at          DATE           NOT NULL,

    PRIMARY KEY (id),
    UNIQUE INDEX uq_snapshot_date (snapshot_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 7. (Optional) Sample seed data – comment out if not needed
-- ============================================================

-- Portfolio holdings
INSERT INTO portfolio_items (ticker, quantity, asset_type, purchase_price, purchase_date) VALUES
('AAPL',  50, 'STOCK', 150.0000, '2024-01-15'),
('MSFT',  30, 'STOCK', 310.5000, '2024-02-10'),
('BTC',    2, 'CRYPTO', 42000.0000, '2024-03-01'),
('AGG',  100, 'BOND',   98.7500, '2024-01-20');

-- Transaction history
INSERT INTO transactions (ticker, asset_type, transaction_type, quantity, price_per_unit, transaction_date, notes) VALUES
('AAPL', 'STOCK',  'BUY',  50, 150.0000, '2024-01-15', 'Initial purchase'),
('MSFT', 'STOCK',  'BUY',  30, 310.5000, '2024-02-10', 'Added to portfolio'),
('BTC',  'CRYPTO', 'BUY',   2, 42000.0000, '2024-03-01', 'Crypto allocation'),
('AGG',  'BOND',   'BUY', 100,  98.7500, '2024-01-20', 'Bond allocation'),
('AAPL', 'STOCK',  'SELL', 10, 175.0000, '2024-06-01', 'Partial profit taking');

-- Dividend records
INSERT INTO dividend_records (ticker, dividend_per_share, shares_held, total_dividend, dividend_date) VALUES
('AAPL', 0.2400, 50,  12.00, '2024-02-15'),
('MSFT', 0.7500, 30,  22.50, '2024-03-13'),
('AGG',  0.2100, 100, 21.00, '2024-04-01');

-- Watchlist
INSERT INTO watchlist_items (ticker, asset_type, added_date) VALUES
('GOOGL', 'STOCK',  '2024-05-01'),
('AMZN',  'STOCK',  '2024-05-15'),
('ETH',   'CRYPTO', '2024-06-01');

