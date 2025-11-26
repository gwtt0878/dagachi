import api from './auth'
import type { User } from '../types'

// 특정 유저 조회
export const getUserById = async (id: number): Promise<User> => {
  const response = await api.get<User>(`/api/users/${id}`)
  return response.data
}

export const getCurrentUser = async (): Promise<User> => {
  const response = await api.get<User>(`/api/users/me`)
  return response.data
}