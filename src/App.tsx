import { useState, useEffect } from "react";
import { invoke } from "@tauri-apps/api/core";
import { listen } from "@tauri-apps/api/event";
import { LoginModal } from "./components/LoginModal";
import "./App.css";

export type Page = "play" | "mods" | "installed" | "shaders" | "texturepacks" | "shop" | "console" | "settings";
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

type GameMode = "default" | "bedwars" | "speedrun";

const GAME_MODES: { id: GameMode; name: string; sub: string; version: string; server?: string; gradient: string; accentBorder: string }[] = [
  { id: "default", name: "Minecraft", sub: "Latest + 51 Pulsar Mods", version: "1.21.11", gradient: "linear-gradient(135deg, #0C0C18 0%, #12101E 40%, #1A1428 100%)", accentBorder: "rgba(198, 120, 221, 0.3)" },
  { id: "bedwars", name: "Bedwars", sub: "Hypixel 1.8.9 PvP", version: "1.8.9", server: "mc.hypixel.net", gradient: "linear-gradient(135deg, #0C0810 0%, #1A0C14 40%, #241018 100%)", accentBorder: "rgba(232, 97, 77, 0.3)" },
  { id: "speedrun", name: "Speedrun", sub: "1.16.1 + SpeedRunIGT", version: "1.16.1", gradient: "linear-gradient(135deg, #080C10 0%, #0C1418 40%, #101C20 100%)", accentBorder: "rgba(52, 211, 153, 0.3)" },
];

