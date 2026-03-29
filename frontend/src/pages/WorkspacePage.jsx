import React, { useState, useEffect, useCallback } from 'react';
import { useParams } from 'react-router-dom';
import Editor from '@monaco-editor/react';
import axios from 'axios';
import API_BASE_URL from '../apiConfig';
import {
  Play, CheckCircle, RefreshCcw, FileWarning, Terminal,
  History, X, ChevronDown, ChevronUp, Clock, Cpu
} from 'lucide-react';

const TEMPLATES = {
  '1': {
    javascript: `function twoSum(nums, target) {
    
}`,
    python: `def twoSum(nums, target):
    `,
    java: `class Solution {
    public int[] twoSum(int[] nums, int target) {
        
        return new int[]{};
    }
}`,
    cpp: `class Solution {
public:
    vector<int> twoSum(vector<int>& nums, int target) {
        return {};
    }
};`
  },
  '2': {
    javascript: `function addTwoNumbers(l1, l2) {
    
}`,
    python: `# ListNode is pre-defined: class ListNode: val, next
def addTwoNumbers(l1, l2):
    `,
    java: `class Solution {
    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        // ListNode is pre-defined in the runner
        
        return null;
    }
}`,
    cpp: `class Solution {
public:
    ListNode* addTwoNumbers(ListNode* l1, ListNode* l2) {
        return nullptr;
    }
};`
  },
  '3': {
    javascript: `function lengthOfLongestSubstring(s) {
    
}`,
    python: `def lengthOfLongestSubstring(s):
    `,
    java: `class Solution {
    public int lengthOfLongestSubstring(String s) {
        
        return 0;
    }
}`,
    cpp: `class Solution {
public:
    int lengthOfLongestSubstring(string s) {
        return 0;
    }
};`
  },
  '4': {
    javascript: `function findMedianSortedArrays(nums1, nums2) {
    
}`,
    python: `def findMedianSortedArrays(nums1, nums2):
    `,
    java: `class Solution {
    public double findMedianSortedArrays(int[] nums1, int[] nums2) {
        
        return 0.0;
    }
}`,
    cpp: `class Solution {
public:
    double findMedianSortedArrays(vector<int>& nums1, vector<int>& nums2) {
        return 0.0;
    }
};`
  },
  '5': {
    javascript: `function threeSum(nums) {
    
}`,
    python: `def threeSum(nums):
    `,
    java: `class Solution {
    public List<List<Integer>> threeSum(int[] nums) {
        // List, ArrayList, Arrays are available
        
        return new ArrayList<>();
    }
}`,
    cpp: `class Solution {
public:
    vector<vector<int>> threeSum(vector<int>& nums) {
        return {};
    }
};`
  },
  '6': {
    javascript: `function isMatch(s, p) {
    
}`,
    python: `def isMatch(s, p):
    `,
    java: `class Solution {
    public boolean isMatch(String s, String p) {
        
        return false;
    }
}`,
    cpp: `class Solution {
public:
    bool isMatch(string s, string p) {
        return false;
    }
};`
  },
};

const fallback = {
  javascript: `function solution(input) {\n    \n}`,
  python: `def solution(input):\n    pass`,
  java: `class Solution {\n    public Object solution(Object input) {\n        return null;\n    }\n}`,
  cpp: `class Solution {\npublic:\n    void solution() {}\n};`
};

const getTemplate = (id, lang) =>
  (TEMPLATES[String(id)] && TEMPLATES[String(id)][lang]) || fallback[lang];

const STATUS_COLOR = {
  'Accepted':      'var(--success)',
  'Wrong Answer':  'var(--error)',
  'Runtime Error': 'var(--error)',
  'Compile Error': 'var(--warning)',
  'Not Executed':  'var(--warning)',
};

