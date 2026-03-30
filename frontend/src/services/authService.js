const JSON_HEADERS = {
  'Content-Type': 'application/json',
}

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

export async function registerUser({ username, password }) {
  const response = await fetch('/api/users/register', {
    method: 'POST',
    headers: JSON_HEADERS,
    body: JSON.stringify({ username, password }),
  })

  return parseJsonResponse(response)
}

export async function loginUser({ username, password }) {
  const response = await fetch('/api/users/login', {
    method: 'POST',
    headers: JSON_HEADERS,
    body: JSON.stringify({ username, password }),
  })

  return parseJsonResponse(response)
}

export async function generateTelegramLinkCode(userId) {
  const response = await fetch('/api/users/telegram-link-code', {
    method: 'POST',
    headers: {
      'X-User-Id': String(userId),
    },
  })

  return parseJsonResponse(response)
}
