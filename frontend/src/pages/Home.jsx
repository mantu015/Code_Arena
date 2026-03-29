import React from 'react';
import { Link } from 'react-router-dom';
import { Terminal, Code, Award, Zap, Flame, User } from 'lucide-react';

const Home = ({ currentUser }) => {
  return (
    <div className="main-container animate-fade-in" style={{ paddingTop: '5rem', paddingBottom: '5rem', textAlign: 'center' }}>
      <div style={{ maxWidth: '820px', margin: '0 auto' }}>
        <div style={{ display: 'inline-flex', alignItems: 'center', gap: 8, padding: '6px 16px',
          background: 'rgba(99,102,241,0.1)', border: '1px solid rgba(99,102,241,0.2)',
          borderRadius: 999, color: 'var(--brand-primary)', fontWeight: 600, marginBottom: '2rem' }}>
          <Zap size={15} fill="currentColor" /> Code Arena Beta
        </div>

        <h1 style={{ fontSize: '4rem', lineHeight: 1.1, marginBottom: '1.5rem', letterSpacing: '-0.02em' }}>
          Master Algorithms.<br />
          <span style={{ background: 'var(--brand-gradient)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
            Dominate the Arena.
          </span>
        </h1>

        <p style={{ fontSize: '1.2rem', color: 'var(--text-secondary)', marginBottom: '3rem', lineHeight: 1.7 }}>
          A premium coding platform to practice data structures, solve complex algorithmic challenges,
          and climb the global leaderboard.
        </p>

        {currentUser ? (
          <div style={{ display: 'flex', justifyContent: 'center', gap: 12, flexWrap: 'wrap' }}>
            <Link to="/problems" className="btn btn-primary" style={{ fontSize: '1rem', padding: '0.875rem 2rem' }}>
              <Terminal size={18} /> Start Coding
            </Link>
            <Link to="/daily" className="btn btn-secondary" style={{ fontSize: '1rem', padding: '0.875rem 2rem',
              borderColor: 'rgba(239,68,68,0.4)', color: 'var(--error)' }}>
              <Flame size={18} /> Daily Challenge
            </Link>
            <Link to="/leaderboard" className="btn btn-secondary" style={{ fontSize: '1rem', padding: '0.875rem 2rem' }}>
              <Award size={18} /> Leaderboard
            </Link>
          </div>
        ) : (
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.95rem' }}>
            Sign in or create an account to unlock the full platform.
          </p>
        )}
      </div>

      {/* Feature Cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))',
        gap: '1.5rem', marginTop: '6rem', textAlign: 'left' }}>
        {[
          { icon: <Code size={30} color="var(--brand-primary)" />,  title: 'Real Execution Engine',  desc: 'JavaScript code is actually executed against test cases. See per-test-case pass/fail with real output diff.' },
          { icon: <Terminal size={30} color="var(--success)" />,    title: 'In-Browser IDE',          desc: 'Monaco Editor with syntax highlighting, multi-language support, and instant code reset.' },
          { icon: <Flame size={30} color="var(--error)" />,         title: 'Daily Challenge',         desc: 'A new problem every day with +50 bonus points. Solve it before midnight to claim your reward.' },
          { icon: <Award size={30} color="var(--warning)" />,       title: 'Global Leaderboard',      desc: 'Earn points for solving challenges. Climb the ranks and earn badges from Newcomer to Grandmaster.' },
          { icon: <User size={30} color="var(--brand-secondary)" />,title: 'Profile & Stats',         desc: 'Track your acceptance rate, solved count by difficulty, rank, and full submission history.' },
          { icon: <Zap size={30} color="var(--brand-primary)" />,   title: 'Submission History',      desc: 'Every submission is saved. Review past attempts per problem directly from the workspace.' },
        ].map((feat, idx) => (
          <div key={idx} className="glass-panel"
            style={{ padding: '1.75rem', transition: 'transform 0.3s ease, box-shadow 0.3s ease' }}
            onMouseOver={e => { e.currentTarget.style.transform = 'translateY(-5px)'; e.currentTarget.style.boxShadow = '0 16px 40px rgba(0,0,0,0.4)'; }}
            onMouseOut={e => { e.currentTarget.style.transform = 'translateY(0)'; e.currentTarget.style.boxShadow = ''; }}>
            <div style={{ marginBottom: '1.25rem', background: 'rgba(255,255,255,0.03)',
              width: 56, height: 56, display: 'flex', alignItems: 'center', justifyContent: 'center', borderRadius: 12 }}>
              {feat.icon}
            </div>
            <h3 style={{ fontSize: '1.15rem', marginBottom: '0.75rem' }}>{feat.title}</h3>
            <p style={{ color: 'var(--text-secondary)', lineHeight: 1.65, fontSize: '0.92rem' }}>{feat.desc}</p>
          </div>
        ))}
      </div>
    </div>
  );
};

export default Home;