const TCRow = ({ tc, index }) => {
  const [open, setOpen] = useState(false);
  const color = tc.passed ? 'var(--success)' : 'var(--error)';
  return (
    <div style={{ border: `1px solid ${color}22`, borderRadius: 8, marginBottom: 8, overflow: 'hidden' }}>
      <div
        onClick={() => setOpen(o => !o)}
        style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between',
          padding: '0.6rem 1rem', cursor: 'pointer',
          background: tc.passed ? 'rgba(16,185,129,0.06)' : 'rgba(239,68,68,0.06)' }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
          {tc.passed
            ? <CheckCircle size={15} color="var(--success)" />
            : <FileWarning size={15} color="var(--error)" />}
          <span style={{ fontWeight: 600, color }}>Test Case {tc.index}</span>
          <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>{tc.input}</span>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          {tc.runtimeMs > 0 && (
            <span style={{ fontSize: '0.78rem', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: 4 }}>
              <Clock size={12} /> {tc.runtimeMs}ms
            </span>
          )}
          {open ? <ChevronUp size={14} /> : <ChevronDown size={14} />}
        </div>
      </div>
      {open && (
        <div style={{ padding: '0.75rem 1rem', fontFamily: 'Fira Code, monospace', fontSize: '0.85rem',
          background: 'rgba(0,0,0,0.2)', display: 'flex', flexDirection: 'column', gap: 6 }}>
          <div><span style={{ color: 'var(--text-secondary)' }}>Input:    </span><span style={{ color: 'var(--text-primary)' }}>{tc.input}</span></div>
          <div><span style={{ color: 'var(--text-secondary)' }}>Expected: </span><span style={{ color: 'var(--success)' }}>{tc.expected}</span></div>
          <div><span style={{ color: 'var(--text-secondary)' }}>Got:      </span>
            <span style={{ color: tc.passed ? 'var(--success)' : 'var(--error)' }}>{tc.actual}</span>
          </div>
        </div>
      )}
    </div>
  );
};

