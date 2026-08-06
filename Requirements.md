# Functional Requirements — Portfolio Tracker

## Purpose

This document defines the functional requirements for the Portfolio Tracker application. Each requirement is uniquely identified and mapped to one or more user stories.

---

# Holdings Management

### FR-HOLD-01 — Add Holding
The system shall allow an investor to create a portfolio holding by entering:
- Ticker
- Asset type
- Quantity
- Purchase price
- Purchase date

The ticker shall automatically be stored in uppercase.

---

### FR-HOLD-02 — View Holdings
The system shall display all holdings in a table containing:
- Ticker
- Asset Type
- Quantity
- Purchase Price
- Purchase Date
- Cost Basis

---

### FR-HOLD-03 — Edit Holding
The system shall allow an investor to edit any existing holding.

Changes shall be reflected immediately after a successful update.

---

### FR-HOLD-04 — Delete Holding
The system shall allow an investor to delete a holding only after confirmation.

---

### FR-HOLD-05 — Live Holding Metrics
The system shall display for every holding:
- Current market price
- 24-hour change
- Current value
- Unrealized Profit/Loss

The system shall retrieve prices from the external pricing service and use fallback values if unavailable.

---

# Transaction History

### FR-TXN-01 — Record Transactions
The system shall allow investors to record BUY and SELL transactions including:
- Ticker
- Transaction Type
- Quantity
- Price
- Date
- Notes

---

### FR-TXN-02 — View & Filter Transactions
The system shall:
- Display all transactions
- Show BUY/SELL badges
- Support filtering by ticker

---

### FR-TXN-03 — Live Transaction Comparison
The system shall display:
- Current market price
- Difference from transaction price

for every transaction.

---

### FR-TXN-04 — Delete Transaction
The system shall allow confirmed deletion of transaction records.

---

# Dividend Tracking

### FR-DIV-01 — Record Dividend
The system shall allow recording dividend payments including:
- Ticker
- Per-share amount
- Shares
- Date

---

### FR-DIV-02 — View & Filter Dividends
The system shall:
- Display dividend history
- Support filtering by ticker
- Display visible total

---

### FR-DIV-03 — Portfolio Dividend Total
The system shall calculate and display cumulative dividends across the portfolio.

---

### FR-DIV-04 — Delete Dividend
The system shall support confirmed deletion of dividend records.

---

# Watchlist

### FR-WATCH-01 — Add Watchlist Item
The system shall:
- Search companies by name
- Autocomplete ticker symbols
- Automatically populate ticker and asset type
- Add selected items to the watchlist

---

### FR-WATCH-02 — View Watchlist
The system shall display watchlist items as cards containing:
- Ticker
- Current Price
- Daily Change

An empty-state view shall be shown when no items exist.

---

### FR-WATCH-03 — Edit Watchlist Item
The system shall allow updating watchlist entries.

---

### FR-WATCH-04 — Delete Watchlist Item
The system shall support confirmed deletion of watchlist items.

---

# Performance Analytics

### FR-PERF-01 — Portfolio Performance
The system shall calculate:
- Unrealized Gain/Loss
- Return Percentage
- Holding Duration

using live prices with fallback support.

---

# Risk Analytics

### FR-RISK-01 — Risk Analysis
The system shall calculate:
- Portfolio concentration
- Diversification score
- Risk level

---

# Tax Estimation

### FR-TAX-01 — Tax Estimation
The system shall:
- Classify holdings as Short-Term or Long-Term
- Estimate tax only on positive gains
- Apply configured tax percentages

---

# Dashboard

### FR-DASH-01 — Portfolio Dashboard
The system shall display:
- Total Positions
- Total Quantity
- Total Cost Basis
- Estimated Portfolio Value
- Overall Profit/Loss

---

### FR-DASH-02 — Asset Type Filtering
The dashboard shall support filtering by:
- Stock
- Bond
- Crypto
- All Assets

---

### FR-DASH-03 — Asset Breakdown
The dashboard shall display grouped asset summaries by asset type.

---

# Portfolio Overview

### FR-OVW-01 — Summary Cards
The overview page shall display:
- Total Portfolio Value
- Total Investment
- Stock Value
- Bond Value
- Crypto Value

---

### FR-OVW-02 — Allocation Charts
The overview page shall display:
- Asset Allocation Pie Chart
- Sector Allocation Pie Chart

---

### FR-OVW-03 — Performance Chart
The overview page shall display a portfolio trend chart supporting:
- Daily
- Weekly
- Monthly
- Yearly
- All-Time

time ranges.

---

### FR-OVW-04 — Live Overview
The overview page shall retrieve all displayed data from backend services instead of mock data.

---

# Live Price Integration

### FR-PRICE-01 — Live Price Retrieval
The system shall retrieve current market prices from an external pricing service.

---

### FR-PRICE-02 — Manual Refresh
Users shall be able to manually refresh live prices without reloading the page.

---

### FR-PRICE-03 — Fallback Pricing
If live pricing is unavailable, the system shall use stored purchase prices instead of failing.

---

# Navigation

### FR-NAV-01 — Sidebar Navigation
The application shall provide:
- Responsive sidebar
- Active page highlighting

---

### FR-NAV-02 — Top Navigation
The application shall provide:
- Search bar
- Profile access

---

### FR-NAV-03 — Application Routing
The application shall support routing for:
- Dashboard
- Holdings
- Transactions
- Dividends
- Watchlist
- Tax
- Profile

---

# Profile

### FR-PROF-01 — System Information
The profile page shall display:
- Backend URL
- Database Type
- API Documentation Link
- Technology Stack

---

# API Quality

### FR-API-01 — Input Validation
The system shall validate all incoming requests and return field-level validation errors.

---

### FR-API-02 — Global Error Handling
The system shall return standardized API error responses for all failures.

---

### FR-API-03 — API Documentation
The system shall expose Swagger/OpenAPI documentation.

---

# Platform Configuration (Roadmap)

### FR-CFG-01 — Configurable API Base URL
The frontend shall use an environment variable for the backend API URL.

---

# User Experience (Roadmap)

### FR-UX-01 — Toast Notifications
The application shall display toast notifications for successful and failed actions.

---

# Infrastructure (Roadmap)

### FR-INFRA-01 — MySQL Support
The backend shall support a MySQL production profile.

---

# Security (Roadmap)

### FR-SEC-01 — User Authentication
The system shall support secure user login.

---

### FR-SEC-02 — Password Hashing
Passwords shall be securely hashed before storage.

---

### FR-SEC-03 — Protected APIs
Authenticated users shall be required for protected endpoints.

---

### FR-SEC-04 — User Data Isolation
Each user shall only access their own portfolio data.

---

### FR-SEC-05 — Restricted CORS
Only approved frontend origins shall access the API in production.

---

### FR-SEC-06 — Dependency & Input Security
Security scanning shall execute during CI.

---

### FR-SEC-07 — Rate Limiting
The system shall throttle excessive requests to configured endpoints.

---

# Testing (Roadmap)

### FR-TEST-01 — Unit Testing
Core business services shall have automated unit tests.

---

### FR-TEST-02 — Integration Testing
All controllers shall have integration tests.

---

### FR-TEST-03 — Frontend Component Testing
Core pages shall have component tests.

---

### FR-TEST-04 — End-to-End Testing
Critical user journeys shall be covered by automated E2E tests.

---

### FR-TEST-05 — Continuous Integration Testing
All test suites shall execute automatically for every pull request before merge.