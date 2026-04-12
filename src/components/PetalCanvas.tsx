import { useRef, useEffect } from "react";

interface Star {
  x: number; y: number; size: number;
  speedX: number; speedY: number;
  wobbleSpeed: number; wobbleAmp: number; phase: number;
  alpha: number; color: string; rotation: number; rotSpeed: number;
}

const BLUES = ["#7AA2F7", "#89B4FA", "#B4BEFE", "#CDD6F4", "#5B6EAE", "#A6ADC8", "#ffffff", "#9AB8F7"];

function newStar(w: number, h: number, randomY = false): Star {
  const r = Math.random;
  return {
    x: r() * (w + 80) - 40,
    y: randomY ? r() * h : -10 - r() * 50,
    size: 1 + r() * 3,
    speedX: 0.05 + r() * 0.15,
    speedY: 0.1 + r() * 0.2,
    wobbleSpeed: 1 + r() * 2,
    wobbleAmp: 5 + r() * 10,
    phase: r() * Math.PI * 2,
    alpha: 0.2 + r() * 0.6,
    color: BLUES[Math.floor(r() * BLUES.length)],
    rotation: r() * Math.PI * 2,
    rotSpeed: (r() - 0.5) * 0.01,
  };
}

export function PetalCanvas() {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const starsRef = useRef<Star[]>([]);
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

    // Init stars
    starsRef.current = Array.from({ length: 50 }, () => newStar(w, h, true));

    function draw() {
      ctx.clearRect(0, 0, w, h);
      const stars = starsRef.current;

      for (let i = 0; i < stars.length; i++) {
        const p = stars[i];
        p.x += p.speedX;
        p.y += p.speedY;
        p.phase += p.wobbleSpeed * 0.01;
        p.rotation += p.rotSpeed;
        p.x += Math.sin(p.phase) * p.wobbleAmp * 0.005;

        // Twinkle effect
        const twinkle = 0.7 + Math.sin(p.phase * 3) * 0.3;

        if (p.y > h + 20 || p.x > w + 50) {
          stars[i] = newStar(w, h, false);
          continue;
        }

        ctx.save();
        ctx.translate(p.x, p.y);
        ctx.globalAlpha = p.alpha * twinkle;
        ctx.fillStyle = p.color;

        // Star shape - small glowing circle
        ctx.beginPath();
        ctx.arc(0, 0, p.size, 0, Math.PI * 2);
        ctx.fill();

        // Cross sparkle for larger stars
        if (p.size > 2) {
          ctx.globalAlpha = p.alpha * twinkle * 0.3;
          ctx.beginPath();
          ctx.moveTo(-p.size * 2, 0);
          ctx.lineTo(p.size * 2, 0);
          ctx.moveTo(0, -p.size * 2);
          ctx.lineTo(0, p.size * 2);
          ctx.strokeStyle = p.color;
          ctx.lineWidth = 0.5;
          ctx.stroke();
        }

        ctx.restore();
      }

      // Spawn occasionally
      if (Math.random() < 0.1) {
        stars.push(newStar(w, h, false));
      }
      // Cap stars
      if (stars.length > 80) stars.splice(0, stars.length - 80);

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
