import type { Comment } from '../types'

/**
 * 평면 댓글 리스트를 트리 구조로 변환
 */
export function buildCommentTree(comments: Comment[]): Comment[] {
  // 댓글 맵 생성 (빠른 조회를 위해)
  const commentMap = new Map<number, Comment>()
  const rootComments: Comment[] = []

  // 모든 댓글을 맵에 저장하고 replies 배열 초기화
  comments.forEach(comment => {
    commentMap.set(comment.id, { ...comment, replies: [] })
  })

  // 트리 구조 생성
  comments.forEach(comment => {
    const commentWithReplies = commentMap.get(comment.id)!

    if (comment.parentCommentId === null) {
      // 루트 댓글 (대댓글이 아닌 댓글)
      rootComments.push(commentWithReplies)
    } else {
      // 대댓글인 경우 부모 댓글의 replies에 추가
      const parent = commentMap.get(comment.parentCommentId)
      if (parent) {
        if (!parent.replies) {
          parent.replies = []
        }
        parent.replies.push(commentWithReplies)
      }
    }
  })

  return rootComments
}

/**
 * 날짜 포맷팅
 */
export function formatCommentDate(dateString: string): string {
  const date = new Date(dateString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)

  if (seconds < 60) {
    return '방금 전'
  } else if (minutes < 60) {
    return `${minutes}분 전`
  } else if (hours < 24) {
    return `${hours}시간 전`
  } else if (days < 7) {
    return `${days}일 전`
  } else {
    return date.toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }
}
