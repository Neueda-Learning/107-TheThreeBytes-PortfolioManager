import { RefreshCw } from 'lucide-react'

export default function RefreshAction({ onClick, loading = false, label = 'Refresh', disabled = false }) {
  return (
    <button
      type="button"
      onClick={onClick}
      disabled={disabled || loading}
      className="inline-flex items-center gap-2 rounded-2xl border border-slate-200 bg-white px-3 py-2 text-sm font-semibold text-slate-700 transition hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-70"
    >
      <RefreshCw size={14} className={loading ? 'animate-spin' : ''} />
      {label}
    </button>
  )
}
