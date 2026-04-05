import { useState } from "react";

export function SettingsPage() {
  const [memory, setMemory] = useState(2048);
  const [javaPath, setJavaPath] = useState("/usr/bin/java");

  return (
    <div style={{ padding: "28px", height: "100%", overflowY: "auto", maxWidth: "480px" }}>
      <h2 style={{ margin: "0 0 4px", fontSize: "20px", fontWeight: "700" }}>Settings</h2>
      <p style={{ margin: "0 0 32px", fontSize: "13px", color: "#555" }}>Configure Bloom Client</p>

      <div style={{ display: "flex", flexDirection: "column", gap: "24px" }}>
        {/* Memory */}
        <div>
          <label style={{ display: "block", fontSize: "13px", fontWeight: "600", marginBottom: "8px" }}>
            Memory — {memory} MB
          </label>
          <input
            type="range" min={512} max={8192} step={256} value={memory}
            onChange={e => setMemory(Number(e.target.value))}
            style={{ width: "100%", accentColor: "#fff" }}
          />
          <div style={{ display: "flex", justifyContent: "space-between", fontSize: "11px", color: "#444", marginTop: "4px" }}>
            <span>512 MB</span><span>8192 MB</span>
          </div>
        </div>

        {/* Java path */}
        <div>
          <label style={{ display: "block", fontSize: "13px", fontWeight: "600", marginBottom: "8px" }}>Java Path</label>
          <input
            value={javaPath}
            onChange={e => setJavaPath(e.target.value)}
            style={{
              width: "100%", background: "#161616", border: "1px solid #222",
              color: "#fff", borderRadius: "6px", padding: "10px 14px",
              fontSize: "13px", outline: "none", boxSizing: "border-box",
            }}
          />
        </div>

        {/* Save button */}
        <button style={{
          background: "#fff", color: "#000", border: "none", borderRadius: "6px",
          padding: "10px 24px", fontSize: "13px", fontWeight: "600",
          cursor: "pointer", alignSelf: "flex-start",
        }}>
          Save Settings
        </button>
      </div>
    </div>
  );
}
