import { useEffect, useMemo, useState } from 'react'
import { Eye, Plus, RefreshCw, Search, Trash2, X } from 'lucide-react'
import ConfirmDialog from '../components/ConfirmDialog'
import {
  addToWatchlist,
  getApiErrorMessage,
  getPrice,
  getWatchlist,
  removeFromWatchlist,
} from '../services/portfolioService'
import { currency, formatPercent } from '../utils/formatters'
import { tickerCatalog } from '../data/tickerCatalog'

const ASSET_TYPES = ['STOCK', 'BOND', 'CRYPTO']
const emptyForm = { ticker: '', assetType: 'STOCK' }

function AddWatchlistModal({ isOpen, onClose, onSubmit }) {
  const [form, setForm] = useState(emptyForm)
  const [lookup, setLookup] = useState('')

  useEffect(() => {
    if (isOpen) {
      setForm(emptyForm)
      setLookup('')
    }
  }, [isOpen])

  const options = useMemo(() => {
    const query = lookup.trim().toLowerCase()
    if (!query) return tickerCatalog.slice(0, 8)

    return tickerCatalog
      .filter((item) => `${item.ticker} ${item.name} ${item.assetType}`.toLowerCase().includes(query))
      .slice(0, 8)
  }, [lookup])

  if (!isOpen) return null

  const handleChange = (e) => setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }))

  const selectOption = (item) => {
    setForm((prev) => ({ ...prev, ticker: item.ticker, assetType: item.assetType }))
    setLookup(item.name)
  }

  const handleSubmit = (e) => {
    e.preventDefault()
    onSubmit({ ...form, ticker: form.ticker.trim().toUpperCase() })
    onClose()
  }

  return (
    <div className="fixed inset-0 z-40 flex items-center justify-center bg-slate-950/40 p-4">
      <div className="w-full max-w-sm rounded-[28px] border border-slate-200 bg-white p-5 shadow-2xl">
        <div className="mb-4 flex items-center justify-between">
          <div>
            <h3 className="text-xl font-semibold text-slate-900">Add to Watchlist</h3>
            <p className="text-sm text-slate-500">Search by company name or ticker symbol.</p>
          </div>
          <button type="button" onClick={onClose} className="rounded-full p-2 text-slate-500 hover:bg-slate-100"><X size={18} /></button>
        </div>

        <div className="mb-4">
          <label className="block text-sm font-medium text-slate-700">
            Find ticker
            <div className="relative mt-1">
              <Search size={14} className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
              <input
                value={lookup}
                onChange={(e) => setLookup(e.target.value)}
                placeholder="e.g. Amazon, Apple, Bitcoin"
                className="w-full rounded-2xl border border-slate-200 py-2 pl-9 pr-3 text-sm"
              />
            </div>
          </label>
          <div className="mt-2 max-h-44 overflow-auto rounded-2xl border border-slate-200 bg-slate-50 p-2">
            {options.length === 0 ? (
              <p className="px-2 py-3 text-xs text-slate-500">No matches. You can still enter ticker manually below.</p>
            ) : (
              options.map((item) => (
                <button
                  key={item.ticker}
                  type="button"
                  onClick={() => selectOption(item)}
                  className="flex w-full items-center justify-between rounded-xl px-2 py-2 text-left text-sm text-slate-700 hover:bg-white"
                >
                  <span>
                    <span className="font-semibold text-slate-900">{item.ticker}</span> - {item.name}
                  </span>
                  <span className="rounded-full bg-white px-2 py-0.5 text-xs font-medium text-slate-500">{item.assetType}</span>
                </button>
              ))
            )}
          </div>
        </div>

        <form className="space-y-4" onSubmit={handleSubmit}>
          <label className="block text-sm font-medium text-slate-700">
            Ticker
            <input required name="ticker" value={form.ticker} onChange={handleChange} placeholder="e.g. AMZN" className="mt-1 w-full rounded-2xl border border-slate-200 px-3 py-2 text-sm uppercase" />
          </label>
          <label className="block text-sm font-medium text-slate-700">
            Asset Type
            <select name="assetType" value={form.assetType} onChange={handleChange} className="mt-1 w-full rounded-2xl border border-slate-200 px-3 py-2 text-sm">
              {ASSET_TYPES.map((t) => <option key={t}>{t}</option>)}
            </select>
          </label>
          <div className="flex justify-end gap-3 pt-2">
            <button type="button" onClick={onClose} className="rounded-2xl border border-slate-200 px-4 py-2 text-sm font-medium text-slate-600">Cancel</button>
            <button type="submit" className="rounded-2xl bg-slate-900 px-4 py-2 text-sm font-semibold text-white">Add to Watchlist</button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default function Watchlist() {
  const [items, setItems] = useState([])
  const [pricesByTicker, setPricesByTicker] = useState({})
  const [loading, setLoading] = useState(true)
  const [priceLoading, setPriceLoading] = useState(false)
  const [error, setError] = useState(null)
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [confirmDeleteId, setConfirmDeleteId] = useState(null)

  const loadPrices = async (watchlist) => {
    if (!watchlist.length) {
      setPricesByTicker({})
      return
    }

    setPriceLoading(true)
    const responses = await Promise.allSettled(watchlist.map((item) => getPrice(item.ticker)))
    const next = {}

    watchlist.forEach((item, index) => {
      const result = responses[index]
      if (result.status === 'fulfilled') {
        next[item.ticker] = result.value
      }
    })

    setPricesByTicker(next)
    setPriceLoading(false)
  }

  const load = async () => {
    try {
      setLoading(true)
      setError(null)
      const watchlist = await getWatchlist()
      setItems(watchlist)
      await loadPrices(watchlist)
    } catch (err) {
      setError(getApiErrorMessage(err, 'Could not load watchlist.'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [])

  const handleSave = async (form) => {
    try {
      await addToWatchlist(form)
      await load()
    } catch (err) {
      setError(getApiErrorMessage(err, 'Could not add ticker to watchlist.'))
    }
  }

  const handleConfirmDelete = async () => {
    try {
      await removeFromWatchlist(confirmDeleteId)
      setConfirmDeleteId(null)
      await load()
    } catch (err) {
      setError(getApiErrorMessage(err, 'Could not remove ticker from watchlist.'))
    }
  }

  return (
    <div className="space-y-6">
      <div className="rounded-[24px] border border-slate-200 bg-white p-5 shadow-[0_10px_30px_rgba(15,23,42,0.05)]">
        <div className="mb-4 flex items-center justify-between">
          <div>
            <h2 className="text-xl font-semibold text-slate-900">Watchlist</h2>
            <p className="text-sm text-slate-500">Instruments you are tracking with live price snapshots.</p>
          </div>
          <div className="flex items-center gap-3">
            <div className="rounded-full bg-slate-100 px-3 py-1 text-sm font-medium text-slate-600">{items.length} tracked</div>
            <button onClick={() => loadPrices(items)} className="inline-flex items-center gap-2 rounded-2xl border border-slate-200 bg-white px-3 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-100">
              <RefreshCw size={14} className={priceLoading ? 'animate-spin' : ''} /> Refresh Prices
            </button>
            <button onClick={() => setIsModalOpen(true)} className="inline-flex items-center gap-2 rounded-2xl bg-slate-900 px-4 py-2 text-sm font-semibold text-white hover:bg-slate-800">
              <Plus size={15} /> Add Ticker
            </button>
          </div>
        </div>

        {loading && <p className="py-10 text-center text-sm text-slate-500">Loading watchlist...</p>}
        {error && <p className="py-10 text-center text-sm text-rose-500">{error}</p>}

        {!loading && !error && items.length === 0 && (
          <div className="flex flex-col items-center justify-center py-16 text-center">
            <div className="mb-3 rounded-2xl bg-slate-100 p-4 text-slate-400"><Eye size={28} /></div>
            <p className="font-medium text-slate-700">Your watchlist is empty</p>
            <p className="mt-1 text-sm text-slate-500">Add a ticker above to start tracking it.</p>
          </div>
        )}

        {!loading && !error && items.length > 0 && (
          <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
            {items.map((item) => {
              const live = pricesByTicker[item.ticker]
              const isPositive = Number(live?.changePercent || 0) >= 0

              return (
                <div key={item.id} className="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="font-semibold text-slate-900">{item.ticker}</p>
                      <div className="mt-1 flex items-center gap-2">
                        <span className="rounded-full border border-slate-200 bg-white px-2 py-0.5 text-xs font-medium text-slate-600">{item.assetType}</span>
                        <span className="text-xs text-slate-400">Added {item.addedDate}</span>
                      </div>
                    </div>
                    <button onClick={() => setConfirmDeleteId(item.id)} className="ml-2 rounded-xl border border-rose-100 p-2 text-rose-500 hover:bg-rose-50">
                      <Trash2 size={14} />
                    </button>
                  </div>

                  <div className="mt-3 flex items-center justify-between rounded-xl border border-slate-200 bg-white px-3 py-2">
                    <div>
                      <p className="text-xs text-slate-400">Live Price</p>
                      <p className="text-sm font-semibold text-slate-900">{live?.currentPrice ? currency.format(live.currentPrice) : '—'}</p>
                    </div>
                    <div className={`rounded-full px-2.5 py-1 text-xs font-semibold ${isPositive ? 'bg-emerald-50 text-emerald-700' : 'bg-rose-50 text-rose-700'}`}>
                      {live?.changePercent !== undefined && live?.changePercent !== null ? formatPercent(live.changePercent) : '—'}
                    </div>
                  </div>
                </div>
              )
            })}
          </div>
        )}
      </div>

      <AddWatchlistModal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} onSubmit={handleSave} />
      <ConfirmDialog isOpen={Boolean(confirmDeleteId)} title="Remove from watchlist" message="This will remove this ticker from your watchlist." onCancel={() => setConfirmDeleteId(null)} onConfirm={handleConfirmDelete} />
    </div>
  )
}
