import { api, getApiErrorMessage } from './apiClient'

export { getApiErrorMessage }

export const MARKET_ASSET_TYPES = ['STOCK', 'BOND', 'CRYPTO']
export const MARKET_TIMEFRAMES = ['DAILY', 'WEEKLY', 'MONTHLY']
export const DEFAULT_MARKET_ASSET_TYPE = import.meta.env.VITE_MARKET_DEFAULT_ASSET_TYPE || 'STOCK'
export const DEFAULT_MARKET_TIMEFRAME = import.meta.env.VITE_MARKET_DEFAULT_TIMEFRAME || 'MONTHLY'
export const MARKET_SUGGESTION_LIMIT = Number(import.meta.env.VITE_MARKET_SUGGESTION_LIMIT || 10)

const TIMEFRAME_TO_DAYS = {
  DAILY: 2,
  WEEKLY: 7,
  MONTHLY: 30,
}

function normalizeTicker(value) {
  return (value || '').trim().toUpperCase()
}

export async function searchMarketInstruments(assetType, query, limit = 10) {
  const { data } = await api.get('/prices/instruments', {
    params: {
      assetType,
      query: query?.trim() || undefined,
      limit,
    },
  })
  return Array.isArray(data) ? data : []
}

export async function getMarketAssetDetails({ ticker, assetType, name }) {
  const symbol = normalizeTicker(ticker)
  if (!symbol) {
    throw new Error('Please enter a valid ticker symbol.')
  }

  const quoteReq = api.get(`/prices/${encodeURIComponent(symbol)}`, { params: { assetType } })
  const historyReq = api.get(`/prices/history/${encodeURIComponent(symbol)}`, { params: { assetType, days: 2 } })
  const stockReq = assetType === 'STOCK'
    ? api.get(`/stocks/${encodeURIComponent(symbol)}/price`)
    : Promise.resolve({ data: null })

  const [quoteRes, historyRes, stockRes] = await Promise.all([quoteReq, historyReq, stockReq])

  const quote = quoteRes.data
  const history = historyRes.data?.candles || []
  const stockData = stockRes.data

  const previousClose = history.length >= 2 ? Number(history[history.length - 2].close) : null
  const currentPrice = stockData?.currentPrice ?? quote?.currentPrice ?? null
  const resolvedName = stockData?.companyName || name || symbol

  if (currentPrice == null) {
    throw new Error(quote?.errorMessage || `Price data is unavailable for ${symbol}.`)
  }

  return {
    assetName: resolvedName,
    ticker: symbol,
    assetType,
    currentPrice: Number(currentPrice),
    previousClose,
    dailyChange: quote?.change != null ? Number(quote.change) : null,
    dailyChangePercent: quote?.changePercent != null ? Number(quote.changePercent) : null,
    currency: 'USD',
    lastUpdated: new Date().toISOString(),
  }
}

export async function getMarketPriceHistory(assetType, ticker, timeframe) {
  const symbol = normalizeTicker(ticker)
  const normalizedTimeframe = MARKET_TIMEFRAMES.includes(timeframe) ? timeframe : 'MONTHLY'
  const days = TIMEFRAME_TO_DAYS[normalizedTimeframe]

  const { data } = await api.get(`/prices/history/${encodeURIComponent(symbol)}`, {
    params: {
      assetType,
      days,
    },
  })

  const candles = Array.isArray(data?.candles) ? data.candles : []
  const points = candles.map((candle) => ({
    date: candle.date,
    close: Number(candle.close),
  }))

  const first = points[0]?.close
  const last = points[points.length - 1]?.close
  const periodChange = first != null && last != null ? last - first : null
  const periodChangePercent = first ? (periodChange / first) * 100 : null

  return {
    points,
    timeframe: normalizedTimeframe,
    source: data?.source || 'UNKNOWN',
    periodChange,
    periodChangePercent,
    isLimitedToMonth: assetType !== 'STOCK',
  }
}


