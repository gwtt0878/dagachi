import api from './auth'
import type { Posting } from '../types'

export interface CreatePostingRequest {
  title: string
  description: string
  maxCapacity: number
  type: 'PROJECT' | 'STUDY'
}

// 모든 포스팅 목록 조회
export const getAllPostings = async (): Promise<Posting[]> => {
  const response = await api.get<Posting[]>('/api/postings')
  return response.data
}

// 특정 포스팅 상세 조회
export const getPostingById = async (id: number): Promise<Posting> => {
  const response = await api.get<Posting>(`/api/postings/${id}`)
  return response.data
}

// 포스팅 작성
export const createPosting = async (data: CreatePostingRequest): Promise<Posting> => {
  const response = await api.post<Posting>('/api/postings', data)
  return response.data
}

// 포스팅 삭제
export const deletePosting = async (id: number): Promise<void> => {
  await api.delete(`/api/postings/${id}`)
}

