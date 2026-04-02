import { useEffect, useState } from 'react'
import chatBackground from '../assets/backgrounds/fondoChat.png'
import lightChatBackground from '../assets/backgrounds/fondoChatClaro.png'
import mascotImage from '../assets/images/kakebotBlanco.png'
import { generateTelegramLinkCode, getCurrentUser, unlinkTelegram } from '../services/authService.js'
import {
  createExpense,
  deleteExpense,
  getCategoryTotalsByPeriod,
  getMonthCategoryTotals,
  getMonthTotal,
  getRecentExpenses,
  getTodayExpenses,
  getTodayTotal,
} from '../services/expenseService.js'
import '../App.css'

const reviewOptions = [
  {
    title: 'Hoy',
    description: 'Consulta tus gastos del día y el total acumulado.',
  },
  {
    title: 'Mes y categorías',
    description: 'Revisa lo que llevas gastado este mes.',
  },
  {
    title: 'Por período',
    description: 'Compara rangos de fechas y analiza tendencias.',
  },
  {
    title: 'Recientes',
    description: 'Mira rápido tus últimos movimientos registrados.',
  },
]

const expenseCategoryOptions = [
  { value: 'casa', label: 'Casa' },
  { value: 'comida', label: 'Comida' },
  { value: 'transporte', label: 'Transporte' },
  { value: 'ocio', label: 'Ocio' },
  { value: 'otros', label: 'Otros' },
]

const categoryTheme = {
  HOME: 'theme-home',
  FOOD: 'theme-food',
  TRANSPORT: 'theme-transport',
  LEISURE: 'theme-leisure',
  OTHER: 'theme-other',
}

const categoryLabelMap = {
  HOME: 'Casa',
  FOOD: 'Comida',
  TRANSPORT: 'Transporte',
  LEISURE: 'Ocio',
  OTHER: 'Otros',
}

const monthOptions = [
  { value: 1, label: 'Enero' },
  { value: 2, label: 'Febrero' },
  { value: 3, label: 'Marzo' },
  { value: 4, label: 'Abril' },
  { value: 5, label: 'Mayo' },
  { value: 6, label: 'Junio' },
  { value: 7, label: 'Julio' },
  { value: 8, label: 'Agosto' },
  { value: 9, label: 'Septiembre' },
  { value: 10, label: 'Octubre' },
  { value: 11, label: 'Noviembre' },
  { value: 12, label: 'Diciembre' },
]

const currentDate = new Date()
const periodYearOptions = Array.from({ length: 5 }, (_, index) => currentDate.getFullYear() - index)

function formatEuro(value) {
  const number = Number(value)

  if (Number.isNaN(number)) {
    return value
  }

  return new Intl.NumberFormat('es-ES', {
    style: 'currency',
    currency: 'EUR',
  }).format(number)
}

function formatTime(value) {
  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return ''
  }

  return date.toLocaleTimeString('es-ES', {
    hour: '2-digit',
    minute: '2-digit',
  })
}

function formatShortDate(value) {
  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return ''
  }

  return date.toLocaleDateString('es-ES', {
    day: '2-digit',
    month: '2-digit',
  })
}

