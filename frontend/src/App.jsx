import { useEffect, useState } from 'react'
import AuthPage from './pages/AuthPage.jsx'
import DashboardPage from './pages/DashboardPage.jsx'
import TelegramLinkPage from './pages/TelegramLinkPage.jsx'

const USER_STORAGE_KEY = 'kakebot-current-user'

function readStoredUser() {
  try {
    const rawUser = window.localStorage.getItem(USER_STORAGE_KEY)

    if (!rawUser) {
      return null
    }

    return JSON.parse(rawUser)
  } catch {
    return null
  }
}

function App() {
  const [screen, setScreen] = useState(() => (readStoredUser() ? 'dashboard' : 'auth'))
  const [telegramLinkData, setTelegramLinkData] = useState(null)
  const [currentUser, setCurrentUser] = useState(() => readStoredUser())

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

  useEffect(() => {
    if (currentUser) {
      window.localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(currentUser))
      return
    }

    window.localStorage.removeItem(USER_STORAGE_KEY)
  }, [currentUser])

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
