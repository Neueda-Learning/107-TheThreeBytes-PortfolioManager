import InfoTooltip from './InfoTooltip'

export default function SectionHeader({ title, description, countLabel, actions, info }) {
  return (
    <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
      <div>
        <div className="flex items-center gap-1.5">
          <h2 className="pm-display text-xl font-semibold text-slate-900">{title}</h2>
          {info ? <InfoTooltip {...info} /> : null}
        </div>
        {description ? <p className="text-sm text-slate-600">{description}</p> : null}
      </div>
      <div className="flex items-center gap-3">
        {countLabel !== undefined && countLabel !== null ? (
          <div className="rounded-full bg-slate-100 px-3 py-1 text-sm font-medium text-slate-600">
            {countLabel}
          </div>
        ) : null}
        {actions}
      </div>
    </div>
  )
}
