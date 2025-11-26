import { Link } from 'react-router-dom'
import NavBar from '../components/NavBar'
import Button from '../components/Button'
import '../styles/common.css'

function HomePage() {
  return (
    <>
      <NavBar />
      <div className="page-container">
        <h1>다가치 - Dagachi</h1>
      <p style={{ fontSize: '18px', marginBottom: '30px' }}>
        팀 프로젝트와 스터디를 찾아보세요.
      </p>
      
      <div style={{ marginTop: '40px' }}>
        <Link to="/postings">
          <Button style={{ padding: '15px 30px', fontSize: '16px' }}>
            프로젝트 & 스터디 찾기
          </Button>
        </Link>
      </div>

      <div style={{ marginTop: '60px', textAlign: 'left', maxWidth: '800px', margin: '60px auto' }}>
        <h2>주요 기능</h2>
        <ul style={{ fontSize: '16px', lineHeight: '2' }}>
          <li>다양한 프로젝트 모집 공고 조회</li>
          <li>스터디 그룹 찾기</li>
          <li>개발자들과 함께 성장하기</li>
        </ul>
      </div>
      </div>
    </>
  )
}

export default HomePage

