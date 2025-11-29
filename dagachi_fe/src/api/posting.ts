import api from './auth'
import type { Posting, Participation, PageResponse } from '../types'

export interface CreatePostingRequest {
  title: string
  description: string
  maxCapacity: number
  type: 'PROJECT' | 'STUDY'
}

export interface UpdatePostingRequest {
  title: string
  description: string
  maxCapacity: number
  type: 'PROJECT' | 'STUDY'
  status: 'RECRUITING' | 'IN_PROGRESS' | 'COMPLETED'
}

export interface SearchPostingParams {
  title?: string
  type?: 'PROJECT' | 'STUDY' | ''
  status?: 'RECRUITING' | 'IN_PROGRESS' | 'COMPLETED' | ''
  authorNickname?: string
  page?: number
}

// 모든 포스팅 목록 조회 (페이징)
export const getAllPostings = async (page: number = 0): Promise<PageResponse<Posting>> => {
  const response = await api.get<PageResponse<Posting>>(`/api/postings?page=${page}`)
  return response.data
}

// 포스팅 검색 (페이징)
export const searchPostings = async (params: SearchPostingParams): Promise<PageResponse<Posting>> => {
  const queryParams = new URLSearchParams()
  
  if (params.title) queryParams.append('title', params.title)
  if (params.type) queryParams.append('type', params.type)
  if (params.status) queryParams.append('status', params.status)
  if (params.authorNickname) queryParams.append('authorNickname', params.authorNickname)
  queryParams.append('page', String(params.page || 0))
  
  const response = await api.get<PageResponse<Posting>>(`/api/postings/search?${queryParams.toString()}`)
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

// 포스팅 수정
export const updatePosting = async (id: number, data: UpdatePostingRequest): Promise<Posting> => {
  const response = await api.put<Posting>(`/api/postings/${id}`, data)
  return response.data
}

// 포스팅 삭제
export const deletePosting = async (id: number): Promise<void> => {
  await api.delete(`/api/postings/${id}`)
}

// 포스팅 참가 여부 확인
export const checkParticipation = async (postingId: number): Promise<boolean> => {
  const response = await api.get<boolean>(`/api/participation/${postingId}/check`)
  return response.data
}

// 포스팅 참가
export const joinPosting = async (postingId: number): Promise<void> => {
  await api.post(`/api/participation/${postingId}`)
}

// 포스팅 참가 취소
export const leavePosting = async (postingId: number): Promise<void> => {
  await api.delete(`/api/participation/${postingId}`)
}

// 참가자 목록 조회
export const getParticipations = async (postingId: number): Promise<Participation[]> => {
  const response = await api.get<Participation[]>(`/api/participation/${postingId}`)
  return response.data
}

// 참가자 승인
export const approveParticipation = async (postingId: number, participationId: number): Promise<void> => {
  await api.post(`/api/participation/${postingId}/approve/${participationId}`)
}

// 참가자 거절
export const rejectParticipation = async (postingId: number, participationId: number): Promise<void> => {
  await api.delete(`/api/participation/${postingId}/user/${participationId}`)
}