export default function App() {
  const [versions, setVersions] = useState<string[]>(["1.21.11"]);
  const [selectedVersion, setSelectedVersion] = useState("1.21.11");
  const [phase, setPhase] = useState<LaunchPhase>("idle");
  const [progress, setProgress] = useState(0);
  const [status, setStatus] = useState("Ready");
  const [account, setAccount] = useState<AccountInfo | null>(null);
  const [loginModal, setLoginModal] = useState<{ phase: "waiting" | "code" | "error"; code?: string; url?: string; error?: string } | null>(null);
  const [page, setPage] = useState<Page>("play");
  const [showVersionPicker, setShowVersionPicker] = useState(false);
  const [versionCategory, setVersionCategory] = useState<string | null>(null);
  const [gameMode, setGameMode] = useState<GameMode>("default"); // e.g. "1.21"
  const [showAddFriend, setShowAddFriend] = useState(false);

  useEffect(() => {
    invoke<any>("get_account").then(a => {
      if (a) setAccount({ username: a.username, uuid: a.uuid, accessToken: a.access_token });
    }).catch(() => {});

    // Load version type settings
    const s = localStorage.getItem("pulsar-settings");
    const cfg = s ? JSON.parse(s) : {};
    invoke<string[]>("get_versions", {
      showSnapshots: cfg.showSnapshots || false,
      showBetas: cfg.showBetas || false,
    }).then(v => {
      setVersions(v);
      if (v.includes("1.21.11")) setSelectedVersion("1.21.11");
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

    const unlisteners: Array<() => void> = [];
    const cleanup = () => { for (const fn of unlisteners) fn(); };

    const u1 = await listen<{ pct: number; msg: string }>("launch_progress", e => {
      setProgress(e.payload.pct);
      setStatus(e.payload.msg);
    });
    unlisteners.push(u1);

    const u2 = await listen<any>("launch_done", () => {
      setPhase("done");
      setStatus("Game launched!");
      setTimeout(() => { setPhase("idle"); setStatus("Ready"); cleanup(); }, 3000);
    });
    unlisteners.push(u2);

    const u3 = await listen<{ error: string }>("launch_error", e => {
      setPhase("error");
      setStatus(e.payload.error);
      cleanup();
    });
    unlisteners.push(u3);

    try {
      // Refresh auth token before launch to ensure multiplayer works
      let launchAccount = account;
      if (account?.accessToken) {
        try {
          setStatus("Refreshing session...");
          const refreshed = await invoke<any>("refresh_account");
          if (refreshed) {
            // Map snake_case (from Rust) to camelCase (for our frontend)
            const mapped = {
              username: refreshed.username,
              uuid: refreshed.uuid,
              accessToken: refreshed.access_token,
            };
            launchAccount = mapped;
            setAccount(mapped);
          }
        } catch {
          // Refresh failed — try launching with existing token anyway
        }
      }

      // Read settings for launch params
      const settingsRaw = localStorage.getItem("pulsar-settings");
      const settings = settingsRaw ? JSON.parse(settingsRaw) : {};

      await invoke("launch_minecraft", {
        version: selectedVersion,
        username: launchAccount?.username ?? null,
        uuid: launchAccount?.uuid ?? null,
        accessToken: launchAccount?.accessToken ?? null,
        ramMb: settings.ram || 2048,
        javaArgs: settings.javaArgs || null,
      });
    } catch (e: any) {
      setPhase("error");
      setStatus(String(e));
      cleanup();
    }
  }

  async function handleLogin() {
    setLoginModal({ phase: "waiting" });
    try { await invoke("start_microsoft_login"); } catch (e: any) { setLoginModal({ phase: "error", error: String(e) }); }
  }

  const canPlay = phase === "idle" || phase === "error";

  // Sidebar icons for navigation
  const SIDE_ICONS: { id: Page; svg: string }[] = [
    { id: "play", svg: '<path d="M4.5 16.5c-1.5 1.26-2 5-2 5s3.74-.5 5-2c.71-.84.7-2.13-.09-2.91a2.18 2.18 0 0 0-2.91-.09z"/><path d="M12 15l-3-3a22 22 0 0 1 2-3.95A12.88 12.88 0 0 1 22 2c0 2.72-.78 7.5-6 11a22.35 22.35 0 0 1-4 2z"/>' },
    { id: "mods", svg: '<path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="7.5 4.21 12 6.81 16.5 4.21"/>' },
    { id: "installed", svg: '<circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/>' },
    { id: "shaders", svg: '<polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/>' },
    { id: "texturepacks", svg: '<rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/>' },
    { id: "shop", svg: '<circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/><path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/>' },
  ];
  const SIDE_BOTTOM: { id: Page; svg: string }[] = [
    { id: "console", svg: '<polyline points="4 17 10 11 4 5"/><line x1="12" y1="19" x2="20" y2="19"/>' },
    { id: "settings", svg: '<circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 1 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 1 1 0-4h.09A1.65 1.65 0 0 0 4.6 9"/>' },
  ];

  const modeColors: Record<string, string> = { default: "#C4B5FD", bedwars: "#F0997B", speedrun: "#5DCAA5" };
  const currentMode = GAME_MODES.find(m => m.id === gameMode)!;

  return (
    <div style={{ display: "flex", flexDirection: "column", height: "100vh", width: "100vw", background: "#08080F" }}>
      {/* ── Top Bar ── */}
      <div style={{
        display: "flex", alignItems: "center", gap: "14px", padding: "10px 14px",
        borderBottom: "0.5px solid #1A1A28", background: "#0A0A14", fontSize: "12px",
        // @ts-ignore - Tauri window drag
        WebkitAppRegion: "drag", minHeight: "42px",
      }}>
        {/* Traffic lights spacer (macOS) */}
        <div style={{ width: "54px" }} />
        {/* Version pill */}
        <div style={{
          display: "flex", alignItems: "center", gap: "8px", padding: "5px 10px",
          background: "#11111C", border: "0.5px solid #1F1F2E", borderRadius: "6px",
          // @ts-ignore
          WebkitAppRegion: "no-drag", cursor: "pointer",
        }} onClick={() => setShowVersionPicker(true)}>
          <div style={{ width: "14px", height: "14px", borderRadius: "50%", background: "radial-gradient(circle at 35% 35%, #8B5CF6 0%, #4C1D95 50%, #08080F 80%)" }} />
          <span style={{ color: "#E8E6F5" }}>Pulsar Client</span>
          <span style={{ color: "#5A5870" }}>▾</span>
        </div>
        {/* Online badge */}
        <div style={{ display: "flex", alignItems: "center", gap: "6px", color: "#8A88A8" }}>
          <div style={{ width: "7px", height: "7px", borderRadius: "50%", background: "#5DCAA5", boxShadow: "0 0 8px rgba(93,202,165,0.6)" }} />
          Online
        </div>
        <div style={{ flex: 1 }} />
        {/* User pill */}
        <div style={{
          display: "flex", alignItems: "center", gap: "8px", padding: "4px 10px 4px 4px",
          background: "#11111C", border: "0.5px solid #2A1A4D", borderRadius: "6px",
          cursor: "pointer", // @ts-ignore
          WebkitAppRegion: "no-drag",
        }} onClick={account ? () => { invoke("logout"); setAccount(null); } : handleLogin}>
          <div style={{ width: "20px", height: "20px", borderRadius: "4px", background: account ? "linear-gradient(135deg, #8B5CF6, #4C1D95)" : "#1A1530" }} />
          <span style={{ color: "#E8E6F5", fontSize: "12px" }}>{account?.username || "Sign In"}</span>
          <span style={{ color: "#8A88A8", fontSize: "11px" }}>▾</span>
        </div>
      </div>

      {/* ── Body: 3-column ── */}
      <div style={{ display: "grid", gridTemplateColumns: "56px 1fr 260px", flex: 1, overflow: "hidden" }}>

        {/* ── Left Sidebar (icons) ── */}
        <div style={{ background: "#08080F", borderRight: "0.5px solid #1A1A28", padding: "14px 0", display: "flex", flexDirection: "column", gap: "4px", alignItems: "center" }}>
          {SIDE_ICONS.map(({ id, svg }) => (
            <div key={id} onClick={() => setPage(id)}
              style={{
                width: "38px", height: "38px", borderRadius: "9px", display: "flex", alignItems: "center", justifyContent: "center",
                cursor: "pointer", transition: "all 150ms",
                background: page === id ? "#1A1530" : "transparent",
                color: page === id ? "#C4B5FD" : "#5A5870",
              }}
              onMouseEnter={e => { if (page !== id) { e.currentTarget.style.background = "#11111C"; e.currentTarget.style.color = "#C7C5DC"; }}}
              onMouseLeave={e => { if (page !== id) { e.currentTarget.style.background = "transparent"; e.currentTarget.style.color = "#5A5870"; }}}
            >
              <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" dangerouslySetInnerHTML={{ __html: svg }} />
            </div>
          ))}
          <div style={{ width: "24px", height: "1px", background: "#1F1F2E", margin: "4px 0" }} />
          <div style={{ flex: 1 }} />
          {SIDE_BOTTOM.map(({ id, svg }) => (
            <div key={id} onClick={() => setPage(id)}
              style={{
                width: "38px", height: "38px", borderRadius: "9px", display: "flex", alignItems: "center", justifyContent: "center",
                cursor: "pointer", transition: "all 150ms",
                background: page === id ? "#1A1530" : "transparent",
                color: page === id ? "#C4B5FD" : "#5A5870",
              }}
              onMouseEnter={e => { if (page !== id) { e.currentTarget.style.background = "#11111C"; e.currentTarget.style.color = "#C7C5DC"; }}}
              onMouseLeave={e => { if (page !== id) { e.currentTarget.style.background = "transparent"; e.currentTarget.style.color = "#5A5870"; }}}
            >
              <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" dangerouslySetInnerHTML={{ __html: svg }} />
            </div>
          ))}
        </div>

        {/* ── Main Content ── */}
        <div style={{ overflow: "auto", position: "relative" }}>
        {page === "play" && (
          <div className="fade-in" style={{ padding: "22px 24px", display: "flex", flexDirection: "column", gap: "18px" }}>
            {/* Welcome */}
            <div style={{ display: "flex", alignItems: "center", gap: "10px", fontSize: "18px", fontWeight: "500", color: "#F0EEFC" }}>
              Welcome back, traveler
              {account && <div style={{
                display: "flex", alignItems: "center", gap: "6px", padding: "4px 10px",
                background: "#11111C", border: "0.5px solid #2A1A4D", borderRadius: "6px", fontSize: "14px",
              }}>
                <div style={{ width: "18px", height: "18px", borderRadius: "4px", background: "linear-gradient(135deg, #8B5CF6, #4C1D95)" }} />
                {account.username}
              </div>}
            </div>

            {/* ── Hero with Launch Button ── */}
            <div style={{
              position: "relative", height: "280px", borderRadius: "14px", overflow: "hidden",
              background: "radial-gradient(ellipse at 30% 50%, #1A0F3A 0%, #0D0D1F 40%, #08080F 80%)",
              border: "0.5px solid #1F1F2E", display: "flex", alignItems: "center", justifyContent: "center",
            }}>
              {/* Stars */}
              {[[15,12,1],[28,78,2],[65,22,1.5],[42,88,1],[80,65,1],[20,50,2],[75,35,1.5],[50,8,1]].map(([t,l,s],i) => (
                <div key={i} style={{ position: "absolute", top: `${t}%`, left: `${l}%`, width: `${s}px`, height: `${s}px`, background: "#fff", borderRadius: "50%", opacity: 0.4 + (i%3)*0.15 }} />
              ))}

              {/* Black hole */}
              <div style={{ position: "absolute", left: "12%", top: "50%", transform: "translateY(-50%)", width: "110px", height: "110px" }}>
                <div style={{ position: "absolute", inset: "-30%", borderRadius: "50%", background: "radial-gradient(circle, rgba(139,92,246,0.25) 0%, transparent 60%)" }} />
                <div style={{ position: "absolute", inset: 0, borderRadius: "50%", background: "conic-gradient(from 0deg, #C4B5FD, #8B5CF6, #4C1D95, #1A0F3A, #4C1D95, #8B5CF6, #C4B5FD)", animation: "spin 8s linear infinite", opacity: 0.85, filter: "blur(2px)" }} />
                <div style={{ position: "absolute", inset: "22%", borderRadius: "50%", background: "#08080F", boxShadow: "0 0 30px rgba(0,0,0,0.9)" }} />
              </div>

              {/* Launch button — centered in hero */}
              <div onClick={canPlay ? handleLaunch : undefined} style={{
                position: "relative", zIndex: 5, display: "flex", alignItems: "center", gap: "14px",
                padding: "16px 28px 16px 24px", borderRadius: "12px", cursor: canPlay ? "pointer" : "default",
                background: canPlay ? "linear-gradient(135deg, #7C5DC4 0%, #5B3FA6 100%)" : "rgba(255,255,255,0.06)",
                border: canPlay ? "0.5px solid #C4B5FD" : "0.5px solid #1F1F2E",
                boxShadow: canPlay ? "0 0 30px rgba(139,92,246,0.35), inset 0 1px 0 rgba(255,255,255,0.15)" : "none",
                animation: canPlay && phase === "idle" ? "pulse 3s ease-in-out infinite" : "none",
                minWidth: "300px", transition: "all 0.25s",
              }}>
                <div style={{ width: "36px", height: "36px", borderRadius: "50%", background: "rgba(255,255,255,0.95)", color: "#4C1D95", display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0 }}>
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><polygon points="5 3 19 12 5 21 5 3"/></svg>
                </div>
                <div style={{ flex: 1 }}>
                  <div style={{ fontSize: "17px", fontWeight: "500", color: "#FFFFFF", letterSpacing: "0.5px", margin: "0 0 3px" }}>
                    {phase === "done" ? "RUNNING" : phase === "loading" ? "LAUNCHING..." : phase === "error" ? "RETRY" : "LAUNCH GAME"}
                  </div>
                  <div style={{ display: "flex", alignItems: "center", gap: "6px", fontSize: "12px", color: "#E0D7FF" }}>
                    <div style={{ width: "12px", height: "12px", borderRadius: "50%", background: "radial-gradient(circle at 35% 35%, #C4B5FD 0%, #5B3FA6 70%)" }} />
                    Pulsar {currentMode.version} with <span style={{ color: "#fff", fontWeight: "500" }}>Fabric</span>
                  </div>
                </div>
              </div>
            </div>

            {/* Progress */}
            {phase === "loading" && (
              <div>
                <div style={{ height: "3px", background: "rgba(255,255,255,0.03)", borderRadius: "4px", overflow: "hidden" }}>
                  <div style={{ height: "100%", width: `${progress}%`, background: "linear-gradient(90deg, #5B3FA6, #C4B5FD)", transition: "width 0.4s ease", borderRadius: "4px" }} />
                </div>
                <div className="mono" style={{ fontSize: "11px", color: "#8A88A8", marginTop: "6px" }}>{status}</div>
              </div>
            )}
            {phase === "error" && (
              <div style={{ fontSize: "12px", color: "#F0997B", padding: "10px 14px", background: "rgba(240,153,123,0.06)", border: "0.5px solid rgba(240,153,123,0.15)", borderRadius: "8px" }}>{status}</div>
            )}

            {/* ── Game Mode Tabs ── */}
            <div style={{ display: "flex", gap: "6px", padding: "8px", background: "#0D0D17", border: "0.5px solid #1F1F2E", borderRadius: "12px" }}>
              {GAME_MODES.map(mode => {
                const active = gameMode === mode.id;
                const col = modeColors[mode.id];
                return (
                  <div key={mode.id} onClick={() => { setGameMode(mode.id); setSelectedVersion(mode.version); }}
                    style={{
                      flex: 1, height: "56px", borderRadius: "8px", cursor: "pointer", transition: "all 200ms",
                      display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", gap: "4px",
                      background: active ? "#1A1530" : "transparent",
                      boxShadow: active ? "inset 0 0 0 0.5px #5B3FA6" : "none",
                    }}
                    onMouseEnter={e => { if (!active) e.currentTarget.style.background = "#11111C"; }}
                    onMouseLeave={e => { if (!active) e.currentTarget.style.background = "transparent"; }}
                  >
                    <div style={{ fontSize: "13px", fontWeight: "600", color: active ? col : "#5A5870", transition: "color 200ms" }}>{mode.name}</div>
                    <div className="mono" style={{ fontSize: "9px", color: active ? "#8A88A8" : "#2A2A3E" }}>{mode.version}</div>
                  </div>
                );
              })}
            </div>

            {/* ── Daily Reward ── */}
            <DailyReward />
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

        {/* ── Right Column ── */}
        <div style={{ background: "#08080F", borderLeft: "0.5px solid #1A1A28", padding: "22px 18px", display: "flex", flexDirection: "column", gap: "16px", overflow: "auto" }}>
          {/* Friends */}
          <div>
            <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: "12px" }}>
              <div><span style={{ fontSize: "16px", fontWeight: "500", color: "#F0EEFC" }}>Friends</span> <span style={{ color: "#8A88A8", fontSize: "13px" }}>(0 online)</span></div>
              <div
                onClick={() => setShowAddFriend(true)}
                style={{
                  fontSize: "11px", fontWeight: "600", color: "#C4B5FD", cursor: "pointer",
                  padding: "4px 10px", borderRadius: "6px", background: "rgba(139,92,246,0.08)",
                  border: "0.5px solid rgba(139,92,246,0.2)", transition: "all 150ms",
                }}
                onMouseEnter={e => { e.currentTarget.style.background = "rgba(139,92,246,0.15)"; }}
                onMouseLeave={e => { e.currentTarget.style.background = "rgba(139,92,246,0.08)"; }}
              >Add</div>
            </div>
            {/* Empty state */}
            <div style={{ display: "flex", flexDirection: "column", alignItems: "center", padding: "24px 12px", gap: "10px" }}>
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#2A2A3E" strokeWidth="1.5">
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>
              </svg>
              <div style={{ fontSize: "13px", fontWeight: "500", color: "#5A5870", textAlign: "center" }}>Add your first friend</div>
              <div style={{ fontSize: "11px", color: "#3A3850", textAlign: "center", lineHeight: 1.5 }}>See what your friends are playing and join their games.</div>
              <div onClick={() => setShowAddFriend(true)} style={{
                fontSize: "11px", fontWeight: "600", color: "#C4B5FD", cursor: "pointer",
                padding: "6px 14px", borderRadius: "8px", background: "#1A1530",
                border: "0.5px solid #5B3FA6", marginTop: "4px", transition: "all 150ms",
              }}
              onMouseEnter={e => { e.currentTarget.style.background = "#221A40"; }}
              onMouseLeave={e => { e.currentTarget.style.background = "#1A1530"; }}
              >Add Friend</div>
            </div>
          </div>

          {/* Equipped Cosmetic */}
          <div style={{ marginTop: "auto", background: "#0D0D17", border: "0.5px solid #1F1F2E", borderRadius: "12px", overflow: "hidden" }}>
            <div style={{
              height: "160px", position: "relative", overflow: "hidden",
              background: "radial-gradient(ellipse at center, #1A0F3A 0%, #08080F 70%)",
              display: "flex", alignItems: "center", justifyContent: "center",
            }}>
              <div style={{ position: "absolute", inset: "-40%", background: "radial-gradient(circle at center, rgba(139,92,246,0.25) 0%, transparent 50%)", animation: "spin 16s linear infinite" }} />
              <div style={{ position: "relative", width: "50px", height: "80px", zIndex: 2 }}>
                <div style={{
                  position: "absolute", inset: 0, borderRadius: "6px 6px 14px 14px",
                  background: "radial-gradient(ellipse at 40% 25%, #4C1D95 0%, #1A0F3A 60%, #08080F 90%)",
                  border: "0.5px solid #5B3FA6", boxShadow: "0 0 20px rgba(139,92,246,0.3)",
                }}>
                  <div style={{ position: "absolute", inset: "12px", border: "0.5px solid rgba(196,181,253,0.4)", borderRadius: "3px" }} />
                </div>
              </div>
            </div>
            <div style={{ padding: "12px 14px" }}>
              <div style={{ fontSize: "10px", color: "#6B6985", letterSpacing: "1px", textTransform: "uppercase", marginBottom: "4px" }}>Equipped Cape</div>
              <div style={{ fontSize: "14px", fontWeight: "500", color: "#F0EEFC", display: "flex", justifyContent: "space-between" }}>
                <span>OG Pulsar</span>
                <span style={{ fontSize: "11px", color: "#C4B5FD", cursor: "pointer" }} onClick={() => setPage("shop")}>Change</span>
              </div>
              <div className="mono" style={{ fontSize: "11px", color: "#C4B5FD", marginTop: "2px" }}>Limited · v2.1.0</div>
            </div>
          </div>
        </div>
      </div>

      {showVersionPicker && (() => {
        // Group versions by major.minor (e.g. "1.21", "1.20", "26.1")
        const groups: Record<string, string[]> = {};
        for (const v of versions) {
          const parts = v.split(".");
          const key = parts.length >= 2 ? parts.slice(0, 2).join(".") : v;
          if (!groups[key]) groups[key] = [];
          groups[key].push(v);
        }
        const categories = Object.keys(groups);
        const filteredVersions = versionCategory ? (groups[versionCategory] || []) : [];

        return (
        <div style={{
          position: "fixed", inset: 0, background: "rgba(0,0,0,0.75)", backdropFilter: "blur(6px)",
          display: "flex", alignItems: "center", justifyContent: "center", zIndex: 99,
        }} onClick={() => { setShowVersionPicker(false); setVersionCategory(null); }}>
          <div onClick={e => e.stopPropagation()} className="fade-in" style={{
            background: "rgba(0,0,0,0.97)", border: "1px solid rgba(255,255,255,0.08)",
            borderRadius: "16px", padding: "28px 32px", width: "min(560px, 90vw)", maxHeight: "75vh",
            boxShadow: "0 24px 64px rgba(0,0,0,0.5)",
          }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "20px" }}>
              <div style={{ display: "flex", alignItems: "center", gap: "12px" }}>
                {versionCategory && (
                  <div onClick={() => setVersionCategory(null)} style={{
                    cursor: "pointer", color: "var(--text-muted)", fontSize: "13px",
                    padding: "4px 10px", borderRadius: "6px", background: "rgba(255,255,255,0.04)",
                    transition: "all 0.15s",
                  }}
                  onMouseEnter={e => e.currentTarget.style.background = "rgba(255,255,255,0.08)"}
                  onMouseLeave={e => e.currentTarget.style.background = "rgba(255,255,255,0.04)"}
                  >Back</div>
                )}
                <div style={{ fontSize: "15px", fontWeight: "800", color: "#FFFFFF", letterSpacing: "0.06em" }}>
                  {versionCategory ? `MINECRAFT ${versionCategory}` : "SELECT A VERSION"}
                </div>
              </div>
              <div onClick={() => { setShowVersionPicker(false); setVersionCategory(null); }} style={{
                width: "28px", height: "28px", borderRadius: "6px", background: "rgba(255,80,80,0.1)",
                display: "flex", alignItems: "center", justifyContent: "center",
                cursor: "pointer", color: "var(--accent-red)", fontSize: "14px", fontWeight: "700",
                transition: "background 0.15s",
              }}
              onMouseEnter={e => e.currentTarget.style.background = "rgba(255,80,80,0.2)"}
              onMouseLeave={e => e.currentTarget.style.background = "rgba(255,80,80,0.1)"}
              >x</div>
            </div>

            <div style={{
              display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(110px, 1fr))",
              gap: "8px", maxHeight: "55vh", overflowY: "auto",
            }}>
              {!versionCategory ? (
                /* Show major version categories */
                categories.map(cat => {
                  const hasSelected = groups[cat].includes(selectedVersion);
                  const count = groups[cat].length;
                  return (
                    <div key={cat} onClick={() => {
                      if (count === 1) { setSelectedVersion(groups[cat][0]); setShowVersionPicker(false); setVersionCategory(null); }
                      else setVersionCategory(cat);
                    }} style={{
                      padding: "16px 12px", borderRadius: "10px", textAlign: "center",
                      background: hasSelected ? "rgba(255,255,255,0.08)" : "rgba(255,255,255,0.02)",
                      border: hasSelected ? "1px solid rgba(255,255,255,0.18)" : "1px solid rgba(255,255,255,0.04)",
                      cursor: "pointer", transition: "all 0.15s",
                    }}
                    onMouseEnter={e => { e.currentTarget.style.background = "rgba(255,255,255,0.05)"; e.currentTarget.style.borderColor = "rgba(255,255,255,0.15)"; }}
                    onMouseLeave={e => { e.currentTarget.style.background = hasSelected ? "rgba(255,255,255,0.08)" : "rgba(255,255,255,0.02)"; e.currentTarget.style.borderColor = hasSelected ? "rgba(255,255,255,0.18)" : "rgba(255,255,255,0.04)"; }}
                    >
                      <div style={{ fontSize: "16px", fontWeight: "800", color: "var(--text-primary)", marginBottom: "4px" }}>{cat}</div>
                      <div style={{ fontSize: "10px", color: "var(--text-muted)" }}>{count} version{count > 1 ? "s" : ""}</div>
                    </div>
                  );
                })
              ) : (
                /* Show specific versions within category */
                filteredVersions.map(v => (
                  <div key={v} onClick={() => { setSelectedVersion(v); setShowVersionPicker(false); setVersionCategory(null); }} style={{
                    padding: "18px 12px", borderRadius: "10px", textAlign: "center",
                    background: v === selectedVersion ? "rgba(255,255,255,0.1)" : "rgba(255,255,255,0.02)",
                    border: v === selectedVersion ? "1px solid rgba(255,255,255,0.22)" : "1px solid rgba(255,255,255,0.04)",
                    cursor: "pointer", transition: "all 0.15s",
                    fontSize: "14px", fontWeight: "700",
                    color: "var(--text-primary)",
                  }}
                  onMouseEnter={e => { if (v !== selectedVersion) { e.currentTarget.style.background = "rgba(255,255,255,0.04)"; e.currentTarget.style.borderColor = "rgba(255,255,255,0.12)"; }}}
                  onMouseLeave={e => { if (v !== selectedVersion) { e.currentTarget.style.background = "rgba(255,255,255,0.02)"; e.currentTarget.style.borderColor = "rgba(255,255,255,0.04)"; }}}
                  >
                    {v}
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
        );
      })()}
      {loginModal && <LoginModal {...loginModal} onClose={() => setLoginModal(null)} />}
      {showAddFriend && (
        <div style={{
          position: "fixed", inset: 0, background: "rgba(0,0,0,0.75)", backdropFilter: "blur(6px)",
          display: "flex", alignItems: "center", justifyContent: "center", zIndex: 99,
        }} onClick={() => setShowAddFriend(false)}>
          <div onClick={e => e.stopPropagation()} className="fade-in" style={{
            background: "#0D0D17", border: "1px solid #1F1F2E", borderRadius: "14px",
            padding: "24px 28px", width: "min(360px, 85vw)", boxShadow: "0 24px 64px rgba(0,0,0,0.5)",
          }}>
            <div style={{ fontSize: "15px", fontWeight: "600", color: "#F0EEFC", marginBottom: "14px" }}>Add Friend</div>
            <input
              type="text"
              placeholder="Enter username..."
              autoFocus
              style={{
                width: "100%", padding: "10px 14px", borderRadius: "8px", border: "0.5px solid #1F1F2E",
                background: "#08080F", color: "#F0EEFC", fontSize: "13px", outline: "none",
                fontFamily: "inherit", boxSizing: "border-box",
              }}
              onFocus={e => { e.currentTarget.style.borderColor = "#5B3FA6"; }}
              onBlur={e => { e.currentTarget.style.borderColor = "#1F1F2E"; }}
              onKeyDown={e => {
                if (e.key === "Enter") {
                  alert("Friends system coming soon \u2014 stay tuned!");
                  setShowAddFriend(false);
                }
              }}
            />
            <div style={{ display: "flex", gap: "8px", marginTop: "14px", justifyContent: "flex-end" }}>
              <div
                onClick={() => setShowAddFriend(false)}
                style={{
                  padding: "8px 16px", borderRadius: "8px", fontSize: "12px", fontWeight: "600",
                  color: "#8A88A8", cursor: "pointer", background: "rgba(255,255,255,0.04)",
                  transition: "background 150ms",
                }}
                onMouseEnter={e => { e.currentTarget.style.background = "rgba(255,255,255,0.08)"; }}
                onMouseLeave={e => { e.currentTarget.style.background = "rgba(255,255,255,0.04)"; }}
              >Cancel</div>
              <div
                onClick={() => { alert("Friends system coming soon \u2014 stay tuned!"); setShowAddFriend(false); }}
                style={{
                  padding: "8px 16px", borderRadius: "8px", fontSize: "12px", fontWeight: "600",
                  color: "#FFFFFF", cursor: "pointer", background: "linear-gradient(135deg, #7C5DC4, #5B3FA6)",
                  border: "0.5px solid #C4B5FD", transition: "opacity 150ms",
                }}
                onMouseEnter={e => { e.currentTarget.style.opacity = "0.85"; }}
                onMouseLeave={e => { e.currentTarget.style.opacity = "1"; }}
              >Send Request</div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

function DailyReward() {
  const REWARDS = [50, 75, 100, 150, 100, 125, 250]; // Sun-Sat
  const DAY_NAMES = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];

  const [streak, setStreak] = useState(0);
  const [claimed, setClaimed] = useState(false);
  const [showAnim, setShowAnim] = useState(false);

  useEffect(() => {
    const saved = localStorage.getItem("pulsar-daily");
    if (saved) {
      const data = JSON.parse(saved);
      const lastClaim = new Date(data.lastClaim);
      const now = new Date();
      const diffDays = Math.floor((now.getTime() - lastClaim.getTime()) / 86400000);
      if (diffDays === 0) {
        setClaimed(true);
        setStreak(data.streak || 0);
      } else if (diffDays === 1) {
        setStreak(data.streak || 0);
      } else {
        setStreak(0); // missed a day, reset
      }
    }
  }, []);

  function claim() {
    if (claimed) return;
    const newStreak = streak + 1;
    const dayIndex = new Date().getDay();
    const reward = REWARDS[dayIndex];

    // Add points to cosmetics (shared file + localStorage)
    invoke<string>("get_cosmetics").then(raw => {
      const cosmetics = JSON.parse(raw);
      cosmetics.points = (cosmetics.points || 0) + reward;
      const json = JSON.stringify(cosmetics);
      invoke("save_cosmetics", { data: json }).catch(() => {});
      localStorage.setItem("pulsar-cosmetics", json);
    }).catch(() => {
      const cosmetics = JSON.parse(localStorage.getItem("pulsar-cosmetics") || '{"v":2,"points":500,"purchased":[],"equipped":{}}');
      cosmetics.points = (cosmetics.points || 0) + reward;
      localStorage.setItem("pulsar-cosmetics", JSON.stringify(cosmetics));
    });

    // Save daily state
    localStorage.setItem("pulsar-daily", JSON.stringify({
      lastClaim: new Date().toISOString(),
      streak: newStreak,
    }));

    setClaimed(true);
    setStreak(newStreak);
    setShowAnim(true);
    setTimeout(() => setShowAnim(false), 2000);
  }

  const todayIndex = new Date().getDay();
  const todayReward = REWARDS[todayIndex];

  return (
    <div className="pulsar-card" style={{
      padding: "18px 22px",
      background: claimed ? "rgba(110,231,160,0.03)" : "rgba(255,255,255,0.02)",
      border: claimed ? "1px solid rgba(110,231,160,0.1)" : "1px solid rgba(255,255,255,0.06)",
      position: "relative", overflow: "hidden",
    }}>
      {showAnim && (
        <div style={{
          position: "absolute", inset: 0, pointerEvents: "none",
          background: "radial-gradient(circle at center, rgba(255,255,255,0.08) 0%, transparent 70%)",
          animation: "pulse-glow 1s ease-out",
        }} />
      )}
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
        <div>
          <div style={{ fontSize: "10px", fontWeight: "700", letterSpacing: "0.1em", color: "var(--text-muted)", textTransform: "uppercase", marginBottom: "6px" }}>
            Daily Reward {streak > 0 && <span style={{ color: "#FFFFFF" }}>• {streak} day streak</span>}
          </div>
          <div style={{ display: "flex", gap: "6px", marginBottom: "8px" }}>
            {DAY_NAMES.map((d, i) => (
              <div key={d} style={{
                width: "36px", textAlign: "center", padding: "4px 0", borderRadius: "6px", fontSize: "10px",
                background: i === todayIndex
                  ? (claimed ? "rgba(110,231,160,0.12)" : "rgba(255,255,255,0.08)")
                  : "rgba(255,255,255,0.02)",
                border: i === todayIndex
                  ? (claimed ? "1px solid rgba(110,231,160,0.2)" : "1px solid rgba(255,255,255,0.15)")
                  : "1px solid transparent",
              }}>
                <div style={{ color: i === todayIndex ? (claimed ? "var(--accent-green)" : "#FFFFFF") : "var(--text-faint)", fontWeight: "600" }}>{d}</div>
                <div style={{ color: i === todayIndex ? "var(--text-primary)" : "var(--text-dim)", fontWeight: "700", fontSize: "11px" }}>{REWARDS[i]}</div>
              </div>
            ))}
          </div>
        </div>
        <button
          onClick={claim}
          disabled={claimed}
          style={{
            padding: "10px 22px", borderRadius: "8px", border: "none", cursor: claimed ? "default" : "pointer",
            background: claimed
              ? "rgba(110,231,160,0.08)"
              : "linear-gradient(135deg, #FFFFFF, #C0C0C0)",
            color: claimed ? "var(--accent-green)" : "#000000",
            fontSize: "12px", fontWeight: "800", letterSpacing: "0.05em",
            fontFamily: "inherit", transition: "all 0.2s",
            boxShadow: claimed ? "none" : "0 2px 12px rgba(255,255,255,0.15)",
          }}
        >
          {claimed ? `✓ Claimed +${todayReward}` : `Claim +${todayReward}`}
        </button>
      </div>
    </div>
  );
}

function LazyPage({ name }: { name: string }) {
  const [Comp, setComp] = useState<any>(null);
  useEffect(() => {
    import(`./pages/${name}.tsx`).then(m => {
      const key = Object.keys(m).find(k => k.endsWith("Page") || k === "ShopPage" || k === "ConsolePage") || Object.keys(m)[0];
      setComp(() => m[key]);
    });
  }, [name]);
  if (!Comp) return (
    <div style={{ display: "flex", alignItems: "center", justifyContent: "center", height: "100%", color: "var(--text-faint)" }}>
      <div style={{ textAlign: "center" }}>
        <div style={{ fontSize: "24px", marginBottom: "8px", opacity: 0.5 }}>loading...</div>
      </div>
    </div>
  );
  return <Comp versions={["1.21.1","1.21.11"]} selectedVersion="1.21.11" onVersionChange={() => {}} />;
}
