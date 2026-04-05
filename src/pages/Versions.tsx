export function VersionsPage({ versions, selected, onSelect }: { versions: string[]; selected: string; onSelect: (v: string) => void }) {
  return (
    <div style={{ padding: "28px", height: "100%", overflowY: "auto" }}>
      <h2 style={{ margin: "0 0 4px", fontSize: "20px", fontWeight: "700" }}>Versions</h2>
      <p style={{ margin: "0 0 24px", fontSize: "13px", color: "#555" }}>Select a Minecraft version to play</p>

      <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(160px, 1fr))", gap: "8px" }}>
        {versions.map((v, i) => {
          const isSelected = v === selected;
          return (
            <button key={v} onClick={() => onSelect(v)} style={{
              background: isSelected ? "#fff" : "#161616",
              color: isSelected ? "#000" : "#888",
              border: `1px solid ${isSelected ? "#fff" : "#222"}`,
              borderRadius: "8px", padding: "16px", fontSize: "13px",
              fontWeight: isSelected ? "700" : "400",
              cursor: "pointer", textAlign: "left", transition: "all 0.15s",
            }}
            onMouseEnter={e => { if (!isSelected) { e.currentTarget.style.borderColor = "#555"; e.currentTarget.style.color = "#fff"; }}}
            onMouseLeave={e => { if (!isSelected) { e.currentTarget.style.borderColor = "#222"; e.currentTarget.style.color = "#888"; }}}
            >
              <div style={{ fontSize: "11px", marginBottom: "4px", opacity: 0.5 }}>{i === 0 ? "LATEST" : "RELEASE"}</div>
              <div>{v}</div>
            </button>
          );
        })}
      </div>
    </div>
  );
}
