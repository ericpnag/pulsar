import type { Page } from "../App";

const NAV: { id: Page; label: string; icon: string }[] = [
  { id: "home", label: "Home", icon: "⌂" },
  { id: "library", label: "Library", icon: "≡" },
  { id: "modstore", label: "Mod Store", icon: "⊞" },
  { id: "texturepacks", label: "Texture Packs", icon: "◈" },
  { id: "versions", label: "Versions", icon: "◷" },
  { id: "settings", label: "Settings", icon: "⚙" },
];

interface Props {
  activePage: Page;
  onNavigate: (p: Page) => void;
  account: { username: string } | null;
  onLogin: () => void;
  onLogout: () => void;
}

export function Sidebar({ activePage, onNavigate, account, onLogin, onLogout }: Props) {
  return (
    <aside style={{
      width: "200px", minWidth: "200px",
      background: "linear-gradient(180deg, #150d18 0%, #1a1020 100%)",
      borderRight: "1px solid rgba(255,176,192,0.08)",
      display: "flex", flexDirection: "column",
    }}>
      {/* Logo */}
      <div style={{ padding: "20px 16px 16px", borderBottom: "1px solid rgba(255,176,192,0.08)" }}>
        <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
          <span style={{ fontSize: "18px" }}>🌸</span>
          <span style={{ fontWeight: "700", fontSize: "13px", letterSpacing: "0.1em", color: "#FFD1DC" }}>BLOOM</span>
        </div>
      </div>

      {/* Nav */}
      <nav style={{ flex: 1, padding: "8px" }}>
        {NAV.map(({ id, label, icon }) => {
          const active = activePage === id;
          return (
            <button key={id} onClick={() => onNavigate(id)} style={{
              display: "flex", alignItems: "center", gap: "10px",
              width: "100%", padding: "9px 12px", marginBottom: "2px",
              background: active ? "rgba(255,176,192,0.12)" : "transparent",
              color: active ? "#FFD1DC" : "#776070",
              border: "none",
              borderLeft: active ? "2px solid #FFB7C9" : "2px solid transparent",
              borderRadius: "0 6px 6px 0",
              fontSize: "13px", fontWeight: active ? "600" : "400",
              cursor: "pointer", textAlign: "left", transition: "all 0.15s",
            }}
            onMouseEnter={e => { if (!active) { e.currentTarget.style.background = "rgba(255,176,192,0.06)"; e.currentTarget.style.color = "#caa"; }}}
            onMouseLeave={e => { if (!active) { e.currentTarget.style.background = "transparent"; e.currentTarget.style.color = "#776070"; }}}
            >
              <span style={{ fontSize: "14px", width: "16px", textAlign: "center" }}>{icon}</span>
              <span>{label}</span>
            </button>
          );
        })}
      </nav>

      {/* Account strip */}
      <div style={{ padding: "12px 16px", borderTop: "1px solid rgba(255,176,192,0.08)" }}>
        {account ? (
          <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
            <div style={{
              width: "28px", height: "28px", borderRadius: "6px",
              background: "rgba(255,176,192,0.1)", border: "1px solid rgba(255,176,192,0.15)",
              display: "flex", alignItems: "center", justifyContent: "center", fontSize: "12px", color: "#FFB7C9",
            }}>
              {account.username[0]?.toUpperCase()}
            </div>
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: "12px", fontWeight: "600", color: "#caa" }}>{account.username}</div>
              <button onClick={onLogout} style={{ fontSize: "11px", color: "#554050", background: "none", border: "none", cursor: "pointer", padding: 0 }}
                onMouseEnter={e => e.currentTarget.style.color = "#FFB7C9"}
                onMouseLeave={e => e.currentTarget.style.color = "#554050"}
              >Sign out</button>
            </div>
          </div>
        ) : (
          <button onClick={onLogin} style={{
            width: "100%",
            background: "linear-gradient(135deg, rgba(255,183,201,0.15), rgba(248,164,184,0.1))",
            color: "#FFB7C9", border: "1px solid rgba(255,176,192,0.2)",
            borderRadius: "6px", padding: "8px", fontSize: "13px", fontWeight: "600", cursor: "pointer",
            transition: "all 0.15s",
          }}
          onMouseEnter={e => { e.currentTarget.style.background = "rgba(255,176,192,0.2)"; }}
          onMouseLeave={e => { e.currentTarget.style.background = "linear-gradient(135deg, rgba(255,183,201,0.15), rgba(248,164,184,0.1))"; }}
          >Sign In</button>
        )}
      </div>
    </aside>
  );
}
