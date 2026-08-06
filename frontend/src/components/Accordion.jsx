import { ChevronDown } from 'lucide-react'

export default function Accordion({ items, openKeys = [], onToggle }) {
  return (
    <div className="space-y-2.5">
      {items.map(({ key, title, content }) => {
        const isOpen = openKeys.includes(key)
        const buttonId = `accordion-button-${key}`
        const panelId = `accordion-panel-${key}`
        return (
          <div key={key} className="overflow-hidden rounded-2xl border border-slate-200 bg-slate-50">
            <button
              id={buttonId}
              type="button"
              onClick={() => onToggle(key)}
              aria-expanded={isOpen}
              aria-controls={panelId}
              className="flex w-full items-center justify-between gap-3 px-4 py-3 text-left text-slate-800 transition hover:bg-slate-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-400"
            >
              <span className="text-sm font-semibold">{title}</span>
              <ChevronDown size={16} className={`shrink-0 text-slate-600 transition-transform ${isOpen ? 'rotate-180' : 'rotate-0'}`} />
            </button>
            {isOpen ? (
              <div id={panelId} role="region" aria-labelledby={buttonId} className="border-t border-slate-200 px-4 py-3 text-sm text-slate-700">
                {content}
              </div>
            ) : null}
          </div>
        )
      })}
    </div>
  )
}
