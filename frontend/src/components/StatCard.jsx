import PageCard from './PageCard'
import InfoTooltip from './InfoTooltip'

const TONE_CLASS = {
  slate: 'text-slate-900',
  positive: 'text-emerald-400',
  negative: 'text-rose-400',
}

export default function StatCard({ title, value, icon: Icon, tone = 'slate', subtle = false, info }) {
  const toneClass = TONE_CLASS[tone] || TONE_CLASS.slate

  if (subtle) {
    return (
      <div className="pm-glass-card rounded-[20px] border border-slate-200 bg-slate-50 p-4 shadow-[0_8px_20px_rgba(15,23,42,0.03)]">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-1.5">
            <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">{title}</p>
            {info ? <InfoTooltip {...info} /> : null}
          </div>
          {Icon ? <Icon size={18} className="text-slate-400" /> : null}
        </div>
        <p className={`pm-display mt-3 text-xl font-semibold ${toneClass}`}>{value}</p>
      </div>
    )
  }

  return (
    <PageCard className="p-5">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-1.5">
          <p className="text-sm font-medium text-slate-500">{title}</p>
          {info ? <InfoTooltip {...info} /> : null}
        </div>
        {Icon ? <Icon size={18} className="text-slate-400" /> : null}
      </div>
      <p className={`pm-display mt-4 text-2xl font-semibold ${toneClass}`}>{value}</p>
    </PageCard>
  )
}
