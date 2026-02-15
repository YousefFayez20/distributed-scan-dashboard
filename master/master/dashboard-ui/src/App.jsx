import { useState, useEffect, useCallback } from 'react';

const API_BASE = '/api/dashboard';

function timeAgo(isoString) {
  if (!isoString) return '—';
  const diff = Date.now() - new Date(isoString).getTime();
  const seconds = Math.floor(diff / 1000);
  if (seconds < 60) return `${seconds}s ago`;
  const minutes = Math.floor(seconds / 60);
  if (minutes < 60) return `${minutes}m ago`;
  const hours = Math.floor(minutes / 60);
  return `${hours}h ago`;
}

function statusBadgeClass(status) {
  if (!status) return 'badge';
  const s = status.toUpperCase();
  if (s === 'IDLE') return 'badge badge-idle';
  if (s === 'BUSY') return 'badge badge-busy';
  if (s === 'ACTIVE') return 'badge badge-active';
  if (s === 'OPEN') return 'badge badge-open';
  if (s === 'CLOSED') return 'badge badge-closed';
  return 'badge';
}

export default function App() {
  // ─── Workers state ───
  const [workers, setWorkers] = useState([]);
  const [workersLoading, setWorkersLoading] = useState(true);
  const [workersError, setWorkersError] = useState(null);
  const [selected, setSelected] = useState(new Set());

  // ─── Scan state ───
  const [scanLoading, setScanLoading] = useState(false);
  const [scanMessage, setScanMessage] = useState(null); // { type: 'success' | 'error', text }

  // ─── Results state ───
  const [results, setResults] = useState([]);
  const [resultsLoading, setResultsLoading] = useState(true);
  const [resultsError, setResultsError] = useState(null);

  // ─── Fetch workers ───
  const fetchWorkers = useCallback(async () => {
    setWorkersLoading(true);
    setWorkersError(null);
    try {
      const res = await fetch(`${API_BASE}/workers`);
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const data = await res.json();
      setWorkers(data);
      // clean up selections that no longer exist
      setSelected((prev) => {
        const names = new Set(data.map((w) => w.name));
        const next = new Set([...prev].filter((n) => names.has(n)));
        return next;
      });
    } catch (err) {
      setWorkersError(err.message);
    } finally {
      setWorkersLoading(false);
    }
  }, []);

  // ─── Fetch results ───
  const fetchResults = useCallback(async () => {
    setResultsLoading(true);
    setResultsError(null);
    try {
      const res = await fetch(`${API_BASE}/results`);
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const data = await res.json();
      setResults(data);
    } catch (err) {
      setResultsError(err.message);
    } finally {
      setResultsLoading(false);
    }
  }, []);

  // ─── Initial load + polling ───
  useEffect(() => {
    fetchWorkers();
    fetchResults();

    const workersInterval = setInterval(fetchWorkers, 30000);
    const resultsInterval = setInterval(fetchResults, 30000);

    return () => {
      clearInterval(workersInterval);
      clearInterval(resultsInterval);
    };
  }, [fetchWorkers, fetchResults]);

  // ─── Selection handlers ───
  const toggleWorker = (name) => {
    setSelected((prev) => {
      const next = new Set(prev);
      if (next.has(name)) next.delete(name);
      else next.add(name);
      return next;
    });
  };

  const toggleAll = () => {
    if (selected.size === workers.length) {
      setSelected(new Set());
    } else {
      setSelected(new Set(workers.map((w) => w.name)));
    }
  };

  // ─── Start scan ───
  const startScan = async () => {
    setScanLoading(true);
    setScanMessage(null);
    try {
      const res = await fetch(`${API_BASE}/start`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ workerNames: [...selected] }),
      });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      setScanMessage({ type: 'success', text: 'Scan started successfully!' });
      setSelected(new Set());
      // Refresh workers to see status changes
      setTimeout(fetchWorkers, 1000);
    } catch (err) {
      setScanMessage({ type: 'error', text: `Failed to start scan: ${err.message}` });
    } finally {
      setScanLoading(false);
    }
  };

  // ─── Auto-clear toast ───
  useEffect(() => {
    if (!scanMessage) return;
    const timer = setTimeout(() => setScanMessage(null), 5000);
    return () => clearTimeout(timer);
  }, [scanMessage]);

  return (
    <div className="app">
      <header className="app-header">
        <h1>🛡️ Distributed Scan Dashboard</h1>
        <p>Monitor workers · Launch scans · View results</p>
      </header>

      {/* ────── Toast ────── */}
      {scanMessage && (
        <div className={`toast toast-${scanMessage.type}`}>{scanMessage.text}</div>
      )}

      {/* ────── Workers Panel ────── */}
      <section className="panel">
        <div className="panel-header">
          <h2>
            <span className="icon">📡</span> Active Workers
          </h2>
          <div className="panel-actions">
            {selected.size > 0 && (
              <span className="selection-info">{selected.size} selected</span>
            )}
            <button className="btn btn-secondary" onClick={fetchWorkers} disabled={workersLoading}>
              {workersLoading ? <span className="spinner" /> : '↻'} Refresh
            </button>
            <button
              className="btn btn-primary"
              disabled={selected.size === 0 || scanLoading}
              onClick={startScan}
            >
              {scanLoading ? <span className="spinner" /> : '▶'} Start Scan
            </button>
          </div>
        </div>

        {workersError && (
          <div className="toast toast-error">Failed to load workers: {workersError}</div>
        )}

        {workersLoading && workers.length === 0 ? (
          <div className="state-message">
            <span className="spinner" style={{ width: 24, height: 24 }} />
            <p style={{ marginTop: 8 }}>Loading workers…</p>
          </div>
        ) : workers.length === 0 ? (
          <div className="state-message">
            <span className="icon">🔌</span>
            No active workers found. Workers must send heartbeats to register.
          </div>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th className="checkbox-cell">
                    <input
                      type="checkbox"
                      checked={workers.length > 0 && selected.size === workers.length}
                      onChange={toggleAll}
                    />
                  </th>
                  <th>Name</th>
                  <th>Status</th>
                  <th>Last Seen</th>
                </tr>
              </thead>
              <tbody>
                {workers.map((w) => (
                  <tr key={w.id}>
                    <td className="checkbox-cell">
                      <input
                        type="checkbox"
                        checked={selected.has(w.name)}
                        onChange={() => toggleWorker(w.name)}
                      />
                    </td>
                    <td>{w.name}</td>
                    <td>
                      <span className={statusBadgeClass(w.workerStatus)}>
                        {w.workerStatus}
                      </span>
                    </td>
                    <td className="time-ago">{timeAgo(w.lastSeen)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {/* ────── Results Panel ────── */}
      <section className="panel">
        <div className="panel-header">
          <h2>
            <span className="icon">📊</span> Scan Results
          </h2>
          <div className="panel-actions">
            <span className="selection-info">{results.length} result{results.length !== 1 ? 's' : ''}</span>
            <button className="btn btn-secondary" onClick={fetchResults} disabled={resultsLoading}>
              {resultsLoading ? <span className="spinner" /> : '↻'} Refresh
            </button>
          </div>
        </div>

        {resultsError && (
          <div className="toast toast-error">Failed to load results: {resultsError}</div>
        )}

        {resultsLoading && results.length === 0 ? (
          <div className="state-message">
            <span className="spinner" style={{ width: 24, height: 24 }} />
            <p style={{ marginTop: 8 }}>Loading results…</p>
          </div>
        ) : results.length === 0 ? (
          <div className="state-message">
            <span className="icon">📭</span>
            No scan results yet. Start a scan to begin.
          </div>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Worker</th>
                  <th>IP</th>
                  <th>Port</th>
                  <th>Status</th>
                  <th>Assignment</th>
                </tr>
              </thead>
              <tbody>
                {results.map((r, idx) => (
                  <tr key={idx}>
                    <td>{r.workerName}</td>
                    <td><code>{r.ip}</code></td>
                    <td>{r.port}</td>
                    <td>
                      <span className={statusBadgeClass(r.status)}>
                        {r.status}
                      </span>
                    </td>
                    <td>#{r.assignmentId}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  );
}
