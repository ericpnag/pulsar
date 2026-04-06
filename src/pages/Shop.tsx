import { useState, useEffect } from "react";

const PREVIEW_ANIMATIONS = `
@keyframes pv-float { 0%,100%{transform:translateY(0)} 50%{transform:translateY(-3px)} }
@keyframes pv-twinkle { 0%,100%{opacity:0.3} 50%{opacity:1} }
@keyframes pv-flicker { 0%{transform:scaleY(1);opacity:0.6} 25%{transform:scaleY(1.15);opacity:0.75} 50%{transform:scaleY(0.9);opacity:0.55} 75%{transform:scaleY(1.1);opacity:0.7} 100%{transform:scaleY(1);opacity:0.6} }
@keyframes pv-wave { 0%{transform:translateX(0)} 50%{transform:translateX(3px)} 100%{transform:translateX(0)} }
@keyframes pv-spin { from{transform:rotate(0deg)} to{transform:rotate(360deg)} }
@keyframes pv-pulse { 0%,100%{opacity:0.15;r:12} 50%{opacity:0.3;r:14} }
@keyframes pv-orbit { 0%{transform:rotate(0deg) translateX(25px) rotate(0deg)} 100%{transform:rotate(360deg) translateX(25px) rotate(-360deg)} }
@keyframes pv-glow { 0%,100%{opacity:0.2} 50%{opacity:0.4} }
@keyframes pv-drift { 0%{transform:translate(0,0)} 33%{transform:translate(2px,-2px)} 66%{transform:translate(-1px,1px)} 100%{transform:translate(0,0)} }
@keyframes pv-shimmer { 0%,100%{opacity:0.2} 40%{opacity:0.6} 60%{opacity:0.5} }
`;

interface Cosmetic {
  id: string;
  name: string;
  type: "cape" | "wings" | "hat" | "aura";
  price: number;
  color: string;
  color2: string;
  description: string;
}

