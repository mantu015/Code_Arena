import React, { useState } from 'react';
import axios from 'axios';
import { X, Mail, Lock } from 'lucide-react';

const AuthModal = ({ isOpen, onClose, initialMode = 'login', onAuthSuccess }) => {
  const [mode, setMode] = useState(initialMode); // 'login', 'register', 'forgot'
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [verificationCode, setVerificationCode] = useState('');
  const [verificationSent, setVerificationSent] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccessMsg('');

    if (mode === 'register' && password !== confirmPassword) {
        setError('Passwords do not match. Please try again.');
        return;
    }

    if (mode === 'register' && !/^(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9\s]).+$/.test(password)) {
        setError('Password must contain at least one uppercase letter, one number, and one special character.');
        return;
    }

    if (mode === 'forgot') {
        setLoading(true);
        // Simulate forgot password API workflow
        setTimeout(() => {
            setLoading(false);
            setEmail('');
            setSuccessMsg('If an account matches that email address, a password reset link has been sent!');
        }, 800);
        return;
    }

    setLoading(true);
    try {
      if (mode === 'register') {
          if (!verificationSent) {
            const res = await axios.post('/api/users/register', { email });
            setVerificationSent(true);
            setSuccessMsg(res.data.message);
            return;
          }
          await axios.post('/api/users/verify-email', {
            email,
            code: verificationCode,
            password,
          });
          setEmail('');
          setPassword('');
          setConfirmPassword('');
          setVerificationCode('');
          setVerificationSent(false);
          setSuccessMsg('Email verified and registration successful. Please log in.');
          setMode('login');
      } else {
          const res = await axios.post('/api/users/login', { username: email, password });
          setEmail('');
          setPassword('');
          // Pass full user object so App can store id alongside username
          onAuthSuccess({ id: res.data.id, username: res.data.username || email });
          onClose();
      }
    } catch (err) {
      if (err.response && err.response.data) {
          setError(typeof err.response.data === 'string' ? err.response.data : 'Authentication failed');
      } else {
          setError('Network error: Cannot reach the backend server');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleResendCode = async () => {
    setLoading(true);
    setError('');
    setSuccessMsg('');
    try {
      const res = await axios.post('/api/users/register', { email });
      setSuccessMsg(res.data.message);
    } catch (err) {
      if (err.response?.data) {
        setError(typeof err.response.data === 'string' ? err.response.data : 'Could not send the verification code.');
      } else {
        setError('Network error: Cannot reach the backend server');
      }
    } finally {
      setLoading(false);
    }
  };

  const toggleMode = (newMode) => {
      setMode(newMode);
      setError('');
      setSuccessMsg('');
      setConfirmPassword('');
      setVerificationCode('');
      setVerificationSent(false);
  };

  return (
    <div style={{ position: 'fixed', top: 0, left: 0, width: '100vw', height: '100vh', background: 'rgba(0,0,0,0.7)', backdropFilter: 'blur(8px)', zIndex: 1000, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
      <div className="glass-panel animate-fade-in" style={{ width: '400px', padding: '2.5rem', position: 'relative' }}>
        <button onClick={onClose} style={{ position: 'absolute', top: '1.5rem', right: '1.5rem', background: 'transparent', border: 'none', color: 'var(--text-secondary)', cursor: 'pointer' }}>
          <X size={24} />
        </button>
        
        <h2 style={{ fontSize: '1.8rem', marginBottom: '0.5rem', textAlign: 'center' }}>
          {mode === 'login' ? 'Welcome Back' : mode === 'register' ? 'Create Account' : 'Reset Password'}
        </h2>
        <p style={{ color: 'var(--text-secondary)', textAlign: 'center', marginBottom: '2rem' }}>
          {mode === 'login' ? 'Enter your credentials to continue' : mode === 'register' ? 'Join the arena today' : 'Enter your email address to find your account'}
        </p>

        {error && <div style={{ color: 'var(--error)', background: 'rgba(239, 68, 68, 0.1)', padding: '0.75rem', borderRadius: '8px', marginBottom: '1rem', fontSize: '0.9rem' }}>{error}</div>}
        {successMsg && <div style={{ color: 'var(--success)', background: 'rgba(16, 185, 129, 0.1)', padding: '0.75rem', borderRadius: '8px', marginBottom: '1rem', fontSize: '0.9rem' }}>{successMsg}</div>}

        <form onSubmit={handleSubmit} className="flex-col gap-4">
          <div>
            <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.9rem', color: 'var(--text-secondary)' }}>Email Address</label>
            <div style={{ position: 'relative' }}>
              <Mail size={18} style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-secondary)' }} />
              <input type="email" className="input-field" style={{ paddingLeft: '2.5rem' }} placeholder="Enter email address" value={email} onChange={e => {
                setEmail(e.target.value);
                if (verificationSent) {
                  setVerificationSent(false);
                  setVerificationCode('');
                  setSuccessMsg('');
                }
              }} required />
            </div>
          </div>
          
          {mode !== 'forgot' && (
            <div className="flex-col gap-4">
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.9rem', color: 'var(--text-secondary)' }}>Password</label>
                <div style={{ position: 'relative' }}>
                  <Lock size={18} style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-secondary)' }} />
                  <input type="password" className="input-field" style={{ paddingLeft: '2.5rem' }} placeholder="Enter password" value={password} onChange={e => setPassword(e.target.value)} required />
                </div>
                {mode === 'register' && (
                  <div style={{ color: 'var(--text-secondary)', fontSize: '0.78rem', marginTop: '0.4rem' }}>
                    Include an uppercase letter, a number, and a special character.
                  </div>
                )}
                
                {mode === 'login' && (
                    <div style={{ textAlign: 'right', marginTop: '0.5rem' }}>
                        <span style={{ fontSize: '0.85rem', color: 'var(--brand-primary)', cursor: 'pointer' }} onClick={() => toggleMode('forgot')}>Forgot Password?</span>
                    </div>
                )}
              </div>

              {mode === 'register' && (
                <div>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.9rem', color: 'var(--text-secondary)' }}>Confirm Password</label>
                  <div style={{ position: 'relative' }}>
                    <Lock size={18} style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-secondary)' }} />
                    <input type="password" className="input-field" style={{ paddingLeft: '2.5rem' }} placeholder="Re-enter password" value={confirmPassword} onChange={e => setConfirmPassword(e.target.value)} required />
                  </div>
                </div>
              )}

              {mode === 'register' && verificationSent && (
                <div>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.9rem', color: 'var(--text-secondary)' }}>Email Verification Code</label>
                  <input
                    type="text"
                    className="input-field"
                    placeholder="Enter the 6-digit code"
                    value={verificationCode}
                    onChange={e => setVerificationCode(e.target.value.replace(/\D/g, '').slice(0, 6))}
                    inputMode="numeric"
                    pattern="[0-9]{6}"
                    maxLength={6}
                    required
                  />
                  <button type="button" onClick={handleResendCode} disabled={loading}
                    style={{ marginTop: '0.5rem', padding: 0, background: 'none', border: 'none',
                      color: 'var(--brand-primary)', cursor: loading ? 'default' : 'pointer', fontSize: '0.85rem' }}>
                    Resend verification code
                  </button>
                </div>
              )}
            </div>
          )}

          <button type="submit" className="btn btn-primary" style={{ width: '100%', marginTop: '1rem', padding: '0.875rem' }} disabled={loading}>
            {loading ? 'Processing...' : (mode === 'login' ? 'Sign In' : mode === 'register' ? (verificationSent ? 'Verify Email & Sign Up' : 'Send Verification Code') : 'Send Reset Link')}
          </button>
        </form>

        <div style={{ marginTop: '1.5rem', textAlign: 'center', fontSize: '0.9rem', color: 'var(--text-secondary)' }}>
          {mode === 'login' ? "Don't have an account? " : mode === 'register' ? "Already have an account? " : "Remember your password? "}
          <span style={{ color: 'var(--brand-primary)', cursor: 'pointer', fontWeight: 500 }} onClick={() => toggleMode(mode === 'login' ? 'register' : 'login')}>
            {mode === 'login' ? 'Sign Up' : 'Sign In'}
          </span>
        </div>
      </div>
    </div>
  );
};

export default AuthModal;
