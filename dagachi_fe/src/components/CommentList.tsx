import { useState, useEffect } from 'react'
import { getComments, createComment, updateComment, deleteComment } from '../api/comment'
import { buildCommentTree, formatCommentDate } from '../utils/commentUtils'
import { getCurrentUser } from '../api/user'
import type { Comment, CommentCreateRequest, CommentUpdateRequest, PageResponse } from '../types'
import { useToast } from '../hooks/useToast'
import Button from './Button'
import '../styles/comment.css'
import { AxiosError } from 'axios'

interface CommentListProps {
  postingId: number
}

function CommentList({ postingId }: CommentListProps) {
  const { showToast, ToastContainer } = useToast()
  const [comments, setComments] = useState<Comment[]>([])
  const [loading, setLoading] = useState(true)
  const [newComment, setNewComment] = useState('')
  const [editingId, setEditingId] = useState<number | null>(null)
  const [editContent, setEditContent] = useState('')
  const [replyingTo, setReplyingTo] = useState<number | null>(null)
  const [replyContent, setReplyContent] = useState('')
  const [currentUserId, setCurrentUserId] = useState<number | null>(null)
  const [isAdmin, setIsAdmin] = useState(false)
  const [page, setPage] = useState(0)
  const [pageInfo, setPageInfo] = useState<PageResponse<Comment> | null>(null)

  useEffect(() => {
    loadComments()
    loadCurrentUser()
  }, [postingId, page])

  const loadCurrentUser = async () => {
    try {
      const user = await getCurrentUser()
      setCurrentUserId(user.id)
      setIsAdmin(user.role === 'ADMIN')
    } catch (error) {
      console.error('사용자 정보 로드 실패:', error)
    }
  }

  const loadComments = async () => {
    try {
      setLoading(true)
      const pageResponse = await getComments(postingId, page, 5)
      setPageInfo(pageResponse)
      const treeComments = buildCommentTree(pageResponse.content)
      setComments(treeComments)
    } catch (error) {
      console.error('댓글 로드 실패:', error)
      showToast('댓글을 불러오는데 실패했습니다.', 'error')
    } finally {
      setLoading(false)
    }
  }

  const handleCreateComment = async () => {
    if (!newComment.trim()) {
      showToast('댓글 내용을 입력해주세요.', 'error')
      return
    }

    try {
      const data: CommentCreateRequest = { content: newComment.trim() }
      await createComment(postingId, data)
      setNewComment('')
      setPage(0) // 첫 페이지로 이동
      showToast('댓글이 작성되었습니다.', 'success')
      await loadComments()
    } catch (error: unknown) {
      if (error instanceof AxiosError) {
        if (error.response?.status === 403) {
          showToast('댓글 작성 권한이 없습니다.', 'error')
        } else {
          showToast('댓글 작성에 실패했습니다.', 'error')
        }
      }
    }
  }

  const handleCreateReply = async (parentCommentId: number) => {
    if (!replyContent.trim()) {
      showToast('답글 내용을 입력해주세요.', 'error')
      return
    }

    try {
      const data: CommentCreateRequest = {
        content: replyContent.trim(),
        parentCommentId: parentCommentId
      }
      await createComment(postingId, data)
      setReplyContent('')
      setReplyingTo(null)
      setPage(0) // 첫 페이지로 이동
      showToast('답글이 작성되었습니다.', 'success')
      await loadComments()
    } catch (error: unknown) {
      if (error instanceof AxiosError) {
        if (error.response?.status === 403) {
          showToast('답글 작성 권한이 없습니다.', 'error')
        } else {
          showToast('답글 작성에 실패했습니다.', 'error')
        }
      }
    }
  }

  const handleStartEdit = (comment: Comment) => {
    setEditingId(comment.id)
    setEditContent(comment.content)
  }

  const handleCancelEdit = () => {
    setEditingId(null)
    setEditContent('')
  }

  const handleUpdateComment = async (commentId: number) => {
    if (!editContent.trim()) {
      showToast('댓글 내용을 입력해주세요.', 'error')
      return
    }

    try {
      const data: CommentUpdateRequest = { content: editContent.trim() }
      await updateComment(postingId, commentId, data)
      setEditingId(null)
      setEditContent('')
      showToast('댓글이 수정되었습니다.', 'success')
      await loadComments()
    } catch (error: unknown) {
      if (error instanceof AxiosError) {
        if (error.response?.status === 403) {
          showToast('댓글 수정 권한이 없습니다.', 'error')
        } else {
          showToast('댓글 수정에 실패했습니다.', 'error')
        }
      }
    }
  }

  const handleDeleteComment = async (commentId: number) => {
    if (!confirm('정말로 이 댓글을 삭제하시겠습니까?')) {
      return
    }

    try {
      await deleteComment(postingId, commentId)
      showToast('댓글이 삭제되었습니다.', 'success')
      await loadComments()
    } catch (error: unknown) {
      if (error instanceof AxiosError) {
        if (error.response?.status === 403) {
          showToast('댓글 삭제 권한이 없습니다.', 'error')
        } else {
          showToast('댓글 삭제에 실패했습니다.', 'error')
        }
      }
    }
  }

  const canEditOrDelete = (comment: Comment): boolean => {
    if (!currentUserId) return false
    return comment.authorId === currentUserId || isAdmin
  }

  if (loading) {
    return (
      <div className="comment-section">
        <h2>댓글</h2>
        <p>댓글을 불러오는 중...</p>
      </div>
    )
  }

  const totalComments = pageInfo?.totalElements || comments.reduce((acc, c) => acc + 1 + (c.replies?.length || 0), 0)

  return (
    <div className="comment-section">
      <ToastContainer />
      <h2>댓글 ({totalComments})</h2>

      {/* 댓글 작성 폼 */}
      <div className="comment-form">
        <textarea
          id="new-comment"
          name="newComment"
          className="comment-textarea"
          placeholder="댓글을 입력하세요..."
          value={newComment}
          onChange={(e) => setNewComment(e.target.value)}
          rows={3}
        />
        <Button onClick={handleCreateComment} variant="primary" style={{ marginTop: '10px' }}>
          댓글 작성
        </Button>
      </div>

      {/* 댓글 목록 */}
      <div className="comment-list">
        {comments.length === 0 && !loading ? (
          <p className="no-comments">아직 댓글이 없습니다. 첫 댓글을 작성해보세요!</p>
        ) : (
          comments.map((comment) => (
            <CommentItem
              key={comment.id}
              comment={comment}
              depth={0}
              onReply={(id) => {
                setReplyingTo(id)
                setReplyContent('')
              }}
              onCancelReply={() => {
                setReplyingTo(null)
                setReplyContent('')
              }}
              replyingTo={replyingTo}
              replyContent={replyContent}
              onReplyContentChange={setReplyContent}
              onCreateReply={handleCreateReply}
              editingId={editingId}
              editContent={editContent}
              onEditContentChange={setEditContent}
              onStartEdit={handleStartEdit}
              onCancelEdit={handleCancelEdit}
              onUpdate={handleUpdateComment}
              onDelete={handleDeleteComment}
              canEditOrDelete={canEditOrDelete}
            />
          ))
        )}
      </div>

      {/* 페이지네이션 */}
      {pageInfo && pageInfo.totalPages > 1 && (
        <div style={{ display: 'flex', justifyContent: 'center', gap: '10px', marginTop: '20px', alignItems: 'center' }}>
          <Button
            onClick={() => setPage(page - 1)}
            disabled={page === 0}
            variant="secondary"
            style={{ padding: '8px 16px' }}
          >
            이전
          </Button>
          <span style={{ fontSize: '14px', color: '#666' }}>
            {page + 1} / {pageInfo.totalPages}
          </span>
          <Button
            onClick={() => setPage(page + 1)}
            disabled={page >= pageInfo.totalPages - 1}
            variant="secondary"
            style={{ padding: '8px 16px' }}
          >
            다음
          </Button>
        </div>
      )}
    </div>
  )
}