const COSMETICS: Cosmetic[] = [
  { id: "cape_blossom", name: "Cherry Blossom", type: "cape", price: 0, color: "#FFBED0", color2: "#D77890", description: "Pink cape with blossom petals" },
  { id: "cape_midnight", name: "Midnight", type: "cape", price: 500, color: "#1E0F32", color2: "#0F0820", description: "Dark purple with starlight" },
  { id: "cape_frost", name: "Frost", type: "cape", price: 750, color: "#8CC8FF", color2: "#508CDC", description: "Icy blue crystal cape" },
  { id: "cape_flame", name: "Flame", type: "cape", price: 750, color: "#FF7828", color2: "#C83C14", description: "Fiery orange-red cape" },
  { id: "cape_ocean", name: "Ocean", type: "cape", price: 1000, color: "#1450A0", color2: "#0A2864", description: "Deep blue ocean waves" },
  { id: "cape_emerald", name: "Emerald", type: "cape", price: 1250, color: "#1E9646", color2: "#0F6428", description: "Green with gem sparkles" },
  { id: "cape_sunset", name: "Sunset", type: "cape", price: 1500, color: "#FF9650", color2: "#783296", description: "Orange to purple gradient" },
  { id: "cape_galaxy", name: "Galaxy", type: "cape", price: 2500, color: "#0F081E", color2: "#05020F", description: "Dark with colorful stars" },
  { id: "wings_angel", name: "Angel Wings", type: "wings", price: 0, color: "#FFFFFF", color2: "#DDDDEE", description: "White feathered wings" },
  { id: "wings_dragon", name: "Dragon Wings", type: "wings", price: 2000, color: "#AA33FF", color2: "#6611AA", description: "Purple dragon wings" },
  { id: "wings_butterfly", name: "Butterfly Wings", type: "wings", price: 1000, color: "#FFB7C9", color2: "#E88AA0", description: "Delicate pink wings" },
  { id: "hat_crown", name: "Golden Crown", type: "hat", price: 2500, color: "#FFD700", color2: "#CC9900", description: "A majestic golden crown" },
  { id: "hat_halo", name: "Halo", type: "hat", price: 500, color: "#FFFFAA", color2: "#DDDD77", description: "Glowing halo above head" },
  { id: "hat_horns", name: "Devil Horns", type: "hat", price: 750, color: "#FF4444", color2: "#CC2222", description: "Red devil horns" },
  { id: "aura_petals", name: "Petal Aura", type: "aura", price: 0, color: "#FFB7C9", color2: "#F090A8", description: "Floating cherry petals" },
  { id: "aura_flames", name: "Flame Aura", type: "aura", price: 1500, color: "#FF6633", color2: "#DD4411", description: "Fire particles around you" },
  { id: "aura_sparkle", name: "Sparkle Aura", type: "aura", price: 1000, color: "#AADDFF", color2: "#77AADD", description: "Glittering sparkles" },
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
    save(points - c.price, [...owned, c.id], equipped);
  }

  function equip(c: Cosmetic) {
    const newEquipped = { ...equipped };
    if (newEquipped[c.type] === c.id) delete newEquipped[c.type];
    else newEquipped[c.type] = c.id;
    save(points, owned, newEquipped);
  }

  const isOwned = (id: string) => owned.includes(id) || COSMETICS.find(c => c.id === id)?.price === 0;
  const isEquipped = (id: string) => Object.values(equipped).includes(id);

  const filtered = filter === "all" ? COSMETICS : COSMETICS.filter(c => c.type === filter);

  return (
    <div className="fade-in" style={{ display: "flex", flexDirection: "column", height: "100%", padding: "28px", gap: "16px", overflowY: "auto" }}>
      {/* Header */}
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start" }}>
        <div>
          <h2 className="page-title">Cosmetics Shop</h2>
          <p className="page-subtitle">Customize your character</p>
        </div>
        <div className="bloom-card" style={{
          padding: "10px 18px", display: "flex", alignItems: "center", gap: "8px",
        }}>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="3" fill="#FFB7C9"/>
            <ellipse cx="12" cy="6" rx="2.5" ry="3.5" fill="#FFB7C9" opacity="0.6"/>
            <ellipse cx="17" cy="10" rx="2.5" ry="3.5" fill="#F8A4B8" opacity="0.5" transform="rotate(72 12 12)"/>
            <ellipse cx="15" cy="17" rx="2.5" ry="3.5" fill="#FFD1DC" opacity="0.4" transform="rotate(144 12 12)"/>
            <ellipse cx="9" cy="17" rx="2.5" ry="3.5" fill="#F8A4B8" opacity="0.5" transform="rotate(216 12 12)"/>
            <ellipse cx="7" cy="10" rx="2.5" ry="3.5" fill="#FFB7C9" opacity="0.6" transform="rotate(288 12 12)"/>
          </svg>
          <span style={{ fontSize: "18px", fontWeight: "800", color: "var(--pink-light)" }}>{points}</span>
          <span style={{ fontSize: "11px", color: "var(--text-muted)" }}>points</span>
        </div>
      </div>

      {/* Filter tabs */}
      <div style={{ display: "flex", gap: "4px" }}>
        {["all", "cape", "wings", "hat", "aura"].map(t => (
          <button key={t} onClick={() => setFilter(t)} style={{
            padding: "7px 16px", borderRadius: "8px",
            background: filter === t ? "rgba(255,176,192,0.1)" : "transparent",
            border: filter === t ? "1px solid rgba(255,176,192,0.15)" : "1px solid transparent",
            color: filter === t ? "var(--pink-light)" : "var(--text-dim)",
            fontSize: "12px", fontWeight: "600", cursor: "pointer",
            transition: "all 0.15s", fontFamily: "inherit",
          }}
          onMouseEnter={e => { if (filter !== t) e.currentTarget.style.color = "var(--text)"; }}
          onMouseLeave={e => { if (filter !== t) e.currentTarget.style.color = "var(--text-dim)"; }}
          >
            {t === "all" ? "All" : TYPE_LABELS[t]}
          </button>
        ))}
      </div>

      <style>{PREVIEW_ANIMATIONS}</style>
      {/* Grid */}
      <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(190px, 1fr))", gap: "10px" }}>
        {filtered.map(c => {
          const own = isOwned(c.id);
          const eq = isEquipped(c.id);
          return (
            <div key={c.id} className="bloom-card" style={{
              padding: "16px", position: "relative",
              borderColor: eq ? "rgba(255,176,192,0.25)" : undefined,
              background: eq ? "rgba(255,176,192,0.06)" : undefined,
            }}>
              {/* Type badge */}
              <div style={{
                position: "absolute", top: "10px", right: "10px",
                fontSize: "10px", color: "var(--text-dim)",
                background: "rgba(0,0,0,0.3)", padding: "2px 8px", borderRadius: "4px",
                fontWeight: "600", textTransform: "uppercase", letterSpacing: "0.05em",
              }}>
                {c.type}
              </div>

              {/* Preview */}
              <div style={{
                width: "100%", height: "90px", margin: "0 auto 8px",
                borderRadius: "8px", overflow: "hidden",
                filter: own ? "none" : "grayscale(0.6) opacity(0.5)",
              }}>
                <CosmeticPreview cosmetic={c} />
              </div>

              {/* Name */}
              <div style={{ fontSize: "13px", fontWeight: "700", color: "var(--text)", textAlign: "center", marginBottom: "4px" }}>
                {c.name}
              </div>
              <div style={{ fontSize: "11px", color: "var(--text-faint)", textAlign: "center", marginBottom: "12px", lineHeight: 1.4 }}>
                {c.description}
              </div>

              {/* Action */}
              {own ? (
                <button onClick={() => equip(c)} className="bloom-btn" style={{
                  width: "100%", padding: "8px",
                  background: eq ? "linear-gradient(135deg, var(--pink), var(--pink-soft))" : "rgba(255,255,255,0.04)",
                  color: eq ? "#1a0f1a" : "var(--text-muted)",
                  fontSize: "12px",
                }}>
                  {eq ? "Equipped" : "Equip"}
                </button>
              ) : (
                <button onClick={() => buy(c)} disabled={c.price > points}
                  className="bloom-btn-ghost"
                  style={{
                    width: "100%", padding: "8px",
                    borderColor: c.price > points ? "rgba(255,255,255,0.04)" : "rgba(255,176,192,0.15)",
                    color: c.price > points ? "var(--text-faint)" : "var(--pink)",
                    cursor: c.price > points ? "not-allowed" : "pointer",
                    display: "flex", alignItems: "center", justifyContent: "center", gap: "6px",
                    fontSize: "12px",
                  }}
                >
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
                    <circle cx="12" cy="12" r="3" fill="currentColor" opacity="0.6"/>
                    <ellipse cx="12" cy="6" rx="2.5" ry="3.5" fill="currentColor" opacity="0.4"/>
                    <ellipse cx="17" cy="10" rx="2.5" ry="3.5" fill="currentColor" opacity="0.3" transform="rotate(72 12 12)"/>
                  </svg>
                  {c.price}
                </button>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}

function CosmeticPreview({ cosmetic: c }: { cosmetic: Cosmetic }) {
  const W = 180, H = 100;
  const cx = W / 2;

  // Shared defs
  const defs = (
    <defs>
      <linearGradient id={`bg-${c.id}`} x1="0" y1="0" x2="0.3" y2="1">
        <stop offset="0%" stopColor={c.color} stopOpacity="0.12" />
        <stop offset="100%" stopColor={c.color2} stopOpacity="0.04" />
      </linearGradient>
      <linearGradient id={`fg-${c.id}`} x1="0" y1="0" x2="0" y2="1">
        <stop offset="0%" stopColor={c.color} />
        <stop offset="100%" stopColor={c.color2} />
      </linearGradient>
      <radialGradient id={`glow-${c.id}`}>
        <stop offset="0%" stopColor={c.color} stopOpacity="0.25" />
        <stop offset="100%" stopColor={c.color} stopOpacity="0" />
      </radialGradient>
      <filter id={`blur-${c.id}`}><feGaussianBlur stdDeviation="3" /></filter>
    </defs>
  );

  if (c.type === "cape") return (
    <svg width={W} height={H} viewBox={`0 0 ${W} ${H}`} style={{ display: "block" }}>
      {defs}
      <rect width={W} height={H} fill={`url(#bg-${c.id})`} />
      {/* Glow behind cape */}
      <ellipse cx={cx} cy={50} rx="40" ry="35" fill={`url(#glow-${c.id})`} style={{animation:"pv-glow 3s ease-in-out infinite"}} />
      {/* Cape - 3D perspective shape */}
      <path d={`M${cx-24} 6 C${cx-20} 4, ${cx+20} 4, ${cx+24} 6 L${cx+30} 88 C${cx+28} 92, ${cx-28} 92, ${cx-30} 88 Z`}
        fill={`url(#fg-${c.id})`} stroke="rgba(255,255,255,0.12)" strokeWidth="0.5" />
      {/* Shoulder fold */}
      <path d={`M${cx-24} 6 C${cx-15} 14, ${cx+15} 14, ${cx+24} 6`} fill="none" stroke="rgba(255,255,255,0.1)" strokeWidth="0.5" />
      {/* Center fold line */}
      <line x1={cx} y1={8} x2={cx} y2={88} stroke="rgba(0,0,0,0.08)" strokeWidth="0.5" />
      {/* Shine highlight */}
      <path d={`M${cx-22} 8 C${cx-14} 12, ${cx-10} 12, ${cx-2} 8 L${cx-4} 35 C${cx-12} 38, ${cx-18} 36, ${cx-24} 32 Z`}
        fill="rgba(255,255,255,0.08)" />
      {/* Patterns */}
      {c.id === "cape_blossom" && <>
        {[[cx-12,22],[cx+8,18],[cx-6,40],[cx+10,48],[cx-10,62],[cx+5,70],[cx,32],[cx-4,80]].map(([x,y],i) => (
          <g key={i} opacity={0.6 + (i%3)*0.12} style={{animation:`pv-float ${2.5+i*0.4}s ease-in-out ${i*0.3}s infinite`,transformOrigin:`${x}px ${y}px`}}>
            {[0,72,144,216,288].map((a,j) => (
              <ellipse key={j} cx={x} cy={(y as number)-4} rx="2" ry="4" fill="#FFE0EC"
                transform={`rotate(${a} ${x} ${y})`} />
            ))}
            <circle cx={x} cy={y} r="2" fill="#FFF0F4" />
            <circle cx={x} cy={y} r="0.8" fill="#FFD0DD" />
          </g>
        ))}
      </>}
      {c.id === "cape_midnight" && <>
        {/* Big stars */}
        {[[cx-10,20],[cx+12,35],[cx-5,55],[cx+8,72]].map(([x,y],i) => (
          <g key={i}>
            <circle cx={x} cy={y} r="1.5" fill="#fff" opacity="0.9" />
            <line x1={(x as number)-4} y1={y} x2={(x as number)+4} y2={y} stroke="#DDDDFF" strokeWidth="0.3" opacity="0.5" />
            <line x1={x} y1={(y as number)-4} x2={x} y2={(y as number)+4} stroke="#DDDDFF" strokeWidth="0.3" opacity="0.5" />
          </g>
        ))}
        {/* Small stars */}
        {[[cx+3,15],[cx-14,32],[cx+15,45],[cx-8,48],[cx+6,60],[cx-12,70],[cx+10,82],[cx-3,25],[cx+4,38],[cx-10,85],[cx+14,18],[cx,65]].map(([x,y],i) => (
          <circle key={i} cx={x} cy={y} r={0.5 + (i%3)*0.4} fill={i%3===0?"#CCCCFF":i%3===1?"#FFE0FF":"#E0E0FF"}
            style={{animation:`pv-twinkle ${1.5+i*0.3}s ease-in-out ${i*0.2}s infinite`}} />
        ))}
        {/* Crescent moon */}
        <circle cx={cx+6} cy={28} r="5" fill="#EEEEFF" opacity="0.15" />
        <circle cx={cx+8} cy={27} r="4" fill={c.color} />
      </>}
      {c.id === "cape_frost" && <>
        {/* Ice crystals */}
        {[[cx-8,25],[cx+10,45],[cx-4,65],[cx+6,80],[cx-12,48]].map(([x,y],i) => (
          <g key={i} opacity={0.5 + i*0.08}>
            {[0,60,120].map(a => (
              <line key={a} x1={x} y1={y} x2={(x as number)+Math.cos(a*Math.PI/180)*8} y2={(y as number)+Math.sin(a*Math.PI/180)*8}
                stroke="#D0EEFF" strokeWidth="1" />
            ))}
            {[30,90,150].map(a => (
              <line key={a} x1={x} y1={y} x2={(x as number)+Math.cos(a*Math.PI/180)*5} y2={(y as number)+Math.sin(a*Math.PI/180)*5}
                stroke="#B0DDFF" strokeWidth="0.5" />
            ))}
            <circle cx={x} cy={y} r="1.5" fill="#E8F4FF" opacity="0.8" />
          </g>
        ))}
        {/* Frost shimmer */}
        {Array.from({length:8}).map((_,i) => (
          <circle key={i} cx={cx-15+i*4+Math.sin(i)*3} cy={15+i*10} r="0.6" fill="#fff"
            style={{animation:`pv-shimmer ${1.2+i*0.2}s ease-in-out ${i*0.15}s infinite`}} />
        ))}
      </>}
      {c.id === "cape_flame" && <>
        {/* Fire base glow */}
        <ellipse cx={cx} cy={85} rx="28" ry="10" fill="#FF4400" opacity="0.15" filter={`url(#blur-${c.id})`} />
        {/* Flames */}
        {[0,1,2,3,4,5,6,7].map(i => {
          const fx = cx - 20 + i * 6;
          const fh = 18 + Math.sin(i*1.6)*10;
          const fw = 4 + (i%2)*2;
          return <path key={i} d={`M${fx} 90 Q${fx+fw/3} ${90-fh*0.6} ${fx+fw/2} ${90-fh} Q${fx+fw*0.7} ${90-fh*0.6} ${fx+fw} 90`}
            fill={i%3===0?"#FFD030":i%3===1?"#FF8020":"#FF5010"}
            style={{animation:`pv-flicker ${0.6+i*0.15}s ease-in-out ${i*0.08}s infinite`,transformOrigin:`${fx+fw/2}px 90px`}} />;
        })}
        {/* Ember particles */}
        {[[cx-8,30],[cx+12,25],[cx-14,40],[cx+6,20],[cx,35]].map(([x,y],i) => (
          <circle key={i} cx={x} cy={y} r={1+(i%2)} fill="#FFD060" opacity={0.3+i*0.08} />
        ))}
      </>}
      {c.id === "cape_ocean" && <>
        {/* Waves */}
        {[28,40,52,64,76].map((y,i) => (
          <path key={i} d={`M${cx-28} ${y} Q${cx-14} ${y-4-i} ${cx} ${y} Q${cx+14} ${y+4+i} ${cx+28} ${y}`}
            fill="none" stroke={i%2===0?"#60B0EE":"#3088CC"} strokeWidth={1.5-i*0.15} opacity={0.25+i*0.06}
            style={{animation:`pv-wave ${2+i*0.3}s ease-in-out ${i*0.4}s infinite`}} />
        ))}
        {/* Foam highlights */}
        {[[cx-16,30],[cx+10,42],[cx-8,56],[cx+14,68]].map(([x,y],i) => (
          <ellipse key={i} cx={x} cy={y} rx="5" ry="1.5" fill="rgba(255,255,255,0.12)" />
        ))}
      </>}
      {c.id === "cape_emerald" && <>
        {/* Gem shapes */}
        {[[cx-10,25,7],[cx+8,45,9],[cx-5,65,8],[cx+12,30,6],[cx-12,50,5]].map(([x,y,s],i) => (
          <g key={i} style={{animation:`pv-shimmer ${2+i*0.5}s ease-in-out ${i*0.3}s infinite`}}>
            <polygon points={`${x},${(y as number)-(s as number)} ${(x as number)+(s as number)*0.6},${y} ${x},${(y as number)+(s as number)*0.7} ${(x as number)-(s as number)*0.6},${y}`}
              fill="#50FF90" opacity={0.25+i*0.06} />
            <polygon points={`${x},${(y as number)-(s as number)} ${(x as number)+(s as number)*0.3},${(y as number)-(s as number)*0.2} ${x},${(y as number)+(s as number)*0.3} ${(x as number)-(s as number)*0.3},${(y as number)-(s as number)*0.2}`}
              fill="#A0FFD0" opacity={0.2} />
            <circle cx={x} cy={(y as number)-(s as number)*0.3} r="0.8" fill="#EEFFEE" opacity="0.6" />
          </g>
        ))}
      </>}
      {c.id === "cape_sunset" && <>
        {/* Sun */}
        <circle cx={cx} cy={25} r="12" fill="#FFD060" opacity="0.2" filter={`url(#blur-${c.id})`} style={{animation:"pv-glow 3s ease-in-out infinite"}} />
        <circle cx={cx} cy={25} r="7" fill="#FFE080" opacity="0.25" style={{animation:"pv-pulse 3s ease-in-out infinite"}} />
        <circle cx={cx} cy={25} r="4" fill="#FFF0B0" opacity="0.4" />
        {/* Sun rays */}
        {[0,45,90,135,180,225,270,315].map(a => (
          <line key={a} x1={cx+Math.cos(a*Math.PI/180)*9} y1={25+Math.sin(a*Math.PI/180)*9}
            x2={cx+Math.cos(a*Math.PI/180)*16} y2={25+Math.sin(a*Math.PI/180)*16}
            stroke="#FFD060" strokeWidth="0.5" opacity="0.2" />
        ))}
        {/* Horizon clouds */}
        {[[cx-18,55],[cx+10,50],[cx-5,60]].map(([x,y],i) => (
          <ellipse key={i} cx={x} cy={y} rx="10" ry="3" fill="rgba(255,180,120,0.12)" />
        ))}
      </>}
      {c.id === "cape_galaxy" && <>
        {/* Nebula glow */}
        <ellipse cx={cx-5} cy={40} rx="15" ry="20" fill="#6030A0" opacity="0.12" filter={`url(#blur-${c.id})`} style={{animation:"pv-glow 4s ease-in-out infinite"}} />
        <ellipse cx={cx+8} cy={60} rx="12" ry="15" fill="#3050A0" opacity="0.1" filter={`url(#blur-${c.id})`} style={{animation:"pv-glow 5s ease-in-out 1s infinite"}} />
        {/* Stars */}
        {[[cx-10,18,"#C8B0FF",1.8],[cx+14,28,"#FFB0C8",1.5],[cx-6,42,"#B0D0FF",1.2],[cx+10,55,"#FFE0B0",1],
          [cx+3,15,"#FFFFFF",2],[cx-14,35,"#D0B0FF",0.8],[cx+16,45,"#B0FFD0",1],[cx-3,68,"#FFB0E0",1.3],
          [cx+8,75,"#AACCFF",0.9],[cx-10,80,"#FFCCDD",0.7],[cx+2,85,"#CCDDFF",0.6],
          [cx-8,12,"#fff",0.5],[cx+12,65,"#fff",0.5],[cx-15,58,"#DDC0FF",0.8]
        ].map(([x,y,col,r],i) => (
          <g key={i} style={{animation:`pv-twinkle ${2+i*0.3}s ease-in-out ${i*0.25}s infinite`}}>
            <circle cx={x as number} cy={y as number} r={r as number} fill={col as string} opacity={0.5+(i%4)*0.12} />
            {(r as number) > 1.3 && <>
              <line x1={(x as number)-3} y1={y as number} x2={(x as number)+3} y2={y as number} stroke={col as string} strokeWidth="0.3" opacity="0.4" />
              <line x1={x as number} y1={(y as number)-3} x2={x as number} y2={(y as number)+3} stroke={col as string} strokeWidth="0.3" opacity="0.4" />
            </>}
          </g>
        ))}
      </>}
    </svg>
  );

  if (c.type === "wings") return (
    <svg width={W} height={H} viewBox={`0 0 ${W} ${H}`} style={{ display: "block" }}>
      {defs}
      <rect width={W} height={H} fill={`url(#bg-${c.id})`} />
      <ellipse cx={cx} cy={55} rx="50" ry="30" fill={`url(#glow-${c.id})`} />
      {/* Left wing */}
      <path d={`M${cx} 80 Q${cx-20} 65 ${cx-45} 20 Q${cx-50} 12 ${cx-42} 10 Q${cx-30} 15 ${cx-20} 25 Q${cx-10} 40 ${cx} 65`}
        fill={`url(#fg-${c.id})`} opacity="0.75" stroke="rgba(255,255,255,0.1)" strokeWidth="0.5" />
      {/* Right wing */}
      <path d={`M${cx} 80 Q${cx+20} 65 ${cx+45} 20 Q${cx+50} 12 ${cx+42} 10 Q${cx+30} 15 ${cx+20} 25 Q${cx+10} 40 ${cx} 65`}
        fill={`url(#fg-${c.id})`} opacity="0.75" stroke="rgba(255,255,255,0.1)" strokeWidth="0.5" />
      {/* Feather details */}
      {[0,1,2,3,4].map(i => (<g key={i}>
        <line x1={cx-10-i*7} y1={22+i*12} x2={cx-3} y2={48+i*6} stroke="rgba(255,255,255,0.12)" strokeWidth="0.5" />
        <line x1={cx+10+i*7} y1={22+i*12} x2={cx+3} y2={48+i*6} stroke="rgba(255,255,255,0.12)" strokeWidth="0.5" />
      </g>))}
      {/* Wing highlight */}
      <path d={`M${cx} 70 Q${cx-15} 55 ${cx-35} 22 Q${cx-30} 18 ${cx-22} 22 Q${cx-12} 35 ${cx} 58`}
        fill="rgba(255,255,255,0.06)" />
    </svg>
  );

  if (c.type === "hat") return (
    <svg width={W} height={H} viewBox={`0 0 ${W} ${H}`} style={{ display: "block" }}>
      {defs}
      <rect width={W} height={H} fill={`url(#bg-${c.id})`} />
      <ellipse cx={cx} cy={50} rx="35" ry="25" fill={`url(#glow-${c.id})`} />
      {c.id.includes("crown") ? <>
        {/* Crown body */}
        <path d={`M${cx-30} 65 L${cx-30} 30 L${cx-15} 45 L${cx} 20 L${cx+15} 45 L${cx+30} 30 L${cx+30} 65 Z`}
          fill={`url(#fg-${c.id})`} stroke="rgba(255,255,255,0.15)" strokeWidth="0.5" />
        {/* Crown band */}
        <rect x={cx-32} y={65} width="64" height="10" rx="2" fill={c.color2} stroke="rgba(255,255,255,0.1)" strokeWidth="0.5" />
        {/* Jewels */}
        <circle cx={cx-15} cy={35} r="3.5" fill="#FF3030" opacity="0.7" />
        <circle cx={cx} cy={25} r="4" fill="#3060FF" opacity="0.7" />
        <circle cx={cx+15} cy={35} r="3.5" fill="#30CC30" opacity="0.7" />
        {/* Jewel shine */}
        <circle cx={cx-14} cy={34} r="1" fill="#fff" opacity="0.5" />
        <circle cx={cx+1} cy={24} r="1.2" fill="#fff" opacity="0.5" />
        <circle cx={cx+16} cy={34} r="1" fill="#fff" opacity="0.5" />
        {/* Band gems */}
        {[-20,-10,0,10,20].map((dx,i) => <circle key={i} cx={cx+dx} cy={70} r="1.5" fill="#FFE060" opacity="0.5" />)}
      </> : c.id.includes("halo") ? <>
        {/* Outer glow */}
        <ellipse cx={cx} cy={45} rx="32" ry="12" fill={c.color} opacity="0.06" filter={`url(#blur-${c.id})`} />
        {/* Halo ring */}
        <ellipse cx={cx} cy={45} rx="28" ry="10" fill="none" stroke={c.color} strokeWidth="5" opacity="0.5" />
        <ellipse cx={cx} cy={45} rx="28" ry="10" fill="none" stroke="#FFFFFF" strokeWidth="2" opacity="0.2" />
        {/* Inner light */}
        <ellipse cx={cx} cy={43} rx="22" ry="6" fill={c.color} opacity="0.08" />
        {/* Sparkles */}
        {[[cx-20,38],[cx+22,40],[cx,35],[cx-28,45],[cx+28,45]].map(([x,y],i) => (
          <circle key={i} cx={x} cy={y} r="1" fill="#fff" opacity={0.3+i*0.1} />
        ))}
      </> : <>
        {/* Devil horns */}
        <path d={`M${cx-6} 70 Q${cx-8} 50 ${cx-20} 20 Q${cx-22} 14 ${cx-18} 12 Q${cx-14} 14 ${cx-10} 22 Q${cx-4} 40 ${cx-2} 60`}
          fill={`url(#fg-${c.id})`} stroke="rgba(255,255,255,0.1)" strokeWidth="0.5" />
        <path d={`M${cx+6} 70 Q${cx+8} 50 ${cx+20} 20 Q${cx+22} 14 ${cx+18} 12 Q${cx+14} 14 ${cx+10} 22 Q${cx+4} 40 ${cx+2} 60`}
          fill={`url(#fg-${c.id})`} stroke="rgba(255,255,255,0.1)" strokeWidth="0.5" />
        {/* Horn highlights */}
        <path d={`M${cx-8} 60 Q${cx-10} 45 ${cx-19} 18`} fill="none" stroke="rgba(255,255,255,0.15)" strokeWidth="1" />
        <path d={`M${cx+8} 60 Q${cx+10} 45 ${cx+19} 18`} fill="none" stroke="rgba(255,255,255,0.15)" strokeWidth="1" />
        {/* Tips */}
        <circle cx={cx-19} cy={14} r="2" fill="#FF6060" opacity="0.6" />
        <circle cx={cx+19} cy={14} r="2" fill="#FF6060" opacity="0.6" />
      </>}
    </svg>
  );

  // Aura
  return (
    <svg width={W} height={H} viewBox={`0 0 ${W} ${H}`} style={{ display: "block" }}>
      {defs}
      <rect width={W} height={H} fill={`url(#bg-${c.id})`} />
      {/* Player silhouette */}
      <circle cx={cx} cy={28} r="8" fill="rgba(255,255,255,0.06)" />
      <rect x={cx-8} y={36} width="16" height="28" rx="3" fill="rgba(255,255,255,0.05)" />
      <rect x={cx-7} y={64} width="6" height="20" rx="2" fill="rgba(255,255,255,0.04)" />
      <rect x={cx+1} y={64} width="6" height="20" rx="2" fill="rgba(255,255,255,0.04)" />
      <rect x={cx-14} y={38} width="6" height="3" rx="1" fill="rgba(255,255,255,0.03)" transform={`rotate(-30 ${cx-14} 38)`} />
      <rect x={cx+8} y={38} width="6" height="3" rx="1" fill="rgba(255,255,255,0.03)" transform={`rotate(30 ${cx+8} 38)`} />
      {/* Aura glow */}
      <ellipse cx={cx} cy={50} rx="35" ry="30" fill={c.color} opacity="0.04" filter={`url(#blur-${c.id})`} style={{animation:"pv-glow 3s ease-in-out infinite"}} />
      {/* Inner ring */}
      <ellipse cx={cx} cy={50} rx="25" ry="22" fill="none" stroke={c.color} strokeWidth="0.5" opacity="0.12" strokeDasharray="3 4"
        style={{animation:"pv-spin 12s linear infinite",transformOrigin:`${cx}px 50px`}} />
      {/* Outer ring */}
      <ellipse cx={cx} cy={50} rx="38" ry="32" fill="none" stroke={c.color} strokeWidth="0.3" opacity="0.08" strokeDasharray="2 5"
        style={{animation:"pv-spin 20s linear reverse infinite",transformOrigin:`${cx}px 50px`}} />
      {/* Particles */}
      {Array.from({length: 16}).map((_, i) => {
        const angle = (i / 16) * Math.PI * 2 + 0.3;
        const r = 22 + Math.sin(i * 2.3) * 10;
        const px = cx + Math.cos(angle) * r;
        const py = 50 + Math.sin(angle) * r * 0.75;
        const sz = 1.5 + (i % 4);
        return <circle key={i} cx={px} cy={py} r={sz} fill={c.color} opacity={0.2 + (i%5)*0.1}
          style={{animation:`pv-drift ${2+i*0.3}s ease-in-out ${i*0.15}s infinite, pv-twinkle ${3+i*0.4}s ease-in-out ${i*0.2}s infinite`}} />;
      })}
      {/* Bright accents */}
      {[[cx-25,35],[cx+28,42],[cx-20,65],[cx+22,70],[cx,22]].map(([x,y],i) => (
        <circle key={i} cx={x} cy={y} r={1.5+i*0.3} fill="#fff" opacity={0.15+i*0.03} />
      ))}
    </svg>
  );
}
