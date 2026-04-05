import { useState, useEffect } from "react";

interface Settings {
  ram: number;
  javaPath: string;
  closeOnLaunch: boolean;
}

const DEFAULT: Settings = { ram: 2048, javaPath: "", closeOnLaunch: false };

export function SettingsPage() {
  const [settings, setSettings] = useState<Settings>(DEFAULT);

  useEffect(() => {
    const saved = localStorage.getItem("bloom-settings");
    if (saved) setSettings({ ...DEFAULT, ...JSON.parse(saved) });
  }, []);

  function save(patch: Partial<Settings>) {
    const updated = { ...settings, ...patch };
    setSettings(updated);
    localStorage.setItem("bloom-settings", JSON.stringify(updated));
  }

  return (
    <div style={{ padding: "28px", overflowY: "auto", height: "100%" }}>
      <h2 style={{ margin: "0 0 4px", fontSize: "20px", fontWeight: "700", color: "#FFD1DC" }}>Settings</h2>
      <p style={{ margin: "0 0 24px", fontSize: "13px", color: "#776070" }}>Configure your Bloom Client</p>

      <div style={{ marginBottom: "24px" }}>
        <label style={{ fontSize: "13px", fontWeight: "600", color: "#caa", display: "block", marginBottom: "8px" }}>
          Memory (RAM): {settings.ram / 1024}GB
        </label>
        <input type="range" min={1024} max={16384} step={512} value={settings.ram}
          onChange={e => save({ ram: parseInt(e.target.value) })}
          style={{ width: "100%", accentColor: "#FFB7C9" }}
        />
        <div style={{ display: "flex", justifyContent: "space-between", fontSize: "11px", color: "#554455" }}>
          <span>1 GB</span><span>4 GB</span><span>8 GB</span><span>16 GB</span>
        </div>
      </div>

      <div style={{ marginBottom: "24px" }}>
        <label style={{ fontSize: "13px", fontWeight: "600", color: "#caa", display: "block", marginBottom: "8px" }}>
          Java Path (leave empty for auto-detect)
        </label>
        <input type="text" value={settings.javaPath} onChange={e => save({ javaPath: e.target.value })}
          placeholder="Auto-detect" style={{
            width: "100%", background: "rgba(255,255,255,0.04)", border: "1px solid rgba(255,176,192,0.12)",
            color: "#fff", borderRadius: "8px", padding: "10px 14px", fontSize: "13px", outline: "none", boxSizing: "border-box",
          }}
        />
      </div>

      <div style={{
        display: "flex", alignItems: "center", justifyContent: "space-between",
        padding: "12px 0", borderBottom: "1px solid rgba(255,176,192,0.06)",
      }}>
        <span style={{ fontSize: "13px", color: "#caa" }}>Close launcher when game starts</span>
        <div onClick={() => save({ closeOnLaunch: !settings.closeOnLaunch })} style={{
          width: "40px", height: "22px", borderRadius: "11px", cursor: "pointer",
          background: settings.closeOnLaunch ? "#FFB7C9" : "rgba(255,255,255,0.1)",
          transition: "background 0.2s", position: "relative",
        }}>
          <div style={{
            width: "18px", height: "18px", borderRadius: "9px", background: "#fff",
            position: "absolute", top: "2px",
            left: settings.closeOnLaunch ? "20px" : "2px", transition: "left 0.2s",
          }} />
        </div>
      </div>

      <div style={{ marginTop: "32px", padding: "16px", borderRadius: "10px", background: "rgba(255,255,255,0.03)", border: "1px solid rgba(255,176,192,0.08)" }}>
        <div style={{ fontSize: "14px", fontWeight: "700", color: "#FFD1DC", marginBottom: "4px" }}>Bloom Client v1.0.0</div>
        <div style={{ fontSize: "12px", color: "#776070" }}>Cherry blossom Minecraft experience</div>
        <div style={{ fontSize: "12px", color: "#554455", marginTop: "4px" }}>github.com/ericpnag/bloom-launcher</div>
      </div>
    </div>
  );
}
