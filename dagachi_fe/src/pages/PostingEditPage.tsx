import { useState, useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import Button from '../components/Button'
import Input from '../components/Input'
import Modal from '../components/Modal'
import NavBar from '../components/NavBar'
import { getPostingById, updatePosting, type UpdatePostingRequest } from '../api/posting'
import { useToast } from '../hooks/useToast'
import { getCurrentNickname } from '../api/auth'
import type { Posting } from '../types'
import '../styles/common.css'
import { AxiosError } from 'axios'
import NaverMap from '../components/NaverMap'

function PostingEditPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { showToast, ToastContainer } = useToast()
  const [showLoginModal, setShowLoginModal] = useState(false)
  const [posting, setPosting] = useState<Posting | null>(null)
  const [formData, setFormData] = useState<UpdatePostingRequest>({
    title: '',
    description: '',
    maxCapacity: 4,
    type: 'PROJECT',
    status: 'RECRUITING',
    latitude: 0,
    longitude: 0
  })
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    const fetchPosting = async () => {
      // 로그인 체크
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
        
        // 작성자 확인
        const nickname = getCurrentNickname()
        if (!nickname || nickname !== data.authorNickname) {
          setError('수정 권한이 없습니다.')
          setLoading(false)
          return
        }

        // 폼 데이터 초기화
        setFormData({
          title: data.title,
          description: data.description,
          maxCapacity: data.maxCapacity,
          type: data.type as 'PROJECT' | 'STUDY',
          status: data.status as 'RECRUITING' | 'RECRUITED' | 'COMPLETED',
          latitude: data.latitude,
          longitude: data.longitude
        })
      } catch (err: unknown) {
        if (err instanceof AxiosError) {
          if (err.response?.status === 403) {
            navigate('/login')
            return
          }
          if (err.response?.status === 404) {
            setError('존재하지 않는 게시글입니다.')
          } else {
            setError('게시글을 불러오는데 실패했습니다.')
          }
        }
      } finally {
        setLoading(false)
      }
    }

    fetchPosting()
  }, [id, navigate])

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target
    setFormData(prev => ({
      ...prev,
      [name]: name === 'maxCapacity' ? Number(value) : value
    }))
  }

  const handleTextareaChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    setFormData(prev => ({
      ...prev,
      description: e.target.value
    }))
  }

  const handleTypeChange = (type: 'PROJECT' | 'STUDY') => {
    setFormData(prev => ({
      ...prev,
      type
    }))
  }

  const handleStatusChange = (status: 'RECRUITING' | 'RECRUITED' | 'COMPLETED') => {
    setFormData(prev => ({
      ...prev,
      status
    }))
  }

  const handleLocationChange = (lat: number, lng: number) => {
    setFormData(prev => ({
      ...prev,
      latitude: lat,
      longitude: lng
    }))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)

    if (!id) return

    // 유효성 검사
    if (formData.title.length > 100) {
      setError('제목은 100자 이하여야 합니다.')
      return
    }

    if (!formData.description.trim()) {
      setError('내용을 입력해주세요.')
      return
    }

    if (formData.maxCapacity < 1) {
      setError('모집 인원은 1명 이상이어야 합니다.')
      return
    }

    setSubmitting(true)

    try {
      await updatePosting(Number(id), formData)
      showToast('게시글이 수정되었습니다! 🎉', 'success')
      setTimeout(() => {
        navigate(`/postings/${id}`)
      }, 1000)
    } catch (err: unknown) {
      if (err instanceof AxiosError) {
        if (err.response?.status === 403) {
          showToast('수정 권한이 없습니다.', 'error')
          return
        }
        setError(err.response?.data?.message || '게시글 수정에 실패했습니다.')
      } else {
        setError('게시글 수정에 실패했습니다.')
      }
    } finally {
      setSubmitting(false)
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
          <Button onClick={() => navigate(`/postings/${id}`)} variant="primary">
            돌아가기
          </Button>
        </div>
      </>
    )
  }

  if (!posting) {
    return null
  }

  return (
    <>
      <NavBar />
      <ToastContainer />
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
            게시글을 수정하려면 로그인이 필요합니다.
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
          <button onClick={() => navigate(`/postings/${id}`)} className="btn btn-primary">
            ← 돌아가기
          </button>
        </div>

        <div className="posting-create-container">
          <h1>게시글 수정</h1>
          
          <form onSubmit={handleSubmit} className="posting-create-form">
            {/* 타입 선택 */}
            <div className="form-group">
              <label className="form-label">타입 *</label>
              <div className="type-selector">
                <button
                  type="button"
                  className={`type-button ${formData.type === 'PROJECT' ? 'active project' : ''}`}
                  onClick={() => handleTypeChange('PROJECT')}
                >
                  프로젝트
                </button>
                <button
                  type="button"
                  className={`type-button ${formData.type === 'STUDY' ? 'active study' : ''}`}
                  onClick={() => handleTypeChange('STUDY')}
                >
                  스터디
                </button>
              </div>
            </div>

            {/* 상태 선택 */}
            <div className="form-group">
              <label className="form-label">상태 *</label>
              <div className="type-selector">
                <button
                  type="button"
                  className={`type-button ${formData.status === 'RECRUITING' ? 'active project' : ''}`}
                  onClick={() => handleStatusChange('RECRUITING')}
                >
                  모집중
                </button>
                <button
                  type="button"
                  className={`type-button ${formData.status === 'RECRUITED' ? 'active' : ''}`}
                  onClick={() => handleStatusChange('RECRUITED')}
                  style={formData.status === 'RECRUITED' ? { backgroundColor: '#FF9800', borderColor: '#FF9800' } : {}}
                >
                  모집완료
                </button>
                <button
                  type="button"
                  className={`type-button ${formData.status === 'COMPLETED' ? 'active' : ''}`}
                  onClick={() => handleStatusChange('COMPLETED')}
                  style={formData.status === 'COMPLETED' ? { backgroundColor: '#888', borderColor: '#888' } : {}}
                >
                  종료
                </button>
              </div>
            </div>

            {/* 제목 */}
            <div className="form-group">
              <Input
                type="text"
                name="title"
                label="제목"
                value={formData.title}
                onChange={handleInputChange}
                required
                maxLength={100}
                placeholder="게시글 제목을 입력하세요"
                helpText={`${formData.title.length}/100`}
              />
            </div>

            {/* 모집 인원 */}
            <div className="form-group">
              <Input
                type="number"
                name="maxCapacity"
                label="모집 인원"
                value={formData.maxCapacity.toString()}
                onChange={handleInputChange}
                required
                minLength={1}
                maxLength={20}
                placeholder="모집 인원을 입력하세요"
                helpText="최소 1명 이상 20명 이하"
              />
            </div>

            {/* 상세 설명 */}
            <div className="form-group">
              <label className="form-label">상세 설명 *</label>
              <textarea
                name="description"
                className="form-textarea"
                value={formData.description}
                onChange={handleTextareaChange}
                required
                rows={10}
                placeholder="프로젝트/스터디에 대한 상세한 설명을 입력하세요"
              />
              <span className="help-text">
                모집 목적, 진행 방식, 기간, 필요한 기술 스택 등을 자세히 작성해주세요.
              </span>
            </div>

            <div className="form-group">
              <NaverMap withInteraction={true} setPickedLocation={handleLocationChange} latitude={formData.latitude} longitude={formData.longitude} />
            </div>

            {error && <p className="error-message">{error}</p>}

            <div className="form-actions">
              <Button
                type="button"
                variant="secondary"
                onClick={() => navigate(`/postings/${id}`)}
                disabled={submitting}
              >
                취소
              </Button>
              <Button
                type="submit"
                variant="primary"
                disabled={submitting}
              >
                {submitting ? '수정 중...' : '수정 완료'}
              </Button>
            </div>
          </form>
        </div>
      </div>
    </>
  )
}

export default PostingEditPage

