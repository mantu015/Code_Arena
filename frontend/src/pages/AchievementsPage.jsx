import { useState, useEffect } from 'react';
import axios from 'axios';
import { useTheme } from '../contexts/ThemeContext';
import { Trophy, Lock, Star, Zap, Globe, Calendar } from 'lucide-react';

const CATEGORY_ICONS = {
  MILESTONE: Trophy,
  SPEED: Zap,
  LANGUAGE: Globe,
  STREAK: Calendar
};

const RARITY_COLORS = {
  COMMON: '#9CA3AF',
  RARE: '#3B82F6',
  EPIC: '#A855F7',
  LEGENDARY: '#F59E0B'
};

export default function AchievementsPage({ currentUser }) {
  const { theme } = useTheme();
  const isDark = theme === 'dark';
  const [achievements, setAchievements] = useState([]);
  const [userAchievements, setUserAchievements] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadAchievements();
  }, [currentUser]);

  const loadAchievements = async () => {
    try {
      const [allRes, userRes] = await Promise.all([
        axios.get('/api/achievements'),
        currentUser ? axios.get(`/api/achievements/user/${currentUser.id}`) : Promise.resolve({ data: [] })
      ]);
      setAchievements(allRes.data);
      setUserAchievements(userRes.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const isUnlocked = (achievementId) => {
    return userAchievements.some(ua => ua.id === achievementId);
  };

  const totalPoints = userAchievements.reduce((sum, ua) => sum + (ua.points || 0), 0);

  if (loading) return <div style={{ padding: '2rem', textAlign: 'center' }}>Loading...</div>;

  return (
    <div style={{ padding: '2rem', minHeight: '100vh', background: isDark ? '#0a0a0f' : '#f5f5f5' }}>
      <div style={{ maxWidth: '1200px', margin: '0 auto' }}>
        
        {/* Header */}
        <div style={{
          background: isDark ? 'rgba(20, 20, 30, 0.6)' : 'rgba(255, 255, 255, 0.8)',
          backdropFilter: 'blur(10px)',
          borderRadius: '16px',
          padding: '2rem',
          marginBottom: '2rem',
          border: `1px solid ${isDark ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.1)'}`
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '1rem' }}>
            <Trophy size={40} color="#F59E0B" />
            <div>
              <h1 style={{ fontSize: '2rem', fontWeight: '700', margin: 0 }}>Achievements</h1>
              <p style={{ color: isDark ? '#a0a0b0' : '#666', margin: 0 }}>
                {userAchievements.length} / {achievements.length} Unlocked • {totalPoints} Points
              </p>
            </div>
          </div>
          
          <div style={{
            width: '100%',
            height: '8px',
            background: isDark ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.1)',
            borderRadius: '4px',
            overflow: 'hidden'
          }}>
            <div style={{
              width: `${(userAchievements.length / achievements.length) * 100}%`,
              height: '100%',
              background: 'linear-gradient(90deg, #667eea 0%, #764ba2 100%)',
              transition: 'width 0.3s'
            }} />
          </div>
        </div>

        {/* Achievements Grid */}
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))',
          gap: '1.5rem'
        }}>
          {achievements.map(achievement => {
            const unlocked = isUnlocked(achievement.id);
            const Icon = CATEGORY_ICONS[achievement.category] || Star;
            const rarityColor = RARITY_COLORS[achievement.rarity] || '#9CA3AF';

            return (
              <div
                key={achievement.id}
                style={{
                  background: isDark ? 'rgba(20, 20, 30, 0.6)' : 'rgba(255, 255, 255, 0.8)',
                  backdropFilter: 'blur(10px)',
                  borderRadius: '12px',
                  padding: '1.5rem',
                  border: `2px solid ${unlocked ? rarityColor : (isDark ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.1)')}`,
                  opacity: unlocked ? 1 : 0.6,
                  transition: 'all 0.3s',
                  cursor: 'pointer',
                  position: 'relative',
                  overflow: 'hidden'
                }}
              >
                {unlocked && (
                  <div style={{
                    position: 'absolute',
                    top: 0,
                    left: 0,
                    right: 0,
                    height: '4px',
                    background: `linear-gradient(90deg, ${rarityColor}, transparent)`
                  }} />
                )}

                <div style={{ display: 'flex', alignItems: 'flex-start', gap: '1rem', marginBottom: '1rem' }}>
                  <div style={{
                    fontSize: '3rem',
                    filter: unlocked ? 'none' : 'grayscale(100%)'
                  }}>
                    {achievement.icon}
                  </div>
                  <div style={{ flex: 1 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
                      <h3 style={{ fontSize: '1.1rem', fontWeight: '600', margin: 0 }}>
                        {achievement.name}
                      </h3>
                      {!unlocked && <Lock size={14} color={isDark ? '#666' : '#999'} />}
                    </div>
                    <p style={{
                      fontSize: '0.85rem',
                      color: isDark ? '#a0a0b0' : '#666',
                      margin: 0
                    }}>
                      {achievement.description}
                    </p>
                  </div>
                </div>

                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span style={{
                    fontSize: '0.75rem',
                    padding: '0.25rem 0.5rem',
                    borderRadius: '4px',
                    background: rarityColor + '20',
                    color: rarityColor,
                    fontWeight: '600'
                  }}>
                    {achievement.rarity}
                  </span>
                  <span style={{
                    fontSize: '0.9rem',
                    fontWeight: '600',
                    color: '#F59E0B'
                  }}>
                    +{achievement.points} pts
                  </span>
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}
