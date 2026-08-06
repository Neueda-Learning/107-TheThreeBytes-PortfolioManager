import InfoTooltip from './InfoTooltip'

export default function DataTableShell({ headers, hasRows, emptyMessage, colSpan, children }) {
  return (
    <div className="overflow-x-auto">
      <table className="min-w-full divide-y divide-slate-200 text-left text-sm">
        <thead className="bg-slate-50">
          <tr>
            {headers.map((header) => {
              const key = typeof header === 'string' ? header : header.key
              const label = typeof header === 'string' ? header : header.label
              const info = typeof header === 'string' ? null : header.info
              return (
                <th key={key} className="px-4 py-3 font-semibold text-slate-700">
                  <span className="inline-flex items-center gap-1.5">
                    <span>{label}</span>
                    {info ? <InfoTooltip {...info} /> : null}
                  </span>
                </th>
              )
            })}
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100 bg-white">
          {hasRows ? children : (
            <tr>
              <td colSpan={colSpan || headers.length} className="px-4 py-10 text-center text-slate-500">
                {emptyMessage}
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  )
}
