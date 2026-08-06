import { useEffect, useState } from 'react'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import AppShell from './components/AppShell'
import InvestmentModal from './components/InvestmentModal'
import Overview from './pages/Overview'
import Dashboard from './pages/Dashboard'
import Holdings from './pages/Holdings'
import Transactions from './pages/Transactions'
import Watchlist from './pages/Watchlist'
import Dividends from './pages/Dividends'
import Tax from './pages/Tax'
import Profile from './pages/Profile'
import Support from './pages/Support'
import { getApiErrorMessage, getInvestments } from './services/investmentService'

function App() {
  const [searchQuery, setSearchQuery] = useState('')
  const [holdings, setHoldings] = useState([])
  const [isSidebarOpen, setIsSidebarOpen] = useState(false)
  const [isAddModalOpen, setIsAddModalOpen] = useState(false)
  const [refreshToken, setRefreshToken] = useState(0)

  useEffect(() => {
    getInvestments()
      .then(setHoldings)
      .catch((err) => console.error(getApiErrorMessage(err, 'Could not load holdings for search.')))
  }, [refreshToken])

  const handleInvestmentAdded = () => {
    setIsAddModalOpen(false)
    setRefreshToken((token) => token + 1)
  }

  return (
    <BrowserRouter>
      <AppShell
        searchQuery={searchQuery}
        setSearchQuery={setSearchQuery}
        holdings={holdings}
        isSidebarOpen={isSidebarOpen}
        setIsSidebarOpen={setIsSidebarOpen}
        onAddInvestment={() => setIsAddModalOpen(true)}
      >
        <Routes>
          <Route path="/" element={<Overview searchQuery={searchQuery} onSearch={setSearchQuery} refreshToken={refreshToken} />} />
          <Route path="/dashboard" element={<Dashboard refreshToken={refreshToken} />} />
          <Route path="/dashboard/:assetType" element={<Dashboard refreshToken={refreshToken} />} />
          <Route path="/holdings" element={<Holdings refreshToken={refreshToken} />} />
          <Route path="/transactions" element={<Transactions />} />
          <Route path="/watchlist" element={<Watchlist />} />
          <Route path="/dividends" element={<Dividends />} />
          <Route path="/tax" element={<Tax />} />
          <Route path="/profile" element={<Profile />} />
          <Route path="/support" element={<Support />} />
        </Routes>
      </AppShell>

      <InvestmentModal
        isOpen={isAddModalOpen}
        onClose={() => setIsAddModalOpen(false)}
        onSuccess={handleInvestmentAdded}
      />
    </BrowserRouter>
  )
}

export default App
