import { useState, useEffect } from 'react';
import axios from 'axios';
import Editor from '@monaco-editor/react';
import { useTheme } from '../contexts/ThemeContext';
import { Code, Share2, ThumbsUp, ThumbsDown, Eye, Plus, Search, X } from 'lucide-react';

export default function SnippetsPage({ currentUser }) {
  const { theme } = useTheme();
  const isDark = theme === 'dark';
  const [snippets, setSnippets] = useState([]);
  const [view, setView] = useState('my'); // 'my' or 'public'
  const [showCreate, setShowCreate] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  
  const [newSnippet, setNewSnippet] = useState({
    title: '',
    code: '',
    language: 'javascript',
    description: '',
    tags: '',
    isPublic: false
  });

  useEffect(() => {
    loadSnippets();
  }, [view, currentUser]);

  const loadSnippets = async () => {
    try {
      if (view === 'my' && currentUser) {
        const res = await axios.get(`/api/snippets/user/${currentUser.id}`);
        setSnippets(res.data);
      } else {
        const res = await axios.get('/api/snippets/public');
        setSnippets(res.data);
      }
    } catch (err) {
      console.error(err);
    }
  };

  const handleCreate = async () => {
    if (!currentUser) return;
    try {
      await axios.post('/api/snippets', { ...newSnippet, userId: currentUser.id });
      setShowCreate(false);
      setNewSnippet({ title: '', code: '', language: 'javascript', description: '', tags: '', isPublic: false });
      loadSnippets();
    } catch (err) {
      console.error(err);
    }
  };

  const handleVote = async (id, type) => {
    try {
      await axios.post(`/api/snippets/${id}/${type}`);
      loadSnippets();
    } catch (err) {
      console.error(err);
    }
  };

  const copyShareLink = (token) => {
    const url = `${window.location.origin}/snippet/${token}`;
    navigator.clipboard.writeText(url);
    alert('Share link copied!');
  };

  return (
    <div style={{ padding: '2rem', minHeight: '100vh', background: isDark ? '#0a0a0f' : '#f5f5f5' }}>
      <div style={{ maxWidth: '1400px', margin: '0 auto' }}>
        
        {/* Header */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
          <div>
            <h1 style={{ fontSize: '2rem', fontWeight: '700', margin: 0, marginBottom: '0.5rem' }}>
              📚 Code Snippets
            </h1>
            <p style={{ color: isDark ? '#a0a0b0' : '#666', margin: 0 }}>
              Save, share, and discover code snippets
            </p>
          </div>
          {currentUser && (
            <button
              onClick={() => setShowCreate(true)}
              style={{
                padding: '0.75rem 1.5rem',
                borderRadius: '8px',
                border: 'none',
                background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                color: '#fff',
                fontWeight: '600',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                gap: '0.5rem'
              }}
            >
              <Plus size={18} /> New Snippet
            </button>
          )}
        </div>

        {/* Tabs */}
        <div style={{ display: 'flex', gap: '1rem', marginBottom: '2rem' }}>
          {currentUser && (
            <button
              onClick={() => setView('my')}
              style={{
                padding: '0.5rem 1.5rem',
                borderRadius: '8px',
                border: 'none',
                background: view === 'my' ? 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' : (isDark ? 'rgba(255,255,255,0.05)' : 'rgba(0,0,0,0.05)'),
                color: view === 'my' ? '#fff' : (isDark ? '#e0e0e0' : '#333'),
                fontWeight: '600',
                cursor: 'pointer'
              }}
            >
              My Snippets
            </button>
          )}
          <button
            onClick={() => setView('public')}
            style={{
              padding: '0.5rem 1.5rem',
              borderRadius: '8px',
              border: 'none',
              background: view === 'public' ? 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' : (isDark ? 'rgba(255,255,255,0.05)' : 'rgba(0,0,0,0.05)'),
              color: view === 'public' ? '#fff' : (isDark ? '#e0e0e0' : '#333'),
              fontWeight: '600',
              cursor: 'pointer'
            }}
          >
            Public Library
          </button>
        </div>

        {/* Snippets Grid */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(350px, 1fr))', gap: '1.5rem' }}>
          {snippets.map(snippet => (
            <div
              key={snippet.id}
              style={{
                background: isDark ? 'rgba(20, 20, 30, 0.6)' : 'rgba(255, 255, 255, 0.8)',
                backdropFilter: 'blur(10px)',
                borderRadius: '12px',
                padding: '1.5rem',
                border: `1px solid ${isDark ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.1)'}`,
                transition: 'transform 0.2s',
                cursor: 'pointer'
              }}
              onMouseEnter={e => e.currentTarget.style.transform = 'translateY(-4px)'}
              onMouseLeave={e => e.currentTarget.style.transform = 'translateY(0)'}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1rem' }}>
                <div>
                  <h3 style={{ fontSize: '1.1rem', fontWeight: '600', margin: 0, marginBottom: '0.25rem' }}>
                    {snippet.title}
                  </h3>
                  <span style={{
                    fontSize: '0.75rem',
                    padding: '0.25rem 0.5rem',
                    borderRadius: '4px',
                    background: isDark ? 'rgba(102, 126, 234, 0.2)' : 'rgba(102, 126, 234, 0.1)',
                    color: '#667eea'
                  }}>
                    {snippet.language}
                  </span>
                </div>
                <button
                  onClick={() => copyShareLink(snippet.shareToken)}
                  style={{
                    padding: '0.5rem',
                    borderRadius: '6px',
                    border: 'none',
                    background: 'transparent',
                    cursor: 'pointer',
                    color: isDark ? '#a0a0b0' : '#666'
                  }}
                >
                  <Share2 size={16} />
                </button>
              </div>

              <p style={{ fontSize: '0.9rem', color: isDark ? '#a0a0b0' : '#666', marginBottom: '1rem' }}>
                {snippet.description || 'No description'}
              </p>

              <div style={{
                background: isDark ? 'rgba(0,0,0,0.3)' : '#f9f9f9',
                borderRadius: '6px',
                padding: '0.75rem',
                marginBottom: '1rem',
                fontFamily: 'monospace',
                fontSize: '0.85rem',
                maxHeight: '100px',
                overflow: 'hidden'
              }}>
                {snippet.code.substring(0, 150)}...
              </div>

              <div style={{ display: 'flex', gap: '1rem', fontSize: '0.85rem', color: isDark ? '#a0a0b0' : '#666' }}>
                <span style={{ display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
                  <Eye size={14} /> {snippet.views}
                </span>
                <button
                  onClick={() => handleVote(snippet.id, 'upvote')}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '0.25rem',
                    background: 'none',
                    border: 'none',
                    cursor: 'pointer',
                    color: 'inherit'
                  }}
                >
                  <ThumbsUp size={14} /> {snippet.upvotes}
                </button>
                <button
                  onClick={() => handleVote(snippet.id, 'downvote')}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '0.25rem',
                    background: 'none',
                    border: 'none',
                    cursor: 'pointer',
                    color: 'inherit'
                  }}
                >
                  <ThumbsDown size={14} /> {snippet.downvotes}
                </button>
              </div>
            </div>
          ))}
        </div>

        {/* Create Modal */}
        {showCreate && (
          <div style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            background: 'rgba(0,0,0,0.7)',
            backdropFilter: 'blur(8px)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 1000
          }}>
            <div style={{
              background: isDark ? '#1a1a2e' : '#fff',
              borderRadius: '16px',
              padding: '2rem',
              maxWidth: '800px',
              width: '90%',
              maxHeight: '90vh',
              overflow: 'auto'
            }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
                <h2 style={{ fontSize: '1.5rem', fontWeight: '700', margin: 0 }}>Create Snippet</h2>
                <button onClick={() => setShowCreate(false)} style={{ background: 'none', border: 'none', cursor: 'pointer' }}>
                  <X size={24} />
                </button>
              </div>

              <input
                placeholder="Snippet Title"
                value={newSnippet.title}
                onChange={e => setNewSnippet({ ...newSnippet, title: e.target.value })}
                style={{
                  width: '100%',
                  padding: '0.75rem',
                  borderRadius: '8px',
                  border: `1px solid ${isDark ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.1)'}`,
                  background: isDark ? 'rgba(0,0,0,0.3)' : '#fff',
                  color: isDark ? '#e0e0e0' : '#333',
                  marginBottom: '1rem'
                }}
              />

              <select
                value={newSnippet.language}
                onChange={e => setNewSnippet({ ...newSnippet, language: e.target.value })}
                style={{
                  width: '100%',
                  padding: '0.75rem',
                  borderRadius: '8px',
                  border: `1px solid ${isDark ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.1)'}`,
                  background: isDark ? 'rgba(0,0,0,0.3)' : '#fff',
                  color: isDark ? '#e0e0e0' : '#333',
                  marginBottom: '1rem'
                }}
              >
                <option value="javascript">JavaScript</option>
                <option value="python">Python</option>
                <option value="java">Java</option>
                <option value="c">C</option>
                <option value="cpp">C++</option>
              </select>

              <div style={{ marginBottom: '1rem', border: `1px solid ${isDark ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.1)'}`, borderRadius: '8px', overflow: 'hidden' }}>
                <Editor
                  height="300px"
                  language={newSnippet.language}
                  value={newSnippet.code}
                  onChange={val => setNewSnippet({ ...newSnippet, code: val || '' })}
                  theme={isDark ? 'vs-dark' : 'light'}
                  options={{ minimap: { enabled: false }, fontSize: 14 }}
                />
              </div>

              <textarea
                placeholder="Description"
                value={newSnippet.description}
                onChange={e => setNewSnippet({ ...newSnippet, description: e.target.value })}
                style={{
                  width: '100%',
                  padding: '0.75rem',
                  borderRadius: '8px',
                  border: `1px solid ${isDark ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.1)'}`,
                  background: isDark ? 'rgba(0,0,0,0.3)' : '#fff',
                  color: isDark ? '#e0e0e0' : '#333',
                  marginBottom: '1rem',
                  minHeight: '80px',
                  fontFamily: 'inherit'
                }}
              />

              <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.5rem', cursor: 'pointer' }}>
                <input
                  type="checkbox"
                  checked={newSnippet.isPublic}
                  onChange={e => setNewSnippet({ ...newSnippet, isPublic: e.target.checked })}
                />
                <span>Make this snippet public</span>
              </label>

              <button
                onClick={handleCreate}
                style={{
                  width: '100%',
                  padding: '0.75rem',
                  borderRadius: '8px',
                  border: 'none',
                  background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                  color: '#fff',
                  fontWeight: '600',
                  cursor: 'pointer'
                }}
              >
                Create Snippet
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
