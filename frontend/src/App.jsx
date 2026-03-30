import { useState, useEffect } from 'react';
import './App.css';
import axios from 'axios';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { ThemeProvider } from './contexts/ThemeContext';
import Navbar from './components/Navbar';
import Home from './pages/Home';
import ProblemsPage from './pages/ProblemsPage';
import WorkspacePage from './pages/WorkspacePage';
import LeaderboardPage from './pages/LeaderboardPage';
import ProfilePage from './pages/ProfilePage';
import DailyChallengePage from './pages/DailyChallengePage';
import PlaygroundPage from './pages/PlaygroundPage';
import AdminLoginPage from './pages/AdminLoginPage';
import AdminDashboard from './pages/AdminDashboard';

const getStored = (key) => {
  try { const s = localStorage.getItem(key); return s ? JSON.parse(s) : null; }
  catch { return null; }
};

function App() {
  const [currentUser, setCurrentUser] = useState(() => getStored('code_arena_user'));
  const [adminUser,   setAdminUser]   = useState(() => getStored('code_arena_admin'));

  // Validate stored regular user against DB on mount
  useEffect(() => {
    const stored = getStored('code_arena_user');
    if (!stored?.id) return;
    axios.get(`/api/users/${stored.id}`).catch(() => {
      setCurrentUser(null);
      localStorage.removeItem('code_arena_user');
    });
  }, []);

  const handleLogin = (user) => {
    setCurrentUser(user);
    localStorage.setItem('code_arena_user', JSON.stringify(user));
  };
  const handleLogout = () => {
    setCurrentUser(null);
    localStorage.removeItem('code_arena_user');
  };

  const handleAdminLogin = (admin) => {
    setAdminUser(admin);
    localStorage.setItem('code_arena_admin', JSON.stringify(admin));
  };
  const handleAdminLogout = () => {
    setAdminUser(null);
    localStorage.removeItem('code_arena_admin');
  };

  return (
    <ThemeProvider>
      <Router>
        <Routes>
          {/* ── Admin routes — completely separate layout, no Navbar ── */}
          <Route path="/admin" element={<AdminLoginPage onAdminLogin={handleAdminLogin} />} />
          <Route path="/admin/dashboard" element={<AdminDashboard adminUser={adminUser} onLogout={handleAdminLogout} />} />

          {/* ── Regular app routes ── */}
          <Route path="/*" element={
            <div className="app-container">
              <Navbar currentUser={currentUser} onAuthSuccess={handleLogin} onLogout={handleLogout} />
              <main className="main-content">
                <Routes>
                  <Route path="/"            element={<Home currentUser={currentUser} />} />
                  <Route path="/problems"    element={<ProblemsPage currentUser={currentUser} />} />
                  <Route path="/problem/:id" element={<WorkspacePage currentUser={currentUser} />} />
                  <Route path="/leaderboard" element={<LeaderboardPage currentUser={currentUser} />} />
                  <Route path="/profile"     element={<ProfilePage currentUser={currentUser} />} />
                  <Route path="/daily"       element={<DailyChallengePage currentUser={currentUser} />} />
                  <Route path="/playground"  element={<PlaygroundPage />} />
                </Routes>
              </main>
            </div>
          } />
        </Routes>
      </Router>
    </ThemeProvider>
  );
}

export default App;
