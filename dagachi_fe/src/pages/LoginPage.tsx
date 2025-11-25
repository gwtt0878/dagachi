import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { login } from '../api/auth'
import { AxiosError } from 'axios'
import FormContainer from '../components/FormContainer'
import Input from '../components/Input'
import Button from '../components/Button'
import '../styles/common.css'

function LoginPage() {
  const navigate = useNavigate()
  const [formData, setFormData] = useState({
    username: '',
    password: '',
  })
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    })
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)
    setLoading(true)

    try {
      const token = await login(formData)
      localStorage.setItem('token', token)
      alert('로그인 성공!')
      navigate('/postings')
    } catch (err: unknown) {
      if (err instanceof AxiosError) {
        setError(err.response?.data?.message || '로그인에 실패했습니다.')
      } else {
        setError('로그인에 실패했습니다.')
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <FormContainer title="로그인">
      <form onSubmit={handleSubmit}>
        <Input
          type="text"
          name="username"
          label="아이디"
          value={formData.username}
          onChange={handleChange}
          required
        />

        <Input
          type="password"
          name="password"
          label="비밀번호"
          value={formData.password}
          onChange={handleChange}
          required
        />

        {error && <p className="error-message">{error}</p>}

        <Button type="submit" variant="primary" disabled={loading}>
          {loading ? '로그인 중...' : '로그인'}
        </Button>
      </form>

      <div className="link-group">
        <Link to="/signup">회원가입</Link>
        {' | '}
        <Link to="/">홈으로</Link>
      </div>
    </FormContainer>
  )
}

export default LoginPage

