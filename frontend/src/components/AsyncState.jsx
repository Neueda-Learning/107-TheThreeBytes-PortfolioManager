export default function AsyncState({
  loading,
  error,
  isEmpty = false,
  loadingMessage = 'Loading...',
  emptyMessage = 'No data available.',
  onRetry,
  retryLabel = 'Try again',
}) {
  if (loading) {
    return (
      <p role="status" aria-live="polite" className="py-10 text-center text-sm text-slate-500">
        {loadingMessage}
      </p>
    )
  }

  if (error) {
    return (
      <div className="py-10 text-center">
        <p role="alert" className="text-sm text-rose-500">{error}</p>
        {onRetry ? (
          <button
            type="button"
            onClick={onRetry}
            className="mt-3 rounded-2xl border border-slate-200 bg-white px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-100"
          >
            {retryLabel}
          </button>
        ) : null}
      </div>
    )
  }

  if (isEmpty) {
    return <p className="py-10 text-center text-sm text-slate-500">{emptyMessage}</p>
  }

  return null
}
