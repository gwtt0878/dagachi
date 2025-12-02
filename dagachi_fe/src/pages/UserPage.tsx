import { useState, useEffect } from 'react'
import { useNavigate, Link, useParams } from 'react-router-dom'
import { getUserById, getAuthoredPostings, getJoinedPostings } from '../api/user'
import { getCurrentNickname } from '../api/auth'
import { useToast } from '../hooks/useToast'
import type { User, PostingSimple } from '../types'
import Button from '../components/Button'
import NavBar from '../components/NavBar'
import { getTypeLabel, getStatusLabel, getStatusClass } from '../constants'
import '../styles/common.css'
import { AxiosError } from 'axios'

function UserPage() {
  const navigate = useNavigate()
  const { showToast, ToastContainer } = useToast()
  const [user, setUser] = useState<User | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const { id } = useParams<{ id: string }>()
  
  // 작성한 게시글 페이징
  const [authoredPostings, setAuthoredPostings] = useState<PostingSimple[]>([])
  const [authoredPage, setAuthoredPage] = useState(0)
  const [authoredTotalPages, setAuthoredTotalPages] = useState(0)
  
  // 참가한 게시글 페이징
  const [joinedPostings, setJoinedPostings] = useState<PostingSimple[]>([])
  const [joinedPage, setJoinedPage] = useState(0)
  const [joinedTotalPages, setJoinedTotalPages] = useState(0)

  useEffect(() => {
    const fetchUser = async () => {
      const token = localStorage.getItem('token')
      if (!token) {
        navigate('/login')
        return
      }

      if (!id) {
        setError('잘못된 접근입니다.')
        setLoading(false)
        return
      }

      try {
        const data = await getUserById(Number(id))
        setUser(data)
        const currentNickname = getCurrentNickname()
        if (currentNickname && data && currentNickname === data.nickname) {
          navigate('/users/me')
          return
        }
      } catch (err: unknown) {
        if (err instanceof AxiosError) {
          if (err.response?.status === 403 || err.response?.status === 401) {
            showToast('로그인이 필요합니다.', 'error')
            navigate('/login')
            return
          }
          if (err.response?.status === 404) {
            setError('존재하지 않는 사용자입니다.')
            return
          }
        }
        setError('사용자 정보를 불러오는데 실패했습니다.')
        console.error('사용자 조회 오류:', err)
      } finally {
        setLoading(false)
      }
    }

    fetchUser()
  }, [])
  
  // 작성한 게시글 로드
  useEffect(() => {
    if (!user) return
    
    const fetchAuthoredPostings = async () => {
      try {
        const data = await getAuthoredPostings(user.id, authoredPage)
        setAuthoredPostings(data.content)
        setAuthoredTotalPages(data.totalPages)
      } catch (err) {
        console.error('작성한 게시글 조회 오류:', err)
      }
    }
    
    fetchAuthoredPostings()
  }, [user, authoredPage])
  
  // 참가한 게시글 로드
  useEffect(() => {
    if (!user) return
    
    const fetchJoinedPostings = async () => {
      try {
        const data = await getJoinedPostings(user.id, joinedPage)
        setJoinedPostings(data.content)
        setJoinedTotalPages(data.totalPages)
      } catch (err) {
        console.error('참가한 게시글 조회 오류:', err)
      }
    }
    
    fetchJoinedPostings()
  }, [user, joinedPage])

  if (loading) {
    return (
      <>
        <NavBar />
        <div className="page-container">
          <p>불러오는 중...</p>
        </div>
      </>
    )
  }

  if (error) {
    return (
      <>
        <NavBar />
        <div className="page-container">
          <p className="error-message">{error}</p>
          <Button onClick={() => navigate('/')} variant="primary">
            홈으로 돌아가기
          </Button>
        </div>
      </>
    )
  }

  if (!user) {
    return null
  }

  return (
    <>
      <NavBar />
      <ToastContainer />
      <div className="page-container">
        <div className="user-profile">
          <div className="user-profile-header">
            <div className="user-avatar">
              <span style={{ fontSize: '3rem' }}>👤</span>
            </div>
            <div className="user-info">
              <h1 className="user-nickname">{user.nickname}</h1>
            </div>
          </div>

          {/* 작성한 게시글 */}
          <div className="user-section">
            <h2 className="section-title">
              📝 작성한 게시글
            </h2>
            {authoredPostings.length === 0 ? (
              <p className="empty-message">작성한 게시글이 없습니다.</p>
            ) : (
              <>
                <div className="postings-grid">
                  {authoredPostings.map((posting) => (
                    <Link
                      key={posting.id}
                      to={`/postings/${posting.id}`}
                      className="posting-card"
                    >
                      <div className="posting-card-header">
                        <h3 className="posting-card-title">{posting.title}</h3>
                        <div className="posting-card-badges">
                          <span className={`badge ${posting.type === 'PROJECT' ? 'badge-project' : 'badge-study'}`}>
                            {getTypeLabel(posting.type)}
                          </span>
                          <span className={`badge ${getStatusClass(posting.status)}`}>
                            {getStatusLabel(posting.status)}
                          </span>
                        </div>
                      </div>
                      <div className="posting-card-meta">
                        <span>👥 최대 {posting.maxCapacity}명</span>
                        <span>
                          {new Date(posting.createdAt).toLocaleDateString('ko-KR')}
                        </span>
                      </div>
                    </Link>
                  ))}
                </div>
                
                {/* 페이지네이션 */}
                {authoredTotalPages > 0 && (
                  <div style={{ 
                    display: 'flex', 
                    justifyContent: 'center', 
                    alignItems: 'center', 
                    gap: '10px', 
                    marginTop: '20px'
                  }}>
                    <Button
                      onClick={() => setAuthoredPage(0)}
                      disabled={authoredPage === 0}
                      variant="secondary"
                    >
                      처음
                    </Button>
                    <Button
                      onClick={() => setAuthoredPage(authoredPage - 1)}
                      disabled={authoredPage === 0}
                      variant="secondary"
                    >
                      이전
                    </Button>
                    <span style={{ padding: '0 15px', fontWeight: 'bold' }}>
                      {authoredPage + 1} / {authoredTotalPages}
                    </span>
                    <Button
                      onClick={() => setAuthoredPage(authoredPage + 1)}
                      disabled={authoredPage >= authoredTotalPages - 1}
                      variant="secondary"
                    >
                      다음
                    </Button>
                    <Button
                      onClick={() => setAuthoredPage(authoredTotalPages - 1)}
                      disabled={authoredPage >= authoredTotalPages - 1}
                      variant="secondary"
                    >
                      마지막
                    </Button>
                  </div>
                )}
              </>
            )}
          </div>

          {/* 참가한 게시글 */}
          <div className="user-section">
            <h2 className="section-title">
              🙋‍♂️ 참가한 게시글
            </h2>
            {joinedPostings.length === 0 ? (
              <p className="empty-message">참가한 게시글이 없습니다.</p>
            ) : (
              <>
                <div className="postings-grid">
                  {joinedPostings.map((posting) => (
                    <Link
                      key={posting.id}
                      to={`/postings/${posting.id}`}
                      className="posting-card"
                    >
                      <div className="posting-card-header">
                        <h3 className="posting-card-title">{posting.title}</h3>
                        <div className="posting-card-badges">
                          <span className={`badge ${posting.type === 'PROJECT' ? 'badge-project' : 'badge-study'}`}>
                            {getTypeLabel(posting.type)}
                          </span>
                          <span className={`badge ${getStatusClass(posting.status)}`}>
                            {getStatusLabel(posting.status)}
                          </span>
                        </div>
                      </div>
                      <div className="posting-card-meta">
                        <span>✍️ {posting.authorNickname}</span>
                        <span>
                          {new Date(posting.createdAt).toLocaleDateString('ko-KR')}
                        </span>
                      </div>
                    </Link>
                  ))}
                </div>
                
                {/* 페이지네이션 */}
                {joinedTotalPages > 0 && (
                  <div style={{ 
                    display: 'flex', 
                    justifyContent: 'center', 
                    alignItems: 'center', 
                    gap: '10px', 
                    marginTop: '20px'
                  }}>
                    <Button
                      onClick={() => setJoinedPage(0)}
                      disabled={joinedPage === 0}
                      variant="secondary"
                    >
                      처음
                    </Button>
                    <Button
                      onClick={() => setJoinedPage(joinedPage - 1)}
                      disabled={joinedPage === 0}
                      variant="secondary"
                    >
                      이전
                    </Button>
                    <span style={{ padding: '0 15px', fontWeight: 'bold' }}>
                      {joinedPage + 1} / {joinedTotalPages}
                    </span>
                    <Button
                      onClick={() => setJoinedPage(joinedPage + 1)}
                      disabled={joinedPage >= joinedTotalPages - 1}
                      variant="secondary"
                    >
                      다음
                    </Button>
                    <Button
                      onClick={() => setJoinedPage(joinedTotalPages - 1)}
                      disabled={joinedPage >= joinedTotalPages - 1}
                      variant="secondary"
                    >
                      마지막
                    </Button>
                  </div>
                )}
              </>
            )}
          </div>
        </div>
      </div>
    </>
  )
}

export default UserPage

