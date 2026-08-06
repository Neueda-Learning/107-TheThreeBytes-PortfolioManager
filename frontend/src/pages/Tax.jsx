import { useEffect, useMemo, useState } from 'react'
import { Calculator, Receipt, TrendingUp } from 'lucide-react'
import AsyncState from '../components/AsyncState'
import DataTableShell from '../components/DataTableShell'
import FilterChips from '../components/FilterChips'
import PageCard from '../components/PageCard'
import SectionHeader from '../components/SectionHeader'
import StatCard from '../components/StatCard'
import { getApiErrorMessage, getTaxEstimate } from '../services/portfolioService'
import { currency } from '../utils/formatters'

const CATEGORY_FILTERS = ['ALL', 'SHORT_TERM', 'LONG_TERM']

function formatRate(rate) {
  if (rate === null || rate === undefined) return '0.00%'
  return `${(Number(rate) * 100).toFixed(2)}%`
}

export default function Tax() {
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [activeFilter, setActiveFilter] = useState('ALL')

  const load = async () => {
    try {
      setLoading(true)
      setError(null)
      setItems(await getTaxEstimate())
    } catch (err) {
      setError(getApiErrorMessage(err, 'We could not load tax estimates right now.'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [])

  const filteredItems = useMemo(() => {
    if (activeFilter === 'ALL') return items
    return items.filter((item) => item.taxCategory === activeFilter)
  }, [items, activeFilter])

  const metrics = useMemo(() => {
    const totalLiability = filteredItems.reduce((sum, item) => sum + Number(item.estimatedTaxLiability || 0), 0)
    const totalGain = filteredItems.reduce((sum, item) => sum + Number(item.estimatedGain || 0), 0)
    const positiveGainCount = filteredItems.filter((item) => Number(item.estimatedGain || 0) > 0).length

    return {
      totalLiability,
      totalGain,
      positiveGainCount,
    }
  }, [filteredItems])

  return (
    <div className="space-y-6">
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
        <StatCard title="Estimated Total Tax Liability" value={currency.format(metrics.totalLiability)} icon={Calculator} info={{ title: 'Estimated tax liability', description: 'Projected tax based on current gains and holding period. Final tax may differ at filing time.' }} />
        <StatCard title="Estimated Total Unrealized Gain" value={currency.format(metrics.totalGain)} icon={TrendingUp} info={{ title: 'Estimated unrealized gain', description: 'Potential profit on investments you still hold at current prices.' }} />
        <StatCard title="Holdings with Positive Gain" value={String(metrics.positiveGainCount)} icon={Receipt} info={{ title: 'Holdings with gain', description: 'Count of positions currently above their cost basis.' }} />
      </div>

      <PageCard className="p-5">
        <SectionHeader
          title="Tax Estimates"
          description="Short-term and long-term estimated tax by holding."
          countLabel={`${filteredItems.length} holdings`}
          info={{
            title: 'Tax estimates',
            description: 'These estimates help you preview potential tax impact before selling. They are guidance, not tax advice.',
          }}
        />

        <div className="mb-4">
          <FilterChips
            label="Tax category"
            info={{
              title: 'Tax category filter',
              description: 'Short-term usually means held one year or less. Long-term usually means held more than one year.',
            }}
            options={CATEGORY_FILTERS}
            activeValue={activeFilter}
            onChange={setActiveFilter}
            formatLabel={(value) => (value === 'ALL' ? 'All' : value.replace('_', ' '))}
          />
        </div>

        <AsyncState loading={loading} error={error} loadingMessage="Loading tax estimates..." onRetry={load} />

        {!loading && !error && (
          <DataTableShell
            headers={[
              { key: 'ticker', label: 'Ticker', info: { title: 'Ticker', description: 'Short market symbol for each holding.' } },
              { key: 'asset', label: 'Asset', info: { title: 'Asset type', description: 'Investment category such as stock, bond, or crypto.' } },
              { key: 'purchase-date', label: 'Purchase Date', info: { title: 'Purchase date', description: 'Date the holding was bought.' } },
              { key: 'holding-days', label: 'Holding Days', info: { title: 'Holding days', description: 'Number of days between purchase date and today.' } },
              { key: 'category', label: 'Category', info: { title: 'Tax category', description: 'Classifies gains as short-term or long-term based on holding period.' } },
              { key: 'cost-basis', label: 'Cost Basis', info: { title: 'Cost basis', description: 'Original amount invested in this holding.' } },
              { key: 'current-value', label: 'Current Value', info: { title: 'Current value', description: 'Estimated market value based on latest available price.' } },
              { key: 'gain', label: 'Gain', info: { title: 'Gain', description: 'Difference between current value and cost basis.' } },
              { key: 'rate', label: 'Rate', info: { title: 'Tax rate', description: 'Estimated tax rate applied for this holding category.' } },
              { key: 'estimated-tax', label: 'Estimated Tax', info: { title: 'Estimated tax', description: 'Approximate tax due if this holding were sold now.' } },
            ]}
            hasRows={filteredItems.length > 0}
            emptyMessage="No tax estimates found for this filter."
            colSpan={10}
          >
            {filteredItems.map((item) => (
              <tr key={`${item.ticker}-${item.purchaseDate}-${item.holdingDays}`} className="transition hover:bg-slate-50">
                <td className="px-4 py-3 font-semibold text-slate-900">{item.ticker}</td>
                <td className="px-4 py-3 text-slate-600">{item.assetType}</td>
                <td className="px-4 py-3 text-slate-600">{item.purchaseDate}</td>
                <td className="px-4 py-3 text-slate-600">{item.holdingDays}</td>
                <td className="px-4 py-3">
                  <span className={`rounded-full px-2.5 py-1 text-xs font-semibold ${item.taxCategory === 'LONG_TERM' ? 'bg-emerald-50 text-emerald-700' : 'bg-amber-50 text-amber-700'}`}>
                    {item.taxCategory}
                  </span>
                </td>
                <td className="px-4 py-3 text-slate-600">{currency.format(item.costBasis || 0)}</td>
                <td className="px-4 py-3 text-slate-600">{currency.format(item.estimatedCurrentValue || 0)}</td>
                <td className={`px-4 py-3 font-medium ${Number(item.estimatedGain || 0) >= 0 ? 'text-emerald-600' : 'text-rose-600'}`}>
                  {currency.format(item.estimatedGain || 0)}
                </td>
                <td className="px-4 py-3 text-slate-600">{formatRate(item.taxRate)}</td>
                <td className="px-4 py-3 font-semibold text-slate-900">{currency.format(item.estimatedTaxLiability || 0)}</td>
              </tr>
            ))}
          </DataTableShell>
        )}
      </PageCard>
    </div>
  )
}
