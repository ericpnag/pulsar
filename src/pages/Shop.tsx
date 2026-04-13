import { useState, useEffect } from "react";
import { invoke } from "@tauri-apps/api/core";

function openBrowser(url: string) {
  invoke("open_browser", { url }).catch(() => {});
}

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
  { id: "cape_blossom", name: "Bloom", type: "cape", price: 0, color: "#FFBED0", color2: "#D77890", description: "Pink cape with cherry blossom petals" },
  { id: "cape_midnight", name: "Midnight", type: "cape", price: 500, color: "#1E0F32", color2: "#0F0820", description: "Dark purple with starlight" },
  { id: "cape_frost", name: "Frost", type: "cape", price: 750, color: "#8CC8FF", color2: "#508CDC", description: "Icy blue crystal cape" },
  { id: "cape_flame", name: "Flame", type: "cape", price: 750, color: "#FF7828", color2: "#C83C14", description: "Fiery orange-red cape" },
  { id: "cape_ocean", name: "Ocean", type: "cape", price: 1000, color: "#1450A0", color2: "#0A2864", description: "Deep blue ocean waves" },
  { id: "cape_emerald", name: "Emerald", type: "cape", price: 1250, color: "#1E9646", color2: "#0F6428", description: "Green with gem sparkles" },
  { id: "cape_sunset", name: "Sunset", type: "cape", price: 1500, color: "#FF9650", color2: "#783296", description: "Orange to purple gradient" },
  { id: "cape_galaxy", name: "Galaxy", type: "cape", price: 2500, color: "#0F081E", color2: "#05020F", description: "Dark with colorful stars" },
  { id: "cape_void", name: "Void", type: "cape", price: 3000, color: "#1A0A2E", color2: "#0D0518", description: "Deep void with dark energy" },
  { id: "cape_lightning", name: "Lightning", type: "cape", price: 1500, color: "#E8D44D", color2: "#B8960F", description: "Electric yellow lightning bolts" },
  { id: "cape_blood", name: "Bloodmoon", type: "cape", price: 2000, color: "#8B0000", color2: "#4A0000", description: "Dark crimson moonlit cape" },
  { id: "cape_arctic", name: "Arctic", type: "cape", price: 1750, color: "#E0F0FF", color2: "#A0C4E8", description: "Frozen white aurora cape" },
  { id: "cape_phantom", name: "Phantom", type: "cape", price: 1500, color: "#C8C8D2", color2: "#646478", description: "Ghostly spectral wisps" },
  { id: "cape_neon", name: "Neon", type: "cape", price: 2000, color: "#FF32C8", color2: "#00FFFF", description: "Cyberpunk neon glitch" },
  { id: "cape_lava", name: "Lava", type: "cape", price: 1750, color: "#FFA014", color2: "#321905", description: "Molten lava with black crust" },
  { id: "cape_storm", name: "Storm", type: "cape", price: 1500, color: "#3C414B", color2: "#191C23", description: "Thunderstorm with lightning" },
  { id: "cape_solar", name: "Solar", type: "cape", price: 2500, color: "#FFC83C", color2: "#C8500A", description: "Solar flare corona rays" },
  { id: "cape_amethyst", name: "Amethyst", type: "cape", price: 2000, color: "#5A1478", color2: "#28083C", description: "Deep purple crystal clusters" },
  { id: "cape_inferno", name: "Inferno", type: "cape", price: 3000, color: "#C81400", color2: "#3C0505", description: "Hellfire with skull motifs" },
  { id: "cape_drift", name: "Drift", type: "cape", price: 1250, color: "#C8B4DC", color2: "#B4D2DC", description: "Shifting pastel vaporwave" },
  { id: "cape_obsidian", name: "Obsidian", type: "cape", price: 3500, color: "#1E0F1E", color2: "#0A050F", description: "Dark obsidian purple cracks" },
  { id: "cape_blackhole", name: "Black Hole", type: "cape", price: 2500, color: "#FFA030", color2: "#080010", description: "Swirling accretion disk with event horizon" },
  // Creator cosmetics — buyable with points
  { id: "cape_creator", name: "Creator", type: "cape", price: 3000, color: "#FFD700", color2: "#FF4500", description: "Gold & fire cape — support a creator" },
  { id: "cape_youtube", name: "YouTube", type: "cape", price: 2500, color: "#FF0000", color2: "#CC0000", description: "Red play button themed cape" },
  { id: "cape_twitch", name: "Twitch", type: "cape", price: 2500, color: "#9146FF", color2: "#6441A5", description: "Purple streaming cape with chat particles" },
  { id: "cape_tiktok", name: "TikTok", type: "cape", price: 2500, color: "#00F2EA", color2: "#FF0050", description: "Cyan and pink viral cape" },
  { id: "cape_og", name: "OG Pulsar", type: "cape", price: 2000, color: "#FFFFFF", color2: "#808080", description: "Original white cape for early supporters" },
];

// Creator codes that unlock exclusive cosmetics
// Creator codes — support a creator and get bonus points
const CREATOR_CODES: Record<string, number> = {
  "CRAZYFISH564": 5000,
  "BIGBOBBY68": 5000,
  "OLIVERTREEEE": 5000,
  "ERICPNAG": 50000,
};

