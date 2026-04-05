import { useRef, useEffect } from "react";

interface Petal {
  x: number; y: number; size: number;
  speedX: number; speedY: number;
  wobbleSpeed: number; wobbleAmp: number; phase: number;
  alpha: number; color: string; rotation: number; rotSpeed: number;
}

const PINKS = ["#FFB7C9", "#FFC0CB", "#FFD1DC", "#F8A4B8", "#F0C0D0", "#FFE4E9", "#E8899A", "#FFAEC0"];

function newPetal(w: number, h: number, randomY = false): Petal {
  const r = Math.random;
  return {
    x: r() * (w + 80) - 40,
    y: randomY ? r() * h : -10 - r() * 50,
    size: 3 + r() * 5,
    speedX: 0.2 + r() * 0.6,
    speedY: 0.3 + r() * 0.5,
    wobbleSpeed: 1 + r() * 2,
    wobbleAmp: 10 + r() * 20,
    phase: r() * Math.PI * 2,
    alpha: 0.3 + r() * 0.5,
    color: PINKS[Math.floor(r() * PINKS.length)],
    rotation: r() * Math.PI * 2,
    rotSpeed: (r() - 0.5) * 0.03,
  };
}

export function PetalCanvas() {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const petalsRef = useRef<Petal[]>([]);
  const animRef = useRef<number>(0);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext("2d")!;
    let w = 0, h = 0;

    function resize() {
      w = canvas!.parentElement!.clientWidth;
      h = canvas!.parentElement!.clientHeight;
      canvas!.width = w;
      canvas!.height = h;
    }
    resize();
    window.addEventListener("resize", resize);

    // Init petals
    petalsRef.current = Array.from({ length: 50 }, () => newPetal(w, h, true));

    function draw() {
      ctx.clearRect(0, 0, w, h);
      const petals = petalsRef.current;

      for (let i = 0; i < petals.length; i++) {
        const p = petals[i];
        p.x += p.speedX;
        p.y += p.speedY;
        p.phase += p.wobbleSpeed * 0.01;
        p.rotation += p.rotSpeed;
        p.x += Math.sin(p.phase) * p.wobbleAmp * 0.015;

        if (p.y > h + 20 || p.x > w + 50) {
          petals[i] = newPetal(w, h, false);
          continue;
        }

        ctx.save();
        ctx.translate(p.x, p.y);
        ctx.rotate(p.rotation);
        ctx.globalAlpha = p.alpha;
        ctx.fillStyle = p.color;

        // Petal shape
        ctx.beginPath();
        ctx.ellipse(0, 0, p.size, p.size * 0.6, 0, 0, Math.PI * 2);
        ctx.fill();

        // Smaller inner petal
        ctx.globalAlpha = p.alpha * 0.5;
        ctx.fillStyle = "#fff";
        ctx.beginPath();
        ctx.ellipse(0, -p.size * 0.15, p.size * 0.4, p.size * 0.25, 0, 0, Math.PI * 2);
        ctx.fill();

        ctx.restore();
      }

      // Spawn occasionally
      if (Math.random() < 0.15) {
        petals.push(newPetal(w, h, false));
      }
      // Cap petals
      if (petals.length > 80) petals.splice(0, petals.length - 80);

      animRef.current = requestAnimationFrame(draw);
    }

    animRef.current = requestAnimationFrame(draw);
    return () => {
      cancelAnimationFrame(animRef.current);
      window.removeEventListener("resize", resize);
    };
  }, []);

  return (
    <canvas
      ref={canvasRef}
      style={{ position: "absolute", inset: 0, pointerEvents: "none", zIndex: 0 }}
    />
  );
}
