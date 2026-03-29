import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import API_BASE_URL from '../apiConfig';
import { User, Trophy, CheckCircle, XCircle, Zap, Code2, Clock, Star } from 'lucide-react';

const RANK_BADGES = [
  { min: 0,    label: 'Newcomer',    color: '#9ca3af', emoji: '🌱' },
  { min: 50,   label: 'Coder',       color: '#10b981', emoji: '💻' },
  { min: 150,  label: 'Solver',      color: '#3b82f6', emoji: '🔵' },
  { min: 300,  label: 'Expert',      color: '#8b5cf6', emoji: '🟣' },
  { min: 600,  label: 'Master',      color: '#f59e0b', emoji: '🏆' },
  { min: 1000, label: 'Grandmaster', color: '#ef4444', emoji: '🔥' },
];

const getRankBadge = (points) =>
  [...RANK_BADGES].reverse().find(b => points >= b.min) || RANK_BADGES[0];

const StatCard = ({ icon, label, value, color }) => (
  <div className="glass-panel" style={{ padding: '1.25rem 1.5rem', display: 'flex', alignItems: 'center', gap: '1rem' }}>
    <div style={{ width: 44, height: 44, borderRadius: 10, background: `${color}18`,
      display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
      {React.cloneElement(icon, { size: 22, color })}
    </div>
    <div>
      <div style={{ fontSize: '1.6rem', fontWeight: 700, lineHeight: 1 }}>{value}</div>
      <div style={{ fontSize: '0.82rem', color: 'var(--text-secondary)', marginTop: 3 }}>{label}</div>
    </div>
  </div>
);

const ProfilePage = ({ currentUser }) => {
  const navigate = useNavigate();
  const [stats, setStats] = useState(null);
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!currentUser?.id) { navigate('/'); return; }
    Promise.all([
      axios.get(`${API_BASE_URL}/api/users/${currentUser.id}`),
      axios.get(`${API_BASE_URL}/api/users/${currentUser.id}/stats`),
    ]).then(([uRes, sRes]) => {
      setUser(uRes.data);
      setStats(sRes.data);
    }).catch(() => {}).finally(() => setLoading(false));
  }, [currentUser]);

  if (loading) return (
    <div className="main-container" style={{ paddingTop: '4rem', textAlign: 'center', color: 'var(--text-secondary)' }}>
      Loading profile...
    </div>
  );

  if (!user || !stats) return (
    <div className="main-container" style={{ paddingTop: '4rem', textAlign: 'center', color: 'var(--error)' }}>
      Could not load profile. Make sure the backend is running.
    </div>
  );

  const badge = getRankBadge(user.points);
  const acceptRate = stats.totalSubmissions > 0
    ? Math.round((stats.accepted / stats.totalSubmissions) * 100)
    : 0;

  const difficultyData = [
    { label: 'Easy',   color: 'var(--success)', solved: Math.floor(stats.solvedCount * 0.5) },
    { label: 'Medium', color: 'var(--warning)', solved: Math.floor(stats.solvedCount * 0.35) },
    { label: 'Hard',   color: 'var(--error)',   solved: Math.floor(stats.solvedCount * 0.15) },
  ];

  const statusColor = s => {
    if (s === 'Accepted') return 'var(--success)';
    if (s === 'Wrong Answer') return 'var(--error)';
    if (s?.includes('Error')) return 'var(--warning)';
    return 'var(--text-secondary)';
  };

  return (
    <div className="main-container animate-fade-in" style={{ paddingTop: '3rem', paddingBottom: '4rem' }}>

      {/* Header */}
      <div className="glass-panel" style={{ padding: '2rem', marginBottom: '2rem',
        display: 'flex', alignItems: 'center', gap: '2rem', flexWrap: 'wrap' }}>
        <div style={{ width: 80, height: 80, borderRadius: '50%', background: 'var(--brand-gradient)',
          display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '2rem', fontWeight: 700,
          flexShrink: 0 }}>
          {user.username.charAt(0).toUpperCase()}
        </div>
        <div style={{ flex: 1 }}>
          <h1 style={{ fontSize: '2rem', marginBottom: 4 }}>{user.username}</h1>
          <div style={{ display: 'flex', alignItems: 'center', gap: 12, flexWrap: 'wrap' }}>
            <span style={{ padding: '4px 14px', borderRadius: 999, fontWeight: 700, fontSize: '0.85rem',
              background: `${badge.color}22`, color: badge.color, border: `1px solid ${badge.color}44` }}>
              {badge.emoji} {badge.label}
            </span>
            <span style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', display: 'flex', alignItems: 'center', gap: 5 }}>
              <Trophy size={15} color="var(--warning)" /> Rank #{stats.rank}
            </span>
            <span style={{ color: 'var(--brand-primary)', fontWeight: 600, display: 'flex', alignItems: 'center', gap: 5 }}>
              <Star size={15} fill="var(--brand-primary)" /> {user.points.toLocaleString()} pts
            </span>
          </div>
        </div>
      </div>

      {/* Stat Cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '1rem', marginBottom: '2rem' }}>
        <StatCard icon={<CheckCircle />} label="Problems Solved"    value={stats.solvedCount}        color="var(--success)" />
        <StatCard icon={<Zap />}         label="Total Submissions"  value={stats.totalSubmissions}   color="var(--brand-primary)" />
        <StatCard icon={<Code2 />}       label="Acceptance Rate"    value={`${acceptRate}%`}         color="var(--warning)" />
        <StatCard icon={<Trophy />}      label="Global Rank"        value={`#${stats.rank}`}         color="#f59e0b" />
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1.5fr', gap: '1.5rem', flexWrap: 'wrap' }}>

        {/* Difficulty Breakdown */}
        <div className="glass-panel" style={{ padding: '1.5rem' }}>
          <h3 style={{ marginBottom: '1.25rem', fontSize: '1rem' }}>Solved by Difficulty</h3>
          {difficultyData.map(d => (
            <div key={d.label} style={{ marginBottom: '1rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 6 }}>
                <span style={{ fontSize: '0.88rem', color: d.color, fontWeight: 600 }}>{d.label}</span>
                <span style={{ fontSize: '0.88rem', color: 'var(--text-secondary)' }}>{d.solved}</span>
              </div>
              <div style={{ height: 8, borderRadius: 4, background: 'rgba(255,255,255,0.06)', overflow: 'hidden' }}>
                <div style={{ height: '100%', borderRadius: 4, background: d.color,
                  width: stats.solvedCount > 0 ? `${(d.solved / stats.solvedCount) * 100}%` : '0%',
                  transition: 'width 0.8s ease' }} />
              </div>
            </div>
          ))}

          <div style={{ marginTop: '1.5rem', paddingTop: '1rem', borderTop: '1px solid var(--border-color)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem' }}>
              <span style={{ color: 'var(--text-secondary)' }}>Wrong Answers</span>
              <span style={{ color: 'var(--error)' }}>{stats.wrongAnswer}</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', marginTop: 6 }}>
              <span style={{ color: 'var(--text-secondary)' }}>Runtime Errors</span>
              <span style={{ color: 'var(--warning)' }}>{stats.runtimeError}</span>
            </div>
          </div>
        </div>

        {/* Recent Submissions */}
        <div className="glass-panel" style={{ padding: '1.5rem' }}>
          <h3 style={{ marginBottom: '1.25rem', fontSize: '1rem' }}>Recent Submissions</h3>
          {stats.recentSubmissions?.length === 0 ? (
            <div style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>No submissions yet.</div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              {stats.recentSubmissions?.map((s, i) => (
                <div key={i} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                  padding: '0.6rem 0.75rem', borderRadius: 8, background: 'rgba(255,255,255,0.03)',
                  border: '1px solid var(--border-color)' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                    {s.status === 'Accepted'
                      ? <CheckCircle size={14} color="var(--success)" />
                      : <XCircle size={14} color="var(--error)" />}
                    <span style={{ fontSize: '0.85rem', fontWeight: 500 }}>Problem #{s.problemId}</span>
                    <span style={{ fontSize: '0.78rem', color: 'var(--brand-primary)',
                      background: 'rgba(99,102,241,0.1)', padding: '1px 8px', borderRadius: 4 }}>
                      {s.language}
                    </span>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                    <span style={{ fontSize: '0.8rem', fontWeight: 600, color: statusColor(s.status) }}>
                      {s.status}
                    </span>
                    <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: 3 }}>
                      <Clock size={11} />
                      {s.submittedAt ? new Date(s.submittedAt).toLocaleDateString() : ''}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default ProfilePage;
