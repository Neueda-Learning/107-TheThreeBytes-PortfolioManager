import { useEffect, useMemo, useState } from 'react'
import { RefreshCw, Search } from 'lucide-react'
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts'
import AsyncState from '../components/AsyncState'
import PageCard from '../components/PageCard'
import SectionHeader from '../components/SectionHeader'
import {
  getApiErrorMessage,
  getMarketAssetDetails,
  getMarketPriceHistory,
  MARKET_ASSET_TYPES,
  MARKET_TIMEFRAMES,
  searchMarketInstruments,
} from '../services/marketService'
import { currency, formatPercent } from '../utils/formatters'

function formatDateLabel(dateValue, timeframe) {
  const date = new Date(`${dateValue}T00:00:00`)
  if (timeframe === 'DAILY') {
    return date.toLocaleDateString('en-US', { hour: '2-digit', minute: '2-digit' })
  }
  return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })
}

function ChartTooltipContent({ active, payload, label }) {
  if (!active || !payload?.length) return null
  return (
    <div className="rounded-xl border border-slate-200 bg-white p-3 shadow-lg">
      <p className="mb-1 text-xs text-slate-500">{label}</p>
      <p className="text-sm font-semibold text-slate-900">{currency.format(payload[0].value)}</p>
    </div>
  )
}

