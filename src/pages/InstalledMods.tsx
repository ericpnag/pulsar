import { useState, useEffect } from "react";
import { invoke } from "@tauri-apps/api/core";

interface Props {
  versions: string[];
  selectedVersion: string;
}

const MOD_COLORS = ["teal", "purple", "coral", "amber", "blue", "pink"];
const COLOR_BG: Record<string, string> = {
  teal: "linear-gradient(135deg, #1F3340, #16222A)", purple: "linear-gradient(135deg, #2A1F4D, #1A1530)",
  coral: "linear-gradient(135deg, #401F2D, #2A1620)", amber: "linear-gradient(135deg, #3D2D14, #2A2010)",
  blue: "linear-gradient(135deg, #182F45, #0D1F2E)", pink: "linear-gradient(135deg, #401C30, #2A1820)",
};
const COLOR_FG: Record<string, string> = {
  teal: "#5DCAA5", purple: "#C4B5FD", coral: "#F0997B", amber: "#FAC775", blue: "#85B7EB", pink: "#F4C0D1",
};

export function InstalledModsPage({ versions, selectedVersion }: Props) {
  const [mods, setMods] = useState<string[]>([]);
  const [ver, setVer] = useState(selectedVersion);
  const [removing, setRemoving] = useState<Record<string, boolean>>({});
  const [icons, setIcons] = useState<Record<string, string | null | undefined>>({});
  const [filter, setFilter] = useState("");

  useEffect(() => { loadMods(); }, [ver]);

  async function loadMods() {
    setIcons({});
    try {
      const list = await invoke<string[]>("list_installed_mods", { mcVersion: ver });
      setMods(list);
      loadIcons(list);
    } catch { setMods([]); }
  }

  async function loadIcons(modList: string[]) {
    const nonCore = modList.filter(m => !m.includes("pulsar-core"));
    if (!nonCore.length) return;
    try {
      const hashMap = await invoke<Record<string, string>>("get_mod_hashes", { mcVersion: ver });
      const hashes = Object.values(hashMap);
      if (!hashes.length) return;
      const res = await fetch("https://api.modrinth.com/v2/version_files", {
        method: "POST", headers: { "Content-Type": "application/json", "User-Agent": "PulsarClient/1.0" },
        body: JSON.stringify({ hashes, algorithm: "sha1" }),
      });
      if (!res.ok) return;
      const vf: Record<string, { project_id: string }> = await res.json();
      const pids = [...new Set(Object.values(vf).map(v => v.project_id))];
      if (!pids.length) return;
      const pres = await fetch(`https://api.modrinth.com/v2/projects?ids=${encodeURIComponent(JSON.stringify(pids))}`, { headers: { "User-Agent": "PulsarClient/1.0" } });
      if (!pres.ok) return;
      const projects: { id: string; icon_url?: string }[] = await pres.json();
      const pmap: Record<string, string | null> = {};
      for (const p of projects) pmap[p.id] = p.icon_url ?? null;
      const result: Record<string, string | null> = {};
      for (const [fn, hash] of Object.entries(hashMap)) {
        result[fn] = vf[hash] ? (pmap[vf[hash].project_id] ?? null) : null;
      }
      setIcons(result);
    } catch {}
  }

  async function removeMod(filename: string) {
    setRemoving(prev => ({ ...prev, [filename]: true }));
    try {
      await invoke("uninstall_mod", { filename, mcVersion: ver });
      setMods(prev => prev.filter(m => m !== filename));
    } catch {}
    setRemoving(prev => ({ ...prev, [filename]: false }));
  }

  function displayName(filename: string): string {
    return filename.replace(/\.jar$/i, "").replace(/-mc\d[\d.]*.*$/i, "").replace(/\+mc\d[\d.]*.*$/i, "")
      .replace(/-\d[\d.]*.*$/, "").replace(/-/g, " ");
  }

  const filtered = mods.filter(m => !filter || displayName(m).toLowerCase().includes(filter.toLowerCase()));
  const coreCount = mods.filter(m => m.includes("pulsar-core")).length;

  return (
    <div className="fade-in" style={{ padding: "22px 26px", height: "100%", overflowY: "auto" }}>
      {/* Header */}
      <div style={{ display: "flex", alignItems: "flex-end", justifyContent: "space-between", gap: "16px", marginBottom: "16px" }}>
        <div>
          <div style={{ fontSize: "10px", fontWeight: "500", color: "#6B6985", letterSpacing: "1.5px", textTransform: "uppercase", marginBottom: "8px" }}>
            Mods · {ver}
          </div>
          <h1 style={{ fontSize: "22px", fontWeight: "500", color: "#F0EEFC", margin: "0 0 4px", letterSpacing: "-0.3px" }}>Installed mods</h1>
          <p style={{ fontSize: "13px", color: "#8A88A8", margin: 0 }}>
            {mods.length} mod{mods.length !== 1 ? "s" : ""} · loaded with Fabric.
          </p>
        </div>
        {/* Version selector */}
        <select value={ver} onChange={e => setVer(e.target.value)}
          style={{
            padding: "6px 12px", background: "#0D0D17", border: "0.5px solid #1F1F2E", borderRadius: "6px",
            color: "#C7C5DC", fontSize: "12px", cursor: "pointer", outline: "none", fontFamily: "inherit",
          }}>
          {versions.map(v => <option key={v} value={v} style={{ background: "#08080F" }}>{v}</option>)}
        </select>
      </div>

      {/* Search */}
      <div style={{ display: "flex", alignItems: "center", gap: "8px", padding: "9px 12px", background: "#0D0D17", border: "0.5px solid #1F1F2E", borderRadius: "8px", marginBottom: "16px" }}>
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#5A5870" strokeWidth="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
        <input value={filter} onChange={e => setFilter(e.target.value)} placeholder="Search installed mods..."
          style={{ background: "transparent", border: 0, outline: 0, color: "#E8E6F5", fontSize: "13px", flex: 1, fontFamily: "inherit", padding: 0 }} />
      </div>

      {/* Stats row */}
      <div style={{ display: "grid", gridTemplateColumns: "repeat(3, 1fr)", gap: "10px", marginBottom: "18px" }}>
        <div style={{ padding: "12px 14px", background: "#0D0D17", border: "0.5px solid #1F1F2E", borderRadius: "10px" }}>
          <div style={{ fontSize: "11px", color: "#8A88A8", marginBottom: "4px" }}>Total installed</div>
          <div className="mono" style={{ fontSize: "20px", fontWeight: "500", color: "#F0EEFC" }}>{mods.length} <span style={{ fontSize: "11px", color: "#6B6985" }}>mods</span></div>
        </div>
        <div style={{ padding: "12px 14px", background: "#0D0D17", border: "0.5px solid #1F1F2E", borderRadius: "10px" }}>
          <div style={{ fontSize: "11px", color: "#8A88A8", marginBottom: "4px" }}>Pulsar Core</div>
          <div className="mono" style={{ fontSize: "20px", fontWeight: "500", color: "#5DCAA5" }}>{coreCount > 0 ? "Active" : "—"}</div>
        </div>
        <div style={{ padding: "12px 14px", background: "#0D0D17", border: "0.5px solid #1F1F2E", borderRadius: "10px" }}>
          <div style={{ fontSize: "11px", color: "#8A88A8", marginBottom: "4px" }}>External</div>
          <div className="mono" style={{ fontSize: "20px", fontWeight: "500", color: "#C4B5FD" }}>{mods.length - coreCount}</div>
        </div>
      </div>

      {/* Mod rows */}
      <div style={{ display: "flex", flexDirection: "column", gap: "8px" }}>
        {filtered.length === 0 && (
          <div style={{ textAlign: "center", padding: "64px 20px", color: "#6B6985", fontSize: "13px" }}>
            {mods.length === 0 ? "No mods installed" : "No mods match your search"}
          </div>
        )}
        {filtered.map((mod, i) => {
          const isCore = mod.includes("pulsar-core");
          const name = displayName(mod);
          const color = MOD_COLORS[i % MOD_COLORS.length];
          const iconUrl = icons[mod];

          return (
            <div key={mod} style={{
              display: "grid", gridTemplateColumns: "36px 1fr auto", gap: "12px", alignItems: "center",
              padding: "12px 14px", background: "#0D0D17", border: "0.5px solid #1F1F2E", borderRadius: "10px",
              transition: "all 150ms", opacity: removing[mod] ? 0.4 : 1,
            }}
            onMouseEnter={e => { e.currentTarget.style.borderColor = "#2A2A3E"; e.currentTarget.style.background = "#11111C"; }}
            onMouseLeave={e => { e.currentTarget.style.borderColor = "#1F1F2E"; e.currentTarget.style.background = "#0D0D17"; }}
            >
              {/* Icon */}
              {iconUrl ? (
                <img src={iconUrl} alt="" style={{ width: "36px", height: "36px", borderRadius: "8px", objectFit: "cover" }} />
              ) : (
                <div style={{ width: "36px", height: "36px", borderRadius: "8px", background: COLOR_BG[color], display: "flex", alignItems: "center", justifyContent: "center", color: COLOR_FG[color], fontSize: "14px" }}>
                  {isCore ? "◉" : "◆"}
                </div>
              )}

              {/* Info */}
              <div style={{ minWidth: 0 }}>
                <div style={{ display: "flex", alignItems: "center", gap: "8px", marginBottom: "2px" }}>
                  <span style={{ fontSize: "13px", fontWeight: "500", color: "#F0EEFC", textTransform: "capitalize" }}>{name}</span>
                  {isCore && <span style={{ fontSize: "9px", padding: "1px 5px", borderRadius: "3px", background: "rgba(196,181,253,0.12)", color: "#C4B5FD", letterSpacing: "0.3px", fontWeight: "500" }}>CORE</span>}
                  <span style={{ fontSize: "9px", padding: "1px 5px", borderRadius: "3px", background: "rgba(93,202,165,0.12)", color: "#5DCAA5", letterSpacing: "0.3px", fontWeight: "500" }}>Modrinth</span>
                </div>
                <div style={{ fontSize: "11px", color: "#6B6985", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{mod}</div>
              </div>

              {/* Actions */}
              <div style={{ display: "flex", gap: "4px", alignItems: "center" }}>
                {!isCore && (
                  <button onClick={() => removeMod(mod)} disabled={removing[mod]}
                    style={{
                      width: "28px", height: "28px", borderRadius: "7px", background: "transparent",
                      border: "0.5px solid #1F1F2E", color: "#8A88A8", display: "flex", alignItems: "center", justifyContent: "center",
                      cursor: "pointer", transition: "all 150ms",
                    }}
                    onMouseEnter={e => { e.currentTarget.style.color = "#E24B4A"; e.currentTarget.style.borderColor = "#79271F"; }}
                    onMouseLeave={e => { e.currentTarget.style.color = "#8A88A8"; e.currentTarget.style.borderColor = "#1F1F2E"; }}
                  >
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
                  </button>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
