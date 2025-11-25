import { useState, useEffect, useCallback } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import Button from '../components/Button'
import Modal from '../components/Modal'
import { getPostingById, deletePosting } from '../api/posting'
import { useToast } from '../hooks/useToast'
import type { Posting } from '../types'
import '../styles/common.css'
import { AxiosError } from 'axios'
import { getCurrentNickname } from '../api/auth'

function PostingDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { showToast, ToastContainer } = useToast()
  const [posting, setPosting] = useState<Posting | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [showLoginModal, setShowLoginModal] = useState(false)
  const [showDeleteModal, setShowDeleteModal] = useState(false)
  const [deleting, setDeleting] = useState(false)
  const [isAuthor, setIsAuthor] = useState(false)

  const fetchPosting = useCallback(async () => {
    const token = localStorage.getItem('token')
    if (!token) {
      setShowLoginModal(true)
      setLoading(false)
      return
    }

    if (!id) {
      setError('잘못된 접근입니다.')
      setLoading(false)
      return
    }

    try {
      const data = await getPostingById(Number(id))
      setPosting(data)
      const nickname = getCurrentNickname()
      if (nickname && data && nickname === data.authorNickname) {
        setIsAuthor(true)
      }
    } catch (err: unknown) {
      if (err instanceof AxiosError) {
        if (err.response?.status === 403) {
          navigate('/login')
          return
        }
        if (err.response?.status === 404) {
          setError('존재하지 않는 게시글입니다.')
          setLoading(false)
          return
        } else {
          setError('게시글을 불러오는데 실패했습니다.')
          setLoading(false)
          return
        }
      }
    } finally {
      setLoading(false)
    }
  }, [id])

  useEffect(() => {
    fetchPosting()
  }, [fetchPosting])

  const handleDelete = async () => {
    if (!id) return

    setDeleting(true)
    try {
      await deletePosting(Number(id))
      showToast('게시글이 삭제되었습니다.', 'success')
      setTimeout(() => {
        navigate('/postings')
      }, 1000)
    } catch (err: unknown) {
      if (err instanceof AxiosError) {
        if (err.response?.status === 403) {
          showToast('삭제 권한이 없습니다.', 'error')
          return
        }
        if (err.response?.status === 404) {
          showToast('게시글을 찾을 수 없습니다.', 'error')
          return
        }
        showToast('게시글 삭제에 실패했습니다.', 'error')
        console.error('삭제 오류:', err)
      }
    } finally {
      setDeleting(false)
      setShowDeleteModal(false)
    }
  }

  const getTypeLabel = (type: string) => {
    return type === 'PROJECT' ? '프로젝트' : '스터디'
  }

  const getStatusLabel = (status: string) => {
    switch (status) {
      case 'RECRUITING':
        return '모집중'
      case 'CLOSED':
        return '모집완료'
      case 'ENDED':
        return '종료'
      default:
        return status
    }
  }

  const getStatusClass = (status: string) => {
    switch (status) {
      case 'RECRUITING':
        return 'status-recruiting'
      case 'CLOSED':
        return 'status-closed'
      case 'ENDED':
        return 'status-ended'
      default:
        return ''
    }
  }

  if (loading) {
    return (
      <div className="page-container">
        <p>불러오는 중...</p>
      </div>
    )
  }

  if (error) {
    return (
      <div className="page-container">
        <p className="error-message">{error}</p>
        <Button onClick={() => navigate('/postings')} variant="primary">
          목록으로 돌아가기
        </Button>
      </div>
    )
  }

  if (!posting) {
    return null
  }

  return (
    <>
      <ToastContainer />
      <div className="page-container">
        <Modal
          isOpen={showLoginModal}
          onClose={() => {
            setShowLoginModal(false)
            navigate('/')
          }}
          title="🔒 로그인 필요"
        >
          <p style={{ marginBottom: '20px', fontSize: '16px', lineHeight: '1.6' }}>
            게시글을 보려면 로그인이 필요합니다.
          </p>
          <div style={{ display: 'flex', gap: '10px' }}>
            <Button
              variant="primary"
              onClick={() => navigate('/login')}
              style={{ flex: 1 }}
            >
              로그인하기
            </Button>
            <Button
              variant="secondary"
              onClick={() => navigate('/')}
              style={{ flex: 1 }}
            >
              홈으로
            </Button>
          </div>
        </Modal>

        {/* 삭제 확인 모달 */}
        <Modal
          isOpen={showDeleteModal}
          onClose={() => setShowDeleteModal(false)}
          title="⚠️ 게시글 삭제"
        >
          <p style={{ marginBottom: '20px', fontSize: '16px', lineHeight: '1.6' }}>
            정말로 이 게시글을 삭제하시겠습니까?<br />
            삭제된 게시글은 복구할 수 없습니다.
          </p>
          <div style={{ display: 'flex', gap: '10px' }}>
            <Button
              variant="secondary"
              onClick={() => setShowDeleteModal(false)}
              style={{ flex: 1 }}
              disabled={deleting}
            >
              취소
            </Button>
            <Button
              variant="primary"
              onClick={handleDelete}
              style={{ flex: 1, backgroundColor: '#ef4444' }}
              disabled={deleting}
            >
              {deleting ? '삭제 중...' : '삭제'}
            </Button>
          </div>
        </Modal>

      <div className="link-group">
        <button onClick={() => navigate('/postings')} className="btn btn-primary">
          ← 목록으로 돌아가기
        </button>
      </div>

      <div className="posting-detail">
        <div className="posting-detail-header">
          <div className="posting-detail-title-wrapper">
            <h1 className="posting-detail-title">{posting.title}</h1>
            <div className="posting-detail-badges">
              <span className={`badge ${posting.type === 'PROJECT' ? 'badge-project' : 'badge-study'}`}>
                {getTypeLabel(posting.type)}
              </span>
              <span className={`badge ${getStatusClass(posting.status)}`}>
                {getStatusLabel(posting.status)}
              </span>
            </div>
          </div>
        </div>

        <div className="posting-detail-meta">
          <div className="meta-item">
            <span className="meta-label">작성자</span>
            <span className="meta-value">{posting.authorNickname}</span>
          </div>
          <div className="meta-item">
            <span className="meta-label">최대 인원</span>
            <span className="meta-value">{posting.maxCapacity}명</span>
          </div>
          <div className="meta-item">
            <span className="meta-label">작성일</span>
            <span className="meta-value">
              {new Date(posting.createdAt).toLocaleDateString('ko-KR', {
                year: 'numeric',
                month: 'long',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit'
              })}
            </span>
          </div>
          {posting.updatedAt !== posting.createdAt && (
            <div className="meta-item">
              <span className="meta-label">수정일</span>
              <span className="meta-value">
                {new Date(posting.updatedAt).toLocaleDateString('ko-KR', {
                  year: 'numeric',
                  month: 'long',
                  day: 'numeric',
                  hour: '2-digit',
                  minute: '2-digit'
                })}
              </span>
            </div>
          )}
        </div>

        <div className="posting-detail-content">
          <h2>상세 설명</h2>
          <div className="posting-description">
            {posting.description}
          </div>
        </div>

        <div className="posting-detail-actions">
          <Button onClick={() => navigate('/postings')} variant="secondary">
            목록으로
          </Button>
          
        </div>
        <div className="posting-detail-actions-author">
          {isAuthor && (
            <Button 
              onClick={() => setShowDeleteModal(true)} 
              variant="primary"
              style={{ backgroundColor: '#ef4444' }}
            >
              삭제
            </Button>
          )}
        </div>
      </div>
    </div>
    </>
  )
}

export default PostingDetailPage

