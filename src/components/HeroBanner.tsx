import { useRef, useEffect } from "react";

// Animated nebula/planet SVG
function NebulaPlanet({ size, style }: { size: number; style?: React.CSSProperties }) {
  return (
    <svg width={size} height={size} viewBox="0 0 100 100" style={style}>
      {/* Outer glow */}
      <circle cx="50" cy="50" r="40" fill="none" stroke="#7AA2F7" strokeWidth="0.5" opacity={0.2} />
      <circle cx="50" cy="50" r="45" fill="none" stroke="#B4BEFE" strokeWidth="0.3" opacity={0.1} />
      {/* Planet body */}
      <circle cx="50" cy="50" r="28" fill="url(#planetGrad)" />
      {/* Atmosphere glow */}
      <circle cx="50" cy="50" r="30" fill="none" stroke="#89B4FA" strokeWidth="2" opacity={0.3} />
      {/* Ring */}
      <ellipse cx="50" cy="50" rx="42" ry="10" fill="none" stroke="#B4BEFE" strokeWidth="1.5" opacity={0.4} transform="rotate(-15 50 50)" />
      {/* Stars around */}
      {[
        { cx: 15, cy: 20, r: 1.2 }, { cx: 82, cy: 25, r: 0.8 }, { cx: 88, cy: 70, r: 1 },
        { cx: 10, cy: 75, r: 0.7 }, { cx: 30, cy: 12, r: 0.9 }, { cx: 75, cy: 85, r: 0.6 },
      ].map((s, i) => (
        <circle key={i} cx={s.cx} cy={s.cy} r={s.r} fill="#B4BEFE" opacity={0.6} />
      ))}
      <defs>
        <radialGradient id="planetGrad" cx="40%" cy="40%">
          <stop offset="0%" stopColor="#89B4FA" />
          <stop offset="50%" stopColor="#5B6EAE" />
          <stop offset="100%" stopColor="#2A3460" />
        </radialGradient>
      </defs>
    </svg>
  );
}

