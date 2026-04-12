import { useState, useEffect, useRef } from "react";
import { invoke } from "@tauri-apps/api/core";

interface Props {
  versions: string[];
  selectedVersion: string;
}

// Cache: hash → icon_url (null = not on Modrinth)
const hashIconCache: Record<string, string | null> = {};

async function fetchIconsForHashes(
  hashMap: Record<string, string> // filename → sha1
): Promise<Record<string, string | null>> {
  const hashes = Object.values(hashMap);
  if (hashes.length === 0) return {};

  // Step 1: batch hash lookup → { hash: { project_id } }
  let versionFiles: Record<string, { project_id: string }> = {};
  try {
    const res = await fetch("https://api.modrinth.com/v2/version_files", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "User-Agent": "PulsarClient/1.0",
      },
      body: JSON.stringify({ hashes, algorithm: "sha1" }),
    });
    if (res.ok) versionFiles = await res.json();
  } catch {}

  // Collect unique project IDs
  const projectIds = [...new Set(Object.values(versionFiles).map(v => v.project_id))];
  if (projectIds.length === 0) {
    // Nothing matched — mark all as null
    const result: Record<string, string | null> = {};
    for (const [filename] of Object.entries(hashMap)) result[filename] = null;
    return result;
  }

  // Step 2: batch project info → icon_url
  let projects: { id: string; icon_url?: string }[] = [];
  try {
    const res = await fetch(
      `https://api.modrinth.com/v2/projects?ids=${encodeURIComponent(JSON.stringify(projectIds))}`,
      { headers: { "User-Agent": "PulsarClient/1.0" } }
    );
    if (res.ok) projects = await res.json();
  } catch {}

  const projectIconMap: Record<string, string | null> = {};
  for (const p of projects) projectIconMap[p.id] = p.icon_url ?? null;

  // Build filename → icon_url map
  const result: Record<string, string | null> = {};
  for (const [filename, hash] of Object.entries(hashMap)) {
    const projectId = versionFiles[hash]?.project_id;
    result[filename] = projectId ? (projectIconMap[projectId] ?? null) : null;
    hashIconCache[hash] = result[filename];
  }
  return result;
}

function ModIcon({ iconUrl }: { filename?: string; iconUrl: string | null | undefined; isCore: boolean }) {
  if (iconUrl === undefined) {
    return (
      <div style={{
        width: "28px", height: "28px", borderRadius: "6px",
        background: "rgba(255,255,255,0.07)",
        animation: "pulse-glow 2s ease-in-out infinite",
      }} />
    );
  }

  if (iconUrl) {
    return (
      <img
        src={iconUrl}
        alt=""
        width={28}
        height={28}
        style={{ borderRadius: "6px", objectFit: "cover", display: "block" }}
      />
    );
  }

  // Fallback: generic cube/mod icon
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" style={{ opacity: 0.3 }}>
      <path d="M12 2L2 7v10l10 5 10-5V7L12 2z" stroke="#fff" strokeWidth="1.5" strokeLinejoin="round"/>
      <path d="M2 7l10 5 10-5" stroke="#fff" strokeWidth="1.5"/>
      <path d="M12 12v10" stroke="#fff" strokeWidth="1.5"/>
    </svg>
  );
}

function CoreIcon() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
      <circle cx="12" cy="12" r="10" fill="#000"/>
      <circle cx="12" cy="12" r="8" fill="none" stroke="#fff" strokeWidth="0.5" opacity="0.3"/>
      <ellipse cx="12" cy="12" rx="11" ry="4" fill="none" stroke="#fff" strokeWidth="1.5" opacity="0.6"/>
      <circle cx="12" cy="12" r="4" fill="#000"/>
      <circle cx="12" cy="12" r="3" fill="none" stroke="#fff" strokeWidth="0.3" opacity="0.5"/>
    </svg>
  );
}

