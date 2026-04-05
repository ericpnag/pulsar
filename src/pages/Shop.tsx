import { useState, useEffect } from "react";

interface Cosmetic {
  id: string;
  name: string;
  type: "cape" | "wings" | "hat" | "aura";
  price: number; // 0 = free
  color: string;
  emoji: string;
  description: string;
}

const COSMETICS: Cosmetic[] = [
  { id: "cape_blossom", name: "Cherry Blossom Cape", type: "cape", price: 0, color: "#FFB7C9", emoji: "🌸", description: "A flowing cape with cherry blossom petals" },
  { id: "cape_midnight", name: "Midnight Cape", type: "cape", price: 0, color: "#2d1b3d", emoji: "🌙", description: "Dark purple cape with stars" },
  { id: "cape_flame", name: "Flame Cape", type: "cape", price: 100, color: "#FF6633", emoji: "🔥", description: "Fiery animated cape" },
  { id: "cape_ice", name: "Frost Cape", type: "cape", price: 100, color: "#66CCFF", emoji: "❄️", description: "Icy crystalline cape" },
  { id: "cape_rainbow", name: "Rainbow Cape", type: "cape", price: 200, color: "#FF66AA", emoji: "🌈", description: "Color-shifting rainbow cape" },
  { id: "wings_angel", name: "Angel Wings", type: "wings", price: 0, color: "#FFFFFF", emoji: "👼", description: "White feathered angel wings" },
  { id: "wings_dragon", name: "Dragon Wings", type: "wings", price: 150, color: "#AA33FF", emoji: "🐉", description: "Purple dragon wings" },
  { id: "wings_butterfly", name: "Butterfly Wings", type: "wings", price: 100, color: "#FFB7C9", emoji: "🦋", description: "Delicate pink butterfly wings" },
  { id: "hat_crown", name: "Golden Crown", type: "hat", price: 200, color: "#FFD700", emoji: "👑", description: "A majestic golden crown" },
  { id: "hat_halo", name: "Halo", type: "hat", price: 50, color: "#FFFFAA", emoji: "😇", description: "Glowing halo above your head" },
  { id: "hat_horns", name: "Devil Horns", type: "hat", price: 75, color: "#FF4444", emoji: "😈", description: "Red devil horns" },
  { id: "aura_petals", name: "Petal Aura", type: "aura", price: 0, color: "#FFB7C9", emoji: "🌺", description: "Floating cherry blossom petals around you" },
  { id: "aura_flames", name: "Flame Aura", type: "aura", price: 150, color: "#FF6633", emoji: "🔥", description: "Fire particles swirling around you" },
  { id: "aura_sparkle", name: "Sparkle Aura", type: "aura", price: 100, color: "#AADDFF", emoji: "✨", description: "Glittering sparkle particles" },
];

const TYPE_LABELS: Record<string, string> = { cape: "Capes", wings: "Wings", hat: "Hats", aura: "Auras" };

