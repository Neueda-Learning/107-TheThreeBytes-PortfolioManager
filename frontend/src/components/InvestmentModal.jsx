import { useEffect, useMemo, useState } from 'react'
import { AlertTriangle, CheckCircle2, Coins, Landmark, Loader2, Search, TrendingUp, X, XCircle } from 'lucide-react'
import { createInvestment, getApiErrorMessage, getStockQuote, searchInstruments } from '../services/investmentService'
import InfoTooltip from './InfoTooltip'

const today = () => new Date().toISOString().slice(0, 10)

const emptyForm = {
  assetType: 'STOCK',
  name: '',
  ticker: '',
  sector: '',
  issuer: '',
  interestRate: '',
  maturityDate: '',
  quantity: '',
  purchasePrice: '',
  purchaseDate: today(),
}

const TYPE_OPTIONS = [
  { value: 'STOCK', label: 'Stock', icon: TrendingUp },
  { value: 'BOND', label: 'Bond', icon: Landmark },
  { value: 'CRYPTO', label: 'Crypto', icon: Coins },
]

const SECTOR_SUGGESTIONS = [
  'Technology', 'Healthcare', 'Financials', 'Energy', 'Consumer',
  'Industrials', 'Utilities', 'Real Estate', 'Materials', 'Communication Services',
]

const TICKER_PATTERN = /^[A-Za-z.]{1,10}$/

function searchLabel(assetType) {
  if (assetType === 'BOND') return 'Search bond name'
  if (assetType === 'CRYPTO') return 'Search cryptocurrency'
  return 'Search stock/company name'
}

function validate(form) {
  const errors = {}

  if (!form.name.trim()) {
    errors.name = 'Name is required'
  }

  if (!form.ticker.trim()) {
    errors.ticker = 'Symbol is required'
  } else if (!TICKER_PATTERN.test(form.ticker.trim())) {
    errors.ticker = 'Use letters/dot only, up to 10 characters'
  }

  if (form.quantity === '' || form.quantity === null) {
    errors.quantity = 'Quantity is required'
  } else if (!Number.isInteger(Number(form.quantity)) || Number(form.quantity) < 1) {
    errors.quantity = 'Quantity must be a whole number of at least 1'
  }

  if (form.purchasePrice === '' || form.purchasePrice === null) {
    errors.purchasePrice = 'Purchase price is required'
  } else if (Number(form.purchasePrice) <= 0) {
    errors.purchasePrice = 'Purchase price must be greater than 0'
  }

  if (!form.purchaseDate) {
    errors.purchaseDate = 'Purchase date is required'
  } else if (form.purchaseDate > today()) {
    errors.purchaseDate = 'Purchase date cannot be in the future'
  }

  if (form.assetType === 'STOCK' && !form.sector.trim()) {
    errors.sector = 'Sector is required'
  }

  if (form.assetType === 'BOND') {
    if (!form.issuer.trim()) {
      errors.issuer = 'Issuer is required'
    }
    if (!form.maturityDate) {
      errors.maturityDate = 'Maturity date is required'
    } else if (form.purchaseDate && form.maturityDate <= form.purchaseDate) {
      errors.maturityDate = 'Maturity date must be after the purchase date'
    }
    if (form.interestRate !== '' && form.interestRate !== null) {
      const rate = Number(form.interestRate)
      if (Number.isNaN(rate) || rate < 0 || rate > 100) {
        errors.interestRate = 'Interest rate must be between 0 and 100'
      }
    }
  }

  return errors
}

