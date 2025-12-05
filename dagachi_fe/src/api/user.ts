import api from './auth'
import type { User, PageResponse, PostingSimple } from '../types'

// 특정 유저 조회
export const getUserById = async (id: number): Promise<User> => {
  const response = await api.get<User>(`/api/users/${id}`)
  return response.data
}

export const getCurrentUser = async (): Promise<User> => {
  const response = await api.get<User>(`/api/users/me`)
  return response.data
}

// 관리자: 사용자 목록 조회 (페이징)
export const getAdminUsers = async (page: number = 0): Promise<PageResponse<User>> => {
  const response = await api.get<PageResponse<User>>(`/api/admin/users?page=${page}`)
  return response.data
}

// 관리자: 사용자 권한 변경
export const updateUserRole = async (userId: number, role: 'USER' | 'ADMIN'): Promise<void> => {
  await api.put(`/api/admin/users/${userId}/role`, role)
}

// 사용자가 작성한 게시글 조회 (페이징)
export const getAuthoredPostings = async (userId: number, page: number = 0): Promise<PageResponse<PostingSimple>> => {
  const response = await api.get<PageResponse<PostingSimple>>(`/api/users/${userId}/authored?page=${page}`)
  return response.data
}

// 사용자가 참가한 게시글 조회 (페이징)
export const getJoinedPostings = async (userId: number, page: number = 0): Promise<PageResponse<PostingSimple>> => {
  const response = await api.get<PageResponse<PostingSimple>>(`/api/users/${userId}/joined?page=${page}`)
  return response.data
}