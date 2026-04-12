import { useState, useEffect } from "react";
import { invoke } from "@tauri-apps/api/core";

interface Props {
  versions: string[];
  selectedVersion: string;
}

export function InstalledModsPage({ versions, selectedVersion }: Props) {
  const [mods, setMods] = useState<string[]>([]);
  const [ver, setVer] = useState(selectedVersion);
  const [removing, setRemoving] = useState<Record<string, boolean>>({});

  useEffect(() => { loadMods(); }, [ver]);

  async function loadMods() {
    try {
      const list = await invoke<string[]>("list_installed_mods", { mcVersion: ver });
      setMods(list);
    } catch { setMods([]); }
  }

  async function removeMod(filename: string) {
    setRemoving(prev => ({ ...prev, [filename]: true }));
    try {
      await invoke("uninstall_mod", { filename, mcVersion: ver });
      setMods(prev => prev.filter(m => m !== filename));
    } catch (e) { console.error(e); }
    setRemoving(prev => ({ ...prev, [filename]: false }));
  }

  return (
    <div className="fade-in" style={{ display: "flex", flexDirection: "column", height: "100%", padding: "28px", gap: "16px", overflowY: "auto" }}>
      <div>
        <h2 className="page-title">Installed Mods</h2>
        <p className="page-subtitle">Manage mods for each version</p>
      </div>

      <select className="bloom-select" value={ver} onChange={e => setVer(e.target.value)} style={{ width: "160px" }}>
        {versions.map(v => <option key={v} value={v}>{v}</option>)}
      </select>

      <div style={{ display: "flex", flexDirection: "column", gap: "6px" }}>
        {mods.length === 0 && (
          <div className="bloom-empty">No mods installed for {ver}</div>
        )}
        {mods.map(mod => {
          const isCore = mod.includes("bloom-core");
          return (
            <div key={mod} className="bloom-list-item">
              <div className="bloom-icon-placeholder">
                {isCore ? (
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                    <circle cx="12" cy="12" r="5" fill="#7AA2F7" opacity="0.3"/>
                    <circle cx="12" cy="12" r="3" fill="#89B4FA" opacity="0.7"/>
                    <circle cx="12" cy="12" r="1.5" fill="#B4BEFE" opacity="0.9"/>
                  </svg>
                ) : (
                  <span style={{ fontSize: "14px" }}>+</span>
                )}
              </div>
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ fontWeight: "600", fontSize: "13px", color: isCore ? "var(--pink-light)" : "var(--text)" }}>
                  {mod.replace(".jar", "").replace(/-\d+\.\d+\.\d+/, "")}
                </div>
                <div style={{ fontSize: "11px", color: "var(--text-faint)" }}>{mod}</div>
              </div>
              {isCore ? (
                <span style={{ fontSize: "11px", color: "var(--text-faint)", padding: "4px 10px" }}>Core</span>
              ) : (
                <button
                  className="bloom-btn-ghost"
                  onClick={() => removeMod(mod)}
                  disabled={removing[mod]}
                  style={{
                    borderColor: "rgba(255,80,80,0.15)", color: "var(--red)",
                    fontSize: "11px", padding: "5px 12px",
                  }}
                >
                  {removing[mod] ? "..." : "Remove"}
                </button>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}
