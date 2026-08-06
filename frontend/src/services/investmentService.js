import { api, getApiErrorMessage } from './apiClient'
import { tickerCatalog } from '../data/tickerCatalog'

export { getApiErrorMessage }

export const INVESTMENT_TYPES = ['STOCK', 'BOND', 'CRYPTO']

/**
 * Local instrument catalog lookup used for search/autocomplete while typing.
 * Kept in the service layer so components stay free of data-source details.
 */
export function searchInstruments(assetType, query, limit = 8) {
  const normalizedQuery = query.trim().toLowerCase()
  const matches = tickerCatalog.filter((item) => item.assetType === assetType)

  if (!normalizedQuery) {
    return matches.slice(0, limit)
  }

  return matches
    .filter((item) => `${item.ticker} ${item.name}`.toLowerCase().includes(normalizedQuery))
    .slice(0, limit)
}

function toCreatePayload(form) {
  return {
    ticker: form.ticker?.trim().toUpperCase(),
    quantity: Number(form.quantity),
    assetType: form.assetType,
    purchasePrice: Number(form.purchasePrice),
    purchaseDate: form.purchaseDate,
    name: form.name?.trim() || null,
    sector: form.assetType === 'STOCK' ? form.sector?.trim() || null : null,
    issuer: form.assetType === 'BOND' ? form.issuer?.trim() || null : null,
    interestRate: form.assetType === 'BOND' && form.interestRate !== '' && form.interestRate != null
      ? Number(form.interestRate)
      : null,
    maturityDate: form.assetType === 'BOND' ? form.maturityDate || null : null,
  }
}

export async function createInvestment(form) {
  const payload = toCreatePayload(form)
  return (await api.post('/portfolio-items', payload)).data
}

/**
 * Fetches company name, sector and live price for a stock ticker from the backend
 * (which in turn talks to the market data provider - never called directly from the UI).
 */
export async function getStockQuote(ticker) {
  return (await api.get(`/stocks/${encodeURIComponent(ticker.trim().toUpperCase())}/price`)).data
}

export async function getInvestments() {
  return (await api.get('/portfolio-items')).data
}

export async function getInvestmentSummary() {
  return (await api.get('/portfolio-items/summary')).data
}

export async function getDashboardSnapshot() {
  return (await api.get('/dashboard')).data
}