interface CommentItemProps {
  comment: Comment
  depth: number
  onReply: (id: number) => void
  onCancelReply: () => void
  replyingTo: number | null
  replyContent: string
  onReplyContentChange: (content: string) => void
  onCreateReply: (parentId: number) => void
  editingId: number | null
  editContent: string
  onEditContentChange: (content: string) => void
  onStartEdit: (comment: Comment) => void
  onCancelEdit: () => void
  onUpdate: (id: number) => void
  onDelete: (id: number) => void
  canEditOrDelete: (comment: Comment) => boolean
}

function CommentItem({
  comment,
  depth,
  onReply,
  onCancelReply,
  replyingTo,
  replyContent,
  onReplyContentChange,
  onCreateReply,
  editingId,
  editContent,
  onEditContentChange,
  onStartEdit,
  onCancelEdit,
  onUpdate,
  onDelete,
  canEditOrDelete
}: CommentItemProps) {
  const isEditing = editingId === comment.id
  const isReplying = replyingTo === comment.id

  return (
    <div className={`comment-item ${depth > 0 ? 'reply-item' : ''}`}>
      <div className="comment-header">
        <span className="comment-author">{comment.authorNickname}</span>
        <span className="comment-date">{formatCommentDate(comment.createdAt)}</span>
        {comment.updatedAt !== comment.createdAt && (
          <span className="comment-edited">(수정됨)</span>
        )}
      </div>

      {isEditing ? (
        <div className="comment-edit-form">
          <textarea
            id={`edit-comment-${comment.id}`}
            name={`editComment-${comment.id}`}
            className="comment-textarea"
            value={editContent}
            onChange={(e) => onEditContentChange(e.target.value)}
            placeholder="댓글 내용을 입력하세요..."
            rows={3}
          />
          <div className="comment-actions">
            <Button onClick={() => onUpdate(comment.id)} variant="primary" style={{ fontSize: '12px', padding: '4px 8px' }}>
              저장
            </Button>
            <Button onClick={onCancelEdit} variant="secondary" style={{ fontSize: '12px', padding: '4px 8px' }}>
              취소
            </Button>
          </div>
        </div>
      ) : (
        <>
          <div className="comment-content">
            {comment.deletedAt !== null ? (
              <span style={{ color: '#999', fontStyle: 'italic' }}>{comment.content}</span>
            ) : (
              comment.content
            )}
          </div>

          {comment.deletedAt === null && (
            <div className="comment-actions">
              {(
                <button className="reply-button" onClick={() => onReply(comment.id)}>
                  답글
                </button>
              )}
              {canEditOrDelete(comment) && (
                <>
                  <button className="edit-button" onClick={() => onStartEdit(comment)}>
                    수정
                  </button>
                  <button className="delete-button" onClick={() => onDelete(comment.id)}>
                    삭제
                  </button>
                </>
              )}
            </div>
          )}

          {/* 답글 작성 폼 */}
          {isReplying && (
            <div className="reply-form">
              <textarea
                id={`reply-comment-${comment.id}`}
                name={`replyComment-${comment.id}`}
                className="comment-textarea"
                value={replyContent}
                onChange={(e) => onReplyContentChange(e.target.value)}
                placeholder={`${comment.authorNickname}님에게 답글 달기...`}
                rows={2}
              />
              <div className="reply-actions">
                <Button
                  onClick={() => onCreateReply(comment.id)}
                  variant="primary"
                  style={{ fontSize: '12px', padding: '4px 8px' }}
                >
                  작성
                </Button>
                <Button
                  onClick={onCancelReply}
                  variant="secondary"
                  style={{ fontSize: '12px', padding: '4px 8px' }}
                >
                  취소
                </Button>
              </div>
            </div>
          )}
        </>
      )}

      {/* 대댓글들 재귀적으로 렌더링 */}
      {comment.replies && comment.replies.length > 0 && (
        <div className="replies">
          {comment.replies.map((reply) => (
            <CommentItem
              key={reply.id}
              comment={reply}
              depth={depth + 1}
              onReply={onReply}
              onCancelReply={onCancelReply}
              replyingTo={replyingTo}
              replyContent={replyContent}
              onReplyContentChange={onReplyContentChange}
              onCreateReply={onCreateReply}
              editingId={editingId}
              editContent={editContent}
              onEditContentChange={onEditContentChange}
              onStartEdit={onStartEdit}
              onCancelEdit={onCancelEdit}
              onUpdate={onUpdate}
              onDelete={onDelete}
              canEditOrDelete={canEditOrDelete}
            />
          ))}
        </div>
      )}
    </div>
  )
}

export default CommentList
