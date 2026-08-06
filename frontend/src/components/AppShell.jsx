import { Menu, X } from 'lucide-react'
import { useState } from 'react'
import Sidebar from './Sidebar'
import TopNavBar from './TopNavBar'

function AppShell({ searchQuery: _searchQuery, setSearchQuery, holdings, isSidebarOpen, setIsSidebarOpen, onAddInvestment, children }) {
  const [isDesktopSidebarCollapsed, setIsDesktopSidebarCollapsed] = useState(true)

  return (
    <div className="min-h-screen bg-[radial-gradient(circle_at_top_left,_rgba(127,255,60,0.06),_transparent_34%),linear-gradient(180deg,_#0a0a0a_0%,_#0f0f0f_100%)] text-slate-800">
      <div className="mx-auto flex min-h-screen w-full max-w-[1800px] flex-col overflow-hidden">
        <header className="sticky top-0 z-40 border-b border-slate-200/80 bg-white/95 backdrop-blur-sm">
          <div className="flex items-center justify-between gap-2 px-3 py-2.5 sm:px-4 lg:px-6">
            <button
              type="button"
              onClick={() => setIsSidebarOpen(true)}
              aria-label="Open sidebar navigation"
              className="rounded-lg border border-slate-200/80 p-2 text-slate-600 hover:bg-slate-100 lg:hidden"
            >
              <Menu size={18} />
            </button>
            <div className="flex-1">
              <TopNavBar holdings={holdings} onSearch={setSearchQuery} onAddInvestment={onAddInvestment} />
            </div>
          </div>
        </header>

        {isSidebarOpen ? (
          <button
            type="button"
            aria-label="Close sidebar navigation"
            className="fixed inset-0 z-40 bg-slate-950/30 lg:hidden"
            onClick={() => setIsSidebarOpen(false)}
          />
        ) : null}

        <div className="flex min-h-0 flex-1 flex-col lg:flex-row">
          <aside
            className={`hidden shrink-0 border-r border-slate-200/80 bg-white/80 backdrop-blur-sm transition-[width,padding] duration-200 lg:flex ${
              isDesktopSidebarCollapsed ? 'w-[72px] px-2 py-3' : 'w-[272px] px-5 py-5'
            }`}
          >
            <Sidebar
              collapsible
              collapsed={isDesktopSidebarCollapsed}
              onToggleCollapse={() => setIsDesktopSidebarCollapsed((value) => !value)}
            />
          </aside>

          <aside className={`fixed inset-y-0 left-0 z-50 w-[85%] max-w-[280px] shrink-0 border-r border-slate-200/80 bg-white/95 p-4 shadow-xl backdrop-blur-sm transition-transform duration-200 lg:hidden ${isSidebarOpen ? 'translate-x-0' : '-translate-x-full'}`}>
            <div className="mb-4 flex items-center justify-end">
              <button
                type="button"
                onClick={() => setIsSidebarOpen(false)}
                aria-label="Close sidebar navigation"
                className="rounded-lg p-2 text-slate-600 hover:bg-slate-100"
              >
                <X size={18} />
              </button>
            </div>
            <Sidebar />
          </aside>

          <main className="flex-1 overflow-y-auto px-4 py-4 sm:px-6 sm:py-6 lg:px-8 lg:py-8">
            <div className="mb-6 flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
              <div>
                <p className="text-sm font-medium uppercase tracking-[0.32em] text-slate-400">Portfolio overview</p>
                <h1 className="pm-display text-3xl font-semibold text-slate-900 sm:text-4xl">Modern dashboard for your investments</h1>
              </div>

            </div>

            {children}
          </main>
        </div>
      </div>
    </div>
  )
}

export default AppShell
