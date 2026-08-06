import { useEffect, useMemo, useState } from 'react'
import { Pencil, X, TrendingDown } from 'lucide-react'
import AsyncState from '../components/AsyncState'
import ConfirmDialog from '../components/ConfirmDialog'
import DataTableShell from '../components/DataTableShell'
import InfoTooltip from '../components/InfoTooltip'
import PageCard from '../components/PageCard'
import RefreshAction from '../components/RefreshAction'
import SectionHeader from '../components/SectionHeader'
import {
  getApiErrorMessage,
  getPortfolioItems,
  getPrice,
  sellHolding,
  updatePortfolioItem,
} from '../services/portfolioService'
import { currency, formatPercent } from '../utils/formatters'

const ASSET_TYPES = ['STOCK', 'BOND', 'CRYPTO']
const emptyForm = { ticker: '', quantity: 1, assetType: 'STOCK', purchasePrice: '', purchaseDate: '' }
const TICKER_PATTERN = /^[A-Za-z.]{1,10}$/

function validateHoldingForm(form) {
  const errors = {}
  if (!form.ticker?.trim()) {
    errors.ticker = 'Please enter a ticker symbol.'
  } else if (!TICKER_PATTERN.test(form.ticker.trim())) {
    errors.ticker = 'Use letters or dot only, up to 10 characters.'
  }
  if (!form.assetType) {
    errors.assetType = 'Please select an asset type.'
  }
  if (!Number.isInteger(Number(form.quantity)) || Number(form.quantity) < 1) {
    errors.quantity = 'Quantity must be a whole number of at least 1.'
  }
  if (form.purchasePrice === '' || Number(form.purchasePrice) <= 0) {
    errors.purchasePrice = 'Purchase price must be greater than 0.'
  }
  if (!form.purchaseDate) {
    errors.purchaseDate = 'Please select a purchase date.'
  }
  return errors
}

