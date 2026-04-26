import { useState } from "react";
import { invoke } from "@tauri-apps/api/core";
import axios from "axios";

interface Mod { slug: string; project_id: string; title: string; description: string; downloads: number; icon_url?: string; author: string; categories?: string[]; }

interface Props {
  versions: string[];
  selectedVersion: string;
  onVersionChange: (v: string) => void;
}

const CATEGORIES = [
  { name: "All", icon: '<rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/>' },
  { name: "Performance", icon: '<polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/>' },
  { name: "Technology", icon: '<rect x="4" y="4" width="16" height="16" rx="2"/><rect x="9" y="9" width="6" height="6"/><line x1="9" y1="2" x2="9" y2="4"/><line x1="15" y1="2" x2="15" y2="4"/>' },
  { name: "World Gen", icon: '<circle cx="12" cy="12" r="10"/><path d="M2 12h20"/>' },
  { name: "Social", icon: '<path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>' },
  { name: "Cosmetic", icon: '<path d="M12 2l3 7h7l-5.5 4.5L18 21l-6-4-6 4 1.5-7.5L2 9h7z"/>' },
  { name: "Library", icon: '<circle cx="12" cy="12" r="3"/><path d="M3 12a9 9 0 0 1 9-9"/><path d="M21 12a9 9 0 0 1-9 9"/>' },
];

const MOD_COLORS = ["teal", "purple", "coral", "amber", "blue", "pink"];
const COLOR_MAP: Record<string, string> = {
  teal: "linear-gradient(135deg, #1F3340, #16222A)", purple: "linear-gradient(135deg, #2A1F4D, #1A1530)",
  coral: "linear-gradient(135deg, #401F2D, #2A1620)", amber: "linear-gradient(135deg, #3D2D14, #2A2010)",
  blue: "linear-gradient(135deg, #182F45, #0D1F2E)", pink: "linear-gradient(135deg, #401C30, #2A1820)",
};
const TEXT_MAP: Record<string, string> = {
  teal: "#5DCAA5", purple: "#C4B5FD", coral: "#F0997B", amber: "#FAC775", blue: "#85B7EB", pink: "#F4C0D1",
};

function formatDownloads(n: number): string {
  if (n >= 1_000_000) return (n / 1_000_000).toFixed(1) + "M";
  if (n >= 1_000) return (n / 1_000).toFixed(0) + "k";
  return String(n);
}

