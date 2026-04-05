import { useState, useEffect } from "react";
import { invoke } from "@tauri-apps/api/core";
import { listen } from "@tauri-apps/api/event";
import { Sidebar } from "./components/Sidebar";
import { LoginModal } from "./components/LoginModal";
import { HomePage } from "./pages/Home";
import { LibraryPage } from "./pages/Library";
import { ModStorePage } from "./pages/ModStore";
import { TexturePacksPage } from "./pages/TexturePacks";
import { VersionsPage } from "./pages/Versions";
import { SettingsPage } from "./pages/Settings";

export type Page = "home" | "library" | "modstore" | "texturepacks" | "versions" | "settings";
export type LaunchPhase = "idle" | "loading" | "done" | "error";

export interface LaunchState {
  phase: LaunchPhase;
  progress: number;
  status: string;
}

interface AccountInfo {
  username: string;
  uuid: string;
  accessToken: string;
}

export default function App() {
  const [page, setPage] = useState<Page>("home");
  const [versions, setVersions] = useState<string[]>([]);
  const [selectedVersion, setSelectedVersion] = useState("1.21.1");
  const [launch, setLaunch] = useState<LaunchState>({ phase: "idle", progress: 0, status: "" });
  const [account, setAccount] = useState<AccountInfo | null>(null);
  const [loginModal, setLoginModal] = useState<{ phase: "waiting" | "code" | "error"; code?: string; url?: string; error?: string } | null>(null);

  useEffect(() => {
    let unlistenCode: (() => void) | null = null;
    let unlistenSuccess: (() => void) | null = null;
    let unlistenError: (() => void) | null = null;

    invoke<any>("get_account").then(a => { if (a) setAccount({ username: a.username, uuid: a.uuid, accessToken: a.access_token }); }).catch(() => {});
    invoke<string[]>("get_versions")
      .then(v => { setVersions(v); if (v.includes("1.21.1")) setSelectedVersion("1.21.1"); else if (v.length) setSelectedVersion(v[0]); })
      .catch(() => setVersions(["1.21.1"]));

    listen<{ url: string; code: string }>("auth_code", e => {
      setLoginModal({ phase: "code", url: e.payload.url, code: e.payload.code });
    }).then(fn => { unlistenCode = fn; });

    listen<any>("auth_success", e => {
      const acc = { username: e.payload.username, uuid: e.payload.uuid, accessToken: e.payload.accessToken };
      setAccount(acc);
      invoke("save_account", { account: { username: acc.username, uuid: acc.uuid, access_token: acc.accessToken, refresh_token: e.payload.refreshToken } });
      setLoginModal(null);
    }).then(fn => { unlistenSuccess = fn; });

    listen<string>("auth_error", e => {
      setLoginModal({ phase: "error", error: e.payload });
    }).then(fn => { unlistenError = fn; });

    return () => {
      unlistenCode?.();
      unlistenSuccess?.();
      unlistenError?.();
    };
  }, []);

  async function handleLogin() {
    setLoginModal({ phase: "waiting" });
    try {
      await invoke("start_microsoft_login");
    } catch (e: any) {
      setLoginModal({ phase: "error", error: String(e) });
    }
  }

  function handleLogout() {
    invoke("logout");
    setAccount(null);
  }

  async function play() {
    setLaunch({ phase: "loading", progress: 0, status: "Checking files..." });
    const unlisten = await listen<{ pct: number; msg: string }>("launch_progress", e => {
      setLaunch(prev => ({ ...prev, progress: e.payload.pct, status: e.payload.msg }));
    });
    try {
      await invoke("launch_minecraft", {
        version: selectedVersion,
        username: account?.username ?? null,
        uuid: account?.uuid ?? null,
        accessToken: account?.accessToken ?? null,
      });
      setLaunch({ phase: "done", progress: 100, status: "Game launched!" });
      setTimeout(() => setLaunch({ phase: "idle", progress: 0, status: "" }), 3000);
    } catch (e: any) {
      setLaunch({ phase: "error", progress: 0, status: String(e) });
    } finally {
      unlisten();
    }
  }

  const pages: Record<Page, React.ReactNode> = {
    home: <HomePage launch={launch} versions={versions} selectedVersion={selectedVersion} onVersionChange={setSelectedVersion} onPlay={play} />,
    library: <LibraryPage selectedVersion={selectedVersion} />,
    modstore: <ModStorePage versions={versions} selectedVersion={selectedVersion} onVersionChange={setSelectedVersion} />,
    texturepacks: <TexturePacksPage versions={versions} selectedVersion={selectedVersion} onVersionChange={setSelectedVersion} />,
    versions: <VersionsPage versions={versions} selected={selectedVersion} onSelect={setSelectedVersion} />,
    settings: <SettingsPage />,
  };

  return (
    <div style={{ display: "flex", height: "100vh", width: "100vw", background: "#0d0810", color: "#fff", fontFamily: "system-ui, -apple-system, sans-serif", overflow: "hidden" }}>
      <Sidebar activePage={page} onNavigate={setPage} account={account} onLogin={handleLogin} onLogout={handleLogout} />
      <main style={{ flex: 1, overflow: "hidden", display: "flex", flexDirection: "column" }}>
        {pages[page]}
      </main>
      {loginModal && <LoginModal {...loginModal} onClose={() => setLoginModal(null)} />}
    </div>
  );
}
