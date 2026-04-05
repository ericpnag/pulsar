import { useState } from "react";
import { invoke } from "@tauri-apps/api/core";
import axios from "axios";

interface Shader { project_id: string; title: string; description: string; downloads: number; icon_url?: string; }

interface Props {
  versions: string[];
  selectedVersion: string;
  onVersionChange: (v: string) => void;
}

export function ShadersPage({ versions, selectedVersion }: Props) {
  const [query, setQuery] = useState("");
  const [results, setResults] = useState<Shader[]>([]);
  const [loading, setLoading] = useState(false);
  const [loaded, setLoaded] = useState(false);
  const [searchVersion, setSearchVersion] = useState(selectedVersion);
  const [installing, setInstalling] = useState<Record<string, "loading" | "done" | "error">>({});

  async function search(q?: string) {
    setLoading(true);
    try {
      const res = await axios.get("https://api.modrinth.com/v2/search", {
        params: {
          query: q ?? query,
          facets: JSON.stringify([["project_type:shader"],[`versions:${searchVersion}`]]),
          limit: 20, index: "downloads",
        },
      });
      setResults(res.data.hits);
      setLoaded(true);
    } finally { setLoading(false); }
  }

  if (!loaded && !loading) search("");

  async function handleInstall(shader: Shader) {
    setInstalling(prev => ({ ...prev, [shader.project_id]: "loading" }));
    try {
      await invoke("install_mod", { projectId: shader.project_id, mcVersion: searchVersion });
      setInstalling(prev => ({ ...prev, [shader.project_id]: "done" }));
    } catch {
      setInstalling(prev => ({ ...prev, [shader.project_id]: "error" }));
    }
  }

  function label(id: string) {
    const s = installing[id];
    if (s === "loading") return "Installing...";
    if (s === "done") return "Installed";
    if (s === "error") return "Failed";
    return "Install";
  }

  return (
    <div style={{ display: "flex", flexDirection: "column", height: "100%", padding: "28px", gap: "16px", overflowY: "auto" }}>
      <div>
        <h2 style={{ margin: "0 0 4px", fontSize: "20px", fontWeight: "700", color: "#FFD1DC" }}>Shader Packs</h2>
        <p style={{ margin: 0, fontSize: "13px", color: "#776070" }}>Browse shaders from Modrinth</p>
      </div>

      <div style={{ display: "flex", gap: "8px" }}>
        <select value={searchVersion}
          onChange={e => { setSearchVersion(e.target.value); setLoaded(false); setInstalling({}); }}
          style={{
            background: "rgba(255,255,255,0.04)", border: "1px solid rgba(255,176,192,0.12)",
            color: "#fff", borderRadius: "6px", padding: "10px 12px", fontSize: "13px", outline: "none",
          }}>
          {versions.map(v => <option key={v} value={v} style={{ background: "#0d0810" }}>{v}</option>)}
        </select>
        <input value={query} onChange={e => setQuery(e.target.value)}
          onKeyDown={e => e.key === "Enter" && search(query)}
          placeholder="Search shaders..." style={{
            flex: 1, background: "rgba(255,255,255,0.04)", border: "1px solid rgba(255,176,192,0.12)",
            color: "#fff", borderRadius: "6px", padding: "10px 14px", fontSize: "13px", outline: "none",
          }}
        />
        <button onClick={() => search(query)} disabled={loading} style={{
          background: "linear-gradient(135deg, #FFB7C9, #F8A4B8)", color: "#1a0f1a",
          border: "none", borderRadius: "6px", padding: "10px 20px", fontSize: "13px",
          fontWeight: "600", cursor: "pointer", opacity: loading ? 0.6 : 1,
        }}>
          {loading ? "..." : "Search"}
        </button>
      </div>

      <div style={{ display: "flex", flexDirection: "column", gap: "8px" }}>
        {results.map(s => {
          const status = installing[s.project_id];
          return (
            <div key={s.project_id} style={{
              display: "flex", alignItems: "center", gap: "12px",
              background: "rgba(255,255,255,0.03)", border: "1px solid rgba(255,176,192,0.08)",
              borderRadius: "8px", padding: "12px 16px",
            }}>
              {s.icon_url
                ? <img src={s.icon_url} alt="" style={{ width: "40px", height: "40px", borderRadius: "6px", objectFit: "cover" }} />
                : <div style={{ width: "40px", height: "40px", borderRadius: "6px", background: "rgba(255,176,192,0.08)", flexShrink: 0 }} />
              }
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ fontWeight: "600", fontSize: "14px", color: "#caa", marginBottom: "2px" }}>{s.title}</div>
                <div style={{ fontSize: "12px", color: "#776070", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{s.description}</div>
              </div>
              <div style={{ fontSize: "12px", color: "#554455", flexShrink: 0 }}>{(s.downloads / 1000).toFixed(0)}k</div>
              <button onClick={() => handleInstall(s)} disabled={status === "loading" || status === "done"}
                style={{
                  background: status === "done" ? "rgba(85,221,136,0.1)" : "transparent",
                  border: `1px solid ${status === "done" ? "rgba(85,221,136,0.3)" : status === "error" ? "rgba(255,80,80,0.3)" : "rgba(255,176,192,0.15)"}`,
                  color: status === "done" ? "#55DD88" : status === "error" ? "#FF8888" : "#998899",
                  borderRadius: "6px", padding: "6px 14px", fontSize: "12px",
                  cursor: status === "loading" || status === "done" ? "default" : "pointer", flexShrink: 0,
                }}
              >{label(s.project_id)}</button>
            </div>
          );
        })}
        {results.length === 0 && !loading && loaded && (
          <div style={{ textAlign: "center", padding: "48px", color: "#554455", fontSize: "13px" }}>No shaders found</div>
        )}
      </div>
    </div>
  );
}
