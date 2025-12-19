import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import Button from '../components/Button'
import NavBar from '../components/NavBar'
import { getPostingById, getParticipations, approveParticipation, rejectParticipation, cancelApproval } from '../api/posting'
import { useToast } from '../hooks/useToast'
import type { Posting, Participation, PageResponse } from '../types'
import { AxiosError } from 'axios'
import { getCurrentNickname } from '../api/auth'
import { getTypeLabel, getStatusLabel, getStatusClass } from '../constants'
import '../styles/common.css'

function ParticipationManagePage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { showToast, ToastContainer } = useToast()
  const [posting, setPosting] = useState<Posting | null>(null)
  const [participations, setParticipations] = useState<Participation[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [processingId, setProcessingId] = useState<number | null>(null)
  const [page, setPage] = useState(0)
  const [pageInfo, setPageInfo] = useState<PageResponse<Participation> | null>(null)

  const fetchData = async () => {
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
      const postingData = await getPostingById(Number(id))
      setPosting(postingData)

      const nickname = getCurrentNickname()
      if (!nickname || nickname !== postingData.authorNickname) {
        setError('게시글 작성자만 접근할 수 있습니다.')
        setLoading(false)
        return
      }

      const participationsData = await getParticipations(Number(id), page, 5)
      setPageInfo(participationsData)
      setParticipations(participationsData.content)
    } catch (err: unknown) {
      if (err instanceof AxiosError) {
        if (err.response?.status === 403) {
          navigate('/login')
          return
        }
        if (err.response?.status === 404) {
          setError('존재하지 않는 게시글입니다.')
        } else {
          setError('데이터를 불러오는데 실패했습니다.')
        }
      }
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchData()
  }, [id, page])

  const handleApprove = async (participationId: number) => {
    if (!id || processingId) return

    setProcessingId(participationId)
    try {
      await approveParticipation(Number(id), participationId)
      showToast('참가자를 승인했습니다! ✅', 'success')
      setPage(0) // 첫 페이지로 이동
      await fetchData()
    } catch (err: unknown) {
      if (err instanceof AxiosError) {
        if (err.response?.status === 400) {
          showToast('이미 승인되었거나 인원이 가득 찼습니다.', 'error')
        } else {
          showToast('승인에 실패했습니다.', 'error')
        }
      }
    } finally {
      setProcessingId(null)
    }
  }

  const handleReject = async (participationId: number) => {
    if (!id || processingId) return

    setProcessingId(participationId)
    try {
      await rejectParticipation(Number(id), participationId)
      showToast('참가자를 거절했습니다.', 'success')
      setPage(0) // 첫 페이지로 이동
      await fetchData()
    } catch (err: unknown) {
      if (err instanceof AxiosError) {
        showToast('거절에 실패했습니다.', 'error')
      }
    } finally {
      setProcessingId(null)
    }
  }

  const handleCancelApproval = async (participationId: number) => {
    if (!id || processingId) return

    const confirmed = window.confirm('정말로 이 참가자의 승인을 취소하시겠습니까?')
    if (!confirmed) return

    setProcessingId(participationId)
    try {
      await cancelApproval(Number(id), participationId)
      showToast('참가자 승인을 취소했습니다.', 'success')
      setPage(0) // 첫 페이지로 이동
      await fetchData()
    } catch (err: unknown) {
      if (err instanceof AxiosError) {
        showToast('승인 취소에 실패했습니다.', 'error')
      }
    } finally {
      setProcessingId(null)
    }
  }

  const getStatusBadgeStyle = (status: string) => {
    switch (status) {
      case 'APPROVED':
        return { backgroundColor: '#10b981', color: 'white' }
      case 'REJECTED':
        return { backgroundColor: '#ef4444', color: 'white' }
      case 'PENDING':
      default:
        return { backgroundColor: '#f59e0b', color: 'white' }
    }
  }

  const getStatusText = (status: string) => {
    switch (status) {
      case 'APPROVED':
        return '승인됨'
      case 'REJECTED':
        return '거절됨'
      case 'PENDING':
      default:
        return '대기 중'
    }
  }

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
          <Button onClick={() => navigate('/postings')} variant="primary">
            목록으로 돌아가기
          </Button>
        </div>
      </>
    )
  }

  if (!posting) {
    return null
  }

  const pendingParticipations = participations.filter(p => p.status === 'PENDING')
  const approvedParticipations = participations.filter(p => p.status === 'APPROVED')
  const rejectedParticipations = participations.filter(p => p.status === 'REJECTED')

  return (
    <>
      <NavBar />
      <ToastContainer />
      <div className="page-container">
        <div className="link-group">
          <button onClick={() => navigate(`/postings/${id}`)} className="btn btn-primary">
            ← 게시글로 돌아가기
          </button>
        </div>

        <div className="posting-detail">
          <div className="posting-detail-header">
            <div className="posting-detail-title-wrapper">
              <h1 className="posting-detail-title">{posting.title} - 참가자 관리</h1>
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
              <span className="meta-label">최대 인원</span>
              <span className="meta-value">{posting.maxCapacity}명</span>
            </div>
            <div className="meta-item">
              <span className="meta-label">승인된 인원</span>
              <span className="meta-value">{approvedParticipations.length}명</span>
            </div>
            <div className="meta-item">
              <span className="meta-label">대기 중</span>
              <span className="meta-value">{pendingParticipations.length}명</span>
            </div>
          </div>

          {/* 대기 중인 참가자 */}
          {pendingParticipations.length > 0 && (
            <div style={{ marginBottom: '30px' }}>
              <h2 style={{ marginBottom: '15px', color: '#f59e0b' }}>대기 중인 참가자 ({pendingParticipations.length}명)</h2>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                {pendingParticipations.map((participation) => (
                  <div
                    key={participation.participationId}
                    style={{
                      padding: '20px',
                      border: '2px solid #f59e0b',
                      borderRadius: '8px',
                      backgroundColor: '#fffbeb',
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center'
                    }}
                  >
                    <div>
                      <div style={{ fontSize: '18px', fontWeight: 'bold', marginBottom: '8px' }}>
                        {participation.participantNickname}
                      </div>
                      <div style={{ fontSize: '14px', color: '#6b7280' }}>
                        신청일: {new Date(participation.createdAt).toLocaleDateString('ko-KR', {
                          year: 'numeric',
                          month: 'long',
                          day: 'numeric',
                          hour: '2-digit',
                          minute: '2-digit'
                        })}
                      </div>
                    </div>
                    <div style={{ display: 'flex', gap: '10px' }}>
                      <Button
                        onClick={() => handleApprove(participation.participationId)}
                        variant="primary"
                        disabled={processingId !== null}
                        style={{ 
                          backgroundColor: '#10b981',
                          minWidth: '80px'
                        }}
                      >
                        {processingId === participation.participationId ? '처리 중...' : '✅ 승인'}
                      </Button>
                      <Button
                        onClick={() => handleReject(participation.participationId)}
                        variant="primary"
                        disabled={processingId !== null}
                        style={{ 
                          backgroundColor: '#ef4444',
                          minWidth: '80px'
                        }}
                      >
                        {processingId === participation.participationId ? '처리 중...' : '❌ 거절'}
                      </Button>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* 승인된 참가자 */}
          {approvedParticipations.length > 0 && (
            <div style={{ marginBottom: '30px' }}>
              <h2 style={{ marginBottom: '15px', color: '#10b981' }}>승인된 참가자 ({approvedParticipations.length}명)</h2>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                {approvedParticipations.map((participation) => (
                  <div
                    key={participation.participationId}
                    style={{
                      padding: '20px',
                      border: '2px solid #10b981',
                      borderRadius: '8px',
                      backgroundColor: '#f0fdf4',
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center'
                    }}
                  >
                    <div>
                      <div style={{ fontSize: '18px', fontWeight: 'bold', marginBottom: '8px' }}>
                        {participation.participantNickname}
                      </div>
                      <div style={{ fontSize: '14px', color: '#6b7280' }}>
                        승인일: {new Date(participation.updatedAt).toLocaleDateString('ko-KR', {
                          year: 'numeric',
                          month: 'long',
                          day: 'numeric',
                          hour: '2-digit',
                          minute: '2-digit'
                        })}
                      </div>
                    </div>
                    <div style={{ display: 'flex', gap: '20px', alignItems: 'center', minWidth: '200px' }}>
                      <span
                        style={{
                          ...getStatusBadgeStyle('APPROVED'),
                          padding: '8px 16px',
                          borderRadius: '20px',
                          fontSize: '14px',
                          fontWeight: 'bold',
                        }}
                      >
                        {getStatusText('APPROVED')}
                      </span>
                      <Button
                        onClick={() => handleCancelApproval(participation.participationId)}
                        variant="primary"
                        disabled={processingId !== null}
                        style={{ 
                          backgroundColor: '#ef4444',
                          maxWidth: '120px',
                          minWidth: '120px'
                        }}
                      >
                        {processingId === participation.participationId ? '처리 중...' : '승인 취소'}
                      </Button>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* 거절된 참가자 */}
          {rejectedParticipations.length > 0 && (
            <div style={{ marginBottom: '30px' }}>
              <h2 style={{ marginBottom: '15px', color: '#ef4444' }}>거절된 참가자 ({rejectedParticipations.length}명)</h2>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                {rejectedParticipations.map((participation) => (
                  <div
                    key={participation.participationId}
                    style={{
                      padding: '20px',
                      border: '2px solid #ef4444',
                      borderRadius: '8px',
                      backgroundColor: '#fef2f2',
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center'
                    }}
                  >
                    <div>
                      <div style={{ fontSize: '18px', fontWeight: 'bold', marginBottom: '8px' }}>
                        {participation.participantNickname}
                      </div>
                      <div style={{ fontSize: '14px', color: '#6b7280' }}>
                        거절일: {new Date(participation.updatedAt).toLocaleDateString('ko-KR', {
                          year: 'numeric',
                          month: 'long',
                          day: 'numeric',
                          hour: '2-digit',
                          minute: '2-digit'
                        })}
                      </div>
                    </div>
                    <span
                      style={{
                        ...getStatusBadgeStyle('REJECTED'),
                        padding: '8px 16px',
                        borderRadius: '20px',
                        fontSize: '14px',
                        fontWeight: 'bold'
                      }}
                    >
                      {getStatusText('REJECTED')}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          )}

          {participations.length === 0 && !loading && (
            <div style={{ textAlign: 'center', padding: '40px', color: '#6b7280' }}>
              <p style={{ fontSize: '18px' }}>아직 참가 신청이 없습니다.</p>
            </div>
          )}

          {/* 페이지네이션 */}
          {pageInfo && pageInfo.totalPages > 1 && (
            <div style={{ display: 'flex', justifyContent: 'center', gap: '10px', marginTop: '30px', alignItems: 'center' }}>
              <Button
                onClick={() => setPage(page - 1)}
                disabled={page === 0}
                variant="secondary"
                style={{ padding: '8px 16px' }}
              >
                이전
              </Button>
              <span style={{ fontSize: '14px', color: '#666' }}>
                {page + 1} / {pageInfo.totalPages} (전체 {pageInfo.totalElements}명)
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

          <div className="posting-detail-actions">
            <Button onClick={() => navigate(`/postings/${id}`)} variant="secondary">
              게시글로 돌아가기
            </Button>
          </div>
        </div>
      </div>
    </>
  )
}

export default ParticipationManagePage

