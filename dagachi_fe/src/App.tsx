import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import HomePage from './pages/HomePage'
import PostingsPage from './pages/PostingsPage'
import './App.css'

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/postings" element={<PostingsPage />} />
      </Routes>
    </Router>
  )
}

export default App