export default function MarketPrices() {
  const [assetType, setAssetType] = useState('STOCK')
  const [timeframe, setTimeframe] = useState('MONTHLY')
  const [query, setQuery] = useState('')
  const [selectedInstrument, setSelectedInstrument] = useState(null)

  const [suggestionsState, setSuggestionsState] = useState({ loading: false, error: '', data: [] })
  const [detailsState, setDetailsState] = useState({ loading: false, error: '', data: null })
  const [chartState, setChartState] = useState({ loading: false, error: '', data: null })

  useEffect(() => {
    setSelectedInstrument(null)
    setDetailsState({ loading: false, error: '', data: null })
    setChartState({ loading: false, error: '', data: null })
    setQuery('')
  }, [assetType])

  useEffect(() => {
    const timer = window.setTimeout(async () => {
      try {
        setSuggestionsState((prev) => ({ ...prev, loading: true, error: '' }))
        const data = await searchMarketInstruments(assetType, query, 10)
        setSuggestionsState({ loading: false, error: '', data })
      } catch (err) {
        setSuggestionsState({ loading: false, error: getApiErrorMessage(err, 'Could not load instruments.'), data: [] })
      }
    }, 200)

    return () => window.clearTimeout(timer)
  }, [assetType, query])

  const loadDetails = async (instrument) => {
    try {
      setDetailsState({ loading: true, error: '', data: null })
      const data = await getMarketAssetDetails(instrument)
      setDetailsState({ loading: false, error: '', data })
    } catch (err) {
      setDetailsState({ loading: false, error: getApiErrorMessage(err, 'Could not load asset details.'), data: null })
    }
  }

  const loadChart = async (instrument, activeTimeframe) => {
    try {
      setChartState({ loading: true, error: '', data: null })
      const data = await getMarketPriceHistory(instrument.assetType, instrument.ticker, activeTimeframe)
      setChartState({ loading: false, error: '', data })
    } catch (err) {
      setChartState({ loading: false, error: getApiErrorMessage(err, 'Could not load price history.'), data: null })
    }
  }

  useEffect(() => {
    if (!selectedInstrument) return
    loadDetails(selectedInstrument)
  }, [selectedInstrument])

  useEffect(() => {
    if (!selectedInstrument) return
    loadChart(selectedInstrument, timeframe)
  }, [selectedInstrument, timeframe])

  const handleSelectInstrument = (instrument) => {
    setSelectedInstrument(instrument)
    setQuery(instrument.name)
  }

  const handleSearchSubmit = async (event) => {
    event.preventDefault()
    const normalized = query.trim()
    if (!normalized) return

    const resolved = suggestionsState.data.find((item) => {
      const text = `${item.ticker} ${item.name}`.toLowerCase()
      return text.includes(normalized.toLowerCase())
    })

    const instrument = resolved || {
      ticker: normalized.toUpperCase(),
      name: normalized,
      assetType,
    }

    handleSelectInstrument(instrument)
  }

  const chartPoints = useMemo(() => {
    const points = chartState.data?.points || []
    return points.map((point) => ({
      label: formatDateLabel(point.date, timeframe),
      close: point.close,
    }))
  }, [chartState.data, timeframe])

  const details = detailsState.data
  const isPositiveDay = Number(details?.dailyChangePercent || 0) >= 0
  const isPositivePeriod = Number(chartState.data?.periodChangePercent || 0) >= 0

  return (
    <div className="space-y-6">
      <PageCard className="p-5 sm:p-6">
        <SectionHeader
          title="Market Prices"
          description="Search stocks, bonds, and crypto by name or ticker to view live/dummy pricing from backend services."
          info={{
            title: 'Market prices',
            description: 'Stocks use live market APIs. Bonds and crypto use backend dummy data with up to one month of history.',
          }}
        />

        <form className="grid gap-3 sm:grid-cols-[180px_1fr_auto]" onSubmit={handleSearchSubmit}>
          <select
            value={assetType}
            onChange={(event) => setAssetType(event.target.value)}
            className="rounded-2xl border border-slate-200 bg-white px-3 py-2 text-sm font-medium text-slate-700"
          >
            {MARKET_ASSET_TYPES.map((type) => (
              <option key={type} value={type}>{type}</option>
            ))}
          </select>

          <label className="relative">
            <Search size={14} className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
            <input
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              placeholder={assetType === 'CRYPTO' ? 'e.g. Bitcoin or BTC' : 'e.g. Apple or AAPL'}
              className="w-full rounded-2xl border border-slate-200 py-2 pl-9 pr-3 text-sm"
            />
          </label>

          <button
            type="submit"
            className="rounded-2xl bg-slate-900 px-4 py-2 text-sm font-semibold text-white hover:bg-slate-800"
          >
            Search
          </button>
        </form>

        <div className="mt-3 rounded-2xl border border-slate-200 bg-slate-50 p-2">
          <AsyncState
            loading={suggestionsState.loading}
            error={suggestionsState.error}
            isEmpty={!suggestionsState.loading && !suggestionsState.error && suggestionsState.data.length === 0}
            loadingMessage="Looking up instruments..."
            emptyMessage="No instruments match your query. You can still search directly by ticker."
          />

          {!suggestionsState.loading && !suggestionsState.error && suggestionsState.data.length > 0 ? (
            <div className="max-h-52 overflow-auto">
              {suggestionsState.data.map((item) => (
                <button
                  key={`${item.assetType}-${item.ticker}`}
                  type="button"
                  onClick={() => handleSelectInstrument(item)}
                  className="flex w-full items-center justify-between rounded-xl px-3 py-2 text-left text-sm text-slate-700 hover:bg-white"
                >
                  <span>
                    <span className="font-semibold text-slate-900">{item.ticker}</span> - {item.name}
                  </span>
                  <span className="rounded-full border border-slate-200 bg-white px-2 py-0.5 text-xs font-medium text-slate-500">
                    {item.assetType}
                  </span>
                </button>
              ))}
            </div>
          ) : null}
        </div>
      </PageCard>

      {selectedInstrument ? (
        <div className="grid gap-6 xl:grid-cols-[1.05fr_1.95fr]">
          <PageCard className="p-5 sm:p-6">
            <SectionHeader
              title="Asset Details"
              description="Latest price snapshot for the selected asset."
              actions={(
                <button
                  type="button"
                  onClick={() => loadDetails(selectedInstrument)}
                  className="inline-flex items-center gap-2 rounded-2xl border border-slate-200 bg-white px-3 py-1.5 text-sm font-semibold text-slate-700 hover:bg-slate-100"
                >
                  <RefreshCw size={14} className={detailsState.loading ? 'animate-spin' : ''} />
                  Refresh
                </button>
              )}
            />

            <AsyncState
              loading={detailsState.loading}
              error={detailsState.error}
              isEmpty={!detailsState.loading && !detailsState.error && !details}
              emptyMessage="Select an asset to view details."
            />

            {!detailsState.loading && !detailsState.error && details ? (
              <div className="space-y-3 text-sm">
                <div className="rounded-2xl border border-slate-200 bg-slate-50 p-3">
                  <p className="text-xs text-slate-500">Asset</p>
                  <p className="font-semibold text-slate-900">{details.assetName}</p>
                  <p className="text-xs text-slate-500">{details.ticker} - {details.assetType}</p>
                </div>

                <div className="grid gap-2 sm:grid-cols-2">
                  <div className="rounded-xl border border-slate-200 bg-white p-3">
                    <p className="text-xs text-slate-500">Current Price</p>
                    <p className="font-semibold text-slate-900">{currency.format(details.currentPrice)}</p>
                  </div>
                  <div className="rounded-xl border border-slate-200 bg-white p-3">
                    <p className="text-xs text-slate-500">Previous Close</p>
                    <p className="font-semibold text-slate-900">
                      {details.previousClose != null ? currency.format(details.previousClose) : 'Not available'}
                    </p>
                  </div>
                  <div className="rounded-xl border border-slate-200 bg-white p-3">
                    <p className="text-xs text-slate-500">Daily Change</p>
                    <p className={`font-semibold ${isPositiveDay ? 'text-emerald-600' : 'text-rose-600'}`}>
                      {details.dailyChange != null ? currency.format(details.dailyChange) : 'Not available'}
                    </p>
                  </div>
                  <div className="rounded-xl border border-slate-200 bg-white p-3">
                    <p className="text-xs text-slate-500">Daily % Change</p>
                    <p className={`font-semibold ${isPositiveDay ? 'text-emerald-600' : 'text-rose-600'}`}>
                      {formatPercent(details.dailyChangePercent)}
                    </p>
                  </div>
                </div>

                <div className="rounded-xl border border-slate-200 bg-white p-3 text-xs text-slate-500">
                  <p>Currency: {details.currency}</p>
                  <p>Last updated: {new Date(details.lastUpdated).toLocaleString()}</p>
                </div>
              </div>
            ) : null}
          </PageCard>

          <PageCard className="p-5 sm:p-6">
            <SectionHeader
              title="Price Chart"
              description="Historical movement for the selected timeframe."
              actions={(
                <div className="flex flex-wrap gap-2">
                  {MARKET_TIMEFRAMES.map((period) => (
                    <button
                      key={period}
                      type="button"
                      onClick={() => setTimeframe(period)}
                      className={`rounded-full px-3 py-1.5 text-sm font-medium transition ${
                        timeframe === period ? 'bg-slate-900 text-white' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                      }`}
                    >
                      {period[0] + period.slice(1).toLowerCase()}
                    </button>
                  ))}
                </div>
              )}
            />

            <AsyncState
              loading={chartState.loading}
              error={chartState.error}
              isEmpty={!chartState.loading && !chartState.error && chartPoints.length === 0}
              loadingMessage="Loading price history..."
              emptyMessage="No chart data available for this selection."
            />

            {!chartState.loading && !chartState.error && chartPoints.length > 0 ? (
              <>
                <div className="mb-4 flex flex-wrap items-center gap-3">
                  <span className={`rounded-full px-2.5 py-1 text-xs font-semibold ${isPositivePeriod ? 'bg-emerald-50 text-emerald-700' : 'bg-rose-50 text-rose-700'}`}>
                    {chartState.data?.periodChange != null ? currency.format(chartState.data.periodChange) : 'Not available'}
                    {' '}({formatPercent(chartState.data?.periodChangePercent)})
                  </span>
                  {chartState.data?.isLimitedToMonth ? (
                    <span className="text-xs text-slate-500">Bonds and crypto show up to 30 days of backend data.</span>
                  ) : null}
                </div>

                <div className="h-[260px] w-full sm:h-[320px]">
                  <ResponsiveContainer width="100%" height="100%">
                    <LineChart data={chartPoints} margin={{ top: 5, right: 16, left: 0, bottom: 5 }}>
                      <CartesianGrid stroke="rgba(15,23,42,0.08)" strokeDasharray="3 3" vertical={false} />
                      <XAxis
                        dataKey="label"
                        tickLine={false}
                        axisLine={false}
                        tick={{ fontSize: 11, fill: '#64748b' }}
                        interval="preserveStartEnd"
                      />
                      <YAxis
                        tickLine={false}
                        axisLine={false}
                        tick={{ fontSize: 11, fill: '#64748b' }}
                        width={54}
                        tickFormatter={(value) => `$${value >= 1000 ? `${(value / 1000).toFixed(0)}k` : value.toFixed(0)}`}
                      />
                      <Tooltip content={<ChartTooltipContent />} />
                      <Line
                        type="monotone"
                        dataKey="close"
                        stroke="#0f172a"
                        strokeWidth={2.5}
                        dot={false}
                        activeDot={{ r: 4, strokeWidth: 0 }}
                        isAnimationActive
                        animationDuration={350}
                      />
                    </LineChart>
                  </ResponsiveContainer>
                </div>
              </>
            ) : null}
          </PageCard>
        </div>
      ) : null}
    </div>
  )
}

