import { openUrl } from "@tauri-apps/plugin-opener";

interface Props {
  phase: "waiting" | "code" | "error";
  code?: string;
  url?: string;
  error?: string;
  onClose: () => void;
}

export function LoginModal({ phase, code, url, error, onClose }: Props) {
  return (
    <div style={{
      position: "fixed", inset: 0, background: "rgba(0,0,0,0.8)", backdropFilter: "blur(8px)",
      display: "flex", alignItems: "center", justifyContent: "center", zIndex: 100,
    }} onClick={onClose}>
      <div onClick={e => e.stopPropagation()} className="fade-in" style={{
        background: "linear-gradient(180deg, rgba(19,23,41,0.98), rgba(11,14,26,0.98))",
        border: "1px solid rgba(122,162,247,0.12)",
        borderRadius: "16px", padding: "36px", width: "380px", textAlign: "center",
        boxShadow: "0 24px 64px rgba(0,0,0,0.5), 0 0 80px rgba(122,162,247,0.05)",
      }}>
        {/* Nebula icon */}
        <div style={{
          width: "56px", height: "56px", margin: "0 auto 20px",
          borderRadius: "16px",
          background: "linear-gradient(135deg, rgba(122,162,247,0.15), rgba(122,162,247,0.05))",
          border: "1px solid rgba(122,162,247,0.15)",
          display: "flex", alignItems: "center", justifyContent: "center",
          fontSize: "24px",
        }}>
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="5" fill="#7AA2F7" opacity="0.3"/>
            <circle cx="12" cy="12" r="3" fill="#89B4FA" opacity="0.7"/>
            <circle cx="12" cy="12" r="1.5" fill="#B4BEFE" opacity="0.9"/>
            <circle cx="7" cy="8" r="1" fill="#B4BEFE" opacity="0.4"/>
            <circle cx="17" cy="9" r="0.8" fill="#89B4FA" opacity="0.5"/>
          </svg>
        </div>

        <h2 style={{ margin: "0 0 8px", fontSize: "18px", fontWeight: "700", color: "var(--pink-light)" }}>
          Sign in with Microsoft
        </h2>

        {phase === "waiting" && (
          <div>
            <p style={{ color: "var(--text-dim)", fontSize: "13px", marginBottom: "20px" }}>Connecting to Microsoft...</p>
            <div style={{
              width: "32px", height: "32px", margin: "0 auto",
              border: "3px solid rgba(122,162,247,0.1)", borderTopColor: "var(--pink)",
              borderRadius: "50%",
              animation: "spin 0.8s linear infinite",
            }} />
            <style>{`@keyframes spin { to { transform: rotate(360deg); } }`}</style>
          </div>
        )}

        {phase === "code" && code && (
          <>
            <p style={{ color: "var(--text-muted)", fontSize: "13px", margin: "0 0 12px", lineHeight: 1.5 }}>
              Click the button to open the login page, then enter the code:
            </p>
            <button
              onClick={() => { if (url) openUrl(url); }}
              style={{
                background: "linear-gradient(135deg, var(--pink-300, #7AA2F7), var(--pink-400, #5B6EAE))",
                color: "#0B0E1A", border: "none", borderRadius: "8px",
                padding: "10px 24px", fontSize: "13px", fontWeight: "700",
                cursor: "pointer", fontFamily: "inherit", marginBottom: "12px",
                transition: "all 0.2s",
              }}
              onMouseEnter={e => e.currentTarget.style.transform = "translateY(-1px)"}
              onMouseLeave={e => e.currentTarget.style.transform = "translateY(0)"}
            >
              Open Login Page
            </button>
            <div style={{ fontSize: "11px", color: "var(--text-faint)", marginBottom: "16px" }}>
              {url}
            </div>
            <div style={{
              background: "rgba(0,0,0,0.3)", border: "1px solid rgba(122,162,247,0.12)",
              borderRadius: "12px", padding: "20px", marginBottom: "20px",
            }}>
              <div style={{ fontSize: "10px", color: "var(--text-dim)", marginBottom: "8px", letterSpacing: "0.15em", fontWeight: "700" }}>ENTER THIS CODE</div>
              <div style={{
                fontSize: "32px", fontWeight: "800", letterSpacing: "0.25em",
                fontFamily: "monospace", color: "#fff",
              }}>{code}</div>
            </div>
            <p style={{ color: "var(--text-faint)", fontSize: "12px", margin: 0 }}>Waiting for sign in...</p>
          </>
        )}

        {phase === "error" && (
          <>
            <div style={{
              fontSize: "13px", color: "var(--red)", padding: "12px 16px", marginBottom: "20px",
              background: "rgba(255,60,60,0.06)", border: "1px solid rgba(255,60,60,0.15)",
              borderRadius: "8px", lineHeight: 1.5,
            }}>
              {error}
            </div>
            <button className="bloom-btn" onClick={onClose} style={{ padding: "10px 32px" }}>Close</button>
          </>
        )}

        {phase !== "error" && (
          <button onClick={onClose} style={{
            marginTop: "20px", background: "none", border: "none",
            color: "var(--text-faint)", fontSize: "12px", cursor: "pointer", fontFamily: "inherit",
            transition: "color 0.15s",
          }}
          onMouseEnter={e => e.currentTarget.style.color = "var(--text-muted)"}
          onMouseLeave={e => e.currentTarget.style.color = "var(--text-faint)"}
          >Cancel</button>
        )}
      </div>
    </div>
  );
}
