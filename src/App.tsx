import { useState, useEffect } from "react";
import { invoke } from "@tauri-apps/api/core";
import { listen } from "@tauri-apps/api/event";
import { LoginModal } from "./components/LoginModal";

export type Page = "play" | "mods" | "texturepacks" | "installed" | "shaders" | "shop" | "console" | "settings";
export type LaunchPhase = "idle" | "loading" | "done" | "error";

export interface LaunchState {
  phase: LaunchPhase;
  progress: number;
  status: string;
}

export interface AccountInfo {
  username: string;
  uuid: string;
  accessToken: string;
}

export default function App() {
  const [versions, setVersions] = useState<string[]>(["1.21.1"]);
  const [selectedVersion, setSelectedVersion] = useState("1.21.1");
  const [phase, setPhase] = useState("idle");
  const [progress, setProgress] = useState(0);
  const [status, setStatus] = useState("Ready");
  const [account, setAccount] = useState<AccountInfo | null>(null);
  const [loginModal, setLoginModal] = useState<{ phase: "waiting" | "code" | "error"; code?: string; url?: string; error?: string } | null>(null);
  const [page, setPage] = useState("play");

  useEffect(() => {
    invoke<any>("get_account").then(a => {
      if (a) setAccount({ username: a.username, uuid: a.uuid, accessToken: a.access_token });
    }).catch(() => {});

    invoke<string[]>("get_versions").then(v => {
      setVersions(v);
      if (v.includes("1.21.1")) setSelectedVersion("1.21.1");
      else if (v.length) setSelectedVersion(v[0]);
    }).catch(() => {});

    listen<{ url: string; code: string }>("auth_code", e => {
      setLoginModal({ phase: "code", url: e.payload.url, code: e.payload.code });
    });
    listen<any>("auth_success", e => {
      const acc = { username: e.payload.username, uuid: e.payload.uuid, accessToken: e.payload.accessToken };
      setAccount(acc);
      invoke("save_account", { account: { username: acc.username, uuid: acc.uuid, access_token: acc.accessToken, refresh_token: e.payload.refreshToken } });
      setLoginModal(null);
    });
    listen<string>("auth_error", e => {
      setLoginModal({ phase: "error", error: e.payload });
    });
  }, []);

  async function handleLaunch() {
    setPhase("loading");
    setProgress(0);
    setStatus("Starting...");

    // Don't await listen — just set it up, invoke separately
    let unlistenFn: any = null;
    listen<{ pct: number; msg: string }>("launch_progress", e => {
      setProgress(e.payload.pct);
      setStatus(e.payload.msg);
    }).then(fn => { unlistenFn = fn; }).catch(() => {});

    try {
      await invoke("launch_minecraft", {
        version: selectedVersion,
        username: account?.username ?? null,
        uuid: account?.uuid ?? null,
        accessToken: account?.accessToken ?? null,
      });
      setPhase("done");
      setStatus("Game launched!");
      setTimeout(() => { setPhase("idle"); setStatus("Ready"); if (unlistenFn) unlistenFn(); }, 3000);
    } catch (e: any) {
      setPhase("error");
      setStatus(String(e));
      if (unlistenFn) unlistenFn();
    }
  }

  async function handleLogin() {
    setLoginModal({ phase: "waiting" });
    try { await invoke("start_microsoft_login"); } catch (e: any) { setLoginModal({ phase: "error", error: String(e) }); }
  }

  // Simple tabs
  const tabs = ["play", "mods", "installed", "shaders", "texturepacks", "shop", "console", "settings"];

  return (
    <div style={{
      display: "flex", flexDirection: "column", height: "100vh", width: "100vw",
      background: "#0d0810", color: "#fff", fontFamily: "system-ui, sans-serif",
    }}>
      {/* Top bar */}
      <div style={{
        display: "flex", alignItems: "center", height: "50px",
        background: "rgba(13,8,16,0.95)", borderBottom: "1px solid rgba(255,176,192,0.1)",
        padding: "0 16px", gap: "4px", flexShrink: 0,
      }}>
        <span style={{ fontSize: "18px", marginRight: "8px" }}>🌸</span>
        <span style={{ fontWeight: "800", fontSize: "13px", color: "#FFD1DC", marginRight: "24px", letterSpacing: "0.1em" }}>BLOOM</span>

        {tabs.map(t => (
          <div key={t}
            onMouseDown={() => setPage(t)}
            style={{
              padding: "6px 12px", fontSize: "12px", cursor: "pointer",
              color: page === t ? "#FFD1DC" : "#776070",
              borderBottom: page === t ? "2px solid #FFB7C9" : "2px solid transparent",
              fontWeight: page === t ? "700" : "400",
            }}
          >{t.charAt(0).toUpperCase() + t.slice(1)}</div>
        ))}

        <div style={{ flex: 1 }} />

        {account ? (
          <div onMouseDown={() => { invoke("logout"); setAccount(null); }}
            style={{ fontSize: "12px", color: "#FFD1DC", cursor: "pointer" }}>
            {account.username} (sign out)
          </div>
        ) : (
          <div onMouseDown={handleLogin}
            style={{ fontSize: "12px", color: "#FFB7C9", cursor: "pointer", padding: "4px 12px", border: "1px solid rgba(255,176,192,0.2)", borderRadius: "6px" }}>
            Sign In
          </div>
        )}
      </div>

      {/* Content */}
      <div style={{ flex: 1, overflow: "auto" }}>
        {page === "play" && (
          <div style={{ padding: "20px", display: "flex", flexDirection: "column", gap: "12px" }}>
            {/* Banner */}
            <div style={{
              height: "180px", borderRadius: "12px",
              background: "linear-gradient(135deg, #1a1025, #2d1b3d, #4a2040)",
              border: "1px solid rgba(255,176,192,0.1)",
              display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center",
            }}>
              <div style={{ fontSize: "18px", color: "#ccc", marginBottom: "8px" }}>Minecraft {selectedVersion}</div>
              <select value={selectedVersion} onChange={e => setSelectedVersion(e.target.value)}
                style={{ background: "rgba(0,0,0,0.5)", border: "1px solid rgba(255,176,192,0.2)", color: "#fff", borderRadius: "8px", padding: "8px 20px", fontSize: "14px", cursor: "pointer", outline: "none" }}>
                {versions.map(v => <option key={v} value={v}>{v}</option>)}
              </select>
            </div>

            {/* LAUNCH BUTTON */}
            <div
              onMouseDown={() => {
                if (phase === "loading" || phase === "done") return;
                handleLaunch();
              }}
              style={{
                padding: "18px", borderRadius: "10px", textAlign: "center",
                background: phase === "loading" || phase === "done"
                  ? "rgba(255,176,192,0.15)"
                  : "linear-gradient(135deg, #FFB7C9, #F8A4B8)",
                color: phase === "loading" || phase === "done" ? "#998899" : "#1a0f1a",
                fontSize: "16px", fontWeight: "800", letterSpacing: "0.1em",
                cursor: phase === "loading" || phase === "done" ? "default" : "pointer",
                userSelect: "none",
              }}
            >
              🌸 {phase === "done" ? "MINECRAFT IS RUNNING" : phase === "loading" ? "LAUNCHING..." : phase === "error" ? "RETRY" : "LAUNCH FABRIC"}
            </div>

            {/* Progress */}
            {phase === "loading" && (
              <div>
                <div style={{ height: "4px", background: "rgba(255,255,255,0.05)", borderRadius: "2px", overflow: "hidden" }}>
                  <div style={{ height: "100%", width: `${progress}%`, background: "linear-gradient(90deg, #F8A4B8, #FFD1DC)", transition: "width 0.3s" }} />
                </div>
                <div style={{ fontSize: "12px", color: "#776070", marginTop: "4px" }}>{status}</div>
              </div>
            )}
            {phase === "error" && (
              <div style={{ fontSize: "12px", color: "#FF8888", padding: "10px", background: "rgba(255,0,0,0.1)", borderRadius: "6px" }}>{status}</div>
            )}

            {/* Info */}
            <div style={{ display: "flex", gap: "12px" }}>
              <div style={{ flex: 1, padding: "16px", borderRadius: "10px", background: "rgba(255,255,255,0.03)", border: "1px solid rgba(255,176,192,0.08)" }}>
                <div style={{ fontSize: "11px", fontWeight: "800", color: "#998899", marginBottom: "8px" }}>BLOOM CLIENT</div>
                <div style={{ fontSize: "12px", color: "#776070" }}>Right Shift for modules. 11 modules available.</div>
              </div>
              <div style={{ flex: 1, padding: "16px", borderRadius: "10px", background: "rgba(255,255,255,0.03)", border: "1px solid rgba(255,176,192,0.08)" }}>
                <div style={{ fontSize: "11px", fontWeight: "800", color: "#998899", marginBottom: "8px" }}>SERVERS</div>
                <div style={{ fontSize: "12px", color: "#55DD88" }}>● Hypixel — 45k online</div>
              </div>
            </div>
          </div>
        )}

        {page === "mods" && <LazyPage name="ModStore" />}
        {page === "installed" && <LazyPage name="InstalledMods" />}
        {page === "shaders" && <LazyPage name="Shaders" />}
        {page === "texturepacks" && <LazyPage name="TexturePacks" />}
        {page === "shop" && <LazyPage name="Shop" />}
        {page === "console" && <LazyPage name="Console" />}
        {page === "settings" && <LazyPage name="Settings" />}
      </div>

      {loginModal && <LoginModal {...loginModal} onClose={() => setLoginModal(null)} />}
    </div>
  );
}

// Lazy load other pages to keep App.tsx simple
function LazyPage({ name }: { name: string }) {
  const [Comp, setComp] = useState<any>(null);
  useEffect(() => {
    import(`./pages/${name}.tsx`).then(m => {
      const key = Object.keys(m).find(k => k.endsWith("Page") || k === "ShopPage" || k === "ConsolePage") || Object.keys(m)[0];
      setComp(() => m[key]);
    });
  }, [name]);
  if (!Comp) return <div style={{ padding: "40px", color: "#554455" }}>Loading...</div>;
  return <Comp versions={["1.21.1","1.21.11"]} selectedVersion="1.21.1" onVersionChange={() => {}} />;
}
