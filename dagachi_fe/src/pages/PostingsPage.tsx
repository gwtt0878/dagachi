import { useState, useEffect, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import Button from '../components/Button'
import Modal from '../components/Modal'
import '../styles/common.css'
import type { Posting } from '../types'
import api from '../api/auth'
import { AxiosError } from 'axios'



function PostingsPage() {
  const navigate = useNavigate()
  const [postings, setPostings] = useState<Posting[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [showLoginModal, setShowLoginModal] = useState(false)

  const fetchPostings = useCallback(async () => {
    setLoading(true)
    setError(null)
    
    try {
      const response = await api.get<Posting[]>('/api/postings')
      setPostings(response.data)
    } catch (err) {
      if (err instanceof AxiosError && err.status === 403) {
        navigate('/login')
        return
      }
      setError('게시글을 불러오는데 실패했습니다.')
      console.error('API 호출 오류:', err)
    } finally {
      setLoading(false)
    }
  }, [navigate])

  useEffect(() => {
    // 로그인 체크
    const token = localStorage.getItem('token')
    if (!token) {
      setShowLoginModal(true)
      setLoading(false)
      return
    }
    
    fetchPostings()
  }, [fetchPostings])

  return (
    <div className="page-container">
      {/* 로그인 필요 모달 */}
      <Modal
        isOpen={showLoginModal}
        onClose={() => {
          setShowLoginModal(false)
          navigate('/')
        }}
        title="🔒 로그인 필요"
      >
        <p style={{ marginBottom: '20px', fontSize: '16px', lineHeight: '1.6' }}>
          프로젝트와 스터디 목록을 보려면 로그인이 필요합니다.
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

      <div className="link-group">
        <button onClick={() => navigate('/')} className="btn btn-primary">
          ← 홈으로 돌아가기
        </button>
      </div>

      <h1>프로젝트 & 스터디 목록</h1>

      {loading && <p>불러오는 중...</p>}
      
      {error && <p className="error-message">{error}</p>}

      {!loading && !error && postings.length === 0 && (
        <p>등록된 게시글이 없습니다.</p>
      )}

      {!loading && postings.length > 0 && (
        <div className="postings-list">
          {postings.map((posting) => (
            <div key={posting.id} className="card">
              <div className="posting-header">
                <h2 className="posting-title">{posting.title}</h2>
                <span className={`badge ${posting.type === 'PROJECT' ? 'badge-project' : 'badge-study'}`}>
                  {posting.type}
                </span>
              </div>
              <p className="posting-description">{posting.description}</p>
              <p className="posting-created-at">
                {new Date(posting.createdAt).toLocaleDateString('ko-KR', {
                  year: 'numeric',
                  month: 'long',
                  day: 'numeric',
                  hour: '2-digit',
                  minute: '2-digit'
                })}
              </p>
            </div>
          ))}
        </div>
      )}

      <div className="refresh-button">
        <Button onClick={fetchPostings} variant="primary">
          새로고침
        </Button>
      </div>
    </div>
  )
}

export default PostingsPage

