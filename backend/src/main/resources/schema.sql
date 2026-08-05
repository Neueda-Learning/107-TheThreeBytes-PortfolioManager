USE portfolio_db;

-- ============================================================
-- 2. portfolio_items
--    Represents a holding in the user's portfolio.
--    Mirrors com.example.hsbcproject.domain.PortfolioItem
-- ============================================================
CREATE TABLE IF NOT EXISTS portfolio_items (
    id             BIGINT          NOT NULL AUTO_INCREMENT,
    ticker         VARCHAR(10)     NOT NULL,
    quantity       INT             NOT NULL,
    asset_type     ENUM('STOCK','BOND','CRYPTO') NOT NULL,
    purchase_price DECIMAL(15,4)   NOT NULL,
    purchase_date  DATE            NOT NULL,
    name           VARCHAR(200)    NULL,
    sector         VARCHAR(100)    NULL,
    issuer         VARCHAR(200)    NULL,
    interest_rate  DECIMAL(6,3)    NULL,
    maturity_date  DATE            NULL,

    PRIMARY KEY (id),
    INDEX idx_pi_ticker (ticker),
    INDEX idx_pi_asset_type (asset_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 3. transactions
--    Records every BUY / SELL event.
--    Mirrors com.example.hsbcproject.domain.Transaction
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
--    Mirrors com.example.hsbcproject.domain.DividendRecord
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
--    Mirrors com.example.hsbcproject.domain.WatchlistItem
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
--    Mirrors com.example.hsbcproject.domain.PortfolioSnapshot
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
