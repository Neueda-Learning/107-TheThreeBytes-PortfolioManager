import { Trash2 } from 'lucide-react'
import AsyncState from '../components/AsyncState'
import ConfirmDialog from '../components/ConfirmDialog'
import DataTableShell from '../components/DataTableShell'
import PageCard from '../components/PageCard'
import RefreshAction from '../components/RefreshAction'
import SectionHeader from '../components/SectionHeader'
import {
  deleteTransaction,
  getApiErrorMessage,
  getPrice,
  getTransactions,
} from '../services/portfolioService'
import { currency } from '../utils/formatters'
import { useEffect, useMemo, useState } from 'react'

export default function Transactions() {
  const [items, setItems] = useState([])
  const [pricesByTicker, setPricesByTicker] = useState({})
  const [loading, setLoading] = useState(true)
  const [priceLoading, setPriceLoading] = useState(false)
  const [error, setError] = useState(null)
  const [confirmDeleteId, setConfirmDeleteId] = useState(null)

  const loadPrices = async (rows) => {
    if (!rows.length) {
      setPricesByTicker({})
      return
    }

    setPriceLoading(true)
    const tickers = [...new Set(rows.map((row) => row.ticker))]
    const responses = await Promise.allSettled(tickers.map((ticker) => getPrice(ticker)))
    const next = {}

    tickers.forEach((ticker, index) => {
      const result = responses[index]
      if (result.status === 'fulfilled') {
        next[ticker] = result.value
      }
    })

    setPricesByTicker(next)
    setPriceLoading(false)
  }

  const load = async () => {
    try {
      setLoading(true)
      setError(null)
      const txs = await getTransactions()
      setItems(txs)
      await loadPrices(txs)
    } catch (err) {
      setError(getApiErrorMessage(err, 'We could not load your transactions right now.'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [])

  const rows = useMemo(() => {
    return items.map((item) => {
      const livePrice = pricesByTicker[item.ticker]?.currentPrice
      const priceDiff = livePrice !== undefined && livePrice !== null
        ? Number(livePrice) - Number(item.pricePerUnit)
        : null
      return { ...item, livePrice, priceDiff }
    })
  }, [items, pricesByTicker])

  const handleConfirmDelete = async () => {
    try {
      await deleteTransaction(confirmDeleteId)
      setConfirmDeleteId(null)
      await load()
    } catch (err) {
      setError(getApiErrorMessage(err, 'We could not delete this transaction right now.'))
    }
  }

  return (
    <div className="space-y-6">
      <PageCard className="p-5">
        <SectionHeader
          title="Transactions"
          description="Buy and sell history with current market comparison."
          countLabel={`${items.length} records`}
          info={{
            title: 'Transactions',
            description: 'History of completed buys and sells. Use this to review what you traded and when.',
          }}
          actions={<RefreshAction onClick={() => loadPrices(items)} loading={priceLoading} label="Refresh Prices" />}
        />

        <AsyncState loading={loading} error={error} loadingMessage="Loading transactions..." onRetry={load} />

        {!loading && !error && (
          <DataTableShell
            headers={[
              { key: 'ticker', label: 'Ticker', info: { title: 'Ticker', description: 'Short symbol for the traded asset.' } },
              { key: 'type', label: 'Type', info: { title: 'Trade type', description: 'BUY adds units to your portfolio; SELL removes units.' } },
              { key: 'asset', label: 'Asset', info: { title: 'Asset type', description: 'Category of the traded instrument.' } },
              { key: 'quantity', label: 'Quantity', info: { title: 'Quantity', description: 'Number of units traded in this transaction.' } },
              { key: 'price-unit', label: 'Price/Unit', info: { title: 'Price per unit', description: 'Execution price for each unit in this trade.' } },
              { key: 'live-price', label: 'Live Price', info: { title: 'Live price', description: 'Latest available market price for quick comparison.' } },
              { key: 'diff', label: 'Diff', info: { title: 'Price difference', description: 'Difference between current price and transaction price per unit.' } },
              { key: 'total-value', label: 'Total Value', info: { title: 'Total value', description: 'Transaction amount, usually quantity multiplied by price per unit.' } },
              { key: 'date', label: 'Date', info: { title: 'Transaction date', description: 'Date this buy or sell was recorded.' } },
              { key: 'actions', label: 'Actions', info: { title: 'Actions', description: 'Remove a transaction record from history.' } },
            ]}
            hasRows={rows.length > 0}
            emptyMessage="No transactions yet."
            colSpan={10}
          >
            {rows.map((item) => (
              <tr key={item.id} className="transition hover:bg-slate-50">
                <td className="px-4 py-3 font-semibold text-slate-900">{item.ticker}</td>
                <td className="px-4 py-3">
                  <span className={`rounded-full px-2.5 py-1 text-xs font-semibold ${item.transactionType === 'BUY' ? 'bg-emerald-50 text-emerald-700' : 'bg-rose-50 text-rose-700'}`}>
                    {item.transactionType}
                  </span>
                </td>
                <td className="px-4 py-3"><span className="rounded-full bg-slate-100 px-2.5 py-1 text-xs font-medium text-slate-600">{item.assetType}</span></td>
                <td className="px-4 py-3 text-slate-600">{item.quantity}</td>
                <td className="px-4 py-3 text-slate-600">{currency.format(item.pricePerUnit)}</td>
                <td className="px-4 py-3 text-slate-900">{item.livePrice !== undefined && item.livePrice !== null ? currency.format(item.livePrice) : '—'}</td>
                <td className={`px-4 py-3 font-medium ${Number(item.priceDiff || 0) >= 0 ? 'text-emerald-600' : 'text-rose-600'}`}>{item.priceDiff !== null ? currency.format(item.priceDiff) : '—'}</td>
                <td className="px-4 py-3 font-medium text-slate-900">{currency.format(item.totalValue)}</td>
                <td className="px-4 py-3 text-slate-600">{item.transactionDate}</td>
                <td className="px-4 py-3">
                  <button type="button" aria-label={`Delete transaction for ${item.ticker} on ${item.transactionDate}`} onClick={() => setConfirmDeleteId(item.id)} className="rounded-xl border border-rose-100 p-2 text-rose-500 hover:bg-rose-50"><Trash2 size={14} /></button>
                </td>
              </tr>
            ))}
          </DataTableShell>
        )}
      </PageCard>

      <ConfirmDialog isOpen={Boolean(confirmDeleteId)} title="Delete transaction" message="This will permanently remove this transaction record." onCancel={() => setConfirmDeleteId(null)} onConfirm={handleConfirmDelete} />
    </div>
  )
}
