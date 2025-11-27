export interface Posting {
  id: number
  title: string
  description: string
  type: string
  status: string
  maxCapacity: number
  createdAt: string
  updatedAt: string
  authorId: number
  authorNickname: string
}

export interface PostingSimple {
  id: number
  title: string
  type: string
  status: string
  maxCapacity: number
  createdAt: string
  authorNickname: string
}

export interface User {
  id: number
  username: string
  nickname: string
  role: string
  authoredPostings: PostingSimple[]
  joinedPostings: PostingSimple[]
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