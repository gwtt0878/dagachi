export interface Posting {
  id: number
  title: string
  description: string
  type: string
  status: string
  maxCapacity: number
  createdAt: string
  updatedAt: string
  latitude: number
  longitude: number
  authorId: number
  authorNickname: string
}

export interface PostingSimple {
  id: number
  title: string
  type: string
  status: string
  latitude: number
  longitude: number
  maxCapacity: number
  createdAt: string
  updatedAt: string
  authorNickname: string
}

export interface User {
  id: number
  username: string
  nickname: string
  role: string
}

export interface Participation {
  participationId: number
  postingId: number
  participantId: number
  participantNickname: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED'
  createdAt: string
  updatedAt: string
}

export interface ParticipationSimple {
  participationId: number | null
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | null
  createdAt: string | null
}

export interface PageResponse<T> {
  content: T[]
  pageable: {
    pageNumber: number
    pageSize: number
  }
  totalElements: number
  totalPages: number
  last: boolean
  first: boolean
  number: number
  size: number
  numberOfElements: number
  empty: boolean
}

export interface Comment {
  id: number
  parentCommentId: number | null
  authorId: number
  content: string
  createdAt: string
  updatedAt: string
  deletedAt: string | null
  authorNickname: string
  depth: number
  replies?: Comment[] // 트리 구조를 위한 필드
}

export interface CommentCreateRequest {
  content: string
  parentCommentId?: number | null
}

export interface CommentUpdateRequest {
  content: string
}