export default function InvestmentModal({ isOpen, onClose, onSuccess }) {
  const [form, setForm] = useState(emptyForm)
  const [errors, setErrors] = useState({})
  const [isSuggestionsOpen, setIsSuggestionsOpen] = useState(false)
  const [status, setStatus] = useState('idle') // idle | submitting | success | error
  const [statusMessage, setStatusMessage] = useState('')
  const [quote, setQuote] = useState({ status: 'idle', data: null, error: '' }) // idle | loading | success | error

  useEffect(() => {
    if (isOpen) {
      setForm(emptyForm)
      setErrors({})
      setStatus('idle')
      setStatusMessage('')
      setIsSuggestionsOpen(false)
      setQuote({ status: 'idle', data: null, error: '' })
    }
  }, [isOpen])

  const fetchStockQuote = async (rawTicker) => {
    const ticker = (rawTicker || '').trim().toUpperCase()
    if (!TICKER_PATTERN.test(ticker)) return

    setQuote({ status: 'loading', data: null, error: '' })
    try {
      const data = await getStockQuote(ticker)
      if (data.currentPrice == null) {
        setQuote({ status: 'error', data, error: data.errorMessage || 'Price unavailable for this ticker.' })
        return
      }
      setQuote({ status: 'success', data, error: '' })
      setForm((current) => ({
        ...current,
        purchasePrice: String(data.currentPrice),
        name: current.name || data.companyName || current.name,
        sector: current.sector || data.sector || current.sector,
      }))
      setErrors((current) => ({ ...current, purchasePrice: undefined }))
    } catch (err) {
      setQuote({ status: 'error', data: null, error: getApiErrorMessage(err, 'Could not fetch stock price.') })
    }
  }

  const suggestions = useMemo(
    () => searchInstruments(form.assetType, form.name),
    [form.assetType, form.name],
  )

  if (!isOpen) return null
  const titleId = 'investment-modal-title'
  const descriptionId = 'investment-modal-description'

  const setField = (name, value) => {
    setForm((current) => ({ ...current, [name]: value }))
    setErrors((current) => ({ ...current, [name]: undefined }))
  }

  const handleChange = (event) => {
    const { name, value } = event.target
    setField(name, value)
  }

  const handleTypeChange = (assetType) => {
    setForm({ ...emptyForm, assetType, purchaseDate: form.purchaseDate || today() })
    setErrors({})
    setIsSuggestionsOpen(false)
    setQuote({ status: 'idle', data: null, error: '' })
  }

  const handleSelectSuggestion = (item) => {
    setForm((current) => ({ ...current, name: item.name, ticker: item.ticker }))
    setErrors((current) => ({ ...current, name: undefined, ticker: undefined }))
    setIsSuggestionsOpen(false)
    if (form.assetType === 'STOCK') {
      fetchStockQuote(item.ticker)
    }
  }

  const handleTickerBlur = () => {
    if (form.assetType === 'STOCK') {
      fetchStockQuote(form.ticker)
    }
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    const validationErrors = validate(form)
    setErrors(validationErrors)
    if (Object.keys(validationErrors).length > 0) {
      return
    }

    try {
      setStatus('submitting')
      setStatusMessage('')
      const created = await createInvestment(form)
      setStatus('success')
      setStatusMessage(`${form.name || form.ticker} was added to your portfolio.`)
      window.setTimeout(() => {
        onSuccess?.(created)
      }, 700)
    } catch (err) {
      setStatus('error')
      setStatusMessage(getApiErrorMessage(err, 'Could not save this investment.'))
    }
  }

  const isSubmitting = status === 'submitting'

  return (
    <div className="fixed inset-0 z-40 flex items-center justify-center bg-slate-950/40 p-4">
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
        aria-describedby={descriptionId}
        className="w-full max-w-xl rounded-[28px] border border-slate-200 bg-[#161616] p-5 shadow-2xl"
      >
        <div className="mb-4 flex items-center justify-between">
          <div>
            <div className="flex items-center gap-1.5">
              <h3 id={titleId} className="text-xl font-semibold text-slate-900">Add Investment</h3>
              <InfoTooltip title="Add investment" description="Use this form to add a new stock, bond, or crypto holding to your portfolio." />
            </div>
            <p id={descriptionId} className="text-sm text-slate-500">Saved directly to your portfolio database.</p>
          </div>
          <button type="button" onClick={onClose} aria-label="Close add investment dialog" className="rounded-full p-2 text-slate-500 hover:bg-slate-100">
            <X size={18} />
          </button>
        </div>

        <div className="mb-4 grid grid-cols-3 gap-2">
          {TYPE_OPTIONS.map(({ value, label, icon: Icon }) => (
            <button
              key={value}
              type="button"
              disabled={isSubmitting}
              onClick={() => handleTypeChange(value)}
              className={`flex items-center justify-center gap-2 rounded-2xl border px-3 py-2 text-sm font-semibold transition ${
                form.assetType === value
                  ? 'border-slate-900 bg-slate-900 text-white'
                  : 'border-slate-200 bg-white text-slate-600 hover:bg-slate-50'
              }`}
            >
              <Icon size={15} />
              {label}
            </button>
          ))}
        </div>

        <form className="grid gap-4 sm:grid-cols-2" onSubmit={handleSubmit}>
          <label className="relative text-sm font-medium text-slate-700 sm:col-span-2">
            <span className="inline-flex items-center gap-1.5">
              {searchLabel(form.assetType)}
              <InfoTooltip title="Instrument search" description="Type a company or asset name to find matching instruments and auto-fill details." />
            </span>
            <div className="relative mt-1">
              <Search size={14} className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
              <input
                name="name"
                value={form.name}
                onChange={handleChange}
                onFocus={() => setIsSuggestionsOpen(true)}
                onBlur={() => window.setTimeout(() => setIsSuggestionsOpen(false), 150)}
                autoComplete="off"
                placeholder={form.assetType === 'CRYPTO' ? 'e.g. Bitcoin, Ethereum' : form.assetType === 'BOND' ? 'e.g. US Treasury Bond' : 'e.g. Apple, Microsoft'}
                className={`w-full rounded-2xl border px-3 py-2 pl-9 text-sm ${errors.name ? 'border-rose-300' : 'border-slate-200'}`}
              />
            </div>
            {isSuggestionsOpen && suggestions.length > 0 ? (
              <div className="absolute left-0 right-0 top-[calc(100%+0.25rem)] z-10 max-h-48 overflow-auto rounded-2xl border border-slate-200 bg-white p-2 shadow-lg">
                {suggestions.map((item) => (
                  <button
                    key={item.ticker}
                    type="button"
                    onMouseDown={(event) => event.preventDefault()}
                    onClick={() => handleSelectSuggestion(item)}
                    className="flex w-full items-center justify-between rounded-xl px-3 py-2 text-left text-sm text-slate-600 hover:bg-slate-50"
                  >
                    <span><span className="font-semibold text-slate-900">{item.ticker}</span> · {item.name}</span>
                  </button>
                ))}
              </div>
            ) : null}
            {errors.name ? <p className="mt-1 text-xs text-rose-500">{errors.name}</p> : null}
          </label>

          <label className="text-sm font-medium text-slate-700">
            <span className="inline-flex items-center gap-1.5">
              {form.assetType === 'CRYPTO' ? 'Symbol' : 'Ticker Symbol'}
              <InfoTooltip title="Ticker symbol" description="Short exchange code for the asset, like AAPL or BTC." />
            </span>
            <input
              name="ticker"
              value={form.ticker}
              onChange={handleChange}
              onBlur={handleTickerBlur}
              placeholder={form.assetType === 'CRYPTO' ? 'e.g. BTC' : 'e.g. AAPL'}
              className={`mt-1 w-full rounded-2xl border px-3 py-2 text-sm uppercase ${errors.ticker ? 'border-rose-300' : 'border-slate-200'}`}
            />
            {errors.ticker ? <p className="mt-1 text-xs text-rose-500">{errors.ticker}</p> : null}
          </label>

          {form.assetType === 'STOCK' ? (
            <label className="text-sm font-medium text-slate-700">
              <span className="inline-flex items-center gap-1.5">
                Sector
                <InfoTooltip title="Sector" description="Industry group for the stock, useful for diversification and risk review." />
              </span>
              <input
                list="sector-suggestions"
                name="sector"
                value={form.sector}
                onChange={handleChange}
                placeholder="e.g. Technology"
                className={`mt-1 w-full rounded-2xl border px-3 py-2 text-sm ${errors.sector ? 'border-rose-300' : 'border-slate-200'}`}
              />
              <datalist id="sector-suggestions">
                {SECTOR_SUGGESTIONS.map((sector) => <option key={sector} value={sector} />)}
              </datalist>
              {errors.sector ? <p className="mt-1 text-xs text-rose-500">{errors.sector}</p> : null}
            </label>
          ) : null}

          {form.assetType === 'STOCK' && quote.status === 'loading' ? (
            <div role="status" aria-live="polite" className="sm:col-span-2 flex items-center gap-2 rounded-2xl border border-slate-200 bg-slate-50 px-3 py-2 text-xs text-slate-500">
              <Loader2 size={14} className="animate-spin" />
              Fetching latest market price...
            </div>
          ) : null}

          {form.assetType === 'STOCK' && quote.status === 'success' && quote.data ? (
            <div className="sm:col-span-2 rounded-2xl border border-emerald-100 bg-emerald-50 px-3 py-2 text-xs text-emerald-700">
              <p className="font-semibold">{quote.data.companyName || quote.data.symbol}</p>
              <p>
                {quote.data.symbol} · ${Number(quote.data.currentPrice).toFixed(2)}
                {quote.data.sector ? ` · ${quote.data.sector}` : ''}
              </p>
            </div>
          ) : null}

          {form.assetType === 'STOCK' && quote.status === 'error' ? (
            <div role="alert" className="sm:col-span-2 flex items-start gap-2 rounded-2xl border border-amber-200 bg-amber-50 px-3 py-2 text-xs text-amber-700">
              <AlertTriangle size={14} className="mt-0.5 shrink-0" />
              <span>{quote.error} You can enter the purchase price manually.</span>
            </div>
          ) : null}

          {form.assetType === 'BOND' ? (
            <label className="text-sm font-medium text-slate-700">
              <span className="inline-flex items-center gap-1.5">
                Issuer
                <InfoTooltip title="Bond issuer" description="Organization that issued the bond and is responsible for repayment." />
              </span>
              <input
                name="issuer"
                value={form.issuer}
                onChange={handleChange}
                placeholder="e.g. US Treasury"
                className={`mt-1 w-full rounded-2xl border px-3 py-2 text-sm ${errors.issuer ? 'border-rose-300' : 'border-slate-200'}`}
              />
              {errors.issuer ? <p className="mt-1 text-xs text-rose-500">{errors.issuer}</p> : null}
            </label>
          ) : null}

          <label className="text-sm font-medium text-slate-700">
            <span className="inline-flex items-center gap-1.5">
              {form.assetType === 'BOND' ? 'Quantity / Units' : 'Quantity'}
              <InfoTooltip title="Quantity" description="Number of units or shares purchased in this investment." />
            </span>
            <input
              type="number"
              min="1"
              step="1"
              name="quantity"
              value={form.quantity}
              onChange={handleChange}
              className={`mt-1 w-full rounded-2xl border px-3 py-2 text-sm ${errors.quantity ? 'border-rose-300' : 'border-slate-200'}`}
            />
            {errors.quantity ? <p className="mt-1 text-xs text-rose-500">{errors.quantity}</p> : null}
          </label>

          <label className="text-sm font-medium text-slate-700">
            <span className="inline-flex items-center gap-1.5">
              Purchase Price ($){form.assetType === 'STOCK' && quote.status === 'success' ? ' · auto-filled' : ''}
              <InfoTooltip title="Purchase price" description="Amount paid per unit when you bought the investment." />
            </span>
            <div className="relative mt-1">
              <input
                type="number"
                min="0.01"
                step="0.01"
                name="purchasePrice"
                value={form.purchasePrice}
                onChange={handleChange}
                readOnly={form.assetType === 'STOCK' && quote.status === 'success'}
                className={`w-full rounded-2xl border px-3 py-2 text-sm ${errors.purchasePrice ? 'border-rose-300' : 'border-slate-200'} ${form.assetType === 'STOCK' && quote.status === 'success' ? 'bg-slate-50 text-slate-500' : ''}`}
              />
              {form.assetType === 'STOCK' && quote.status === 'loading' ? (
                <Loader2 size={14} className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 animate-spin text-slate-400" />
              ) : null}
            </div>
            {errors.purchasePrice ? <p className="mt-1 text-xs text-rose-500">{errors.purchasePrice}</p> : null}
          </label>

          {form.assetType === 'BOND' ? (
            <label className="text-sm font-medium text-slate-700">
              <span className="inline-flex items-center gap-1.5">
                Interest Rate % (if available)
                <InfoTooltip title="Interest rate" description="Coupon rate for the bond, expressed as an annual percentage." />
              </span>
              <input
                type="number"
                min="0"
                max="100"
                step="0.01"
                name="interestRate"
                value={form.interestRate}
                onChange={handleChange}
                className={`mt-1 w-full rounded-2xl border px-3 py-2 text-sm ${errors.interestRate ? 'border-rose-300' : 'border-slate-200'}`}
              />
              {errors.interestRate ? <p className="mt-1 text-xs text-rose-500">{errors.interestRate}</p> : null}
            </label>
          ) : null}

          {form.assetType === 'BOND' ? (
            <label className="text-sm font-medium text-slate-700">
              <span className="inline-flex items-center gap-1.5">
                Maturity Date
                <InfoTooltip title="Maturity date" description="Date when the bond principal is expected to be repaid." />
              </span>
              <input
                type="date"
                name="maturityDate"
                value={form.maturityDate}
                onChange={handleChange}
                className={`mt-1 w-full rounded-2xl border px-3 py-2 text-sm ${errors.maturityDate ? 'border-rose-300' : 'border-slate-200'}`}
              />
              {errors.maturityDate ? <p className="mt-1 text-xs text-rose-500">{errors.maturityDate}</p> : null}
            </label>
          ) : null}

          <label className="text-sm font-medium text-slate-700">
            <span className="inline-flex items-center gap-1.5">
              Purchase Date
              <InfoTooltip title="Purchase date" description="Date when you bought this investment. It affects return and tax calculations." />
            </span>
            <input
              type="date"
              max={today()}
              name="purchaseDate"
              value={form.purchaseDate}
              onChange={handleChange}
              className={`mt-1 w-full rounded-2xl border px-3 py-2 text-sm ${errors.purchaseDate ? 'border-rose-300' : 'border-slate-200'}`}
            />
            {errors.purchaseDate ? <p className="mt-1 text-xs text-rose-500">{errors.purchaseDate}</p> : null}
          </label>

          {statusMessage ? (
            <div
              role={status === 'error' ? 'alert' : 'status'}
              aria-live={status === 'error' ? undefined : 'polite'}
              className={`sm:col-span-2 flex items-center gap-2 rounded-2xl px-3 py-2 text-sm font-medium ${
                status === 'success' ? 'bg-emerald-50 text-emerald-700' : 'bg-rose-50 text-rose-600'
              }`}
            >
              {status === 'success' ? <CheckCircle2 size={16} /> : <XCircle size={16} />}
              {statusMessage}
            </div>
          ) : null}

          <div className="sm:col-span-2 flex justify-end gap-3 pt-2">
            <button
              type="button"
              onClick={onClose}
              disabled={isSubmitting}
              className="rounded-2xl border border-slate-200 px-4 py-2 text-sm font-medium text-slate-600 disabled:opacity-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting || status === 'success'}
              className="inline-flex items-center gap-2 rounded-2xl bg-slate-900 px-4 py-2 text-sm font-semibold text-white disabled:opacity-60"
            >
              {isSubmitting ? <Loader2 size={15} className="animate-spin" /> : null}
              {isSubmitting ? 'Saving...' : 'Save Investment'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