export function InstalledModsPage({ versions, selectedVersion }: Props) {
  const [mods, setMods] = useState<string[]>([]);
  const [ver, setVer] = useState(selectedVersion);
  const [removing, setRemoving] = useState<Record<string, boolean>>({});
  const [icons, setIcons] = useState<Record<string, string | null | undefined>>({});
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

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
    const nonCoreMods = modList.filter(m => !m.includes("bloom-core"));
    if (nonCoreMods.length === 0) return;

    let hashMap: Record<string, string> = {};
    try {
      hashMap = await invoke<Record<string, string>>("get_mod_hashes", { mcVersion: ver });
    } catch { return; }

    // Only look up mods we don't have cached yet
    const uncached: Record<string, string> = {};
    const preloaded: Record<string, string | null> = {};
    for (const filename of nonCoreMods) {
      const hash = hashMap[filename];
      if (!hash) continue;
      if (hash in hashIconCache) {
        preloaded[filename] = hashIconCache[hash];
      } else {
        uncached[filename] = hash;
      }
    }

    // Apply cached results immediately
    if (Object.keys(preloaded).length > 0) {
      setIcons(prev => ({ ...prev, ...preloaded }));
    }

    // Fetch the rest
    if (Object.keys(uncached).length > 0) {
      const fetched = await fetchIconsForHashes(uncached);
      setIcons(prev => ({ ...prev, ...fetched }));
    }
  }

  async function removeMod(filename: string) {
    setRemoving(prev => ({ ...prev, [filename]: true }));
    try {
      await invoke("uninstall_mod", { filename, mcVersion: ver });
      setMods(prev => prev.filter(m => m !== filename));
    } catch (e) { console.error(e); }
    setRemoving(prev => ({ ...prev, [filename]: false }));
  }

  // Close dropdown when clicking outside
  useEffect(() => {
    function handleClick(e: MouseEvent) {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
        setDropdownOpen(false);
      }
    }
    document.addEventListener("mousedown", handleClick);
    return () => document.removeEventListener("mousedown", handleClick);
  }, []);

  return (
    <div className="fade-in" style={{ display: "flex", flexDirection: "column", height: "100%", padding: "28px", gap: "16px", overflowY: "auto" }}>
      {/* Header row */}
      <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
        <div>
          <h2 className="page-title" style={{ marginBottom: "2px" }}>Installed Mods</h2>
          <p className="page-subtitle" style={{ margin: 0 }}>
            {mods.length > 0 ? `${mods.length} mod${mods.length !== 1 ? "s" : ""}` : "No mods installed"}
          </p>
        </div>

        {/* Custom version picker */}
        <div ref={dropdownRef} style={{ position: "relative" }}>
          <button
            onClick={() => setDropdownOpen(o => !o)}
            style={{
              display: "flex", alignItems: "center", gap: "10px",
              background: dropdownOpen ? "rgba(255,255,255,0.08)" : "rgba(255,255,255,0.04)",
              border: `1px solid ${dropdownOpen ? "rgba(255,255,255,0.18)" : "rgba(255,255,255,0.08)"}`,
              borderRadius: "10px", padding: "9px 14px",
              color: "#fff", cursor: "pointer", fontFamily: "inherit",
              transition: "all 0.15s",
              fontSize: "13px", fontWeight: "600",
              minWidth: "140px", justifyContent: "space-between",
            }}
            onMouseEnter={e => { if (!dropdownOpen) { e.currentTarget.style.background = "rgba(255,255,255,0.07)"; e.currentTarget.style.borderColor = "rgba(255,255,255,0.14)"; }}}
            onMouseLeave={e => { if (!dropdownOpen) { e.currentTarget.style.background = "rgba(255,255,255,0.04)"; e.currentTarget.style.borderColor = "rgba(255,255,255,0.08)"; }}}
          >
            <span style={{ display: "flex", alignItems: "center", gap: "8px" }}>
              {/* Minecraft grass block icon */}
              <svg width="14" height="14" viewBox="0 0 16 16" fill="none" style={{ opacity: 0.7 }}>
                <rect x="1" y="5" width="14" height="10" rx="1" fill="#7b5c3c"/>
                <rect x="1" y="1" width="14" height="5" rx="1" fill="#4a8c3f"/>
                <rect x="1" y="4" width="14" height="3" fill="#5fa34a"/>
              </svg>
              {ver}
            </span>
            <svg width="10" height="10" viewBox="0 0 10 10" fill="none"
              style={{ opacity: 0.5, transform: dropdownOpen ? "rotate(180deg)" : "rotate(0deg)", transition: "transform 0.15s" }}>
              <path d="M2 3.5L5 6.5L8 3.5" stroke="white" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
          </button>

          {dropdownOpen && (
            <div style={{
              position: "absolute", top: "calc(100% + 6px)", right: 0,
              background: "rgba(10,10,10,0.97)", border: "1px solid rgba(255,255,255,0.1)",
              borderRadius: "10px", padding: "4px",
              minWidth: "140px", zIndex: 50,
              boxShadow: "0 8px 32px rgba(0,0,0,0.6)",
              backdropFilter: "blur(12px)",
            }}>
              {versions.map(v => (
                <button key={v} onClick={() => { setVer(v); setDropdownOpen(false); }} style={{
                  display: "flex", alignItems: "center", justifyContent: "space-between",
                  width: "100%", padding: "8px 12px", borderRadius: "7px",
                  background: v === ver ? "rgba(255,255,255,0.08)" : "transparent",
                  border: "none", color: v === ver ? "#fff" : "var(--text-secondary)",
                  fontSize: "13px", fontWeight: v === ver ? "700" : "500",
                  cursor: "pointer", fontFamily: "inherit", textAlign: "left",
                  transition: "all 0.1s",
                }}
                onMouseEnter={e => { if (v !== ver) e.currentTarget.style.background = "rgba(255,255,255,0.05)"; }}
                onMouseLeave={e => { if (v !== ver) e.currentTarget.style.background = "transparent"; }}
                >
                  {v}
                  {v === ver && (
                    <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
                      <path d="M2 6l3 3 5-5" stroke="white" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
                    </svg>
                  )}
                </button>
              ))}
            </div>
          )}
        </div>
      </div>

      <div style={{ display: "flex", flexDirection: "column", gap: "6px" }}>
        {mods.length === 0 && (
          <div className="bloom-empty">No mods installed for {ver}</div>
        )}
        {mods.map(mod => {
          const isCore = mod.includes("bloom-core");
          const displayName = mod
            .replace(/\.jar$/i, "")
            .replace(/-mc\d[\d.]*.*$/i, "")
            .replace(/\+mc\d[\d.]*.*$/i, "")
            .replace(/-\d[\d.]*.*$/, "")
            .replace(/-/g, " ");

          return (
            <div key={mod} className="bloom-list-item">
              <div style={{
                width: "36px", height: "36px", flexShrink: 0,
                display: "flex", alignItems: "center", justifyContent: "center",
                background: "rgba(255,255,255,0.04)", borderRadius: "8px",
                border: "1px solid rgba(255,255,255,0.06)",
                overflow: "hidden",
              }}>
                {isCore
                  ? <CoreIcon />
                  : <ModIcon filename={mod} iconUrl={icons[mod]} isCore={false} />
                }
              </div>
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ fontWeight: "600", fontSize: "13px", color: "var(--text-primary)", textTransform: "capitalize" }}>
                  {displayName}
                </div>
                <div style={{ fontSize: "11px", color: "var(--text-faint)", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                  {mod}
                </div>
              </div>
              {isCore ? (
                <span style={{ fontSize: "11px", color: "var(--text-faint)", padding: "4px 10px" }}>Core</span>
              ) : (
                <button
                  className="bloom-btn-ghost"
                  onClick={() => removeMod(mod)}
                  disabled={removing[mod]}
                  style={{ borderColor: "rgba(255,80,80,0.15)", color: "var(--accent-red)", fontSize: "11px", padding: "5px 12px" }}
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
