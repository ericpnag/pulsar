import { useState, useEffect } from "react";
import { invoke } from "@tauri-apps/api/core";

interface Settings {
  ram: number;
  javaPath: string;
  javaArgs: string;
  closeOnLaunch: boolean;
  showSnapshots: boolean;
  showBetas: boolean;
  discordRpc: boolean;
  reducedMotion: boolean;
  resolution: string;
  colorblindMode: string;
  streamerMode: boolean;
}

const DEFAULT: Settings = {
  ram: 2048, javaPath: "", javaArgs: "", closeOnLaunch: false,
  showSnapshots: false, showBetas: false, discordRpc: true,
  reducedMotion: false, resolution: "",
  colorblindMode: "none", streamerMode: false,
};

const ACCENT_COLORS = [
  { name: "Violet", hex: "#C4B5FD" },
  { name: "Mint", hex: "#5DCAA5" },
  { name: "Coral", hex: "#F0997B" },
  { name: "Pink", hex: "#F4C0D1" },
  { name: "Sky", hex: "#85B7EB" },
  { name: "Gold", hex: "#FAC775" },
];

const JVM_PRESETS = [
  { label: "Aikar's Flags", args: "-XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions -XX:+DisableExplicitGC -XX:+AlwaysPreTouch -XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=40 -XX:G1HeapRegionSize=8M -XX:G1ReservePercent=20 -XX:G1HeapWastePercent=5 -XX:G1MixedGCCountTarget=4 -XX:InitiatingHeapOccupancyPercent=15 -XX:G1MixedGCLiveThresholdPercent=90 -XX:G1RSetUpdatingPauseTimePercent=5 -XX:SurvivorRatio=32 -XX:+PerfDisableSharedMem -XX:MaxTenuringThreshold=1" },
  { label: "ZGC (Low latency)", args: "-XX:+UseZGC -XX:+ZGenerational" },
  { label: "Default", args: "" },
];

type Section = "general" | "account" | "performance" | "display" | "keybinds" | "java" | "notifications" | "privacy" | "accessibility" | "updates" | "about";

interface NavItem {
  id: Section;
  label: string;
  icon: React.ReactNode;
  badge?: string;
}

interface NavGroup {
  title: string;
  items: NavItem[];
}

/* ── Inline SVG icons ── */
const icons = {
  gear: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 1 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 1 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 1 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 1 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg>,
  user: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>,
  perf: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>,
  display: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><rect x="2" y="3" width="20" height="14" rx="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/></svg>,
  keybind: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><rect x="2" y="6" width="20" height="12" rx="2"/></svg>,
  java: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/></svg>,
  bell: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>,
  shield: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>,
  accessibility: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="12" cy="12" r="10"/><path d="M8 14s1.5 2 4 2 4-2 4-2"/><line x1="9" y1="9" x2="9.01" y2="9"/><line x1="15" y1="9" x2="15.01" y2="9"/></svg>,
  refresh: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/></svg>,
  info: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>,
  search: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>,
};

const NAV_GROUPS: NavGroup[] = [
  {
    title: "General",
    items: [
      { id: "general", label: "General", icon: icons.gear },
      { id: "account", label: "Account", icon: icons.user },
    ],
  },
  {
    title: "Game",
    items: [
      { id: "performance", label: "Performance", icon: icons.perf },
      { id: "display", label: "Display", icon: icons.display },
      { id: "keybinds", label: "Keybinds", icon: icons.keybind },
      { id: "java", label: "Java & RAM", icon: icons.java },
    ],
  },
  {
    title: "Pulsar",
    items: [
      { id: "notifications", label: "Notifications", icon: icons.bell },
      { id: "privacy", label: "Privacy", icon: icons.shield },
      { id: "accessibility", label: "Accessibility", icon: icons.accessibility },
    ],
  },
  {
    title: "About",
    items: [
      { id: "updates", label: "Updates", icon: icons.refresh },
      { id: "about", label: "About Pulsar", icon: icons.info, badge: "v2.1.0" },
    ],
  },
];

