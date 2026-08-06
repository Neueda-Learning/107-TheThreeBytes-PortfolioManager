import { api, getApiErrorMessage } from './apiClient'

export { getApiErrorMessage }

// Portfolio Items
export async function getPortfolioItems() {
  return (await api.get('/portfolio-items')).data
}
export async function getPortfolioSummary() {
  return (await api.get('/portfolio-items/summary')).data
}
export async function createPortfolioItem(data) {
  return (await api.post('/portfolio-items', data)).data
}
export async function updatePortfolioItem(id, data) {
  return (await api.put(`/portfolio-items/${id}`, data)).data
}
export async function deletePortfolioItem(id) {
  return api.delete(`/portfolio-items/${id}`)
}
export async function sellHolding(id, quantity, pricePerUnit) {
  return (
    await api.post(`/portfolio-items/${id}/sell`, {
      pricePerUnit,
      quantity,
    })
  ).data
}

// Transactions
export async function getTransactions(ticker) {
  const url = ticker ? `/transactions?ticker=${ticker}` : '/transactions'
  return (await api.get(url)).data
}
export async function createTransaction(data) {
  return (await api.post('/transactions', data)).data
}
export async function deleteTransaction(id) {
  return api.delete(`/transactions/${id}`)
}

// Dividends
export async function getDividends(ticker) {
  const url = ticker ? `/dividends?ticker=${ticker}` : '/dividends'
  return (await api.get(url)).data
}
export async function createDividend(data) {
  return (await api.post('/dividends', data)).data
}
export async function deleteDividend(id) {
  return api.delete(`/dividends/${id}`)
}
export async function getDividendTotal() {
  return (await api.get('/dividends/total')).data
}

// Watchlist
export async function getWatchlist() {
  return (await api.get('/watchlist')).data
}
export async function addToWatchlist(data) {
  return (await api.post('/watchlist', data)).data
}
export async function removeFromWatchlist(id) {
  return api.delete(`/watchlist/${id}`)
}

// Analytics
export async function getDashboard() {
  return (await api.get('/dashboard')).data
}
export async function getDashboardByAssetType(assetType) {
  return (await api.get(`/dashboard/${assetType}`)).data
}
export async function getPerformance() {
  return (await api.get('/performance')).data
}
export async function getRiskAnalysis() {
  return (await api.get('/risk/analysis')).data
}
export async function getTaxEstimate() {
  return (await api.get('/tax/estimate')).data
}

// Prices
export async function getPrice(ticker) {
  return (await api.get(`/prices/${ticker}`)).data
}

// Portfolio Tracking
export async function getPortfolioTracking(period) {
  return (await api.get(`/portfolio-tracking/${period.toLowerCase()}`)).data
}
export async function getAllPortfolioSnapshots() {
  return (await api.get('/portfolio-tracking/snapshots')).data
}
export async function createPortfolioSnapshot() {
  return (await api.post('/portfolio-tracking/snapshot')).data
}
