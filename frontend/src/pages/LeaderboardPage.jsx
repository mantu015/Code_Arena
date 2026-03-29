import React, { useState, useEffect } from 'react';
import axios from 'axios';
import API_BASE_URL from '../apiConfig';
import { Trophy, Star, Zap, Crown } from 'lucide-react';

const RANK_TIERS = [
  { min: 0,    label: 'Newcomer',    color: '#9ca3af' },
  { min: 50,   label: 'Coder',       color: '#10b981' },
  { min: 150,  label: 'Solver',      color: '#3b82f6' },
  { min: 300,  label: 'Expert',      color: '#8b5cf6' },
  { min: 600,  label: 'Master',      color: '#f59e0b' },
  { min: 1000, label: 'Grandmaster', color: '#ef4444' },
];

const getTier = pts => [...RANK_TIERS].reverse().find(t => pts >= t.min) || RANK_TIERS[0];

const PODIUM = [
  { rank: 2, height: 120, color: '#9ca3af', crown: false, label: '2nd' },
  { rank: 1, height: 160, color: '#fbbf24', crown: true,  label: '1st' },
  { rank: 3, height: 90,  color: '#b45309', crown: false, label: '3rd' },
];

const PodiumCard = ({ user, config }) => {
  if (!user) return <div style={{ flex: 1 }} />;
  const tier = getTier(user.points);
  return (
    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 8 }}>
      {config.crown && <Crown size={24} color="#fbbf24" fill="#fbbf24" />}
      <div style={{
        width: 56, height: 56, borderRadius: '50%',
        background: `linear-gradient(135deg, ${config.color}44, ${config.color}22)`,
        border: `2px solid ${config.color}`,
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        fontSize: '1.3rem', fontWeight: 700, color: config.color
      }}>
        {user.username.charAt(0).toUpperCase()}
      </div>
      <div style={{ textAlign: 'center' }}>
        <div style={{ fontWeight: 700, fontSize: '0.95rem' }}>{user.username}</div>
        <div style={{ fontSize: '0.78rem', color: tier.color, fontWeight: 600 }}>{tier.label}</div>
        <div style={{ fontSize: '0.85rem', color: config.color, fontWeight: 700, marginTop: 2,
          display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 4 }}>
          <Star size={12} fill={config.color} color={config.color} /> {user.points.toLocaleString()}
        </div>
      </div>
      <div style={{
        width: '100%', height: config.height, borderRadius: '8px 8px 0 0',
        background: `linear-gradient(180deg, ${config.color}33 0%, ${config.color}11 100%)`,
        border: `1px solid ${config.color}44`, borderBottom: 'none',
        display: 'flex', alignItems: 'flex-start', justifyContent: 'center', paddingTop: 12
      }}>
        <span style={{ fontSize: '1.5rem', fontWeight: 800, color: config.color, opacity: 0.6 }}>
          {config.label}
        </span>
      </div>
    </div>
  );
};

