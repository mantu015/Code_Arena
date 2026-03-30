import { useState, useEffect } from 'react';
import axios from 'axios';
import { useTheme } from '../contexts/ThemeContext';
import { TrendingUp, Clock, Code, Target } from 'lucide-react';

export default function AnalyticsPage({ currentUser }) {
  const { theme } = useTheme();
  const isDark = theme === 'dark';
  const [analytics, setAnalytics] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (currentUser) {
      loadAnalytics();
    }
  }, [currentUser]);

  const loadAnalytics = async () => {
    try {
      const res = await axios.get(`/api/analytics/user/${currentUser.id}`);
      setAnalytics(res.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  if (!currentUser) {
    return (
      <div style={{ padding: '2rem', textAlign: 'center' }}>
        <p>Please log in to view analytics</p>
      </div>
    );
  }

  if (loading) return <div style={{ padding: '2rem', textAlign: 'center' }}>Loading analytics...</div>;

  return (
    <div style={{ padding: '2rem', minHeight: '100vh', background: isDark ? '#0a0a0f' : '#f5f5f5' }}>
      <div style={{ maxWidth: '1400px', margin: '0 auto' }}>
        <div style={{ marginBottom: '2rem' }}>
          <h1 style={{ fontSize: '2rem', fontWeight: '700', margin: 0, marginBottom: '0.5rem' }}>
            📊 Analytics Dashboard
          </h1>
          <p style={{ color: isDark ? '#a0a0b0' : '#666', margin: 0 }}>
            Track your coding journey and performance
          </p>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '1.5rem', marginBottom: '2rem' }}>
          {/* Stats Cards */}
          <div style={{
            background: isDark ? 'rgba(20, 20, 30, 0.6)' : 'rgba(255, 255, 255, 0.8)',
            backdropFilter: 'blur(10px)',
            borderRadius: '12px',
            padding: '1.5rem',
            border: `1px solid ${isDark ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.1)'}`
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '0.5rem' }}>
              <Clock size={24} color="#667eea" />
              <h3 style={{ margin: 0 }}>Avg Time</h3>
            </div>
            <p style={{ fontSize: '2rem', fontWeight: '700', margin: 0 }}>
              {Math.round(analytics?.averageTimeSeconds / 60 || 0)} min
            </p>
          </div>

          <div style={{
            background: isDark ? 'rgba(20, 20, 30, 0.6)' : 'rgba(255, 255, 255, 0.8)',
            backdropFilter: 'blur(10px)',
            borderRadius: '12px',
            padding: '1.5rem',
            border: `1px solid ${isDark ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.1)'}`
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '0.5rem' }}>
              <Code size={24} color="#10b981" />
              <h3 style={{ margin: 0 }}>Languages</h3>
            </div>
            <p style={{ fontSize: '2rem', fontWeight: '700', margin: 0 }}>
              {Object.keys(analytics?.languageProficiency || {}).length}
            </p>
          </div>
        </div>

        {/* Language Proficiency */}
        <div style={{
          background: isDark ? 'rgba(20, 20, 30, 0.6)' : 'rgba(255, 255, 255, 0.8)',
          backdropFilter: 'blur(10px)',
          borderRadius: '12px',
          padding: '1.5rem',
          border: `1px solid ${isDark ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.1)'}`,
          marginBottom: '1.5rem'
        }}>
          <h2 style={{ fontSize: '1.3rem', marginBottom: '1rem' }}>Language Proficiency</h2>
          {Object.entries(analytics?.languageProficiency || {}).map(([lang, count]) => (
            <div key={lang} style={{ marginBottom: '1rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                <span style={{ fontWeight: '600' }}>{lang}</span>
                <span>{count} problems</span>
              </div>
              <div style={{
                width: '100%',
                height: '8px',
                background: isDark ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.1)',
                borderRadius: '4px',
                overflow: 'hidden'
              }}>
                <div style={{
                  width: `${Math.min((count / 50) * 100, 100)}%`,
                  height: '100%',
                  background: 'linear-gradient(90deg, #667eea 0%, #764ba2 100%)'
                }} />
              </div>
            </div>
          ))}
        </div>

        {/* Heatmap */}
        <div style={{
          background: isDark ? 'rgba(20, 20, 30, 0.6)' : 'rgba(255, 255, 255, 0.8)',
          backdropFilter: 'blur(10px)',
          borderRadius: '12px',
          padding: '1.5rem',
          border: `1px solid ${isDark ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.1)'}`
        }}>
          <h2 style={{ fontSize: '1.3rem', marginBottom: '1rem' }}>Activity Heatmap</h2>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(12px, 1fr))', gap: '4px' }}>
            {Object.entries(analytics?.heatmap || {}).slice(0, 365).map(([date, count]) => (
              <div
                key={date}
                title={`${date}: ${count} submissions`}
                style={{
                  width: '12px',
                  height: '12px',
                  borderRadius: '2px',
                  background: count > 0 ? `rgba(102, 126, 234, ${Math.min(count / 5, 1)})` : (isDark ? 'rgba(255,255,255,0.05)' : 'rgba(0,0,0,0.05)')
                }}
              />
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