export function ShopPage() {
  const [points, setPoints] = useState(500);
  const [owned, setOwned] = useState<string[]>([]);
  const [equipped, setEquipped] = useState<Record<string, string>>({});
  const [filter, setFilter] = useState<string>("all");

  useEffect(() => {
    const saved = localStorage.getItem("bloom-cosmetics");
    if (saved) {
      const data = JSON.parse(saved);
      setPoints(data.points ?? 500);
      setOwned(data.owned ?? []);
      setEquipped(data.equipped ?? {});
    }
  }, []);

  function save(p: number, o: string[], e: Record<string, string>) {
    setPoints(p); setOwned(o); setEquipped(e);
    localStorage.setItem("bloom-cosmetics", JSON.stringify({ points: p, owned: o, equipped: e }));
  }

  function buy(c: Cosmetic) {
    if (c.price > points) return;
    const newOwned = [...owned, c.id];
    save(points - c.price, newOwned, equipped);
  }

  function equip(c: Cosmetic) {
    const newEquipped = { ...equipped };
    if (newEquipped[c.type] === c.id) {
      delete newEquipped[c.type];
    } else {
      newEquipped[c.type] = c.id;
    }
    save(points, owned, newEquipped);
  }

  const isOwned = (id: string) => owned.includes(id) || COSMETICS.find(c => c.id === id)?.price === 0;
  const isEquipped = (id: string) => Object.values(equipped).includes(id);

  const filtered = filter === "all" ? COSMETICS : COSMETICS.filter(c => c.type === filter);

  return (
    <div style={{ display: "flex", flexDirection: "column", height: "100%", padding: "28px", gap: "16px", overflowY: "auto" }}>
      {/* Header */}
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start" }}>
        <div>
          <h2 style={{ margin: "0 0 4px", fontSize: "20px", fontWeight: "700", color: "#FFD1DC" }}>Cosmetics Shop</h2>
          <p style={{ margin: 0, fontSize: "13px", color: "#776070" }}>Customize your character with capes, wings, and more</p>
        </div>
        <div style={{
          background: "rgba(255,176,192,0.1)", border: "1px solid rgba(255,176,192,0.2)",
          borderRadius: "8px", padding: "8px 16px", display: "flex", alignItems: "center", gap: "6px",
        }}>
          <span style={{ fontSize: "16px" }}>🌸</span>
          <span style={{ fontSize: "18px", fontWeight: "800", color: "#FFD1DC" }}>{points}</span>
          <span style={{ fontSize: "11px", color: "#998899" }}>points</span>
        </div>
      </div>

      {/* Filter tabs */}
      <div style={{ display: "flex", gap: "4px" }}>
        {["all", "cape", "wings", "hat", "aura"].map(t => (
          <button key={t} onClick={() => setFilter(t)} style={{
            padding: "6px 14px", borderRadius: "6px",
            background: filter === t ? "rgba(255,176,192,0.15)" : "transparent",
            border: filter === t ? "1px solid rgba(255,176,192,0.2)" : "1px solid transparent",
            color: filter === t ? "#FFD1DC" : "#776070",
            fontSize: "12px", fontWeight: "600", cursor: "pointer",
          }}>
            {t === "all" ? "All" : TYPE_LABELS[t]}
          </button>
        ))}
      </div>

      {/* Grid */}
      <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(200px, 1fr))", gap: "10px" }}>
        {filtered.map(c => {
          const own = isOwned(c.id);
          const eq = isEquipped(c.id);
          return (
            <div key={c.id} style={{
              background: eq ? "rgba(255,176,192,0.08)" : "rgba(255,255,255,0.02)",
              border: `1px solid ${eq ? "rgba(255,176,192,0.25)" : "rgba(255,176,192,0.06)"}`,
              borderRadius: "10px", padding: "14px", position: "relative",
              transition: "all 0.15s",
            }}>
              {/* Type badge */}
              <div style={{
                position: "absolute", top: "8px", right: "8px",
                fontSize: "10px", color: "#776070", background: "rgba(0,0,0,0.3)",
                padding: "2px 6px", borderRadius: "4px",
              }}>
                {c.type}
              </div>

              {/* Icon */}
              <div style={{
                fontSize: "32px", textAlign: "center", marginBottom: "8px",
                filter: own ? "none" : "grayscale(0.5) opacity(0.6)",
              }}>
                {c.emoji}
              </div>

              {/* Name */}
              <div style={{ fontSize: "13px", fontWeight: "700", color: "#caa", textAlign: "center", marginBottom: "4px" }}>
                {c.name}
              </div>
              <div style={{ fontSize: "11px", color: "#665566", textAlign: "center", marginBottom: "10px", lineHeight: 1.3 }}>
                {c.description}
              </div>

              {/* Action button */}
              {own ? (
                <button onClick={() => equip(c)} style={{
                  width: "100%", padding: "8px",
                  background: eq ? "linear-gradient(135deg, #FFB7C9, #F8A4B8)" : "rgba(255,255,255,0.06)",
                  color: eq ? "#1a0f1a" : "#998899",
                  border: "none", borderRadius: "6px",
                  fontSize: "12px", fontWeight: "700", cursor: "pointer",
                }}>
                  {eq ? "Equipped ✓" : "Equip"}
                </button>
              ) : (
                <button onClick={() => buy(c)} disabled={c.price > points} style={{
                  width: "100%", padding: "8px",
                  background: c.price > points ? "rgba(255,255,255,0.03)" : "rgba(255,176,192,0.12)",
                  color: c.price > points ? "#554455" : "#FFB7C9",
                  border: `1px solid ${c.price > points ? "rgba(255,255,255,0.05)" : "rgba(255,176,192,0.2)"}`,
                  borderRadius: "6px", fontSize: "12px", fontWeight: "700",
                  cursor: c.price > points ? "not-allowed" : "pointer",
                  display: "flex", alignItems: "center", justifyContent: "center", gap: "4px",
                }}>
                  🌸 {c.price}
                </button>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}
