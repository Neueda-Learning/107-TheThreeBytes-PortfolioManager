import { useEffect, useMemo, useState } from 'react'
import { Coins, Plus, Trash2, X } from 'lucide-react'
import AsyncState from '../components/AsyncState'
import ConfirmDialog from '../components/ConfirmDialog'
import DataTableShell from '../components/DataTableShell'
import InfoTooltip from '../components/InfoTooltip'
import PageCard from '../components/PageCard'
import SectionHeader from '../components/SectionHeader'
import StatCard from '../components/StatCard'
import {
  createDividend,
  deleteDividend,
  getApiErrorMessage,
  getDividendTotal,
  getDividends,
} from '../services/portfolioService'
import { currency } from '../utils/formatters'

const emptyForm = {
  ticker: '',
  dividendPerShare: '',
  sharesHeld: 1,
  dividendDate: '',
}
const TICKER_PATTERN = /^[A-Za-z.]{1,10}$/

function validateDividendForm(form) {
  const errors = {}
  if (!form.ticker?.trim()) {
    errors.ticker = 'Please enter a ticker symbol.'
  } else if (!TICKER_PATTERN.test(form.ticker.trim())) {
    errors.ticker = 'Use letters or dot only, up to 10 characters.'
  }
  if (!Number.isInteger(Number(form.sharesHeld)) || Number(form.sharesHeld) < 1) {
    errors.sharesHeld = 'Shares held must be a whole number of at least 1.'
  }
  if (form.dividendPerShare === '' || Number(form.dividendPerShare) <= 0) {
    errors.dividendPerShare = 'Dividend per share must be greater than 0.'
  }
  if (!form.dividendDate) {
    errors.dividendDate = 'Please select a dividend date.'
  }
  return errors
}

