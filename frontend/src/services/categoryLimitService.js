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

export async function getCategoryLimits(userId) {
  const response = await fetch('/api/users/me/category-limits', {
    headers: {
      'X-User-Id': String(userId),
    },
  })

  return parseJsonResponse(response)
}

export async function updateCategoryLimit(userId, category, monthlyLimit) {
  const response = await fetch(`/api/users/me/category-limits/${category}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': String(userId),
    },
    body: JSON.stringify({ monthlyLimit }),
  })

  return parseJsonResponse(response)
}
