import type { Page } from "../App";

const TABS: { id: Page; label: string }[] = [
  { id: "play", label: "Play" },
  { id: "mods", label: "Mods" },
  { id: "installed", label: "Installed" },
  { id: "shaders", label: "Shaders" },
  { id: "texturepacks", label: "Textures" },
  { id: "shop", label: "Shop" },
  { id: "console", label: "Console" },
  { id: "settings", label: "Settings" },
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
      display: "flex", alignItems: "center", height: "48px", minHeight: "48px",
      background: "rgba(11,14,26,0.85)", backdropFilter: "blur(20px)",
      borderBottom: "1px solid rgba(122,162,247,0.04)",
      padding: "0 16px", gap: "4px",
      position: "relative", zIndex: 10,
      // @ts-ignore
      WebkitAppRegion: "drag",
    }}>
      {/* Logo */}
      <div style={{ display: "flex", alignItems: "center", gap: "8px", marginRight: "20px" }}>
        <div style={{
          width: "24px", height: "24px", borderRadius: "8px",
          background: "linear-gradient(135deg, rgba(122,162,247,0.15), rgba(122,162,247,0.05))",
          display: "flex", alignItems: "center", justifyContent: "center",
        }}>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="5" fill="#7AA2F7" opacity="0.3"/>
            <circle cx="12" cy="12" r="3" fill="#89B4FA" opacity="0.7"/>
            <circle cx="12" cy="12" r="1.5" fill="#B4BEFE" opacity="0.9"/>
            <circle cx="7" cy="8" r="1" fill="#B4BEFE" opacity="0.4"/>
            <circle cx="17" cy="9" r="0.8" fill="#89B4FA" opacity="0.5"/>
          </svg>
        </div>
        <span style={{
          fontWeight: "800", fontSize: "12px", letterSpacing: "0.16em",
          color: "var(--pink-200)",
        }}>
          NEBULA
        </span>
      </div>

      {/* Tabs */}
      <nav style={{ display: "flex", gap: "1px", flex: 1,
        // @ts-ignore
        WebkitAppRegion: "no-drag",
      }}>
        {TABS.map(({ id, label }) => {
          const active = page === id;
          return (
            <button key={id} onClick={() => onNavigate(id)} style={{
              padding: "6px 12px",
              background: active ? "rgba(122,162,247,0.08)" : "transparent",
              color: active ? "var(--pink-200)" : "var(--text-muted)",
              border: "none",
              borderRadius: "6px",
              fontSize: "11.5px", fontWeight: active ? "700" : "500",
              cursor: "pointer", transition: "all 0.15s",
              fontFamily: "inherit",
              letterSpacing: "0.01em",
            }}
            onMouseEnter={e => { if (!active) { e.currentTarget.style.color = "var(--text-secondary)"; e.currentTarget.style.background = "rgba(255,255,255,0.02)"; } }}
            onMouseLeave={e => { if (!active) { e.currentTarget.style.color = "var(--text-muted)"; e.currentTarget.style.background = "transparent"; } }}
            >
              {label}
            </button>
          );
        })}
      </nav>

      {/* Account */}
      <div style={{ display: "flex", alignItems: "center",
        // @ts-ignore
        WebkitAppRegion: "no-drag",
      }}>
        {account ? (
          <div style={{ display: "flex", alignItems: "center", gap: "8px", cursor: "pointer", padding: "4px 8px", borderRadius: "8px", transition: "background 0.15s" }}
            onClick={onLogout}
            onMouseEnter={e => e.currentTarget.style.background = "rgba(255,255,255,0.03)"}
            onMouseLeave={e => e.currentTarget.style.background = "transparent"}
          >
            <div style={{
              width: "26px", height: "26px", borderRadius: "7px",
              background: "linear-gradient(135deg, rgba(122,162,247,0.12), rgba(122,162,247,0.06))",
              border: "1px solid rgba(122,162,247,0.1)",
              display: "flex", alignItems: "center", justifyContent: "center",
              fontSize: "11px", fontWeight: "700", color: "var(--pink-300)",
            }}>
              {account.username[0]?.toUpperCase()}
            </div>
            <span style={{ fontSize: "12px", fontWeight: "600", color: "var(--text-secondary)" }}>{account.username}</span>
          </div>
        ) : (
          <button onClick={onLogin} style={{
            background: "rgba(122,162,247,0.06)", border: "1px solid rgba(122,162,247,0.1)",
            color: "var(--pink-300)", borderRadius: "7px", padding: "5px 14px",
            fontSize: "11px", fontWeight: "600", cursor: "pointer", fontFamily: "inherit",
            transition: "all 0.15s",
          }}
          onMouseEnter={e => { e.currentTarget.style.background = "rgba(122,162,247,0.1)"; e.currentTarget.style.borderColor = "rgba(122,162,247,0.2)"; }}
          onMouseLeave={e => { e.currentTarget.style.background = "rgba(122,162,247,0.06)"; e.currentTarget.style.borderColor = "rgba(122,162,247,0.1)"; }}
          >
            Sign In
          </button>
        )}
      </div>
    </header>
  );
}
