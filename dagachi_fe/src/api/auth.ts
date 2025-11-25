import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || ''

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 요청 인터셉터 - 토큰 자동 추가
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

export interface SignupRequest {
  username: string
  password: string
  nickname: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
}

// 회원가입
export const signup = async (data: SignupRequest): Promise<void> => {
  await api.post('/api/auth/signup', data)
}

// 로그인
export const login = async (data: LoginRequest): Promise<string> => {
  const response = await api.post<string>('/api/auth/login', data)
  return response.data
}

// 로그아웃
export const logout = (): void => {
  localStorage.removeItem('token')
}

export default api