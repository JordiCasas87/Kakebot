async function parseJsonResponse(response) {
  const contentType = response.headers.get('content-type') ?? ''
  const hasJsonBody = contentType.includes('application/json')
  const data = hasJsonBody ? await response.json() : null

  if (!response.ok) {
    const details = Array.isArray(data?.details) ? data.details.join(' ') : ''
    const message = data?.message || 'Ha ocurrido un error inesperado.'
    throw new Error(details ? `${message} ${details}` : message)
  }

  return data
}

export async function createExpense(userId, expense) {
  const response = await fetch('/api/expenses', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': String(userId),
    },
    body: JSON.stringify(expense),
  })

  return parseJsonResponse(response)
}

export async function getTodayExpenses(userId) {
  const response = await fetch('/api/expenses/today', {
    headers: {
      'X-User-Id': String(userId),
    },
  })

  return parseJsonResponse(response)
}

export async function getTodayTotal(userId) {
  const response = await fetch('/api/expenses/total/today', {
    headers: {
      'X-User-Id': String(userId),
    },
  })

  return parseJsonResponse(response)
}

export async function getMonthExpenses(userId) {
  const response = await fetch('/api/expenses/month', {
    headers: {
      'X-User-Id': String(userId),
    },
  })

  return parseJsonResponse(response)
}

export async function getMonthCategoryTotals(userId) {
  const response = await fetch('/api/expenses/total/month/by-category', {
    headers: {
      'X-User-Id': String(userId),
    },
  })

  return parseJsonResponse(response)
}

export async function getMonthTotal(userId) {
  const response = await fetch('/api/expenses/total/month', {
    headers: {
      'X-User-Id': String(userId),
    },
  })

  return parseJsonResponse(response)
}

export async function getCategoryTotalsByPeriod(userId, year, month) {
  const searchParams = new URLSearchParams({
    year: String(year),
    month: String(month),
  })

  const response = await fetch(`/api/expenses/total/by-category/period?${searchParams.toString()}`, {
    headers: {
      'X-User-Id': String(userId),
    },
  })

  return parseJsonResponse(response)
}

export async function getRecentExpenses(userId, limit = 20) {
  const searchParams = new URLSearchParams({
    limit: String(limit),
  })

  const response = await fetch(`/api/expenses/recent?${searchParams.toString()}`, {
    headers: {
      'X-User-Id': String(userId),
    },
  })

  return parseJsonResponse(response)
}

export async function deleteExpense(userId, expenseId) {
  const response = await fetch(`/api/expenses/${expenseId}`, {
    method: 'DELETE',
    headers: {
      'X-User-Id': String(userId),
    },
  })

  return parseJsonResponse(response)
}

export async function downloadMonthlyExpensePdf(userId, year, month) {
  const searchParams = new URLSearchParams({
    year: String(year),
    month: String(month),
  })

  const response = await fetch(`/api/reports/expenses/monthly/pdf?${searchParams.toString()}`, {
    headers: {
      'X-User-Id': String(userId),
    },
  })

  if (!response.ok) {
    throw new Error('No se ha podido descargar el PDF mensual.')
  }

  return response.blob()
}
