import { useRef, useEffect } from "react";

// Animated sakura flower SVG
function Sakura({ size, style }: { size: number; style?: React.CSSProperties }) {
  return (
    <svg width={size} height={size} viewBox="0 0 100 100" style={style}>
      {[0, 72, 144, 216, 288].map((angle, i) => (
        <g key={i} transform={`rotate(${angle} 50 50)`}>
          <path d="M50 50 C45 35, 35 15, 50 5 C65 15, 55 35, 50 50" fill={i % 2 === 0 ? "#FFB7C9" : "#FFC8D6"} opacity={0.8} />
          <path d="M50 50 C47 38, 42 22, 50 12 C54 20, 51 36, 50 50" fill="#FFE0EA" opacity={0.35} />
        </g>
      ))}
      <circle cx="50" cy="50" r="5" fill="#FFD1DC" />
      <circle cx="50" cy="50" r="3" fill="#FFE8F0" />
      {[0, 72, 144, 216, 288].map((a, i) => (
        <g key={`s${i}`} transform={`rotate(${a + 36} 50 50)`}>
          <circle cx="50" cy="44" r="1" fill="#FFD1DC" opacity={0.7} />
        </g>
      ))}
    </svg>
  );
}

// Falling petals inside banner
function Petals({ width, height }: { width: number; height: number }) {
  const ref = useRef<HTMLCanvasElement>(null);
  const anim = useRef(0);

  useEffect(() => {
    const c = ref.current;
    if (!c || !width) return;
    const ctx = c.getContext("2d")!;
    c.width = width; c.height = height;
    const COLS = ["#FFB7C9", "#FFC0CB", "#FFD1DC", "#F8A4B8", "#FFE4E9"];

    type P = { x: number; y: number; s: number; vx: number; vy: number; w: number; ws: number; a: number; c: string; r: number; rs: number };
    const make = (top = false): P => ({
      x: Math.random() * width, y: top ? -10 - Math.random() * 30 : Math.random() * height,
      s: 2 + Math.random() * 3, vx: 0.15 + Math.random() * 0.3, vy: 0.2 + Math.random() * 0.3,
      w: Math.random() * 6.28, ws: 0.8 + Math.random() * 1.2, a: 0.15 + Math.random() * 0.35,
      c: COLS[Math.floor(Math.random() * COLS.length)], r: Math.random() * 6.28, rs: (Math.random() - 0.5) * 0.03,
    });
    const ps: P[] = Array.from({ length: 25 }, () => make());

    const draw = () => {
      ctx.clearRect(0, 0, width, height);
      for (let i = 0; i < ps.length; i++) {
        const p = ps[i];
        p.x += p.vx + Math.sin(p.w) * 0.2; p.y += p.vy; p.w += p.ws * 0.01; p.r += p.rs;
        if (p.y > height + 10 || p.x > width + 20) { ps[i] = make(true); continue; }
        ctx.save(); ctx.translate(p.x, p.y); ctx.rotate(p.r); ctx.globalAlpha = p.a;
        ctx.fillStyle = p.c; ctx.beginPath(); ctx.ellipse(0, 0, p.s, p.s * 0.5, 0, 0, Math.PI * 2); ctx.fill();
        ctx.restore();
      }
      if (Math.random() < 0.06 && ps.length < 35) ps.push(make(true));
      if (ps.length > 35) ps.splice(0, ps.length - 35);
      anim.current = requestAnimationFrame(draw);
    };
    anim.current = requestAnimationFrame(draw);
    return () => cancelAnimationFrame(anim.current);
  }, [width, height]);

  return <canvas ref={ref} style={{ position: "absolute", inset: 0, pointerEvents: "none", zIndex: 1 }} />;
}

interface Props {
  selectedVersion: string;
  versions?: string[];
  onVersionChange?: (v: string) => void;
  onOpenPicker?: () => void;
}

