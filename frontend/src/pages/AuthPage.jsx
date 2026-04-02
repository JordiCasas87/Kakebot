import { useEffect, useRef, useState } from 'react'
import chatBackground from '../assets/backgrounds/fondoChat.png'
import explanationBackground from '../assets/backgrounds/fondoExplicacionClaro.png'
import lightChatBackground from '../assets/backgrounds/fondoChatClaro.png'
import mascotAnimation from '../assets/animations/kakebotAnimHappy.mp4'
import originalKakeboImage from '../assets/images/kakebo.jpg'
import notebookIcon from '../assets/images/iconoLibreta.png'
import { generateTelegramLinkCode, loginUser, registerUser } from '../services/authService.js'
import '../App.css'

const MODES = {
  login: 'login',
  register: 'register',
}

function AuthPage({ onLoginSuccess, onRegisterSuccess }) {
  const [mode, setMode] = useState(null)
  const [isKakeboModalOpen, setIsKakeboModalOpen] = useState(false)
  const [registerUsername, setRegisterUsername] = useState('')
  const [registerPassword, setRegisterPassword] = useState('')
  const [registerPasswordConfirmation, setRegisterPasswordConfirmation] = useState('')
  const [loginUsername, setLoginUsername] = useState('')
  const [loginPassword, setLoginPassword] = useState('')
  const [errorMessage, setErrorMessage] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const videoRef = useRef(null)

  const isLogin = mode === MODES.login

  const resetFeedback = () => {
    setErrorMessage('')
  }

  const handleModeChange = (nextMode) => {
    setMode(nextMode)
    resetFeedback()
  }

  const validateRegisterForm = () => {
    if (!registerUsername.trim()) {
      return 'Necesitamos un nombre de usuario para crear tu cuenta.'
    }

    if (!registerPassword) {
      return 'Necesitamos una contraseña para crear tu cuenta.'
    }

    if (registerPassword !== registerPasswordConfirmation) {
      return 'Las contraseñas no coinciden.'
    }

    return null
  }

  const handleRegisterSubmit = async (event) => {
    event.preventDefault()
    resetFeedback()

    const validationError = validateRegisterForm()

    if (validationError) {
      setErrorMessage(validationError)
      return
    }

    setIsSubmitting(true)

    try {
      const user = await registerUser({
        username: registerUsername.trim(),
        password: registerPassword,
      })

      const telegramLink = await generateTelegramLinkCode(user.id)

      onRegisterSuccess?.({
        username: user.username,
        code: telegramLink.code,
        expiresAt: telegramLink.expiresAt,
      })
    } catch (error) {
      setErrorMessage(error.message)
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleLoginSubmit = async (event) => {
    event.preventDefault()
    resetFeedback()

    if (!loginUsername.trim() || !loginPassword) {
      setErrorMessage('Necesitamos tu usuario y tu contraseña para entrar.')
      return
    }

    setIsSubmitting(true)

    try {
      const user = await loginUser({
        username: loginUsername.trim(),
        password: loginPassword,
      })

      onLoginSuccess?.(user)
    } catch (error) {
      setErrorMessage(error.message)
    } finally {
      setIsSubmitting(false)
    }
  }

  useEffect(() => {
    const videoElement = videoRef.current

    if (!videoElement) {
      return undefined
    }

    let replayTimeoutId

    const scheduleReplay = () => {
      replayTimeoutId = window.setTimeout(() => {
        videoElement.currentTime = 0
        videoElement.play().catch(() => {})
      }, 5000)
    }

    const handleEnded = () => {
      scheduleReplay()
    }

    videoElement.addEventListener('ended', handleEnded)
    videoElement.play().catch(() => {})

    return () => {
      videoElement.removeEventListener('ended', handleEnded)
      if (replayTimeoutId) {
        window.clearTimeout(replayTimeoutId)
      }
    }
  }, [])

  useEffect(() => {
    const previousBodyOverflow = document.body.style.overflow
    const previousTouchAction = document.body.style.touchAction
    const previousHtmlOverflow = document.documentElement.style.overflow
    const previousHtmlTouchAction = document.documentElement.style.touchAction

    if (isKakeboModalOpen) {
      document.body.style.overflow = 'hidden'
      document.body.style.touchAction = 'none'
      document.documentElement.style.overflow = 'hidden'
      document.documentElement.style.touchAction = 'none'
    }

    return () => {
      document.body.style.overflow = previousBodyOverflow
      document.body.style.touchAction = previousTouchAction
      document.documentElement.style.overflow = previousHtmlOverflow
      document.documentElement.style.touchAction = previousHtmlTouchAction
    }
  }, [isKakeboModalOpen])

  return (
    <main className="app-shell">
      <section
        className="hero-panel"
        style={{ '--chat-background': `url(${chatBackground})` }}
      >
        <div className="hero-copy">
          <p className="eyebrow">KakeBot</p>
          <h1>Tu registro de gastos diario, fácil, rápido y con un toque kawaii.</h1>
          <p className="hero-text">
            Registra rápido desde Telegram, revisa con calma desde la app y deja
            que tu compañero financiero te recuerde cómo va tu economía.
          </p>
        </div>

        <div className="mascot-card">
          <video
            ref={videoRef}
            className="mascot-image"
            autoPlay
            muted
            playsInline
            aria-label="KakeBot animation"
          >
            <source src={mascotAnimation} type="video/mp4" />
          </video>
        </div>

        <div className="hero-kakebo-entry" aria-label="Acceso a información sobre KakeBot">
          <button
            className="hero-kakebo-icon"
            type="button"
            aria-label="What is KakeBot?"
            onClick={() => setIsKakeboModalOpen(true)}
          >
            <img src={notebookIcon} alt="" />
          </button>
          <span className="hero-kakebo-text">What&apos;s KakeBot?</span>
        </div>
      </section>

      <section
        className="auth-panel"
        style={{ '--auth-background': `url(${lightChatBackground})` }}
      >
        <div className="auth-panel-content">
          <div className="auth-card">
            <div className="auth-toggle">
              <button
                className={isLogin ? 'toggle-button active' : 'toggle-button'}
                onClick={() => handleModeChange(MODES.login)}
                type="button"
              >
                Iniciar sesión
              </button>
              <button
                className={!isLogin ? 'toggle-button active' : 'toggle-button'}
                onClick={() => handleModeChange(MODES.register)}
                type="button"
              >
                Crear cuenta
              </button>
            </div>

            {mode ? (
              <div className="auth-content">
                <div>
                  <p className="card-label">{isLogin ? 'Acceso' : 'Nuevo usuario'}</p>
                  <h2>{isLogin ? 'Accede a tu cuenta' : 'Crea tu cuenta'}</h2>
                </div>

                <form
                  className="auth-form"
                  onSubmit={isLogin ? handleLoginSubmit : handleRegisterSubmit}
                >
                  {!isLogin && (
                    <label className="field">
                      <span>Nombre de usuario</span>
                      <input
                        type="text"
                        placeholder="Nombre usuario"
                        value={registerUsername}
                        onChange={(event) => setRegisterUsername(event.target.value)}
                      />
                    </label>
                  )}

                  {isLogin && (
                    <label className="field">
                      <span>Usuario</span>
                      <input
                        type="text"
                        placeholder="Nombre usuario"
                        value={loginUsername}
                        onChange={(event) => setLoginUsername(event.target.value)}
                      />
                    </label>
                  )}

                  <label className="field">
                    <span>Contraseña</span>
                    <input
                      type="password"
                      placeholder="******"
                      value={isLogin ? loginPassword : registerPassword}
                      onChange={(event) =>
                        isLogin
                          ? setLoginPassword(event.target.value)
                          : setRegisterPassword(event.target.value)
                      }
                    />
                  </label>

                  {!isLogin && (
                    <label className="field">
                      <span>Repite la contraseña</span>
                      <input
                        type="password"
                        placeholder="******"
                        value={registerPasswordConfirmation}
                        onChange={(event) => setRegisterPasswordConfirmation(event.target.value)}
                      />
                    </label>
                  )}

                  {errorMessage ? <p className="auth-error-message">{errorMessage}</p> : null}

                  <button className="submit-button" type="submit" disabled={isSubmitting}>
                    {isSubmitting
                      ? isLogin
                        ? 'Entrando en KakeBot...'
                        : 'Preparando tu cuenta...'
                      : isLogin
                        ? 'Entrar en KakeBot'
                        : 'Crear mi cuenta'}
                  </button>
                </form>
              </div>
            ) : (
              <div className="auth-placeholder"></div>
            )}
          </div>

          <p className="project-footnote">
            Proyecto personal de Jordi Casas González · Marzo 2026 · Versión 0.1
          </p>
        </div>
      </section>

      {isKakeboModalOpen ? (
        <div className="modal-overlay" onClick={() => setIsKakeboModalOpen(false)} role="presentation">
          <div
            className="expense-modal kakebo-info-modal"
            onClick={(event) => event.stopPropagation()}
            role="dialog"
            aria-modal="true"
            aria-labelledby="kakebo-info-title"
            style={{ '--modal-background': `url(${explanationBackground})` }}
          >
            <div className="expense-modal-header kakebo-info-header">
              <div className="kakebo-info-title-wrap">
                <div>
                  <h2 id="kakebo-info-title">¿Qué es KakeBot?</h2>
                </div>
              </div>
            </div>

            <div className="kakebo-info-content">
              <p className="kakebo-info-copy">
                <strong>Kakebo</strong> es un método japonés de control de gastos
                basado en anotar lo que compras y revisarlo con calma. La idea no
                es solo registrar números, sino tomar conciencia de tus hábitos y
                gastar con más intención.
              </p>

              <div className="kakebo-info-visual">
                <img src={originalKakeboImage} alt="Ejemplo de un Kakebo original" className="kakebo-info-image" />
              </div>

              <p className="kakebo-info-copy">
                <strong>KakeBot</strong> toma esa inspiración y la adapta a una
                experiencia actual: registro rápido desde Telegram y revisión más
                tranquila desde la app web, manteniendo el espíritu de observar,
                ordenar y entender mejor tus gastos.
              </p>
            </div>

            <div className="expense-modal-actions single-action">
              <button
                className="submit-button secondary-button modal-secondary-button"
                onClick={() => setIsKakeboModalOpen(false)}
                type="button"
              >
                Cerrar
              </button>
            </div>
          </div>
        </div>
      ) : null}
    </main>
  )
}

export default AuthPage
