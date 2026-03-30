import { useState } from 'react'
import AuthPage from './pages/AuthPage.jsx'
import DashboardPage from './pages/DashboardPage.jsx'
import TelegramLinkPage from './pages/TelegramLinkPage.jsx'

function App() {
  const [screen, setScreen] = useState('auth')
  const [telegramLinkData, setTelegramLinkData] = useState(null)
  const [currentUser, setCurrentUser] = useState(null)

  const handleRegisterSuccess = (data) => {
    setTelegramLinkData(data)
    setScreen('telegram-link')
  }

  const handleLoginSuccess = (user) => {
    setCurrentUser(user)
    setScreen('dashboard')
  }

  const handleBackToAuth = () => {
    setScreen('auth')
  }

  const handleLogout = () => {
    setCurrentUser(null)
    setScreen('auth')
  }

  if (screen === 'dashboard' && currentUser) {
    return <DashboardPage user={currentUser} onLogout={handleLogout} />
  }

  if (screen === 'telegram-link' && telegramLinkData) {
    return (
      <TelegramLinkPage
        code={telegramLinkData.code}
        expiresAt={telegramLinkData.expiresAt}
        username={telegramLinkData.username}
        onBackToAuth={handleBackToAuth}
      />
    )
  }

  return (
    <AuthPage
      onLoginSuccess={handleLoginSuccess}
      onRegisterSuccess={handleRegisterSuccess}
    />
  )
}

export default App
