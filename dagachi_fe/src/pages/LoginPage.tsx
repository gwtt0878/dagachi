import { useState, useEffect } from 'react'
import { useNavigate, Link, useSearchParams } from 'react-router-dom'
import { login } from '../api/auth'
import { AxiosError } from 'axios'
import FormContainer from '../components/FormContainer'
import Input from '../components/Input'
import Button from '../components/Button'
import { useToast } from '../hooks/useToast'
import '../styles/common.css'

function LoginPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const { showToast, ToastContainer } = useToast()
  const [formData, setFormData] = useState({
    username: '',
    password: '',
  })
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  // URL에서 expired 파라미터 체크
  useEffect(() => {
    if (searchParams.get('expired') === 'true') {
      showToast('세션이 만료되었습니다. 다시 로그인해주세요.', 'error')
    }
  }, [searchParams, showToast])

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
      showToast('로그인 성공! 환영합니다 🎉', 'success')
      setTimeout(() => {
        navigate('/postings')
      }, 1000)
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
    <>
      <ToastContainer />
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
    </>
  )
}

export default LoginPage

