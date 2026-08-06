import { useCallback, useEffect, useState } from 'react'
import { RefreshCw } from 'lucide-react'
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts'
import { getAllPortfolioSnapshots, getPortfolioTracking } from '../services/portfolioService'
import InfoTooltip from './InfoTooltip'

const PERIODS = [
  { label: 'Daily', value: 'DAILY' },
  { label: 'Weekly', value: 'WEEKLY' },
  { label: 'Monthly', value: 'MONTHLY' },
  { label: 'Yearly', value: 'YEARLY' },
  { label: 'All Time', value: 'ALL' },
]

const usd = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD', maximumFractionDigits: 2 })

function formatLabel(dateStr, period) {
  const date = new Date(dateStr + 'T00:00:00')
  if (period === 'YEARLY') return date.getFullYear().toString()
  if (period === 'DAILY') return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })
  return date.toLocaleDateString('en-US', { month: 'short', year: '2-digit' })
}

function ChartTooltip({ active, payload, label }) {
  if (!active || !payload?.length) return null
  return (
    <div className="rounded-xl border border-slate-200 bg-white p-3 shadow-lg">
      <p className="mb-1 text-xs text-slate-500">{label}</p>
      <p className="text-sm font-semibold text-slate-900">{usd.format(payload[0].value)}</p>
    </div>
  )
}

export default function PerformanceChart() {
  const [activePeriod, setActivePeriod] = useState('MONTHLY')
  const [chartData, setChartData] = useState([])
  const [metrics, setMetrics] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const fetchData = useCallback(async (period) => {
    setLoading(true)
    setError(null)
    try {
      let snapshots
      let periodMetrics = null
      if (period === 'ALL') {
        snapshots = await getAllPortfolioSnapshots()
      } else {
        const response = await getPortfolioTracking(period)
        snapshots = response.snapshots ?? []
        periodMetrics = response.metrics ?? null
      }
      setChartData(
        snapshots.map((s) => ({
          name: formatLabel(s.snapshotDate, period),
          value: Number(s.totalValue),
        }))
      )
      setMetrics(periodMetrics)
    } catch {
      setError('We could not load performance data right now.')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchData(activePeriod)
  }, [activePeriod, fetchData])

  const periodChange = metrics ? Number(metrics.periodChange) : null
  const periodChangePct = metrics ? Number(metrics.periodChangePct) : null
  const isPositive = periodChange !== null && periodChange >= 0

  return (
    <div className="rounded-[24px] border border-slate-200 bg-white p-4 shadow-[0_10px_30px_rgba(15,23,42,0.05)] sm:p-5">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <div className="flex items-center gap-1.5">
            <h3 className="text-lg font-semibold text-slate-900">Portfolio Performance</h3>
            <InfoTooltip
              title="Portfolio performance chart"
              description="This chart tracks your portfolio value over time. Use the period filters to zoom in or out and compare trends."
            />
          </div>
          <p className="text-sm text-slate-500">
            {metrics?.periodLabel ?? 'Portfolio value over time'}
          </p>
          {periodChange !== null && (
            <p className={`mt-1 text-sm font-medium ${isPositive ? 'text-emerald-600' : 'text-rose-600'}`}>
              {isPositive ? '+' : ''}{usd.format(periodChange)}
              {' '}({isPositive ? '+' : ''}{periodChangePct.toFixed(2)}%)
            </p>
          )}
        </div>
        <div className="flex flex-wrap gap-2">
          {PERIODS.map(({ label, value }) => (
            <button
              key={value}
              type="button"
              onClick={() => setActivePeriod(value)}
              className={`rounded-full px-3 py-1.5 text-sm font-medium transition ${
                activePeriod === value
                  ? 'bg-slate-900 text-white'
                  : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
              }`}
            >
              {label}
            </button>
          ))}
        </div>
      </div>

      <div className="mt-6 h-[280px] w-full sm:h-[340px]">
        {loading ? (
          <div role="status" aria-live="polite" className="flex h-full items-center justify-center">
            <RefreshCw size={20} className="animate-spin text-slate-400" />
            <span className="sr-only">Loading performance chart data</span>
          </div>
        ) : error ? (
          <div className="flex h-full flex-col items-center justify-center gap-3">
            <p role="alert" className="text-sm text-rose-500">{error}</p>
            <button
              type="button"
              onClick={() => fetchData(activePeriod)}
              className="rounded-2xl border border-slate-200 bg-white px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-100"
            >
              Try again
            </button>
          </div>
        ) : chartData.length === 0 ? (
          <div className="flex h-full items-center justify-center text-center">
            <p className="text-sm text-slate-500">No snapshots yet.<br />Add holdings to start tracking performance.</p>
          </div>
        ) : (
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={chartData} margin={{ top: 5, right: 16, left: 0, bottom: 5 }}>
              <CartesianGrid stroke="rgba(255,255,255,0.08)" strokeDasharray="3 3" vertical={false} />
              <XAxis
                dataKey="name"
                tickLine={false}
                axisLine={false}
                tick={{ fontSize: 11, fill: '#9a9a96' }}
                interval="preserveStartEnd"
              />
              <YAxis
                tickLine={false}
                axisLine={false}
                tick={{ fontSize: 11, fill: '#9a9a96' }}
                tickFormatter={(v) => `$${v >= 1000 ? `${(v / 1000).toFixed(0)}k` : v}`}
                width={52}
              />
              <Tooltip content={<ChartTooltip />} />
              <Line
                type="monotone"
                dataKey="value"
                stroke="#7fff3c"
                strokeWidth={2.5}
                dot={false}
                activeDot={{ r: 4, strokeWidth: 0 }}
                isAnimationActive={true}
                animationDuration={400}
              />
            </LineChart>
          </ResponsiveContainer>
        )}
      </div>
    </div>
  )
}
