import { useState } from 'react'
import './App.css'

interface Project {
  id: number
  title: string
  description: string
  type: string
}

function App() {
  const [projects, setProjects] = useState<Project[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const fetchProjects = async () => {
    setLoading(true)
    setError(null)
    
    try {
      const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || ''
      const response = await fetch(`${apiBaseUrl}/api/projects`)
      
      if (!response.ok) {
        throw new Error('서버 응답 오류')
      }
      
      const data = await response.json()
      setProjects(data)
      console.log('서버에서 받은 데이터:', data)
    } catch (err) {
      setError('프로젝트를 불러오는데 실패했습니다.')
      console.error('API 호출 오류:', err)
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      <div>
      </div>
      <div>
        <h1>다가치 - Dagachi</h1>
        <p>팀 프로젝트와 스터디를 찾아보세요.</p>

        <button onClick={fetchProjects} disabled={loading}>
          {loading ? '불러오는 중...' : '프로젝트 & 스터디 찾기'}
        </button>

        {error && <p style={{ color: 'red' }}>{error}</p>}

        {projects.length > 0 && (
          <div style={{ marginTop: '20px', textAlign: 'left' }}>
            <h2>프로젝트 & 스터디 목록</h2>
            {projects.map((project) => (
              <div key={project.id} style={{ 
                border: '1px solid #ccc', 
                padding: '10px', 
                margin: '10px 0',
                borderRadius: '5px'
              }}>
                <h3>{project.title} <span style={{ fontSize: '12px', color: '#666' }}>({project.type})</span></h3>
                <p>{project.description}</p>
              </div>
            ))}
          </div>
        )}
      </div>
    </>
  )
}

export default App