/* ── Toggle component ── */
function Toggle({ value, onChange }: { value: boolean; onChange: (v: boolean) => void }) {
  return (
    <div
      onClick={() => onChange(!value)}
      style={{
        width: "32px", height: "18px", borderRadius: "9px", cursor: "pointer",
        background: value ? "#2A1A4D" : "#1F1F2E",
        position: "relative", transition: "all 150ms", flexShrink: 0,
      }}
    >
      <div style={{
        position: "absolute", top: "1.5px", left: value ? "15.5px" : "1.5px",
        width: "15px", height: "15px", borderRadius: "50%",
        background: value ? "#C4B5FD" : "#5A5870",
        transition: "all 150ms",
      }} />
    </div>
  );
}

/* ── Settings row ── */
function SetRow({ label, desc, children, tag, tagWarn }: {
  label: string; desc: string; children: React.ReactNode; tag?: string; tagWarn?: boolean;
}) {
  return (
    <div style={{
      display: "grid", gridTemplateColumns: "1fr auto", gap: "16px", alignItems: "center",
      padding: "14px 18px", borderBottom: "0.5px solid #1A1A28",
    }}>
      <div style={{ minWidth: 0 }}>
        <p style={{
          fontSize: "13px", fontWeight: 500, color: "#F0EEFC", margin: "0 0 2px",
          display: "flex", alignItems: "center", gap: "8px",
        }}>
          {label}
          {tag && (
            <span style={{
              display: "inline-block", fontSize: "9px", padding: "2px 6px",
              background: tagWarn ? "rgba(216,90,48,0.12)" : "rgba(196,181,253,0.12)",
              color: tagWarn ? "#F0997B" : "#C4B5FD",
              borderRadius: "3px", letterSpacing: "0.4px", textTransform: "uppercase", fontWeight: 500,
            }}>{tag}</span>
          )}
        </p>
        <p style={{ fontSize: "11px", color: "#8A88A8", margin: 0, lineHeight: 1.4 }}>{desc}</p>
      </div>
      {children}
    </div>
  );
}

/* ── Settings card wrapper ── */
function Card({ children }: { children: React.ReactNode }) {
  return (
    <div style={{
      background: "#0D0D17", border: "0.5px solid #1F1F2E", borderRadius: "12px", overflow: "hidden",
    }}>
      {children}
    </div>
  );
}

function SectionHeader({ children }: { children: React.ReactNode }) {
  return (
    <p style={{
      fontSize: "10px", color: "#6B6985", letterSpacing: "1.4px",
      textTransform: "uppercase", margin: "0 0 10px", fontWeight: 500,
    }}>{children}</p>
  );
}

