import type { LaunchState } from "../App";
import { PetalCanvas } from "../components/PetalCanvas";

interface Props {
  launch: LaunchState;
  versions: string[];
  selectedVersion: string;
  onVersionChange: (v: string) => void;
  onPlay: () => void;
}

export function HomePage({ launch, versions, selectedVersion, onVersionChange, onPlay }: Props) {
  const isLoading = launch.phase === "loading";
  const isDone = launch.phase === "done";
  const isError = launch.phase === "error";
  const isIdle = launch.phase === "idle";

  return (
    <div style={{
      flex: 1, display: "flex", flexDirection: "column", height: "100%",
      background: "linear-gradient(180deg, #1a1025 0%, #2d1b3d 35%, #4a2040 70%, #1a0f1a 100%)",
      position: "relative", overflow: "hidden",
    }}>
      <PetalCanvas />

      {/* Hero content */}
      <div style={{ flex: 1, display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", gap: "14px", zIndex: 1, position: "relative" }}>

        {/* Logo */}
        <div style={{
          fontSize: "38px", fontWeight: "800", letterSpacing: "0.12em", color: "#fff",
          textShadow: "0 0 40px rgba(255,176,192,0.3), 0 0 80px rgba(255,176,192,0.15)",
        }}>
          B L O O M
        </div>
        <div style={{
          fontSize: "12px", letterSpacing: "0.3em", color: "#99889988",
          marginTop: "-8px", marginBottom: "8px",
        }}>
          C L I E N T
        </div>

        {/* Separator */}
        <div style={{
          width: "100px", height: "1px", marginBottom: "8px",
          background: "linear-gradient(90deg, transparent, rgba(255,176,192,0.4), transparent)",
        }} />

        {/* Version selector */}
        <select
          value={selectedVersion}
          onChange={e => onVersionChange(e.target.value)}
          disabled={isLoading}
          style={{
            background: "rgba(255,255,255,0.06)", border: "1px solid rgba(255,176,192,0.15)", color: "#caa",
            borderRadius: "6px", padding: "6px 14px", fontSize: "13px",
            cursor: "pointer", outline: "none", marginBottom: "4px",
            backdropFilter: "blur(8px)",
          }}
        >
          {versions.map(v => (
            <option key={v} value={v} style={{ background: "#1a1025" }}>{v === versions[0] ? `${v} (latest)` : v}</option>
          ))}
        </select>

        {/* Play button */}
        <button
          onClick={isIdle || isError ? onPlay : undefined}
          disabled={isLoading || isDone}
          style={{
            background: isLoading || isDone
              ? "rgba(255,176,192,0.15)"
              : "linear-gradient(135deg, #FFB7C9, #F8A4B8)",
            color: isLoading || isDone ? "#886" : "#1a0f1a",
            border: "none", borderRadius: "10px",
            padding: "14px 72px", fontSize: "15px", fontWeight: "700",
            letterSpacing: "0.1em",
            cursor: isLoading || isDone ? "not-allowed" : "pointer",
            boxShadow: isIdle || isError ? "0 0 30px rgba(255,176,192,0.3), 0 4px 20px rgba(0,0,0,0.3)" : "none",
            transition: "all 0.2s",
            position: "relative", zIndex: 1,
          }}
          onMouseEnter={e => { if (isIdle || isError) { e.currentTarget.style.transform = "scale(1.03)"; e.currentTarget.style.boxShadow = "0 0 50px rgba(255,176,192,0.5), 0 4px 20px rgba(0,0,0,0.3)"; }}}
          onMouseLeave={e => { e.currentTarget.style.transform = "scale(1)"; e.currentTarget.style.boxShadow = "0 0 30px rgba(255,176,192,0.3), 0 4px 20px rgba(0,0,0,0.3)"; }}
        >
          {isDone ? "LAUNCHED" : isLoading ? "LOADING..." : isError ? "RETRY" : "P L A Y"}
        </button>
      </div>

      {/* Bottom status bar */}
      <div style={{ padding: "12px 20px 16px", borderTop: "1px solid rgba(255,176,192,0.08)", zIndex: 1, position: "relative" }}>
        <div style={{ height: "2px", background: "rgba(255,255,255,0.05)", borderRadius: "1px", marginBottom: "8px", overflow: "hidden" }}>
          <div style={{
            height: "100%", width: isLoading ? `${launch.progress}%` : isDone ? "100%" : "0%",
            background: "linear-gradient(90deg, #F8A4B8, #FFD1DC)",
            transition: "width 0.4s ease",
          }} />
        </div>
        <div style={{ fontSize: "12px", color: isError ? "#f88" : "#665566" }}>
          {launch.status || "Ready"}
        </div>
      </div>
    </div>
  );
}