const TYPE_LABELS: Record<string, string> = { cape: "Capes", wings: "Wings", hat: "Hats", aura: "Auras" };

// Payment — hosted on Vercel
const STORE_URL = "https://bloom-launcher.vercel.app";
const POINT_TIERS = [
  { amount: 500, price: "$0.50", priceNum: 0.35, color: "#FFFFFF", popular: false, bonus: "",
    payUrl: `${STORE_URL}/store.html?tier=500` },
  { amount: 1500, price: "$0.50", priceNum: 0.35, color: "#FFFFFF", popular: true, bonus: "+200 bonus",
    payUrl: `${STORE_URL}/store.html?tier=1500` },
  { amount: 3500, price: "$0.50", priceNum: 0.35, color: "#E0E0E0", popular: false, bonus: "+500 bonus",
    payUrl: `${STORE_URL}/store.html?tier=3500` },
  { amount: 8000, price: "$0.50", priceNum: 0.35, color: "#A0A0A0", popular: false, bonus: "+1500 bonus",
    payUrl: `${STORE_URL}/store.html?tier=8000` },
];

// Bump this when the data format changes to force a clean slate
const DATA_VERSION = 2;

export function ShopPage() {
  const [points, setPoints] = useState(500);
  const [purchased, setPurchased] = useState<string[]>([]);
  const [equipped, setEquipped] = useState<Record<string, string>>({});
  const [filter, setFilter] = useState<string>("all");
  const [purchaseModal, setPurchaseModal] = useState<typeof POINT_TIERS[0] | null>(null);
  const [purchasePhase, setPurchasePhase] = useState<"confirm" | "paying" | "processing" | "success">("confirm");

  useEffect(() => {
    invoke<string>("get_cosmetics").then(raw => {
      const data = JSON.parse(raw);
      // If data version doesn't match, reset owned items (keep points)
      if ((data.v ?? 1) < DATA_VERSION) {
        const reset = { v: DATA_VERSION, points: data.points ?? 500, purchased: [], equipped: {} };
        invoke("save_cosmetics", { data: JSON.stringify(reset) }).catch(() => {});
        localStorage.setItem("pulsar-cosmetics", JSON.stringify(reset));
        setPoints(reset.points);
        return;
      }
      setPoints(data.points ?? 500);
      setPurchased(data.purchased ?? []);
      setEquipped(data.equipped ?? {});
    }).catch(() => {
      const saved = localStorage.getItem("pulsar-cosmetics");
      if (saved) {
        const data = JSON.parse(saved);
        if ((data.v ?? 1) < DATA_VERSION) { localStorage.removeItem("pulsar-cosmetics"); return; }
        setPoints(data.points ?? 500);
        setPurchased(data.purchased ?? []);
        setEquipped(data.equipped ?? {});
      }
    });
  }, []);

  function save(p: number, pur: string[], e: Record<string, string>) {
    setPoints(p); setPurchased(pur); setEquipped(e);
    const json = JSON.stringify({ v: DATA_VERSION, points: p, purchased: pur, equipped: e });
    invoke("save_cosmetics", { data: json }).catch(() => {});
    localStorage.setItem("pulsar-cosmetics", json);
  }

  function buy(c: Cosmetic) {
    if (c.price > points || isOwned(c.id)) return;
    save(points - c.price, [...purchased, c.id], equipped);
  }

  function equip(c: Cosmetic) {
    const newEquipped = { ...equipped };
    if (newEquipped[c.type] === c.id) delete newEquipped[c.type];
    else newEquipped[c.type] = c.id;
    save(points, purchased, newEquipped);
  }

  // A cosmetic is owned if it's free (price 0) or was explicitly purchased
  const isOwned = (id: string) => {
    const cosmetic = COSMETICS.find(c => c.id === id);
    return cosmetic?.price === 0 || purchased.includes(id);
  };
  const isEquipped = (id: string) => Object.values(equipped).includes(id);

  const filtered = filter === "all" ? COSMETICS : COSMETICS.filter(c => c.type === filter);

  // Redeem code system
  const [redeemCode, setRedeemCode] = useState("");
  const [redeemMsg, setRedeemMsg] = useState<{ text: string; ok: boolean } | null>(null);
  const [showRedeem, setShowRedeem] = useState(false);
  const [redeemLoading, setRedeemLoading] = useState(false);

  async function redeemPoints() {
    const code = redeemCode.trim().toUpperCase();
    if (!code || redeemLoading) return;

    // Hardcoded gift codes
    const GIFT_CODES: Record<string, number> = {
      "ANGRYBANGRY500": 2500,
      "WELCOME100": 2500,
      "PULSAR": 2500,
      "BLACKHOLE": 2500,
      "FREEPOINTS": 2500,
      "MINECRAFT": 2500,
      "BEDWARS": 2500,
      "SPEEDRUN": 2500,
      "SPACE": 2500,
      "LAUNCH": 2500,
    };
    const localUsed: string[] = JSON.parse(localStorage.getItem("pulsar-used-codes") || "[]");

    // Creator codes — bonus points for supporting a creator
    if (CREATOR_CODES[code]) {
      if (localUsed.includes(code)) { setRedeemMsg({ text: "Code already used", ok: false }); return; }
      localStorage.setItem("pulsar-used-codes", JSON.stringify([...localUsed, code]));
      save(points + CREATOR_CODES[code], purchased, equipped);
      setRedeemMsg({ text: `+${CREATOR_CODES[code]} points — thanks for supporting ${code}!`, ok: true });
      setRedeemCode("");
      return;
    }

    if (GIFT_CODES[code]) {
      if (localUsed.includes(code)) { setRedeemMsg({ text: "Code already used", ok: false }); return; }
      localStorage.setItem("pulsar-used-codes", JSON.stringify([...localUsed, code]));
      save(points + GIFT_CODES[code], purchased, equipped);
      setRedeemMsg({ text: `+${GIFT_CODES[code]} points added!`, ok: true });
      setRedeemCode("");
      return;
    }

    // Server-validated codes (from Stripe payments)
    setRedeemLoading(true);
    try {
      const res = await fetch("https://bloom-launcher.vercel.app/api/validate-code", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ code }),
      });
      const data = await res.json();
      if (data.valid) {
        save(points + data.points, purchased, equipped);
        setRedeemMsg({ text: `+${data.points} points added!`, ok: true });
        setRedeemCode("");
      } else {
        setRedeemMsg({ text: data.error || "Invalid code", ok: false });
      }
    } catch {
      setRedeemMsg({ text: "Could not verify code. Check your connection.", ok: false });
    }
    setRedeemLoading(false);
  }

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
            <circle cx="12" cy="12" r="10" fill="#000000"/>
            <circle cx="12" cy="12" r="8" fill="none" stroke="#ffffff" strokeWidth="0.5" opacity="0.3"/>
            <ellipse cx="12" cy="12" rx="11" ry="4" fill="none" stroke="#ffffff" strokeWidth="1.5" opacity="0.6"/>
            <circle cx="12" cy="12" r="4" fill="#000000"/>
            <circle cx="12" cy="12" r="3" fill="none" stroke="#ffffff" strokeWidth="0.3" opacity="0.5"/>
          </svg>
          <span style={{ fontSize: "18px", fontWeight: "800", color: "#FFFFFF" }}>{points}</span>
          <span style={{ fontSize: "11px", color: "var(--text-muted)" }}>points</span>
        </div>
      </div>

      {/* Points Store */}
      <div style={{ display: "grid", gridTemplateColumns: "repeat(4, 1fr)", gap: "8px" }}>
        {POINT_TIERS.map((tier, i) => (
          <div key={i} className="bloom-card" onClick={() => {
            setPurchaseModal(tier);
            setPurchasePhase("confirm");
          }} style={{
            padding: "14px", textAlign: "center", cursor: "pointer",
            position: "relative", transition: "all 0.15s",
            border: tier.popular ? "1px solid rgba(255,255,255,0.2)" : undefined,
            background: tier.popular ? "rgba(255,255,255,0.05)" : undefined,
          }}
          onMouseEnter={e => { e.currentTarget.style.transform = "translateY(-2px)"; e.currentTarget.style.boxShadow = "0 4px 16px rgba(255,255,255,0.08)"; }}
          onMouseLeave={e => { e.currentTarget.style.transform = "translateY(0)"; e.currentTarget.style.boxShadow = "none"; }}
          >
            {tier.popular && (
              <div style={{
                position: "absolute", top: "-8px", left: "50%", transform: "translateX(-50%)",
                background: "linear-gradient(135deg, #FFFFFF, #C0C0C0)", color: "#000000",
                fontSize: "9px", fontWeight: "800", padding: "2px 10px", borderRadius: "8px",
                letterSpacing: "0.05em", textTransform: "uppercase",
              }}>Popular</div>
            )}
            <div style={{ fontSize: "10px", fontWeight: "700", color: "var(--text-muted)", letterSpacing: "0.08em", textTransform: "uppercase", marginBottom: "6px" }}>
              Pulsar Points
            </div>
            <div style={{ fontSize: "22px", fontWeight: "800", color: "#FFFFFF", marginBottom: "2px" }}>
              {tier.amount.toLocaleString()}
            </div>
            <div style={{
              fontSize: "13px", fontWeight: "700", color: "var(--text-primary)",
              background: "rgba(255,255,255,0.06)", borderRadius: "6px", padding: "4px 12px",
              display: "inline-block", marginTop: "6px",
            }}>{tier.price}</div>
          </div>
        ))}
      </div>

      {/* Redeem Code */}
      <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
        <button onClick={() => setShowRedeem(!showRedeem)} style={{
          background: "rgba(255,255,255,0.04)", border: "1px solid rgba(255,255,255,0.08)",
          borderRadius: "8px", padding: "7px 14px", fontSize: "12px", fontWeight: "600",
          color: "var(--text-secondary)", cursor: "pointer", fontFamily: "inherit",
          transition: "all 0.15s",
        }}>
          Redeem Code
        </button>
        {showRedeem && (
          <>
            <input
              className="bloom-input"
              value={redeemCode}
              onChange={e => { setRedeemCode(e.target.value); setRedeemMsg(null); }}
              onKeyDown={e => e.key === "Enter" && redeemPoints()}
              placeholder="Enter code..."
              style={{ flex: 1, padding: "7px 12px", fontSize: "12px" }}
            />
            <button onClick={redeemPoints} className="bloom-btn" style={{ padding: "7px 16px", fontSize: "12px" }}>
              Redeem
            </button>
          </>
        )}
        {redeemMsg && (
          <span style={{ fontSize: "12px", fontWeight: "600", color: redeemMsg.ok ? "var(--accent-green)" : "var(--accent-red)" }}>
            {redeemMsg.text}
          </span>
        )}
      </div>

      <div style={{ height: "1px", background: "rgba(255,255,255,0.06)" }} />

      {/* Creators Section */}
      <div>
        <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: "14px" }}>
          <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
            <span style={{ fontSize: "16px" }}>⭐</span>
            <span style={{ fontSize: "14px", fontWeight: "800", color: "#FFD700", letterSpacing: "0.04em" }}>CREATORS</span>
            <span style={{ fontSize: "11px", color: "var(--text-faint)" }}>Use their code to support them</span>
          </div>
        </div>
        <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(200px, 1fr))", gap: "10px" }}>
          {[
            { name: "CrazyFish564", code: "CRAZYFISH564", yt: "https://www.youtube.com/@crazyfish564", role: "YouTube Creator", color: "#FF0000" },
            { name: "BigBobby68", code: "BIGBOBBY68", yt: "https://www.youtube.com/@BigBobby68", role: "YouTube Creator", color: "#FF0000" },
            { name: "OliverTreeee", code: "OLIVERTREEEE", role: "Pulsar Creator", color: "#9146FF" },
          ].map(creator => (
            <div key={creator.name} className="bloom-card" style={{
              padding: "0", overflow: "hidden", transition: "all 0.2s",
            }}>
              {/* Banner */}
              <div style={{
                height: "48px",
                background: `linear-gradient(135deg, ${creator.color}40, ${creator.color}15)`,
                position: "relative",
              }}>
                <div style={{
                  position: "absolute", bottom: "-16px", left: "14px",
                  width: "36px", height: "36px", borderRadius: "10px",
                  border: "2px solid #111", overflow: "hidden",
                  background: "#1a1a1a",
                }}>
                  <img
                    src={`https://mc-heads.net/avatar/${creator.name}/36`}
                    alt=""
                    width={36} height={36}
                    style={{ display: "block" }}
                    onError={(e) => { (e.target as HTMLImageElement).style.display = "none"; }}
                  />
                </div>
              </div>
              {/* Info */}
              <div style={{ padding: "22px 14px 14px" }}>
                <div style={{ fontSize: "13px", fontWeight: "700", color: "#fff", marginBottom: "2px" }}>
                  {creator.name}
                </div>
                <div style={{ fontSize: "10px", color: "var(--text-faint)", marginBottom: "10px" }}>
                  {creator.role}
                </div>
                {/* Social links */}
                <div style={{ display: "flex", gap: "6px", marginBottom: "10px" }}>
                  {creator.yt && (
                    <div onClick={() => invoke("open_browser", { url: creator.yt })} style={{
                      padding: "4px 10px", borderRadius: "6px", fontSize: "10px", fontWeight: "600",
                      background: "rgba(255,0,0,0.1)", color: "#FF4444", cursor: "pointer",
                      display: "flex", alignItems: "center", gap: "4px", transition: "all 0.15s",
                    }}>
                      <svg width="10" height="10" viewBox="0 0 24 24" fill="currentColor"><path d="M23.5 6.2c-.3-1-1-1.8-2-2.1C19.6 3.5 12 3.5 12 3.5s-7.6 0-9.5.6c-1 .3-1.8 1-2 2.1C0 8.1 0 12 0 12s0 3.9.5 5.8c.3 1 1 1.8 2 2.1 1.9.6 9.5.6 9.5.6s7.6 0 9.5-.6c1-.3 1.8-1 2-2.1.5-1.9.5-5.8.5-5.8s0-3.9-.5-5.8zM9.5 15.5V8.5l6.5 3.5-6.5 3.5z"/></svg>
                      YouTube
                    </div>
                  )}
                </div>
                {/* Use Code button */}
                <button onClick={() => { setRedeemCode(creator.code); setShowRedeem(true); }} style={{
                  width: "100%", padding: "8px", borderRadius: "8px", border: "none",
                  background: "linear-gradient(135deg, rgba(255,215,0,0.15), rgba(255,215,0,0.05))",
                  border: "1px solid rgba(255,215,0,0.15)",
                  color: "#FFD700", fontSize: "11px", fontWeight: "700", cursor: "pointer",
                  fontFamily: "inherit", transition: "all 0.15s", letterSpacing: "0.04em",
                }}
                onMouseEnter={e => { e.currentTarget.style.background = "rgba(255,215,0,0.2)"; }}
                onMouseLeave={e => { e.currentTarget.style.background = "linear-gradient(135deg, rgba(255,215,0,0.15), rgba(255,215,0,0.05))"; }}
                >
                  USE CODE: {creator.code}
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>

      <div style={{ height: "1px", background: "rgba(255,255,255,0.06)" }} />

      {/* Filter tabs */}
      <div style={{ display: "flex", gap: "4px" }}>
        {["all", "cape"].map(t => (
          <button key={t} onClick={() => setFilter(t)} style={{
            padding: "7px 16px", borderRadius: "8px",
            background: filter === t ? "rgba(255,255,255,0.08)" : "transparent",
            border: filter === t ? "1px solid rgba(255,255,255,0.15)" : "1px solid transparent",
            color: filter === t ? "#FFFFFF" : "var(--text-muted)",
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
              borderColor: eq ? "rgba(255,255,255,0.22)" : undefined,
              background: eq ? "rgba(255,255,255,0.05)" : undefined,
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
                  background: eq ? "linear-gradient(135deg, #FFFFFF, #C0C0C0)" : "rgba(255,255,255,0.04)",
                  color: eq ? "#000000" : "var(--text-muted)",
                  fontSize: "12px",
                }}>
                  {eq ? "Equipped" : "Equip"}
                </button>
              ) : (
                <button onClick={() => buy(c)} disabled={c.price > points}
                  className="bloom-btn-ghost"
                  style={{
                    width: "100%", padding: "8px",
                    borderColor: c.price > points ? "rgba(255,255,255,0.04)" : "rgba(255,255,255,0.15)",
                    color: c.price > points ? "var(--text-faint)" : "#FFFFFF",
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

      {/* Purchase Modal */}
      {purchaseModal && (
        <div style={{
          position: "fixed", inset: 0, background: "rgba(0,0,0,0.75)", backdropFilter: "blur(8px)",
          display: "flex", alignItems: "center", justifyContent: "center", zIndex: 999,
        }} onClick={() => { if (purchasePhase !== "processing") setPurchaseModal(null); }}>
          <div onClick={e => e.stopPropagation()} className="fade-in" style={{
            background: "rgba(0,0,0,0.97)", border: "1px solid rgba(255,255,255,0.1)",
            borderRadius: "16px", padding: "32px 36px", width: "min(400px, 90vw)",
            boxShadow: "0 24px 64px rgba(0,0,0,0.6)",
          }}>
            {purchasePhase === "confirm" && (<>
              <div style={{ textAlign: "center", marginBottom: "24px" }}>
                <div style={{ fontSize: "10px", fontWeight: "700", letterSpacing: "0.12em", color: "var(--text-muted)", textTransform: "uppercase", marginBottom: "8px" }}>
                  Purchase Pulsar Points
                </div>
                <div style={{ fontSize: "42px", fontWeight: "800", color: "#FFFFFF", lineHeight: 1 }}>
                  {purchaseModal.amount.toLocaleString()}
                </div>
                {purchaseModal.bonus && (
                  <div style={{ fontSize: "12px", color: "var(--accent-green)", fontWeight: "600", marginTop: "4px" }}>
                    {purchaseModal.bonus}
                  </div>
                )}
                <div style={{ fontSize: "11px", color: "var(--text-muted)", marginTop: "12px" }}>Pulsar Points</div>
              </div>

              <div style={{
                background: "rgba(255,255,255,0.02)", border: "1px solid rgba(255,255,255,0.04)",
                borderRadius: "10px", padding: "16px", marginBottom: "20px",
              }}>
                <div style={{ display: "flex", justifyContent: "space-between", marginBottom: "8px" }}>
                  <span style={{ fontSize: "12px", color: "var(--text-muted)" }}>{purchaseModal.amount.toLocaleString()} Pulsar Points</span>
                  <span style={{ fontSize: "12px", color: "var(--text-primary)", fontWeight: "600" }}>{purchaseModal.price}</span>
                </div>
                {purchaseModal.bonus && (
                  <div style={{ display: "flex", justifyContent: "space-between", marginBottom: "8px" }}>
                    <span style={{ fontSize: "12px", color: "var(--accent-green)" }}>{purchaseModal.bonus}</span>
                    <span style={{ fontSize: "12px", color: "var(--accent-green)", fontWeight: "600" }}>FREE</span>
                  </div>
                )}
                <div style={{ height: "1px", background: "rgba(255,255,255,0.06)", margin: "8px 0" }} />
                <div style={{ display: "flex", justifyContent: "space-between" }}>
                  <span style={{ fontSize: "13px", color: "var(--text-primary)", fontWeight: "700" }}>Total</span>
                  <span style={{ fontSize: "13px", color: "#FFFFFF", fontWeight: "800" }}>{purchaseModal.price}</span>
                </div>
              </div>

              <button onClick={() => {
                // Open Stripe payment link in the browser
                openBrowser(purchaseModal.payUrl);
                setPurchasePhase("paying");
              }} style={{
                width: "100%", padding: "14px", border: "none", borderRadius: "10px",
                background: "linear-gradient(135deg, #FFFFFF, #C0C0C0)",
                color: "#000000", fontSize: "13px", fontWeight: "800", cursor: "pointer",
                fontFamily: "inherit", letterSpacing: "0.06em",
                boxShadow: "0 4px 20px rgba(255,255,255,0.12)",
                transition: "all 0.2s",
              }}
              onMouseEnter={e => { e.currentTarget.style.transform = "translateY(-1px)"; e.currentTarget.style.boxShadow = "0 8px 32px rgba(255,255,255,0.18)"; }}
              onMouseLeave={e => { e.currentTarget.style.transform = "translateY(0)"; e.currentTarget.style.boxShadow = "0 4px 20px rgba(255,255,255,0.12)"; }}
              >
                PAY {purchaseModal.price}
              </button>

              <button onClick={() => setPurchaseModal(null)} style={{
                width: "100%", padding: "10px", border: "none", borderRadius: "8px",
                background: "transparent", color: "var(--text-muted)", fontSize: "12px",
                cursor: "pointer", fontFamily: "inherit", marginTop: "8px",
              }}>Cancel</button>
            </>)}

            {purchasePhase === "paying" && (
              <div style={{ textAlign: "center", padding: "10px 0" }}>
                <div style={{ fontSize: "14px", color: "#FFFFFF", fontWeight: "600", marginBottom: "8px" }}>
                  Complete payment in your browser
                </div>
                <div style={{ fontSize: "12px", color: "var(--text-muted)", marginBottom: "20px", lineHeight: 1.6 }}>
                  A payment page has opened in your browser.<br/>
                  After completing your purchase, click the button below.
                </div>

                <button onClick={() => {
                  setPurchasePhase("processing");
                  setTimeout(() => {
                    save(points + purchaseModal!.amount, purchased, equipped);
                    setPurchasePhase("success");
                    setTimeout(() => setPurchaseModal(null), 1500);
                  }, 1200);
                }} style={{
                  width: "100%", padding: "14px", border: "none", borderRadius: "10px",
                  background: "linear-gradient(135deg, #FFFFFF, #C0C0C0)",
                  color: "#000000", fontSize: "13px", fontWeight: "800", cursor: "pointer",
                  fontFamily: "inherit", letterSpacing: "0.06em",
                  boxShadow: "0 4px 20px rgba(255,255,255,0.12)",
                }}>
                  I'VE COMPLETED PAYMENT
                </button>

                <button onClick={() => {
                  openBrowser(purchaseModal!.payUrl);
                }} style={{
                  width: "100%", padding: "10px", border: "none", borderRadius: "8px",
                  background: "rgba(255,255,255,0.04)", color: "var(--text-secondary)", fontSize: "12px",
                  cursor: "pointer", fontFamily: "inherit", marginTop: "8px",
                }}>Reopen payment page</button>

                <button onClick={() => { setPurchasePhase("confirm"); }} style={{
                  width: "100%", padding: "10px", border: "none", borderRadius: "8px",
                  background: "transparent", color: "var(--text-faint)", fontSize: "11px",
                  cursor: "pointer", fontFamily: "inherit", marginTop: "4px",
                }}>Cancel</button>
              </div>
            )}

            {purchasePhase === "processing" && (
              <div style={{ textAlign: "center", padding: "20px 0" }}>
                <div style={{ fontSize: "14px", color: "#FFFFFF", fontWeight: "600", marginBottom: "16px" }}>
                  Processing payment...
                </div>
                <div style={{
                  width: "40px", height: "40px", margin: "0 auto",
                  border: "3px solid rgba(255,255,255,0.08)", borderTopColor: "#FFFFFF",
                  borderRadius: "50%", animation: "spin 0.8s linear infinite",
                }} />
                <style>{`@keyframes spin { to { transform: rotate(360deg); } }`}</style>
              </div>
            )}

            {purchasePhase === "success" && (
              <div style={{ textAlign: "center", padding: "20px 0" }}>
                <div style={{
                  width: "48px", height: "48px", margin: "0 auto 16px",
                  background: "rgba(110,231,160,0.12)", borderRadius: "50%",
                  display: "flex", alignItems: "center", justifyContent: "center",
                  fontSize: "24px",
                }}>✓</div>
                <div style={{ fontSize: "16px", color: "var(--accent-green)", fontWeight: "700", marginBottom: "6px" }}>
                  Payment Successful!
                </div>
                <div style={{ fontSize: "12px", color: "var(--text-muted)" }}>
                  +{purchaseModal.amount.toLocaleString()} Pulsar Points added
                </div>
              </div>
            )}
          </div>
        </div>
      )}
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
      {c.id === "cape_phantom" && <>
        {/* Spectral wisps */}
        {[[cx-10,25],[cx+8,40],[cx-5,60],[cx+12,75],[cx-8,85]].map(([x,y],i) => (
          <ellipse key={i} cx={x} cy={y} rx="6" ry="12" fill="#E8E8FF" opacity={0.12+i*0.03}
            style={{animation:`pv-float ${3+i*0.5}s ease-in-out ${i*0.4}s infinite`}} />
        ))}
        {/* Ghost faces */}
        {[[cx-6,35],[cx+10,65]].map(([x,y],i) => (
          <g key={i} opacity={0.15}>
            <circle cx={x} cy={y} r="5" fill="none" stroke="#DDDDFF" strokeWidth="0.5" />
            <circle cx={(x as number)-2} cy={(y as number)-1} r="0.8" fill="#CCCCFF" />
            <circle cx={(x as number)+2} cy={(y as number)-1} r="0.8" fill="#CCCCFF" />
          </g>
        ))}
      </>}
      {c.id === "cape_neon" && <>
        {/* Neon grid */}
        {[25,40,55,70].map((y,i) => (
          <line key={`h${i}`} x1={cx-26} y1={y} x2={cx+26} y2={y} stroke="#FF32C8" strokeWidth="0.5" opacity="0.3" />
        ))}
        {[cx-18,cx-6,cx+6,cx+18].map((x,i) => (
          <line key={`v${i}`} x1={x} y1={12} x2={x} y2={85} stroke="#00FFFF" strokeWidth="0.5" opacity="0.25" />
        ))}
        {/* Glitch blocks */}
        {[[cx-12,30,8,3,"#FF40D0"],[cx+5,55,10,2,"#00EEFF"],[cx-8,72,6,3,"#FF60E0"]].map(([x,y,w,h,col],i) => (
          <rect key={i} x={x as number} y={y as number} width={w as number} height={h as number} fill={col as string} opacity={0.2+i*0.05}
            style={{animation:`pv-shimmer ${1+i*0.3}s ease-in-out ${i*0.2}s infinite`}} />
        ))}
        {/* Neon glow spots */}
        <circle cx={cx-8} cy={35} r="4" fill="#FF32C8" opacity="0.15" filter={`url(#blur-${c.id})`} />
        <circle cx={cx+10} cy={60} r="5" fill="#00FFFF" opacity="0.12" filter={`url(#blur-${c.id})`} />
      </>}
      {c.id === "cape_lava" && <>
        {/* Lava cracks */}
        {[[cx-15,25,cx+5,35],[cx+3,45,cx+20,55],[cx-10,60,cx+8,75]].map(([x1,y1,x2,y2],i) => (
          <line key={i} x1={x1} y1={y1} x2={x2} y2={y2} stroke="#FF8020" strokeWidth="1.5" opacity={0.4+i*0.05}
            style={{animation:`pv-glow ${2+i*0.3}s ease-in-out ${i*0.3}s infinite`}} />
        ))}
        {/* Molten pools */}
        {[[cx-8,30],[cx+10,50],[cx-3,70]].map(([x,y],i) => (
          <ellipse key={i} cx={x} cy={y} rx="5" ry="3" fill="#FF6010" opacity={0.2+i*0.05}
            style={{animation:`pv-pulse ${1.5+i*0.2}s ease-in-out ${i*0.2}s infinite`}} />
        ))}
        <ellipse cx={cx} cy={80} rx="25" ry="8" fill="#FF4400" opacity="0.1" filter={`url(#blur-${c.id})`} />
      </>}
      {c.id === "cape_sakura" && <>
        {/* Branch */}
        <line x1={cx-20} y1={45} x2={cx+20} y2={30} stroke="#644028" strokeWidth="1.5" opacity="0.4" />
        {/* Flowers */}
        {[[cx-12,32],[cx+8,25],[cx-6,50],[cx+10,58],[cx-10,72],[cx+5,78]].map(([x,y],i) => (
          <g key={i} style={{animation:`pv-float ${2.5+i*0.3}s ease-in-out ${i*0.25}s infinite`,transformOrigin:`${x}px ${y}px`}}>
            {[0,72,144,216,288].map((a,j) => (
              <ellipse key={j} cx={x} cy={(y as number)-3} rx="1.5" ry="3.5" fill="#FFD8E6" opacity="0.5"
                transform={`rotate(${a} ${x} ${y})`} />
            ))}
            <circle cx={x} cy={y} r="1.5" fill="#FFF0F4" opacity="0.7" />
          </g>
        ))}
        {/* Falling petals */}
        {Array.from({length:5}).map((_,i) => (
          <ellipse key={i} cx={cx-15+i*8} cy={20+i*14} rx="2" ry="1" fill="#FFE0EC" opacity={0.3+i*0.05}
            transform={`rotate(${30+i*25} ${cx-15+i*8} ${20+i*14})`}
            style={{animation:`pv-drift ${3+i*0.4}s ease-in-out ${i*0.3}s infinite`}} />
        ))}
      </>}
      {c.id === "cape_storm" && <>
        {/* Dark clouds */}
        {[[cx-12,18],[cx+8,15],[cx,22]].map(([x,y],i) => (
          <ellipse key={i} cx={x} cy={y} rx="12" ry="5" fill="#1A1C22" opacity={0.3+i*0.05} />
        ))}
        {/* Lightning bolt */}
        <path d={`M${cx+2} 28 L${cx-3} 45 L${cx+1} 45 L${cx-4} 65`} fill="none" stroke="#FFFFD8" strokeWidth="1.5" opacity="0.7"
          style={{animation:"pv-shimmer 2s ease-in-out infinite"}} />
        {/* Rain streaks */}
        {Array.from({length:8}).map((_,i) => (
          <line key={i} x1={cx-20+i*6} y1={35+i*3} x2={cx-21+i*6} y2={42+i*3} stroke="#A0B8D0" strokeWidth="0.5" opacity={0.2+i*0.03} />
        ))}
      </>}
      {c.id === "cape_solar" && <>
        {/* Corona */}
        <circle cx={cx} cy={30} r="16" fill="#FFE060" opacity="0.1" filter={`url(#blur-${c.id})`} style={{animation:"pv-pulse 3s ease-in-out infinite"}} />
        <circle cx={cx} cy={30} r="10" fill="#FFCC40" opacity="0.2" />
        <circle cx={cx} cy={30} r="6" fill="#FFE080" opacity="0.4" />
        {/* Sun rays */}
        {[0,30,60,90,120,150,180,210,240,270,300,330].map(a => (
          <line key={a} x1={cx+Math.cos(a*Math.PI/180)*11} y1={30+Math.sin(a*Math.PI/180)*11}
            x2={cx+Math.cos(a*Math.PI/180)*22} y2={30+Math.sin(a*Math.PI/180)*22}
            stroke="#FFD060" strokeWidth="0.5" opacity="0.25" style={{animation:`pv-glow ${2+a/180}s ease-in-out infinite`}} />
        ))}
        {/* Flare arc */}
        <path d={`M${cx-15} 65 Q${cx} 45 ${cx+15} 65`} fill="none" stroke="#FFA030" strokeWidth="1" opacity="0.25" />
      </>}
      {c.id === "cape_amethyst" && <>
        {/* Crystal shards */}
        {[[cx-10,25,12],[cx+8,35,14],[cx-5,55,10],[cx+12,65,11],[cx-8,80,9]].map(([x,y,h],i) => (
          <polygon key={i} points={`${x},${(y as number)-(h as number)} ${(x as number)+4},${y} ${(x as number)-4},${y}`}
            fill="#8840C0" opacity={0.3+i*0.05} style={{animation:`pv-shimmer ${2+i*0.4}s ease-in-out ${i*0.25}s infinite`}} />
        ))}
        {/* Crystal highlights */}
        {[[cx-10,20],[cx+8,30],[cx-5,50],[cx+12,60]].map(([x,y],i) => (
          <circle key={i} cx={x} cy={y} r="0.8" fill="#DDB0FF" opacity="0.6" />
        ))}
        {/* Purple glow */}
        <ellipse cx={cx} cy={50} rx="20" ry="30" fill="#6020A0" opacity="0.08" filter={`url(#blur-${c.id})`} />
      </>}
      {c.id === "cape_inferno" && <>
        {/* Hellfire glow */}
        <ellipse cx={cx} cy={85} rx="28" ry="10" fill="#FF2200" opacity="0.15" filter={`url(#blur-${c.id})`} />
        {/* Flames */}
        {[0,1,2,3,4,5,6].map(i => {
          const fx = cx - 18 + i * 6;
          const fh = 15 + Math.sin(i*1.8)*8;
          return <path key={i} d={`M${fx} 88 Q${fx+2} ${88-fh*0.6} ${fx+3} ${88-fh} Q${fx+4} ${88-fh*0.6} ${fx+5} 88`}
            fill={i%2===0?"#FF3010":"#CC1000"}
            style={{animation:`pv-flicker ${0.5+i*0.12}s ease-in-out ${i*0.08}s infinite`,transformOrigin:`${fx+3}px 88px`}} />;
        })}
        {/* Skull */}
        <circle cx={cx} cy={40} r="6" fill="#401005" opacity="0.4" />
        <circle cx={cx-2} cy={38} r="1" fill="#FF5500" opacity="0.6" />
        <circle cx={cx+2} cy={38} r="1" fill="#FF5500" opacity="0.6" />
      </>}
      {c.id === "cape_drift" && <>
        {/* Pastel gradient bands */}
        {[[20,"#FFB8D0"],[35,"#B8D0FF"],[50,"#D0B8FF"],[65,"#FFD0B8"],[80,"#B8FFD0"]].map(([y,col],i) => (
          <rect key={i} x={cx-25} y={y as number} width="50" height="8" fill={col as string} opacity={0.12+i*0.02} rx="2"
            style={{animation:`pv-drift ${3+i*0.3}s ease-in-out ${i*0.3}s infinite`}} />
        ))}
        {/* Sparkle dots */}
        {Array.from({length:6}).map((_,i) => (
          <circle key={i} cx={cx-12+i*5} cy={25+i*10} r="0.6" fill="#fff" opacity={0.3+i*0.08}
            style={{animation:`pv-twinkle ${2+i*0.3}s ease-in-out ${i*0.2}s infinite`}} />
        ))}
        {/* Vaporwave sun stripes */}
        {[0,1,2].map(i => (
          <line key={i} x1={cx-15} y1={78+i*3} x2={cx+15} y2={78+i*3} stroke="#FF78C8" strokeWidth="0.5" opacity="0.15" />
        ))}
      </>}
      {c.id === "cape_obsidian" && <>
        {/* Purple crack lines */}
        {[[cx-15,20,cx+5,35],[cx+3,40,cx-10,60],[cx-8,55,cx+15,75],[cx+5,70,cx-5,88]].map(([x1,y1,x2,y2],i) => (
          <line key={i} x1={x1} y1={y1} x2={x2} y2={y2} stroke="#7828B4" strokeWidth="0.8" opacity={0.3+i*0.05}
            style={{animation:`pv-glow ${3+i*0.4}s ease-in-out ${i*0.3}s infinite`}} />
        ))}
        {/* Purple glow points */}
        {[[cx-10,28],[cx+8,50],[cx-5,68],[cx+10,82]].map(([x,y],i) => (
          <circle key={i} cx={x} cy={y} r="2" fill="#A050E0" opacity={0.15+i*0.05}
            style={{animation:`pv-shimmer ${2+i*0.3}s ease-in-out ${i*0.2}s infinite`}} />
        ))}
        {/* Dark sheen */}
        <rect x={cx-22} y={30} width="44" height="2" fill="rgba(80,60,100,0.08)" transform={`rotate(-10 ${cx} 31)`} />
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
