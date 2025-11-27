import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import HomePage from './pages/HomePage'
import PostingsPage from './pages/PostingsPage'
import PostingDetailPage from './pages/PostingDetailPage'
import PostingCreatePage from './pages/PostingCreatePage'
import PostingEditPage from './pages/PostingEditPage'
import ParticipationManagePage from './pages/ParticipationManagePage'
import LoginPage from './pages/LoginPage'
import SignupPage from './pages/SignupPage'
import UserPage from './pages/UserPage'
import MyInfoPage from './pages/MyInfoPage'
import './App.css'

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/postings" element={<PostingsPage />} />
        <Route path="/postings/create" element={<PostingCreatePage />} />
        <Route path="/postings/:id/edit" element={<PostingEditPage />} />
        <Route path="/postings/:id/participants" element={<ParticipationManagePage />} />
        <Route path="/postings/:id" element={<PostingDetailPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />
        <Route path="/users/me" element={<MyInfoPage />} />
        <Route path="/users/:id" element={<UserPage />} />
      </Routes>
    </Router>
  )
}

export default App