const HistoryDrawer = ({ userId, problemId, onClose }) => {
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    axios.get(`${API_BASE_URL}/api/submissions/user/${userId}/problem/${problemId}`)
      .then(r => setHistory(r.data.reverse()))
      .catch(() => setHistory([]))
      .finally(() => setLoading(false));
  }, [userId, problemId]);

  const statusColor = s => STATUS_COLOR[s] || 'var(--text-secondary)';

  return (
    <div style={{ position: 'absolute', top: 0, right: 0, width: 380, height: '100%',
      background: 'var(--bg-secondary)', borderLeft: '1px solid var(--border-color)',
      zIndex: 50, display: 'flex', flexDirection: 'column', boxShadow: '-8px 0 32px rgba(0,0,0,0.4)' }}>
      <div style={{ padding: '1rem 1.25rem', borderBottom: '1px solid var(--border-color)',
        display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <span style={{ fontWeight: 700, fontSize: '1rem' }}>Submission History</span>
        <button onClick={onClose} style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-secondary)' }}>
          <X size={20} />
        </button>
      </div>
      <div style={{ flex: 1, overflowY: 'auto', padding: '1rem' }}>
        {loading ? (
          <div style={{ color: 'var(--text-secondary)', textAlign: 'center', marginTop: '2rem' }}>Loading...</div>
        ) : history.length === 0 ? (
          <div style={{ color: 'var(--text-secondary)', textAlign: 'center', marginTop: '2rem' }}>No submissions yet.</div>
        ) : history.map((s, i) => (
          <div key={s.id} style={{ marginBottom: 12, padding: '0.75rem 1rem',
            background: 'rgba(255,255,255,0.03)', borderRadius: 8,
            border: '1px solid var(--border-color)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 6 }}>
              <span style={{ fontWeight: 600, color: statusColor(s.status) }}>{s.status}</span>
              <span style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>
                {s.submittedAt ? new Date(s.submittedAt).toLocaleString() : ''}
              </span>
            </div>
            <div style={{ fontSize: '0.82rem', color: 'var(--text-secondary)' }}>
              Language: <span style={{ color: 'var(--brand-primary)' }}>{s.language}</span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

const WorkspacePage = ({ currentUser }) => {
  const { id } = useParams();
  const [code, setCode] = useState('');
  const [language, setLanguage] = useState('javascript');
  const [problem, setProblem] = useState(null);
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);   // ExecutionResult from backend
  const [showHistory, setShowHistory] = useState(false);
  const [activeTab, setActiveTab] = useState('results'); // 'results' | 'console'
  const [consoleLog, setConsoleLog] = useState('');

  useEffect(() => {
    axios.get(`${API_BASE_URL}/api/problems/${id}`)
      .then(r => {
        const d = r.data;
        let examples = [];
        try {
          const tcs = JSON.parse(d.testCases || '[]');
          examples = tcs.map(tc => ({ input: tc.input, output: tc.expected }));
        } catch { /* ignore */ }
        setProblem({ id: d.id, title: d.title, difficulty: d.difficulty, description: d.description, examples });
      })
      .catch(() => setProblem({ id, title: `Problem #${id}`, difficulty: 'Medium', description: 'Backend offline — start the Spring Boot server.', examples: [] }));
  }, [id]);

  useEffect(() => {
    setCode(getTemplate(id, language));
    setResult(null);
  }, [id, language]);

  const handleRun = useCallback(async () => {
    setLoading(true);
    setResult(null);
    setConsoleLog('Sending to execution engine...');
    setActiveTab('results');

    try {
      const res = await axios.post(`${API_BASE_URL}/api/execute`, {
        userId: currentUser?.id ?? 0,
        problemId: Number(id),
        code,
        language,
      });
      setResult(res.data);
      setConsoleLog('');
    } catch (err) {
      setConsoleLog(`Network error: ${err.message}\nMake sure the backend is running on port 8080.`);
    }
    setLoading(false);
  }, [code, language, id, currentUser]);

  const allPassed = result?.status === 'Accepted';
  const statusColor = result ? (STATUS_COLOR[result.status] || 'var(--text-secondary)') : 'var(--text-secondary)';

  return (
    <div style={{ display: 'flex', height: 'calc(100vh - 72px)', overflow: 'hidden', position: 'relative' }}>

      {/* LEFT: Problem */}
      <div style={{ flex: '1', borderRight: '1px solid var(--border-color)', overflowY: 'auto',
        padding: '2rem', background: 'rgba(0,0,0,0.2)', minWidth: 0 }}>
        {problem ? (
          <>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
              <h1 style={{ fontSize: '1.75rem' }}>{problem.title}</h1>
              <span className={`badge badge-${problem.difficulty.toLowerCase()}`}>{problem.difficulty}</span>
            </div>
            <div style={{ color: 'var(--text-secondary)', fontSize: '1rem', lineHeight: 1.8,
              marginBottom: '2rem', whiteSpace: 'pre-line' }}>
              {problem.description}
            </div>
            {problem.examples.length > 0 && (
              <>
                <h3 style={{ marginBottom: '1rem' }}>Examples</h3>
                {problem.examples.map((ex, i) => (
                  <div key={i} style={{ background: 'rgba(255,255,255,0.03)', padding: '1rem',
                    borderRadius: 8, marginBottom: '0.75rem', border: '1px solid var(--border-color)',
                    fontFamily: 'Fira Code, monospace', fontSize: '0.88rem' }}>
                    <div style={{ marginBottom: 4 }}><span style={{ color: 'var(--text-secondary)' }}>Input: </span>
                      <span style={{ color: 'var(--success)' }}>{ex.input}</span></div>
                    <div><span style={{ color: 'var(--text-secondary)' }}>Output: </span>
                      <span style={{ color: 'var(--brand-primary)' }}>{ex.output}</span></div>
                  </div>
                ))}
              </>
            )}
          </>
        ) : (
          <div style={{ color: 'var(--text-secondary)' }}>Loading problem...</div>
        )}
      </div>

      {/* RIGHT: Editor + Console */}
      <div style={{ flex: '1.4', display: 'flex', flexDirection: 'column', minWidth: 0 }}>

        {/* Toolbar */}
        <div style={{ padding: '0.6rem 1rem', borderBottom: '1px solid var(--border-color)',
          background: 'var(--bg-secondary)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <select value={language} onChange={e => setLanguage(e.target.value)}
            style={{ background: 'rgba(0,0,0,0.3)', border: '1px solid var(--border-color)',
              color: 'var(--text-primary)', padding: '0.35rem 0.75rem', borderRadius: 6, outline: 'none', cursor: 'pointer' }}>
            <option value="javascript">JavaScript</option>
            <option value="python">Python</option>
            <option value="java">Java</option>
            <option value="cpp">C++</option>
          </select>

          <div style={{ display: 'flex', gap: 8 }}>
            {currentUser && (
              <button className="btn btn-secondary" style={{ padding: '0.35rem 0.75rem', fontSize: '0.85rem' }}
                onClick={() => setShowHistory(h => !h)}>
                <History size={14} /> History
              </button>
            )}
            <button className="btn btn-secondary" style={{ padding: '0.35rem 0.75rem', fontSize: '0.85rem' }}
              onClick={() => { setCode(getTemplate(id, language)); setResult(null); }}>
              <RefreshCcw size={14} /> Reset
            </button>
            <button className="btn btn-primary" style={{ padding: '0.35rem 1.25rem' }}
              onClick={handleRun} disabled={loading}>
              {loading
                ? <><RefreshCcw size={15} style={{ animation: 'spin 1s linear infinite' }} /> Running...</>
                : <><Play size={15} fill="currentColor" /> Run & Submit</>}
            </button>
          </div>
        </div>

        {/* Monaco Editor */}
        <div style={{ flex: 1, minHeight: 0 }}>
          <Editor
            height="100%"
            theme="vs-dark"
            language={language === 'cpp' ? 'cpp' : language}
            value={code}
            onChange={v => setCode(v)}
            options={{
              minimap: { enabled: false },
              fontSize: 14,
              fontFamily: "'Fira Code', monospace",
              scrollBeyondLastLine: false,
              smoothScrolling: true,
              cursorBlinking: 'smooth',
              padding: { top: 16 },
              lineNumbers: 'on',
            }}
          />
        </div>

        {/* Results / Console Panel */}
        <div style={{ height: '38%', borderTop: '1px solid var(--border-color)',
          background: '#0d0d14', display: 'flex', flexDirection: 'column' }}>

          {/* Tab bar */}
          <div style={{ display: 'flex', borderBottom: '1px solid var(--border-color)', padding: '0 1rem' }}>
            {['results', 'console'].map(tab => (
              <button key={tab} onClick={() => setActiveTab(tab)}
                style={{ background: 'none', border: 'none', cursor: 'pointer', padding: '0.6rem 1rem',
                  fontSize: '0.82rem', fontWeight: 600, textTransform: 'capitalize',
                  color: activeTab === tab ? 'var(--brand-primary)' : 'var(--text-secondary)',
                  borderBottom: activeTab === tab ? '2px solid var(--brand-primary)' : '2px solid transparent',
                  marginBottom: -1 }}>
                {tab === 'results' ? <><Cpu size={13} style={{ marginRight: 5, verticalAlign: 'middle' }} />Test Results</> : <><Terminal size={13} style={{ marginRight: 5, verticalAlign: 'middle' }} />Console</>}
              </button>
            ))}

            {/* Status badge */}
            {result && (
              <div style={{ marginLeft: 'auto', display: 'flex', alignItems: 'center', gap: 8 }}>
                <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: 4 }}>
                  <Clock size={12} /> {result.runtimeMs}ms total
                </span>
                <span style={{ padding: '2px 10px', borderRadius: 4, fontSize: '0.8rem', fontWeight: 700,
                  color: statusColor, background: `${statusColor}18` }}>
                  {result.status}
                </span>
              </div>
            )}
          </div>

          <div style={{ flex: 1, overflowY: 'auto', padding: '0.75rem 1rem' }}>
            {activeTab === 'results' ? (
              <>
                {loading && (
                  <div style={{ color: 'var(--text-secondary)', fontFamily: 'Fira Code, monospace', fontSize: '0.88rem' }}>
                    ⏳ Executing against test cases...
                  </div>
                )}
                {!loading && !result && (
                  <div style={{ color: 'var(--text-muted)', fontFamily: 'Fira Code, monospace', fontSize: '0.88rem' }}>
                    &gt; Press "Run &amp; Submit" to execute your code against all test cases.
                  </div>
                )}
                {result?.errorMessage && (
                  <div style={{ color: 'var(--warning)', background: 'rgba(245,158,11,0.08)',
                    padding: '0.75rem', borderRadius: 8, marginBottom: 12,
                    fontFamily: 'Fira Code, monospace', fontSize: '0.85rem' }}>
                    ⚠ {result.errorMessage}
                  </div>
                )}
                {result?.testResults?.map((tc, i) => <TCRow key={i} tc={tc} index={i} />)}
                {result?.status === 'Accepted' && (
                  <div style={{ marginTop: 12, padding: '0.75rem 1rem', borderRadius: 8,
                    background: 'rgba(16,185,129,0.1)', border: '1px solid rgba(16,185,129,0.3)',
                    color: 'var(--success)', fontWeight: 600, fontSize: '0.9rem' }}>
                    🎉 All test cases passed! +10 points awarded to your profile.
                  </div>
                )}
              </>
            ) : (
              <div style={{ fontFamily: 'Fira Code, monospace', fontSize: '0.88rem',
                color: 'var(--text-secondary)', whiteSpace: 'pre-wrap' }}>
                {consoleLog || '> Console output will appear here.'}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Submission History Drawer */}
      {showHistory && currentUser && (
        <HistoryDrawer
          userId={currentUser.id}
          problemId={Number(id)}
          onClose={() => setShowHistory(false)}
        />
      )}

      <style>{`@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }`}</style>
    </div>
  );
};

export default WorkspacePage;
