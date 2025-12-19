import api from './auth'
import type { Comment, CommentCreateRequest, CommentUpdateRequest, PageResponse } from '../types'

// 댓글 목록 조회 (페이징)
export const getComments = async (
  postingId: number,
  page: number = 0,
  size: number = 5,
  sort: string = 'createdAt,asc'
): Promise<PageResponse<Comment>> => {
  const response = await api.get<PageResponse<Comment>>(
    `/api/postings/${postingId}/comment?page=${page}&size=${size}&sort=${sort}`
  )
  return response.data
}

// 댓글 작성
export const createComment = async (
  postingId: number,
  data: CommentCreateRequest
): Promise<Comment> => {
  const response = await api.post<Comment>(`/api/postings/${postingId}/comment`, data)
  return response.data
}

// 댓글 수정
export const updateComment = async (
  postingId: number,
  commentId: number,
  data: CommentUpdateRequest
): Promise<Comment> => {
  const response = await api.put<Comment>(
    `/api/postings/${postingId}/comment/${commentId}`,
    data
  )
  return response.data
}

// 댓글 삭제
export const deleteComment = async (postingId: number, commentId: number): Promise<void> => {
  await api.delete(`/api/postings/${postingId}/comment/${commentId}`)
}
