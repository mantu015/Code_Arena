import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import API_BASE_URL from '../apiConfig';
import { Shield, Lock, User, Eye, EyeOff } from 'lucide-react';

const AdminLoginPage = ({ onAdminLogin }) => {
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPw, setShowPw] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const res = await axios.post(`${API_BASE_URL}/api/admin/login`, { username, password });
      onAdminLogin(res.data);
      navigate('/admin/dashboard');
    } catch {
      setError('Invalid admin credentials.');
    }
    setLoading(false);
  };

  return (
    <div style={{
      minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center',
      background: 'linear-gradient(135deg, #0a0a0f 0%, #0f0f1a 50%, #0a0a0f 100%)',
      position: 'relative', overflow: 'hidden'
    }}>
      {/* Background grid */}
      <div style={{
        position: 'absolute', inset: 0, opacity: 0.04,
        backgroundImage: 'linear-gradient(var(--border-color) 1px, transparent 1px), linear-gradient(90deg, var(--border-color) 1px, transparent 1px)',
        backgroundSize: '40px 40px'
      }} />

      {/* Glow */}
      <div style={{
        position: 'absolute', width: 400, height: 400, borderRadius: '50%',
        background: 'radial-gradient(circle, rgba(239,68,68,0.08) 0%, transparent 70%)',
        top: '20%', left: '50%', transform: 'translateX(-50%)', pointerEvents: 'none'
      }} />

      <div className="glass-panel animate-fade-in" style={{ width: 420, padding: '2.5rem', position: 'relative', zIndex: 1 }}>
        {/* Header */}
        <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
          <div style={{
            width: 64, height: 64, borderRadius: '50%', margin: '0 auto 1rem',
            background: 'rgba(239,68,68,0.1)', border: '2px solid rgba(239,68,68,0.3)',
            display: 'flex', alignItems: 'center', justifyContent: 'center'
          }}>
            <Shield size={28} color="#ef4444" />
          </div>
          <h1 style={{ fontSize: '1.75rem', marginBottom: 6 }}>Admin Portal</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
            Restricted access — authorized personnel only
          </p>
        </div>

        {error && (
          <div style={{
            color: 'var(--error)', background: 'rgba(239,68,68,0.1)', padding: '0.75rem',
            borderRadius: 8, marginBottom: '1.25rem', fontSize: '0.88rem',
            border: '1px solid rgba(239,68,68,0.2)', textAlign: 'center'
          }}>
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <div>
            <label style={{ display: 'block', marginBottom: 6, fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
              Admin Username
            </label>
            <div style={{ position: 'relative' }}>
              <User size={16} style={{ position: 'absolute', left: 12, top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
              <input
                type="text" className="input-field" style={{ paddingLeft: '2.5rem' }}
                placeholder="Enter admin username"
                value={username} onChange={e => setUsername(e.target.value)} required
              />
            </div>
          </div>

          <div>
            <label style={{ display: 'block', marginBottom: 6, fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
              Password
            </label>
            <div style={{ position: 'relative' }}>
              <Lock size={16} style={{ position: 'absolute', left: 12, top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
              <input
                type={showPw ? 'text' : 'password'} className="input-field"
                style={{ paddingLeft: '2.5rem', paddingRight: '2.5rem' }}
                placeholder="Enter password"
                value={password} onChange={e => setPassword(e.target.value)} required
              />
              <button type="button" onClick={() => setShowPw(p => !p)}
                style={{ position: 'absolute', right: 12, top: '50%', transform: 'translateY(-50%)',
                  background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-muted)' }}>
                {showPw ? <EyeOff size={16} /> : <Eye size={16} />}
              </button>
            </div>
          </div>

          <button type="submit" disabled={loading}
            style={{
              marginTop: 8, padding: '0.875rem', borderRadius: 8, border: 'none', cursor: 'pointer',
              background: loading ? 'rgba(239,68,68,0.5)' : 'linear-gradient(135deg, #ef4444, #dc2626)',
              color: 'white', fontWeight: 700, fontSize: '1rem',
              boxShadow: '0 4px 15px rgba(239,68,68,0.3)', transition: 'all 0.2s',
              display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8
            }}>
            <Shield size={18} />
            {loading ? 'Authenticating...' : 'Access Admin Panel'}
          </button>
        </form>

        <div style={{ marginTop: '1.5rem', textAlign: 'center' }}>
          <a href="/" style={{ color: 'var(--text-muted)', fontSize: '0.85rem', textDecoration: 'none' }}>
            ← Back to Code Arena
          </a>
        </div>
      </div>
    </div>
  );
};

export default AdminLoginPage;
