import chatBackground from '../assets/backgrounds/fondoChat.png'
import lightChatBackground from '../assets/backgrounds/fondoChatClaro.png'
import mascotImage from '../assets/images/kakebotIMG.png'
import '../App.css'

function formatExpiration(expiresAt) {
  if (!expiresAt) {
    return 'Disponible durante unos minutos.'
  }

  const date = new Date(expiresAt)

  if (Number.isNaN(date.getTime())) {
    return 'Válido durante 10 minutos.'
  }

  return `Válido hasta las ${date.toLocaleTimeString('es-ES', {
    hour: '2-digit',
    minute: '2-digit',
  })}.`
}

function TelegramLinkPage({ code, expiresAt, username, onBackToAuth }) {
  const expirationMessage = formatExpiration(expiresAt)

  return (
    <main className="app-shell">
      <section
        className="hero-panel"
        style={{ '--chat-background': `url(${chatBackground})` }}
      >
        <div className="hero-copy">
          <p className="eyebrow">KakeBot</p>
          <h1>Tu cuenta ya está lista para empezar.</h1>
          <p className="hero-text telegram-hero-text">
            {username
              ? `Buen trabajo, ${username}. Ahora solo falta vincular tu bot para registrar gastos desde Telegram.`
              : 'Buen trabajo. Ahora solo falta vincular tu bot para registrar gastos desde Telegram.'}
          </p>
        </div>

        <div className="link-mascot-card">
          <img
            src={mascotImage}
            alt="Mascota de KakeBot"
            className="link-mascot-image"
          />
        </div>
      </section>

      <section
        className="auth-panel"
        style={{ '--auth-background': `url(${lightChatBackground})` }}
      >
        <div className="auth-panel-content">
          <div className="auth-card telegram-link-card">
            <div className="auth-content">
              <div>
                <p className="card-label">Cuenta creada</p>
                <h2>Vincula ahora tu bot de Telegram</h2>
              </div>

              <p className="telegram-link-copy">
                Si quieres registrar gastos desde Telegram, usa este código temporal:
              </p>

              <div className="telegram-code-box">
                <span>{code}</span>
              </div>

              <p className="telegram-expiration-note">{expirationMessage}</p>

              <div className="telegram-steps">
                <p className="telegram-steps-title">Cómo hacerlo</p>
                <ol>
                  <li>
                    Busca <code>@Kakebotapp_bot</code> en Telegram.
                  </li>
                  <li>Abre el chat del bot.</li>
                  <li>Escribe este comando:</li>
                </ol>
              </div>

              <div className="telegram-command-box">
                <code>/link {code}</code>
              </div>

              <p className="telegram-link-copy small">
                Cuando el bot confirme la vinculación, ya podrás registrar gastos desde el chat.
              </p>

              <button className="submit-button secondary-button" onClick={onBackToAuth} type="button">
                Volver al acceso
              </button>
            </div>
          </div>

          <p className="project-footnote">
            Proyecto personal de Jordi Casas González · Marzo 2026 · Versión 0.1
          </p>
        </div>
      </section>
    </main>
  )
}

export default TelegramLinkPage
