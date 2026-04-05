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
    <div style={{ display: "flex", flexDirection: "column", height: "100%", padding: "28px", gap: "16px", overflowY: "auto" }}>
      <div>
        <h2 style={{ margin: "0 0 4px", fontSize: "20px", fontWeight: "700", color: "#FFD1DC" }}>Installed Mods</h2>
        <p style={{ margin: 0, fontSize: "13px", color: "#776070" }}>Manage mods for each version</p>
      </div>

      <select value={ver} onChange={e => setVer(e.target.value)} style={{
        background: "rgba(255,255,255,0.04)", border: "1px solid rgba(255,176,192,0.12)",
        color: "#fff", borderRadius: "6px", padding: "8px 12px", fontSize: "13px",
        outline: "none", width: "160px",
      }}>
        {versions.map(v => <option key={v} value={v} style={{ background: "#0d0810" }}>{v}</option>)}
      </select>

      <div style={{ display: "flex", flexDirection: "column", gap: "6px" }}>
        {mods.length === 0 && (
          <div style={{ textAlign: "center", padding: "48px", color: "#554455", fontSize: "13px" }}>
            No mods installed for {ver}
          </div>
        )}
        {mods.map(mod => (
          <div key={mod} style={{
            display: "flex", alignItems: "center", gap: "12px",
            background: "rgba(255,255,255,0.03)", border: "1px solid rgba(255,176,192,0.08)",
            borderRadius: "8px", padding: "10px 16px",
          }}>
            <div style={{
              width: "36px", height: "36px", borderRadius: "6px",
              background: "rgba(255,176,192,0.08)",
              display: "flex", alignItems: "center", justifyContent: "center",
              fontSize: "16px", color: "#FFB7C9",
            }}>⊞</div>
            <div style={{ flex: 1, minWidth: 0 }}>
              <div style={{ fontWeight: "600", fontSize: "13px", color: "#caa" }}>{mod.replace(".jar", "")}</div>
              <div style={{ fontSize: "11px", color: "#554455" }}>{mod}</div>
            </div>
            <button onClick={() => removeMod(mod)} disabled={removing[mod] || mod.includes("bloom-core")}
              style={{
                background: mod.includes("bloom-core") ? "transparent" : "rgba(255,80,80,0.1)",
                border: `1px solid ${mod.includes("bloom-core") ? "rgba(255,176,192,0.1)" : "rgba(255,80,80,0.2)"}`,
                color: mod.includes("bloom-core") ? "#554455" : "#FF8888",
                borderRadius: "6px", padding: "6px 12px", fontSize: "11px",
                cursor: mod.includes("bloom-core") ? "default" : "pointer",
              }}
            >
              {mod.includes("bloom-core") ? "Core" : removing[mod] ? "..." : "Remove"}
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}