function DividendModal({ isOpen, onClose, onSubmit }) {
  const [form, setForm] = useState(emptyForm)
  const [errors, setErrors] = useState({})
  const [submitError, setSubmitError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  useEffect(() => {
    if (isOpen) {
      setForm(emptyForm)
      setErrors({})
      setSubmitError('')
      setIsSubmitting(false)
    }
  }, [isOpen])

  if (!isOpen) return null
  const dialogTitleId = 'dividend-modal-title'
  const dialogDescriptionId = 'dividend-modal-description'

  const handleChange = (e) => {
    const { name, value } = e.target
    setForm((prev) => ({ ...prev, [name]: name === 'sharesHeld' ? Number(value) : value }))
    setErrors((prev) => ({ ...prev, [name]: undefined }))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    const nextErrors = validateDividendForm(form)
    setErrors(nextErrors)
    setSubmitError('')
    if (Object.keys(nextErrors).length > 0) return

    setIsSubmitting(true)
    const ok = await onSubmit({
      ...form,
      ticker: form.ticker.trim().toUpperCase(),
      dividendPerShare: parseFloat(form.dividendPerShare),
      sharesHeld: parseInt(form.sharesHeld, 10),
    })
    setIsSubmitting(false)
    if (ok) {
      onClose()
      return
    }
    setSubmitError('We could not save this dividend record. Please check the details and try again.')
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
            <h3 id={dialogTitleId} className="text-xl font-semibold text-slate-900">Record Dividend</h3>
            <p id={dialogDescriptionId} className="text-sm text-slate-500">Save a dividend payment for one holding.</p>
          </div>
          <button type="button" aria-label="Close dividend form" disabled={isSubmitting} onClick={onClose} className="rounded-full p-2 text-slate-500 hover:bg-slate-100 disabled:opacity-50"><X size={18} /></button>
        </div>

        <form className="grid gap-4 sm:grid-cols-2" onSubmit={handleSubmit}>
          <label className="text-sm font-medium text-slate-700">
            <span className="inline-flex items-center gap-1.5">
              Ticker
              <InfoTooltip title="Ticker symbol" description="Short code used to identify the company or asset paying the dividend." />
            </span>
            <input required name="ticker" value={form.ticker} onChange={handleChange} placeholder="e.g. AAPL" className={`mt-1 w-full rounded-2xl border px-3 py-2 text-sm uppercase ${errors.ticker ? 'border-rose-300' : 'border-slate-200'}`} />
            {errors.ticker ? <p className="mt-1 text-xs text-rose-500">{errors.ticker}</p> : null}
          </label>
          <label className="text-sm font-medium text-slate-700">
            <span className="inline-flex items-center gap-1.5">
              Dividend per Share ($)
              <InfoTooltip title="Dividend per share" description="Cash dividend paid for each share you held on the dividend date." />
            </span>
            <input required type="number" min="0.01" step="0.01" name="dividendPerShare" value={form.dividendPerShare} onChange={handleChange} className={`mt-1 w-full rounded-2xl border px-3 py-2 text-sm ${errors.dividendPerShare ? 'border-rose-300' : 'border-slate-200'}`} />
            {errors.dividendPerShare ? <p className="mt-1 text-xs text-rose-500">{errors.dividendPerShare}</p> : null}
          </label>
          <label className="text-sm font-medium text-slate-700">
            Shares Held
            <input required type="number" min="1" name="sharesHeld" value={form.sharesHeld} onChange={handleChange} className={`mt-1 w-full rounded-2xl border px-3 py-2 text-sm ${errors.sharesHeld ? 'border-rose-300' : 'border-slate-200'}`} />
            {errors.sharesHeld ? <p className="mt-1 text-xs text-rose-500">{errors.sharesHeld}</p> : null}
          </label>
          <label className="text-sm font-medium text-slate-700">
            Dividend Date
            <input required type="date" name="dividendDate" value={form.dividendDate} onChange={handleChange} className={`mt-1 w-full rounded-2xl border px-3 py-2 text-sm ${errors.dividendDate ? 'border-rose-300' : 'border-slate-200'}`} />
            {errors.dividendDate ? <p className="mt-1 text-xs text-rose-500">{errors.dividendDate}</p> : null}
          </label>
          {submitError ? <p role="alert" className="sm:col-span-2 text-sm text-rose-500">{submitError}</p> : null}
          <div className="sm:col-span-2 flex justify-end gap-3 pt-2">
            <button type="button" disabled={isSubmitting} onClick={onClose} className="rounded-2xl border border-slate-200 px-4 py-2 text-sm font-medium text-slate-600 disabled:opacity-50">Cancel</button>
            <button type="submit" disabled={isSubmitting} className="rounded-2xl bg-emerald-700 px-4 py-2 text-sm font-semibold text-white disabled:opacity-60">Save Dividend</button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default function Dividends() {
  const [items, setItems] = useState([])
  const [total, setTotal] = useState(0)
  const [tickerFilter, setTickerFilter] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [confirmDeleteId, setConfirmDeleteId] = useState(null)

  const load = async (ticker = '') => {
    try {
      setLoading(true)
      setError(null)
      const [rows, totalResponse] = await Promise.all([
        getDividends(ticker.trim() || undefined),
        getDividendTotal(),
      ])
      setItems(rows)
      setTotal(Number(totalResponse?.totalDividendsReceived || 0))
    } catch (err) {
      setError(getApiErrorMessage(err, 'We could not load dividends right now.'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [])

  const totalFromRows = useMemo(
    () => items.reduce((sum, item) => sum + Number(item.totalDividend || 0), 0),
    [items],
  )

  const handleApplyFilter = () => {
    load(tickerFilter)
  }

  const handleCreate = async (payload) => {
    try {
      await createDividend(payload)
      await load(tickerFilter)
      return true
    } catch (err) {
      setError(getApiErrorMessage(err, 'We could not save this dividend right now.'))
      return false
    }
  }

  const handleConfirmDelete = async () => {
    try {
      await deleteDividend(confirmDeleteId)
      setConfirmDeleteId(null)
      await load(tickerFilter)
    } catch (err) {
      setError(getApiErrorMessage(err, 'We could not delete this dividend right now.'))
    }
  }

  return (
    <div className="space-y-6">
      <div className="grid gap-4 sm:grid-cols-2">
        <StatCard title="Total Dividends (All Holdings)" value={currency.format(total)} icon={Coins} info={{ title: 'Total dividends', description: 'Total dividend income recorded across all holdings.' }} />
        <StatCard title="Visible Rows Total" value={currency.format(totalFromRows)} icon={Coins} info={{ title: 'Visible rows total', description: 'Sum of dividends from the currently filtered table rows.' }} />
      </div>

      <PageCard className="p-5">
        <SectionHeader
          title="Dividends"
          description="Record, filter, and manage dividend payments."
          info={{
            title: 'Dividends',
            description: 'Track dividend payments and monitor income from your holdings.',
          }}
          actions={(
            <button onClick={() => setIsModalOpen(true)} className="inline-flex items-center gap-2 rounded-2xl bg-emerald-700 px-4 py-2 text-sm font-semibold text-white hover:bg-slate-800">
              <Plus size={15} /> Add Dividend
            </button>
          )}
        />

        <div className="mb-4 flex flex-wrap items-end gap-2">
          <label className="text-sm font-medium text-slate-700">
            <span className="inline-flex items-center gap-1.5">
              Filter by ticker
              <InfoTooltip title="Ticker filter" description="Show only dividend entries for one ticker symbol." />
            </span>
            <input
              value={tickerFilter}
              onChange={(e) => setTickerFilter(e.target.value)}
              placeholder="e.g. AAPL"
              className="mt-1 w-52 rounded-2xl border border-slate-200 px-3 py-2 text-sm uppercase"
            />
          </label>
          <button onClick={handleApplyFilter} className="rounded-2xl border border-slate-200 bg-white px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-100">Apply</button>
          <button onClick={() => { setTickerFilter(''); load('') }} className="rounded-2xl border border-slate-200 bg-white px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-100">Clear</button>
        </div>

        <AsyncState loading={loading} error={error} loadingMessage="Loading dividends..." onRetry={() => load(tickerFilter)} />

        {!loading && !error && (
          <DataTableShell
            headers={[
              { key: 'ticker', label: 'Ticker', info: { title: 'Ticker', description: 'Symbol of the asset that paid the dividend.' } },
              { key: 'dividend-per-share', label: 'Dividend/Share', info: { title: 'Dividend per share', description: 'Amount paid for each share you held.' } },
              { key: 'shares-held', label: 'Shares Held', info: { title: 'Shares held', description: 'Number of shares owned when the dividend was paid.' } },
              { key: 'total-dividend', label: 'Total Dividend', info: { title: 'Total dividend', description: 'Total cash received for this entry, calculated as dividend per share times shares held.' } },
              { key: 'dividend-date', label: 'Dividend Date', info: { title: 'Dividend date', description: 'Date when the dividend payment was recorded.' } },
              { key: 'actions', label: 'Actions', info: { title: 'Actions', description: 'Remove a dividend record.' } },
            ]}
            hasRows={items.length > 0}
            emptyMessage="No dividends found yet. Record a dividend to get started."
            colSpan={6}
          >
            {items.map((item) => (
              <tr key={item.id} className="transition hover:bg-slate-50">
                <td className="px-4 py-3 font-semibold text-slate-900">{item.ticker}</td>
                <td className="px-4 py-3 text-slate-600">{currency.format(Number(item.dividendPerShare || 0))}</td>
                <td className="px-4 py-3 text-slate-600">{item.sharesHeld}</td>
                <td className="px-4 py-3 font-semibold text-slate-900">{currency.format(Number(item.totalDividend || 0))}</td>
                <td className="px-4 py-3 text-slate-600">{item.dividendDate}</td>
                <td className="px-4 py-3">
                  <button type="button" aria-label={`Delete dividend record for ${item.ticker} on ${item.dividendDate}`} onClick={() => setConfirmDeleteId(item.id)} className="rounded-xl border border-rose-100 p-2 text-rose-500 hover:bg-rose-50"><Trash2 size={14} /></button>
                </td>
              </tr>
            ))}
          </DataTableShell>
        )}
      </PageCard>

      <DividendModal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} onSubmit={handleCreate} />
      <ConfirmDialog isOpen={Boolean(confirmDeleteId)} title="Delete dividend" message="This will remove the dividend record permanently." onCancel={() => setConfirmDeleteId(null)} onConfirm={handleConfirmDelete} />
    </div>
  )
}
