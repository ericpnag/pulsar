import { useState, useEffect } from "react";
import { invoke } from "@tauri-apps/api/core";
import { listen } from "@tauri-apps/api/event";
import { TopNav } from "./components/TopNav";
import { PetalCanvas } from "./components/PetalCanvas";
import { HeroBanner } from "./components/HeroBanner";
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
  const [versionCategory, setVersionCategory] = useState<string | null>(null); // e.g. "1.21"

  useEffect(() => {
    invoke<any>("get_account").then(a => {
      if (a) setAccount({ username: a.username, uuid: a.uuid, accessToken: a.access_token });
    }).catch(() => {});

    invoke<string[]>("get_versions").then(v => {
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

      await invoke("launch_minecraft", {
        version: selectedVersion,
        username: launchAccount?.username ?? null,
        uuid: launchAccount?.uuid ?? null,
        accessToken: launchAccount?.accessToken ?? null,
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

  return (
    <div style={{ display: "flex", flexDirection: "column", height: "100vh", width: "100vw", position: "relative" }}>
      {/* Subtle petal animation */}
      <div style={{ position: "fixed", inset: 0, pointerEvents: "none", zIndex: 0, opacity: 0.25 }}>
        <PetalCanvas />
      </div>

      <TopNav
        page={page}
        onNavigate={setPage}
        account={account}
        onLogin={handleLogin}
        onLogout={() => { invoke("logout"); setAccount(null); }}
      />

      {/* Content */}
      <div style={{ flex: 1, overflow: "auto", position: "relative", zIndex: 1 }}>
        {page === "play" && (
          <div className="fade-in" style={{ padding: "24px", display: "flex", flexDirection: "column", gap: "16px" }}>
            {/* Hero Banner with animated flowers */}
            <HeroBanner
              selectedVersion={selectedVersion}
              versions={versions}
              onVersionChange={setSelectedVersion}
              onOpenPicker={() => setShowVersionPicker(true)}
            />

            {/* Launch Button */}
            <button
              onClick={canPlay ? handleLaunch : undefined}
              disabled={!canPlay}
              style={{
                width: "100%", padding: "16px", border: "none", borderRadius: "var(--radius)",
                background: canPlay
                  ? "linear-gradient(135deg, var(--pink-300), var(--pink-400), var(--pink-500))"
                  : "rgba(255,176,192,0.06)",
                color: canPlay ? "#1a0a12" : "var(--text-faint)",
                fontSize: "13px", fontWeight: "800", letterSpacing: "0.14em",
                cursor: canPlay ? "pointer" : "default",
                boxShadow: canPlay ? "0 4px 20px var(--pink-glow)" : "none",
                transition: "all 0.25s ease",
                fontFamily: "inherit",
                animation: phase === "idle" ? "pulse-glow 4s ease-in-out infinite" : "none",
                textTransform: "uppercase",
              }}
              onMouseEnter={e => { if (canPlay) { e.currentTarget.style.transform = "translateY(-1px)"; e.currentTarget.style.boxShadow = "0 8px 32px rgba(255,176,192,0.3)"; }}}
              onMouseLeave={e => { if (canPlay) { e.currentTarget.style.transform = "translateY(0)"; e.currentTarget.style.boxShadow = "0 4px 20px var(--pink-glow)"; }}}
            >
              {phase === "done" ? "Minecraft is Running" : phase === "loading" ? "Launching..." : phase === "error" ? "Retry" : "Launch Game"}
            </button>

            {/* Progress */}
            {phase === "loading" && (
              <div className="fade-in" style={{ padding: "0 2px" }}>
                <div style={{ height: "3px", background: "rgba(255,255,255,0.03)", borderRadius: "4px", overflow: "hidden" }}>
                  <div style={{
                    height: "100%", width: `${progress}%`,
                    background: "linear-gradient(90deg, var(--pink-400), var(--pink-200))",
                    transition: "width 0.4s ease",
                    borderRadius: "4px",
                  }} />
                </div>
                <div style={{ fontSize: "11px", color: "var(--text-muted)", marginTop: "8px" }}>{status}</div>
              </div>
            )}

            {/* Error */}
            {phase === "error" && (
              <div className="fade-in" style={{
                fontSize: "12px", color: "var(--accent-red)", padding: "12px 16px",
                background: "rgba(255,80,80,0.04)", border: "1px solid rgba(255,80,80,0.1)",
                borderRadius: "var(--radius-sm)", lineHeight: 1.6,
              }}>
                {status}
              </div>
            )}

            {/* Daily Reward + Info cards */}
            <DailyReward />

            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "10px" }}>
              <div className="bloom-card" style={{ padding: "18px 20px" }}>
                <div style={{ fontSize: "10px", fontWeight: "700", letterSpacing: "0.1em", color: "var(--text-muted)", marginBottom: "10px", textTransform: "uppercase" }}>
                  Bloom Client
                </div>
                <div style={{ fontSize: "12px", color: "var(--text-muted)", lineHeight: 1.7 }}>
                  Press <span style={{ color: "var(--pink-300)", fontWeight: "600", background: "rgba(255,176,192,0.08)", padding: "1px 6px", borderRadius: "4px", fontSize: "11px" }}>Right Shift</span> in-game for modules
                </div>
              </div>
              <div className="bloom-card" style={{ padding: "18px 20px" }}>
                <div style={{ fontSize: "10px", fontWeight: "700", letterSpacing: "0.1em", color: "var(--text-muted)", marginBottom: "10px", textTransform: "uppercase" }}>
                  Servers
                </div>
                {[
                  { name: "Hypixel", players: "45,231" },
                  { name: "BedWars Practice", players: "2,104" },
                  { name: "PvP Legacy", players: "891" },
                ].map((s, i) => (
                  <div key={i} style={{
                    display: "flex", justifyContent: "space-between", alignItems: "center",
                    padding: "3px 0",
                  }}>
                    <span style={{ fontSize: "12px", color: "var(--text-secondary)" }}>{s.name}</span>
                    <span style={{ fontSize: "10px", color: "var(--accent-green)", fontWeight: "600", fontVariantNumeric: "tabular-nums" }}>{s.players}</span>
                  </div>
                ))}
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
            background: "rgba(12,8,18,0.97)", border: "1px solid rgba(255,176,192,0.08)",
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
                  onMouseEnter={e => e.currentTarget.style.background = "rgba(255,176,192,0.08)"}
                  onMouseLeave={e => e.currentTarget.style.background = "rgba(255,255,255,0.04)"}
                  >Back</div>
                )}
                <div style={{ fontSize: "15px", fontWeight: "800", color: "var(--pink-200)", letterSpacing: "0.06em" }}>
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
                      background: hasSelected ? "rgba(255,176,192,0.08)" : "rgba(255,255,255,0.02)",
                      border: hasSelected ? "1px solid rgba(255,176,192,0.2)" : "1px solid rgba(255,255,255,0.04)",
                      cursor: "pointer", transition: "all 0.15s",
                    }}
                    onMouseEnter={e => { e.currentTarget.style.background = "rgba(255,255,255,0.05)"; e.currentTarget.style.borderColor = "rgba(255,176,192,0.15)"; }}
                    onMouseLeave={e => { e.currentTarget.style.background = hasSelected ? "rgba(255,176,192,0.08)" : "rgba(255,255,255,0.02)"; e.currentTarget.style.borderColor = hasSelected ? "rgba(255,176,192,0.2)" : "rgba(255,255,255,0.04)"; }}
                    >
                      <div style={{ fontSize: "16px", fontWeight: "800", color: hasSelected ? "var(--pink-200)" : "var(--text-primary)", marginBottom: "4px" }}>{cat}</div>
                      <div style={{ fontSize: "10px", color: "var(--text-muted)" }}>{count} version{count > 1 ? "s" : ""}</div>
                    </div>
                  );
                })
              ) : (
                /* Show specific versions within category */
                filteredVersions.map(v => (
                  <div key={v} onClick={() => { setSelectedVersion(v); setShowVersionPicker(false); setVersionCategory(null); }} style={{
                    padding: "18px 12px", borderRadius: "10px", textAlign: "center",
                    background: v === selectedVersion ? "rgba(255,176,192,0.1)" : "rgba(255,255,255,0.02)",
                    border: v === selectedVersion ? "1px solid rgba(255,176,192,0.25)" : "1px solid rgba(255,255,255,0.04)",
                    cursor: "pointer", transition: "all 0.15s",
                    fontSize: "14px", fontWeight: "700",
                    color: v === selectedVersion ? "var(--pink-200)" : "var(--text-secondary)",
                  }}
                  onMouseEnter={e => { if (v !== selectedVersion) { e.currentTarget.style.background = "rgba(255,255,255,0.04)"; e.currentTarget.style.borderColor = "rgba(255,176,192,0.12)"; }}}
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
    const saved = localStorage.getItem("bloom-daily");
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
      localStorage.setItem("bloom-cosmetics", json);
    }).catch(() => {
      const cosmetics = JSON.parse(localStorage.getItem("bloom-cosmetics") || '{"points":500,"owned":[],"equipped":{}}');
      cosmetics.points = (cosmetics.points || 0) + reward;
      localStorage.setItem("bloom-cosmetics", JSON.stringify(cosmetics));
    });

    // Save daily state
    localStorage.setItem("bloom-daily", JSON.stringify({
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
    <div className="bloom-card" style={{
      padding: "18px 22px",
      background: claimed ? "rgba(110,231,160,0.03)" : "rgba(255,176,192,0.04)",
      border: claimed ? "1px solid rgba(110,231,160,0.1)" : "1px solid rgba(255,176,192,0.12)",
      position: "relative", overflow: "hidden",
    }}>
      {showAnim && (
        <div style={{
          position: "absolute", inset: 0, pointerEvents: "none",
          background: "radial-gradient(circle at center, rgba(255,176,192,0.15) 0%, transparent 70%)",
          animation: "pulse-glow 1s ease-out",
        }} />
      )}
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
        <div>
          <div style={{ fontSize: "10px", fontWeight: "700", letterSpacing: "0.1em", color: "var(--text-muted)", textTransform: "uppercase", marginBottom: "6px" }}>
            Daily Reward {streak > 0 && <span style={{ color: "var(--pink-300)" }}>• {streak} day streak</span>}
          </div>
          <div style={{ display: "flex", gap: "6px", marginBottom: "8px" }}>
            {DAY_NAMES.map((d, i) => (
              <div key={d} style={{
                width: "36px", textAlign: "center", padding: "4px 0", borderRadius: "6px", fontSize: "10px",
                background: i === todayIndex
                  ? (claimed ? "rgba(110,231,160,0.12)" : "rgba(255,176,192,0.12)")
                  : "rgba(255,255,255,0.02)",
                border: i === todayIndex
                  ? (claimed ? "1px solid rgba(110,231,160,0.2)" : "1px solid rgba(255,176,192,0.15)")
                  : "1px solid transparent",
              }}>
                <div style={{ color: i === todayIndex ? (claimed ? "var(--accent-green)" : "var(--pink-200)") : "var(--text-faint)", fontWeight: "600" }}>{d}</div>
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
              : "linear-gradient(135deg, var(--pink-300), var(--pink-400))",
            color: claimed ? "var(--accent-green)" : "#1a0a12",
            fontSize: "12px", fontWeight: "800", letterSpacing: "0.05em",
            fontFamily: "inherit", transition: "all 0.2s",
            boxShadow: claimed ? "none" : "0 2px 12px rgba(255,176,192,0.2)",
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
