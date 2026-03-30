import lightChatBackground from '../assets/backgrounds/fondoChatClaro.png'
import mascotImage from '../assets/images/kakebotBlanco.png'
import '../App.css'

const reviewOptions = [
  {
    title: 'Hoy',
    description: 'Consulta tus gastos del dia y el total acumulado.',
  },
  {
    title: 'Mes',
    description: 'Revisa lo que llevas gastado este mes.',
  },
  {
    title: 'Por periodo',
    description: 'Compara rangos de fechas y analiza tendencias.',
  },
  {
    title: 'Por categoria',
    description: 'Descubre en que se va mas parte de tu presupuesto.',
  },
  {
    title: 'Recientes',
    description: 'Mira rapido tus ultimos movimientos registrados.',
  },
]

function DashboardPage({ user, onLogout }) {
  return (
    <main
      className="dashboard-shell"
      style={{ '--dashboard-background': `url(${lightChatBackground})` }}
    >
      <section className="dashboard-hero">
        <div className="dashboard-copy">
          <p className="eyebrow">KakeBot</p>
          <h1>Tu espacio para registrar y revisar tus gastos con calma.</h1>
          <p className="dashboard-text">
            Has entrado como <strong>{user.username}</strong>. Desde aqui podras
            registrar movimientos nuevos y revisar tu economia del dia, del mes
            o por categoria.
          </p>
        </div>

        <div className="dashboard-mascot">
          <img src={mascotImage} alt="Mascota de KakeBot" className="dashboard-mascot-image" />
        </div>
      </section>

      <section className="dashboard-content">
        <div className="dashboard-card dashboard-primary-card">
          <div>
            <p className="card-label">Accion principal</p>
            <h2>Registrar gasto</h2>
          </div>
          <p className="dashboard-card-text">
            Este sera el acceso rapido al formulario principal para anotar un
            gasto nuevo en la app.
          </p>
          <button className="submit-button" type="button">
            Registrar gasto
          </button>
        </div>

        <div className="dashboard-card">
          <div>
            <p className="card-label">Revision</p>
            <h2>Revisar tus gastos</h2>
          </div>

          <div className="review-grid">
            {reviewOptions.map((option) => (
              <article className="review-card" key={option.title}>
                <h3>{option.title}</h3>
                <p>{option.description}</p>
              </article>
            ))}
          </div>
        </div>

        <div className="dashboard-footer-actions">
          <button className="submit-button secondary-button" onClick={onLogout} type="button">
            Cerrar sesion
          </button>
        </div>
      </section>
    </main>
  )
}

export default DashboardPage
