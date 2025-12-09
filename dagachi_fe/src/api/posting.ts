import api from './auth'
import type { Posting, Participation, ParticipationSimple, PageResponse, PostingSimple } from '../types'

export interface CreatePostingRequest {
  title: string
  description: string
  maxCapacity: number
  type: 'PROJECT' | 'STUDY'
  latitude: number
  longitude: number
}

export interface UpdatePostingRequest {
  title: string
  description: string
  maxCapacity: number
  type: 'PROJECT' | 'STUDY'
  status: 'RECRUITING' | 'RECRUITED' | 'COMPLETED'
  latitude: number
  longitude: number
}

export interface SearchPostingParams {
  title?: string
  type?: 'PROJECT' | 'STUDY' | ''
  status?: 'RECRUITING' | 'RECRUITED' | 'COMPLETED' | ''
  authorNickname?: string
  page?: number
  // 거리순 정렬을 위한 파라미터
  userLatitude?: number
  userLongitude?: number
  sortByDistance?: boolean
}

// 모든 포스팅 목록 조회 (페이징)
export const getAllPostings = async (page: number = 0): Promise<PageResponse<PostingSimple>> => {
  const response = await api.get<PageResponse<PostingSimple>>(`/api/postings?page=${page}`)
  return response.data
}

// 포스팅 검색 (페이징)
export const searchPostings = async (params: SearchPostingParams): Promise<PageResponse<PostingSimple>> => {
  // 페이지 정보는 query parameter로 유지
  const queryParams = new URLSearchParams()
  queryParams.append('page', String(params.page || 0))
  
  // 검색 조건은 request body로 전송
  const searchBody = {
    title: params.title || undefined,
    type: params.type || undefined,
    status: params.status || undefined,
    authorNickname: params.authorNickname || undefined,
    userLatitude: params.userLatitude,
    userLongitude: params.userLongitude,
    sortByDistance: params.sortByDistance || false
  }
  
  const response = await api.post<PageResponse<PostingSimple>>(`/api/postings/search?${queryParams.toString()}`, searchBody)
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
export const checkParticipation = async (postingId: number): Promise<ParticipationSimple> => {
  const response = await api.get<ParticipationSimple>(`/api/participation/${postingId}/check`)
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

// 참가자 승인 취소 (승인된 참가자를 거절 처리)
export const cancelApproval = async (postingId: number, participationId: number): Promise<void> => {
  await api.delete(`/api/participation/${postingId}/user/${participationId}`)
}

