import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import API_BASE_URL from '../apiConfig';
import {
  Shield, Users, FileCode2, BarChart2, LogOut, Trash2,
  CheckCircle, XCircle, Clock, Code2, RefreshCcw, AlertTriangle
} from 'lucide-react';

const TABS = [
  { id: 'stats',       label: 'Overview',    icon: <BarChart2 size={16} /> },
  { id: 'users',       label: 'Users',       icon: <Users size={16} /> },
  { id: 'submissions', label: 'Submissions', icon: <FileCode2 size={16} /> },
];

const StatCard = ({ icon, label, value, color, sub }) => (
  <div className="glass-panel" style={{ padding: '1.25rem 1.5rem', display: 'flex', alignItems: 'center', gap: '1rem' }}>
    <div style={{ width: 46, height: 46, borderRadius: 10, background: `${color}18`,
      display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
      {React.cloneElement(icon, { size: 22, color })}
    </div>
    <div>
      <div style={{ fontSize: '1.7rem', fontWeight: 700, lineHeight: 1 }}>{value}</div>
      <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginTop: 3 }}>{label}</div>
      {sub && <div style={{ fontSize: '0.75rem', color, marginTop: 2 }}>{sub}</div>}
    </div>
  </div>
);

const statusColor = s => {
  if (s === 'Accepted') return 'var(--success)';
  if (s === 'Wrong Answer') return 'var(--error)';
  if (s?.includes('Error')) return 'var(--warning)';
  return 'var(--text-secondary)';
};

const LANG_COLORS = { javascript: '#f7df1e', python: '#3776ab', java: '#ed8b00', cpp: '#00599c' };

