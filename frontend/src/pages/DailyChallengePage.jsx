import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import axios from 'axios';
import API_BASE_URL from '../apiConfig';
import { Flame, Clock, Play, Star, CheckCircle } from 'lucide-react';

const pad = n => String(n).padStart(2, '0');

const Countdown = () => {
  const [timeLeft, setTimeLeft] = useState({ h: 0, m: 0, s: 0 });

  useEffect(() => {
    const tick = () => {
      const now = new Date();
      const midnight = new Date();
      midnight.setHours(24, 0, 0, 0);
      const diff = Math.floor((midnight - now) / 1000);
      setTimeLeft({ h: Math.floor(diff / 3600), m: Math.floor((diff % 3600) / 60), s: diff % 60 });
    };
    tick();
    const id = setInterval(tick, 1000);
    return () => clearInterval(id);
  }, []);

  return (
    <div style={{ display: 'flex', gap: 12, justifyContent: 'center', marginTop: '1rem' }}>
      {[['h', timeLeft.h], ['m', timeLeft.m], ['s', timeLeft.s]].map(([label, val]) => (
        <div key={label} style={{ textAlign: 'center' }}>
          <div style={{ fontSize: '2.5rem', fontWeight: 700, fontFamily: 'Fira Code, monospace',
            background: 'var(--brand-gradient)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent',
            lineHeight: 1 }}>
            {pad(val)}
          </div>
          <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: 2 }}>
            {label}
          </div>
        </div>
      ))}
    </div>
  );
};

const DailyChallengePage = ({ currentUser }) => {
  const [challenge, setChallenge] = useState(null);
  const [loading, setLoading] = useState(true);
  const [claiming, setClaiming] = useState(false);
  const [claimMsg, setClaimMsg] = useState('');
  const [claimError, setClaimError] = useState('');

  useEffect(() => {
    axios.get(`${API_BASE_URL}/api/daily`)
      .then(r => setChallenge(r.data))
      .catch(() => setChallenge(null))
      .finally(() => setLoading(false));
  }, []);

  const handleClaim = async () => {
    if (!currentUser?.id) return;
    setClaiming(true);
    setClaimMsg('');
    setClaimError('');
    try {
      const res = await axios.post(`${API_BASE_URL}/api/daily/claim`, { userId: currentUser.id });
      setClaimMsg(res.data.message);
    } catch (err) {
      setClaimError(err.response?.data || 'Could not claim bonus.');
    }
    setClaiming(false);
  };

  return (
    <div className="main-container animate-fade-in" style={{ paddingTop: '4rem', paddingBottom: '4rem', maxWidth: 700, margin: '0 auto' }}>

      {/* Header */}
      <div style={{ textAlign: 'center', marginBottom: '3rem' }}>
        <div style={{ display: 'inline-flex', alignItems: 'center', gap: 8, padding: '6px 18px',
          background: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.25)',
          borderRadius: 999, color: 'var(--error)', fontWeight: 600, marginBottom: '1.5rem' }}>
          <Flame size={16} fill="currentColor" /> Daily Challenge
        </div>
        <h1 style={{ fontSize: '3rem', marginBottom: '0.5rem' }}>Today's Problem</h1>
        <p style={{ color: 'var(--text-secondary)' }}>Solve it before midnight for a <strong style={{ color: 'var(--warning)' }}>+50 bonus points</strong>!</p>
        <div style={{ marginTop: '1.5rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8,
            color: 'var(--text-secondary)', fontSize: '0.85rem', marginBottom: 4 }}>
            <Clock size={14} /> Resets in
          </div>
          <Countdown />
        </div>
      </div>

      {loading ? (
        <div style={{ textAlign: 'center', color: 'var(--text-secondary)' }}>Loading challenge...</div>
      ) : !challenge ? (
        <div style={{ textAlign: 'center', color: 'var(--error)' }}>Could not load daily challenge. Start the backend server.</div>
      ) : (
        <div className="glass-panel" style={{ padding: '2rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1.5rem' }}>
            <div>
              <h2 style={{ fontSize: '1.6rem', marginBottom: 8 }}>{challenge.problem?.title}</h2>
              <div style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
                <span className={`badge badge-${(challenge.problem?.difficulty || 'easy').toLowerCase()}`}>
                  {challenge.problem?.difficulty}
                </span>
                <span style={{ fontSize: '0.85rem', color: 'var(--warning)', display: 'flex', alignItems: 'center', gap: 4 }}>
                  <Star size={13} fill="var(--warning)" /> +{challenge.bonusPoints} bonus pts
                </span>
              </div>
            </div>
            <div style={{ fontSize: '0.82rem', color: 'var(--text-muted)' }}>
              {new Date(challenge.challengeDate).toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric' })}
            </div>
          </div>

          <p style={{ color: 'var(--text-secondary)', lineHeight: 1.7, marginBottom: '2rem',
            display: '-webkit-box', WebkitLineClamp: 4, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
            {challenge.problem?.description}
          </p>

          <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap' }}>
            <Link to={`/problem/${challenge.problem?.id}`} className="btn btn-primary" style={{ flex: 1, justifyContent: 'center' }}>
              <Play size={16} fill="currentColor" /> Solve Now
            </Link>
            {currentUser && (
              <button className="btn btn-secondary" style={{ flex: 1 }} onClick={handleClaim} disabled={claiming}>
                <CheckCircle size={16} /> {claiming ? 'Claiming...' : 'Claim Bonus'}
              </button>
            )}
          </div>

          {claimMsg && (
            <div style={{ marginTop: '1rem', padding: '0.75rem', borderRadius: 8,
              background: 'rgba(16,185,129,0.1)', color: 'var(--success)', fontWeight: 600, textAlign: 'center' }}>
              🎉 {claimMsg}
            </div>
          )}
          {claimError && (
            <div style={{ marginTop: '1rem', padding: '0.75rem', borderRadius: 8,
              background: 'rgba(239,68,68,0.1)', color: 'var(--error)', textAlign: 'center' }}>
              {claimError}
            </div>
          )}
          {!currentUser && (
            <div style={{ marginTop: '1rem', textAlign: 'center', color: 'var(--text-secondary)', fontSize: '0.88rem' }}>
              Sign in to claim the bonus points after solving!
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default DailyChallengePage;
