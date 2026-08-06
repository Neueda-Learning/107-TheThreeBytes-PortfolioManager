import { AlertTriangle } from 'lucide-react'

export default function ConfirmDialog({ isOpen, title, message, onCancel, onConfirm }) {
  if (!isOpen) return null

  const titleId = `confirm-dialog-title-${title.toLowerCase().replace(/[^a-z0-9]+/g, '-')}`
  const messageId = `confirm-dialog-message-${title.toLowerCase().replace(/[^a-z0-9]+/g, '-')}`

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/75 p-4 backdrop-blur-sm">
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
        aria-describedby={messageId}
        className="w-full max-w-md rounded-[28px] border border-slate-700 bg-[#1b1b1b] p-6 shadow-[0_20px_60px_rgba(0,0,0,0.65)]"
      >
        <div className="flex items-start gap-4">
          <div className="rounded-2xl bg-rose-900/30 p-3 text-rose-400">
            <AlertTriangle size={20} />
          </div>

          <div className="flex-1">
            <h3
              id={titleId}
              className="text-lg font-semibold text-white"
            >
              {title}
            </h3>

            <p
              id={messageId}
              className="mt-2 text-sm leading-6 text-slate-300"
            >
              {message}
            </p>
          </div>
        </div>

        <div className="mt-6 flex justify-end gap-3">
          <button
            type="button"
            onClick={onCancel}
            className="rounded-2xl border border-slate-600 bg-slate-800 px-4 py-2 text-sm font-medium text-slate-200 transition-colors hover:bg-slate-700"
          >
            Cancel
          </button>

          <button
            type="button"
            onClick={onConfirm}
            className="rounded-2xl bg-rose-600 px-4 py-2 text-sm font-semibold text-white transition-colors hover:bg-rose-700"
          >
            Sell Holding
          </button>
        </div>
      </div>
    </div>
  )
}