import { useMemo, useState } from 'react'
import { ChevronDown, MoreHorizontal, Plus, Search, UserCircle2 } from 'lucide-react'
import { useNavigate } from 'react-router-dom'

export default function TopNavBar({ holdings, onAddInvestment, onSearch }) {
  const navigate = useNavigate()
  const [query, setQuery] = useState('')
  const [isProfileOpen, setIsProfileOpen] = useState(false)
  const [isMoreOpen, setIsMoreOpen] = useState(false)

  const suggestions = useMemo(() => {
    const value = query.trim().toLowerCase()
    if (!value) return []
    return holdings.filter((item) => {
      const searchText = `${item.ticker} ${item.name || ''} ${item.assetType}`.toLowerCase()
      return searchText.includes(value)
    }).slice(0, 5)
  }, [holdings, query])

  const handleSelectSuggestion = (item) => {
    setQuery(item.ticker)
    onSearch?.(item.ticker)
  }

  return (
    <div className="w-full border-b border-slate-200/80 bg-white/95 px-3 py-2.5 backdrop-blur-sm sm:px-4 lg:px-6">
      <div className="flex w-full items-center justify-between gap-2.5">
        <div className="flex min-w-0 items-center gap-3">
          <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-emerald-700 text-[11px] font-semibold text-white">
            PM
          </div>
          <div className="min-w-0">
            <p className="truncate text-[10px] font-semibold uppercase tracking-[0.28em] text-slate-400">Portfolio Manager</p>
            <h1 className="pm-display truncate text-sm font-semibold text-slate-900">Portfolio Manager</h1>
          </div>
        </div>

{/*         <div className="hidden flex-1 max-w-xl sm:block"> */}
{/*           <div className="relative"> */}
{/*             <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" /> */}
{/*             <input */}
{/*               aria-label="Search investments" */}
{/*               value={query} */}
{/*               onChange={(event) => { */}
{/*                 const nextValue = event.target.value */}
{/*                 setQuery(nextValue) */}
{/*                 onSearch?.(nextValue) */}
{/*               }} */}
{/*               placeholder="Search stocks, bonds or crypto" */}
{/*               className="pm-control-input w-full rounded-lg py-2 pl-9 pr-3 text-sm outline-none ring-0 transition" */}
{/*             /> */}
{/*             {suggestions.length > 0 ? ( */}
{/*               <div className="absolute left-0 right-0 top-[calc(100%+0.35rem)] rounded-lg border border-slate-200 bg-white p-2 shadow-sm"> */}
{/*                 {suggestions.map((item) => ( */}
{/*                   <button */}
{/*                     key={item.id} */}
{/*                     type="button" */}
{/*                     onClick={() => handleSelectSuggestion(item)} */}
{/*                     className="flex w-full items-center justify-between rounded-md px-3 py-2 text-left text-sm text-slate-600 transition hover:bg-slate-50" */}
{/*                   > */}
{/*                     <span> */}
{/*                       <span className="font-medium text-slate-900">{item.ticker}</span> · {item.name || 'Unnamed holding'} */}
{/*                     </span> */}
{/*                     <span className="text-xs uppercase tracking-[0.2em] text-slate-400">{item.assetType}</span> */}
{/*                   </button> */}
{/*                 ))} */}
{/*               </div> */}
{/*             ) : null} */}
{/*           </div> */}
{/*         </div> */}

        <div className="flex flex-1 items-center justify-end gap-2 sm:gap-3">
          <div className="flex-1 sm:hidden">
            <div className="relative">
              <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
              <input
                aria-label="Search investments"
                value={query}
                onChange={(event) => {
                  const nextValue = event.target.value
                  setQuery(nextValue)
                  onSearch?.(nextValue)
                }}
                placeholder="Search"
                className="pm-control-input w-full rounded-lg py-2 pl-9 pr-3 text-sm outline-none ring-0 transition"
              />
            </div>
          </div>

          <div className="relative sm:hidden">
            <button
              type="button"
              onClick={() => setIsMoreOpen((open) => !open)}
              aria-label="Open more actions menu"
              aria-expanded={isMoreOpen}
              aria-haspopup="menu"
              className="flex items-center justify-center rounded-lg border border-slate-200/80 bg-white p-2 text-slate-600"
            >
              <MoreHorizontal size={18} />
            </button>
            {isMoreOpen ? (
              <div className="absolute right-0 mt-2 w-44 rounded-lg border border-slate-200 bg-white p-2 shadow-sm">
                <button type="button" onClick={() => { navigate('/market-prices'); setIsMoreOpen(false) }} className="flex w-full items-center rounded-md px-3 py-2 text-left text-sm text-slate-600 hover:bg-slate-50">Market Prices</button>
                <button type="button" onClick={() => { navigate('/watchlist'); setIsMoreOpen(false) }} className="flex w-full items-center rounded-md px-3 py-2 text-left text-sm text-slate-600 hover:bg-slate-50">View Watchlist</button>
                <button type="button" onClick={() => { onAddInvestment?.(); setIsMoreOpen(false) }} className="flex w-full items-center rounded-md bg-emerald-800 px-3 py-2 text-left text-sm font-semibold text-white hover:bg-emerald-800">Add Investment</button>
              </div>
            ) : null}
          </div>

          <div className="hidden items-center gap-2 sm:flex">
            <button
              type="button"
              onClick={() => navigate('/market-prices')}
              className="pm-control rounded-lg px-3 py-2 text-sm font-medium transition"
            >
              Market Prices
            </button>
            <button
              type="button"
              onClick={() => navigate('/watchlist')}
              className="pm-control rounded-lg px-3 py-2 text-sm font-medium transition"
            >
              View Watchlist
            </button>
            <button
              type="button"
              onClick={onAddInvestment}
              className="inline-flex items-center gap-2 rounded-lg bg-emerald-700 px-3.5 py-2 text-sm font-semibold text-white transition hover:bg-slate-800"
            >
              <Plus size={16} />
              Add Investment
            </button>
          </div>

          <div className="relative">
            <button
              type="button"
              onClick={() => setIsProfileOpen((open) => !open)}
              aria-label="Open profile menu"
              aria-expanded={isProfileOpen}
              aria-haspopup="menu"
              className="flex items-center gap-2 rounded-lg border border-slate-200/80 bg-white p-2 text-sm font-medium text-slate-700"
            >
              <UserCircle2 size={20} />
              <ChevronDown size={15} />
            </button>
            {isProfileOpen ? (
              <div className="absolute right-0 mt-2 w-44 rounded-lg border border-slate-200 bg-white p-2 shadow-sm">
                <button type="button" onClick={() => { navigate('/profile'); setIsProfileOpen(false) }} className="flex w-full items-center rounded-md px-3 py-2 text-left text-sm text-slate-600 hover:bg-slate-50">View Profile</button>
              </div>
            ) : null}
          </div>
        </div>
      </div>
    </div>
  )
}
