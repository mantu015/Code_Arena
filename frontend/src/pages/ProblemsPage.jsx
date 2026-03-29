import React, { useState, useEffect, useMemo } from 'react';
import { Link } from 'react-router-dom';
import axios from 'axios';
import API_BASE_URL from '../apiConfig';
import { Play, CheckCircle, FileCode2, Search } from 'lucide-react';

const MOCK_PROBLEMS = [
  { id: 1, title: 'Two Sum',                                       difficulty: 'Easy'   },
  { id: 2, title: 'Add Two Numbers',                               difficulty: 'Medium' },
  { id: 3, title: 'Longest Substring Without Repeating Characters',difficulty: 'Medium' },
  { id: 4, title: 'Median of Two Sorted Arrays',                   difficulty: 'Hard'   },
  { id: 5, title: '3Sum',                                          difficulty: 'Medium' },
  { id: 6, title: 'Regular Expression Matching',                   difficulty: 'Hard'   },
];

const FILTERS = ['All', 'Easy', 'Medium', 'Hard'];

const ProblemsPage = ({ currentUser }) => {
  const [problems, setProblems]     = useState([]);
  const [loading, setLoading]       = useState(true);
  const [solvedCount, setSolvedCount] = useState(0);
  const [userPoints, setUserPoints]   = useState(0);
  const [search, setSearch]           = useState('');
  const [filter, setFilter]           = useState('All');

  useEffect(() => {
    const fetchData = async () => {
      try {
        const res = await axios.get(`${API_BASE_URL}/api/problems`);
        setProblems(res.data?.length > 0 ? res.data : MOCK_PROBLEMS);
      } catch {
        setProblems(MOCK_PROBLEMS);
      }

      if (currentUser?.id) {
        try {
          const [solvedRes, userRes] = await Promise.all([
            axios.get(`${API_BASE_URL}/api/submissions/user/${currentUser.id}/solved`),
            axios.get(`${API_BASE_URL}/api/users/${currentUser.id}`),
          ]);
          setSolvedCount(solvedRes.data);
          setUserPoints(userRes.data.points ?? 0);
        } catch { /* leave defaults */ }
      }
      setLoading(false);
    };
    fetchData();
  }, [currentUser]);

  const filtered = useMemo(() => {
    return problems.filter(p => {
      const matchDiff = filter === 'All' || p.difficulty === filter;
      const matchSearch = p.title.toLowerCase().includes(search.toLowerCase());
      return matchDiff && matchSearch;
    });
  }, [problems, filter, search]);

  const counts = useMemo(() => ({
    All:    problems.length,
    Easy:   problems.filter(p => p.difficulty === 'Easy').length,
    Medium: problems.filter(p => p.difficulty === 'Medium').length,
    Hard:   problems.filter(p => p.difficulty === 'Hard').length,
  }), [problems]);

  return (
    <div className="main-container animate-fade-in" style={{ paddingTop: '3rem', paddingBottom: '3rem' }}>

      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '2rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '2.5rem', marginBottom: '0.5rem' }}>Problem Set</h1>
          <p style={{ color: 'var(--text-secondary)' }}>Solve algorithmic challenges to improve your skills</p>
        </div>
        {currentUser && (
          <div style={{ background: 'rgba(255,255,255,0.05)', padding: '0.75rem 1.25rem',
            borderRadius: 10, display: 'flex', gap: '1.25rem', alignItems: 'center',
            border: '1px solid var(--border-color)' }}>
            <span style={{ color: 'var(--success)', display: 'flex', alignItems: 'center', gap: 6 }}>
              <CheckCircle size={15} /> {solvedCount} Solved
            </span>
            <span style={{ color: 'var(--warning)', display: 'flex', alignItems: 'center', gap: 6 }}>
              <FileCode2 size={15} /> {userPoints.toLocaleString()} pts
            </span>
          </div>
        )}
      </div>

      {/* Search + Filter Bar */}
      <div style={{ display: 'flex', gap: '1rem', marginBottom: '1.5rem', flexWrap: 'wrap', alignItems: 'center' }}>
        {/* Search */}
        <div style={{ position: 'relative', flex: 1, minWidth: 200 }}>
          <Search size={16} style={{ position: 'absolute', left: 12, top: '50%', transform: 'translateY(-50%)',
            color: 'var(--text-muted)', pointerEvents: 'none' }} />
          <input
            type="text"
            placeholder="Search problems..."
            value={search}
            onChange={e => setSearch(e.target.value)}
            className="input-field"
            style={{ paddingLeft: '2.5rem' }}
          />
        </div>

        {/* Difficulty Tabs */}
        <div style={{ display: 'flex', gap: 6, background: 'rgba(255,255,255,0.04)',
          padding: '4px', borderRadius: 10, border: '1px solid var(--border-color)' }}>
          {FILTERS.map(f => {
            const active = filter === f;
            const color = f === 'Easy' ? 'var(--success)' : f === 'Medium' ? 'var(--warning)' : f === 'Hard' ? 'var(--error)' : 'var(--brand-primary)';
            return (
              <button key={f} onClick={() => setFilter(f)}
                style={{ padding: '0.35rem 1rem', borderRadius: 7, border: 'none', cursor: 'pointer',
                  fontWeight: 600, fontSize: '0.82rem', transition: 'all 0.2s',
                  background: active ? (f === 'All' ? 'var(--brand-gradient)' : `${color}22`) : 'transparent',
                  color: active ? (f === 'All' ? 'white' : color) : 'var(--text-secondary)',
                  boxShadow: active ? `0 2px 8px ${color}33` : 'none' }}>
                {f} <span style={{ opacity: 0.7, fontSize: '0.75rem' }}>({counts[f]})</span>
              </button>
            );
          })}
        </div>
      </div>

      {/* Table */}
      <div className="glass-panel" style={{ overflow: 'hidden' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
          <thead>
            <tr style={{ borderBottom: '1px solid var(--border-color)', backgroundColor: 'rgba(0,0,0,0.2)' }}>
              <th style={{ padding: '1.1rem 1.5rem', width: 60, color: 'var(--text-secondary)', fontWeight: 600, fontSize: '0.85rem' }}>#</th>
              <th style={{ padding: '1.1rem 1.5rem', color: 'var(--text-secondary)', fontWeight: 600, fontSize: '0.85rem' }}>Title</th>
              <th style={{ padding: '1.1rem 1.5rem', width: 120, color: 'var(--text-secondary)', fontWeight: 600, fontSize: '0.85rem' }}>Difficulty</th>
              <th style={{ padding: '1.1rem 1.5rem', width: 100, textAlign: 'right', color: 'var(--text-secondary)', fontWeight: 600, fontSize: '0.85rem' }}>Action</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan="4" style={{ padding: '2.5rem', textAlign: 'center', color: 'var(--text-secondary)' }}>Loading problems...</td></tr>
            ) : filtered.length === 0 ? (
              <tr><td colSpan="4" style={{ padding: '2.5rem', textAlign: 'center', color: 'var(--text-secondary)' }}>
                No problems match "{search}" {filter !== 'All' ? `in ${filter}` : ''}.
              </td></tr>
            ) : filtered.map(problem => (
              <tr key={problem.id}
                style={{ borderBottom: '1px solid var(--border-color)', transition: 'background 0.15s' }}
                onMouseOver={e => e.currentTarget.style.backgroundColor = 'rgba(255,255,255,0.025)'}
                onMouseOut={e => e.currentTarget.style.backgroundColor = 'transparent'}>
                <td style={{ padding: '1.1rem 1.5rem', color: 'var(--text-muted)', fontSize: '0.9rem' }}>{problem.id}</td>
                <td style={{ padding: '1.1rem 1.5rem', fontWeight: 500 }}>
                  <Link to={`/problem/${problem.id}`}
                    style={{ transition: 'color 0.2s' }}
                    onMouseOver={e => e.currentTarget.style.color = 'var(--brand-primary)'}
                    onMouseOut={e => e.currentTarget.style.color = 'inherit'}>
                    {problem.title}
                  </Link>
                </td>
                <td style={{ padding: '1.1rem 1.5rem' }}>
                  <span className={`badge badge-${(problem.difficulty || 'easy').toLowerCase()}`}>
                    {problem.difficulty}
                  </span>
                </td>
                <td style={{ padding: '1.1rem 1.5rem', textAlign: 'right' }}>
                  <Link to={`/problem/${problem.id}`} className="btn btn-secondary"
                    style={{ padding: '0.35rem 0.9rem', fontSize: '0.82rem' }}>
                    <Play size={13} fill="currentColor" /> Solve
                  </Link>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default ProblemsPage;
