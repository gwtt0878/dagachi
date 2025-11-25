import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { signup } from '../api/auth'
import { AxiosError } from 'axios'
import FormContainer from '../components/FormContainer'
import Input from '../components/Input'
import Button from '../components/Button'
import { useToast } from '../hooks/useToast'
import '../styles/common.css'

function SignupPage() {
  const navigate = useNavigate()
  const { showToast, ToastContainer } = useToast()
  const [formData, setFormData] = useState({
    username: '',
    password: '',
    passwordConfirm: '',
    nickname: '',
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

    // 비밀번호 확인
    if (formData.password !== formData.passwordConfirm) {
      setError('비밀번호가 일치하지 않습니다.')
      return
    }

    // 비밀번호 길이 검증
    if (formData.password.length < 8) {
      setError('비밀번호는 8자 이상이어야 합니다.')
      return
    }

    setLoading(true)

    try {
      await signup({
        username: formData.username,
        password: formData.password,
        nickname: formData.nickname,
      })
      showToast('회원가입 성공! 로그인해주세요 🎉', 'success')
      setTimeout(() => {
        navigate('/login')
      }, 1000)
    } catch (err: unknown) {
      if (err instanceof AxiosError) {
        setError(err.response?.data?.message || '회원가입에 실패했습니다.')
      } else {
        setError('회원가입에 실패했습니다.')
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      <ToastContainer />
      <FormContainer title="회원가입">
        <form onSubmit={handleSubmit}>
        <Input
          type="text"
          name="username"
          label="아이디"
          value={formData.username}
          onChange={handleChange}
          required
          minLength={4}
          maxLength={20}
          helpText="4자 이상 20자 이하"
        />

        <Input
          type="text"
          name="nickname"
          label="닉네임"
          value={formData.nickname}
          onChange={handleChange}
          required
          helpText="필수 입력"
        />

        <Input
          type="password"
          name="password"
          label="비밀번호"
          value={formData.password}
          onChange={handleChange}
          required
          minLength={8}
          helpText="8자 이상"
        />

        <Input
          type="password"
          name="passwordConfirm"
          label="비밀번호 확인"
          value={formData.passwordConfirm}
          onChange={handleChange}
          required
        />

        {error && <p className="error-message">{error}</p>}

        <Button type="submit" variant="primary" disabled={loading}>
          {loading ? '가입 중...' : '회원가입'}
        </Button>
      </form>

        <div className="link-group">
          <Link to="/login">로그인</Link>
          {' | '}
          <Link to="/">홈으로</Link>
        </div>
      </FormContainer>
    </>
  )
}

export default SignupPage

