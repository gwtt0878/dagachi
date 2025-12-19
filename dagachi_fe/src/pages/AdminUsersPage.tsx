import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import NavBar from '../components/NavBar'
import Button from '../components/Button'
import '../styles/common.css'
import type { User, PageResponse } from '../types'
import { getCurrentUser, getAdminUsers, updateUserRole } from '../api/user'
import { AxiosError } from 'axios'

function AdminUsersPage() {
  const navigate = useNavigate()

  const [currentUser, setCurrentUser] = useState<User | null>(null)
  const [users, setUsers] = useState<User[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)

  const [editedRoles, setEditedRoles] = useState<Record<number, 'USER' | 'ADMIN'>>({})
  const [savingUserId, setSavingUserId] = useState<number | null>(null)

  useEffect(() => {
    const fetchMe = async () => {
      const token = localStorage.getItem('token')
      if (!token) {
        navigate('/login')
        return
      }

      try {
        const me = await getCurrentUser()
        console.log(me);
        
        if (me.role !== 'ADMIN') {
          setError('관리자만 접근할 수 있는 페이지입니다.')
          return
        }
        setCurrentUser(me)
      } catch (err) {
        if (err instanceof AxiosError && (err.response?.status === 401 || err.response?.status === 403)) {
          navigate('/login')
          return
        }
        setError('사용자 정보를 불러오는데 실패했습니다.')
        console.error('현재 사용자 조회 오류:', err)
      } finally {
        setLoading(false)
      }
    }

    fetchMe()
  }, [navigate])

  useEffect(() => {
    const fetchUsers = async () => {
      if (!currentUser || currentUser.role !== 'ADMIN') {
        return
      }

      setLoading(true)
      setError(null)

      try {
        const data: PageResponse<User> = await getAdminUsers(page)
        setUsers(data.content)
        setTotalPages(data.totalPages)
      } catch (err) {
        alert(err)      
        if (err instanceof AxiosError && (err.response?.status === 401 || err.response?.status === 403)) {
          navigate('/login')
          return
        }
      } finally {
        setLoading(false)
      }
    }

    fetchUsers()
  }, [currentUser, page, navigate])

  const handleRoleChange = (userId: number, role: 'USER' | 'ADMIN') => {
    setEditedRoles((prev) => ({
      ...prev,
      [userId]: role,
    }))
  }

  const handleSaveRole = async (user: User) => {
    const newRole = editedRoles[user.id] ?? user.role
    if (newRole === user.role) {
      return
    }

    try {
      setSavingUserId(user.id)
      await updateUserRole(user.id, newRole)
      const data = await getAdminUsers(page)
      setUsers(data.content)
      setTotalPages(data.totalPages)
      setEditedRoles((prev) => {
        const copy = { ...prev }
        delete copy[user.id]
        return copy
      })
    } catch (err) {
      console.error('권한 변경 실패:', err)
      alert('권한 변경에 실패했습니다.')
    } finally {
      setSavingUserId(null)
    }
  }

  const handlePageChange = (newPage: number) => {
    if (newPage < 0 || newPage >= totalPages) return
    setPage(newPage)
  }

  if (loading && !currentUser && !error) {
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
          <Button onClick={() => navigate('/')} variant="primary">
            홈으로 돌아가기
          </Button>
        </div>
      </>
    )
  }

  return (
    <>
      <NavBar />
      <div className="page-container">
        <h1>관리자 메뉴</h1>
        <p style={{ marginBottom: '20px', color: '#555' }}>
          전체 사용자 목록을 조회하고 권한을 변경할 수 있습니다.
        </p>

        {loading && <p>불러오는 중...</p>}

        {!loading && users.length === 0 && (
          <p>등록된 사용자가 없습니다.</p>
        )}

        {!loading && users.length > 0 && (
          <div style={{ maxWidth: '900px', margin: '0 auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead>
                <tr style={{ backgroundColor: '#f3f4f6' }}>
                  <th style={{ padding: '10px', borderBottom: '1px solid #e5e7eb' }}>ID</th>
                  <th style={{ padding: '10px', borderBottom: '1px solid #e5e7eb' }}>Username</th>
                  <th style={{ padding: '10px', borderBottom: '1px solid #e5e7eb' }}>Nickname</th>
                  <th style={{ padding: '10px', borderBottom: '1px solid #e5e7eb' }}>Role</th>
                  <th style={{ padding: '10px', borderBottom: '1px solid #e5e7eb' }}>액션</th>
                </tr>
              </thead>
              <tbody>
                {users.map((u) => {
                  const currentRole = (editedRoles[u.id] ?? u.role) as 'USER' | 'ADMIN'
                  const isSelf = currentUser ? currentUser.id === u.id : false

                  return (
                    <tr key={u.id}>
                      <td style={{ padding: '8px', borderBottom: '1px solid #e5e7eb' }}>{u.id}</td>
                      <td style={{ padding: '8px', borderBottom: '1px solid #e5e7eb' }}>{u.username}</td>
                      <td style={{ padding: '8px', borderBottom: '1px solid #e5e7eb' }}>{u.nickname}</td>
                      <td style={{ padding: '8px', borderBottom: '1px solid #e5e7eb' }}>
                        <select
                          id={`user-role-${u.id}`}
                          name={`userRole-${u.id}`}
                          value={currentRole}
                          disabled={isSelf}
                          onChange={(e) => handleRoleChange(u.id, e.target.value as 'USER' | 'ADMIN')}
                          style={{
                            padding: '6px 8px',
                            borderRadius: '4px',
                            border: '1px solid #ccc',
                            fontSize: '14px',
                          }}
                        >
                          <option value="USER">USER</option>
                          <option value="ADMIN">ADMIN</option>
                        </select>
                        {isSelf && (
                          <div style={{ fontSize: '12px', color: '#999', marginTop: '4px' }}>
                            자신의 권한은 변경할 수 없습니다.
                          </div>
                        )}
                      </td>
                      <td style={{ padding: '8px', borderBottom: '1px solid #e5e7eb' }}>
                        <Button
                          variant="secondary"
                          onClick={() => handleSaveRole(u)}
                          disabled={isSelf || savingUserId === u.id || (editedRoles[u.id] ?? u.role) === u.role}
                          style={{ padding: '6px 12px', fontSize: '14px' }}
                        >
                          {savingUserId === u.id ? '저장 중...' : '저장'}
                        </Button>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>

            {/* 페이지네이션 */}
            {totalPages > 0 && (
              <div
                style={{
                  display: 'flex',
                  justifyContent: 'center',
                  alignItems: 'center',
                  gap: '10px',
                  marginTop: '20px',
                }}
              >
                <Button
                  onClick={() => handlePageChange(0)}
                  disabled={page === 0}
                  variant="secondary"
                >
                  처음
                </Button>
                <Button
                  onClick={() => handlePageChange(page - 1)}
                  disabled={page === 0}
                  variant="secondary"
                >
                  이전
                </Button>
                <span style={{ padding: '0 15px', fontWeight: 'bold' }}>
                  {page + 1} / {totalPages}
                </span>
                <Button
                  onClick={() => handlePageChange(page + 1)}
                  disabled={page >= totalPages - 1}
                  variant="secondary"
                >
                  다음
                </Button>
                <Button
                  onClick={() => handlePageChange(totalPages - 1)}
                  disabled={page >= totalPages - 1}
                  variant="secondary"
                >
                  마지막
                </Button>
              </div>
            )}
          </div>
        )}
      </div>
    </>
  )
}

export default AdminUsersPage


