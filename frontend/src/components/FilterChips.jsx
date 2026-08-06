import InfoTooltip from './InfoTooltip'

export default function FilterChips({ options, activeValue, onChange, formatLabel, label, info }) {
  return (
    <div>
      {label ? (
        <div className="mb-2 flex items-center gap-1.5">
          <p className="text-xs font-semibold uppercase tracking-[0.2em] text-slate-500">{label}</p>
          {info ? <InfoTooltip {...info} /> : null}
        </div>
      ) : null}
      <div className="flex flex-wrap gap-2">
        {options.map((option) => {
          const active = option === activeValue
          const optionLabel = formatLabel ? formatLabel(option) : option
          return (
            <button
              key={option}
              type="button"
              onClick={() => onChange(option)}
              className={`rounded-full px-3 py-2 text-sm font-medium transition ${active ? 'bg-slate-900 text-white' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'}`}
            >
              {optionLabel}
            </button>
          )
        })}
      </div>
    </div>
  )
}
