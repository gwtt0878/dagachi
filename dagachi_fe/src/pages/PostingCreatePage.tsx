import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import Button from '../components/Button'
import Input from '../components/Input'
import Modal from '../components/Modal'
import NavBar from '../components/NavBar'
import { createPosting, type CreatePostingRequest } from '../api/posting'
import { useToast } from '../hooks/useToast'
import '../styles/common.css'
import { AxiosError } from 'axios'
import NaverMap from '../components/NaverMap'

function PostingCreatePage() {
  const navigate = useNavigate()
  const { showToast, ToastContainer } = useToast()
  const [showLoginModal, setShowLoginModal] = useState(false)
  const [formData, setFormData] = useState<CreatePostingRequest>({
    title: '',
    description: '',
    maxCapacity: 4,
    type: 'PROJECT',
    latitude: 0,
    longitude: 0
  })
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    // 로그인 체크
    const token = localStorage.getItem('token')
    if (!token) {
      setShowLoginModal(true)
    }
  }, [])

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

    setLoading(true)

    try {
      const newPosting = await createPosting(formData)
      showToast('게시글이 작성되었습니다! 🎉', 'success')
      setTimeout(() => {
        navigate(`/postings/${newPosting.id}`)
      }, 1000)
    } catch (err: unknown) {
      if (err instanceof AxiosError) {
        if (err.response?.status === 403) {
          showToast('게시글 작성 권한이 없습니다.', 'error')
          return
        }
        setError(err.response?.data?.message || '게시글 작성에 실패했습니다.')
      } else {
        setError('게시글 작성에 실패했습니다.')
      }
    } finally {
      setLoading(false)
    }
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
            게시글을 작성하려면 로그인이 필요합니다.
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
          <button onClick={() => navigate('/postings')} className="btn btn-primary">
            ← 목록으로 돌아가기
          </button>
        </div>

        <div className="posting-create-container">
          <h1>게시글 작성</h1>
          
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
              <NaverMap withInteraction={true} setPickedLocation={handleLocationChange} />
              <span className="help-text">
                오프라인으로 만난다면 장소를 정해주세요.
              </span>
            </div>

            {error && <p className="error-message">{error}</p>}

            <div className="form-actions">
              <Button
                type="button"
                variant="secondary"
                onClick={() => navigate('/postings')}
                disabled={loading}
              >
                취소
              </Button>
              <Button
                type="submit"
                variant="primary"
                disabled={loading}
              >
                {loading ? '작성 중...' : '작성 완료'}
              </Button>
            </div>
          </form>
        </div>
      </div>
    </>
  )
}

export default PostingCreatePage

