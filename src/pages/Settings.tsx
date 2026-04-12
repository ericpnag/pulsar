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
    const saved = localStorage.getItem("nebula-settings");
    if (saved) setSettings({ ...DEFAULT, ...JSON.parse(saved) });
  }, []);

  function save(patch: Partial<Settings>) {
    const updated = { ...settings, ...patch };
    setSettings(updated);
    localStorage.setItem("nebula-settings", JSON.stringify(updated));
  }

  return (
    <div className="fade-in" style={{ padding: "28px 32px", overflowY: "auto", height: "100%", maxWidth: "560px" }}>
      <h2 className="page-title">Settings</h2>
      <p className="page-subtitle" style={{ marginBottom: "24px" }}>Configure your Nebula Client</p>

      {/* RAM */}
      <div className="bloom-card" style={{ padding: "20px", marginBottom: "12px" }}>
        <div style={{ display: "flex", justifyContent: "space-between", marginBottom: "12px" }}>
          <label style={{ fontSize: "13px", fontWeight: "600", color: "var(--text)" }}>Memory (RAM)</label>
          <span style={{ fontSize: "13px", fontWeight: "700", color: "var(--pink)" }}>{settings.ram / 1024} GB</span>
        </div>
        <input type="range" min={1024} max={16384} step={512} value={settings.ram}
          onChange={e => save({ ram: parseInt(e.target.value) })}
          style={{ width: "100%", accentColor: "var(--pink)", height: "4px" }}
        />
        <div style={{ display: "flex", justifyContent: "space-between", fontSize: "10px", color: "var(--text-faint)", marginTop: "6px" }}>
          <span>1 GB</span><span>4 GB</span><span>8 GB</span><span>16 GB</span>
        </div>
      </div>

      {/* Java Path */}
      <div className="bloom-card" style={{ padding: "20px", marginBottom: "12px" }}>
        <label style={{ fontSize: "13px", fontWeight: "600", color: "var(--text)", display: "block", marginBottom: "10px" }}>
          Java Path
        </label>
        <input
          className="bloom-input"
          type="text"
          value={settings.javaPath}
          onChange={e => save({ javaPath: e.target.value })}
          placeholder="Auto-detect"
          style={{ width: "100%" }}
        />
        <div style={{ fontSize: "11px", color: "var(--text-faint)", marginTop: "6px" }}>Leave empty to auto-detect Java installation</div>
      </div>

      {/* Close on launch */}
      <div className="bloom-card" style={{ padding: "18px 20px", marginBottom: "12px" }}>
        <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
          <span style={{ fontSize: "13px", fontWeight: "600", color: "var(--text)" }}>Close launcher when game starts</span>
          <div onClick={() => save({ closeOnLaunch: !settings.closeOnLaunch })} style={{
            width: "42px", height: "24px", borderRadius: "12px", cursor: "pointer",
            background: settings.closeOnLaunch
              ? "linear-gradient(135deg, var(--pink), var(--pink-soft))"
              : "rgba(255,255,255,0.08)",
            transition: "background 0.2s", position: "relative",
            border: settings.closeOnLaunch ? "none" : "1px solid var(--border)",
          }}>
            <div style={{
              width: "18px", height: "18px", borderRadius: "9px", background: "#fff",
              position: "absolute", top: settings.closeOnLaunch ? "3px" : "2px",
              left: settings.closeOnLaunch ? "21px" : "3px",
              transition: "left 0.2s",
              boxShadow: "0 1px 3px rgba(0,0,0,0.3)",
            }} />
          </div>
        </div>
      </div>

      {/* About */}
      <div className="bloom-card" style={{ padding: "20px", marginTop: "24px" }}>
        <div style={{ display: "flex", alignItems: "center", gap: "12px", marginBottom: "12px" }}>
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="5" fill="#7AA2F7" opacity="0.3"/>
            <circle cx="12" cy="12" r="3" fill="#89B4FA" opacity="0.7"/>
            <circle cx="12" cy="12" r="1.5" fill="#B4BEFE" opacity="0.9"/>
            <circle cx="7" cy="8" r="1" fill="#B4BEFE" opacity="0.4"/>
            <circle cx="17" cy="9" r="0.8" fill="#89B4FA" opacity="0.5"/>
          </svg>
          <div>
            <div style={{ fontSize: "15px", fontWeight: "800", color: "var(--pink-light)" }}>Nebula Client</div>
            <div style={{ fontSize: "11px", color: "var(--text-dim)" }}>v1.0.0</div>
          </div>
        </div>
        <div style={{ fontSize: "12px", color: "var(--text-dim)", lineHeight: 1.6 }}>
          A space themed Minecraft experience.
        </div>
        <div style={{ fontSize: "11px", color: "var(--text-faint)", marginTop: "8px" }}>
          github.com/ericpnag/bloom-launcher
        </div>
      </div>
    </div>
  );
}
