import type { Page } from "../App";

const TABS: { id: Page; label: string; icon: string }[] = [
  { id: "play", label: "Play", icon: "▶" },
  { id: "mods", label: "Mods", icon: "⊞" },
  { id: "installed", label: "Installed", icon: "≡" },
  { id: "shaders", label: "Shaders", icon: "◆" },
  { id: "texturepacks", label: "Textures", icon: "◈" },
  { id: "settings", label: "Settings", icon: "⚙" },
];

interface Props {
  page: Page;
  onNavigate: (p: Page) => void;
  account: { username: string } | null;
  onLogin: () => void;
  onLogout: () => void;
}

export function TopNav({ page, onNavigate, account, onLogin, onLogout }: Props) {
  return (
    <header style={{
      display: "flex", alignItems: "center", height: "56px", minHeight: "56px",
      background: "rgba(13,8,16,0.95)", borderBottom: "1px solid rgba(255,176,192,0.08)",
      padding: "0 20px", gap: "8px",
      // @ts-ignore
      WebkitAppRegion: "drag",
    }}>
      {/* Logo */}
      <div style={{ display: "flex", alignItems: "center", gap: "10px", marginRight: "32px" }}>
        <span style={{ fontSize: "22px" }}>🌸</span>
        <span style={{ fontWeight: "800", fontSize: "14px", letterSpacing: "0.12em", color: "#FFD1DC" }}>
          BLOOM CLIENT
        </span>
      </div>

      {/* Tabs */}
      <nav style={{ display: "flex", gap: "4px", flex: 1, // @ts-ignore
        WebkitAppRegion: "no-drag" }}>
        {TABS.map(({ id, label, icon }) => {
          const active = page === id;
          return (
            <button key={id} onClick={() => onNavigate(id)} style={{
              display: "flex", alignItems: "center", gap: "6px",
              padding: "8px 16px", background: "transparent",
              color: active ? "#FFD1DC" : "#776070",
              border: "none", borderBottom: active ? "2px solid #FFB7C9" : "2px solid transparent",
              fontSize: "13px", fontWeight: active ? "700" : "500",
              cursor: "pointer", transition: "all 0.15s",
              letterSpacing: "0.03em",
            }}
            onMouseEnter={e => { if (!active) e.currentTarget.style.color = "#caa"; }}
            onMouseLeave={e => { if (!active) e.currentTarget.style.color = "#776070"; }}
            >
              <span style={{ fontSize: "13px" }}>{icon}</span>
              {label}
            </button>
          );
        })}
      </nav>

      {/* Account */}
      <div style={{ display: "flex", alignItems: "center", gap: "10px", // @ts-ignore
        WebkitAppRegion: "no-drag" }}>
        {account ? (
          <div style={{ display: "flex", alignItems: "center", gap: "8px", cursor: "pointer" }} onClick={onLogout}>
            <div style={{
              width: "32px", height: "32px", borderRadius: "6px",
              background: "rgba(255,176,192,0.12)", border: "1px solid rgba(255,176,192,0.2)",
              display: "flex", alignItems: "center", justifyContent: "center",
              fontSize: "13px", fontWeight: "700", color: "#FFB7C9",
            }}>
              {account.username[0]?.toUpperCase()}
            </div>
            <div>
              <div style={{ fontSize: "10px", color: "#776070", lineHeight: 1 }}>Signed in as</div>
              <div style={{ fontSize: "13px", fontWeight: "700", color: "#FFD1DC" }}>{account.username}</div>
            </div>
          </div>
        ) : (
          <button onClick={onLogin} style={{
            background: "rgba(255,176,192,0.12)", border: "1px solid rgba(255,176,192,0.2)",
            color: "#FFB7C9", borderRadius: "6px", padding: "6px 14px",
            fontSize: "12px", fontWeight: "600", cursor: "pointer",
          }}>
            Sign In
          </button>
        )}
      </div>
    </header>
  );
}