export function ModStorePage({ versions, selectedVersion }: Props) {
  const [query, setQuery] = useState("");
  const [results, setResults] = useState<Mod[]>([]);
  const [loading, setLoading] = useState(false);
  const [loaded, setLoaded] = useState(false);
  const [searchVersion, setSearchVersion] = useState(selectedVersion);
  const [installing, setInstalling] = useState<Record<string, "loading" | "done" | "error">>({});
  const [activeCategory, setActiveCategory] = useState("All");
  const [tab, setTab] = useState<"browse" | "installed" | "updates">("browse");
  const [importExpanded, setImportExpanded] = useState(false);
  const [importUrl, setImportUrl] = useState("");
  const [importStatus, setImportStatus] = useState<"idle" | "searching" | "found" | "not_found" | "invalid">("idle");
  const [importMod, setImportMod] = useState<Mod | null>(null);

  async function search(q?: string, ver?: string, cat?: string) {
    setLoading(true);
    const v = ver ?? searchVersion;
    const category = cat ?? activeCategory;
    const facets: string[][] = [["project_type:mod"], [`versions:${v}`], ["categories:fabric"]];
    if (category !== "All") facets.push([`categories:${category.toLowerCase().replace(" ", "-")}`]);
    try {
      const res = await axios.get("https://api.modrinth.com/v2/search", {
        params: { query: q ?? query, facets: JSON.stringify(facets), limit: 20, index: "downloads" },
      });
      setResults(res.data.hits);
      setLoaded(true);
    } finally { setLoading(false); }
  }

  if (!loaded && !loading) search("");

  async function handleImportUrl() {
    const trimmed = importUrl.trim();
    // Match CurseForge or Modrinth URLs
    const cfMatch = trimmed.match(/curseforge\.com\/minecraft\/mc-mods\/([a-z0-9_-]+)/i);
    const mrMatch = trimmed.match(/modrinth\.com\/mod\/([a-z0-9_-]+)/i);
    const slug = cfMatch?.[1] ?? mrMatch?.[1];
    if (!slug) { setImportStatus("invalid"); return; }
    setImportStatus("searching");
    setImportMod(null);
    try {
      const res = await axios.get(`https://api.modrinth.com/v2/project/${slug}`);
      const p = res.data;
      setImportMod({ slug: p.slug, project_id: p.id, title: p.title, description: p.description, downloads: p.downloads, icon_url: p.icon_url, author: p.team ?? "Unknown" });
      setImportStatus("found");
    } catch {
      setImportStatus("not_found");
    }
  }

  async function handleInstall(mod: Mod) {
    setInstalling(prev => ({ ...prev, [mod.project_id]: "loading" }));
    try {
      await invoke("install_mod", { projectId: mod.project_id, mcVersion: searchVersion });
      setInstalling(prev => ({ ...prev, [mod.project_id]: "done" }));
    } catch {
      setInstalling(prev => ({ ...prev, [mod.project_id]: "error" }));
    }
  }

  return (
    <div className="fade-in" style={{ padding: "22px 26px", height: "100%", overflowY: "auto" }}>
      {/* Header */}
      <div style={{ display: "flex", alignItems: "flex-end", justifyContent: "space-between", gap: "16px", marginBottom: "18px" }}>
        <div>
          <div style={{ fontSize: "10px", fontWeight: "500", color: "#6B6985", letterSpacing: "1.5px", textTransform: "uppercase", marginBottom: "8px" }}>Mods</div>
          <h1 style={{ fontSize: "22px", fontWeight: "500", color: "#F0EEFC", margin: "0 0 4px", letterSpacing: "-0.3px" }}>Browse mods</h1>
          <p style={{ fontSize: "13px", color: "#8A88A8", margin: 0 }}>From Modrinth, installed in one click.</p>
        </div>
        {/* Tabs */}
        <div style={{ display: "flex", gap: "4px", padding: "4px", background: "#0D0D17", border: "0.5px solid #1F1F2E", borderRadius: "8px" }}>
          {(["browse", "installed", "updates"] as const).map(t => (
            <div key={t} onClick={() => setTab(t)} style={{
              padding: "6px 14px", borderRadius: "6px", fontSize: "12px", cursor: "pointer",
              color: tab === t ? "#C4B5FD" : "#8A88A8",
              background: tab === t ? "#1A1530" : "transparent",
            }}>
              {t === "browse" ? "Browse" : t === "installed" ? "Installed" : "Updates"}
            </div>
          ))}
        </div>
      </div>

      {/* Search + Sort */}
      <div style={{ display: "grid", gridTemplateColumns: "1fr auto", gap: "8px", marginBottom: "14px" }}>
        <div style={{
          display: "flex", alignItems: "center", gap: "8px", padding: "9px 12px",
          background: "#0D0D17", border: "0.5px solid #1F1F2E", borderRadius: "8px",
        }}>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#5A5870" strokeWidth="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
          <input value={query} onChange={e => setQuery(e.target.value)}
            onKeyDown={e => e.key === "Enter" && search(query)}
            placeholder="Search Modrinth mods..."
            style={{ background: "transparent", border: 0, outline: 0, color: "#E8E6F5", fontSize: "13px", flex: 1, fontFamily: "inherit", padding: 0 }}
          />
        </div>
        <select value={searchVersion}
          onChange={e => { setSearchVersion(e.target.value); setLoaded(false); setInstalling({}); }}
          style={{
            padding: "9px 14px", background: "#0D0D17", border: "0.5px solid #1F1F2E", borderRadius: "8px",
            fontSize: "12px", color: "#C7C5DC", cursor: "pointer", outline: "none", fontFamily: "inherit",
          }}>
          {versions.map(v => <option key={v} value={v} style={{ background: "#08080F" }}>{v}</option>)}
        </select>
      </div>

      {/* Import from URL */}
      <div style={{ marginBottom: "14px" }}>
        <div
          onClick={() => setImportExpanded(!importExpanded)}
          style={{
            display: "flex", alignItems: "center", gap: "8px", padding: "8px 12px",
            background: "#0D0D17", border: "0.5px solid #1F1F2E", borderRadius: importExpanded ? "8px 8px 0 0" : "8px",
            cursor: "pointer", transition: "all 150ms",
          }}
          onMouseEnter={e => { e.currentTarget.style.borderColor = "#2A2A3E"; }}
          onMouseLeave={e => { e.currentTarget.style.borderColor = "#1F1F2E"; }}
        >
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="#5A5870" strokeWidth="2">
            <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71" />
            <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71" />
          </svg>
          <span style={{ fontSize: "12px", color: "#8A88A8", flex: 1 }}>Import from CurseForge or Modrinth URL</span>
          <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="#5A5870" strokeWidth="2"
            style={{ transform: importExpanded ? "rotate(180deg)" : "rotate(0deg)", transition: "transform 150ms" }}>
            <polyline points="6 9 12 15 18 9" />
          </svg>
        </div>
        {importExpanded && (
          <div style={{
            padding: "12px", background: "#0A0A14", border: "0.5px solid #1F1F2E", borderTop: "none",
            borderRadius: "0 0 8px 8px",
          }}>
            <div style={{ display: "flex", gap: "8px" }}>
              <input
                value={importUrl}
                onChange={e => { setImportUrl(e.target.value); setImportStatus("idle"); }}
                onKeyDown={e => e.key === "Enter" && handleImportUrl()}
                placeholder="https://www.curseforge.com/minecraft/mc-mods/sodium"
                style={{
                  flex: 1, padding: "8px 10px", background: "#0D0D17", border: "0.5px solid #1F1F2E",
                  borderRadius: "6px", color: "#E8E6F5", fontSize: "12px", fontFamily: "inherit", outline: "none",
                }}
              />
              <button
                onClick={handleImportUrl}
                disabled={importStatus === "searching"}
                style={{
                  padding: "8px 14px", borderRadius: "6px", border: "0.5px solid #5B3FA6",
                  background: "#1A1530", color: "#C4B5FD", fontSize: "12px", fontWeight: "500",
                  cursor: importStatus === "searching" ? "default" : "pointer", fontFamily: "inherit",
                }}
              >
                {importStatus === "searching" ? "..." : "Look up"}
              </button>
            </div>
            {importStatus === "invalid" && (
              <div style={{ fontSize: "11px", color: "#F0997B", marginTop: "8px" }}>
                Paste a CurseForge or Modrinth mod URL (e.g. https://www.curseforge.com/minecraft/mc-mods/sodium)
              </div>
            )}
            {importStatus === "not_found" && (
              <div style={{ fontSize: "11px", color: "#F0997B", marginTop: "8px", lineHeight: 1.5 }}>
                This mod isn't on Modrinth. Download the .jar manually from CurseForge and drop it in your mods folder.
              </div>
            )}
            {importStatus === "found" && importMod && (
              <div style={{
                marginTop: "10px", display: "flex", alignItems: "center", gap: "10px",
                padding: "10px 12px", background: "#0D0D17", border: "0.5px solid #1F1F2E", borderRadius: "8px",
              }}>
                {importMod.icon_url
                  ? <img src={importMod.icon_url} alt="" style={{ width: "36px", height: "36px", borderRadius: "8px", objectFit: "cover" }} />
                  : <div style={{ width: "36px", height: "36px", borderRadius: "8px", background: "linear-gradient(135deg, #1F3340, #16222A)", display: "flex", alignItems: "center", justifyContent: "center", color: "#5DCAA5", fontSize: "14px" }}>+</div>
                }
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ fontSize: "13px", fontWeight: "500", color: "#F0EEFC" }}>{importMod.title}</div>
                  <div style={{ fontSize: "11px", color: "#8A88A8", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{importMod.description}</div>
                </div>
                <button
                  onClick={() => handleInstall(importMod)}
                  disabled={installing[importMod.project_id] === "loading" || installing[importMod.project_id] === "done"}
                  style={{
                    padding: "6px 14px", borderRadius: "6px", fontSize: "11px", fontWeight: "500", fontFamily: "inherit",
                    cursor: installing[importMod.project_id] === "done" ? "default" : "pointer",
                    background: installing[importMod.project_id] === "done" ? "#16222A" : "#1A1530",
                    border: installing[importMod.project_id] === "done" ? "0.5px solid #1D9E75" : "0.5px solid #5B3FA6",
                    color: installing[importMod.project_id] === "done" ? "#5DCAA5" : "#C4B5FD",
                  }}
                >
                  {installing[importMod.project_id] === "done" ? "Installed" : installing[importMod.project_id] === "loading" ? "..." : "Install"}
                </button>
              </div>
            )}
          </div>
        )}
      </div>

      {/* 2-column: categories + mods */}
      <div style={{ display: "grid", gridTemplateColumns: "180px 1fr", gap: "18px" }}>
        {/* Category sidebar */}
        <div style={{ display: "flex", flexDirection: "column", gap: "16px" }}>
          <div>
            <div style={{ fontSize: "10px", fontWeight: "500", color: "#6B6985", letterSpacing: "1.2px", textTransform: "uppercase", marginBottom: "6px", padding: "0 4px" }}>Categories</div>
            <div style={{ display: "flex", flexDirection: "column", gap: "2px" }}>
              {CATEGORIES.map(cat => (
                <div key={cat.name} onClick={() => { setActiveCategory(cat.name); setLoaded(false); }}
                  style={{
                    display: "flex", alignItems: "center", gap: "10px", padding: "7px 10px", borderRadius: "7px",
                    cursor: "pointer", transition: "all 150ms",
                    background: activeCategory === cat.name ? "#1A1530" : "transparent",
                    color: activeCategory === cat.name ? "#C4B5FD" : "#C7C5DC",
                  }}
                  onMouseEnter={e => { if (activeCategory !== cat.name) e.currentTarget.style.background = "#11111C"; }}
                  onMouseLeave={e => { if (activeCategory !== cat.name) e.currentTarget.style.background = "transparent"; }}
                >
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"
                    style={{ color: activeCategory === cat.name ? "#C4B5FD" : "#5A5870", flexShrink: 0 }}
                    dangerouslySetInnerHTML={{ __html: cat.icon }} />
                  <span style={{ flex: 1, fontSize: "12px" }}>{cat.name}</span>
                </div>
              ))}
            </div>
          </div>

          {/* Sources card */}
          <div style={{ background: "#0D0D17", border: "0.5px solid #1F1F2E", borderRadius: "10px", padding: "12px 14px" }}>
            <div style={{ fontSize: "11px", color: "#6B6985", marginBottom: "8px", letterSpacing: "0.5px", textTransform: "uppercase", fontWeight: "500" }}>Sources</div>
            <div style={{ display: "flex", alignItems: "center", gap: "8px", padding: "6px 0" }}>
              <div style={{ width: "8px", height: "8px", borderRadius: "50%", background: "#5DCAA5" }} />
              <span style={{ fontSize: "12px", color: "#E8E6F5", flex: 1 }}>Modrinth</span>
              <span className="mono" style={{ fontSize: "11px", color: "#6B6985" }}>Active</span>
            </div>
            <div style={{ display: "flex", alignItems: "center", gap: "8px", padding: "6px 0", opacity: 0.45 }}>
              <div style={{ width: "8px", height: "8px", borderRadius: "50%", background: "#F87A1A" }} />
              <span style={{ fontSize: "12px", color: "#E8E6F5", flex: 1 }}>CurseForge</span>
              <span className="mono" style={{ fontSize: "10px", color: "#6B6985" }}>Soon</span>
            </div>
          </div>
        </div>

        {/* Mods grid */}
        <div>
          {/* Featured banner */}
          {results.length > 0 && (
            <div style={{
              background: "linear-gradient(135deg, #1A0F3A 0%, #0D0D1F 50%, #08080F 100%)",
              border: "0.5px solid #2A1A4D", borderRadius: "12px", padding: "16px 18px", marginBottom: "16px",
              display: "grid", gridTemplateColumns: "56px 1fr auto", gap: "14px", alignItems: "center",
              position: "relative", overflow: "hidden",
            }}>
              <div style={{ position: "absolute", right: "-50px", top: "-50px", width: "200px", height: "200px", background: "radial-gradient(circle, rgba(139,92,246,0.18) 0%, transparent 60%)", pointerEvents: "none" }} />
              {results[0].icon_url
                ? <img src={results[0].icon_url} alt="" style={{ width: "56px", height: "56px", borderRadius: "12px", objectFit: "cover", position: "relative", zIndex: 1 }} />
                : <div style={{ width: "56px", height: "56px", borderRadius: "12px", background: "linear-gradient(135deg, #5B3FA6, #2A1F4D)", display: "flex", alignItems: "center", justifyContent: "center", color: "#C4B5FD", position: "relative", zIndex: 1, fontSize: "20px" }}>★</div>
              }
              <div style={{ position: "relative", zIndex: 1 }}>
                <span style={{ display: "inline-block", fontSize: "9px", padding: "2px 6px", background: "rgba(196,181,253,0.15)", color: "#C4B5FD", borderRadius: "3px", letterSpacing: "0.5px", textTransform: "uppercase", fontWeight: "500", marginBottom: "6px" }}>Most Popular</span>
                <div style={{ fontSize: "15px", fontWeight: "500", color: "#F0EEFC", marginBottom: "2px" }}>{results[0].title}</div>
                <div style={{ fontSize: "12px", color: "#8A88A8" }}>{results[0].description.slice(0, 80)}{results[0].description.length > 80 ? "..." : ""}</div>
              </div>
              <button onClick={() => handleInstall(results[0])} style={{
                padding: "8px 16px", borderRadius: "7px", border: "0.5px solid #C4B5FD", cursor: "pointer",
                background: installing[results[0].project_id] === "done" ? "#16222A" : "linear-gradient(135deg, #7C5DC4, #5B3FA6)",
                color: installing[results[0].project_id] === "done" ? "#5DCAA5" : "#FFFFFF",
                fontSize: "12px", fontWeight: "500", fontFamily: "inherit", position: "relative", zIndex: 1,
              }}>
                {installing[results[0].project_id] === "done" ? "✓ Installed" : installing[results[0].project_id] === "loading" ? "..." : "Install"}
              </button>
            </div>
          )}

          {/* Section title */}
          <div style={{ display: "flex", alignItems: "flex-end", justifyContent: "space-between", margin: "4px 0 10px" }}>
            <h3 style={{ fontSize: "14px", fontWeight: "500", color: "#F0EEFC", margin: 0 }}>
              {loading ? "Searching..." : `Popular ${activeCategory !== "All" ? activeCategory + " " : ""}mods`}
            </h3>
            <span className="mono" style={{ fontSize: "11px", color: "#6B6985" }}>{results.length} results</span>
          </div>

          {/* Mod cards grid */}
          <div style={{ display: "grid", gridTemplateColumns: "repeat(2, 1fr)", gap: "10px" }}>
            {results.slice(loaded ? 0 : 0).map((mod, i) => {
              const color = MOD_COLORS[i % MOD_COLORS.length];
              const status = installing[mod.project_id];
              return (
                <div key={mod.project_id} style={{
                  background: "#0D0D17", border: "0.5px solid #1F1F2E", borderRadius: "10px",
                  padding: "12px 14px", cursor: "pointer", transition: "all 150ms",
                  display: "flex", flexDirection: "column", gap: "10px",
                }}
                onMouseEnter={e => { e.currentTarget.style.borderColor = "#2A2A3E"; e.currentTarget.style.background = "#11111C"; e.currentTarget.style.transform = "translateY(-1px)"; }}
                onMouseLeave={e => { e.currentTarget.style.borderColor = "#1F1F2E"; e.currentTarget.style.background = "#0D0D17"; e.currentTarget.style.transform = ""; }}
                >
                  {/* Top: icon + name */}
                  <div style={{ display: "flex", gap: "12px", alignItems: "flex-start" }}>
                    {mod.icon_url
                      ? <img src={mod.icon_url} alt="" style={{ width: "40px", height: "40px", borderRadius: "9px", objectFit: "cover", flexShrink: 0 }} />
                      : <div style={{ width: "40px", height: "40px", borderRadius: "9px", background: COLOR_MAP[color], display: "flex", alignItems: "center", justifyContent: "center", color: TEXT_MAP[color], flexShrink: 0, fontSize: "16px" }}>◆</div>
                    }
                    <div style={{ flex: 1, minWidth: 0 }}>
                      <div style={{ fontSize: "13px", fontWeight: "500", color: "#F0EEFC", marginBottom: "2px" }}>{mod.title}</div>
                      <div style={{ fontSize: "11px", color: "#8A88A8" }}>by <span style={{ color: "#C4B5FD" }}>{mod.author}</span></div>
                    </div>
                  </div>

                  {/* Description */}
                  <div style={{ fontSize: "12px", color: "#8A88A8", lineHeight: 1.45, overflow: "hidden", display: "-webkit-box", WebkitLineClamp: 2, WebkitBoxOrient: "vertical" as any }}>
                    {mod.description}
                  </div>

                  {/* Bottom: stats + install */}
                  <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", paddingTop: "10px", borderTop: "0.5px solid #1A1A28" }}>
                    <div className="mono" style={{ display: "flex", gap: "12px", fontSize: "11px", color: "#6B6985" }}>
                      <span style={{ display: "flex", alignItems: "center", gap: "4px" }}>
                        <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
                        {formatDownloads(mod.downloads)}
                      </span>
                    </div>
                    <button onClick={e => { e.stopPropagation(); handleInstall(mod); }}
                      disabled={status === "loading" || status === "done"}
                      style={{
                        padding: "5px 12px", borderRadius: "5px", fontSize: "11px", fontWeight: "500",
                        cursor: status === "done" || status === "loading" ? "default" : "pointer",
                        fontFamily: "inherit", transition: "all 150ms",
                        background: status === "done" ? "#16222A" : "#1A1530",
                        border: status === "done" ? "0.5px solid #1D9E75" : "0.5px solid #5B3FA6",
                        color: status === "done" ? "#5DCAA5" : status === "loading" ? "#8A88A8" : "#C4B5FD",
                      }}>
                      {status === "done" ? "✓ Installed" : status === "loading" ? "..." : status === "error" ? "Failed" : "Install"}
                    </button>
                  </div>
                </div>
              );
            })}
          </div>

          {results.length === 0 && !loading && loaded && (
            <div style={{ textAlign: "center", padding: "64px 20px", color: "#6B6985", fontSize: "13px" }}>No mods found</div>
          )}
        </div>
      </div>
    </div>
  );
}
