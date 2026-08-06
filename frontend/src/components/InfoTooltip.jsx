import { useEffect, useId, useRef, useState } from 'react'
import { Info, X } from 'lucide-react'

export default function InfoTooltip({ title, description, learnMoreLink }) {
  const [isOpen, setIsOpen] = useState(false)
  const containerRef = useRef(null)
  const tooltipId = useId()

  useEffect(() => {
    if (!isOpen) return undefined

    const handlePointerDown = (event) => {
      if (!containerRef.current?.contains(event.target)) {
        setIsOpen(false)
      }
    }

    const handleEscape = (event) => {
      if (event.key === 'Escape') {
        setIsOpen(false)
      }
    }

    document.addEventListener('mousedown', handlePointerDown)
    document.addEventListener('touchstart', handlePointerDown)
    document.addEventListener('keydown', handleEscape)
    return () => {
      document.removeEventListener('mousedown', handlePointerDown)
      document.removeEventListener('touchstart', handlePointerDown)
      document.removeEventListener('keydown', handleEscape)
    }
  }, [isOpen])

  return (
    <span
      ref={containerRef}
      className="group/info relative inline-flex items-center"
      onMouseEnter={() => setIsOpen(true)}
      onMouseLeave={() => setIsOpen(false)}
    >
      <button
        type="button"
        aria-label={`More info about ${title}`}
        aria-expanded={isOpen}
        aria-describedby={isOpen ? tooltipId : undefined}
        onClick={(event) => {
          event.stopPropagation()
          setIsOpen((open) => !open)
        }}
        onFocus={() => setIsOpen(true)}
        onBlur={(event) => {
          if (!containerRef.current?.contains(event.relatedTarget)) {
            setIsOpen(false)
          }
        }}
        className="inline-flex h-5 w-5 items-center justify-center rounded-full text-slate-500 transition hover:bg-slate-200 hover:text-slate-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-400"
      >
        <Info size={14} />
      </button>

      {isOpen ? (
        <span
          id={tooltipId}
          role="tooltip"
          className="absolute left-1/2 top-[calc(100%+8px)] z-30 w-64 -translate-x-1/2 rounded-xl border border-slate-200 bg-white p-3 text-left shadow-lg"
        >
          <span className="mb-1 block text-xs font-semibold text-slate-900">{title}</span>
          <span className="block text-xs leading-relaxed text-slate-700">{description}</span>
          {learnMoreLink ? (
            <a
              href={learnMoreLink}
              target="_blank"
              rel="noreferrer noopener"
              className="mt-2 inline-flex text-xs font-semibold text-emerald-400 hover:text-emerald-300"
            >
              Learn more
            </a>
          ) : null}
          <button
            type="button"
            onClick={() => setIsOpen(false)}
            aria-label={`Dismiss info about ${title}`}
            className="mt-2 inline-flex items-center gap-1 rounded-md border border-slate-200 px-2 py-1 text-xs font-medium text-slate-600 hover:bg-slate-100 sm:hidden"
          >
            <X size={12} />
            Dismiss
          </button>
        </span>
      ) : null}
    </span>
  )
}
