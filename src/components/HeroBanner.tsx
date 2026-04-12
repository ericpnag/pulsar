import { useRef, useEffect } from "react";

// Animated black hole SVG with accretion disk
function BlackHole({ size, style }: { size: number; style?: React.CSSProperties }) {
  return (
    <svg width={size} height={size} viewBox="0 0 100 100" style={style}>
      {/* Outer accretion glow */}
      <ellipse cx="50" cy="50" rx="45" ry="15" fill="none" stroke="#E06C75" strokeWidth="1.5" opacity={0.3} />
      <ellipse cx="50" cy="50" rx="40" ry="12" fill="none" stroke="#D19A66" strokeWidth="1" opacity={0.25} />
      {/* Event horizon */}
      <circle cx="50" cy="50" r="20" fill="#0A0A0F" />
      {/* Inner glow ring */}
      <circle cx="50" cy="50" r="22" fill="none" stroke="#C678DD" strokeWidth="1.5" opacity={0.4} />
      <circle cx="50" cy="50" r="25" fill="none" stroke="#C678DD" strokeWidth="0.5" opacity={0.2} />
      {/* Gravitational lensing arcs */}
      <ellipse cx="50" cy="50" rx="35" ry="10" fill="none" stroke="#E06C75" strokeWidth="0.8" opacity={0.4} transform="rotate(-10 50 50)" />
      {/* Distant stars being pulled */}
      {[
        { cx: 15, cy: 20, r: 1.2 }, { cx: 82, cy: 25, r: 0.8 }, { cx: 88, cy: 70, r: 1 },
        { cx: 10, cy: 75, r: 0.7 }, { cx: 30, cy: 12, r: 0.9 }, { cx: 75, cy: 85, r: 0.6 },
      ].map((s, i) => (
        <circle key={i} cx={s.cx} cy={s.cy} r={s.r} fill="#C678DD" opacity={0.6} />
      ))}
      <defs>
        <radialGradient id="bhGrad" cx="40%" cy="40%">
          <stop offset="0%" stopColor="#C678DD" stopOpacity="0.1" />
          <stop offset="50%" stopColor="#E06C75" stopOpacity="0.05" />
          <stop offset="100%" stopColor="#0A0A0F" stopOpacity="0" />
        </radialGradient>
      </defs>
      <circle cx="50" cy="50" r="35" fill="url(#bhGrad)" />
    </svg>
  );
}

