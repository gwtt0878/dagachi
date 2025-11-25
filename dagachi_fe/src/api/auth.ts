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

// 응답 인터셉터 - 401/403 자동 처리
api.interceptors.response.use(
  (response) => {
    // 성공 응답은 그대로 반환
    return response
  },
  (error) => {
    // 401 Unauthorized 또는 403 Forbidden
    if (error.response && (error.response.status === 401 || error.response.status === 403)) {
      // 토큰이 만료되었거나 유효하지 않음
      const token = localStorage.getItem('token')
      if (token) {
        // 토큰 삭제
        localStorage.removeItem('token')
        console.log('토큰이 만료되었거나 유효하지 않아 자동으로 삭제되었습니다.')
        
        // 로그인 페이지로 리다이렉트
        // 현재 경로가 로그인/회원가입 페이지가 아닐 때만 리다이렉트
        if (!window.location.pathname.includes('/login') && 
            !window.location.pathname.includes('/signup')) {
          window.location.href = '/login?expired=true'
        }
      }
    }
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

export const getCurrentNickname = (): string | null => {
  const token = localStorage.getItem('token')
  if (!token) {
    return null;
  }
  const payload = JSON.parse(atob(token.split('.')[1]))
  return payload.nickname
}

export default api