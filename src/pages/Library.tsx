export function LibraryPage({ selectedVersion }: { selectedVersion: string }) {
  return (
    <div style={{ padding: "28px", height: "100%", overflowY: "auto" }}>
      <h2 style={{ margin: "0 0 4px", fontSize: "20px", fontWeight: "700" }}>Library</h2>
      <p style={{ margin: "0 0 24px", fontSize: "13px", color: "#555" }}>Minecraft {selectedVersion}</p>

      <div style={{ border: "1px solid #1f1f1f", borderRadius: "8px", padding: "32px", textAlign: "center" }}>
        <div style={{ fontSize: "32px", marginBottom: "12px", opacity: 0.3 }}>📦</div>
        <div style={{ fontSize: "14px", color: "#555" }}>No mods installed for {selectedVersion}</div>
        <div style={{ fontSize: "12px", color: "#333", marginTop: "4px" }}>Browse the Mod Store to install mods</div>
      </div>
    </div>
  );
}
