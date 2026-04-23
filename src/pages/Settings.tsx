import { useState, useEffect } from "react";
import { invoke } from "@tauri-apps/api/core";

interface Settings {
  ram: number;
  javaPath: string;
  javaArgs: string;
  closeOnLaunch: boolean;
  showSnapshots: boolean;
  showBetas: boolean;
  discordRpc: boolean;
  reducedMotion: boolean;
  resolution: string;
  colorblindMode: string;
  streamerMode: boolean;
}

const DEFAULT: Settings = {
  ram: 2048, javaPath: "", javaArgs: "", closeOnLaunch: false,
  showSnapshots: false, showBetas: false, discordRpc: true,
  reducedMotion: false, resolution: "",
  colorblindMode: "none", streamerMode: false,
};

function Toggle({ value, onChange, label, description }: { value: boolean; onChange: (v: boolean) => void; label: string; description?: string }) {
  return (
    <div className="pulsar-card" style={{ padding: "16px 20px", marginBottom: "10px" }}>
      <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
        <div>
          <span style={{ fontSize: "13px", fontWeight: "600", color: "var(--text)" }}>{label}</span>
          {description && <div style={{ fontSize: "11px", color: "var(--text-faint)", marginTop: "2px" }}>{description}</div>}
        </div>
        <div onClick={() => onChange(!value)} style={{
          width: "42px", height: "24px", borderRadius: "12px", cursor: "pointer",
          background: value ? "linear-gradient(135deg, var(--pink), var(--pink-soft))" : "rgba(255,255,255,0.08)",
          transition: "background 0.2s", position: "relative",
          border: value ? "none" : "1px solid var(--border)", flexShrink: 0, marginLeft: "16px",
        }}>
          <div style={{
            width: "18px", height: "18px", borderRadius: "9px", background: "#fff",
            position: "absolute", top: value ? "3px" : "2px", left: value ? "21px" : "3px",
            transition: "left 0.2s", boxShadow: "0 1px 3px rgba(0,0,0,0.3)",
          }} />
        </div>
      </div>
    </div>
  );
}