function formatExpiration(expiresAt) {
  if (!expiresAt) {
    return 'Válido durante 10 minutos.'
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

function DashboardPage({ user, onLogout, onUserUpdate }) {
  const [isExpenseModalOpen, setIsExpenseModalOpen] = useState(false)
  const [activeInsightsModal, setActiveInsightsModal] = useState(null)
  const [isSettingsModalOpen, setIsSettingsModalOpen] = useState(false)
  const [category, setCategory] = useState('comida')
  const [description, setDescription] = useState('')
  const [amount, setAmount] = useState('')
  const [expenseFeedback, setExpenseFeedback] = useState('')
  const [expenseError, setExpenseError] = useState('')
  const [isCreatingExpense, setIsCreatingExpense] = useState(false)
  const [todayExpenses, setTodayExpenses] = useState([])
  const [todayTotal, setTodayTotalValue] = useState(0)
  const [monthCategoryTotals, setMonthCategoryTotals] = useState([])
  const [monthTotal, setMonthTotalValue] = useState(0)
  const [periodMonth, setPeriodMonth] = useState(currentDate.getMonth() + 1)
  const [periodYear, setPeriodYear] = useState(currentDate.getFullYear())
  const [periodCategoryTotals, setPeriodCategoryTotals] = useState([])
  const [periodTotal, setPeriodTotal] = useState(0)
  const [recentExpenses, setRecentExpenses] = useState([])
  const [insightsError, setInsightsError] = useState('')
  const [isLoadingInsights, setIsLoadingInsights] = useState(false)
  const [deletingExpenseId, setDeletingExpenseId] = useState(null)
  const [settingsError, setSettingsError] = useState('')
  const [settingsFeedback, setSettingsFeedback] = useState('')
  const [isGeneratingLinkCode, setIsGeneratingLinkCode] = useState(false)
  const [isUnlinkingTelegram, setIsUnlinkingTelegram] = useState(false)
  const [isRefreshingUser, setIsRefreshingUser] = useState(false)
  const [telegramLinkCodeData, setTelegramLinkCodeData] = useState(null)
  const [isTelegramLinked, setIsTelegramLinked] = useState(Boolean(user.externalId))

  useEffect(() => {
    const hasOpenModal = isExpenseModalOpen || activeInsightsModal !== null || isSettingsModalOpen
    const previousBodyOverflow = document.body.style.overflow
    const previousTouchAction = document.body.style.touchAction

    if (hasOpenModal) {
      document.body.style.overflow = 'hidden'
      document.body.style.touchAction = 'none'
    }

    return () => {
      document.body.style.overflow = previousBodyOverflow
      document.body.style.touchAction = previousTouchAction
    }
  }, [activeInsightsModal, isExpenseModalOpen, isSettingsModalOpen])

  useEffect(() => {
    setIsTelegramLinked(Boolean(user.externalId))
  }, [user.externalId])

  const handleOpenExpenseModal = () => {
    setExpenseError('')
    setExpenseFeedback('')
    setIsExpenseModalOpen(true)
  }

  const handleCloseExpenseModal = () => {
    setExpenseError('')
    setExpenseFeedback('')
    setIsExpenseModalOpen(false)
  }

  const handleCloseInsightsModal = () => {
    setActiveInsightsModal(null)
    setInsightsError('')
  }

  const handleOpenSettingsModal = async () => {
    setSettingsError('')
    setSettingsFeedback('')
    setTelegramLinkCodeData(null)
    setIsSettingsModalOpen(true)

    setIsRefreshingUser(true)

    try {
      const refreshedUser = await getCurrentUser(user.id)
      setIsTelegramLinked(Boolean(refreshedUser.externalId))
      onUserUpdate(refreshedUser)
    } catch (error) {
      setSettingsError(error.message)
    } finally {
      setIsRefreshingUser(false)
    }
  }

  const handleCloseSettingsModal = () => {
    setSettingsError('')
    setSettingsFeedback('')
    setTelegramLinkCodeData(null)
    setIsSettingsModalOpen(false)
  }

  const openTodayModal = async () => {
    setInsightsError('')
    setIsLoadingInsights(true)
    setActiveInsightsModal('today')

    try {
      const [expenses, totalResponse] = await Promise.all([
        getTodayExpenses(user.id),
        getTodayTotal(user.id),
      ])

      setTodayExpenses(expenses)
      setTodayTotalValue(totalResponse.total)
    } catch (error) {
      setInsightsError(error.message)
    } finally {
      setIsLoadingInsights(false)
    }
  }

  const openMonthModal = async () => {
    setInsightsError('')
    setIsLoadingInsights(true)
    setActiveInsightsModal('month')

    try {
      const [categoryTotals, totalResponse] = await Promise.all([
        getMonthCategoryTotals(user.id),
        getMonthTotal(user.id),
      ])

      setMonthCategoryTotals(categoryTotals)
      setMonthTotalValue(totalResponse.total)
    } catch (error) {
      setInsightsError(error.message)
    } finally {
      setIsLoadingInsights(false)
    }
  }

  const loadPeriodSummary = async (selectedYear, selectedMonth) => {
    setInsightsError('')
    setIsLoadingInsights(true)

    try {
      const categoryTotals = await getCategoryTotalsByPeriod(user.id, selectedYear, selectedMonth)
      setPeriodCategoryTotals(categoryTotals)
      setPeriodTotal(
        categoryTotals.reduce((sum, item) => sum + Number(item.total ?? 0), 0),
      )
    } catch (error) {
      setInsightsError(error.message)
    } finally {
      setIsLoadingInsights(false)
    }
  }

  const openPeriodModal = async () => {
    setActiveInsightsModal('period')
    await loadPeriodSummary(periodYear, periodMonth)
  }

  const openRecentModal = async () => {
    setInsightsError('')
    setIsLoadingInsights(true)
    setActiveInsightsModal('recent')

    try {
      const expenses = await getRecentExpenses(user.id, 20)
      setRecentExpenses(expenses)
    } catch (error) {
      setInsightsError(error.message)
    } finally {
      setIsLoadingInsights(false)
    }
  }

  const resetExpenseForm = () => {
    setCategory('comida')
    setDescription('')
    setAmount('')
  }

  const handleExpenseSubmit = async (event) => {
    event.preventDefault()
    setExpenseError('')
    setExpenseFeedback('')

    if (!description.trim()) {
      setExpenseError('Necesitamos una descripcion breve para guardar el gasto.')
      return
    }

    if (!amount.trim()) {
      setExpenseError('Necesitamos el importe para guardar el gasto.')
      return
    }

    setIsCreatingExpense(true)

    try {
      const savedExpense = await createExpense(user.id, {
        category,
        description: description.trim(),
        amount: amount.trim(),
      })

      setExpenseFeedback(
        `Gasto guardado correctamente: ${savedExpense.description} (${savedExpense.amount} €).`,
      )
      resetExpenseForm()
    } catch (error) {
      setExpenseError(error.message)
    } finally {
      setIsCreatingExpense(false)
    }
  }

  const handlePeriodSubmit = async (event) => {
    event.preventDefault()
    await loadPeriodSummary(periodYear, periodMonth)
  }

  const handleDeleteTodayExpense = async (expenseId) => {
    setInsightsError('')
    setDeletingExpenseId(expenseId)

    try {
      await deleteExpense(user.id, expenseId)
      const remainingExpenses = todayExpenses.filter((expense) => expense.id !== expenseId)
      setTodayExpenses(remainingExpenses)
      setTodayTotalValue(
        remainingExpenses.reduce((sum, expense) => sum + Number(expense.amount ?? 0), 0),
      )
    } catch (error) {
      setInsightsError(error.message)
    } finally {
      setDeletingExpenseId(null)
    }
  }

  const handleGenerateTelegramCode = async () => {
    setSettingsError('')
    setSettingsFeedback('')
    setIsGeneratingLinkCode(true)

    try {
      const data = await generateTelegramLinkCode(user.id)
      setTelegramLinkCodeData(data)
      setSettingsFeedback('Nuevo código de vinculación generado correctamente.')
    } catch (error) {
      setSettingsError(error.message)
    } finally {
      setIsGeneratingLinkCode(false)
    }
  }

  const handleUnlinkTelegram = async () => {
    setSettingsError('')
    setSettingsFeedback('')
    setIsUnlinkingTelegram(true)

    try {
      await unlinkTelegram(user.id)
      setIsTelegramLinked(false)
      setTelegramLinkCodeData(null)
      setSettingsFeedback('Tu cuenta de Telegram se ha desvinculado correctamente.')
      onUserUpdate({
        ...user,
        externalId: null,
      })
    } catch (error) {
      setSettingsError(error.message)
    } finally {
      setIsUnlinkingTelegram(false)
    }
  }

  return (
    <main
      className="dashboard-shell"
      style={{ '--dashboard-background': `url(${lightChatBackground})` }}
    >
      <section className="dashboard-hero">
        <div className="dashboard-copy">
          <p className="eyebrow">KakeBot</p>
          <h1>Tu espacio para registrar y revisar tus gastos.</h1>
          <div className="dashboard-mascot">
            <img src={mascotImage} alt="Mascota de KakeBot" className="dashboard-mascot-image" />
          </div>
          <p className="dashboard-text">
            Has entrado como <strong>{user.username}</strong>. Esta es tu página
            de gestión de gastos y configuración.
          </p>
        </div>
      </section>

      <section className="dashboard-content">
        <div className="dashboard-card dashboard-primary-card">
          <div>
            <p className="card-label">Acción principal</p>
            <h2>Registrar gasto</h2>
          </div>
          <p className="dashboard-card-text">
            Este será el acceso rápido al formulario principal para anotar un
            gasto nuevo en la app.
          </p>
          <button className="submit-button" onClick={handleOpenExpenseModal} type="button">
            Registrar gasto
          </button>
        </div>

        <div className="dashboard-card">
          <div>
            <p className="card-label">Revisión</p>
            <h2>Revisar tus gastos</h2>
          </div>

          <div className="review-grid">
            {reviewOptions.map((option) => (
              <article
                className={`review-card ${option.title === 'Hoy' || option.title === 'Mes y categorías' || option.title === 'Por período' || option.title === 'Recientes' ? 'review-card-clickable' : ''}`}
                key={option.title}
                onClick={
                  option.title === 'Hoy'
                    ? openTodayModal
                    : option.title === 'Mes y categorías'
                      ? openMonthModal
                      : option.title === 'Por período'
                        ? openPeriodModal
                        : option.title === 'Recientes'
                          ? openRecentModal
                      : undefined
                }
                role={option.title === 'Hoy' || option.title === 'Mes y categorías' || option.title === 'Por período' || option.title === 'Recientes' ? 'button' : undefined}
                tabIndex={option.title === 'Hoy' || option.title === 'Mes y categorías' || option.title === 'Por período' || option.title === 'Recientes' ? 0 : undefined}
                onKeyDown={(event) => {
                  if (option.title === 'Hoy' && (event.key === 'Enter' || event.key === ' ')) {
                    event.preventDefault()
                    openTodayModal()
                  }
                  if (option.title === 'Mes y categorías' && (event.key === 'Enter' || event.key === ' ')) {
                    event.preventDefault()
                    openMonthModal()
                  }
                  if (option.title === 'Por período' && (event.key === 'Enter' || event.key === ' ')) {
                    event.preventDefault()
                    openPeriodModal()
                  }
                  if (option.title === 'Recientes' && (event.key === 'Enter' || event.key === ' ')) {
                    event.preventDefault()
                    openRecentModal()
                  }
                }}
              >
                <h3>{option.title}</h3>
                <p>{option.description}</p>
              </article>
            ))}
          </div>
        </div>

        <div className="dashboard-footer-actions">
          <button className="submit-button secondary-button" onClick={handleOpenSettingsModal} type="button">
            Configuración
          </button>
          <button className="submit-button secondary-button" onClick={onLogout} type="button">
            Cerrar sesión
          </button>
        </div>
      </section>

      {isExpenseModalOpen ? (
        <div className="modal-overlay" onClick={handleCloseExpenseModal} role="presentation">
          <div
            className="expense-modal"
            onClick={(event) => event.stopPropagation()}
            role="dialog"
            aria-modal="true"
            aria-labelledby="expense-modal-title"
            style={{ '--modal-background': `url(${chatBackground})` }}
          >
            <div className="expense-modal-header">
              <div>
                <p className="card-label">Nuevo gasto</p>
                <h2 id="expense-modal-title">Registrar un gasto nuevo</h2>
              </div>
            </div>

            <form className="auth-form" onSubmit={handleExpenseSubmit}>
              <label className="field">
                <span>Tipo de gasto</span>
                <select
                  className="field-select"
                  value={category}
                  onChange={(event) => setCategory(event.target.value)}
                >
                  {expenseCategoryOptions.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </label>

              <label className="field">
                <span>Descripción</span>
                <input
                  type="text"
                  placeholder="Compra en supermercado"
                  value={description}
                  onChange={(event) => setDescription(event.target.value)}
                />
              </label>

              <label className="field">
                <span>Importe</span>
                <input
                  type="text"
                  placeholder="23,50"
                  value={amount}
                  onChange={(event) => setAmount(event.target.value)}
                />
              </label>

              {expenseError ? <p className="auth-error-message">{expenseError}</p> : null}
              {expenseFeedback ? <p className="auth-success-message">{expenseFeedback}</p> : null}

              <div className="expense-modal-actions">
                <button className="submit-button modal-primary-button" disabled={isCreatingExpense} type="submit">
                  {isCreatingExpense ? 'Guardando gasto...' : 'Guardar gasto'}
                </button>

                <button
                  className="submit-button secondary-button modal-secondary-button"
                  onClick={handleCloseExpenseModal}
                  type="button"
                >
                  Cerrar
                </button>
              </div>
            </form>
          </div>
        </div>
      ) : null}

      {activeInsightsModal === 'today' ? (
        <div className="modal-overlay" onClick={handleCloseInsightsModal} role="presentation">
          <div
            className="expense-modal insights-modal"
            onClick={(event) => event.stopPropagation()}
            role="dialog"
            aria-modal="true"
            aria-labelledby="today-modal-title"
            style={{ '--modal-background': `url(${lightChatBackground})` }}
          >
            <div className="expense-modal-header">
              <div>
                <p className="card-label">Hoy</p>
                <h2 id="today-modal-title">Tus gastos de hoy</h2>
              </div>
            </div>

            {isLoadingInsights ? <p className="insights-empty-state">Cargando tus gastos de hoy...</p> : null}
            {!isLoadingInsights && insightsError ? <p className="auth-error-message">{insightsError}</p> : null}

            {!isLoadingInsights && !insightsError ? (
              <>
                {todayExpenses.length ? (
                  <div className="today-expenses-list">
                    {todayExpenses.map((expense) => (
                      <article
                        className={`today-expense-card ${categoryTheme[expense.category] ?? 'theme-other'}`}
                        key={expense.id}
                      >
                        <div className="today-expense-header">
                          <span>{categoryLabelMap[expense.category] ?? expense.category}</span>
                          <div className="expense-card-amount-block">
                            <strong>{formatEuro(expense.amount)}</strong>
                            <button
                              className="expense-delete-button"
                              disabled={deletingExpenseId === expense.id}
                              onClick={() => handleDeleteTodayExpense(expense.id)}
                              type="button"
                            >
                              {deletingExpenseId === expense.id ? 'Borrando...' : 'Eliminar'}
                            </button>
                          </div>
                        </div>
                        <p>{expense.description}</p>
                        <div className="expense-card-footer">
                          <small>{formatTime(expense.registeredAt)}</small>
                        </div>
                      </article>
                    ))}
                  </div>
                ) : (
                  <p className="insights-empty-state">Todavía no has registrado gastos hoy.</p>
                )}

                <div className="totals-summary-card">
                  <span>Total del día</span>
                  <strong>{formatEuro(todayTotal)}</strong>
                </div>
              </>
            ) : null}

            <div className="expense-modal-actions single-action">
              <button
                className="submit-button secondary-button modal-secondary-button"
                onClick={handleCloseInsightsModal}
                type="button"
              >
                Cerrar
              </button>
            </div>
          </div>
        </div>
      ) : null}

      {activeInsightsModal === 'month' ? (
        <div className="modal-overlay" onClick={handleCloseInsightsModal} role="presentation">
          <div
            className="expense-modal insights-modal"
            onClick={(event) => event.stopPropagation()}
            role="dialog"
            aria-modal="true"
            aria-labelledby="month-modal-title"
            style={{ '--modal-background': `url(${lightChatBackground})` }}
          >
            <div className="expense-modal-header">
              <div>
                <p className="card-label">Mes</p>
                <h2 id="month-modal-title">Tu resumen del mes</h2>
              </div>
            </div>

            {isLoadingInsights ? <p className="insights-empty-state">Cargando tu resumen mensual...</p> : null}
            {!isLoadingInsights && insightsError ? <p className="auth-error-message">{insightsError}</p> : null}

            {!isLoadingInsights && !insightsError ? (
              <>
                <div className="month-summary-list">
                  {monthCategoryTotals.map((item) => (
                    <article
                      className={`month-summary-card ${categoryTheme[item.category] ?? 'theme-other'}`}
                      key={item.category}
                    >
                      <span>{categoryLabelMap[item.category] ?? item.category}</span>
                      <strong>{formatEuro(item.total)}</strong>
                    </article>
                  ))}
                </div>

                <div className="totals-summary-card">
                  <span>Total del mes</span>
                  <strong>{formatEuro(monthTotal)}</strong>
                </div>
              </>
            ) : null}

            <div className="expense-modal-actions single-action">
              <button
                className="submit-button secondary-button modal-secondary-button"
                onClick={handleCloseInsightsModal}
                type="button"
              >
                Cerrar
              </button>
            </div>
          </div>
        </div>
      ) : null}

      {activeInsightsModal === 'period' ? (
        <div className="modal-overlay" onClick={handleCloseInsightsModal} role="presentation">
          <div
            className="expense-modal insights-modal"
            onClick={(event) => event.stopPropagation()}
            role="dialog"
            aria-modal="true"
            aria-labelledby="period-modal-title"
            style={{ '--modal-background': `url(${lightChatBackground})` }}
          >
            <div className="expense-modal-header">
              <div>
                <p className="card-label">Por período</p>
                <h2 id="period-modal-title">Tu resumen por mes</h2>
              </div>
            </div>

            <form className="period-filter-form" onSubmit={handlePeriodSubmit}>
              <label className="field">
                <span>Mes</span>
                <select
                  className="field-select"
                  value={periodMonth}
                  onChange={(event) => setPeriodMonth(Number(event.target.value))}
                >
                  {monthOptions.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </label>

              <label className="field">
                  <span>Año</span>
                <select
                  className="field-select"
                  value={periodYear}
                  onChange={(event) => setPeriodYear(Number(event.target.value))}
                >
                  {periodYearOptions.map((year) => (
                    <option key={year} value={year}>
                      {year}
                    </option>
                  ))}
                </select>
              </label>

              <button className="submit-button modal-primary-button" disabled={isLoadingInsights} type="submit">
                {isLoadingInsights ? 'Consultando...' : 'Consultar período'}
              </button>
            </form>

            {!isLoadingInsights && insightsError ? <p className="auth-error-message">{insightsError}</p> : null}

            {!isLoadingInsights && !insightsError ? (
              <>
                <div className="month-summary-list">
                  {periodCategoryTotals.map((item) => (
                    <article
                      className={`month-summary-card ${categoryTheme[item.category] ?? 'theme-other'}`}
                      key={item.category}
                    >
                      <span>{categoryLabelMap[item.category] ?? item.category}</span>
                      <strong>{formatEuro(item.total)}</strong>
                    </article>
                  ))}
                </div>

                <div className="totals-summary-card">
                  <span>Total del período</span>
                  <strong>{formatEuro(periodTotal)}</strong>
                </div>
              </>
            ) : null}

            <div className="expense-modal-actions single-action">
              <button
                className="submit-button secondary-button modal-secondary-button"
                onClick={handleCloseInsightsModal}
                type="button"
              >
                Cerrar
              </button>
            </div>
          </div>
        </div>
      ) : null}

      {activeInsightsModal === 'recent' ? (
        <div className="modal-overlay" onClick={handleCloseInsightsModal} role="presentation">
          <div
            className="expense-modal insights-modal"
            onClick={(event) => event.stopPropagation()}
            role="dialog"
            aria-modal="true"
            aria-labelledby="recent-modal-title"
            style={{ '--modal-background': `url(${lightChatBackground})` }}
          >
            <div className="expense-modal-header">
              <div>
                <p className="card-label">Recientes</p>
                <h2 id="recent-modal-title">Tus últimos 20 movimientos</h2>
              </div>
            </div>

            {isLoadingInsights ? <p className="insights-empty-state">Cargando tus movimientos recientes...</p> : null}
            {!isLoadingInsights && insightsError ? <p className="auth-error-message">{insightsError}</p> : null}

            {!isLoadingInsights && !insightsError ? (
              <>
                {recentExpenses.length ? (
                  <div className="today-expenses-list">
                    {recentExpenses.map((expense) => (
                      <article
                        className={`today-expense-card ${categoryTheme[expense.category] ?? 'theme-other'}`}
                        key={expense.id}
                      >
                        <div className="today-expense-header">
                          <span>{categoryLabelMap[expense.category] ?? expense.category}</span>
                          <strong>{formatEuro(expense.amount)}</strong>
                        </div>
                        <p>{expense.description}</p>
                        <div className="expense-card-footer">
                          <small>{formatShortDate(expense.registeredAt)} · {formatTime(expense.registeredAt)}</small>
                        </div>
                      </article>
                    ))}
                  </div>
                ) : (
                  <p className="insights-empty-state">Todavía no tienes movimientos recientes para mostrar.</p>
                )}
              </>
            ) : null}

            <div className="expense-modal-actions single-action">
              <button
                className="submit-button secondary-button modal-secondary-button"
                onClick={handleCloseInsightsModal}
                type="button"
              >
                Cerrar
              </button>
            </div>
          </div>
        </div>
      ) : null}

      {isSettingsModalOpen ? (
        <div className="modal-overlay" onClick={handleCloseSettingsModal} role="presentation">
          <div
            className="expense-modal insights-modal"
            onClick={(event) => event.stopPropagation()}
            role="dialog"
            aria-modal="true"
            aria-labelledby="settings-modal-title"
            style={{ '--modal-background': `url(${lightChatBackground})` }}
          >
            <div className="expense-modal-header">
              <div>
                <p className="card-label">Configuración</p>
                <h2 id="settings-modal-title">Telegram y vinculación</h2>
              </div>
            </div>

            <p className="telegram-link-copy">
              {isTelegramLinked
                ? 'Tu chat de Telegram está vinculado a esta cuenta. Desde aquí puedes generar un nuevo código o desvincularlo.'
                : 'Esta cuenta no tiene Telegram vinculado ahora mismo. Puedes generar un nuevo código para conectarla cuando quieras.'}
            </p>

            {isRefreshingUser ? (
              <p className="insights-empty-state">Comprobando el estado actual de tu cuenta...</p>
            ) : null}

            {settingsError ? <p className="auth-error-message">{settingsError}</p> : null}
            {settingsFeedback ? <p className="auth-success-message">{settingsFeedback}</p> : null}

            {telegramLinkCodeData ? (
              <>
                <div className="telegram-code-box settings-code-box">
                  <span>{telegramLinkCodeData.code}</span>
                </div>

                <p className="telegram-expiration-note">
                  {formatExpiration(telegramLinkCodeData.expiresAt)}
                </p>

                <div className="telegram-steps">
                  <p className="telegram-steps-title">Cómo usarlo</p>
                  <ol>
                    <li>Busca <code>@Kakebotapp_bot</code> en Telegram.</li>
                    <li>Abre el chat del bot.</li>
                    <li>Escribe este comando:</li>
                  </ol>
                </div>

                <div className="telegram-command-box">
                  <code>/link {telegramLinkCodeData.code}</code>
                </div>
              </>
            ) : null}

            <div className="settings-actions">
              <button
                className="submit-button"
                disabled={isGeneratingLinkCode || isRefreshingUser}
                onClick={handleGenerateTelegramCode}
                type="button"
              >
                {isGeneratingLinkCode ? 'Generando código...' : 'Generar nuevo código'}
              </button>

              <button
                className="submit-button danger-button"
                disabled={isUnlinkingTelegram || isRefreshingUser}
                onClick={handleUnlinkTelegram}
                type="button"
              >
                {isUnlinkingTelegram ? 'Desvinculando...' : 'Desvincular Telegram'}
              </button>
            </div>

            <div className="expense-modal-actions single-action">
              <button
                className="submit-button secondary-button modal-secondary-button"
                onClick={handleCloseSettingsModal}
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

export default DashboardPage
