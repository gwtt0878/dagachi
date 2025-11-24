import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import '../App.css'

interface Posting {
  id: number
  title: string
  description: string
  type: string
  createdAt: string
}

function PostingsPage() {
  const [postings, setPostings] = useState<Posting[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    fetchPostings()
  }, [])

  const fetchPostings = async () => {
    setLoading(true)
    setError(null)
    
    try {
      const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:3000'
      const response = await fetch(`${apiBaseUrl}/api/postings`)
      
      if (!response.ok) {
        throw new Error('서버 응답 오류')
      }
      
      const data = await response.json()
      setPostings(data)
      console.log('서버에서 받은 데이터:', data)
    } catch (err) {
      setError('게시글을 불러오는데 실패했습니다.')
      console.error('API 호출 오류:', err)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{ padding: '20px', maxWidth: '1200px', margin: '0 auto' }}>
      <div style={{ marginBottom: '20px' }}>
        <Link to="/" style={{ textDecoration: 'none', color: '#646cff', backgroundColor: '#edeace', padding: '10px 20px', borderRadius: '5px' }}>
          ← 홈으로 돌아가기
        </Link>
      </div>

      <h1>프로젝트 & 스터디 목록</h1>

      {loading && <p>불러오는 중...</p>}
      
      {error && <p style={{ color: 'red' }}>{error}</p>}

      {!loading && !error && postings.length === 0 && (
        <p>등록된 게시글이 없습니다.</p>
      )}

      {!loading && postings.length > 0 && (
        <div style={{ marginTop: '20px' }}>
          {postings.map((posting) => (
            <div key={posting.id} style={{ 
              border: '1px solid #ccc', 
              padding: '20px', 
              margin: '15px 0',
              borderRadius: '8px',
              backgroundColor: '#edeace',
              transition: 'transform 0.2s',
              cursor: 'pointer'
            }}
            onMouseEnter={(e) => e.currentTarget.style.transform = 'translateY(-2px)'}
            onMouseLeave={(e) => e.currentTarget.style.transform = 'translateY(0)'}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <h2 style={{ margin: '0 0 10px 0' }}>{posting.title}</h2>
                <span style={{ 
                  fontSize: '14px', 
                  color: posting.type === 'PROJECT' ? '#4CAF50' : '#2196F3',
                  fontWeight: 'bold',
                  padding: '4px 8px',
                  borderRadius: '4px',
                  backgroundColor: posting.type === 'PROJECT' ? 'rgba(76, 175, 80, 0.1)' : 'rgba(33, 150, 243, 0.1)'
                }}>
                  {posting.type}
                </span>
              </div>
              <p style={{ margin: '10px 0', color: '#000000' }}>{posting.description}</p>
              <p style={{ fontSize: '12px', color: '#888', margin: '10px 0 0 0' }}>
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

      <div style={{ marginTop: '30px', textAlign: 'center' }}>
        <button onClick={fetchPostings} style={{
          padding: '10px 20px',
          backgroundColor: '#646cff',
          color: 'white',
          border: 'none',
          borderRadius: '5px',
          cursor: 'pointer'
        }}>
          새로고침
        </button>
      </div>
    </div>
  )
}

export default PostingsPage

