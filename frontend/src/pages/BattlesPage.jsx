import { useState, useEffect } from 'react';
import axios from 'axios';
import { useTheme } from '../contexts/ThemeContext';
import { Swords, Users, Clock, Trophy } from 'lucide-react';

export default function BattlesPage({ currentUser }) {
  const { theme } = useTheme();
  const isDark = theme === 'dark';
  const [battles, setBattles] = useState([]);
  const [myBattles, setMyBattles] = useState([]);
  const [showCreate, setShowCreate] = useState(false);

  useEffect(() => {
    loadBattles();
  }, [currentUser]);

  const loadBattles = async () => {
    try {
      const [available, mine] = await Promise.all([
        axios.get('/api/battles/available'),
        currentUser ? axios.get(`/api/battles/user/${currentUser.id}`) : Promise.resolve({ data: [] })
      ]);
      setBattles(available.data);
      setMyBattles(mine.data);
    } catch (err) {
      console.error(err);
    }
  };

  const createBattle = async (problemId) => {
    if (!currentUser) return;
    try {
      const res = await axios.post('/api/battles/create', { userId: currentUser.id, problemId });
      alert(`Battle created! Code: ${res.data.battleCode}`);
      loadBattles();
    } catch (err) {
      console.error(err);
    }
  };

  const joinBattle = async (battleCode) => {
    if (!currentUser) return;
    try {
      await axios.post('/api/battles/join', { battleCode, userId: currentUser.id });
      alert('Joined battle!');
      loadBattles();
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <div style={{ padding: '2rem', minHeight: '100vh', background: isDark ? '#0a0a0f' : '#f5f5f5' }}>
      <div style={{ maxWidth: '1200px', margin: '0 auto' }}>
        <div style={{ marginBottom: '2rem' }}>
          <h1 style={{ fontSize: '2rem', fontWeight: '700', margin: 0, marginBottom: '0.5rem' }}>
            ⚔️ Code Battles
          </h1>
          <p style={{ color: isDark ? '#a0a0b0' : '#666', margin: 0 }}>
            Challenge others in real-time coding duels
          </p>
        </div>

        <div style={{ display: 'grid', gap: '1.5rem' }}>
          <div style={{
            background: isDark ? 'rgba(20, 20, 30, 0.6)' : 'rgba(255, 255, 255, 0.8)',
            backdropFilter: 'blur(10px)',
            borderRadius: '12px',
            padding: '1.5rem',
            border: `1px solid ${isDark ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.1)'}`
          }}>
            <h2 style={{ fontSize: '1.3rem', marginBottom: '1rem' }}>Available Battles</h2>
            {battles.length === 0 ? (
              <p style={{ color: isDark ? '#a0a0b0' : '#666' }}>No battles available. Create one!</p>
            ) : (
              battles.map(battle => (
                <div key={battle.id} style={{
                  padding: '1rem',
                  background: isDark ? 'rgba(0,0,0,0.3)' : '#f9f9f9',
                  borderRadius: '8px',
                  marginBottom: '0.5rem',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center'
                }}>
                  <div>
                    <strong>Battle #{battle.battleCode}</strong>
                    <p style={{ margin: 0, fontSize: '0.9rem', color: isDark ? '#a0a0b0' : '#666' }}>
                      Problem ID: {battle.problemId}
                    </p>
                  </div>
                  <button
                    onClick={() => joinBattle(battle.battleCode)}
                    style={{
                      padding: '0.5rem 1rem',
                      borderRadius: '6px',
                      border: 'none',
                      background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                      color: '#fff',
                      fontWeight: '600',
                      cursor: 'pointer'
                    }}
                  >
                    Join Battle
                  </button>
                </div>
              ))
            )}
          </div>

          {currentUser && (
            <div style={{
              background: isDark ? 'rgba(20, 20, 30, 0.6)' : 'rgba(255, 255, 255, 0.8)',
              backdropFilter: 'blur(10px)',
              borderRadius: '12px',
              padding: '1.5rem',
              border: `1px solid ${isDark ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.1)'}`
            }}>
              <h2 style={{ fontSize: '1.3rem', marginBottom: '1rem' }}>My Battles</h2>
              {myBattles.map(battle => (
                <div key={battle.id} style={{
                  padding: '1rem',
                  background: isDark ? 'rgba(0,0,0,0.3)' : '#f9f9f9',
                  borderRadius: '8px',
                  marginBottom: '0.5rem'
                }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <span><strong>Battle #{battle.battleCode}</strong></span>
                    <span style={{
                      padding: '0.25rem 0.5rem',
                      borderRadius: '4px',
                      fontSize: '0.75rem',
                      background: battle.status === 'COMPLETED' ? '#10b981' : '#f59e0b',
                      color: '#fff'
                    }}>
                      {battle.status}
                    </span>
                  </div>
                  {battle.winnerId && (
                    <p style={{ margin: '0.5rem 0 0 0', fontSize: '0.9rem' }}>
                      Winner: {battle.winnerId === currentUser.id ? 'You! 🎉' : 'Opponent'}
                    </p>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