/* ── Main export ── */
export function SettingsPage() {
  const [settings, setSettings] = useState<Settings>(DEFAULT);
  const [settingsSection, setSettingsSection] = useState<Section>("general");
  const [searchQuery, setSearchQuery] = useState("");
  const [accentColor, setAccentColor] = useState("#C4B5FD");

  // Read account from localStorage
  const [accountName, setAccountName] = useState("Not signed in");
  useEffect(() => {
    try {
      const acc = localStorage.getItem("pulsar-account");
      if (acc) {
        const parsed = JSON.parse(acc);
        if (parsed?.username) setAccountName(parsed.username);
      }
    } catch { /* ignore */ }
  }, []);

  useEffect(() => {
    const saved = localStorage.getItem("pulsar-settings");
    if (saved) setSettings({ ...DEFAULT, ...JSON.parse(saved) });
  }, []);

  function save(patch: Partial<Settings>) {
    const updated = { ...settings, ...patch };
    setSettings(updated);
    localStorage.setItem("pulsar-settings", JSON.stringify(updated));
  }

  useEffect(() => {
    const root = document.documentElement;
    root.classList.remove("colorblind-deuteranopia", "colorblind-protanopia", "colorblind-tritanopia");
    if (settings.colorblindMode === "deuteranopia") root.classList.add("colorblind-deuteranopia");
    else if (settings.colorblindMode === "protanopia") root.classList.add("colorblind-protanopia");
    else if (settings.colorblindMode === "tritanopia") root.classList.add("colorblind-tritanopia");
  }, [settings.colorblindMode]);

  const ramGB = (settings.ram / 1024).toFixed(1).replace(/\.0$/, "");
  const ramWarning = settings.ram > 8192 ? "High RAM may cause GC pauses. 4-8 GB recommended for most setups." : "";
  const ramPercent = ((settings.ram - 1024) / (16384 - 1024)) * 100;

  /* ── Filtered nav items ── */
  const filteredGroups = searchQuery.trim()
    ? NAV_GROUPS.map(g => ({
        ...g,
        items: g.items.filter(i => i.label.toLowerCase().includes(searchQuery.toLowerCase())),
      })).filter(g => g.items.length > 0)
    : NAV_GROUPS;

  /* ── Section titles ── */
  const sectionMeta: Record<Section, { label: string; title: string; sub: string }> = {
    general: { label: "Settings \u00b7 General", title: "General", sub: "Core launcher behavior, account, and the basics." },
    account: { label: "Settings \u00b7 Account", title: "Account", sub: "Manage your Microsoft account and profile." },
    performance: { label: "Settings \u00b7 Performance", title: "Performance", sub: "RAM, JVM flags, and resolution settings." },
    display: { label: "Settings \u00b7 Display", title: "Display", sub: "Visual settings for Minecraft." },
    keybinds: { label: "Settings \u00b7 Keybinds", title: "Keybinds", sub: "Customize your key bindings." },
    java: { label: "Settings \u00b7 Java & RAM", title: "Java & RAM", sub: "Java path, JVM arguments, and memory allocation." },
    notifications: { label: "Settings \u00b7 Notifications", title: "Notifications", sub: "Configure notification preferences." },
    privacy: { label: "Settings \u00b7 Privacy", title: "Privacy", sub: "Streamer mode, analytics, and data settings." },
    accessibility: { label: "Settings \u00b7 Accessibility", title: "Accessibility", sub: "Colorblind mode, reduced motion, and more." },
    updates: { label: "Settings \u00b7 Updates", title: "Updates", sub: "Version info and update preferences." },
    about: { label: "Settings \u00b7 About", title: "About Pulsar", sub: "Version, source code, and maintenance." },
  };

  // Update checker
  const [updateStatus, setUpdateStatus] = useState<"idle" | "checking" | "up-to-date" | "update-available">("idle");
  const [latestVersion, setLatestVersion] = useState("");
  const [releaseUrl, setReleaseUrl] = useState("");

  async function checkForUpdates() {
    setUpdateStatus("checking");
    try {
      const res = await fetch("https://api.github.com/repos/ericpnag/pulsar/releases/latest");
      const data = await res.json();
      const tag = data.tag_name || "";
      setLatestVersion(tag);
      setReleaseUrl(data.html_url || "https://github.com/ericpnag/pulsar/releases");
      if (tag === "v2.1.0") {
        setUpdateStatus("up-to-date");
      } else {
        setUpdateStatus("update-available");
      }
    } catch {
      setUpdateStatus("idle");
    }
  }

  const meta = sectionMeta[settingsSection];

  /* ── Section content renderers ── */
  function renderGeneral() {
    return (
      <>
        {/* Account card */}
        <div style={{ marginBottom: "26px" }}>
          <SectionHeader>Account</SectionHeader>
          <Card>
            <div style={{ display: "flex", alignItems: "center", gap: "14px", padding: "16px 18px" }}>
              <div style={{
                width: "44px", height: "44px", borderRadius: "10px",
                background: "linear-gradient(135deg, #8B5CF6, #4C1D95)", flexShrink: 0,
              }} />
              <div style={{ flex: 1 }}>
                <p style={{ fontSize: "14px", fontWeight: 500, color: "#F0EEFC", margin: 0 }}>{accountName}</p>
                <p style={{ fontSize: "12px", color: "#8A88A8", margin: "2px 0 0", fontFamily: "monospace" }}>
                  {accountName !== "Not signed in" ? "Microsoft Account" : "Sign in to play online"}
                </p>
              </div>
              <button style={{
                padding: "7px 14px", background: "#11111C", border: "0.5px solid #2A2A3E",
                borderRadius: "7px", color: "#C7C5DC", fontSize: "12px", cursor: "pointer",
              }}>Manage</button>
            </div>
          </Card>
        </div>

        {/* Launcher toggles */}
        <div style={{ marginBottom: "26px" }}>
          <SectionHeader>Launcher</SectionHeader>
          <Card>
            <SetRow label="Close on launch" desc="Close the launcher when Minecraft starts.">
              <Toggle value={settings.closeOnLaunch} onChange={v => save({ closeOnLaunch: v })} />
            </SetRow>
            <SetRow label="Discord Rich Presence" desc="Show your current Pulsar activity in Discord status.">
              <Toggle value={settings.discordRpc} onChange={v => save({ discordRpc: v })} />
            </SetRow>
            <SetRow label="Show Snapshots" desc="Include snapshot versions in the version selector.">
              <Toggle value={settings.showSnapshots} onChange={v => save({ showSnapshots: v })} />
            </SetRow>
            <div style={{ borderBottom: "none" }}>
              <SetRow label="Show Betas & Alphas" desc="Include old beta and alpha versions (no Fabric support).">
                <Toggle value={settings.showBetas} onChange={v => save({ showBetas: v })} />
              </SetRow>
            </div>
          </Card>
        </div>

        {/* Theme accent color */}
        <div style={{ marginBottom: "26px" }}>
          <SectionHeader>Theme</SectionHeader>
          <Card>
            <SetRow label="Theme accent" desc="Color used for highlights, buttons, and active states.">
              <div style={{ display: "flex", gap: "6px", alignItems: "center" }}>
                {ACCENT_COLORS.map(c => (
                  <div
                    key={c.hex}
                    onClick={() => setAccentColor(c.hex)}
                    style={{
                      width: "22px", height: "22px", borderRadius: "6px",
                      background: c.hex, border: "0.5px solid #2A2A3E", cursor: "pointer",
                      boxShadow: accentColor === c.hex ? "0 0 0 1.5px #C4B5FD" : "none",
                    }}
                    title={c.name}
                  />
                ))}
              </div>
            </SetRow>
          </Card>
        </div>
      </>
    );
  }

  function renderPerformance() {
    return (
      <>
        <div style={{ marginBottom: "26px" }}>
          <SectionHeader>Memory</SectionHeader>
          <Card>
            <SetRow label="RAM allocation" desc="Memory dedicated to Minecraft. 4 GB recommended for vanilla." tag="Auto">
              <div style={{ display: "flex", alignItems: "center", gap: "10px", minWidth: "220px" }}>
                <div style={{ flex: 1, position: "relative" }}>
                  <input
                    type="range" min={1024} max={16384} step={512} value={settings.ram}
                    onChange={e => save({ ram: parseInt(e.target.value) })}
                    style={{
                      width: "100%", height: "4px", appearance: "none", WebkitAppearance: "none",
                      background: `linear-gradient(to right, #5B3FA6 0%, #C4B5FD ${ramPercent}%, #1F1F2E ${ramPercent}%)`,
                      borderRadius: "2px", outline: "none", cursor: "pointer",
                    }}
                  />
                </div>
                <span style={{
                  fontSize: "11px", color: "#C4B5FD", fontFamily: "monospace", minWidth: "38px", textAlign: "right",
                }}>{ramGB} GB</span>
              </div>
            </SetRow>
            {ramWarning && (
              <div style={{ padding: "0 18px 12px", fontSize: "11px", color: "#DDAA44" }}>{ramWarning}</div>
            )}
            <SetRow label="Window Resolution" desc="Custom resolution. Leave empty for default (854x480).">
              <input
                type="text" value={settings.resolution}
                onChange={e => save({ resolution: e.target.value })}
                placeholder="854x480"
                style={{
                  padding: "7px 12px", background: "#08080F", border: "0.5px solid #1F1F2E",
                  borderRadius: "7px", fontSize: "12px", color: "#E8E6F5", outline: "none",
                  fontFamily: "monospace", width: "100px", boxSizing: "border-box",
                }}
              />
            </SetRow>
          </Card>
        </div>

        <div style={{ marginBottom: "26px" }}>
          <SectionHeader>JVM Arguments</SectionHeader>
          <Card>
            <SetRow label="JVM arguments preset" desc="Pre-configured Java flags optimized for performance.">
              <div style={{
                display: "flex", gap: "4px", padding: "3px",
                background: "#08080F", border: "0.5px solid #1F1F2E", borderRadius: "7px",
              }}>
                {JVM_PRESETS.map(preset => {
                  const isActive = settings.javaArgs === preset.args;
                  return (
                    <div
                      key={preset.label}
                      onClick={() => save({ javaArgs: preset.args })}
                      style={{
                        padding: "5px 12px", borderRadius: "5px", fontSize: "11px",
                        color: isActive ? "#C4B5FD" : "#8A88A8",
                        background: isActive ? "#1A1530" : "transparent",
                        cursor: "pointer",
                      }}
                    >{preset.label}</div>
                  );
                })}
              </div>
            </SetRow>
            <div style={{ padding: "14px 18px" }}>
              <p style={{ fontSize: "11px", color: "#8A88A8", margin: "0 0 8px" }}>Custom JVM flags (advanced)</p>
              <input
                type="text" value={settings.javaArgs}
                onChange={e => save({ javaArgs: e.target.value })}
                placeholder="Additional JVM flags"
                style={{
                  width: "100%", padding: "7px 12px", background: "#08080F",
                  border: "0.5px solid #1F1F2E", borderRadius: "7px", fontSize: "12px",
                  color: "#E8E6F5", outline: "none", fontFamily: "monospace", boxSizing: "border-box",
                }}
              />
            </div>
          </Card>
        </div>

        <div style={{ marginBottom: "26px" }}>
          <SectionHeader>Java</SectionHeader>
          <Card>
            <SetRow label="Java path" desc="Pulsar auto-downloads the right Java for each MC version.">
              <input
                type="text" value={settings.javaPath}
                onChange={e => save({ javaPath: e.target.value })}
                placeholder="Auto-detect"
                style={{
                  padding: "7px 12px", background: "#08080F", border: "0.5px solid #1F1F2E",
                  borderRadius: "7px", fontSize: "12px", color: "#E8E6F5", outline: "none",
                  fontFamily: "monospace", width: "180px", boxSizing: "border-box",
                }}
              />
            </SetRow>
          </Card>
        </div>
      </>
    );
  }

  function renderPrivacy() {
    return (
      <>
        <div style={{ marginBottom: "26px" }}>
          <SectionHeader>Privacy</SectionHeader>
          <Card>
            <SetRow label="Streamer mode" desc="Hide email, IP, and account info from screenshots and screen shares.">
              <Toggle value={settings.streamerMode} onChange={v => save({ streamerMode: v })} />
            </SetRow>
          </Card>
        </div>
      </>
    );
  }

  function renderAccessibility() {
    return (
      <>
        <div style={{ marginBottom: "26px" }}>
          <SectionHeader>Vision</SectionHeader>
          <Card>
            <SetRow label="Colorblind mode" desc="Remap accent colors for better visibility.">
              <select
                value={settings.colorblindMode}
                onChange={e => save({ colorblindMode: e.target.value })}
                style={{
                  padding: "7px 12px", background: "#08080F", border: "0.5px solid #1F1F2E",
                  borderRadius: "7px", fontSize: "12px", color: "#E8E6F5", cursor: "pointer",
                  minWidth: "140px", outline: "none", fontFamily: "inherit",
                }}
              >
                <option value="none">None</option>
                <option value="deuteranopia">Deuteranopia (Red-Green)</option>
                <option value="protanopia">Protanopia (Red-Green)</option>
                <option value="tritanopia">Tritanopia (Blue-Yellow)</option>
              </select>
            </SetRow>
            <SetRow label="Reduced motion" desc="Disable animations for better performance and comfort.">
              <Toggle value={settings.reducedMotion} onChange={v => save({ reducedMotion: v })} />
            </SetRow>
          </Card>
        </div>
      </>
    );
  }

  function renderAbout() {
    return (
      <>
        <div style={{ marginBottom: "26px" }}>
          <SectionHeader>Version</SectionHeader>
          <Card>
            <div style={{ display: "flex", alignItems: "center", gap: "14px", padding: "16px 18px", borderBottom: "0.5px solid #1A1A28" }}>
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none">
                <circle cx="12" cy="12" r="10" fill="#000000"/>
                <circle cx="12" cy="12" r="8" fill="none" stroke="#ffffff" strokeWidth="0.5" opacity="0.3"/>
                <ellipse cx="12" cy="12" rx="11" ry="4" fill="none" stroke="#ffffff" strokeWidth="1.5" opacity="0.6"/>
                <ellipse cx="12" cy="12" rx="9" ry="3" fill="none" stroke="#A0A0A0" strokeWidth="0.8" opacity="0.4"/>
                <circle cx="12" cy="12" r="4" fill="#000000"/>
                <circle cx="12" cy="12" r="3" fill="none" stroke="#ffffff" strokeWidth="0.3" opacity="0.5"/>
              </svg>
              <div style={{ flex: 1 }}>
                <p style={{ fontSize: "15px", fontWeight: 800, color: "#C4B5FD", margin: 0 }}>Pulsar Client</p>
                <p style={{ fontSize: "11px", color: "#8A88A8", margin: "2px 0 0" }}>v2.1.0</p>
              </div>
            </div>
            <div style={{ padding: "14px 18px", borderBottom: "0.5px solid #1A1A28" }}>
              <p style={{ fontSize: "12px", color: "#8A88A8", margin: 0, lineHeight: 1.6 }}>
                Free Minecraft PvP client with 51+ mods, animated capes, and FPS boost.
              </p>
            </div>
            <div style={{
              display: "grid", gridTemplateColumns: "1fr auto", gap: "16px", alignItems: "center",
              padding: "12px 18px",
            }}>
              <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
                <span style={{ fontSize: "12px", color: "#C7C5DC" }}>Source code</span>
                <span style={{ fontSize: "11px", color: "#6B6985", fontFamily: "monospace" }}>github.com/ericpnag/pulsar</span>
              </div>
              <button
                onClick={() => window.open("https://github.com/ericpnag/pulsar", "_blank")}
                style={{
                  padding: "7px 14px", background: "#11111C", border: "0.5px solid #2A2A3E",
                  borderRadius: "7px", color: "#C7C5DC", fontSize: "12px", cursor: "pointer",
                }}
              >View source</button>
            </div>
          </Card>
        </div>

        {/* Update checker */}
        <div style={{ marginBottom: "26px" }}>
          <SectionHeader>Updates</SectionHeader>
          <Card>
            <div style={{
              display: "flex", alignItems: "center", justifyContent: "space-between",
              padding: "14px 18px", gap: "16px",
            }}>
              <div style={{ flex: 1 }}>
                <p style={{ fontSize: "13px", fontWeight: 500, color: "#F0EEFC", margin: "0 0 2px" }}>Check for updates</p>
                <p style={{ fontSize: "11px", color: "#8A88A8", margin: 0 }}>
                  {updateStatus === "idle" && "Compare your version against the latest release."}
                  {updateStatus === "checking" && "Checking..."}
                  {updateStatus === "up-to-date" && (
                    <span style={{ color: "#5DCAA5" }}>Up to date (v2.1.0)</span>
                  )}
                  {updateStatus === "update-available" && (
                    <span>
                      <span style={{ color: "#FAC775" }}>Update available: {latestVersion}</span>
                      {" \u2014 "}
                      <span
                        onClick={() => window.open(releaseUrl, "_blank")}
                        style={{ color: "#C4B5FD", cursor: "pointer", textDecoration: "underline" }}
                      >View release</span>
                    </span>
                  )}
                </p>
              </div>
              <button
                onClick={checkForUpdates}
                disabled={updateStatus === "checking"}
                style={{
                  padding: "7px 14px", background: "#11111C", border: "0.5px solid #2A2A3E",
                  borderRadius: "7px", color: "#C7C5DC", fontSize: "12px", cursor: "pointer",
                  opacity: updateStatus === "checking" ? 0.5 : 1,
                }}
              >{updateStatus === "checking" ? "Checking..." : "Check now"}</button>
            </div>
          </Card>
        </div>

        <div style={{ marginBottom: "26px" }}>
          <SectionHeader>Maintenance</SectionHeader>
          <Card>
            <SetRow label="Repair game files" desc="Re-download version data, natives, and Pulsar mods.">
              <button
                onClick={async () => {
                  try {
                    const result = await invoke<string>("repair_game", { mcVersion: "1.21.11" });
                    alert(result);
                  } catch (e) { alert("Repair failed: " + e); }
                }}
                style={{
                  padding: "7px 14px", background: "#11111C",
                  border: "0.5px solid #401818", borderRadius: "7px",
                  color: "#E24B4A", fontSize: "12px", cursor: "pointer",
                }}
              >Repair</button>
            </SetRow>
          </Card>
        </div>
      </>
    );
  }

  function renderPlaceholder(title: string) {
    return (
      <div style={{ marginBottom: "26px" }}>
        <Card>
          <div style={{ padding: "24px 18px", textAlign: "center" }}>
            <p style={{ fontSize: "13px", color: "#8A88A8", margin: 0 }}>{title} settings coming soon.</p>
          </div>
        </Card>
      </div>
    );
  }

  function renderContent() {
    switch (settingsSection) {
      case "general": return renderGeneral();
      case "account": return renderGeneral(); // account is shown in general
      case "performance": return renderPerformance();
      case "java": return renderPerformance();
      case "privacy": return renderPrivacy();
      case "accessibility": return renderAccessibility();
      case "about": return renderAbout();
      case "updates": return renderAbout();
      case "display": return renderPlaceholder("Display");
      case "keybinds": return renderPlaceholder("Keybinds");
      case "notifications": return renderPlaceholder("Notifications");
      default: return renderGeneral();
    }
  }

  return (
    <div className="fade-in" style={{
      display: "grid", gridTemplateColumns: "220px 1fr",
      height: "100%", background: "#08080F",
    }}>
      {/* ── Left sidebar ── */}
      <div style={{
        background: "#0A0A14", borderRight: "0.5px solid #1A1A28",
        padding: "22px 14px", overflowY: "auto",
      }}>
        {/* Search */}
        <div style={{
          display: "flex", alignItems: "center", gap: "8px",
          padding: "7px 10px", background: "#0D0D17",
          border: "0.5px solid #1F1F2E", borderRadius: "7px", marginBottom: "18px",
        }}>
          <div style={{ width: "12px", height: "12px", color: "#5A5870", flexShrink: 0 }}>{icons.search}</div>
          <input
            type="text" placeholder="Search settings..."
            value={searchQuery} onChange={e => setSearchQuery(e.target.value)}
            style={{
              background: "transparent", border: 0, outline: 0, color: "#E8E6F5",
              fontSize: "12px", flex: 1, fontFamily: "inherit", padding: 0,
            }}
          />
        </div>

        {/* Nav groups */}
        {filteredGroups.map(group => (
          <div key={group.title} style={{ marginBottom: "16px" }}>
            <p style={{
              fontSize: "9px", color: "#6B6985", letterSpacing: "1.4px",
              textTransform: "uppercase", margin: "0 8px 6px", fontWeight: 500,
            }}>{group.title}</p>
            <div style={{ display: "flex", flexDirection: "column", gap: "1px" }}>
              {group.items.map(item => {
                const isActive = settingsSection === item.id;
                return (
                  <div
                    key={item.id}
                    onClick={() => setSettingsSection(item.id)}
                    style={{
                      display: "flex", alignItems: "center", gap: "10px",
                      padding: "7px 10px", borderRadius: "7px", cursor: "pointer",
                      transition: "all 150ms",
                      background: isActive ? "#1A1530" : "transparent",
                    }}
                  >
                    <div style={{
                      width: "14px", height: "14px", flexShrink: 0,
                      color: isActive ? "#C4B5FD" : "#5A5870",
                    }}>{item.icon}</div>
                    <span style={{
                      flex: 1, fontSize: "12px",
                      color: isActive ? "#F0EEFC" : "#C7C5DC",
                    }}>{item.label}</span>
                    {item.badge && (
                      <span style={{
                        fontSize: "10px", padding: "1px 6px",
                        background: "rgba(216,90,48,0.15)", color: "#F0997B",
                        borderRadius: "8px", fontWeight: 500,
                      }}>{item.badge}</span>
                    )}
                  </div>
                );
              })}
            </div>
          </div>
        ))}
      </div>

      {/* ── Right content area ── */}
      <div style={{ padding: "24px 30px", maxWidth: "720px", overflowY: "auto" }}>
        <p style={{
          fontSize: "10px", fontWeight: 500, color: "#6B6985",
          letterSpacing: "1.5px", textTransform: "uppercase", margin: "0 0 8px",
        }}>{meta.label}</p>
        <h1 style={{
          fontSize: "22px", fontWeight: 500, color: "#F0EEFC",
          margin: "0 0 4px", letterSpacing: "-0.3px",
        }}>{meta.title}</h1>
        <p style={{ fontSize: "13px", color: "#8A88A8", margin: "0 0 24px" }}>{meta.sub}</p>

        {renderContent()}
      </div>
    </div>
  );
}
