import { useState } from 'react';
import Editor from '@monaco-editor/react';
import axios from 'axios';
import { useTheme } from '../contexts/ThemeContext';
import { Play, Trash2, Copy, Code2 } from 'lucide-react';

const TEMPLATES = {
  javascript: `// JavaScript Playground
console.log("Hello, World!");

// Read input from stdin
// const readline = require('readline');
// Use input() in Python style for simplicity`,
  
  python: `# Python Playground
print("Hello, World!")

# Read input from stdin
# name = input("Enter name: ")
# print(f"Hello, {name}!")`,
  
  java: `import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
        
        // Read input from stdin
        // Scanner sc = new Scanner(System.in);
        // String name = sc.nextLine();
        // System.out.println("Hello, " + name);
    }
}`,
  
  c: `#include <stdio.h>

int main() {
    printf("Hello, World!\\n");
    
    // Read input from stdin
    // char name[100];
    // scanf("%s", name);
    // printf("Hello, %s!\\n", name);
    
    return 0;
}`,
  
  cpp: `#include <iostream>
using namespace std;

int main() {
    cout << "Hello, World!" << endl;
    
    // Read input from stdin
    // string name;
    // cin >> name;
    // cout << "Hello, " << name << "!" << endl;
    
    return 0;
}`
};

