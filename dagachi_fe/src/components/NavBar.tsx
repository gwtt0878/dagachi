import { Link, useNavigate } from 'react-router-dom'
import { logout } from '../api/auth'
import Button from './Button'
import '../styles/navbar.css'

function NavBar() {
  const navigate = useNavigate()
  const isLoggedIn = !!localStorage.getItem('token')

  const handleLogout = () => {
    logout()
    navigate('/')
    window.location.reload()
  }

  return (
    <nav className="navbar">
      <div className="navbar-container">
        <Link to="/" className="navbar-logo">
          <h2>다가치 Dagachi</h2>
        </Link>
        
        <div className="navbar-menu">
          {isLoggedIn ? (
            <>
              <Link to={`/users/me`} className="navbar-link">
                내 정보
              </Link>
              <Button 
                variant="logout" 
                onClick={handleLogout}
                style={{ padding: '8px 16px', fontSize: '14px' }}
              >
                로그아웃
              </Button>
            </>
          ) : (
            <>
              <Link to="/login">
                <Button 
                  variant="small"
                  style={{ padding: '8px 16px', fontSize: '14px' }}
                >
                  로그인
                </Button>
              </Link>
              <Link to="/signup">
                <Button 
                  variant="secondary"
                  style={{ padding: '8px 16px', fontSize: '14px' }}
                >
                  회원가입
                </Button>
              </Link>
            </>
          )}
        </div>
      </div>
    </nav>
  )
}

export default NavBar

