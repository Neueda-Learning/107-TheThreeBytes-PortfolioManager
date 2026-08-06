// Holdings here are real backend PortfolioItemResponse objects, optionally enriched
// with a `currentPrice` looked up from the live price service.
export function getHoldingsMetrics(holdings) {
  const totalInvested = holdings.reduce((sum, item) => sum + item.purchasePrice * item.quantity, 0)
  const totalValue = holdings.reduce((sum, item) => sum + (item.currentPrice ?? item.purchasePrice) * item.quantity, 0)
  const totalGain = totalValue - totalInvested
  const gainPct = totalInvested > 0 ? (totalGain / totalInvested) * 100 : 0

  const byAssetType = holdings.reduce((acc, item) => {
    const key = item.assetType.toLowerCase()
    acc[key] = (acc[key] || 0) + (item.currentPrice ?? item.purchasePrice) * item.quantity
    return acc
  }, {})

  const bySector = holdings.reduce((acc, item) => {
    const key = item.sector || 'Uncategorized'
    acc[key] = (acc[key] || 0) + (item.currentPrice ?? item.purchasePrice) * item.quantity
    return acc
  }, {})

  return {
    totalInvested,
    totalValue,
    totalGain,
    gainPct,
    byAssetType,
    bySector,
  }
}

// Real cumulative invested-capital trend derived from each holding's purchase date/price,
// optionally filtered to a single asset type. No synthetic data points are generated.
export function buildCumulativeInvestmentSeries(holdings, assetType = 'ALL') {
  const filtered = assetType === 'ALL' ? holdings : holdings.filter((item) => item.assetType === assetType)
  if (!filtered.length) return []

  const sorted = [...filtered].sort((a, b) => new Date(a.purchaseDate) - new Date(b.purchaseDate))
  let running = 0
  return sorted.map((item) => {
    running += Number(item.purchasePrice) * Number(item.quantity)
    return { name: item.purchaseDate, value: Math.round(running * 100) / 100 }
  })
}

