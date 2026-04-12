import { useState, useEffect, useRef } from "react";
import { listen } from "@tauri-apps/api/event";

interface LogEntry {
  time: string;
  level: "info" | "warn" | "error" | "debug";
  message: string;
}

const LEVEL_COLORS = { info: "var(--green)", warn: "var(--yellow)", error: "var(--red)", debug: "var(--text-muted)" };

export function ConsolePage() {
  const [logs, setLogs] = useState<LogEntry[]>([]);
  const [filter, setFilter] = useState("");
  const [levels, setLevels] = useState({ info: true, warn: true, error: true, debug: true });
  const bottomRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const unsubs: (() => void)[] = [];

    listen<{ pct: number; msg: string }>("launch_progress", e => {
      addLog("info", e.payload.msg);
    }).then(fn => unsubs.push(fn));

    listen<string>("auth_error", e => {
      addLog("error", `Auth error: ${e.payload}`);
    }).then(fn => unsubs.push(fn));

    listen<any>("auth_success", e => {
      addLog("info", `Logged in as ${e.payload.username}`);
    }).then(fn => unsubs.push(fn));

    listen<any>("auth_code", e => {
      addLog("info", `Auth code: ${e.payload.code}`);
    }).then(fn => unsubs.push(fn));

    addLog("info", "Nebula Client started");

    return () => unsubs.forEach(fn => fn());
  }, []);

  function addLog(level: LogEntry["level"], message: string) {
    const time = new Date().toLocaleTimeString("en-US", { hour12: false });
    setLogs(prev => [...prev.slice(-500), { time, level, message }]);
    setTimeout(() => bottomRef.current?.scrollIntoView({ behavior: "smooth" }), 50);
  }

  const filtered = logs.filter(l =>
    levels[l.level] && (!filter || l.message.toLowerCase().includes(filter.toLowerCase()))
  );

  return (
    <div className="fade-in" style={{ display: "flex", flexDirection: "column", height: "100%", padding: "24px", gap: "12px" }}>
      {/* Header */}
      <div style={{ display: "flex", alignItems: "center", gap: "12px", flexShrink: 0, flexWrap: "wrap" }}>
        {(["info", "warn", "error", "debug"] as const).map(level => (
          <label key={level} style={{
            display: "flex", alignItems: "center", gap: "5px", cursor: "pointer",
            fontSize: "12px", color: levels[level] ? LEVEL_COLORS[level] : "var(--text-faint)",
            transition: "color 0.15s",
          }}>
            <input type="checkbox" checked={levels[level]}
              onChange={() => setLevels(prev => ({ ...prev, [level]: !prev[level] }))}
              style={{ accentColor: "var(--pink)" }}
            />
            {level.charAt(0).toUpperCase() + level.slice(1)}
          </label>
        ))}

        <div style={{ flex: 1 }} />

        <button className="bloom-btn-ghost" onClick={() => setLogs([])} style={{ fontSize: "11px", padding: "4px 10px" }}>
          Clear
        </button>
        <button className="bloom-btn-ghost" onClick={() => {
          const text = logs.map(l => `[${l.time}] [${l.level.toUpperCase()}] ${l.message}`).join("\n");
          navigator.clipboard.writeText(text);
        }} style={{ fontSize: "11px", padding: "4px 10px" }}>
          Copy
        </button>
      </div>

      {/* Search */}
      <input
        className="bloom-input"
        value={filter}
        onChange={e => setFilter(e.target.value)}
        placeholder="Filter logs..."
        style={{ flexShrink: 0, fontSize: "12px", padding: "8px 12px" }}
      />

      {/* Stats */}
      <div style={{
        display: "flex", gap: "20px", padding: "8px 14px", flexShrink: 0,
        background: "var(--bg-card)", borderRadius: "8px", border: "1px solid var(--border)",
        fontSize: "11px", fontWeight: "700",
      }}>
        <span style={{ color: "var(--text-muted)" }}>Entries: <span style={{ color: "var(--text)" }}>{filtered.length}</span></span>
        <span style={{ color: "var(--text-muted)" }}>Errors: <span style={{ color: "var(--red)" }}>{logs.filter(l => l.level === "error").length}</span></span>
        <span style={{ color: "var(--text-muted)" }}>Warnings: <span style={{ color: "var(--yellow)" }}>{logs.filter(l => l.level === "warn").length}</span></span>
      </div>

      {/* Log output */}
      <div style={{
        flex: 1, overflow: "auto", borderRadius: "10px",
        background: "rgba(0,0,0,0.25)", border: "1px solid var(--border)",
        padding: "12px", fontFamily: "'JetBrains Mono', 'SF Mono', monospace", fontSize: "11px", lineHeight: 1.7,
      }}>
        {filtered.length === 0 && (
          <div className="bloom-empty" style={{ padding: "40px" }}>No logs yet</div>
        )}
        {filtered.map((log, i) => (
          <div key={i} style={{ borderBottom: "1px solid rgba(255,255,255,0.02)", padding: "2px 0" }}>
            <span style={{ color: "var(--text-faint)" }}>[{log.time}]</span>{" "}
            <span style={{ color: LEVEL_COLORS[log.level], fontWeight: "600" }}>[{log.level.toUpperCase()}]</span>{" "}
            <span style={{ color: log.level === "error" ? "#FF8888" : "var(--text)" }}>{log.message}</span>
          </div>
        ))}
        <div ref={bottomRef} />
      </div>
    </div>
  );
}
