import { useEffect, useMemo, useState } from 'react'
import { BriefcaseBusiness, Coins, Landmark, Sparkles } from 'lucide-react'
import SummaryCard from '../components/SummaryCard'
import AllocationPieChart from '../components/AllocationPieChart'
import PerformanceChart from '../components/PerformanceChart'
import HoldingsTable from '../components/HoldingsTable'
import InfoTooltip from '../components/InfoTooltip'
import { getApiErrorMessage, getInvestments } from '../services/investmentService'
import { getPrice } from '../services/portfolioService'
import { getHoldingsMetrics } from '../utils/portfolioUtils'

function createSummaryCards(metrics) {
  return [
    {
      title: 'Total Portfolio Value',
      value: formatCurrencyValue(metrics.totalValue),
      change: metrics.gainPct,
      icon: BriefcaseBusiness,
      info: {
        title: 'Total portfolio value',
        description: 'The current combined market value of all your investments.',
      },
    },
    {
      title: 'Total Invested',
      value: formatCurrencyValue(metrics.totalInvested),
      change: undefined,
      icon: Landmark,
      info: {
        title: 'Total invested',
        description: 'The total amount you have put into your portfolio so far.',
      },
    },
    {
      title: 'Total Stock Investment',
      value: formatCurrencyValue(metrics.byAssetType.stock || 0),
      change: undefined,
      icon: Sparkles,
      info: {
        title: 'Stock allocation',
        description: 'How much of your money is currently allocated to stocks.',
      },
    },
    {
      title: 'Total Bond Investment',
      value: formatCurrencyValue(metrics.byAssetType.bond || 0),
      change: undefined,
      icon: Landmark,
      info: {
        title: 'Bond allocation',
        description: 'How much of your money is currently allocated to bonds.',
      },
    },
    {
      title: 'Total Crypto Investment',
      value: formatCurrencyValue(metrics.byAssetType.crypto || 0),
      change: undefined,
      icon: Coins,
      info: {
        title: 'Crypto allocation',
        description: 'How much of your money is currently allocated to crypto assets.',
      },
    },
  ]
}

function formatCurrencyValue(value) {
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD', maximumFractionDigits: 2 }).format(value || 0)
}

function createAllocationData(holdings) {
  const metrics = getHoldingsMetrics(holdings)
  const sectors = Object.entries(metrics.bySector).map(([name, value]) => ({ name, value }))
  const assetTypes = Object.entries(metrics.byAssetType).map(([name, value]) => ({ name: name.charAt(0).toUpperCase() + name.slice(1), value }))

  return { sectors, assetTypes }
}

function createHoldingsTableData(holdings) {
  return holdings.map((item) => {
    const currentPrice = item.currentPrice ?? item.purchasePrice
    const totalValue = currentPrice * item.quantity
    const costBasis = item.purchasePrice * item.quantity
    return {
      ...item,
      currentPrice,
      totalValue,
      profitLoss: totalValue - costBasis,
      profitLossPct: costBasis > 0 ? ((currentPrice - item.purchasePrice) / item.purchasePrice) * 100 : 0,
    }
  })
}

export default function Overview({ searchQuery: searchQueryProp, refreshToken }) {
  const [holdings, setHoldings] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const searchQuery = searchQueryProp || ''

  const load = async () => {
    try {
      setLoading(true)
      setError(null)
      const items = await getInvestments()

      const tickers = [...new Set(items.map((item) => item.ticker))]
      const priceResults = await Promise.allSettled(tickers.map((ticker) => getPrice(ticker)))
      const pricesByTicker = {}
      tickers.forEach((ticker, index) => {
        const result = priceResults[index]
        if (result.status === 'fulfilled' && result.value?.currentPrice != null) {
          pricesByTicker[ticker] = Number(result.value.currentPrice)
        }
      })

      setHoldings(items.map((item) => ({ ...item, currentPrice: pricesByTicker[item.ticker] ?? item.purchasePrice })))
    } catch (err) {
      setError(getApiErrorMessage(err, 'We could not load your portfolio right now.'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [refreshToken])

  const metrics = useMemo(() => getHoldingsMetrics(holdings), [holdings])
  const summaryCards = useMemo(() => createSummaryCards(metrics), [metrics])
  const allocations = useMemo(() => createAllocationData(holdings), [holdings])
  const holdingsTableData = useMemo(() => createHoldingsTableData(holdings), [holdings])

  const filteredHoldings = useMemo(() => {
    if (!searchQuery.trim()) return holdingsTableData
    const query = searchQuery.toLowerCase()
    return holdingsTableData.filter((item) => `${item.ticker} ${item.name || ''} ${item.assetType}`.toLowerCase().includes(query))
  }, [holdingsTableData, searchQuery])

  return (
    <div className="space-y-6">
      {loading ? <p role="status" aria-live="polite" className="py-6 text-center text-sm text-slate-500">Loading portfolio...</p> : null}
      {error ? (
        <div className="py-6 text-center">
          <p role="alert" className="text-sm text-rose-500">{error}</p>
          <button
            type="button"
            onClick={load}
            className="mt-3 rounded-2xl border border-slate-200 bg-white px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-100"
          >
            Try again
          </button>
        </div>
      ) : null}

      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-5">
        {summaryCards.map((card) => (
          <SummaryCard key={card.title} {...card} />
        ))}
      </div>

      <div className="grid gap-6 xl:grid-cols-2">
        <AllocationPieChart
          title="Portfolio Allocation by Sector"
          data={allocations.sectors}
          info={{
            title: 'Sector allocation',
            description: 'Shows how your portfolio is distributed across sectors. It helps you spot concentration risk.',
          }}
        />
        <AllocationPieChart
          title="Portfolio Allocation by Asset Type"
          data={allocations.assetTypes}
          info={{
            title: 'Asset type allocation',
            description: 'Shows the split between stocks, bonds, and crypto so you can balance risk and stability.',
          }}
        />
      </div>

      <PerformanceChart />

      <div className="rounded-[24px] border border-slate-200 bg-white p-5 shadow-[0_10px_30px_rgba(15,23,42,0.05)]">
        <div className="mb-4 flex items-center justify-between">
          <div>
            <div className="flex items-center gap-1.5">
              <h3 className="text-lg font-semibold text-slate-900">Portfolio Holdings</h3>
              <InfoTooltip
                title="Portfolio holdings"
                description="This table lists all investments you currently own with estimated live pricing and profit/loss."
              />
            </div>
            <p className="text-sm text-slate-500">Live holdings from your portfolio, priced with the backend price service.</p>
          </div>
          <div className="rounded-full bg-slate-100 px-3 py-1 text-sm font-medium text-slate-600">
            {holdings.length} positions
          </div>
        </div>
        <HoldingsTable holdings={filteredHoldings} />
      </div>
    </div>
  )
}