function HoldingModal({ isOpen, onClose, onSubmit, initialData, mode }) {
  const [form, setForm] = useState(emptyForm)
  const [errors, setErrors] = useState({})
  const [submitError, setSubmitError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  useEffect(() => {
    if (isOpen) {
      setForm(initialData ? {
        ticker: initialData.ticker,
        quantity: initialData.quantity,
        assetType: initialData.assetType,
        purchasePrice: initialData.purchasePrice,
        purchaseDate: initialData.purchaseDate,
      } : emptyForm)
      setErrors({})
      setSubmitError('')
      setIsSubmitting(false)
    }
  }, [isOpen, initialData])

  if (!isOpen) return null
  const dialogTitleId = mode === 'edit' ? 'edit-holding-title' : 'add-holding-title'
  const dialogDescriptionId = mode === 'edit' ? 'edit-holding-description' : 'add-holding-description'

  const handleChange = (e) => {
    const { name, value } = e.target
    setForm((prev) => ({ ...prev, [name]: name === 'quantity' ? Number(value) : value }))
    setErrors((prev) => ({ ...prev, [name]: undefined }))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    const nextErrors = validateHoldingForm(form)
    setErrors(nextErrors)
    setSubmitError('')
    if (Object.keys(nextErrors).length > 0) return

    setIsSubmitting(true)
    const ok = await onSubmit({
      ...form,
      ticker: form.ticker.trim().toUpperCase(),
      purchasePrice: parseFloat(form.purchasePrice),
      quantity: parseInt(form.quantity, 10),
    })
    setIsSubmitting(false)

    if (ok) {
      onClose()
      return
    }
    setSubmitError('We could not save this holding. Please review the details and try again.')
  }

  return (
    <div className="fixed inset-0 z-40 flex items-center justify-center bg-slate-950/40 p-4">
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby={dialogTitleId}
        aria-describedby={dialogDescriptionId}
        className="w-full max-w-xl rounded-[28px] border border-slate-200 bg-white p-5 shadow-2xl"
      >
        <div className="mb-4 flex items-center justify-between">
          <div>
            <h3 id={dialogTitleId} className="text-xl font-semibold text-slate-900">{mode === 'edit' ? 'Edit Holding' : 'Add Holding'}</h3>
            <p id={dialogDescriptionId} className="text-sm text-slate-500">Saved directly to the backend.</p>
          </div>
          <button type="button" aria-label="Close holding form" disabled={isSubmitting} onClick={onClose} className="rounded-full p-2 text-slate-500 hover:bg-slate-100 disabled:opacity-50"><X size={18} /></button>
        </div>
        <form className="grid gap-4 sm:grid-cols-2" onSubmit={handleSubmit}>
          <label className="text-sm font-medium text-slate-700">
            <span className="inline-flex items-center gap-1.5">
              Ticker
              <InfoTooltip title="Ticker symbol" description="Short code used by exchanges to identify an asset, such as AAPL." />
            </span>
            <input required name="ticker" value={form.ticker} onChange={handleChange} placeholder="e.g. AAPL" className={`mt-1 w-full rounded-2xl border px-3 py-2 text-sm uppercase ${errors.ticker ? 'border-rose-300' : 'border-slate-200'}`} />
            {errors.ticker ? <p className="mt-1 text-xs text-rose-500">{errors.ticker}</p> : null}
          </label>
          <label className="text-sm font-medium text-slate-700">
            <span className="inline-flex items-center gap-1.5">
              Asset Type
              <InfoTooltip title="Asset type" description="Classifies the holding as stock, bond, or crypto for reporting and risk analysis." />
            </span>
            <select name="assetType" value={form.assetType} onChange={handleChange} className={`mt-1 w-full rounded-2xl border px-3 py-2 text-sm ${errors.assetType ? 'border-rose-300' : 'border-slate-200'}`}>
              {ASSET_TYPES.map((t) => <option key={t}>{t}</option>)}
            </select>
            {errors.assetType ? <p className="mt-1 text-xs text-rose-500">{errors.assetType}</p> : null}
          </label>
          <label className="text-sm font-medium text-slate-700">
            Quantity
            <input required type="number" min="1" name="quantity" value={form.quantity} onChange={handleChange} className={`mt-1 w-full rounded-2xl border px-3 py-2 text-sm ${errors.quantity ? 'border-rose-300' : 'border-slate-200'}`} />
            {errors.quantity ? <p className="mt-1 text-xs text-rose-500">{errors.quantity}</p> : null}
          </label>
          <label className="text-sm font-medium text-slate-700">
            <span className="inline-flex items-center gap-1.5">
              Purchase Price ($)
              <InfoTooltip title="Purchase price" description="Price paid per unit when you bought this holding. It is used to calculate gains and losses." />
            </span>
            <input required type="number" min="0.01" step="0.01" name="purchasePrice" value={form.purchasePrice} onChange={handleChange} className={`mt-1 w-full rounded-2xl border px-3 py-2 text-sm ${errors.purchasePrice ? 'border-rose-300' : 'border-slate-200'}`} />
            {errors.purchasePrice ? <p className="mt-1 text-xs text-rose-500">{errors.purchasePrice}</p> : null}
          </label>
          <label className="sm:col-span-2 text-sm font-medium text-slate-700">
            Purchase Date
            <input required type="date" name="purchaseDate" value={form.purchaseDate} onChange={handleChange} className={`mt-1 w-full rounded-2xl border px-3 py-2 text-sm ${errors.purchaseDate ? 'border-rose-300' : 'border-slate-200'}`} />
            {errors.purchaseDate ? <p className="mt-1 text-xs text-rose-500">{errors.purchaseDate}</p> : null}
          </label>
          {submitError ? <p role="alert" className="sm:col-span-2 text-sm text-rose-500">{submitError}</p> : null}
          <div className="sm:col-span-2 flex justify-end gap-3 pt-2">
            <button type="button" disabled={isSubmitting} onClick={onClose} className="rounded-2xl border border-slate-200 px-4 py-2 text-sm font-medium text-slate-600 disabled:opacity-50">Cancel</button>
            <button type="submit" disabled={isSubmitting} className="rounded-2xl bg-emerald-700 px-4 py-2 text-sm font-semibold text-white disabled:opacity-60">Save Holding</button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default function Holdings({ refreshToken }) {
  const [items, setItems] = useState([])
  const [pricesByTicker, setPricesByTicker] = useState({})
  const [loading, setLoading] = useState(true)
  const [priceLoading, setPriceLoading] = useState(false)
  const [error, setError] = useState(null)
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [editingItem, setEditingItem] = useState(null)
  const [confirmSellItem, setConfirmSellItem] = useState(null)

  const loadPrices = async (holdings) => {
    if (!holdings.length) {
      setPricesByTicker({})
      return
    }

    setPriceLoading(true)
    const tickers = [...new Set(holdings.map((item) => item.ticker))]
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
      const holdings = await getPortfolioItems()
      setItems(holdings)
      await loadPrices(holdings)
    } catch (err) {
      setError(getApiErrorMessage(err, 'We could not load your holdings right now.'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [refreshToken])

  const tableRows = useMemo(() => {
    return items.map((item) => {
      const priceData = pricesByTicker[item.ticker]
      const livePrice = priceData?.currentPrice
      const changePercent = priceData?.changePercent
      const currentValue = (livePrice ?? item.purchasePrice) * item.quantity
      const costBasis = item.purchasePrice * item.quantity
      const unrealized = currentValue - costBasis

      return {
        ...item,
        livePrice,
        changePercent,
        costBasis,
        currentValue,
        unrealized,
      }
    })
  }, [items, pricesByTicker])

  const handleEdit = (item) => {
    setEditingItem(item)
    setIsModalOpen(true)
  }

  const handleSave = async (form) => {
    try {
      if (!editingItem) return false
      await updatePortfolioItem(editingItem.id, form)
      await load()
      return true
    } catch (err) {
      setError(getApiErrorMessage(err, 'We could not save this holding right now.'))
      return false
    }
  }


  const handleConfirmSell = async () => {
    try {
      const price = confirmSellItem.livePrice ?? confirmSellItem.purchasePrice
      await sellHolding(confirmSellItem.id, price)
      setConfirmSellItem(null)
      await load()
    } catch (err) {
      setError(getApiErrorMessage(err, 'We could not complete this sell action right now.'))
    }
  }

  return (
    <div className="space-y-6">
      <PageCard className="p-5">
        <SectionHeader
          title="Holdings"
          description="Portfolio positions + live prices from backend price service."
          countLabel={`${items.length} positions`}
          info={{
            title: 'Holdings',
            description: 'Your current portfolio positions. Use this table to review live value and manage each holding.',
          }}
          actions={(
            <RefreshAction onClick={() => loadPrices(items)} loading={priceLoading} label="Refresh Prices" />
          )}
        />

        <AsyncState loading={loading} error={error} loadingMessage="Loading holdings..." onRetry={load} />

        {!loading && !error && (
          <DataTableShell
             headers={[
               { key: 'ticker', label: 'Ticker', info: { title: 'Ticker', description: 'Short symbol that identifies the asset.' } },
               { key: 'asset-type', label: 'Asset Type', info: { title: 'Asset type', description: 'Investment category such as stock, bond, or crypto.' } },
               { key: 'quantity', label: 'Quantity', info: { title: 'Quantity', description: 'Number of units you currently own.' } },
               { key: 'purchase-price', label: 'Purchase Price', info: { title: 'Purchase price', description: 'Average price per unit paid when acquired.' } },
               { key: 'live-price', label: 'Live Price', info: { title: 'Live price', description: 'Latest available market price from the price service.' } },
               { key: 'change-24h', label: '24h Change', info: { title: '24-hour change', description: 'Percentage movement in price over the past 24 hours.' } },
               { key: 'current-value', label: 'Current Value', info: { title: 'Current value', description: 'Estimated value now, calculated as live price multiplied by quantity.' } },
               { key: 'unrealized', label: 'Unrealized P/L', info: { title: 'Unrealized gain/loss', description: 'Potential profit or loss on holdings you have not sold yet.' } },
               { key: 'actions', label: 'Actions', info: { title: 'Actions', description: 'Edit details or sell all units.' } },
             ]}
            hasRows={tableRows.length > 0}
            emptyMessage="No holdings found yet."
            colSpan={9}
          >
            {tableRows.map((item) => (
              <tr key={item.id} className="transition hover:bg-slate-50">
                <td className="px-4 py-3 font-semibold text-slate-900">{item.ticker}</td>
                <td className="px-4 py-3"><span className="rounded-full bg-slate-100 px-2.5 py-1 text-xs font-medium text-slate-600">{item.assetType}</span></td>
                <td className="px-4 py-3 text-slate-600">{item.quantity}</td>
                <td className="px-4 py-3 text-slate-600">{currency.format(item.purchasePrice)}</td>
                <td className="px-4 py-3 font-medium text-slate-900">{item.livePrice !== undefined && item.livePrice !== null ? currency.format(item.livePrice) : '—'}</td>
                <td className={`px-4 py-3 font-medium ${Number(item.changePercent || 0) >= 0 ? 'text-emerald-600' : 'text-rose-600'}`}>
                  {item.changePercent !== undefined && item.changePercent !== null ? formatPercent(item.changePercent) : '—'}
                </td>
                <td className="px-4 py-3 text-slate-900">{currency.format(item.currentValue)}</td>
                <td className={`px-4 py-3 font-medium ${item.unrealized >= 0 ? 'text-emerald-600' : 'text-rose-600'}`}>{currency.format(item.unrealized)}</td>
                 <td className="px-4 py-3">
                   <div className="flex items-center gap-2">
                     <button type="button" aria-label={`Edit holding ${item.ticker}`} onClick={() => handleEdit(item)} className="rounded-xl border border-slate-200 p-2 text-slate-500 hover:bg-slate-100"><Pencil size={14} /></button>
                     <button type="button" aria-label={`Sell all units of ${item.ticker}`} onClick={() => setConfirmSellItem(item)} title="Sell all" className="rounded-xl border border-amber-200 p-2 text-amber-600 hover:bg-amber-50"><TrendingDown size={14} /></button>
                   </div>
                 </td>
              </tr>
            ))}
          </DataTableShell>
        )}
      </PageCard>

       <HoldingModal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} onSubmit={handleSave} initialData={editingItem} mode={editingItem ? 'edit' : 'add'} />
       <ConfirmDialog
         isOpen={Boolean(confirmSellItem)}
         title="Sell holding"
         message={confirmSellItem ? `Sell all ${confirmSellItem.quantity} unit(s) of ${confirmSellItem.ticker} at ${confirmSellItem.livePrice != null ? `live price $${Number(confirmSellItem.livePrice).toFixed(2)}` : `purchase price $${Number(confirmSellItem.purchasePrice).toFixed(2)}`}? This will remove the position and log a SELL transaction.` : ''}
         onCancel={() => setConfirmSellItem(null)}
         onConfirm={handleConfirmSell}
       />
     </div>
   )
 }