const LeaderboardPage = ({ currentUser }) => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    axios.get(`${API_BASE_URL}/api/leaderboard`)
      .then(r => setUsers(r.data?.length > 0 ? r.data : MOCK))
      .catch(() => setUsers(MOCK))
      .finally(() => setLoading(false));
  }, []);

  const top3 = [users[1], users[0], users[2]]; // podium order: 2nd, 1st, 3rd
  const rest = users.slice(3);

  return (
    <div className="main-container animate-fade-in" style={{ paddingTop: '3rem', paddingBottom: '4rem' }}>

      {/* Header */}
      <div style={{ textAlign: 'center', marginBottom: '3rem' }}>
        <div style={{ display: 'inline-flex', alignItems: 'center', gap: 8, padding: '6px 18px',
          background: 'rgba(245,158,11,0.1)', border: '1px solid rgba(245,158,11,0.25)',
          borderRadius: 999, color: 'var(--warning)', fontWeight: 600, marginBottom: '1.25rem' }}>
          <Trophy size={15} fill="currentColor" /> Global Leaderboard
        </div>
        <h1 style={{ fontSize: '3rem', marginBottom: 8 }}>Top Coders</h1>
        <p style={{ color: 'var(--text-secondary)', fontSize: '1rem' }}>
          Earn points by solving problems and climb the ranks
        </p>
      </div>

      {loading ? (
        <div style={{ textAlign: 'center', color: 'var(--text-secondary)', padding: '4rem' }}>Loading...</div>
      ) : (
        <>
          {/* Podium — top 3 */}
          {users.length >= 3 && (
            <div style={{ display: 'flex', alignItems: 'flex-end', justifyContent: 'center',
              gap: '1rem', marginBottom: '3rem', maxWidth: 600, margin: '0 auto 3rem' }}>
              {PODIUM.map((cfg, i) => (
                <PodiumCard key={i} user={top3[i]} config={cfg} />
              ))}
            </div>
          )}

          {/* Rank list — 4th onwards */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.6rem' }}>
            {/* Header row */}
            <div style={{ display: 'grid', gridTemplateColumns: '60px 1fr 120px 140px',
              padding: '0.6rem 1.5rem', fontSize: '0.78rem', color: 'var(--text-muted)',
              fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.05em' }}>
              <span>Rank</span><span>Player</span><span style={{ textAlign: 'right' }}>Tier</span>
              <span style={{ textAlign: 'right' }}>Points</span>
            </div>

            {/* Top 3 in list too */}
            {users.slice(0, 3).map((user, i) => {
              const tier = getTier(user.points);
              const rankColors = ['#fbbf24', '#9ca3af', '#b45309'];
              const isMe = currentUser?.id === user.id;
              return (
                <div key={user.id}
                  style={{
                    display: 'grid', gridTemplateColumns: '60px 1fr 120px 140px',
                    padding: '0.875rem 1.5rem', borderRadius: 12,
                    background: isMe ? 'rgba(99,102,241,0.08)' : 'rgba(255,255,255,0.03)',
                    border: isMe ? '1px solid rgba(99,102,241,0.3)' : `1px solid ${rankColors[i]}22`,
                    alignItems: 'center', transition: 'transform 0.15s',
                  }}
                  onMouseOver={e => e.currentTarget.style.transform = 'translateX(4px)'}
                  onMouseOut={e => e.currentTarget.style.transform = 'translateX(0)'}>
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                    <span style={{ fontSize: '1.2rem' }}>
                      {i === 0 ? '🥇' : i === 1 ? '🥈' : '🥉'}
                    </span>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                    <div style={{ width: 36, height: 36, borderRadius: '50%',
                      background: `linear-gradient(135deg, ${rankColors[i]}44, ${rankColors[i]}22)`,
                      border: `1.5px solid ${rankColors[i]}`,
                      display: 'flex', alignItems: 'center', justifyContent: 'center',
                      fontSize: '0.9rem', fontWeight: 700, color: rankColors[i] }}>
                      {user.username.charAt(0).toUpperCase()}
                    </div>
                    <div>
                      <div style={{ fontWeight: 600, fontSize: '0.95rem' }}>
                        {user.username} {isMe && <span style={{ fontSize: '0.75rem', color: 'var(--brand-primary)', marginLeft: 6 }}>(you)</span>}
                      </div>
                    </div>
                  </div>
                  <div style={{ textAlign: 'right' }}>
                    <span style={{ padding: '3px 10px', borderRadius: 999, fontSize: '0.75rem', fontWeight: 700,
                      background: `${tier.color}18`, color: tier.color, border: `1px solid ${tier.color}33` }}>
                      {tier.label}
                    </span>
                  </div>
                  <div style={{ textAlign: 'right', fontWeight: 700, color: rankColors[i],
                    display: 'flex', alignItems: 'center', justifyContent: 'flex-end', gap: 5 }}>
                    <Star size={13} fill={rankColors[i]} color={rankColors[i]} />
                    {user.points.toLocaleString()}
                  </div>
                </div>
              );
            })}

            {/* Rest of the list */}
            {rest.map((user, i) => {
              const rank = i + 4;
              const tier = getTier(user.points);
              const isMe = currentUser?.id === user.id;
              return (
                <div key={user.id}
                  style={{
                    display: 'grid', gridTemplateColumns: '60px 1fr 120px 140px',
                    padding: '0.75rem 1.5rem', borderRadius: 10,
                    background: isMe ? 'rgba(99,102,241,0.08)' : 'rgba(255,255,255,0.02)',
                    border: isMe ? '1px solid rgba(99,102,241,0.3)' : '1px solid var(--border-color)',
                    alignItems: 'center', transition: 'all 0.15s',
                  }}
                  onMouseOver={e => { e.currentTarget.style.background = isMe ? 'rgba(99,102,241,0.1)' : 'rgba(255,255,255,0.04)'; e.currentTarget.style.transform = 'translateX(4px)'; }}
                  onMouseOut={e => { e.currentTarget.style.background = isMe ? 'rgba(99,102,241,0.08)' : 'rgba(255,255,255,0.02)'; e.currentTarget.style.transform = 'translateX(0)'; }}>
                  <div style={{ textAlign: 'center', fontWeight: 700, color: 'var(--text-muted)', fontSize: '0.9rem' }}>
                    #{rank}
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                    <div style={{ width: 34, height: 34, borderRadius: '50%',
                      background: 'rgba(255,255,255,0.06)', border: '1px solid var(--border-color)',
                      display: 'flex', alignItems: 'center', justifyContent: 'center',
                      fontSize: '0.85rem', fontWeight: 700 }}>
                      {user.username.charAt(0).toUpperCase()}
                    </div>
                    <span style={{ fontWeight: 500, fontSize: '0.92rem' }}>
                      {user.username} {isMe && <span style={{ fontSize: '0.75rem', color: 'var(--brand-primary)', marginLeft: 6 }}>(you)</span>}
                    </span>
                  </div>
                  <div style={{ textAlign: 'right' }}>
                    <span style={{ padding: '2px 8px', borderRadius: 999, fontSize: '0.72rem', fontWeight: 600,
                      background: `${tier.color}12`, color: tier.color }}>
                      {tier.label}
                    </span>
                  </div>
                  <div style={{ textAlign: 'right', fontWeight: 600, color: 'var(--brand-primary)',
                    display: 'flex', alignItems: 'center', justifyContent: 'flex-end', gap: 5, fontSize: '0.9rem' }}>
                    <Zap size={12} fill="var(--brand-primary)" color="var(--brand-primary)" />
                    {user.points.toLocaleString()}
                  </div>
                </div>
              );
            })}
          </div>
        </>
      )}
    </div>
  );
};

const MOCK = [
  { id: 1, username: 'algo_master99', points: 1540 },
  { id: 2, username: 'bug_hunter',    points: 1250 },
  { id: 3, username: 'byte_wizard',   points: 980  },
  { id: 4, username: 'neo_matrix',    points: 870  },
  { id: 5, username: 'coffee_coder',  points: 850  },
  { id: 6, username: 'dev_ninja',     points: 720  },
];

export default LeaderboardPage;
