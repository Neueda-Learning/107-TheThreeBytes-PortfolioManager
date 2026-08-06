import axios from 'axios'

export const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000,
})

function extractApiErrorMessage(error) {
  if (error?.code === 'ECONNABORTED') {
    return 'The request timed out. Please try again.'
  }
  if (!error?.response) {
    return 'Unable to connect right now. Please check your connection and try again.'
  }
  if (error?.response?.data?.details?.length) {
    return error.response.data.details.join(', ')
  }
  if (error?.response?.data?.message) {
    return error.response.data.message
  }
  if (error?.response?.status) {
    const status = error.response.status
    if (status >= 500) {
      return 'Something went wrong on our side. Please try again in a moment.'
    }
    if (status === 404) {
      return 'The requested information could not be found.'
    }
    if (status === 401 || status === 403) {
      return 'You do not have permission to perform this action.'
    }
    if (status === 400 || status === 422) {
      return 'Some details are invalid. Please review the form and try again.'
    }
    return 'We could not complete your request. Please try again.'
  }
  return error?.message || 'Unexpected error. Please try again.'
}

api.interceptors.response.use(
  (response) => response,
  (error) => Promise.reject(new Error(extractApiErrorMessage(error))),
)

export function getApiErrorMessage(error, fallback = 'Something went wrong. Please try again.') {
  const message = error?.message?.trim()
  if (!message) return fallback
  if (/request failed with status/i.test(message)) return fallback
  if (/network error/i.test(message)) return 'Unable to connect right now. Please check your connection and try again.'
  if (/timeout|timed out/i.test(message)) return 'The request took too long. Please try again.'
  return message
}