export function HeroBanner({ selectedVersion, onOpenPicker }: Props) {
  const containerRef = useRef<HTMLDivElement>(null);

  return (
    <div ref={containerRef} style={{
      height: "200px", borderRadius: "var(--radius-lg)", overflow: "hidden",
      background: "linear-gradient(140deg, #120a18 0%, #1e1028 25%, #361a38 55%, #2a1230 80%, #180e20 100%)",
      border: "1px solid rgba(255,176,192,0.06)",
      display: "flex", alignItems: "center", justifyContent: "center",
      position: "relative",
    }}>
      {/* Glow */}
      <div style={{ position: "absolute", width: "350px", height: "350px", background: "radial-gradient(circle, rgba(255,176,192,0.08) 0%, transparent 65%)", top: "-140px", left: "50%", transform: "translateX(-50%)", pointerEvents: "none" }} />

      {/* Petals */}
      <Petals width={containerRef.current?.clientWidth ?? 800} height={200} />

      {/* Left flower */}
      <div style={{ position: "absolute", left: "10%", top: "50%", transform: "translateY(-50%)", zIndex: 2, animation: "flower-float-left 7s ease-in-out infinite", opacity: 0.8 }}>
        <Sakura size={120} style={{ animation: "flower-spin 25s linear infinite", filter: "drop-shadow(0 2px 8px rgba(255,176,192,0.2))" }} />
      </div>

      {/* Right flower */}
      <div style={{ position: "absolute", right: "10%", top: "50%", transform: "translateY(-50%)", zIndex: 2, animation: "flower-float-right 8s ease-in-out infinite", opacity: 0.8 }}>
        <Sakura size={120} style={{ animation: "flower-spin 30s linear infinite reverse", filter: "drop-shadow(0 2px 8px rgba(255,176,192,0.2))" }} />
      </div>

      {/* Small accent flowers */}
      <div style={{ position: "absolute", left: "28%", top: "18%", zIndex: 2, animation: "flower-float-left 9s ease-in-out infinite", opacity: 0.3 }}>
        <Sakura size={32} style={{ animation: "flower-spin 18s linear infinite" }} />
      </div>
      <div style={{ position: "absolute", right: "28%", bottom: "15%", zIndex: 2, animation: "flower-float-right 10s ease-in-out infinite", opacity: 0.25 }}>
        <Sakura size={28} style={{ animation: "flower-spin 22s linear infinite reverse" }} />
      </div>

      {/* Center */}
      <div style={{ position: "relative", zIndex: 3, textAlign: "center" }}>
        <div style={{
          fontSize: "38px", fontWeight: "900", letterSpacing: "0.06em", lineHeight: 1,
          background: "linear-gradient(180deg, #FFE4EC, #FFB7C9, #F8A4B8)",
          WebkitBackgroundClip: "text", WebkitTextFillColor: "transparent",
          filter: "drop-shadow(0 1px 6px rgba(255,176,192,0.25))",
          marginBottom: "6px",
        }}>
          BLOOM
        </div>
        <div style={{ fontSize: "11px", fontWeight: "500", color: "var(--text-muted)", letterSpacing: "0.3em", textTransform: "uppercase", marginBottom: "14px" }}>
          Minecraft Client
        </div>
        <button
          onClick={onOpenPicker}
          style={{
            background: "rgba(0,0,0,0.4)", border: "1px solid rgba(255,176,192,0.15)",
            padding: "8px 24px", fontSize: "13px", fontWeight: "600", borderRadius: "8px",
            color: "#fff", cursor: "pointer", fontFamily: "inherit",
            display: "flex", alignItems: "center", gap: "8px",
            transition: "all 0.15s",
          }}
          onMouseEnter={e => { e.currentTarget.style.borderColor = "rgba(255,176,192,0.3)"; e.currentTarget.style.background = "rgba(0,0,0,0.5)"; }}
          onMouseLeave={e => { e.currentTarget.style.borderColor = "rgba(255,176,192,0.15)"; e.currentTarget.style.background = "rgba(0,0,0,0.4)"; }}
        >
          Minecraft {selectedVersion}
          <span style={{ fontSize: "10px", opacity: 0.5 }}>&#9660;</span>
        </button>
      </div>

      <style>{`
        @keyframes flower-float-left {
          0%, 100% { transform: translateY(-50%) translateX(0) rotate(0deg); }
          33% { transform: translateY(-54%) translateX(4px) rotate(2deg); }
          66% { transform: translateY(-47%) translateX(-3px) rotate(-1.5deg); }
        }
        @keyframes flower-float-right {
          0%, 100% { transform: translateY(-50%) translateX(0) rotate(0deg); }
          33% { transform: translateY(-46%) translateX(-4px) rotate(-2deg); }
          66% { transform: translateY(-53%) translateX(3px) rotate(1.5deg); }
        }
        @keyframes flower-spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
      `}</style>
    </div>
  );
}
