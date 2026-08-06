import { TrendingUp, TrendingDown } from 'lucide-react'
import InfoTooltip from './InfoTooltip'

export default function SummaryCard({ title, value, change, icon: Icon, info }) {
  const positive = Number(change || 0) >= 0

  return (
    <div className="pm-glass-card rounded-[24px] border border-slate-200 bg-white p-5 shadow-[0_10px_30px_rgba(15,23,42,0.05)]">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-1.5">
          <p className="text-sm font-medium text-slate-500">{title}</p>
          {info ? <InfoTooltip {...info} /> : null}
        </div>
        {Icon ? <Icon className="text-slate-400" size={18} /> : null}
      </div>
      <p className="pm-display mt-5 text-2xl font-semibold text-slate-900">{value}</p>
      {change !== undefined && change !== null ? (
        <div className={`mt-3 inline-flex items-center gap-1 rounded-full px-2.5 py-1 text-sm ${positive ? 'bg-emerald-50 text-emerald-600' : 'bg-rose-50 text-rose-600'}`}>
          {positive ? <TrendingUp size={14} /> : <TrendingDown size={14} />}
          <span>{Math.abs(Number(change)).toFixed(2)}%</span>
        </div>
      ) : null}
    </div>
  )
}
