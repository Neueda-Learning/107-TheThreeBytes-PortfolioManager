export const currency = new Intl.NumberFormat('en-US', {
  style: 'currency',
  currency: 'USD',
  maximumFractionDigits: 2,
})

export function formatPercent(value) {
  if (value === undefined || value === null || Number.isNaN(Number(value))) {
    return '—'
  }
  return `${Number(value) >= 0 ? '+' : ''}${Number(value).toFixed(2)}%`
}
