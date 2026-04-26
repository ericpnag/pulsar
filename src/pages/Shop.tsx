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
  // Creator capes — verified only, not buyable with points
  { id: "cape_creator", name: "Creator", type: "cape", price: -1, color: "#FFD700", color2: "#FF4500", description: "Verified content creators only" },
  { id: "cape_youtube", name: "YouTube", type: "cape", price: -1, color: "#FF0000", color2: "#CC0000", description: "Verified YouTube creators only" },
  { id: "cape_twitch", name: "Twitch", type: "cape", price: -1, color: "#9146FF", color2: "#6441A5", description: "Verified Twitch streamers only" },
  { id: "cape_tiktok", name: "TikTok", type: "cape", price: -1, color: "#00F2EA", color2: "#FF0050", description: "Verified TikTok creators only" },
  // OG cape — limited edition, claimed via button
  { id: "cape_og", name: "OG Pulsar", type: "cape", price: -2, color: "#FFFFFF", color2: "#808080", description: "Limited edition — first 100 players" },
];

// Creator codes that unlock exclusive cosmetics
// Verified creator codes — only given to approved creators, unlocks exclusive capes
const VERIFIED_CREATORS: Record<string, string[]> = {
  "CRAZYFISH564": ["cape_creator", "cape_youtube"],
  "BIGBOBBY68": ["cape_creator", "cape_youtube"],
  "OLIVERTREEEE": ["cape_creator"],
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
    if (c.price < 0 || c.price > points || isOwned(c.id)) return;
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

    // Verified creator codes — unlock exclusive capes
    if (VERIFIED_CREATORS[code]) {
      if (localUsed.includes(code)) { setRedeemMsg({ text: "Code already used", ok: false }); return; }
      const capes = VERIFIED_CREATORS[code];
      const newPurchased = [...purchased];
      for (const id of capes) { if (!newPurchased.includes(id)) newPurchased.push(id); }
      localStorage.setItem("pulsar-used-codes", JSON.stringify([...localUsed, code]));
      save(points, newPurchased, equipped);
      setRedeemMsg({ text: `Verified! ${capes.length} creator cape${capes.length > 1 ? "s" : ""} unlocked`, ok: true });
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

  const [shopTab, setShopTab] = useState<"shop" | "owned">("shop");

  const ownedCount = COSMETICS.filter(c => isOwned(c.id)).length;
  const availableCount = COSMETICS.filter(c => c.price >= 0).length;
  const buyableCapes = COSMETICS.filter(c => c.price > 0 && c.type === "cape");
  const featuredCape = buyableCapes.find(c => c.id === "cape_blackhole") || buyableCapes[0];

  const displayedCapes = shopTab === "owned"
    ? filtered.filter(c => isOwned(c.id))
    : filtered;

  return (
    <div className="fade-in" style={{ display: "flex", flexDirection: "column", height: "100%", padding: "28px", gap: "18px", overflowY: "auto" }}>
      <style>{PREVIEW_ANIMATIONS}</style>
      <style>{`
        @keyframes hero-float { 0%,100%{transform:translateY(0) rotate(-6deg)} 50%{transform:translateY(-8px) rotate(-6deg)} }
        @keyframes hero-star { 0%,100%{opacity:0.2} 50%{opacity:0.8} }
      `}</style>

      {/* Header */}
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start" }}>
        <div>
          <div style={{ fontSize: "11px", fontWeight: "600", color: "#6B6985", letterSpacing: "0.08em", textTransform: "uppercase", marginBottom: "4px" }}>Cosmetics</div>
          <h2 style={{ fontSize: "22px", fontWeight: "800", color: "#F0EEFC", margin: 0, letterSpacing: "-0.02em" }}>Wardrobe</h2>
          <p style={{ fontSize: "12px", color: "#6B6985", margin: "4px 0 0 0" }}>
            {ownedCount} owned &middot; {availableCount} available
          </p>
        </div>
        <div style={{ display: "flex", alignItems: "center", gap: "12px" }}>
          {/* Shop / Owned tabs */}
          <div style={{ display: "flex", gap: "2px", background: "#0D0D17", borderRadius: "10px", padding: "3px", border: "0.5px solid #1F1F2E" }}>
            {(["shop", "owned"] as const).map(tab => (
              <button key={tab} onClick={() => setShopTab(tab)} style={{
                padding: "6px 16px", borderRadius: "8px", border: "none",
                background: shopTab === tab ? "#5B3FA6" : "transparent",
                color: shopTab === tab ? "#F0EEFC" : "#6B6985",
                fontSize: "12px", fontWeight: "600", cursor: "pointer", fontFamily: "inherit",
                transition: "all 0.15s", textTransform: "capitalize",
              }}>
                {tab}
              </button>
            ))}
          </div>
          {/* Points badge */}
          <div style={{
            padding: "8px 16px", display: "flex", alignItems: "center", gap: "8px",
            background: "#0D0D17", borderRadius: "10px", border: "0.5px solid #1F1F2E",
          }}>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="12" r="10" fill="#000000"/>
              <ellipse cx="12" cy="12" rx="11" ry="4" fill="none" stroke="#C4B5FD" strokeWidth="1.5" opacity="0.6"/>
              <circle cx="12" cy="12" r="4" fill="#000000"/>
              <circle cx="12" cy="12" r="3" fill="none" stroke="#C4B5FD" strokeWidth="0.3" opacity="0.5"/>
            </svg>
            <span style={{ fontSize: "16px", fontWeight: "800", color: "#F0EEFC", fontFamily: "monospace" }}>{points}</span>
          </div>
        </div>
      </div>

      {/* Hero Banner — featured cape */}
      {featuredCape && (
        <div style={{
          position: "relative", height: "220px", borderRadius: "16px", overflow: "hidden",
          background: `linear-gradient(135deg, ${featuredCape.color}18, #0D0D17 60%, ${featuredCape.color2}10)`,
          border: "0.5px solid #1F1F2E",
        }}>
          {/* Stars in background */}
          {Array.from({ length: 20 }).map((_, i) => (
            <div key={i} style={{
              position: "absolute",
              left: `${5 + (i * 37) % 90}%`,
              top: `${8 + (i * 23) % 80}%`,
              width: `${1 + (i % 3)}px`,
              height: `${1 + (i % 3)}px`,
              borderRadius: "50%",
              background: "#C4B5FD",
              opacity: 0.15 + (i % 4) * 0.1,
              animation: `hero-star ${2 + (i % 3)}s ease-in-out ${i * 0.3}s infinite`,
            }} />
          ))}
          {/* Content */}
          <div style={{
            position: "relative", zIndex: 1, padding: "32px 36px", height: "100%",
            display: "flex", alignItems: "center", justifyContent: "space-between",
          }}>
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: "10px", fontWeight: "700", color: "#C4B5FD", letterSpacing: "0.1em", textTransform: "uppercase", marginBottom: "8px" }}>
                Featured
              </div>
              <div style={{ fontSize: "28px", fontWeight: "800", color: "#F0EEFC", letterSpacing: "-0.02em", marginBottom: "6px" }}>
                {featuredCape.name}
              </div>
              <div style={{ fontSize: "13px", color: "#8A88A8", marginBottom: "20px", maxWidth: "280px" }}>
                {featuredCape.description}
              </div>
              {isOwned(featuredCape.id) ? (
                <button onClick={() => equip(featuredCape)} style={{
                  padding: "10px 28px", borderRadius: "10px",
                  border: isEquipped(featuredCape.id) ? "none" : "0.5px solid #5B3FA6",
                  background: isEquipped(featuredCape.id) ? "linear-gradient(135deg, #5B3FA6, #7C5FD6)" : "rgba(91,63,166,0.2)",
                  color: "#F0EEFC", fontSize: "13px", fontWeight: "700", cursor: "pointer",
                  fontFamily: "inherit", transition: "all 0.15s",
                }}>
                  {isEquipped(featuredCape.id) ? "Equipped" : "Equip"}
                </button>
              ) : (
                <button onClick={() => buy(featuredCape)} disabled={featuredCape.price > points} style={{
                  padding: "10px 28px", borderRadius: "10px", border: "none",
                  background: featuredCape.price <= points ? "linear-gradient(135deg, #5B3FA6, #7C5FD6)" : "rgba(91,63,166,0.15)",
                  color: featuredCape.price <= points ? "#F0EEFC" : "#6B6985",
                  fontSize: "13px", fontWeight: "700", cursor: featuredCape.price <= points ? "pointer" : "not-allowed",
                  fontFamily: "inherit", transition: "all 0.15s",
                  display: "flex", alignItems: "center", gap: "6px",
                }}>
                  <span style={{ fontFamily: "monospace" }}>{featuredCape.price}</span> points
                </button>
              )}
            </div>
            {/* Cape preview on right */}
            <div style={{
              width: "160px", height: "160px", position: "relative", flexShrink: 0,
              animation: "hero-float 4s ease-in-out infinite",
            }}>
              <div style={{
                width: "120px", height: "150px", margin: "5px auto",
                borderRadius: "16px",
                background: `linear-gradient(180deg, ${featuredCape.color}, ${featuredCape.color2})`,
                border: "2px solid rgba(255,255,255,0.08)",
                boxShadow: `0 8px 40px ${featuredCape.color}40, 0 0 80px ${featuredCape.color}15`,
                position: "relative", overflow: "hidden",
              }}>
                {/* Inner border */}
                <div style={{
                  position: "absolute", inset: "4px", borderRadius: "12px",
                  border: "1px solid rgba(255,255,255,0.1)",
                }} />
                {/* Shine */}
                <div style={{
                  position: "absolute", top: 0, left: 0, right: "50%", bottom: 0,
                  background: "linear-gradient(135deg, rgba(255,255,255,0.12), transparent)",
                  borderRadius: "14px 0 0 14px",
                }} />
              </div>
            </div>
          </div>
        </div>
      )}

      {/* OG Cape — Limited Edition */}
      <OGCapeSection onClaim={() => {
        if (!purchased.includes("cape_og")) {
          save(points, [...purchased, "cape_og"], equipped);
        }
      }} isOwned={purchased.includes("cape_og")} />

      {/* Points Store */}
      <div style={{ display: "grid", gridTemplateColumns: "repeat(4, 1fr)", gap: "8px" }}>
        {POINT_TIERS.map((tier, i) => (
          <div key={i} onClick={() => {
            setPurchaseModal(tier);
            setPurchasePhase("confirm");
          }} style={{
            padding: "14px", textAlign: "center", cursor: "pointer",
            position: "relative", transition: "all 0.15s",
            background: tier.popular ? "rgba(91,63,166,0.08)" : "#0D0D17",
            borderRadius: "12px",
            border: tier.popular ? "0.5px solid #5B3FA6" : "0.5px solid #1F1F2E",
          }}
          onMouseEnter={e => { e.currentTarget.style.transform = "translateY(-2px)"; e.currentTarget.style.boxShadow = "0 4px 16px rgba(91,63,166,0.15)"; }}
          onMouseLeave={e => { e.currentTarget.style.transform = "translateY(0)"; e.currentTarget.style.boxShadow = "none"; }}
          >
            {tier.popular && (
              <div style={{
                position: "absolute", top: "-8px", left: "50%", transform: "translateX(-50%)",
                background: "linear-gradient(135deg, #5B3FA6, #7C5FD6)", color: "#F0EEFC",
                fontSize: "9px", fontWeight: "800", padding: "2px 10px", borderRadius: "8px",
                letterSpacing: "0.05em", textTransform: "uppercase",
              }}>Popular</div>
            )}
            <div style={{ fontSize: "10px", fontWeight: "700", color: "#6B6985", letterSpacing: "0.08em", textTransform: "uppercase", marginBottom: "6px" }}>
              Pulsar Points
            </div>
            <div style={{ fontSize: "22px", fontWeight: "800", color: "#F0EEFC", marginBottom: "2px", fontFamily: "monospace" }}>
              {tier.amount.toLocaleString()}
            </div>
            <div style={{
              fontSize: "13px", fontWeight: "700", color: "#F0EEFC",
              background: "rgba(91,63,166,0.12)", borderRadius: "6px", padding: "4px 12px",
              display: "inline-block", marginTop: "6px",
            }}>{tier.price}</div>
          </div>
        ))}
      </div>

      {/* Redeem Code */}
      <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
        <button onClick={() => setShowRedeem(!showRedeem)} style={{
          background: "#0D0D17", border: "0.5px solid #1F1F2E",
          borderRadius: "8px", padding: "7px 14px", fontSize: "12px", fontWeight: "600",
          color: "#8A88A8", cursor: "pointer", fontFamily: "inherit",
          transition: "all 0.15s",
        }}>
          Redeem Code
        </button>
        {showRedeem && (
          <>
            <input
              className="pulsar-input"
              value={redeemCode}
              onChange={e => { setRedeemCode(e.target.value); setRedeemMsg(null); }}
              onKeyDown={e => e.key === "Enter" && redeemPoints()}
              placeholder="Enter code..."
              style={{ flex: 1, padding: "7px 12px", fontSize: "12px", background: "#0D0D17", border: "0.5px solid #1F1F2E", borderRadius: "8px", color: "#F0EEFC" }}
            />
            <button onClick={redeemPoints} className="pulsar-btn" style={{ padding: "7px 16px", fontSize: "12px", background: "#5B3FA6", border: "none", borderRadius: "8px", color: "#F0EEFC", cursor: "pointer", fontFamily: "inherit", fontWeight: "600" }}>
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

      <div style={{ height: "1px", background: "#1F1F2E" }} />

      {/* Creators Section */}
      <div>
        <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: "14px" }}>
          <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
            <span style={{ fontSize: "16px" }}>⭐</span>
            <span style={{ fontSize: "14px", fontWeight: "800", color: "#FFD700", letterSpacing: "0.04em" }}>VERIFIED CREATORS</span>
            <span style={{ fontSize: "11px", color: "#6B6985" }}>Exclusive capes for verified partners</span>
          </div>
        </div>
        <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(200px, 1fr))", gap: "10px" }}>
          {[
            { name: "CrazyFish564", yt: "https://www.youtube.com/@crazyfish564", role: "YouTube Creator", color: "#FF0000", cape: "cape_youtube" },
            { name: "BigBobby68", yt: "https://www.youtube.com/@BigBobby68", role: "YouTube Creator", color: "#FF0000", cape: "cape_creator" },
            { name: "OliverTreeee", role: "Pulsar Creator", color: "#9146FF", cape: "cape_og" },
          ].map(creator => {
            const cape = COSMETICS.find(c => c.id === creator.cape);
            return (
            <div key={creator.name} style={{
              padding: "0", overflow: "hidden", transition: "all 0.2s",
              background: "#0D0D17", borderRadius: "12px", border: "0.5px solid #1F1F2E",
            }}>
              {/* Banner with cape preview */}
              <div style={{
                height: "56px",
                background: cape ? `linear-gradient(135deg, ${cape.color}, ${cape.color2})` : `linear-gradient(135deg, ${creator.color}40, ${creator.color}15)`,
                position: "relative",
              }}>
                <div style={{
                  position: "absolute", bottom: "-16px", left: "14px",
                  width: "36px", height: "36px", borderRadius: "10px",
                  border: "2px solid #0D0D17", overflow: "hidden",
                  background: "#1a1a1a",
                }}>
                  <img
                    src={`https://mc-heads.net/avatar/${creator.name}/36`}
                    alt="" width={36} height={36}
                    style={{ display: "block" }}
                    onError={(e) => { (e.target as HTMLImageElement).style.display = "none"; }}
                  />
                </div>
                {cape && (
                  <div style={{ position: "absolute", top: "6px", right: "10px", fontSize: "9px", fontWeight: "700",
                    background: "rgba(0,0,0,0.5)", color: "#fff", padding: "2px 8px", borderRadius: "4px",
                    backdropFilter: "blur(4px)",
                  }}>
                    Wears: {cape.name}
                  </div>
                )}
              </div>
              {/* Info */}
              <div style={{ padding: "22px 14px 14px" }}>
                <div style={{ fontSize: "13px", fontWeight: "700", color: "#F0EEFC", marginBottom: "2px" }}>
                  {creator.name}
                </div>
                <div style={{ fontSize: "10px", color: "#6B6985", marginBottom: "10px" }}>
                  {creator.role}
                </div>
                {/* Social + Verified badge */}
                <div style={{ display: "flex", gap: "6px" }}>
                  {creator.yt && (
                    <div onClick={() => invoke("open_browser", { url: creator.yt })} style={{
                      flex: 1, padding: "7px", borderRadius: "6px", fontSize: "10px", fontWeight: "600",
                      background: "rgba(255,0,0,0.1)", color: "#FF4444", cursor: "pointer",
                      display: "flex", alignItems: "center", justifyContent: "center", gap: "4px",
                    }}>
                      <svg width="10" height="10" viewBox="0 0 24 24" fill="currentColor"><path d="M23.5 6.2c-.3-1-1-1.8-2-2.1C19.6 3.5 12 3.5 12 3.5s-7.6 0-9.5.6c-1 .3-1.8 1-2 2.1C0 8.1 0 12 0 12s0 3.9.5 5.8c.3 1 1 1.8 2 2.1 1.9.6 9.5.6 9.5.6s7.6 0 9.5-.6c1-.3 1.8-1 2-2.1.5-1.9.5-5.8.5-5.8s0-3.9-.5-5.8zM9.5 15.5V8.5l6.5 3.5-6.5 3.5z"/></svg>
                      YouTube
                    </div>
                  )}
                  <div style={{
                    flex: 1, padding: "7px", borderRadius: "6px", fontSize: "10px", fontWeight: "700",
                    background: "rgba(76,175,80,0.1)", color: "#4CAF50",
                    display: "flex", alignItems: "center", justifyContent: "center", gap: "4px",
                  }}>
                    <svg width="10" height="10" viewBox="0 0 24 24" fill="currentColor"><path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/></svg>
                    Verified
                  </div>
                </div>
              </div>
            </div>
          );})}
        </div>
      </div>

      <div style={{ height: "1px", background: "#1F1F2E" }} />

      {/* Category pills */}
      <div style={{ display: "flex", gap: "6px" }}>
        {["all", "cape"].map(t => {
          const count = t === "all" ? COSMETICS.length : COSMETICS.filter(c => c.type === t).length;
          return (
            <button key={t} onClick={() => setFilter(t)} style={{
              padding: "7px 18px", borderRadius: "20px",
              background: filter === t ? "#5B3FA6" : "#0D0D17",
              border: filter === t ? "0.5px solid #5B3FA6" : "0.5px solid #1F1F2E",
              color: filter === t ? "#F0EEFC" : "#6B6985",
              fontSize: "12px", fontWeight: "600", cursor: "pointer",
              transition: "all 0.15s", fontFamily: "inherit",
              display: "flex", alignItems: "center", gap: "6px",
            }}>
              {t === "all" ? "All Capes" : TYPE_LABELS[t]}
              <span style={{
                fontSize: "10px", fontWeight: "700",
                background: filter === t ? "rgba(255,255,255,0.15)" : "rgba(255,255,255,0.06)",
                padding: "1px 6px", borderRadius: "10px",
                color: filter === t ? "#F0EEFC" : "#6B6985",
              }}>{count}</span>
            </button>
          );
        })}
      </div>

      {/* Cape Grid — 4 columns */}
      <div style={{ display: "grid", gridTemplateColumns: "repeat(4, 1fr)", gap: "12px" }}>
        {displayedCapes.map(c => {
          const own = isOwned(c.id);
          const eq = isEquipped(c.id);
          const isFree = c.price === 0;
          const isLimited = c.price === -2;
          return (
            <div key={c.id} style={{
              position: "relative", borderRadius: "14px", overflow: "hidden",
              border: eq ? "0.5px solid #5B3FA6" : "0.5px solid #1F1F2E",
              background: "#0D0D17",
              transition: "all 0.25s cubic-bezier(0.4, 0, 0.2, 1)",
              cursor: "pointer",
            }}
            onMouseEnter={e => {
              e.currentTarget.style.transform = "translateY(-2px)";
              e.currentTarget.style.boxShadow = `0 8px 30px rgba(91,63,166,0.2)`;
              e.currentTarget.style.borderColor = "#5B3FA6";
            }}
            onMouseLeave={e => {
              e.currentTarget.style.transform = "";
              e.currentTarget.style.boxShadow = "";
              e.currentTarget.style.borderColor = eq ? "#5B3FA6" : "#1F1F2E";
            }}
            onClick={() => { if (own) equip(c); else if (c.price >= 0) buy(c); }}
            >
              {/* Preview area — square aspect ratio */}
              <div style={{
                width: "100%", aspectRatio: "1", position: "relative",
                background: `radial-gradient(circle at center, ${c.color}20, transparent 70%)`,
                overflow: "hidden",
                filter: own ? "none" : "grayscale(0.6) brightness(0.5)",
                transition: "filter 0.3s",
              }}>
                <CosmeticPreview cosmetic={c} />

                {/* Equipped badge — top right */}
                {eq && (
                  <div style={{
                    position: "absolute", top: "8px", right: "8px",
                    fontSize: "9px", fontWeight: "700", color: "#C4B5FD", letterSpacing: "0.06em",
                    background: "rgba(91,63,166,0.25)", padding: "3px 8px", borderRadius: "6px",
                    border: "0.5px solid rgba(91,63,166,0.4)",
                  }}>EQUIPPED</div>
                )}

                {/* FREE or LIMITED badge — top left */}
                {isFree && !own && (
                  <div style={{
                    position: "absolute", top: "8px", left: "8px",
                    fontSize: "9px", fontWeight: "700", color: "#4CAF50",
                    background: "rgba(76,175,80,0.15)", padding: "3px 8px", borderRadius: "6px",
                    border: "0.5px solid rgba(76,175,80,0.25)",
                  }}>FREE</div>
                )}
                {isLimited && (
                  <div style={{
                    position: "absolute", top: "8px", left: "8px",
                    fontSize: "9px", fontWeight: "700", color: "#C4B5FD",
                    background: "rgba(91,63,166,0.15)", padding: "3px 8px", borderRadius: "6px",
                    border: "0.5px solid rgba(91,63,166,0.25)",
                  }}>LIMITED</div>
                )}
                {c.price === -1 && (
                  <div style={{
                    position: "absolute", top: "8px", left: "8px",
                    fontSize: "9px", fontWeight: "700", color: "#FFD700",
                    background: "rgba(255,215,0,0.1)", padding: "3px 8px", borderRadius: "6px",
                    border: "0.5px solid rgba(255,215,0,0.2)",
                  }}>VERIFIED</div>
                )}
              </div>

              {/* Name + price row */}
              <div style={{ padding: "10px 12px 12px", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                <div>
                  <div style={{ fontSize: "13px", fontWeight: "700", color: "#F0EEFC", letterSpacing: "-0.01em" }}>{c.name}</div>
                  <div style={{ fontSize: "10px", color: "#6B6985", marginTop: "2px" }}>{c.description}</div>
                </div>
                {!own && c.price >= 0 && (
                  <div style={{
                    fontSize: "12px", fontWeight: "700", color: c.price > points ? "#6B6985" : "#F0EEFC",
                    fontFamily: "monospace", flexShrink: 0, marginLeft: "8px",
                  }}>
                    {c.price === 0 ? "Free" : c.price}
                  </div>
                )}
                {own && !eq && (
                  <div style={{
                    fontSize: "10px", fontWeight: "600", color: "#8A88A8",
                    flexShrink: 0, marginLeft: "8px",
                  }}>Owned</div>
                )}
              </div>
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
            background: "#0D0D17", border: "0.5px solid #1F1F2E",
            borderRadius: "16px", padding: "32px 36px", width: "min(400px, 90vw)",
            boxShadow: "0 24px 64px rgba(0,0,0,0.6)",
          }}>
            {purchasePhase === "confirm" && (<>
              <div style={{ textAlign: "center", marginBottom: "24px" }}>
                <div style={{ fontSize: "10px", fontWeight: "700", letterSpacing: "0.12em", color: "#6B6985", textTransform: "uppercase", marginBottom: "8px" }}>
                  Purchase Pulsar Points
                </div>
                <div style={{ fontSize: "42px", fontWeight: "800", color: "#F0EEFC", lineHeight: 1, fontFamily: "monospace" }}>
                  {purchaseModal.amount.toLocaleString()}
                </div>
                {purchaseModal.bonus && (
                  <div style={{ fontSize: "12px", color: "var(--accent-green)", fontWeight: "600", marginTop: "4px" }}>
                    {purchaseModal.bonus}
                  </div>
                )}
                <div style={{ fontSize: "11px", color: "#6B6985", marginTop: "12px" }}>Pulsar Points</div>
              </div>

              <div style={{
                background: "rgba(255,255,255,0.02)", border: "0.5px solid #1F1F2E",
                borderRadius: "10px", padding: "16px", marginBottom: "20px",
              }}>
                <div style={{ display: "flex", justifyContent: "space-between", marginBottom: "8px" }}>
                  <span style={{ fontSize: "12px", color: "#8A88A8" }}>{purchaseModal.amount.toLocaleString()} Pulsar Points</span>
                  <span style={{ fontSize: "12px", color: "#F0EEFC", fontWeight: "600" }}>{purchaseModal.price}</span>
                </div>
                {purchaseModal.bonus && (
                  <div style={{ display: "flex", justifyContent: "space-between", marginBottom: "8px" }}>
                    <span style={{ fontSize: "12px", color: "var(--accent-green)" }}>{purchaseModal.bonus}</span>
                    <span style={{ fontSize: "12px", color: "var(--accent-green)", fontWeight: "600" }}>FREE</span>
                  </div>
                )}
                <div style={{ height: "1px", background: "#1F1F2E", margin: "8px 0" }} />
                <div style={{ display: "flex", justifyContent: "space-between" }}>
                  <span style={{ fontSize: "13px", color: "#F0EEFC", fontWeight: "700" }}>Total</span>
                  <span style={{ fontSize: "13px", color: "#F0EEFC", fontWeight: "800" }}>{purchaseModal.price}</span>
                </div>
              </div>

              <button onClick={() => {
                openBrowser(purchaseModal.payUrl);
                setPurchasePhase("paying");
              }} style={{
                width: "100%", padding: "14px", border: "none", borderRadius: "10px",
                background: "linear-gradient(135deg, #5B3FA6, #7C5FD6)",
                color: "#F0EEFC", fontSize: "13px", fontWeight: "800", cursor: "pointer",
                fontFamily: "inherit", letterSpacing: "0.06em",
                boxShadow: "0 4px 20px rgba(91,63,166,0.3)",
                transition: "all 0.2s",
              }}
              onMouseEnter={e => { e.currentTarget.style.transform = "translateY(-1px)"; e.currentTarget.style.boxShadow = "0 8px 32px rgba(91,63,166,0.4)"; }}
              onMouseLeave={e => { e.currentTarget.style.transform = "translateY(0)"; e.currentTarget.style.boxShadow = "0 4px 20px rgba(91,63,166,0.3)"; }}
              >
                PAY {purchaseModal.price}
              </button>

              <button onClick={() => setPurchaseModal(null)} style={{
                width: "100%", padding: "10px", border: "none", borderRadius: "8px",
                background: "transparent", color: "#6B6985", fontSize: "12px",
                cursor: "pointer", fontFamily: "inherit", marginTop: "8px",
              }}>Cancel</button>
            </>)}

            {purchasePhase === "paying" && (
              <div style={{ textAlign: "center", padding: "10px 0" }}>
                <div style={{ fontSize: "14px", color: "#F0EEFC", fontWeight: "600", marginBottom: "8px" }}>
                  Complete payment in your browser
                </div>
                <div style={{ fontSize: "12px", color: "#8A88A8", marginBottom: "20px", lineHeight: 1.6 }}>
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
                  background: "linear-gradient(135deg, #5B3FA6, #7C5FD6)",
                  color: "#F0EEFC", fontSize: "13px", fontWeight: "800", cursor: "pointer",
                  fontFamily: "inherit", letterSpacing: "0.06em",
                  boxShadow: "0 4px 20px rgba(91,63,166,0.3)",
                }}>
                  I'VE COMPLETED PAYMENT
                </button>

                <button onClick={() => {
                  openBrowser(purchaseModal!.payUrl);
                }} style={{
                  width: "100%", padding: "10px", border: "none", borderRadius: "8px",
                  background: "rgba(91,63,166,0.1)", color: "#8A88A8", fontSize: "12px",
                  cursor: "pointer", fontFamily: "inherit", marginTop: "8px",
                }}>Reopen payment page</button>

                <button onClick={() => { setPurchasePhase("confirm"); }} style={{
                  width: "100%", padding: "10px", border: "none", borderRadius: "8px",
                  background: "transparent", color: "#6B6985", fontSize: "11px",
                  cursor: "pointer", fontFamily: "inherit", marginTop: "4px",
                }}>Cancel</button>
              </div>
            )}

            {purchasePhase === "processing" && (
              <div style={{ textAlign: "center", padding: "20px 0" }}>
                <div style={{ fontSize: "14px", color: "#F0EEFC", fontWeight: "600", marginBottom: "16px" }}>
                  Processing payment...
                </div>
                <div style={{
                  width: "40px", height: "40px", margin: "0 auto",
                  border: "3px solid #1F1F2E", borderTopColor: "#C4B5FD",
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
                <div style={{ fontSize: "12px", color: "#8A88A8" }}>
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
  const W = 220, H = 140;
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
    <svg width="100%" height="100%" viewBox={`0 0 ${W} ${H}`} preserveAspectRatio="xMidYMid slice" style={{ display: "block" }}>
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
        {/* Deep ocean gradient layers */}
        <ellipse cx={cx} cy={75} rx="30" ry="18" fill="#1040A0" opacity="0.12" filter={`url(#blur-${c.id})`} />
        <ellipse cx={cx-10} cy={30} rx="20" ry="15" fill="#60C8FF" opacity="0.08" filter={`url(#blur-${c.id})`} />
        {/* Waves */}
        {[22,32,42,52,62,72,82].map((y,i) => (
          <path key={i} d={`M${cx-30} ${y} Q${cx-18} ${y-5-i*0.5} ${cx-6} ${y} Q${cx+6} ${y+5+i*0.5} ${cx+18} ${y} Q${cx+26} ${y-3} ${cx+30} ${y}`}
            fill="none" stroke={i%3===0?"#80D0FF":i%3===1?"#50A8EE":"#3888DD"} strokeWidth={1.8-i*0.12} opacity={0.2+i*0.06}
            style={{animation:`pv-wave ${2+i*0.3}s ease-in-out ${i*0.35}s infinite`}} />
        ))}
        {/* Foam crests */}
        {[[cx-18,24],[cx+8,34],[cx-10,44],[cx+14,54],[cx-4,64],[cx+10,74]].map(([x,y],i) => (
          <ellipse key={i} cx={x} cy={y} rx="6" ry="1.8" fill="rgba(255,255,255,0.15)"
            style={{animation:`pv-shimmer ${2+i*0.3}s ease-in-out ${i*0.2}s infinite`}} />
        ))}
        {/* Underwater light rays */}
        {[cx-12,cx-4,cx+4,cx+12].map((x,i) => (
          <line key={i} x1={x} y1={10} x2={x+i*2-3} y2={90} stroke="#60D0FF" strokeWidth="0.5" opacity={0.04+i*0.01} />
        ))}
        {/* Bubbles */}
        {[[cx-8,30,2],[cx+12,45,1.5],[cx-14,55,1.8],[cx+6,25,1.2],[cx,68,1.6],[cx+16,60,1],[cx-4,82,1.3]].map(([x,y,r],i) => (
          <g key={i} style={{animation:`pv-float ${3+i*0.4}s ease-in-out ${i*0.25}s infinite`,transformOrigin:`${x}px ${y}px`}}>
            <circle cx={x as number} cy={y as number} r={r as number} fill="none" stroke="#A0E0FF" strokeWidth="0.5" opacity={0.3+i*0.04} />
            <circle cx={(x as number)+0.3} cy={(y as number)-0.3} r={(r as number)*0.25} fill="#fff" opacity={0.4} />
          </g>
        ))}
      </>}
      {c.id === "cape_emerald" && <>
        {/* Background crystal glow */}
        <ellipse cx={cx-5} cy={35} rx="18" ry="25" fill="#20A060" opacity="0.08" filter={`url(#blur-${c.id})`} style={{animation:"pv-glow 4s ease-in-out infinite"}} />
        <ellipse cx={cx+8} cy={65} rx="15" ry="20" fill="#40FF80" opacity="0.06" filter={`url(#blur-${c.id})`} style={{animation:"pv-glow 5s ease-in-out 1s infinite"}} />
        {/* Large gem shapes */}
        {[[cx-10,25,9],[cx+8,40,11],[cx-5,60,10],[cx+12,28,7],[cx-12,48,6],[cx+4,75,8]].map(([x,y,s],i) => (
          <g key={i} style={{animation:`pv-shimmer ${2+i*0.4}s ease-in-out ${i*0.25}s infinite`}}>
            <polygon points={`${x},${(y as number)-(s as number)} ${(x as number)+(s as number)*0.6},${y} ${x},${(y as number)+(s as number)*0.7} ${(x as number)-(s as number)*0.6},${y}`}
              fill="#50FF90" opacity={0.2+i*0.04} />
            {/* Inner facet */}
            <polygon points={`${x},${(y as number)-(s as number)*0.8} ${(x as number)+(s as number)*0.25},${(y as number)-(s as number)*0.1} ${x},${(y as number)+(s as number)*0.3} ${(x as number)-(s as number)*0.25},${(y as number)-(s as number)*0.1}`}
              fill="#A0FFD0" opacity={0.18} />
            {/* Top highlight */}
            <circle cx={x} cy={(y as number)-(s as number)*0.4} r="1" fill="#E0FFF0" opacity="0.7" />
            {/* Reflection edge */}
            <line x1={x as number} y1={(y as number)-(s as number)} x2={(x as number)+(s as number)*0.6} y2={y as number}
              stroke="#B0FFD0" strokeWidth="0.3" opacity="0.3" />
          </g>
        ))}
        {/* Sparkle particles */}
        {[[cx-15,18],[cx+15,22],[cx-8,38],[cx+16,55],[cx-14,70],[cx+8,82],[cx,15],[cx+3,85]].map(([x,y],i) => (
          <g key={i} style={{animation:`pv-twinkle ${1.5+i*0.3}s ease-in-out ${i*0.2}s infinite`}}>
            <line x1={(x as number)-2} y1={y} x2={(x as number)+2} y2={y} stroke="#B0FFD0" strokeWidth="0.4" opacity="0.5" />
            <line x1={x} y1={(y as number)-2} x2={x} y2={(y as number)+2} stroke="#B0FFD0" strokeWidth="0.4" opacity="0.5" />
          </g>
        ))}
      </>}
      {c.id === "cape_sunset" && <>
        {/* Sky gradient layers */}
        <rect x={cx-28} y={60} width="56" height="30" fill="#FF6030" opacity="0.06" />
        <rect x={cx-28} y={45} width="56" height="20" fill="#FFA050" opacity="0.04" />
        {/* Sun */}
        <circle cx={cx} cy={28} r="16" fill="#FFD060" opacity="0.12" filter={`url(#blur-${c.id})`} style={{animation:"pv-glow 3s ease-in-out infinite"}} />
        <circle cx={cx} cy={28} r="10" fill="#FFE080" opacity="0.2" style={{animation:"pv-pulse 3s ease-in-out infinite"}} />
        <circle cx={cx} cy={28} r="6" fill="#FFF0B0" opacity="0.35" />
        <circle cx={cx} cy={28} r="3" fill="#FFFFD0" opacity="0.5" />
        {/* Sun rays */}
        {[0,30,60,90,120,150,180,210,240,270,300,330].map(a => (
          <line key={a} x1={cx+Math.cos(a*Math.PI/180)*9} y1={28+Math.sin(a*Math.PI/180)*9}
            x2={cx+Math.cos(a*Math.PI/180)*20} y2={28+Math.sin(a*Math.PI/180)*20}
            stroke="#FFD060" strokeWidth={a%60===0?"0.8":"0.4"} opacity={0.15+Math.sin(a*Math.PI/60)*0.05}
            style={{animation:`pv-glow ${2+a/200}s ease-in-out infinite`}} />
        ))}
        {/* Horizon line */}
        <line x1={cx-28} y1={62} x2={cx+28} y2={62} stroke="#FF8040" strokeWidth="0.5" opacity="0.2" />
        {/* Layered clouds */}
        {[[cx-20,52,12,4],[cx+8,48,14,4.5],[cx-5,56,10,3.5],[cx+15,55,8,3],[cx-14,60,11,3]].map(([x,y,rx,ry],i) => (
          <ellipse key={i} cx={x as number} cy={y as number} rx={rx as number} ry={ry as number}
            fill={i%2===0?"#FFB080":"#FFC8A0"} opacity={0.1+i*0.02}
            style={{animation:`pv-drift ${4+i*0.5}s ease-in-out ${i*0.4}s infinite`}} />
        ))}
        {/* Water reflection */}
        {[68,74,80].map((y,i) => (
          <line key={i} x1={cx-15+i*3} y1={y} x2={cx+15-i*3} y2={y}
            stroke="#FFD080" strokeWidth="0.5" opacity={0.1-i*0.02} />
        ))}
        {/* Distant birds */}
        {[[cx-14,35],[cx+10,32],[cx-6,38]].map(([x,y],i) => (
          <path key={i} d={`M${(x as number)-2} ${y} Q${x} ${(y as number)-1.5} ${(x as number)+2} ${y}`}
            fill="none" stroke="#553020" strokeWidth="0.5" opacity="0.15" />
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
        {/* Ethereal glow */}
        <ellipse cx={cx} cy={40} rx="22" ry="30" fill="#C0C0FF" opacity="0.06" filter={`url(#blur-${c.id})`} style={{animation:"pv-pulse 4s ease-in-out infinite"}} />
        {/* Spectral wisps — layered */}
        {[[cx-10,20,5,14],[cx+8,35,7,16],[cx-5,55,6,13],[cx+12,70,5,12],[cx-8,82,4,10],[cx+3,15,3,8]].map(([x,y,rx,ry],i) => (
          <ellipse key={i} cx={x as number} cy={y as number} rx={rx as number} ry={ry as number} fill="#E0E0FF" opacity={0.08+i*0.02}
            style={{animation:`pv-float ${3+i*0.5}s ease-in-out ${i*0.35}s infinite`}} />
        ))}
        {/* Wisp trails */}
        {[[cx-12,18,cx-6,40],[cx+10,30,cx+4,55],[cx-3,50,cx+6,72],[cx+8,65,cx-2,85]].map(([x1,y1,x2,y2],i) => (
          <path key={i} d={`M${x1} ${y1} Q${(x1 as number)+(x2 as number)-(x1 as number)*1.5} ${((y1 as number)+(y2 as number))/2} ${x2} ${y2}`}
            fill="none" stroke="#D0D0FF" strokeWidth="0.5" opacity={0.1+i*0.02}
            style={{animation:`pv-drift ${4+i*0.4}s ease-in-out ${i*0.3}s infinite`}} />
        ))}
        {/* Ghost faces — detailed */}
        {[[cx-6,32],[cx+10,60],[cx-2,80]].map(([x,y],i) => (
          <g key={i} opacity={0.18+i*0.02} style={{animation:`pv-float ${4+i*0.6}s ease-in-out ${i*0.5}s infinite`,transformOrigin:`${x}px ${y}px`}}>
            <circle cx={x} cy={y} r="6" fill="none" stroke="#D0D0FF" strokeWidth="0.4" />
            <circle cx={(x as number)-2.5} cy={(y as number)-1} r="1" fill="#C0C0FF" />
            <circle cx={(x as number)+2.5} cy={(y as number)-1} r="1" fill="#C0C0FF" />
            <path d={`M${(x as number)-1.5} ${(y as number)+2} Q${x} ${(y as number)+3.5} ${(x as number)+1.5} ${(y as number)+2}`}
              fill="none" stroke="#B0B0EE" strokeWidth="0.4" opacity="0.6" />
          </g>
        ))}
        {/* Floating orbs */}
        {[[cx-15,28],[cx+15,45],[cx-12,65],[cx+14,78],[cx,42],[cx-8,50]].map(([x,y],i) => (
          <circle key={i} cx={x} cy={y} r={0.6+i*0.15} fill="#E8E8FF" opacity={0.2+i*0.04}
            style={{animation:`pv-twinkle ${2+i*0.3}s ease-in-out ${i*0.2}s infinite`}} />
        ))}
      </>}
      {c.id === "cape_neon" && <>
        {/* Background glow zones */}
        <ellipse cx={cx-8} cy={35} rx="18" ry="20" fill="#FF32C8" opacity="0.08" filter={`url(#blur-${c.id})`} />
        <ellipse cx={cx+10} cy={65} rx="16" ry="18" fill="#00FFFF" opacity="0.06" filter={`url(#blur-${c.id})`} />
        {/* Perspective grid — horizontal */}
        {[18,28,38,48,58,68,78,85].map((y,i) => (
          <line key={`h${i}`} x1={cx-26} y1={y} x2={cx+26} y2={y} stroke="#FF32C8" strokeWidth={0.4+i*0.05} opacity={0.12+i*0.025}
            style={{animation:`pv-shimmer ${3+i*0.2}s ease-in-out ${i*0.1}s infinite`}} />
        ))}
        {/* Vertical lines */}
        {[cx-22,cx-14,cx-6,cx+2,cx+10,cx+18].map((x,i) => (
          <line key={`v${i}`} x1={x} y1={10} x2={x} y2={88} stroke="#00FFFF" strokeWidth="0.4" opacity={0.1+i*0.02} />
        ))}
        {/* Glitch blocks */}
        {[[cx-14,28,10,3,"#FF40D0"],[cx+3,42,12,2.5,"#00EEFF"],[cx-10,60,8,3,"#FF60E0"],[cx+8,72,9,2,"#00DDFF"],[cx-6,82,14,2,"#FF50D8"]].map(([x,y,w,h,col],i) => (
          <rect key={i} x={x as number} y={y as number} width={w as number} height={h as number} fill={col as string} opacity={0.18+i*0.03}
            style={{animation:`pv-shimmer ${0.8+i*0.25}s ease-in-out ${i*0.15}s infinite`}} />
        ))}
        {/* Neon text-like symbols */}
        {[[cx-8,35],[cx+6,55]].map(([x,y],i) => (
          <g key={i} opacity={0.35} style={{animation:`pv-pulse ${2+i*0.5}s ease-in-out ${i*0.3}s infinite`}}>
            <rect x={(x as number)-5} y={y as number} width="10" height="0.8" fill={i%2===0?"#FF32C8":"#00FFFF"} />
            <rect x={(x as number)-3} y={(y as number)+2} width="6" height="0.8" fill={i%2===0?"#00FFFF":"#FF32C8"} />
          </g>
        ))}
        {/* Scanline effect */}
        {Array.from({length:12}).map((_,i) => (
          <line key={i} x1={cx-26} y1={12+i*6.5} x2={cx+26} y2={12+i*6.5}
            stroke="#fff" strokeWidth="0.2" opacity="0.04" />
        ))}
        {/* Pixel scatter */}
        {[[cx-18,20],[cx+16,30],[cx-16,50],[cx+18,70],[cx-12,85],[cx+12,15]].map(([x,y],i) => (
          <rect key={i} x={(x as number)-1} y={(y as number)-1} width="2" height="2"
            fill={i%2===0?"#FF32C8":"#00FFFF"} opacity={0.25+i*0.04}
            style={{animation:`pv-twinkle ${1.5+i*0.2}s ease-in-out ${i*0.15}s infinite`}} />
        ))}
      </>}
      {c.id === "cape_lava" && <>
        {/* Deep magma glow */}
        <ellipse cx={cx} cy={80} rx="28" ry="12" fill="#FF3000" opacity="0.12" filter={`url(#blur-${c.id})`} style={{animation:"pv-glow 3s ease-in-out infinite"}} />
        <ellipse cx={cx-5} cy={40} rx="18" ry="20" fill="#FF6020" opacity="0.06" filter={`url(#blur-${c.id})`} />
        {/* Lava crack network */}
        <path d={`M${cx-15} 18 L${cx-8} 30 L${cx+5} 28 L${cx+12} 38 L${cx+8} 50`}
          fill="none" stroke="#FF8020" strokeWidth="1.2" opacity="0.4" style={{animation:"pv-glow 2.5s ease-in-out infinite"}} />
        <path d={`M${cx-8} 30 L${cx-12} 45 L${cx-5} 58 L${cx+4} 65 L${cx} 78`}
          fill="none" stroke="#FFA040" strokeWidth="1" opacity="0.35" style={{animation:"pv-glow 3s ease-in-out 0.5s infinite"}} />
        <path d={`M${cx+5} 28 L${cx+16} 42 L${cx+10} 60 L${cx+14} 75`}
          fill="none" stroke="#FF6010" strokeWidth="0.8" opacity="0.3" style={{animation:"pv-glow 2.8s ease-in-out 0.3s infinite"}} />
        {/* Branch cracks */}
        {[[cx-12,45,cx-18,52],[cx+4,65,cx+14,68],[cx-5,58,cx-14,62],[cx+10,60,cx+18,55]].map(([x1,y1,x2,y2],i) => (
          <line key={i} x1={x1} y1={y1} x2={x2} y2={y2} stroke="#FF9030" strokeWidth="0.6" opacity={0.25+i*0.04} />
        ))}
        {/* Molten pools */}
        {[[cx-10,32,5,3],[cx+8,48,6,3.5],[cx-3,68,7,4],[cx+12,40,4,2.5]].map(([x,y,rx,ry],i) => (
          <ellipse key={i} cx={x as number} cy={y as number} rx={rx as number} ry={ry as number}
            fill="#FF6010" opacity={0.18+i*0.04}
            style={{animation:`pv-pulse ${1.5+i*0.2}s ease-in-out ${i*0.2}s infinite`}} />
        ))}
        {/* Rising heat embers */}
        {[[cx-6,20,1.5],[cx+10,15,1.2],[cx-14,30,1],[cx+6,12,0.8],[cx,25,1.3],[cx+16,22,0.7],[cx-10,18,0.9]].map(([x,y,r],i) => (
          <circle key={i} cx={x as number} cy={y as number} r={r as number}
            fill={i%2===0?"#FFD060":"#FFA030"} opacity={0.2+i*0.03}
            style={{animation:`pv-float ${2.5+i*0.3}s ease-in-out ${i*0.2}s infinite`,transformOrigin:`${x}px ${y}px`}} />
        ))}
        {/* Surface texture */}
        {Array.from({length:6}).map((_,i) => (
          <line key={i} x1={cx-20+i*3} y1={75+Math.sin(i)*4} x2={cx-18+i*3} y2={82+Math.cos(i)*3}
            stroke="#401008" strokeWidth="0.4" opacity="0.15" />
        ))}
      </>}
      {c.id === "cape_sakura" && <>
        {/* Soft pink glow */}
        <ellipse cx={cx} cy={40} rx="25" ry="30" fill="#FF90B0" opacity="0.06" filter={`url(#blur-${c.id})`} />
        {/* Main branch */}
        <path d={`M${cx-22} 48 Q${cx-10} 42 ${cx} 38 Q${cx+10} 34 ${cx+20} 28`}
          fill="none" stroke="#644028" strokeWidth="1.8" opacity="0.45" />
        {/* Sub branches */}
        <path d={`M${cx-10} 42 Q${cx-14} 55 ${cx-16} 65`} fill="none" stroke="#644028" strokeWidth="1" opacity="0.3" />
        <path d={`M${cx+5} 36 Q${cx+10} 48 ${cx+8} 60`} fill="none" stroke="#644028" strokeWidth="1" opacity="0.3" />
        <path d={`M${cx-5} 40 Q${cx-8} 32 ${cx-12} 25`} fill="none" stroke="#644028" strokeWidth="0.8" opacity="0.25" />
        {/* Full flowers — detailed 5-petal */}
        {[[cx-14,30],[cx+10,24],[cx-8,50],[cx+12,55],[cx-14,68],[cx+5,72],[cx,38],[cx-6,82],[cx+16,42]].map(([x,y],i) => (
          <g key={i} style={{animation:`pv-float ${2.5+i*0.3}s ease-in-out ${i*0.2}s infinite`,transformOrigin:`${x}px ${y}px`}}>
            {[0,72,144,216,288].map((a,j) => (
              <ellipse key={j} cx={x} cy={(y as number)-3.5} rx="1.8" ry="4" fill={i%3===0?"#FFD8E6":i%3===1?"#FFCCD8":"#FFE0EC"} opacity={0.5+i*0.03}
                transform={`rotate(${a} ${x} ${y})`} />
            ))}
            <circle cx={x} cy={y} r="2" fill="#FFF0F4" opacity="0.8" />
            <circle cx={x} cy={y} r="0.8" fill="#FFD0DD" opacity="0.9" />
          </g>
        ))}
        {/* Falling petals — scattered */}
        {Array.from({length:10}).map((_,i) => (
          <ellipse key={i} cx={cx-18+i*4+Math.sin(i*1.5)*5} cy={15+i*7+Math.cos(i)*4} rx="2.2" ry="1.2"
            fill={i%2===0?"#FFE0EC":"#FFCCD8"} opacity={0.25+i*0.03}
            transform={`rotate(${20+i*30} ${cx-18+i*4+Math.sin(i*1.5)*5} ${15+i*7+Math.cos(i)*4})`}
            style={{animation:`pv-drift ${3+i*0.3}s ease-in-out ${i*0.25}s infinite`}} />
        ))}
        {/* Soft bokeh */}
        {[[cx-20,20],[cx+18,65],[cx-16,78],[cx+20,18]].map(([x,y],i) => (
          <circle key={i} cx={x} cy={y} r="3" fill="#FFB0D0" opacity="0.06"
            style={{animation:`pv-pulse ${3+i*0.4}s ease-in-out ${i*0.3}s infinite`}} />
        ))}
      </>}
      {c.id === "cape_storm" && <>
        {/* Storm sky background */}
        <ellipse cx={cx} cy={18} rx="30" ry="14" fill="#0A0C14" opacity="0.15" filter={`url(#blur-${c.id})`} />
        {/* Layered dark clouds */}
        {[[cx-14,14,14,5.5],[cx+6,12,16,6],[cx-2,18,12,5],[cx-18,20,10,4],[cx+14,22,11,4.5]].map(([x,y,rx,ry],i) => (
          <ellipse key={i} cx={x as number} cy={y as number} rx={rx as number} ry={ry as number}
            fill="#1A1C26" opacity={0.25+i*0.04} />
        ))}
        {/* Main lightning bolt — detailed zigzag */}
        <path d={`M${cx+2} 26 L${cx-2} 36 L${cx+3} 38 L${cx-4} 52 L${cx+1} 54 L${cx-5} 68`}
          fill="none" stroke="#FFFFD8" strokeWidth="1.8" opacity="0.8"
          style={{animation:"pv-shimmer 2s ease-in-out infinite"}} />
        {/* Lightning glow */}
        <path d={`M${cx+2} 26 L${cx-2} 36 L${cx+3} 38 L${cx-4} 52 L${cx+1} 54 L${cx-5} 68`}
          fill="none" stroke="#FFFFAA" strokeWidth="4" opacity="0.1" filter={`url(#blur-${c.id})`}
          style={{animation:"pv-shimmer 2s ease-in-out infinite"}} />
        {/* Branch lightning */}
        <path d={`M${cx-2} 36 L${cx-10} 42 L${cx-14} 48`} fill="none" stroke="#FFFFD8" strokeWidth="0.8" opacity="0.5"
          style={{animation:"pv-shimmer 2s ease-in-out 0.1s infinite"}} />
        <path d={`M${cx+1} 54 L${cx+8} 58 L${cx+12} 64`} fill="none" stroke="#FFFFD8" strokeWidth="0.6" opacity="0.4"
          style={{animation:"pv-shimmer 2s ease-in-out 0.2s infinite"}} />
        {/* Rain streaks — many and varied */}
        {Array.from({length:14}).map((_,i) => (
          <line key={i} x1={cx-24+i*4+Math.sin(i)*2} y1={30+Math.sin(i*1.5)*5}
            x2={cx-25+i*4+Math.sin(i)*2} y2={38+Math.sin(i*1.5)*5+i*0.5}
            stroke="#8098B0" strokeWidth={0.4+i*0.02} opacity={0.12+i*0.015}
            style={{animation:`pv-drift ${1+i*0.1}s linear ${i*0.08}s infinite`}} />
        ))}
        {/* Ground puddle reflections */}
        {[[cx-12,82],[cx+8,85],[cx,80]].map(([x,y],i) => (
          <ellipse key={i} cx={x} cy={y} rx="6" ry="2" fill="#A0B8D0" opacity={0.06+i*0.02}
            style={{animation:`pv-shimmer ${2+i*0.3}s ease-in-out ${i*0.2}s infinite`}} />
        ))}
        {/* Distant flash */}
        <circle cx={cx+15} cy={15} r="8" fill="#FFFFD0" opacity="0.04" filter={`url(#blur-${c.id})`}
          style={{animation:"pv-shimmer 3s ease-in-out 1s infinite"}} />
      </>}
      {c.id === "cape_solar" && <>
        {/* Outer corona haze */}
        <circle cx={cx} cy={35} r="30" fill="#FFD060" opacity="0.04" filter={`url(#blur-${c.id})`} style={{animation:"pv-pulse 4s ease-in-out infinite"}} />
        {/* Corona rings */}
        <circle cx={cx} cy={35} r="22" fill="#FFE060" opacity="0.06" filter={`url(#blur-${c.id})`} style={{animation:"pv-pulse 3s ease-in-out infinite"}} />
        <circle cx={cx} cy={35} r="15" fill="#FFCC40" opacity="0.15" />
        <circle cx={cx} cy={35} r="10" fill="#FFE080" opacity="0.3" />
        <circle cx={cx} cy={35} r="6" fill="#FFF0A0" opacity="0.5" />
        <circle cx={cx} cy={35} r="3" fill="#FFFFC0" opacity="0.7" />
        {/* Sun rays — varying lengths */}
        {Array.from({length:24}).map((_,i) => {
          const a = i * 15 * Math.PI / 180;
          const len = i%3===0 ? 26 : i%3===1 ? 20 : 16;
          return <line key={i} x1={cx+Math.cos(a)*10} y1={35+Math.sin(a)*10}
            x2={cx+Math.cos(a)*len} y2={35+Math.sin(a)*len}
            stroke="#FFD060" strokeWidth={i%3===0?"0.8":"0.4"} opacity={0.15+Math.sin(i)*0.05}
            style={{animation:`pv-glow ${2+i*0.15}s ease-in-out ${i*0.1}s infinite`}} />;
        })}
        {/* Solar flares */}
        <path d={`M${cx-12} 55 Q${cx-18} 42 ${cx-14} 35`} fill="none" stroke="#FFA030" strokeWidth="1" opacity="0.2"
          style={{animation:"pv-pulse 3s ease-in-out infinite"}} />
        <path d={`M${cx+12} 55 Q${cx+20} 44 ${cx+16} 35`} fill="none" stroke="#FFA030" strokeWidth="0.8" opacity="0.15"
          style={{animation:"pv-pulse 3s ease-in-out 0.5s infinite"}} />
        {/* Solar wind particles */}
        {[[cx-18,60],[cx+16,58],[cx-10,72],[cx+12,70],[cx-4,78],[cx+6,82],[cx-16,80],[cx+18,75]].map(([x,y],i) => (
          <circle key={i} cx={x} cy={y} r={0.5+i*0.1} fill="#FFD080" opacity={0.15+i*0.02}
            style={{animation:`pv-drift ${3+i*0.3}s ease-in-out ${i*0.2}s infinite`}} />
        ))}
        {/* Sunspot */}
        <circle cx={cx-2} cy={33} r="1.5" fill="#CC9020" opacity="0.3" />
        <circle cx={cx+3} cy={37} r="1" fill="#CC9020" opacity="0.25" />
      </>}
      {c.id === "cape_amethyst" && <>
        {/* Deep purple ambient */}
        <ellipse cx={cx} cy={45} rx="24" ry="35" fill="#6020A0" opacity="0.08" filter={`url(#blur-${c.id})`} style={{animation:"pv-glow 4s ease-in-out infinite"}} />
        <ellipse cx={cx+8} cy={30} rx="12" ry="15" fill="#A050E0" opacity="0.06" filter={`url(#blur-${c.id})`} />
        {/* Crystal cluster — varied sizes and angles */}
        {[[cx-12,28,14,4,-8],[cx+6,22,16,5,12],[cx-4,42,12,3.5,-5],[cx+12,50,13,4,8],[cx-8,62,11,3.5,-10],[cx+4,72,10,3,5],[cx-14,48,8,2.5,-15],[cx+14,38,9,3,18]].map(([x,y,h,w,rot],i) => (
          <g key={i} style={{animation:`pv-shimmer ${2+i*0.3}s ease-in-out ${i*0.2}s infinite`}}>
            <polygon points={`${x},${(y as number)-(h as number)} ${(x as number)+(w as number)},${y} ${(x as number)-(w as number)},${y}`}
              fill={i%3===0?"#8840C0":i%3===1?"#9950D0":"#7730B0"} opacity={0.25+i*0.03}
              transform={`rotate(${rot} ${x} ${(y as number)-(h as number)/2})`} />
            {/* Inner facet highlight */}
            <polygon points={`${x},${(y as number)-(h as number)} ${(x as number)+(w as number)*0.4},${(y as number)-(h as number)*0.3} ${(x as number)-(w as number)*0.2},${(y as number)-(h as number)*0.3}`}
              fill="#C080FF" opacity={0.15}
              transform={`rotate(${rot} ${x} ${(y as number)-(h as number)/2})`} />
          </g>
        ))}
        {/* Crystal tips sparkle */}
        {[[cx-12,14],[cx+6,6],[cx-4,30],[cx+12,37],[cx-8,51],[cx+4,62]].map(([x,y],i) => (
          <g key={i} style={{animation:`pv-twinkle ${1.5+i*0.25}s ease-in-out ${i*0.15}s infinite`}}>
            <line x1={(x as number)-2.5} y1={y} x2={(x as number)+2.5} y2={y} stroke="#DDB0FF" strokeWidth="0.4" opacity="0.6" />
            <line x1={x} y1={(y as number)-2.5} x2={x} y2={(y as number)+2.5} stroke="#DDB0FF" strokeWidth="0.4" opacity="0.6" />
            <circle cx={x} cy={y} r="0.6" fill="#fff" opacity="0.7" />
          </g>
        ))}
        {/* Ambient particles */}
        {[[cx-18,20],[cx+18,28],[cx-16,55],[cx+16,68],[cx,85],[cx-6,15]].map(([x,y],i) => (
          <circle key={i} cx={x} cy={y} r="0.8" fill="#C890FF" opacity={0.2+i*0.03}
            style={{animation:`pv-float ${3+i*0.4}s ease-in-out ${i*0.25}s infinite`,transformOrigin:`${x}px ${y}px`}} />
        ))}
      </>}
      {c.id === "cape_inferno" && <>
        {/* Hellfire ambient */}
        <ellipse cx={cx} cy={85} rx="30" ry="14" fill="#FF1500" opacity="0.15" filter={`url(#blur-${c.id})`} style={{animation:"pv-glow 2s ease-in-out infinite"}} />
        <ellipse cx={cx} cy={50} rx="20" ry="25" fill="#FF3000" opacity="0.05" filter={`url(#blur-${c.id})`} />
        {/* Multi-layer flames */}
        {[0,1,2,3,4,5,6,7,8].map(i => {
          const fx = cx - 22 + i * 5.5;
          const fh = 20 + Math.sin(i*1.6)*12;
          const fw = 4.5 + (i%2)*2;
          return <g key={i}>
            {/* Outer flame */}
            <path d={`M${fx} 90 Q${fx+fw/3} ${90-fh*0.6} ${fx+fw/2} ${90-fh} Q${fx+fw*0.7} ${90-fh*0.6} ${fx+fw} 90`}
              fill={i%3===0?"#FF2008":i%3===1?"#CC0800":"#AA0500"}
              style={{animation:`pv-flicker ${0.5+i*0.1}s ease-in-out ${i*0.06}s infinite`,transformOrigin:`${fx+fw/2}px 90px`}} />
            {/* Inner bright core */}
            <path d={`M${fx+fw*0.3} 90 Q${fx+fw*0.4} ${90-fh*0.35} ${fx+fw/2} ${90-fh*0.5} Q${fx+fw*0.6} ${90-fh*0.35} ${fx+fw*0.7} 90`}
              fill={i%2===0?"#FF6030":"#FF4020"} opacity="0.5"
              style={{animation:`pv-flicker ${0.4+i*0.08}s ease-in-out ${i*0.05}s infinite`,transformOrigin:`${fx+fw/2}px 90px`}} />
          </g>;
        })}
        {/* Skull — detailed */}
        <g opacity="0.5">
          <ellipse cx={cx} cy={38} rx="7" ry="8" fill="#300808" />
          <ellipse cx={cx} cy={35} rx="6.5" ry="6" fill="#401010" />
          {/* Eyes */}
          <ellipse cx={cx-2.5} cy={35} rx="1.5" ry="2" fill="#FF4400" style={{animation:"pv-pulse 2s ease-in-out infinite"}} />
          <ellipse cx={cx+2.5} cy={35} rx="1.5" ry="2" fill="#FF4400" style={{animation:"pv-pulse 2s ease-in-out 0.3s infinite"}} />
          {/* Nose */}
          <path d={`M${cx-0.8} 38 L${cx} 37 L${cx+0.8} 38`} fill="none" stroke="#FF3300" strokeWidth="0.4" opacity="0.6" />
          {/* Teeth */}
          {[-3,-1,1,3].map((dx,i) => (
            <rect key={i} x={cx+dx-0.4} y={40} width="0.8" height="1.5" fill="#501010" opacity="0.5" />
          ))}
        </g>
        {/* Rising embers */}
        {[[cx-10,20,1.8],[cx+12,15,1.5],[cx-15,28,1.2],[cx+8,10,1],[cx,22,1.4],[cx+18,25,0.8],[cx-6,12,1.1],[cx+4,18,0.9]].map(([x,y,r],i) => (
          <circle key={i} cx={x as number} cy={y as number} r={r as number}
            fill={i%3===0?"#FF8040":i%3===1?"#FFB060":"#FF6030"} opacity={0.2+i*0.025}
            style={{animation:`pv-float ${2+i*0.25}s ease-in-out ${i*0.15}s infinite`,transformOrigin:`${x}px ${y}px`}} />
        ))}
      </>}
      {c.id === "cape_drift" && <>
        {/* Ambient glow */}
        <ellipse cx={cx} cy={50} rx="22" ry="30" fill="#D0B8FF" opacity="0.06" filter={`url(#blur-${c.id})`} />
        {/* Pastel gradient bands — more layers with blur */}
        {[[14,"#FFB8D0",10],[24,"#B8D0FF",9],[34,"#D0B8FF",8],[44,"#FFD0B8",9],[54,"#B8FFD0",8],[64,"#FFE0B8",9],[74,"#C0B8FF",8],[82,"#FFB8E0",7]].map(([y,col,h],i) => (
          <rect key={i} x={cx-26} y={y as number} width="52" height={h as number} fill={col as string} opacity={0.1+i*0.012} rx="3"
            style={{animation:`pv-drift ${3+i*0.25}s ease-in-out ${i*0.25}s infinite`}} />
        ))}
        {/* Diagonal streaks */}
        {Array.from({length:6}).map((_,i) => (
          <line key={i} x1={cx-24+i*10} y1={10} x2={cx-20+i*10} y2={90}
            stroke={["#FFB8D0","#B8D0FF","#D0B8FF","#FFD0B8","#B8FFD0","#FFE0B8"][i]} strokeWidth="0.4" opacity="0.06" />
        ))}
        {/* Vaporwave sun — detailed */}
        <circle cx={cx} cy={82} r="10" fill="#FF78C8" opacity="0.08" />
        <circle cx={cx} cy={82} r="7" fill="#FF90D0" opacity="0.1" />
        {[0,1,2,3,4].map(i => (
          <line key={i} x1={cx-10} y1={78+i*2} x2={cx+10} y2={78+i*2} stroke="#FF78C8" strokeWidth="0.6" opacity={0.12-i*0.02} />
        ))}
        {/* Sparkle constellation */}
        {[[cx-18,18],[cx+14,22],[cx-8,38],[cx+10,42],[cx-14,58],[cx+16,62],[cx-4,75],[cx+6,28],[cx,48]].map(([x,y],i) => (
          <g key={i} style={{animation:`pv-twinkle ${2+i*0.25}s ease-in-out ${i*0.15}s infinite`}}>
            <circle cx={x} cy={y} r="0.7" fill="#fff" opacity={0.3+i*0.03} />
            {i%3===0 && <>
              <line x1={(x as number)-1.5} y1={y} x2={(x as number)+1.5} y2={y} stroke="#fff" strokeWidth="0.3" opacity="0.25" />
              <line x1={x} y1={(y as number)-1.5} x2={x} y2={(y as number)+1.5} stroke="#fff" strokeWidth="0.3" opacity="0.25" />
            </>}
          </g>
        ))}
      </>}
      {c.id === "cape_obsidian" && <>
        {/* Deep purple ambient */}
        <ellipse cx={cx} cy={45} rx="22" ry="35" fill="#2A1040" opacity="0.1" filter={`url(#blur-${c.id})`} />
        {/* Obsidian surface texture — angular facets */}
        {[[cx-20,12,cx-5,30],[cx-5,30,cx+18,15],[cx+18,15,cx+10,45],[cx-20,12,cx-18,48],[cx-18,48,cx-5,30],
          [cx-5,30,cx+10,45],[cx+10,45,cx+5,70],[cx-18,48,cx-8,65],[cx-8,65,cx+5,70],[cx+5,70,cx+18,60],
          [cx-8,65,cx-15,85],[cx-15,85,cx+5,70],[cx+5,70,cx+15,88]].map(([x1,y1,x2,y2],i) => (
          <line key={i} x1={x1} y1={y1} x2={x2} y2={y2} stroke="#1A0830" strokeWidth="0.4" opacity={0.12+i*0.008} />
        ))}
        {/* Purple energy crack network */}
        <path d={`M${cx-16} 18 L${cx-8} 32 L${cx+6} 28 L${cx+14} 40 L${cx+8} 55`}
          fill="none" stroke="#7828B4" strokeWidth="1" opacity="0.4" style={{animation:"pv-glow 3s ease-in-out infinite"}} />
        <path d={`M${cx-8} 32 L${cx-14} 50 L${cx-6} 62 L${cx+4} 70 L${cx} 85`}
          fill="none" stroke="#9040D0" strokeWidth="0.8" opacity="0.3" style={{animation:"pv-glow 3.5s ease-in-out 0.5s infinite"}} />
        <path d={`M${cx+6} 28 L${cx+18} 48 L${cx+10} 68 L${cx+16} 82`}
          fill="none" stroke="#6820A0" strokeWidth="0.6" opacity="0.25" style={{animation:"pv-glow 4s ease-in-out 1s infinite"}} />
        {/* Energy glow at crack junctions */}
        {[[cx-8,32],[cx+6,28],[cx+14,40],[cx-14,50],[cx-6,62],[cx+4,70],[cx+10,68]].map(([x,y],i) => (
          <circle key={i} cx={x} cy={y} r="2.5" fill="#A050E0" opacity={0.1+i*0.02}
            filter={`url(#blur-${c.id})`}
            style={{animation:`pv-shimmer ${2+i*0.3}s ease-in-out ${i*0.2}s infinite`}} />
        ))}
        {/* Gloss sheen lines */}
        {[25,45,65].map((y,i) => (
          <rect key={i} x={cx-22} y={y} width="44" height="1.5" fill="rgba(120,80,160,0.06)"
            transform={`rotate(${-12+i*6} ${cx} ${y})`} />
        ))}
        {/* Floating dark particles */}
        {[[cx-16,22],[cx+16,35],[cx-12,58],[cx+14,75],[cx,15],[cx-6,82]].map(([x,y],i) => (
          <circle key={i} cx={x} cy={y} r="0.6" fill="#C080FF" opacity={0.2+i*0.03}
            style={{animation:`pv-float ${3+i*0.3}s ease-in-out ${i*0.2}s infinite`,transformOrigin:`${x}px ${y}px`}} />
        ))}
      </>}
      {c.id === "cape_blackhole" && <>
        {/* Background gravitational lensing glow */}
        <ellipse cx={cx} cy={45} rx="35" ry="20" fill="#FFA040" opacity="0.04" filter={`url(#blur-${c.id})`} />
        {/* Accretion disk — multiple detailed rings */}
        {[38,34,30,26,22,18].map((r,i) => (
          <ellipse key={i} cx={cx} cy={45} rx={r} ry={r*0.28} fill="none"
            stroke={i<2?"#FF6020":i<4?"#FFA040":"#FFD080"}
            strokeWidth={0.3+i*0.2} opacity={0.06+i*0.06}
            style={{animation:`pv-spin ${25-i*3}s linear infinite`}} />
        ))}
        {/* Bright accretion band */}
        <ellipse cx={cx} cy={45} rx="24" ry="7" fill="none" stroke="#FFD080" strokeWidth="1.2" opacity="0.15"
          style={{animation:"pv-spin 18s linear infinite"}} />
        {/* Event horizon */}
        <circle cx={cx} cy={45} r="9" fill="#000" />
        <circle cx={cx} cy={45} r="10" fill="none" stroke="#FFA040" strokeWidth="0.6" opacity="0.5"
          style={{animation:"pv-pulse 3s ease-in-out infinite"}} />
        {/* Photon ring */}
        <circle cx={cx} cy={45} r="11" fill="none" stroke="#FFD080" strokeWidth="0.3" opacity="0.25"
          style={{animation:"pv-pulse 3s ease-in-out 0.5s infinite"}} />
        {/* Spiraling matter */}
        {[0,1,2,3,4,5,6,7].map(i => {
          const a = i * 0.85 + 0.3;
          const r2 = 13 + i * 3.2;
          return <circle key={i} cx={cx + Math.cos(a)*r2} cy={45 + Math.sin(a)*r2*0.28} r={0.6+i*0.15}
            fill={i<3?"#FFD080":i<6?"#FFA060":"#FF8040"} opacity={0.2+i*0.06}
            style={{animation:`pv-twinkle ${2+i*0.25}s ease-in-out ${i*0.3}s infinite`}} />;
        })}
        {/* Relativistic jets */}
        <path d={`M${cx} 36 Q${cx-2} 22 ${cx-1} 8`} fill="none" stroke="#B0A0FF" strokeWidth="1" opacity="0.2"
          style={{animation:"pv-glow 2.5s ease-in-out infinite"}} />
        <path d={`M${cx} 54 Q${cx+2} 68 ${cx+1} 88`} fill="none" stroke="#B0A0FF" strokeWidth="1" opacity="0.2"
          style={{animation:"pv-glow 2.5s ease-in-out 0.5s infinite"}} />
        {/* Jet glow */}
        <path d={`M${cx} 36 Q${cx-2} 22 ${cx-1} 8`} fill="none" stroke="#C0B0FF" strokeWidth="3" opacity="0.04"
          filter={`url(#blur-${c.id})`} />
        <path d={`M${cx} 54 Q${cx+2} 68 ${cx+1} 88`} fill="none" stroke="#C0B0FF" strokeWidth="3" opacity="0.04"
          filter={`url(#blur-${c.id})`} />
        {/* Background stars being warped */}
        {[[cx-20,18],[cx+18,22],[cx-22,68],[cx+20,72],[cx-16,38],[cx+16,52]].map(([x,y],i) => (
          <circle key={i} cx={x} cy={y} r="0.5" fill="#fff" opacity={0.15+i*0.02}
            style={{animation:`pv-twinkle ${2.5+i*0.3}s ease-in-out ${i*0.2}s infinite`}} />
        ))}
      </>}
      {c.id === "cape_creator" && <>
        {/* Gold ambient */}
        <ellipse cx={cx} cy={45} rx="25" ry="30" fill="#FFD700" opacity="0.06" filter={`url(#blur-${c.id})`} style={{animation:"pv-glow 3s ease-in-out infinite"}} />
        {/* Star burst rays */}
        {Array.from({length:10}).map((_,i) => {
          const a = i * Math.PI * 2 / 10 - Math.PI/2;
          const len = i%2===0 ? 24 : 16;
          return <line key={i} x1={cx+Math.cos(a)*6} y1={45+Math.sin(a)*6} x2={cx+Math.cos(a)*len} y2={45+Math.sin(a)*len}
            stroke="#FFD700" strokeWidth={i%2===0?"1":"0.5"} opacity={i%2===0?0.3:0.18}
            style={{animation:`pv-glow ${2+i*0.15}s ease-in-out ${i*0.15}s infinite`}} />;
        })}
        {/* Central star */}
        <circle cx={cx} cy={45} r="10" fill="#FFD700" opacity="0.15" filter={`url(#blur-${c.id})`} style={{animation:"pv-pulse 2s ease-in-out infinite"}} />
        <circle cx={cx} cy={45} r="6" fill="#FFD700" opacity="0.3" />
        <circle cx={cx} cy={45} r="3" fill="#FFE860" opacity="0.6" />
        <circle cx={cx} cy={45} r="1.5" fill="#FFFFF0" opacity="0.8" />
        {/* 5-pointed star shape */}
        {[0,1,2,3,4].map(i => {
          const outer = i * Math.PI * 2 / 5 - Math.PI/2;
          const inner = (i + 0.5) * Math.PI * 2 / 5 - Math.PI/2;
          return <g key={i}>
            <line x1={cx+Math.cos(outer)*12} y1={45+Math.sin(outer)*12}
              x2={cx+Math.cos(inner)*5} y2={45+Math.sin(inner)*5}
              stroke="#FFE060" strokeWidth="0.5" opacity="0.35" />
            <line x1={cx+Math.cos(inner)*5} y1={45+Math.sin(inner)*5}
              x2={cx+Math.cos((i+1)*Math.PI*2/5-Math.PI/2)*12} y2={45+Math.sin((i+1)*Math.PI*2/5-Math.PI/2)*12}
              stroke="#FFE060" strokeWidth="0.5" opacity="0.35" />
          </g>;
        })}
        {/* Sparkles with cross glints */}
        {[[cx-18,20],[cx+16,25],[cx-14,40],[cx+18,55],[cx-16,68],[cx+14,78],[cx,15],[cx,82]].map(([x,y],i) => (
          <g key={i} style={{animation:`pv-twinkle ${1.5+i*0.2}s ease-in-out ${i*0.15}s infinite`}}>
            <circle cx={x} cy={y} r="1" fill="#FFD700" opacity={0.35+i*0.03} />
            <line x1={(x as number)-2} y1={y} x2={(x as number)+2} y2={y} stroke="#FFD700" strokeWidth="0.3" opacity="0.3" />
            <line x1={x} y1={(y as number)-2} x2={x} y2={(y as number)+2} stroke="#FFD700" strokeWidth="0.3" opacity="0.3" />
          </g>
        ))}
      </>}
      {c.id === "cape_youtube" && <>
        {/* Red ambient glow */}
        <ellipse cx={cx} cy={45} rx="22" ry="18" fill="#FF0000" opacity="0.06" filter={`url(#blur-${c.id})`} style={{animation:"pv-glow 3s ease-in-out infinite"}} />
        {/* Play button — detailed with shadow */}
        <rect x={cx-16} y={33} width="32" height="22" rx="5" fill="#CC0000" opacity="0.15" />
        <rect x={cx-15} y={32} width="30" height="21" rx="5" fill="#FF0000" opacity="0.85" />
        <rect x={cx-15} y={32} width="30" height="10" rx="5" fill="#FF2020" opacity="0.2" />
        <polygon points={`${cx-4},38 ${cx-4},49 ${cx+7},43.5`} fill="#fff" opacity="0.95" />
        {/* Glow rings */}
        <rect x={cx-18} y={30} width="36" height="28" rx="7" fill="none" stroke="#FF0000" strokeWidth="0.5" opacity="0.15"
          style={{animation:"pv-pulse 3s ease-in-out infinite"}} />
        <rect x={cx-21} y={28} width="42" height="32" rx="9" fill="none" stroke="#FF0000" strokeWidth="0.3" opacity="0.08"
          style={{animation:"pv-pulse 3s ease-in-out 0.3s infinite"}} />
        {/* Video reel bars — top and bottom */}
        {[14,18,22,66,70,74].map((y,i) => (
          <rect key={i} x={cx-20+i*2} y={y} width="40-i*4" height="1.2" fill="#fff" opacity={0.04+i*0.005} rx="0.5" />
        ))}
        {/* Subscriber count effect dots */}
        {[[cx-18,20],[cx+16,22],[cx-14,72],[cx+18,68],[cx-20,45],[cx+20,42]].map(([x,y],i) => (
          <circle key={i} cx={x} cy={y} r="0.8" fill={i%2===0?"#FF4040":"#fff"} opacity={0.15+i*0.02}
            style={{animation:`pv-twinkle ${2+i*0.3}s ease-in-out ${i*0.2}s infinite`}} />
        ))}
      </>}
      {c.id === "cape_twitch" && <>
        {/* Purple ambient */}
        <ellipse cx={cx} cy={45} rx="22" ry="25" fill="#9146FF" opacity="0.06" filter={`url(#blur-${c.id})`} style={{animation:"pv-glow 3s ease-in-out infinite"}} />
        {/* Twitch icon — detailed with depth */}
        <path d={`M${cx-12},28 L${cx-12},60 L${cx-5},67 L${cx+3},67 L${cx+10},60 L${cx+10},28 Z`}
          fill="#7028CC" opacity="0.2" />
        <path d={`M${cx-11},27 L${cx-11},59 L${cx-4},66 L${cx+4},66 L${cx+11},59 L${cx+11},27 Z`}
          fill="#9146FF" opacity="0.55" stroke="#A060FF" strokeWidth="0.4" />
        {/* Icon sheen */}
        <path d={`M${cx-11},27 L${cx+11},27 L${cx+11},38 L${cx-11},42 Z`}
          fill="#B080FF" opacity="0.1" />
        {/* Eyes — glowing */}
        <rect x={cx-6} y={38} width="3.5" height="12" fill="#fff" opacity="0.9" rx="0.5" />
        <rect x={cx+3} y={38} width="3.5" height="12" fill="#fff" opacity="0.9" rx="0.5" />
        <rect x={cx-6} y={38} width="3.5" height="12" fill="#fff" opacity="0.15" rx="0.5" filter={`url(#blur-${c.id})`} />
        <rect x={cx+3} y={38} width="3.5" height="12" fill="#fff" opacity="0.15" rx="0.5" filter={`url(#blur-${c.id})`} />
        {/* Chat bubbles — floating with detail */}
        {[[cx-18,22,4.5],[cx+16,32,3.5],[cx-14,72,4],[cx+18,75,3],[cx-20,50,3]].map(([x,y,r],i) => (
          <g key={i} style={{animation:`pv-float ${3+i*0.4}s ease-in-out ${i*0.25}s infinite`,transformOrigin:`${x}px ${y}px`}}>
            <circle cx={x as number} cy={y as number} r={r as number} fill="#9146FF" opacity={0.12+i*0.03} />
            {/* Chat lines inside */}
            <line x1={(x as number)-(r as number)*0.5} y1={(y as number)-0.5} x2={(x as number)+(r as number)*0.5} y2={(y as number)-0.5}
              stroke="#fff" strokeWidth="0.4" opacity="0.3" />
            <line x1={(x as number)-(r as number)*0.4} y1={(y as number)+1} x2={(x as number)+(r as number)*0.3} y2={(y as number)+1}
              stroke="#fff" strokeWidth="0.3" opacity="0.2" />
          </g>
        ))}
        {/* Pulse ring */}
        <path d={`M${cx-14},25 L${cx-14},62 L${cx-6},70 L${cx+6},70 L${cx+14},62 L${cx+14},25 Z`}
          fill="none" stroke="#9146FF" strokeWidth="0.4" opacity="0.12"
          style={{animation:"pv-pulse 3s ease-in-out infinite"}} />
      </>}
      {c.id === "cape_tiktok" && <>
        {/* Dual-color glow */}
        <ellipse cx={cx-6} cy={45} rx="18" ry="25" fill="#00F2EA" opacity="0.05" filter={`url(#blur-${c.id})`} />
        <ellipse cx={cx+6} cy={45} rx="18" ry="25" fill="#FF0050" opacity="0.05" filter={`url(#blur-${c.id})`} />
        {/* Music note — cyan layer */}
        <circle cx={cx-5} cy={56} r="5.5" fill="#00F2EA" opacity="0.55" />
        <rect x={cx-2} y={30} width="2.5" height="26" fill="#00F2EA" opacity="0.55" />
        <path d={`M${cx+0.5} 30 Q${cx+6} 28 ${cx+8} 32`} fill="none" stroke="#00F2EA" strokeWidth="2" opacity="0.55" />
        {/* Music note — red layer (offset for 3D effect) */}
        <circle cx={cx-3} cy={54} r="5.5" fill="#FF0050" opacity="0.35" />
        <rect x={cx} y={28} width="2.5" height="26" fill="#FF0050" opacity="0.35" />
        <path d={`M${cx+2.5} 28 Q${cx+8} 26 ${cx+10} 30`} fill="none" stroke="#FF0050" strokeWidth="2" opacity="0.35" />
        {/* White core note */}
        <circle cx={cx-4} cy={55} r="4" fill="#fff" opacity="0.15" />
        <rect x={cx-1} y={29} width="1.5" height="26" fill="#fff" opacity="0.15" />
        {/* Glitch blocks — scattered */}
        {[[cx-20,18,10,2,"#00F2EA"],[cx+8,24,12,1.8,"#FF0050"],[cx-16,42,8,2,"#FF0050"],[cx+10,52,11,1.5,"#00F2EA"],
          [cx-12,68,9,2,"#00F2EA"],[cx+6,76,13,1.8,"#FF0050"],[cx-18,82,7,1.5,"#FF0050"],[cx+14,85,8,1.5,"#00F2EA"]
        ].map(([x,y,w,h,col],i) => (
          <rect key={i} x={x as number} y={y as number} width={w as number} height={h as number}
            fill={col as string} opacity={0.1+i*0.015} rx="0.5"
            style={{animation:`pv-drift ${1.5+i*0.3}s ease-in-out ${i*0.12}s infinite`}} />
        ))}
        {/* Scanlines */}
        {Array.from({length:8}).map((_,i) => (
          <line key={i} x1={cx-24} y1={14+i*10} x2={cx+24} y2={14+i*10}
            stroke="#fff" strokeWidth="0.2" opacity="0.03" />
        ))}
        {/* Floating music symbols */}
        {[[cx-16,30],[cx+18,40],[cx-18,62],[cx+16,70]].map(([x,y],i) => (
          <g key={i} opacity={0.2} style={{animation:`pv-float ${3+i*0.4}s ease-in-out ${i*0.3}s infinite`,transformOrigin:`${x}px ${y}px`}}>
            <circle cx={x} cy={(y as number)+2} r="2" fill={i%2===0?"#00F2EA":"#FF0050"} />
            <rect x={(x as number)+1} y={(y as number)-4} width="1" height="6" fill={i%2===0?"#00F2EA":"#FF0050"} />
          </g>
        ))}
      </>}
      {c.id === "cape_og" && <>
        {/* Clean premium glow */}
        <ellipse cx={cx} cy={45} rx="20" ry="25" fill="#fff" opacity="0.03" filter={`url(#blur-${c.id})`} style={{animation:"pv-glow 4s ease-in-out infinite"}} />
        {/* Elegant diagonal lines */}
        {Array.from({length:10}).map((_,i) => (
          <line key={i} x1={cx-28+i*7} y1={10} x2={cx-23+i*7} y2={90}
            stroke="#fff" strokeWidth={i%3===0?"0.6":"0.3"} opacity={i%3===0?0.08:0.04} />
        ))}
        {/* Pulsar logo — detailed */}
        <circle cx={cx} cy={45} r="14" fill="none" stroke="#fff" strokeWidth="0.6" opacity="0.15"
          style={{animation:"pv-pulse 4s ease-in-out infinite"}} />
        <circle cx={cx} cy={45} r="12" fill="none" stroke="#fff" strokeWidth="0.8" opacity="0.2" />
        {/* Orbital rings */}
        <ellipse cx={cx} cy={45} rx="18" ry="5.5" fill="none" stroke="#fff" strokeWidth="0.5" opacity="0.15"
          style={{animation:"pv-spin 20s linear infinite"}} />
        <ellipse cx={cx} cy={45} rx="15" ry="8" fill="none" stroke="#fff" strokeWidth="0.3" opacity="0.1"
          transform={`rotate(60 ${cx} 45)`} style={{animation:"pv-spin 25s linear reverse infinite"}} />
        {/* Core */}
        <circle cx={cx} cy={45} r="5" fill="#fff" opacity="0.08" />
        <circle cx={cx} cy={45} r="3" fill="#fff" opacity="0.12" />
        <circle cx={cx} cy={45} r="1.5" fill="#fff" opacity="0.2" />
        {/* Corner markers — premium feel */}
        {[[cx-20,18],[cx+20,18],[cx-22,72],[cx+22,72]].map(([x,y],i) => (
          <g key={i}>
            <circle cx={x} cy={y} r="1.2" fill="#fff" opacity="0.12" />
            <line x1={(x as number)-(i<2?3:-3)} y1={y} x2={x} y2={y} stroke="#fff" strokeWidth="0.3" opacity="0.08" />
            <line x1={x} y1={(y as number)+(i<2?-3:3)} x2={x} y2={y} stroke="#fff" strokeWidth="0.3" opacity="0.08" />
          </g>
        ))}
        {/* "OG" text feel — subtle pattern */}
        <line x1={cx-10} y1={20} x2={cx+10} y2={20} stroke="#fff" strokeWidth="0.3" opacity="0.06" />
        <line x1={cx-10} y1={70} x2={cx+10} y2={70} stroke="#fff" strokeWidth="0.3" opacity="0.06" />
        {/* Particle scatter */}
        {[[cx-16,28],[cx+14,32],[cx-12,58],[cx+16,62],[cx,82],[cx-8,15],[cx+8,80]].map(([x,y],i) => (
          <circle key={i} cx={x} cy={y} r="0.5" fill="#fff" opacity={0.1+i*0.02}
            style={{animation:`pv-twinkle ${2.5+i*0.3}s ease-in-out ${i*0.2}s infinite`}} />
        ))}
      </>}
      {/* Default capes without custom detail */}
      {!["cape_blossom","cape_midnight","cape_frost","cape_flame","cape_ocean","cape_emerald","cape_sunset","cape_galaxy","cape_phantom","cape_neon","cape_lava","cape_sakura","cape_storm","cape_solar","cape_amethyst","cape_inferno","cape_drift","cape_obsidian","cape_blackhole","cape_creator","cape_youtube","cape_twitch","cape_tiktok","cape_og"].includes(c.id) && c.id.startsWith("cape_") && <>
        {/* Generic cape detail — stars and shimmer */}
        {[0,1,2,3,4].map(i => (
          <circle key={i} cx={cx-15+i*8} cy={20+i*14} r="1.5" fill="#fff" opacity={0.15+i*0.04}
            style={{animation:`pv-twinkle ${2+i*0.3}s ease-in-out ${i*0.2}s infinite`}} />
        ))}
        <rect x={cx-20} y={40} width="40" height="1" fill="rgba(255,255,255,0.05)" transform={`rotate(-8 ${cx} 40)`}
          style={{animation:`pv-shimmer 3s ease-in-out infinite`}} />
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

function OGCapeSection({ onClaim, isOwned }: { onClaim: () => void; isOwned: boolean }) {
  const [ogInfo, setOgInfo] = useState<{ count: number; remaining: number } | null>(null);
  const [claiming, setClaiming] = useState(false);
  const [claimed, setClaimed] = useState(isOwned);

  useEffect(() => {
    fetch("https://bloom-launcher.vercel.app/api/claim-og")
      .then(r => r.json())
      .then(setOgInfo)
      .catch(() => setOgInfo({ count: 0, remaining: 100 }));
  }, []);

  async function handleClaim() {
    if (claiming || claimed) return;
    setClaiming(true);
    try {
      const res = await fetch("https://bloom-launcher.vercel.app/api/claim-og", {
        method: "POST", headers: { "Content-Type": "application/json" },
      });
      const data = await res.json();
      if (data.success) {
        onClaim();
        setClaimed(true);
        setOgInfo({ count: data.count, remaining: data.remaining });
      }
    } catch {}
    setClaiming(false);
  }

  const soldOut = ogInfo && ogInfo.remaining <= 0 && !claimed;

  return (
    <div className="pulsar-card" style={{
      padding: "0", overflow: "hidden",
      border: "1px solid rgba(255,255,255,0.12)",
    }}>
      {/* Banner */}
      <div style={{
        height: "60px", background: "linear-gradient(135deg, #FFFFFF, #C0C0C0, #808080)",
        display: "flex", alignItems: "center", justifyContent: "center", position: "relative",
      }}>
        <span style={{ fontSize: "20px", fontWeight: "900", color: "#000", letterSpacing: "0.1em" }}>OG PULSAR</span>
        <div style={{
          position: "absolute", top: "8px", right: "10px", fontSize: "9px", fontWeight: "800",
          background: soldOut ? "rgba(255,50,50,0.9)" : "rgba(0,0,0,0.6)", color: "#fff",
          padding: "3px 10px", borderRadius: "4px", letterSpacing: "0.06em",
        }}>
          {soldOut ? "SOLD OUT" : `${ogInfo?.remaining ?? "?"}/100 LEFT`}
        </div>
      </div>
      {/* Content */}
      <div style={{ padding: "16px 18px", display: "flex", alignItems: "center", justifyContent: "space-between" }}>
        <div>
          <div style={{ fontSize: "14px", fontWeight: "700", color: "#fff", marginBottom: "2px" }}>
            OG Pulsar Cape
          </div>
          <div style={{ fontSize: "11px", color: "var(--text-faint)" }}>
            Limited edition — free for the first 100 players
          </div>
        </div>
        <button onClick={handleClaim} disabled={!!soldOut || claimed || claiming} style={{
          padding: "10px 24px", borderRadius: "8px", border: "none",
          background: claimed ? "rgba(76,175,80,0.12)" : soldOut ? "rgba(255,255,255,0.04)" : "linear-gradient(135deg, #fff, #ccc)",
          color: claimed ? "#4CAF50" : soldOut ? "var(--text-faint)" : "#000",
          fontSize: "12px", fontWeight: "800", cursor: soldOut || claimed ? "default" : "pointer",
          fontFamily: "inherit", transition: "all 0.15s", letterSpacing: "0.04em",
        }}>
          {claimed ? "Claimed ✓" : claiming ? "..." : soldOut ? "Sold Out" : "Claim Free"}
        </button>
      </div>
    </div>
  );
}
