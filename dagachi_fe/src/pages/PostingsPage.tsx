import { useState, useEffect, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import Button from '../components/Button'
import Modal from '../components/Modal'
import NavBar from '../components/NavBar'
import '../styles/common.css'
import type { PostingSimple } from '../types'
import { getAllPostings, searchPostings, type SearchPostingParams } from '../api/posting'
import { AxiosError } from 'axios'
import { getTypeLabel, getStatusLabel } from '../constants'
import { getCurrentLocation, calculateDistance, formatDistance, type UserLocation, type LocationError } from '../utils/location'

function PostingsPage() {
  const navigate = useNavigate()
  const [postings, setPostings] = useState<PostingSimple[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [showLoginModal, setShowLoginModal] = useState(false)
  const [currentPage, setCurrentPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  
  // 검색 필터
  const [searchMode, setSearchMode] = useState(false)
  const [searchTitle, setSearchTitle] = useState('')
  const [searchType, setSearchType] = useState<'' | 'PROJECT' | 'STUDY'>('')
  const [searchStatus, setSearchStatus] = useState<'' | 'RECRUITING' | 'RECRUITED' | 'COMPLETED'>('')
  const [searchAuthorNickname, setSearchAuthorNickname] = useState('')
  
  // 정렬 방식
  const [sortType, setSortType] = useState<'date' | 'distance'>('date')
  const [userLocation, setUserLocation] = useState<UserLocation | null>(null)
  const [locationLoading, setLocationLoading] = useState(false)
  const [locationError, setLocationError] = useState<string | null>(null)

  const fetchPostings = useCallback(async (page: number = 0) => {
    setLoading(true)
    setError(null)
    
    try {
      const data = await getAllPostings(page)
      setPostings(data.content)
      setTotalPages(data.totalPages)
      setCurrentPage(data.number)
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
  
  const handleSearch = async () => {
    // 검색 조건이 하나라도 있으면 검색 모드
    if (!searchTitle && !searchType && !searchStatus && !searchAuthorNickname && sortType === 'date') {
      setError('최소 하나 이상의 검색 조건을 입력해주세요.')
      return
    }
    
    // 거리순 정렬이 선택되었지만 위치 정보가 없는 경우
    if (sortType === 'distance' && !userLocation) {
      setError('거리순 정렬을 위해서는 위치 정보가 필요합니다.')
      return
    }
    
    setSearchMode(true)
    setCurrentPage(0)
    setLoading(true)
    setError(null)
    
    try {
      const params: SearchPostingParams = {
        title: searchTitle || undefined,
        type: searchType || undefined,
        status: searchStatus || undefined,
        authorNickname: searchAuthorNickname || undefined,
        page: 0,
        sortByDistance: sortType === 'distance' || undefined,
        userLatitude: sortType === 'distance' && userLocation ? userLocation.latitude : undefined,
        userLongitude: sortType === 'distance' && userLocation ? userLocation.longitude : undefined
      }
      
      const data = await searchPostings(params)
      setPostings(data.content)
      setTotalPages(data.totalPages)
      setCurrentPage(data.number)
    } catch (err) {
      setError('검색에 실패했습니다.')
      console.error('검색 오류:', err)
    } finally {
      setLoading(false)
    }
  }
  
  const handleSearchWithPage = async (page: number) => {
    setLoading(true)
    setError(null)
    
    try {
      const params: SearchPostingParams = {
        title: searchTitle || undefined,
        type: searchType || undefined,
        status: searchStatus || undefined,
        authorNickname: searchAuthorNickname || undefined,
        page,
        sortByDistance: sortType === 'distance' || undefined,
        userLatitude: sortType === 'distance' && userLocation ? userLocation.latitude : undefined,
        userLongitude: sortType === 'distance' && userLocation ? userLocation.longitude : undefined
      }
      
      const data = await searchPostings(params)
      setPostings(data.content)
      setTotalPages(data.totalPages)
      setCurrentPage(data.number)
    } catch (err) {
      setError('검색에 실패했습니다.')
      console.error('검색 오류:', err)
    } finally {
      setLoading(false)
    }
  }
  
  const handleReset = () => {
    setSearchMode(false)
    setSearchTitle('')
    setSearchType('')
    setSearchStatus('')
    setSearchAuthorNickname('')
    setSortType('date')
    setCurrentPage(0)
    fetchPostings(0)
  }
  
  // 사용자 위치 정보 가져오기
  const handleGetLocation = async () => {
    setLocationLoading(true)
    setLocationError(null)
    
    try {
      const location = await getCurrentLocation()
      setUserLocation(location)
    } catch (err) {
      const error = err as LocationError
      setLocationError(error.message)
    } finally {
      setLocationLoading(false)
    }
  }

  // 정렬 방식 변경 처리
  const handleSortTypeChange = (newSortType: 'date' | 'distance') => {
    setSortType(newSortType)
  }

  useEffect(() => {
    // 로그인 체크
    const token = localStorage.getItem('token')
    if (!token) {
      setShowLoginModal(true)
      setLoading(false)
      return
    }
    
    // 페이지 로드 시 자동으로 위치 정보 가져오기 (백그라운드에서)
    handleGetLocation()
    
    fetchPostings(currentPage)
  }, [fetchPostings, currentPage])
  
  const handlePageChange = (newPage: number) => {
    if (searchMode) {
      handleSearchWithPage(newPage)
    } else {
      setCurrentPage(newPage)
    }
  }

  return (
    <>
      <NavBar />
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

      <div style={{ marginBottom: '20px', textAlign: 'center' }}>
        <Button onClick={() => navigate('/postings/create')} variant="primary">
          ✏️ 게시글 작성
        </Button>
      </div>

      {/* 검색 필터 */}
      <div style={{
        maxWidth: '1000px',
        margin: '0 auto 30px',
        padding: '20px',
        backgroundColor: '#f5f5f5',
        borderRadius: '8px',
        border: '1px solid #ddd'
      }}>
        <h3 style={{ marginTop: 0, marginBottom: '15px', color: '#333' }}>🔍 게시글 검색</h3>
        
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '15px', marginBottom: '15px' }}>
          {/* 제목 검색 */}
          <div>
            <label style={{ display: 'block', marginBottom: '5px', fontSize: '14px', color: '#666' }}>제목</label>
            <input
              type="text"
              value={searchTitle}
              onChange={(e) => setSearchTitle(e.target.value)}
              placeholder="제목 검색..."
              style={{
                width: '100%',
                padding: '8px',
                borderRadius: '4px',
                border: '1px solid #ccc',
                fontSize: '14px',
                boxSizing: 'border-box'
              }}
            />
          </div>
          
          {/* 타입 선택 */}
          <div>
            <label style={{ display: 'block', marginBottom: '5px', fontSize: '14px', color: '#666' }}>타입</label>
            <select
              value={searchType}
              onChange={(e) => setSearchType(e.target.value as '' | 'PROJECT' | 'STUDY')}
              style={{
                width: '100%',
                padding: '8px',
                borderRadius: '4px',
                border: '1px solid #ccc',
                fontSize: '14px',
                boxSizing: 'border-box'
              }}
            >
              <option value="">전체</option>
              <option value="PROJECT">프로젝트</option>
              <option value="STUDY">스터디</option>
            </select>
          </div>
          
          {/* 상태 선택 */}
          <div>
            <label style={{ display: 'block', marginBottom: '5px', fontSize: '14px', color: '#666' }}>상태</label>
            <select
              value={searchStatus}
              onChange={(e) => setSearchStatus(e.target.value as '' | 'RECRUITING' | 'RECRUITED' | 'COMPLETED')}
              style={{
                width: '100%',
                padding: '8px',
                borderRadius: '4px',
                border: '1px solid #ccc',
                fontSize: '14px',
                boxSizing: 'border-box'
              }}
            >
              <option value="">전체</option>
              <option value="RECRUITING">모집중</option>
              <option value="RECRUITED">모집완료</option>
              <option value="COMPLETED">종료</option>
            </select>
          </div>
          
          {/* 작성자 검색 */}
          <div>
            <label style={{ display: 'block', marginBottom: '5px', fontSize: '14px', color: '#666' }}>작성자</label>
            <input
              type="text"
              value={searchAuthorNickname}
              onChange={(e) => setSearchAuthorNickname(e.target.value)}
              placeholder="작성자 닉네임..."
              style={{
                width: '100%',
                padding: '8px',
                borderRadius: '4px',
                border: '1px solid #ccc',
                fontSize: '14px',
                boxSizing: 'border-box'
              }}
            />
          </div>
          
          {/* 정렬 방식 선택 */}
          <div>
            <label style={{ display: 'block', marginBottom: '5px', fontSize: '14px', color: '#666' }}>정렬</label>
            <select
              value={sortType}
              onChange={(e) => handleSortTypeChange(e.target.value as 'date' | 'distance')}
              style={{
                width: '100%',
                padding: '8px',
                borderRadius: '4px',
                border: '1px solid #ccc',
                fontSize: '14px',
                boxSizing: 'border-box'
              }}
            >
              <option value="date">게시일자순</option>
              <option value="distance">거리순</option>
            </select>
          </div>
        </div>
        
        {/* 위치 관련 상태 메시지 */}
        <div style={{ marginTop: '15px' }}>
          {/* 위치 정보 로딩 중 */}
          {locationLoading && (
            <div style={{ 
              color: '#856404', 
              fontSize: '12px', 
              padding: '8px',
              backgroundColor: '#fff3cd',
              borderRadius: '4px',
              border: '1px solid #ffeaa7',
              marginBottom: '8px'
            }}>
              📍 위치 정보 확인 중...
            </div>
          )}
          
          {/* 위치 오류 메시지 */}
          {locationError && !locationLoading && (
            <div style={{ 
              color: '#dc3545', 
              fontSize: '12px', 
              padding: '8px',
              backgroundColor: '#f8d7da',
              borderRadius: '4px',
              border: '1px solid #f5c6cb',
              marginBottom: '8px'
            }}>
              ⚠️ {locationError} (거리순 정렬 시 위치 정보가 필요합니다)
            </div>
          )}
          
          {/* 위치 정보 확인 완료 */}
          {userLocation && !locationLoading && !locationError && (
            <div style={{ 
              color: '#155724', 
              fontSize: '12px', 
              padding: '8px',
              marginBottom: '8px',
              backgroundColor: '#d4edda',
              borderRadius: '4px',
              border: '1px solid #c3e6cb'
            }}>
              ✅ 위치 정보가 확인되었습니다. 거리순 정렬을 사용할 수 있습니다.
            </div>
          )}
        </div>
        
        {/* 검색 버튼 */}
        <div style={{ display: 'flex', gap: '10px', justifyContent: 'center' }}>
          <Button onClick={handleSearch} variant="primary">
            검색
          </Button>
          <Button onClick={handleReset} variant="secondary">
            초기화
          </Button>
        </div>
        
        {searchMode && (
          <div style={{ marginTop: '10px', textAlign: 'center', color: '#666', fontSize: '14px' }}>
            🔎 검색 결과입니다
            {sortType === 'distance' && userLocation && (
              <div style={{ marginTop: '5px', fontSize: '12px', color: '#28a745' }}>
                📍 거리순으로 정렬됨
              </div>
            )}
          </div>
        )}
      </div>

      {loading && <p>불러오는 중...</p>}
      
      {error && <p className="error-message">{error}</p>}

      {!loading && !error && postings.length === 0 && (
        <p>등록된 게시글이 없습니다.</p>
      )}

      {!loading && postings.length > 0 && (
        <div className="postings-list">
          {postings.map((posting) => (
            <div 
              key={posting.id} 
              className="card"
              onClick={() => navigate(`/postings/${posting.id}`)}
            >
              <div className="posting-header">
                <h2 className="posting-title">{posting.title}</h2>
                <span className={`badge ${posting.type === 'PROJECT' ? 'badge-project' : 'badge-study'}`}>
                  {getTypeLabel(posting.type)}
                </span>
              </div>
              <div className="posting-status">
                <span className={`badge ${posting.status === 'RECRUITING' ? 'badge-recruiting' : 'badge-closed'}`}>
                  {getStatusLabel(posting.status)}
                </span>
              </div>
              <div className="meta-item">
                <span className="posting-author">모집자: {posting.authorNickname}</span>
                {/* 거리 정보 표시 */}
                {userLocation && posting.latitude && posting.longitude && (
                  <span style={{ 
                    marginLeft: '10px', 
                    color: '#666', 
                    fontSize: '14px',
                    backgroundColor: '#e8f5e8',
                    padding: '2px 8px',
                    borderRadius: '12px',
                    border: '1px solid #c3e6c3'
                  }}>
                    📍 {formatDistance(calculateDistance(
                      userLocation.latitude,
                      userLocation.longitude,
                      posting.latitude,
                      posting.longitude
                    ))}
                  </span>
                )}
              </div>
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

      {/* 페이지네이션 */}
      {!loading && totalPages > 0 && (
        <div style={{ 
          display: 'flex', 
          justifyContent: 'center', 
          alignItems: 'center', 
          gap: '10px', 
          marginTop: '30px',
          marginBottom: '20px'
        }}>
          <Button
            onClick={() => handlePageChange(0)}
            disabled={currentPage === 0}
            variant="secondary"
          >
            처음
          </Button>
          <Button
            onClick={() => handlePageChange(currentPage - 1)}
            disabled={currentPage === 0}
            variant="secondary"
          >
            이전
          </Button>
          <span style={{ 
            padding: '0 15px', 
            fontSize: '16px',
            fontWeight: 'bold'
          }}>
            {currentPage + 1} / {totalPages}
          </span>
          <Button
            onClick={() => handlePageChange(currentPage + 1)}
            disabled={currentPage >= totalPages - 1}
            variant="secondary"
          >
            다음
          </Button>
          <Button
            onClick={() => handlePageChange(totalPages - 1)}
            disabled={currentPage >= totalPages - 1}
            variant="secondary"
          >
            마지막
          </Button>
        </div>
      )}

      <div className="refresh-button">
        <Button 
          onClick={() => searchMode ? handleSearchWithPage(currentPage) : fetchPostings(currentPage)} 
          variant="primary"
        >
          새로고침
        </Button>
      </div>
    </div>
    </>
  )
}

export default PostingsPage