// Floating star particles inside banner
function StarField({ width, height }: { width: number; height: number }) {
  const ref = useRef<HTMLCanvasElement>(null);
  const anim = useRef(0);

  useEffect(() => {
    const c = ref.current;
    if (!c || !width) return;
    const ctx = c.getContext("2d")!;
    c.width = width; c.height = height;
    const COLS = ["#7AA2F7", "#89B4FA", "#B4BEFE", "#CDD6F4", "#ffffff"];

    type P = { x: number; y: number; s: number; vx: number; vy: number; w: number; ws: number; a: number; c: string; twinklePhase: number; twinkleSpeed: number };
    const make = (top = false): P => ({
      x: Math.random() * width, y: top ? -10 - Math.random() * 30 : Math.random() * height,
      s: 0.5 + Math.random() * 2, vx: 0.05 + Math.random() * 0.1, vy: 0.05 + Math.random() * 0.15,
      w: Math.random() * 6.28, ws: 0.8 + Math.random() * 1.2, a: 0.2 + Math.random() * 0.5,
      c: COLS[Math.floor(Math.random() * COLS.length)],
      twinklePhase: Math.random() * 6.28, twinkleSpeed: 1 + Math.random() * 3,
    });
    const ps: P[] = Array.from({ length: 30 }, () => make());

    const draw = () => {
      ctx.clearRect(0, 0, width, height);
      for (let i = 0; i < ps.length; i++) {
        const p = ps[i];
        p.x += p.vx; p.y += p.vy; p.w += p.ws * 0.01;
        p.twinklePhase += p.twinkleSpeed * 0.02;
        if (p.y > height + 10 || p.x > width + 20) { ps[i] = make(true); continue; }
        const twinkle = 0.6 + Math.sin(p.twinklePhase) * 0.4;
        ctx.save(); ctx.translate(p.x, p.y); ctx.globalAlpha = p.a * twinkle;
        ctx.fillStyle = p.c; ctx.beginPath(); ctx.arc(0, 0, p.s, 0, Math.PI * 2); ctx.fill();
        ctx.restore();
      }
      if (Math.random() < 0.04 && ps.length < 40) ps.push(make(true));
      if (ps.length > 40) ps.splice(0, ps.length - 40);
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
      background: "linear-gradient(140deg, #0B0E1A 0%, #131729 25%, #1a2040 55%, #151b35 80%, #0d1020 100%)",
      border: "1px solid rgba(122,162,247,0.06)",
      display: "flex", alignItems: "center", justifyContent: "center",
      position: "relative",
    }}>
      {/* Glow */}
      <div style={{ position: "absolute", width: "350px", height: "350px", background: "radial-gradient(circle, rgba(122,162,247,0.08) 0%, transparent 65%)", top: "-140px", left: "50%", transform: "translateX(-50%)", pointerEvents: "none" }} />

      {/* Stars */}
      <StarField width={containerRef.current?.clientWidth ?? 800} height={200} />

      {/* Left planet */}
      <div style={{ position: "absolute", left: "10%", top: "50%", transform: "translateY(-50%)", zIndex: 2, animation: "planet-float-left 7s ease-in-out infinite", opacity: 0.8 }}>
        <NebulaPlanet size={120} style={{ animation: "planet-spin 40s linear infinite", filter: "drop-shadow(0 2px 8px rgba(122,162,247,0.2))" }} />
      </div>

      {/* Right planet */}
      <div style={{ position: "absolute", right: "10%", top: "50%", transform: "translateY(-50%)", zIndex: 2, animation: "planet-float-right 8s ease-in-out infinite", opacity: 0.8 }}>
        <NebulaPlanet size={120} style={{ animation: "planet-spin 50s linear infinite reverse", filter: "drop-shadow(0 2px 8px rgba(122,162,247,0.2))" }} />
      </div>

      {/* Small accent stars */}
      <div style={{ position: "absolute", left: "28%", top: "18%", zIndex: 2, animation: "planet-float-left 9s ease-in-out infinite", opacity: 0.3 }}>
        <NebulaPlanet size={32} style={{ animation: "planet-spin 18s linear infinite" }} />
      </div>
      <div style={{ position: "absolute", right: "28%", bottom: "15%", zIndex: 2, animation: "planet-float-right 10s ease-in-out infinite", opacity: 0.25 }}>
        <NebulaPlanet size={28} style={{ animation: "planet-spin 22s linear infinite reverse" }} />
      </div>

      {/* Center */}
      <div style={{ position: "relative", zIndex: 3, textAlign: "center" }}>
        <div style={{
          fontSize: "38px", fontWeight: "900", letterSpacing: "0.06em", lineHeight: 1,
          background: "linear-gradient(180deg, #B4BEFE, #7AA2F7, #5B6EAE)",
          WebkitBackgroundClip: "text", WebkitTextFillColor: "transparent",
          filter: "drop-shadow(0 1px 6px rgba(122,162,247,0.25))",
          marginBottom: "6px",
        }}>
          NEBULA
        </div>
        <div style={{ fontSize: "11px", fontWeight: "500", color: "var(--text-muted)", letterSpacing: "0.3em", textTransform: "uppercase", marginBottom: "14px" }}>
          Minecraft Client
        </div>
        <button
          onClick={onOpenPicker}
          style={{
            background: "rgba(0,0,0,0.4)", border: "1px solid rgba(122,162,247,0.15)",
            padding: "8px 24px", fontSize: "13px", fontWeight: "600", borderRadius: "8px",
            color: "#fff", cursor: "pointer", fontFamily: "inherit",
            display: "flex", alignItems: "center", gap: "8px",
            transition: "all 0.15s",
          }}
          onMouseEnter={e => { e.currentTarget.style.borderColor = "rgba(122,162,247,0.3)"; e.currentTarget.style.background = "rgba(0,0,0,0.5)"; }}
          onMouseLeave={e => { e.currentTarget.style.borderColor = "rgba(122,162,247,0.15)"; e.currentTarget.style.background = "rgba(0,0,0,0.4)"; }}
        >
          Minecraft {selectedVersion}
          <span style={{ fontSize: "10px", opacity: 0.5 }}>&#9660;</span>
        </button>
      </div>

      <style>{`
        @keyframes planet-float-left {
          0%, 100% { transform: translateY(-50%) translateX(0) rotate(0deg); }
          33% { transform: translateY(-54%) translateX(4px) rotate(2deg); }
          66% { transform: translateY(-47%) translateX(-3px) rotate(-1.5deg); }
        }
        @keyframes planet-float-right {
          0%, 100% { transform: translateY(-50%) translateX(0) rotate(0deg); }
          33% { transform: translateY(-46%) translateX(-4px) rotate(-2deg); }
          66% { transform: translateY(-53%) translateX(3px) rotate(1.5deg); }
        }
        @keyframes planet-spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
      `}</style>
    </div>
  );
}
