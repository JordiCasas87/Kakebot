import { useState } from 'react'
import AuthPage from './pages/AuthPage.jsx'
import TelegramLinkPage from './pages/TelegramLinkPage.jsx'

function App() {
  const [screen, setScreen] = useState('auth')
  const [telegramLinkData, setTelegramLinkData] = useState(null)

  const handleRegisterSuccess = (data) => {
    setTelegramLinkData(data)
    setScreen('telegram-link')
  }

  const handleBackToAuth = () => {
    setScreen('auth')
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

  return <AuthPage onRegisterSuccess={handleRegisterSuccess} />
}

export default App