const AdminDashboard = ({ adminUser, onLogout }) => {
  const navigate = useNavigate();
  const [tab, setTab] = useState('stats');
  const [stats, setStats] = useState(null);
  const [users, setUsers] = useState([]);
  const [submissions, setSubmissions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [deleteConfirm, setDeleteConfirm] = useState(null);

  const token = `admin:${adminUser?.username}`;

  const headers = { 'X-Admin-Token': token };

  const fetchAll = async () => {
    setLoading(true);
    try {
      const [sRes, uRes, subRes] = await Promise.all([
        axios.get(`${API_BASE_URL}/api/admin/stats`, { headers }),
        axios.get(`${API_BASE_URL}/api/admin/users`, { headers }),
        axios.get(`${API_BASE_URL}/api/admin/submissions`, { headers }),
      ]);
      setStats(sRes.data);
      setUsers(uRes.data);
      setSubmissions(subRes.data);
    } catch { /* handle silently */ }
    setLoading(false);
  };

  useEffect(() => {
    if (!adminUser) { navigate('/admin'); return; }
    fetchAll();
  }, [adminUser]);

  const handleDelete = async (userId) => {
    try {
      await axios.delete(`${API_BASE_URL}/api/admin/users/${userId}`, { headers });
      setUsers(u => u.filter(x => x.id !== userId));
      setDeleteConfirm(null);
    } catch { /* ignore */ }
  };

  const handleLogout = () => { onLogout(); navigate('/admin'); };

  return (
    <div style={{ minHeight: '100vh', background: 'var(--bg-primary)', display: 'flex', flexDirection: 'column' }}>

      {/* Top Bar */}
      <div style={{
        padding: '0.875rem 2rem', borderBottom: '1px solid var(--border-color)',
        background: 'rgba(239,68,68,0.04)', backdropFilter: 'blur(12px)',
        display: 'flex', justifyContent: 'space-between', alignItems: 'center',
        position: 'sticky', top: 0, zIndex: 100
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <div style={{ width: 32, height: 32, borderRadius: 8, background: 'rgba(239,68,68,0.15)',
            display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <Shield size={18} color="#ef4444" />
          </div>
          <div>
            <span style={{ fontWeight: 700, fontSize: '1rem' }}>Code<span style={{ color: '#ef4444' }}>Arena</span> Admin</span>
            <span style={{ marginLeft: 12, fontSize: '0.75rem', color: 'var(--text-muted)',
              background: 'rgba(239,68,68,0.1)', padding: '2px 8px', borderRadius: 4,
              border: '1px solid rgba(239,68,68,0.2)', color: '#ef4444' }}>
              ADMIN
            </span>
          </div>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <span style={{ fontSize: '0.88rem', color: 'var(--text-secondary)' }}>
            Logged in as <strong style={{ color: 'var(--text-primary)' }}>{adminUser?.username}</strong>
          </span>
          <button onClick={fetchAll} className="btn btn-secondary" style={{ padding: '0.4rem 0.75rem', fontSize: '0.82rem' }}>
            <RefreshCcw size={14} /> Refresh
          </button>
          <button onClick={handleLogout} className="btn btn-secondary"
            style={{ padding: '0.4rem 0.75rem', fontSize: '0.82rem', color: 'var(--error)', borderColor: 'rgba(239,68,68,0.3)' }}>
            <LogOut size={14} /> Logout
          </button>
        </div>
      </div>

      <div style={{ display: 'flex', flex: 1 }}>

        {/* Sidebar */}
        <div style={{ width: 220, borderRight: '1px solid var(--border-color)',
          padding: '1.5rem 1rem', background: 'rgba(0,0,0,0.15)', flexShrink: 0 }}>
          {TABS.map(t => (
            <button key={t.id} onClick={() => setTab(t.id)}
              style={{
                width: '100%', display: 'flex', alignItems: 'center', gap: 10,
                padding: '0.65rem 1rem', borderRadius: 8, border: 'none', cursor: 'pointer',
                marginBottom: 4, fontWeight: 500, fontSize: '0.9rem', transition: 'all 0.15s',
                background: tab === t.id ? 'rgba(239,68,68,0.12)' : 'transparent',
                color: tab === t.id ? '#ef4444' : 'var(--text-secondary)',
              }}>
              {t.icon} {t.label}
              {t.id === 'users' && users.length > 0 && (
                <span style={{ marginLeft: 'auto', fontSize: '0.75rem', background: 'rgba(255,255,255,0.08)',
                  padding: '1px 7px', borderRadius: 10 }}>{users.length}</span>
              )}
              {t.id === 'submissions' && submissions.length > 0 && (
                <span style={{ marginLeft: 'auto', fontSize: '0.75rem', background: 'rgba(255,255,255,0.08)',
                  padding: '1px 7px', borderRadius: 10 }}>{submissions.length}</span>
              )}
            </button>
          ))}
        </div>

        {/* Main Content */}
        <div style={{ flex: 1, padding: '2rem', overflowY: 'auto' }}>
          {loading ? (
            <div style={{ textAlign: 'center', color: 'var(--text-secondary)', paddingTop: '4rem' }}>
              Loading platform data...
            </div>
          ) : (

            // ── OVERVIEW TAB ──────────────────────────────────────────────
            tab === 'stats' && stats && (
              <div className="animate-fade-in">
                <h2 style={{ fontSize: '1.5rem', marginBottom: '1.5rem' }}>Platform Overview</h2>

                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '1rem', marginBottom: '2rem' }}>
                  <StatCard icon={<Users />}     label="Total Users"       value={stats.totalUsers}       color="var(--brand-primary)" />
                  <StatCard icon={<Code2 />}     label="Problems"          value={stats.totalProblems}    color="var(--success)" />
                  <StatCard icon={<FileCode2 />} label="Submissions"       value={stats.totalSubmissions} color="var(--warning)" />
                  <StatCard icon={<CheckCircle />} label="Accepted"        value={stats.accepted}         color="var(--success)"
                    sub={`${stats.acceptanceRate}% acceptance rate`} />
                  <StatCard icon={<XCircle />}   label="Wrong Answers"     value={stats.wrongAnswer}      color="var(--error)" />
                  <StatCard icon={<AlertTriangle />} label="Errors"        value={stats.errors}           color="var(--warning)" />
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem', marginBottom: '1.5rem' }}>

                  {/* Language Distribution */}
                  <div className="glass-panel" style={{ padding: '1.5rem' }}>
                    <h3 style={{ fontSize: '1rem', marginBottom: '1.25rem' }}>Language Distribution</h3>
                    {Object.entries(stats.languageDistribution || {})
                      .sort((a, b) => b[1] - a[1])
                      .map(([lang, count]) => {
                        const pct = stats.totalSubmissions > 0 ? Math.round((count / stats.totalSubmissions) * 100) : 0;
                        const color = LANG_COLORS[lang] || 'var(--brand-primary)';
                        return (
                          <div key={lang} style={{ marginBottom: '0.875rem' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 5 }}>
                              <span style={{ fontSize: '0.88rem', fontWeight: 600, textTransform: 'capitalize' }}>{lang}</span>
                              <span style={{ fontSize: '0.82rem', color: 'var(--text-secondary)' }}>{count} ({pct}%)</span>
                            </div>
                            <div style={{ height: 7, borderRadius: 4, background: 'rgba(255,255,255,0.06)' }}>
                              <div style={{ height: '100%', borderRadius: 4, background: color, width: `${pct}%`, transition: 'width 0.8s ease' }} />
                            </div>
                          </div>
                        );
                      })}
                  </div>

                  {/* Top Problems */}
                  <div className="glass-panel" style={{ padding: '1.5rem' }}>
                    <h3 style={{ fontSize: '1rem', marginBottom: '1.25rem' }}>Most Attempted Problems</h3>
                    {(stats.topProblems || []).map((p, i) => (
                      <div key={i} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                        padding: '0.6rem 0', borderBottom: '1px solid var(--border-color)' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                          <span style={{ width: 22, height: 22, borderRadius: '50%', background: 'var(--brand-gradient)',
                            display: 'flex', alignItems: 'center', justifyContent: 'center',
                            fontSize: '0.72rem', fontWeight: 700, color: 'white', flexShrink: 0 }}>{i + 1}</span>
                          <span style={{ fontSize: '0.88rem' }}>{p.title || `Problem #${p.problemId}`}</span>
                        </div>
                        <span style={{ fontSize: '0.82rem', color: 'var(--brand-primary)', fontWeight: 600 }}>
                          {p.attempts} attempts
                        </span>
                      </div>
                    ))}
                  </div>
                </div>

                {/* Recent Submissions */}
                <div className="glass-panel" style={{ overflow: 'hidden' }}>
                  <div style={{ padding: '1rem 1.5rem', borderBottom: '1px solid var(--border-color)', fontWeight: 700 }}>
                    Recent Submissions
                  </div>
                  <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                    <thead>
                      <tr style={{ background: 'rgba(0,0,0,0.2)', fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                        {['User', 'Problem', 'Language', 'Status', 'Time'].map(h => (
                          <th key={h} style={{ padding: '0.75rem 1rem', textAlign: 'left', fontWeight: 600 }}>{h}</th>
                        ))}
                      </tr>
                    </thead>
                    <tbody>
                      {(stats.recentSubmissions || []).map((s, i) => (
                        <tr key={i} style={{ borderBottom: '1px solid var(--border-color)', fontSize: '0.85rem' }}>
                          <td style={{ padding: '0.75rem 1rem', fontWeight: 500 }}>{s.username || `#${s.userId}`}</td>
                          <td style={{ padding: '0.75rem 1rem', color: 'var(--text-secondary)' }}>{s.problemTitle || `#${s.problemId}`}</td>
                          <td style={{ padding: '0.75rem 1rem' }}>
                            <span style={{ padding: '2px 8px', borderRadius: 4, fontSize: '0.78rem',
                              background: `${LANG_COLORS[s.language] || '#666'}22`,
                              color: LANG_COLORS[s.language] || 'var(--text-secondary)' }}>
                              {s.language}
                            </span>
                          </td>
                          <td style={{ padding: '0.75rem 1rem' }}>
                            <span style={{ color: statusColor(s.status), fontWeight: 600, fontSize: '0.82rem' }}>{s.status}</span>
                          </td>
                          <td style={{ padding: '0.75rem 1rem', color: 'var(--text-muted)', fontSize: '0.78rem' }}>
                            {s.submittedAt ? new Date(s.submittedAt).toLocaleString() : '—'}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            )
          )}

          {/* ── USERS TAB ──────────────────────────────────────────────── */}
          {!loading && tab === 'users' && (
            <div className="animate-fade-in">
              <h2 style={{ fontSize: '1.5rem', marginBottom: '1.5rem' }}>
                Registered Users <span style={{ fontSize: '1rem', color: 'var(--text-secondary)', fontWeight: 400 }}>({users.length})</span>
              </h2>
              <div className="glass-panel" style={{ overflow: 'hidden' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                  <thead>
                    <tr style={{ background: 'rgba(0,0,0,0.2)', fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                      {['#', 'Username', 'Points', 'Role', 'Actions'].map(h => (
                        <th key={h} style={{ padding: '0.875rem 1.25rem', textAlign: 'left', fontWeight: 600 }}>{h}</th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {users.map((u, i) => (
                      <tr key={u.id} style={{ borderBottom: '1px solid var(--border-color)', transition: 'background 0.15s' }}
                        onMouseOver={e => e.currentTarget.style.background = 'rgba(255,255,255,0.02)'}
                        onMouseOut={e => e.currentTarget.style.background = 'transparent'}>
                        <td style={{ padding: '0.875rem 1.25rem', color: 'var(--text-muted)', fontSize: '0.85rem' }}>{i + 1}</td>
                        <td style={{ padding: '0.875rem 1.25rem' }}>
                          <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                            <div style={{ width: 30, height: 30, borderRadius: '50%', background: 'var(--brand-gradient)',
                              display: 'flex', alignItems: 'center', justifyContent: 'center',
                              fontSize: '0.8rem', fontWeight: 700, color: 'white', flexShrink: 0 }}>
                              {u.username.charAt(0).toUpperCase()}
                            </div>
                            <span style={{ fontWeight: 500 }}>{u.username}</span>
                          </div>
                        </td>
                        <td style={{ padding: '0.875rem 1.25rem', color: 'var(--brand-primary)', fontWeight: 600 }}>
                          {u.points.toLocaleString()} pts
                        </td>
                        <td style={{ padding: '0.875rem 1.25rem' }}>
                          <span style={{ padding: '2px 8px', borderRadius: 4, fontSize: '0.75rem', fontWeight: 600,
                            background: 'rgba(16,185,129,0.1)', color: 'var(--success)' }}>
                            {u.role}
                          </span>
                        </td>
                        <td style={{ padding: '0.875rem 1.25rem' }}>
                          <button onClick={() => setDeleteConfirm(u)}
                            style={{ background: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.2)',
                              color: 'var(--error)', padding: '0.3rem 0.75rem', borderRadius: 6,
                              cursor: 'pointer', fontSize: '0.8rem', display: 'flex', alignItems: 'center', gap: 5 }}>
                            <Trash2 size={13} /> Delete
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* ── SUBMISSIONS TAB ──────────────────────────────────────────── */}
          {!loading && tab === 'submissions' && (
            <div className="animate-fade-in">
              <h2 style={{ fontSize: '1.5rem', marginBottom: '1.5rem' }}>
                All Submissions <span style={{ fontSize: '1rem', color: 'var(--text-secondary)', fontWeight: 400 }}>({submissions.length})</span>
              </h2>
              <div className="glass-panel" style={{ overflow: 'hidden' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                  <thead>
                    <tr style={{ background: 'rgba(0,0,0,0.2)', fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                      {['ID', 'User', 'Problem', 'Language', 'Status', 'Submitted'].map(h => (
                        <th key={h} style={{ padding: '0.875rem 1.25rem', textAlign: 'left', fontWeight: 600 }}>{h}</th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {submissions.slice(0, 100).map(s => (
                      <tr key={s.id} style={{ borderBottom: '1px solid var(--border-color)', fontSize: '0.85rem' }}
                        onMouseOver={e => e.currentTarget.style.background = 'rgba(255,255,255,0.02)'}
                        onMouseOut={e => e.currentTarget.style.background = 'transparent'}>
                        <td style={{ padding: '0.75rem 1.25rem', color: 'var(--text-muted)' }}>#{s.id}</td>
                        <td style={{ padding: '0.75rem 1.25rem', fontWeight: 500 }}>User #{s.userId}</td>
                        <td style={{ padding: '0.75rem 1.25rem', color: 'var(--text-secondary)' }}>Problem #{s.problemId}</td>
                        <td style={{ padding: '0.75rem 1.25rem' }}>
                          <span style={{ padding: '2px 8px', borderRadius: 4, fontSize: '0.78rem',
                            background: `${LANG_COLORS[s.language] || '#666'}22`,
                            color: LANG_COLORS[s.language] || 'var(--text-secondary)' }}>
                            {s.language}
                          </span>
                        </td>
                        <td style={{ padding: '0.75rem 1.25rem' }}>
                          <span style={{ color: statusColor(s.status), fontWeight: 600, fontSize: '0.82rem' }}>{s.status}</span>
                        </td>
                        <td style={{ padding: '0.75rem 1.25rem', color: 'var(--text-muted)', fontSize: '0.78rem' }}>
                          {s.submittedAt ? new Date(s.submittedAt).toLocaleString() : '—'}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
                {submissions.length > 100 && (
                  <div style={{ padding: '0.75rem 1.25rem', color: 'var(--text-muted)', fontSize: '0.82rem', textAlign: 'center' }}>
                    Showing latest 100 of {submissions.length} submissions
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Delete Confirm Modal */}
      {deleteConfirm && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.7)', backdropFilter: 'blur(8px)',
          zIndex: 1000, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <div className="glass-panel animate-fade-in" style={{ width: 380, padding: '2rem', textAlign: 'center' }}>
            <Trash2 size={36} color="var(--error)" style={{ margin: '0 auto 1rem' }} />
            <h3 style={{ fontSize: '1.25rem', marginBottom: 8 }}>Delete User</h3>
            <p style={{ color: 'var(--text-secondary)', marginBottom: '1.5rem', fontSize: '0.9rem' }}>
              Delete <strong>{deleteConfirm.username}</strong>? This will also remove all their submissions. This cannot be undone.
            </p>
            <div style={{ display: 'flex', gap: 10 }}>
              <button className="btn btn-secondary" style={{ flex: 1 }} onClick={() => setDeleteConfirm(null)}>Cancel</button>
              <button onClick={() => handleDelete(deleteConfirm.id)}
                style={{ flex: 1, padding: '0.75rem', borderRadius: 8, border: 'none', cursor: 'pointer',
                  background: 'var(--error)', color: 'white', fontWeight: 600 }}>
                Delete
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminDashboard;