// Swirling particles being pulled toward center
function SwirlField({ width, height }: { width: number; height: number }) {
  const ref = useRef<HTMLCanvasElement>(null);
  const anim = useRef(0);

  useEffect(() => {
    const c = ref.current;
    if (!c || !width) return;
    const ctx = c.getContext("2d")!;
    c.width = width; c.height = height;
    const COLS = ["#C678DD", "#E06C75", "#D19A66", "#E0E0E8", "#ffffff"];
    const centerX = width / 2;
    const centerY = height / 2;

    type P = { x: number; y: number; s: number; angle: number; radius: number; speed: number; a: number; c: string; decay: number };
    const make = (): P => {
      const angle = Math.random() * Math.PI * 2;
      const radius = 40 + Math.random() * Math.max(width, height) * 0.5;
      return {
        x: centerX + Math.cos(angle) * radius,
        y: centerY + Math.sin(angle) * radius,
        s: 0.5 + Math.random() * 2,
        angle, radius,
        speed: 0.003 + Math.random() * 0.008,
        a: 0.2 + Math.random() * 0.5,
        c: COLS[Math.floor(Math.random() * COLS.length)],
        decay: 0.998 + Math.random() * 0.001,
      };
    };
    const ps: P[] = Array.from({ length: 30 }, () => make());

    const draw = () => {
      ctx.clearRect(0, 0, width, height);
      for (let i = 0; i < ps.length; i++) {
        const p = ps[i];
        p.angle += p.speed;
        p.radius *= p.decay;
        p.x = centerX + Math.cos(p.angle) * p.radius;
        p.y = centerY + Math.sin(p.angle) * p.radius;

        if (p.radius < 15 || p.x < -20 || p.x > width + 20 || p.y < -20 || p.y > height + 20) {
          ps[i] = make();
          continue;
        }

        const twinkle = 0.6 + Math.sin(p.angle * 5) * 0.4;
        ctx.save(); ctx.translate(p.x, p.y); ctx.globalAlpha = p.a * twinkle;
        ctx.fillStyle = p.c; ctx.beginPath(); ctx.arc(0, 0, p.s, 0, Math.PI * 2); ctx.fill();
        ctx.restore();
      }
      if (Math.random() < 0.04 && ps.length < 40) ps.push(make());
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
      background: "linear-gradient(140deg, #0A0A0F 0%, #12101A 25%, #1a1525 55%, #151020 80%, #0A0A0F 100%)",
      border: "1px solid rgba(198,120,221,0.06)",
      display: "flex", alignItems: "center", justifyContent: "center",
      position: "relative",
    }}>
      {/* Glow */}
      <div style={{ position: "absolute", width: "350px", height: "350px", background: "radial-gradient(circle, rgba(198,120,221,0.08) 0%, transparent 65%)", top: "-140px", left: "50%", transform: "translateX(-50%)", pointerEvents: "none" }} />

      {/* Swirling particles */}
      <SwirlField width={containerRef.current?.clientWidth ?? 800} height={200} />

      {/* Left black hole */}
      <div style={{ position: "absolute", left: "10%", top: "50%", transform: "translateY(-50%)", zIndex: 2, animation: "planet-float-left 7s ease-in-out infinite", opacity: 0.8 }}>
        <BlackHole size={120} style={{ animation: "planet-spin 40s linear infinite", filter: "drop-shadow(0 2px 8px rgba(198,120,221,0.2))" }} />
      </div>

      {/* Right black hole */}
      <div style={{ position: "absolute", right: "10%", top: "50%", transform: "translateY(-50%)", zIndex: 2, animation: "planet-float-right 8s ease-in-out infinite", opacity: 0.8 }}>
        <BlackHole size={120} style={{ animation: "planet-spin 50s linear infinite reverse", filter: "drop-shadow(0 2px 8px rgba(198,120,221,0.2))" }} />
      </div>

      {/* Small accent elements */}
      <div style={{ position: "absolute", left: "28%", top: "18%", zIndex: 2, animation: "planet-float-left 9s ease-in-out infinite", opacity: 0.3 }}>
        <BlackHole size={32} style={{ animation: "planet-spin 18s linear infinite" }} />
      </div>
      <div style={{ position: "absolute", right: "28%", bottom: "15%", zIndex: 2, animation: "planet-float-right 10s ease-in-out infinite", opacity: 0.25 }}>
        <BlackHole size={28} style={{ animation: "planet-spin 22s linear infinite reverse" }} />
      </div>

      {/* Center */}
      <div style={{ position: "relative", zIndex: 3, textAlign: "center" }}>
        <div style={{
          fontSize: "38px", fontWeight: "900", letterSpacing: "0.06em", lineHeight: 1,
          background: "linear-gradient(180deg, #C678DD, #E06C75, #D19A66)",
          WebkitBackgroundClip: "text", WebkitTextFillColor: "transparent",
          filter: "drop-shadow(0 1px 6px rgba(198,120,221,0.25))",
          marginBottom: "6px",
        }}>
          PULSAR
        </div>
        <div style={{ fontSize: "11px", fontWeight: "500", color: "var(--text-muted)", letterSpacing: "0.3em", textTransform: "uppercase", marginBottom: "14px" }}>
          Minecraft Client
        </div>
        <button
          onClick={onOpenPicker}
          style={{
            background: "rgba(0,0,0,0.4)", border: "1px solid rgba(198,120,221,0.15)",
            padding: "8px 24px", fontSize: "13px", fontWeight: "600", borderRadius: "8px",
            color: "#fff", cursor: "pointer", fontFamily: "inherit",
            display: "flex", alignItems: "center", gap: "8px",
            transition: "all 0.15s",
          }}
          onMouseEnter={e => { e.currentTarget.style.borderColor = "rgba(198,120,221,0.3)"; e.currentTarget.style.background = "rgba(0,0,0,0.5)"; }}
          onMouseLeave={e => { e.currentTarget.style.borderColor = "rgba(198,120,221,0.15)"; e.currentTarget.style.background = "rgba(0,0,0,0.4)"; }}
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
