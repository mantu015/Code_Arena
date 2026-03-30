import React, { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Code2, Trophy, List, User, LogOut, Sun, Moon, Flame } from 'lucide-react';
import AuthModal from './AuthModal';
import { useTheme } from '../contexts/ThemeContext';

const Navbar = ({ currentUser, onAuthSuccess, onLogout }) => {
  const { isDark, toggleTheme } = useTheme();
  const location = useLocation();
  const [isAuthOpen, setIsAuthOpen] = useState(false);
  const [authMode, setAuthMode] = useState('login');
  const [isLogoutOpen, setIsLogoutOpen] = useState(false);

  const navLinks = [
    { name: 'Problems',    path: '/problems',   icon: <List size={17} /> },
    { name: 'Playground',  path: '/playground', icon: <Code2 size={17} /> },
    { name: 'Daily',       path: '/daily',      icon: <Flame size={17} /> },
    { name: 'Leaderboard', path: '/leaderboard',icon: <Trophy size={17} /> },
  ];

  const openAuth = (mode) => { setAuthMode(mode); setIsAuthOpen(true); };
  const confirmLogout = () => { onLogout(); setIsLogoutOpen(false); };

  const isActive = (path) => location.pathname === path;

  return (
    <nav style={{ padding: '0.875rem 2rem', borderBottom: '1px solid var(--border-color)',
      background: 'var(--bg-glass)', backdropFilter: 'blur(12px)',
      position: 'sticky', top: 0, zIndex: 100,
      display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>

      <Link to="/" style={{ display: 'flex', alignItems: 'center', gap: 8, fontWeight: 700, fontSize: '1.2rem', textDecoration: 'none' }}>
        <Code2 color="var(--brand-primary)" size={26} />
        <span>Code<span style={{ color: 'var(--brand-primary)' }}>Arena</span></span>
      </Link>

      {/* Nav Links — only shown when logged in */}
      {currentUser && (
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
          {navLinks.map(link => (
            <Link key={link.path} to={link.path}
              style={{ display: 'flex', alignItems: 'center', gap: 6, padding: '0.45rem 0.9rem',
                borderRadius: 8, fontWeight: 500, fontSize: '0.9rem', textDecoration: 'none',
                transition: 'all 0.2s',
                color: isActive(link.path) ? 'var(--brand-primary)' : 'var(--text-secondary)',
                background: isActive(link.path) ? 'rgba(99,102,241,0.1)' : 'transparent' }}
              onMouseOver={e => { if (!isActive(link.path)) e.currentTarget.style.color = 'var(--text-primary)'; }}
              onMouseOut={e => { if (!isActive(link.path)) e.currentTarget.style.color = 'var(--text-secondary)'; }}>
              {link.icon} {link.name}
            </Link>
          ))}
        </div>
      )}

      <div style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
        <button onClick={toggleTheme} className="btn btn-secondary"
          style={{ padding: '0.5rem', borderRadius: 10, display: 'flex', alignItems: 'center', justifyContent: 'center' }}
          title={isDark ? 'Switch to Light Mode' : 'Switch to Dark Mode'}>
          {isDark ? <Sun size={18} /> : <Moon size={18} />}
        </button>

        {currentUser ? (
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <Link to="/profile"
              style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '0.4rem 1rem',
                borderRadius: 8, border: '1px solid var(--border-color)',
                background: 'rgba(255,255,255,0.04)', textDecoration: 'none',
                transition: 'background 0.2s' }}
              onMouseOver={e => e.currentTarget.style.background = 'rgba(255,255,255,0.08)'}
              onMouseOut={e => e.currentTarget.style.background = 'rgba(255,255,255,0.04)'}>
              <div style={{ width: 26, height: 26, borderRadius: '50%', background: 'var(--brand-gradient)',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                fontSize: '0.75rem', fontWeight: 700, color: 'white' }}>
                {currentUser.username.charAt(0).toUpperCase()}
              </div>
              <span style={{ fontWeight: 600, fontSize: '0.9rem' }}>{currentUser.username}</span>
            </Link>
            <button className="btn btn-secondary" style={{ padding: '0.5rem' }}
              onClick={() => setIsLogoutOpen(true)} title="Sign Out">
              <LogOut size={16} />
            </button>
          </div>
        ) : (
          <>
            <button className="btn btn-secondary" style={{ fontSize: '0.9rem', padding: '0.5rem 1rem' }}
              onClick={() => openAuth('login')}>
              <User size={16} /> Sign In
            </button>
            <button className="btn btn-primary" style={{ fontSize: '0.9rem', padding: '0.5rem 1rem' }}
              onClick={() => openAuth('register')}>
              Sign Up
            </button>
          </>
        )}
      </div>

      <AuthModal isOpen={isAuthOpen} onClose={() => setIsAuthOpen(false)}
        initialMode={authMode} key={authMode} onAuthSuccess={onAuthSuccess} />

      {isLogoutOpen && (
        <div style={{ position: 'fixed', top: 0, left: 0, width: '100vw', height: '100vh',
          background: 'rgba(0,0,0,0.7)', backdropFilter: 'blur(8px)', zIndex: 1000,
          display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <div className="glass-panel animate-fade-in" style={{ width: 380, padding: '2.5rem', textAlign: 'center' }}>
            <h2 style={{ fontSize: '1.5rem', marginBottom: '1rem' }}>Confirm Logout</h2>
            <p style={{ color: 'var(--text-secondary)', marginBottom: '2rem' }}>Are you sure you want to log out?</p>
            <div style={{ display: 'flex', gap: 12, justifyContent: 'center' }}>
              <button className="btn btn-secondary" style={{ width: '45%' }} onClick={() => setIsLogoutOpen(false)}>Cancel</button>
              <button className="btn btn-primary" style={{ width: '45%', background: 'var(--error)', boxShadow: '0 4px 15px rgba(239,68,68,0.3)' }}
                onClick={confirmLogout}>Log Out</button>
            </div>
          </div>
        </div>
      )}
    </nav>
  );
};

export default Navbar;
