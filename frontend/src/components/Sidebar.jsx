import {
  LayoutDashboard,
  Landmark,
  ArrowLeftRight,
  Eye,
  UserCircle,
  Calculator,
  BarChart3,
  LineChart,
  Coins,
  MessageSquareText,
  BriefcaseBusiness,
  PanelLeftClose,
  PanelLeftOpen,
} from 'lucide-react'
import { NavLink } from 'react-router-dom'

const navigationItems = [
  { name: 'Overview', path: '/', icon: LayoutDashboard },
  { name: 'Dashboard', path: '/dashboard', icon: BarChart3 },
  { name: 'Holdings', path: '/holdings', icon: Landmark },
  { name: 'Transactions', path: '/transactions', icon: ArrowLeftRight },
  { name: 'Watchlist', path: '/watchlist', icon: Eye },
  { name: 'Dividends', path: '/dividends', icon: Coins },
  { name: 'Market Prices', path: '/market-prices', icon: LineChart },
  { name: 'Tax', path: '/tax', icon: Calculator },
  { name: 'Profile', path: '/profile', icon: UserCircle },
  { name: 'Support', path: '/support', icon: MessageSquareText },
]

export default function Sidebar({ collapsible = false, collapsed = false, onToggleCollapse }) {
  return (
    <aside className={`flex h-full w-full flex-col justify-between rounded-[28px] border border-slate-200 bg-white/80 shadow-[0_10px_30px_rgba(15,23,42,0.06)] backdrop-blur ${collapsible ? 'p-3' : 'p-5'}`}>
      <div>
        <div className={`mb-8 ${collapsible && collapsed ? 'flex flex-col items-center gap-2' : 'flex items-center justify-between'}`}>
          {collapsible && collapsed ? (
            <button
              type="button"
              onClick={onToggleCollapse}
              aria-label="Open sidebar"
              title="Open sidebar"
              className="rounded-lg p-2 text-slate-500 transition hover:bg-slate-100 hover:text-slate-800"
            >
              <PanelLeftOpen size={16} />
            </button>
          ) : null}
          <div className="flex items-center">
            <div className="rounded-xl border border-slate-200 bg-white p-2 text-slate-600">
              <BriefcaseBusiness size={18} />
            </div>
            <div className={`${collapsible && collapsed ? 'ml-0 max-w-0 overflow-hidden opacity-0' : 'ml-3 max-w-[160px] opacity-100'} transition-all duration-200`}>
              <p className="text-xs font-semibold uppercase tracking-[0.28em] text-slate-400">Portfolio</p>
              <h2 className="pm-display mt-1 text-xl font-semibold text-slate-900">Manager</h2>
            </div>
          </div>
          {collapsible && !collapsed ? (
            <button
              type="button"
              onClick={onToggleCollapse}
              aria-label="Close sidebar"
              title="Close sidebar"
              className="rounded-lg p-2 text-slate-500 transition hover:bg-slate-100 hover:text-slate-800"
            >
              <PanelLeftClose size={16} />
            </button>
          ) : null}
        </div>

        <nav className="space-y-2">
          {navigationItems.map(({ name, path, icon: Icon }) => (
            <NavLink
              key={path}
              to={path}
              className={({ isActive }) =>
                [
                  `group relative flex items-center rounded-2xl py-3 text-sm font-medium transition-all ${collapsible && collapsed ? 'justify-center px-2' : 'gap-3 px-3'}`,
                  isActive
                    ? 'bg-slate-900 text-white'
                    : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900',
                ].join(' ')
              }
            >
              <Icon size={18} className="shrink-0" />
              <span className={`${collapsible && collapsed ? 'max-w-0 overflow-hidden whitespace-nowrap opacity-0' : 'opacity-100'} transition-all duration-200`}>
                {name}
              </span>
              {collapsible && collapsed ? (
                <span className="pointer-events-none absolute left-[calc(100%+10px)] top-1/2 z-10 hidden -translate-y-1/2 whitespace-nowrap rounded-md border border-slate-200 bg-white px-2 py-1 text-xs font-medium text-slate-700 shadow-sm group-hover:block">
                  {name}
                </span>
              ) : null}
            </NavLink>
          ))}
        </nav>
      </div>


    </aside>
  )
}
