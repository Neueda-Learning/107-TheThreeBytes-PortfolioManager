import { AlertTriangle } from 'lucide-react'

export default function ConfirmDialog({ isOpen, title, message, onCancel, onConfirm }) {
  if (!isOpen) return null
  const titleId = `confirm-dialog-title-${title.toLowerCase().replace(/[^a-z0-9]+/g, '-')}`
  const messageId = `confirm-dialog-message-${title.toLowerCase().replace(/[^a-z0-9]+/g, '-')}`

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/40 p-4">
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
        aria-describedby={messageId}
        className="w-full max-w-md rounded-[28px] border border-slate-200 bg-white p-5 shadow-2xl"
      >
        <div className="flex items-start gap-3">
          <div className="rounded-2xl bg-rose-50 p-2 text-rose-600">
            <AlertTriangle size={18} />
          </div>
          <div>
            <h3 id={titleId} className="text-lg font-semibold text-slate-900">{title}</h3>
            <p id={messageId} className="mt-1 text-sm text-slate-600">{message}</p>
          </div>
        </div>
        <div className="mt-5 flex justify-end gap-3">
          <button type="button" onClick={onCancel} className="rounded-2xl border border-slate-200 px-4 py-2 text-sm font-medium text-slate-600">Cancel</button>
          <button type="button" onClick={onConfirm} className="rounded-2xl bg-rose-600 px-4 py-2 text-sm font-semibold text-white">Delete</button>
        </div>
      </div>
    </div>
  )
}
