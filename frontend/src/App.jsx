import { useState } from 'react';

export default function App() {
  const [input, setInput] = useState('');
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setResult(null);
    const paths = input.split('\n').map(s => s.trim()).filter(Boolean);
    try {
      const res = await fetch('/api/delete', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ paths })
      });
      if (!res.ok) throw new Error('HTTP ' + res.status);
      setResult(await res.json());
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="container">
      <h1>Safe File Deleter</h1>
      <p className="hint">One absolute path per line. Symlinks, non-regular files, and paths outside allowed_root are refused.</p>
      <form onSubmit={handleSubmit}>
        <textarea
          value={input}
          onChange={e => setInput(e.target.value)}
          placeholder="/path/to/file1.txt&#10;/path/to/file2.txt"
          rows={8}
        />
        <button type="submit" disabled={loading || !input.trim()}>
          {loading ? 'Deleting...' : 'Delete Files'}
        </button>
      </form>
      {error && <div className="error">Error: {error}</div>}
      {result && (
        <div className="result">
          <p><strong>Allowed root:</strong> {result.allowedRoot}</p>
          <h3>Deleted ({result.deleted.length})</h3>
          <ul>{result.deleted.map((p, i) => <li key={i} className="ok">{p}</li>)}</ul>
          <h3>Refused ({result.refused.length})</h3>
          <ul>{result.refused.map((r, i) => <li key={i} className="bad">{r.path} — {r.reason}</li>)}</ul>
        </div>
      )}
    </div>
  );
}
