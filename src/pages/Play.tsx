import { LaunchState } from "../App";
// PetalCanvas removed from hero for now

interface Props {
  launch: LaunchState;
  versions: string[];
  selectedVersion: string;
  onVersionChange: (v: string) => void;
  onPlay: () => void;
}

export function PlayPage({ launch, versions, selectedVersion, onVersionChange, onPlay }: Props) {
  const canPlay = launch.phase === "idle" || launch.phase === "error";

  return (
    <div style={{ padding: "20px", display: "flex", flexDirection: "column", gap: "12px", height: "100%", overflowY: "auto" }}>

      {/* Hero Banner */}
      <div style={{
        borderRadius: "12px", overflow: "hidden",
        height: "200px", flexShrink: 0,
        background: "linear-gradient(135deg, #0B0E1A 0%, #131729 50%, #1a2040 100%)",
        border: "1px solid rgba(122,162,247,0.1)",
        display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center",
        position: "relative",
      }}>
        <div style={{ fontSize: "18px", fontWeight: "300", color: "#ccc", marginBottom: "4px", zIndex: 1 }}>
          Minecraft {selectedVersion}
        </div>
        <select
          value={selectedVersion}
          onChange={e => onVersionChange(e.target.value)}
          style={{
            background: "rgba(0,0,0,0.5)", border: "1px solid rgba(122,162,247,0.2)",
            color: "#fff", borderRadius: "8px", padding: "10px 24px",
            fontSize: "14px", fontWeight: "600", cursor: "pointer", outline: "none",
            zIndex: 1,
          }}
        >
          {versions.map(v => <option key={v} value={v} style={{ background: "#0B0E1A" }}>{v}</option>)}
        </select>
      </div>

      {/* Launch Button */}
      <div
        role="button"
        tabIndex={0}
        onMouseDown={(e) => { e.preventDefault(); if (canPlay) onPlay(); }}
        style={{
          width: "100%", padding: "18px", flexShrink: 0,
          background: canPlay
            ? "linear-gradient(135deg, #7AA2F7, #5B6EAE, #4A5899)"
            : "rgba(122,162,247,0.15)",
          color: canPlay ? "#0B0E1A" : "#998899",
          borderRadius: "10px",
          fontSize: "16px", fontWeight: "800", letterSpacing: "0.1em",
          cursor: canPlay ? "pointer" : "default",
          boxShadow: canPlay ? "0 4px 20px rgba(122,162,247,0.25)" : "none",
          display: "flex", alignItems: "center", justifyContent: "center", gap: "8px",
          userSelect: "none",
          boxSizing: "border-box",
        }}
      >
        {launch.phase === "done" ? "LAUNCHED" : launch.phase === "loading" ? "LAUNCHING..." : launch.phase === "error" ? "RETRY" : "LAUNCH FABRIC"}
      </div>

      {/* Progress */}
      {launch.phase === "loading" && (
        <div>
          <div style={{ height: "4px", background: "rgba(255,255,255,0.05)", borderRadius: "2px", overflow: "hidden" }}>
            <div style={{
              height: "100%", width: `${launch.progress}%`,
              background: "linear-gradient(90deg, #5B6EAE, #89B4FA)",
              transition: "width 0.4s ease",
            }} />
          </div>
          <div style={{ fontSize: "12px", color: "#776070", marginTop: "4px" }}>{launch.status}</div>
        </div>
      )}
      {launch.phase === "error" && (
        <div style={{ fontSize: "12px", color: "#FF8888", padding: "8px", background: "rgba(255,0,0,0.1)", borderRadius: "6px" }}>
          {launch.status}
        </div>
      )}

      {/* Info cards */}
      <div style={{ display: "flex", gap: "12px", flexShrink: 0 }}>
        <div style={{
          flex: 1, borderRadius: "10px",
          background: "rgba(255,255,255,0.03)", border: "1px solid rgba(122,162,247,0.08)",
          padding: "16px",
        }}>
          <div style={{ fontSize: "11px", fontWeight: "800", letterSpacing: "0.1em", color: "#998899", marginBottom: "8px" }}>
            NEBULA CLIENT
          </div>
          <div style={{ fontSize: "12px", color: "#776070", lineHeight: 1.5 }}>
            Press Right Shift in-game for modules. 11 modules available.
          </div>
        </div>
        <div style={{
          flex: 1, borderRadius: "10px",
          background: "rgba(255,255,255,0.03)", border: "1px solid rgba(122,162,247,0.08)",
          padding: "16px",
        }}>
          <div style={{ fontSize: "11px", fontWeight: "800", letterSpacing: "0.1em", color: "#998899", marginBottom: "8px" }}>
            POPULAR SERVERS
          </div>
          {[
            { name: "Hypixel", players: "45,231" },
            { name: "BedWars Practice", players: "2,104" },
            { name: "PvP Legacy", players: "891" },
          ].map((s, i) => (
            <div key={i} style={{
              display: "flex", justifyContent: "space-between",
              padding: "5px 0", borderBottom: i < 2 ? "1px solid rgba(122,162,247,0.05)" : "none",
            }}>
              <span style={{ fontSize: "12px", color: "#caa" }}>{s.name}</span>
              <span style={{ fontSize: "11px", color: "#55DD88" }}>● {s.players}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