export default function PlaygroundPage() {
  const { theme } = useTheme();
  const isDark = theme === 'dark';
  const [language, setLanguage] = useState('javascript');
  const [code, setCode] = useState(TEMPLATES.javascript);
  const [input, setInput] = useState('');
  const [output, setOutput] = useState('');
  const [isRunning, setIsRunning] = useState(false);
  const [runtime, setRuntime] = useState(null);

  const handleLanguageChange = (lang) => {
    setLanguage(lang);
    setCode(TEMPLATES[lang]);
    setOutput('');
    setRuntime(null);
  };

  const handleRun = async () => {
    setIsRunning(true);
    setOutput('Running...');
    setRuntime(null);

    try {
      const res = await axios.post('/api/playground/run', { code, language, input });
      
      setOutput(res.data.output || '(no output)');
      setRuntime(res.data.runtime);
    } catch (err) {
      setOutput('Error: ' + (err.response?.data?.message || err.message));
    } finally {
      setIsRunning(false);
    }
  };

  const handleClear = () => {
    setCode(TEMPLATES[language]);
    setInput('');
    setOutput('');
    setRuntime(null);
  };

  const handleCopyOutput = () => {
    navigator.clipboard.writeText(output);
  };

  return (
    <div style={{ 
      minHeight: 'calc(100vh - 60px)', 
      padding: '2rem',
      background: isDark ? '#0a0a0f' : '#f5f5f5'
    }}>
      <div style={{
        maxWidth: '1400px',
        margin: '0 auto',
        background: isDark ? 'rgba(20, 20, 30, 0.6)' : 'rgba(255, 255, 255, 0.8)',
        backdropFilter: 'blur(10px)',
        borderRadius: '16px',
        border: `1px solid ${isDark ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.1)'}`,
        padding: '2rem',
        boxShadow: '0 8px 32px rgba(0,0,0,0.1)'
      }}>
        
        <div style={{ marginBottom: '1.5rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.5rem' }}>
            <Code2 size={32} color="#667eea" />
            <h1 style={{ 
              fontSize: '2rem', 
              fontWeight: '700',
              background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
              WebkitBackgroundClip: 'text',
              WebkitTextFillColor: 'transparent',
              margin: 0
            }}>
              Code Playground
            </h1>
          </div>
          <p style={{ 
            color: isDark ? '#a0a0b0' : '#666',
            fontSize: '0.95rem',
            margin: 0
          }}>
            Practice coding in JavaScript, Python, Java, C, and C++ • No login required • Not saved to database
          </p>
        </div>

        <div style={{ 
          display: 'flex', 
          gap: '0.5rem', 
          marginBottom: '1rem',
          flexWrap: 'wrap'
        }}>
          {['javascript', 'python', 'java', 'c', 'cpp'].map(lang => (
            <button
              key={lang}
              onClick={() => handleLanguageChange(lang)}
              style={{
                padding: '0.5rem 1rem',
                borderRadius: '8px',
                border: 'none',
                background: language === lang
                  ? 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
                  : isDark ? 'rgba(255,255,255,0.05)' : 'rgba(0,0,0,0.05)',
                color: language === lang ? '#fff' : isDark ? '#e0e0e0' : '#333',
                cursor: 'pointer',
                fontWeight: language === lang ? '600' : '400',
                transition: 'all 0.2s'
              }}
            >
              {lang === 'cpp' ? 'C++' : lang.charAt(0).toUpperCase() + lang.slice(1)}
            </button>
          ))}
        </div>

        <div style={{ 
          display: 'grid', 
          gridTemplateColumns: '1fr 1fr', 
          gap: '1rem',
          marginBottom: '1rem'
        }}>
          <div style={{ 
            gridColumn: '1 / -1',
            border: `1px solid ${isDark ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.1)'}`,
            borderRadius: '8px',
            overflow: 'hidden'
          }}>
            <Editor
              height="400px"
              language={language === 'cpp' ? 'cpp' : language}
              value={code}
              onChange={(val) => setCode(val || '')}
              theme={isDark ? 'vs-dark' : 'light'}
              options={{
                minimap: { enabled: false },
                fontSize: 14,
                lineNumbers: 'on',
                scrollBeyondLastLine: false,
                automaticLayout: true
              }}
            />
          </div>

          <div>
            <label style={{ 
              display: 'block', 
              marginBottom: '0.5rem',
              fontWeight: '600',
              color: isDark ? '#e0e0e0' : '#333'
            }}>
              Input (stdin)
            </label>
            <textarea
              value={input}
              onChange={(e) => setInput(e.target.value)}
              placeholder="Enter input here..."
              style={{
                width: '100%',
                height: '150px',
                padding: '0.75rem',
                borderRadius: '8px',
                border: `1px solid ${isDark ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.1)'}`,
                background: isDark ? 'rgba(0,0,0,0.3)' : '#fff',
                color: isDark ? '#e0e0e0' : '#333',
                fontFamily: 'monospace',
                fontSize: '0.9rem',
                resize: 'vertical'
              }}
            />
          </div>

          <div>
            <div style={{ 
              display: 'flex', 
              justifyContent: 'space-between', 
              alignItems: 'center',
              marginBottom: '0.5rem'
            }}>
              <label style={{ 
                fontWeight: '600',
                color: isDark ? '#e0e0e0' : '#333'
              }}>
                Output
              </label>
              {output && (
                <button
                  onClick={handleCopyOutput}
                  style={{
                    padding: '0.25rem 0.5rem',
                    borderRadius: '4px',
                    border: 'none',
                    background: 'transparent',
                    color: isDark ? '#a0a0b0' : '#666',
                    cursor: 'pointer',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '0.25rem',
                    fontSize: '0.85rem'
                  }}
                >
                  <Copy size={14} /> Copy
                </button>
              )}
            </div>
            <div style={{
              width: '100%',
              height: '150px',
              padding: '0.75rem',
              borderRadius: '8px',
              border: `1px solid ${isDark ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.1)'}`,
              background: isDark ? 'rgba(0,0,0,0.3)' : '#f9f9f9',
              color: isDark ? '#e0e0e0' : '#333',
              fontFamily: 'monospace',
              fontSize: '0.9rem',
              overflowY: 'auto',
              whiteSpace: 'pre-wrap',
              wordBreak: 'break-word'
            }}>
              {output || 'Output will appear here...'}
            </div>
            {runtime !== null && (
              <div style={{ 
                marginTop: '0.5rem',
                fontSize: '0.85rem',
                color: isDark ? '#a0a0b0' : '#666'
              }}>
                ⚡ Runtime: {runtime}ms
              </div>
            )}
          </div>
        </div>

        <div style={{ display: 'flex', gap: '1rem' }}>
          <button
            onClick={handleRun}
            disabled={isRunning}
            style={{
              padding: '0.75rem 2rem',
              borderRadius: '8px',
              border: 'none',
              background: isRunning 
                ? 'rgba(102, 126, 234, 0.5)'
                : 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
              color: '#fff',
              fontWeight: '600',
              cursor: isRunning ? 'not-allowed' : 'pointer',
              display: 'flex',
              alignItems: 'center',
              gap: '0.5rem',
              fontSize: '1rem',
              transition: 'all 0.2s'
            }}
          >
            <Play size={18} />
            {isRunning ? 'Running...' : 'Run Code'}
          </button>

          <button
            onClick={handleClear}
            style={{
              padding: '0.75rem 1.5rem',
              borderRadius: '8px',
              border: `1px solid ${isDark ? 'rgba(255,255,255,0.2)' : 'rgba(0,0,0,0.2)'}`,
              background: 'transparent',
              color: isDark ? '#e0e0e0' : '#333',
              fontWeight: '500',
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              gap: '0.5rem',
              transition: 'all 0.2s'
            }}
          >
            <Trash2 size={18} />
            Clear
          </button>
        </div>

      </div>
    </div>
  );
}
