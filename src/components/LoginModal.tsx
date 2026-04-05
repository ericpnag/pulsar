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
      position: "fixed", inset: 0, background: "rgba(0,0,0,0.85)",
      display: "flex", alignItems: "center", justifyContent: "center", zIndex: 100,
    }}>
      <div style={{
        background: "#111", border: "1px solid #222", borderRadius: "12px",
        padding: "32px", width: "360px", textAlign: "center",
      }}>
        <div style={{ fontSize: "32px", marginBottom: "16px" }}>🌸</div>
        <h2 style={{ margin: "0 0 8px", fontSize: "18px", fontWeight: "700" }}>Sign in with Microsoft</h2>

        {phase === "waiting" && (
          <p style={{ color: "#555", fontSize: "13px" }}>Connecting to Microsoft...</p>
        )}

        {phase === "code" && code && (
          <>
            <p style={{ color: "#888", fontSize: "13px", margin: "0 0 16px" }}>
              Visit the link below and enter the code to sign in:
            </p>
            <a href={url} target="_blank" rel="noreferrer" style={{ color: "#fff", fontSize: "13px", display: "block", marginBottom: "16px" }}>
              {url}
            </a>
            <div style={{
              background: "#1a1a1a", border: "1px solid #333", borderRadius: "8px",
              padding: "16px", marginBottom: "16px",
            }}>
              <div style={{ fontSize: "11px", color: "#555", marginBottom: "4px", letterSpacing: "0.1em" }}>ENTER THIS CODE</div>
              <div style={{ fontSize: "28px", fontWeight: "800", letterSpacing: "0.2em", fontFamily: "monospace" }}>{code}</div>
            </div>
            <p style={{ color: "#444", fontSize: "12px", margin: 0 }}>Waiting for you to complete sign in...</p>
          </>
        )}

        {phase === "error" && (
          <>
            <p style={{ color: "#f88", fontSize: "13px", marginBottom: "16px" }}>{error}</p>
            <button onClick={onClose} style={{
              background: "#fff", color: "#000", border: "none", borderRadius: "6px",
              padding: "8px 24px", fontSize: "13px", fontWeight: "600", cursor: "pointer",
            }}>Close</button>
          </>
        )}

        {phase !== "error" && (
          <button onClick={onClose} style={{
            marginTop: "16px", background: "none", border: "none",
            color: "#444", fontSize: "12px", cursor: "pointer",
          }}>Cancel</button>
        )}
      </div>
    </div>
  );
}