export function SettingsPage() {
  const [settings, setSettings] = useState<Settings>(DEFAULT);

  useEffect(() => {
    const saved = localStorage.getItem("pulsar-settings");
    if (saved) setSettings({ ...DEFAULT, ...JSON.parse(saved) });
  }, []);

  function save(patch: Partial<Settings>) {
    const updated = { ...settings, ...patch };
    setSettings(updated);
    localStorage.setItem("pulsar-settings", JSON.stringify(updated));
  }

  useEffect(() => {
    const root = document.documentElement;
    root.classList.remove("colorblind-deuteranopia", "colorblind-protanopia", "colorblind-tritanopia");
    if (settings.colorblindMode === "deuteranopia") root.classList.add("colorblind-deuteranopia");
    else if (settings.colorblindMode === "protanopia") root.classList.add("colorblind-protanopia");
    else if (settings.colorblindMode === "tritanopia") root.classList.add("colorblind-tritanopia");
  }, [settings.colorblindMode]);

  const ramGB = (settings.ram / 1024).toFixed(1).replace(/\.0$/, "");
  const ramWarning = settings.ram > 8192 ? "High RAM may cause GC pauses. 4-8 GB recommended for most setups." : "";

  return (
    <div className="fade-in" style={{ padding: "28px 32px", overflowY: "auto", height: "100%", maxWidth: "580px" }}>
      <h2 className="page-title">Settings</h2>
      <p className="page-subtitle" style={{ marginBottom: "20px" }}>Configure your Pulsar Client</p>

      {/* ── Performance ── */}
      <div style={{ fontSize: "11px", fontWeight: "700", color: "var(--pink)", textTransform: "uppercase", letterSpacing: "1.5px", marginBottom: "8px", marginTop: "4px" }}>Performance</div>

      {/* RAM */}
      <div className="pulsar-card" style={{ padding: "18px 20px", marginBottom: "10px" }}>
        <div style={{ display: "flex", justifyContent: "space-between", marginBottom: "10px" }}>
          <label style={{ fontSize: "13px", fontWeight: "600", color: "var(--text)" }}>Memory (RAM)</label>
          <span style={{ fontSize: "13px", fontWeight: "700", color: "var(--pink)" }}>{ramGB} GB</span>
        </div>
        <input type="range" min={1024} max={16384} step={512} value={settings.ram}
          onChange={e => save({ ram: parseInt(e.target.value) })}
          style={{ width: "100%", accentColor: "var(--pink)", height: "4px" }}
        />
        <div style={{ display: "flex", justifyContent: "space-between", fontSize: "10px", color: "var(--text-faint)", marginTop: "6px" }}>
          <span>1 GB</span><span>4 GB</span><span>8 GB</span><span>16 GB</span>
        </div>
        {ramWarning && <div style={{ fontSize: "11px", color: "#DDAA44", marginTop: "8px" }}>{ramWarning}</div>}
      </div>

      {/* Resolution */}
      <div className="pulsar-card" style={{ padding: "18px 20px", marginBottom: "10px" }}>
        <label style={{ fontSize: "13px", fontWeight: "600", color: "var(--text)", display: "block", marginBottom: "8px" }}>
          Window Resolution
        </label>
        <input className="pulsar-input" type="text" value={settings.resolution}
          onChange={e => save({ resolution: e.target.value })}
          placeholder="Default (854x480)"
          style={{ width: "100%" }}
        />
        <div style={{ fontSize: "11px", color: "var(--text-faint)", marginTop: "4px" }}>Format: 1920x1080. Leave empty for default.</div>
      </div>

      {/* ── Java ── */}
      <div style={{ fontSize: "11px", fontWeight: "700", color: "var(--pink)", textTransform: "uppercase", letterSpacing: "1.5px", marginBottom: "8px", marginTop: "20px" }}>Java</div>

      {/* Java Path */}
      <div className="pulsar-card" style={{ padding: "18px 20px", marginBottom: "10px" }}>
        <label style={{ fontSize: "13px", fontWeight: "600", color: "var(--text)", display: "block", marginBottom: "8px" }}>
          Java Path
        </label>
        <input className="pulsar-input" type="text" value={settings.javaPath}
          onChange={e => save({ javaPath: e.target.value })}
          placeholder="Auto-detect (recommended)"
          style={{ width: "100%" }}
        />
        <div style={{ fontSize: "11px", color: "var(--text-faint)", marginTop: "4px" }}>Pulsar auto-downloads the right Java version for each MC version.</div>
      </div>

      {/* JVM Args */}
      <div className="pulsar-card" style={{ padding: "18px 20px", marginBottom: "10px" }}>
        <label style={{ fontSize: "13px", fontWeight: "600", color: "var(--text)", display: "block", marginBottom: "8px" }}>
          JVM Arguments
        </label>
        <input className="pulsar-input" type="text" value={settings.javaArgs}
          onChange={e => save({ javaArgs: e.target.value })}
          placeholder="Additional JVM flags (advanced)"
          style={{ width: "100%", fontFamily: "monospace", fontSize: "12px" }}
        />
        <div style={{ fontSize: "11px", color: "var(--text-faint)", marginTop: "4px" }}>G1GC optimizations are included by default. Add extra flags if needed.</div>
        {/* JVM Presets */}
        <div style={{ display: "flex", gap: "6px", marginTop: "10px", flexWrap: "wrap" }}>
          {[
            { label: "Aikar's Flags", args: "-XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions -XX:+DisableExplicitGC -XX:+AlwaysPreTouch -XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=40 -XX:G1HeapRegionSize=8M -XX:G1ReservePercent=20 -XX:G1HeapWastePercent=5 -XX:G1MixedGCCountTarget=4 -XX:InitiatingHeapOccupancyPercent=15 -XX:G1MixedGCLiveThresholdPercent=90 -XX:G1RSetUpdatingPauseTimePercent=5 -XX:SurvivorRatio=32 -XX:+PerfDisableSharedMem -XX:MaxTenuringThreshold=1" },
            { label: "ZGC (Low latency)", args: "-XX:+UseZGC -XX:+ZGenerational" },
            { label: "Default", args: "" },
          ].map(preset => (
            <button key={preset.label} className="pulsar-btn-ghost"
              onClick={() => save({ javaArgs: preset.args })}
              style={{ fontSize: "10px", padding: "4px 10px" }}
            >
              {preset.label}
            </button>
          ))}
        </div>
      </div>

      {/* ── Versions ── */}
      <div style={{ fontSize: "11px", fontWeight: "700", color: "var(--pink)", textTransform: "uppercase", letterSpacing: "1.5px", marginBottom: "8px", marginTop: "20px" }}>Versions</div>

      <Toggle label="Show Snapshots" description="Include snapshot versions in the version selector"
        value={settings.showSnapshots} onChange={v => save({ showSnapshots: v })} />
      <Toggle label="Show Betas & Alphas" description="Include old beta and alpha versions (no Fabric support)"
        value={settings.showBetas} onChange={v => save({ showBetas: v })} />

      {/* ── Launcher ── */}
      <div style={{ fontSize: "11px", fontWeight: "700", color: "var(--pink)", textTransform: "uppercase", letterSpacing: "1.5px", marginBottom: "8px", marginTop: "20px" }}>Launcher</div>

      <Toggle label="Close on Launch" description="Close the launcher when Minecraft starts"
        value={settings.closeOnLaunch} onChange={v => save({ closeOnLaunch: v })} />
      <Toggle label="Discord Rich Presence" description="Show Pulsar Client status on your Discord profile"
        value={settings.discordRpc} onChange={v => save({ discordRpc: v })} />
      <Toggle label="Reduced Motion" description="Disable animations for better performance"
        value={settings.reducedMotion} onChange={v => save({ reducedMotion: v })} />

      {/* ── Accessibility ── */}
      <div style={{ fontSize: "11px", fontWeight: "700", color: "var(--pink)", textTransform: "uppercase", letterSpacing: "1.5px", marginBottom: "8px", marginTop: "20px" }}>Accessibility</div>

      <div className="pulsar-card" style={{ padding: "18px 20px", marginBottom: "10px" }}>
        <label style={{ fontSize: "13px", fontWeight: "600", color: "var(--text)", display: "block", marginBottom: "8px" }}>
          Colorblind Mode
        </label>
        <select className="pulsar-select" value={settings.colorblindMode}
          onChange={e => save({ colorblindMode: e.target.value })}
          style={{ width: "100%" }}
        >
          <option value="none">None</option>
          <option value="deuteranopia">Deuteranopia (Red-Green)</option>
          <option value="protanopia">Protanopia (Red-Green)</option>
          <option value="tritanopia">Tritanopia (Blue-Yellow)</option>
        </select>
        <div style={{ fontSize: "11px", color: "var(--text-faint)", marginTop: "4px" }}>Remap accent colors for better visibility.</div>
      </div>

      <Toggle label="Streamer Mode" description="Hide personal info (username, IP in F3)"
        value={settings.streamerMode} onChange={v => save({ streamerMode: v })} />

      {/* ── Maintenance ── */}
      <div style={{ fontSize: "11px", fontWeight: "700", color: "var(--pink)", textTransform: "uppercase", letterSpacing: "1.5px", marginBottom: "8px", marginTop: "20px" }}>Maintenance</div>

      <div className="pulsar-card" style={{ padding: "18px 20px", marginBottom: "10px" }}>
        <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
          <div>
            <div style={{ fontSize: "13px", fontWeight: "600", color: "var(--text)" }}>Repair Game Files</div>
            <div style={{ fontSize: "11px", color: "var(--text-faint)", marginTop: "2px" }}>Re-download version data, natives, and Pulsar mods</div>
          </div>
          <button className="pulsar-btn-ghost" style={{ fontSize: "11px", padding: "6px 14px" }}
            onClick={async () => {
              try {
                const result = await invoke<string>("repair_game", { mcVersion: "1.21.11" });
                alert(result);
              } catch (e) { alert("Repair failed: " + e); }
            }}>
            Repair
          </button>
        </div>
      </div>

      {/* About */}
      <div className="pulsar-card" style={{ padding: "20px", marginTop: "24px", marginBottom: "32px" }}>
        <div style={{ display: "flex", alignItems: "center", gap: "12px", marginBottom: "12px" }}>
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="10" fill="#000000"/>
            <circle cx="12" cy="12" r="8" fill="none" stroke="#ffffff" strokeWidth="0.5" opacity="0.3"/>
            <ellipse cx="12" cy="12" rx="11" ry="4" fill="none" stroke="#ffffff" strokeWidth="1.5" opacity="0.6"/>
            <ellipse cx="12" cy="12" rx="9" ry="3" fill="none" stroke="#A0A0A0" strokeWidth="0.8" opacity="0.4"/>
            <circle cx="12" cy="12" r="4" fill="#000000"/>
            <circle cx="12" cy="12" r="3" fill="none" stroke="#ffffff" strokeWidth="0.3" opacity="0.5"/>
          </svg>
          <div>
            <div style={{ fontSize: "15px", fontWeight: "800", color: "var(--pink-light)" }}>Pulsar Client</div>
            <div style={{ fontSize: "11px", color: "var(--text-dim)" }}>v2.1.0</div>
          </div>
        </div>
        <div style={{ fontSize: "12px", color: "var(--text-dim)", lineHeight: 1.6 }}>
          Free Minecraft PvP client with 51+ mods, animated capes, and FPS boost.
        </div>
        <div style={{ fontSize: "11px", color: "var(--text-faint)", marginTop: "8px" }}>
          github.com/ericpnag/pulsar
        </div>
      </div>
    </div>
  );
}
