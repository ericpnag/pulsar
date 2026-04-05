import { useState } from "react";
import { invoke } from "@tauri-apps/api/core";
import axios from "axios";

interface Mod { slug: string; project_id: string; title: string; description: string; downloads: number; icon_url?: string; author: string; }

interface Props {
  versions: string[];
  selectedVersion: string;
  onVersionChange: (v: string) => void;
}

export function ModStorePage({ versions, selectedVersion, onVersionChange: _onVersionChange }: Props) {
  const [query, setQuery] = useState("");
  const [results, setResults] = useState<Mod[]>([]);
  const [loading, setLoading] = useState(false);
  const [loaded, setLoaded] = useState(false);
  const [searchVersion, setSearchVersion] = useState(selectedVersion);
  const [installing, setInstalling] = useState<Record<string, "loading" | "done" | "error">>({});

  async function search(q?: string, ver?: string) {
    setLoading(true);
    const v = ver ?? searchVersion;
    try {
      const res = await axios.get("https://api.modrinth.com/v2/search", {
        params: {
          query: q ?? query,
          facets: JSON.stringify([["project_type:mod"],[`versions:${v}`],["categories:fabric"]]),
          limit: 20,
          index: "downloads",
        },
      });
      setResults(res.data.hits);
      setLoaded(true);
    } finally {
      setLoading(false);
    }
  }

  if (!loaded && !loading) { search(""); }

  async function handleInstall(mod: Mod) {
    setInstalling(prev => ({ ...prev, [mod.project_id]: "loading" }));
    try {
      await invoke("install_mod", { projectId: mod.project_id, mcVersion: searchVersion });
      setInstalling(prev => ({ ...prev, [mod.project_id]: "done" }));
    } catch (e) {
      console.error("Install failed:", e);
      setInstalling(prev => ({ ...prev, [mod.project_id]: "error" }));
    }
  }

  function installLabel(id: string) {
    const s = installing[id];
    if (s === "loading") return "Installing...";
    if (s === "done") return "Installed";
    if (s === "error") return "Failed";
    return "Install";
  }

  return (
    <div style={{ display: "flex", flexDirection: "column", height: "100%", padding: "28px", gap: "16px", overflowY: "auto" }}>
      <div>
        <h2 style={{ margin: "0 0 4px", fontSize: "20px", fontWeight: "700" }}>Mod Store</h2>
        <p style={{ margin: 0, fontSize: "13px", color: "#555" }}>Browse Fabric mods from Modrinth</p>
      </div>

      <div style={{ display: "flex", gap: "8px" }}>
        <select
          value={searchVersion}
          onChange={e => { setSearchVersion(e.target.value); setLoaded(false); setInstalling({}); }}
          style={{
            background: "#161616", border: "1px solid #222", color: "#fff",
            borderRadius: "6px", padding: "10px 12px", fontSize: "13px", outline: "none",
          }}
        >
          {versions.map(v => <option key={v} value={v}>{v}</option>)}
        </select>
        <input
          value={query}
          onChange={e => setQuery(e.target.value)}
          onKeyDown={e => e.key === "Enter" && search(query)}
          placeholder="Search mods..."
          style={{
            flex: 1, background: "#161616", border: "1px solid #222", color: "#fff",
            borderRadius: "6px", padding: "10px 14px", fontSize: "13px", outline: "none",
          }}
        />
        <button onClick={() => search(query)} disabled={loading} style={{
          background: "#fff", color: "#000", border: "none", borderRadius: "6px",
          padding: "10px 20px", fontSize: "13px", fontWeight: "600", cursor: "pointer",
          opacity: loading ? 0.6 : 1,
        }}>
          {loading ? "..." : "Search"}
        </button>
      </div>

      <div style={{ display: "flex", flexDirection: "column", gap: "8px" }}>
        {results.map(mod => {
          const status = installing[mod.project_id];
          return (
            <div key={mod.project_id} style={{
              display: "flex", alignItems: "center", gap: "12px",
              background: "#161616", border: "1px solid #222", borderRadius: "8px", padding: "12px 16px",
            }}>
              {mod.icon_url
                ? <img src={mod.icon_url} alt="" style={{ width: "40px", height: "40px", borderRadius: "6px", objectFit: "cover" }} />
                : <div style={{ width: "40px", height: "40px", borderRadius: "6px", background: "#222", flexShrink: 0 }} />
              }
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ fontWeight: "600", fontSize: "14px", marginBottom: "2px" }}>{mod.title}</div>
                <div style={{ fontSize: "12px", color: "#555", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{mod.description}</div>
              </div>
              <div style={{ fontSize: "12px", color: "#444", flexShrink: 0 }}>{(mod.downloads / 1000).toFixed(0)}k</div>
              <button
                onClick={() => handleInstall(mod)}
                disabled={status === "loading" || status === "done"}
                style={{
                  background: status === "done" ? "#1a3a1a" : "transparent",
                  border: `1px solid ${status === "done" ? "#2a5a2a" : status === "error" ? "#5a2a2a" : "#333"}`,
                  color: status === "done" ? "#5f5" : status === "error" ? "#f55" : "#888",
                  borderRadius: "6px", padding: "6px 14px", fontSize: "12px",
                  cursor: status === "loading" || status === "done" ? "default" : "pointer",
                  flexShrink: 0, opacity: status === "loading" ? 0.6 : 1,
                }}
                onMouseEnter={e => { if (!status) { e.currentTarget.style.borderColor = "#fff"; e.currentTarget.style.color = "#fff"; } }}
                onMouseLeave={e => { if (!status) { e.currentTarget.style.borderColor = "#333"; e.currentTarget.style.color = "#888"; } }}
              >
                {installLabel(mod.project_id)}
              </button>
            </div>
          );
        })}
        {results.length === 0 && !loading && loaded && (
          <div style={{ textAlign: "center", padding: "48px", color: "#333", fontSize: "13px" }}>
            No mods found
          </div>
        )}
      </div>
    </div>
  );
}
