import axios from 'axios'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

http.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers = config.headers ?? {}
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

http.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const status = error?.response?.status
    const data = error?.response?.data
    const message =
      data?.message ||
      error?.message ||
      (status ? `Request failed with status ${status}` : 'Network error')

    return Promise.reject({ status, message, data, raw: error })
  }
)

export default http