use std::time::Duration;
use std::path::PathBuf;
use reqwest::blocking::Client;
use serde::{Deserialize, Serialize};
use tauri::{AppHandle, Emitter, State};
use std::sync::Mutex;

const CLIENT_ID: &str = "c36a9fb6-4f2a-41ff-90bd-ae7cc92031eb";

#[derive(Deserialize)]
struct DeviceCodeResp {
    device_code: String,
    user_code: String,
    #[serde(alias = "verification_url")]
    verification_uri: String,
    interval: u64,
}

#[derive(Deserialize)]
struct MsTokenResp {
    access_token: Option<String>,
    refresh_token: Option<String>,
    error: Option<String>,
}

#[derive(Deserialize)]
struct XblResp {
    #[serde(rename = "Token")]
    token: String,
    #[serde(rename = "DisplayClaims")]
    display_claims: XblClaims,
}

#[derive(Deserialize)]
struct XblClaims { xui: Vec<XblXui> }

#[derive(Deserialize)]
struct XblXui { uhs: String }

#[derive(Deserialize)]
struct McTokenResp { access_token: String }

#[derive(Deserialize)]
struct McProfile { id: String, name: String }

#[derive(Serialize, Deserialize, Clone, Default, Debug)]
pub struct Account {
    pub username: String,
    pub uuid: String,
    pub access_token: String,
    pub refresh_token: String,
}

/// Multi-account storage
#[derive(Serialize, Deserialize, Clone, Default, Debug)]
struct AccountStore {
    accounts: Vec<Account>,
    active: usize, // index of active account
}

pub struct AccountState(pub Mutex<Option<Account>>);

fn account_path() -> PathBuf {
    #[cfg(target_os = "macos")]
    { PathBuf::from(std::env::var("HOME").unwrap_or_else(|_| ".".to_string())).join("Library/Application Support/bloom/accounts.json") }
    #[cfg(target_os = "windows")]
    { PathBuf::from(std::env::var("APPDATA").unwrap_or_else(|_| ".".to_string())).join("bloom/accounts.json") }
    #[cfg(target_os = "linux")]
    { PathBuf::from(std::env::var("HOME").unwrap_or_else(|_| ".".to_string())).join(".bloom/accounts.json") }
}

// Legacy single-account path for migration
fn legacy_account_path() -> PathBuf {
    #[cfg(target_os = "macos")]
    { PathBuf::from(std::env::var("HOME").unwrap_or_else(|_| ".".to_string())).join("Library/Application Support/bloom/account.json") }
    #[cfg(target_os = "windows")]
    { PathBuf::from(std::env::var("APPDATA").unwrap_or_else(|_| ".".to_string())).join("bloom/account.json") }
    #[cfg(target_os = "linux")]
    { PathBuf::from(std::env::var("HOME").unwrap_or_else(|_| ".".to_string())).join(".bloom/account.json") }
}

fn load_store() -> AccountStore {
    // Try new multi-account format first
    if let Ok(data) = std::fs::read_to_string(account_path()) {
        if let Ok(store) = serde_json::from_str::<AccountStore>(&data) {
            return store;
        }
    }
    // Migrate from legacy single-account format
    if let Ok(data) = std::fs::read_to_string(legacy_account_path()) {
        if let Ok(account) = serde_json::from_str::<Account>(&data) {
            let store = AccountStore { accounts: vec![account], active: 0 };
            save_store(&store);
            let _ = std::fs::remove_file(legacy_account_path());
            return store;
        }
    }
    AccountStore::default()
}

fn save_store(store: &AccountStore) {
    let path = account_path();
    if let Some(parent) = path.parent() {
        let _ = std::fs::create_dir_all(parent);
    }
    let _ = std::fs::write(&path, serde_json::to_string_pretty(store).unwrap_or_default());
}

fn load_account() -> Option<Account> {
    let store = load_store();
    store.accounts.get(store.active).cloned()
}

fn save_account_to_disk(account: &Account) {
    let mut store = load_store();
    // Update existing or add new
    if let Some(existing) = store.accounts.iter_mut().find(|a| a.uuid == account.uuid) {
        *existing = account.clone();
    } else {
        store.accounts.push(account.clone());
        store.active = store.accounts.len() - 1;
    }
    save_store(&store);
}

fn delete_account_from_disk() {
    let _ = std::fs::remove_file(account_path());
    let _ = std::fs::remove_file(legacy_account_path());
}

#[tauri::command]
pub fn start_microsoft_login(app: AppHandle) -> Result<(), String> {
    let client = Client::new();

    let raw = client
        .post("https://login.microsoftonline.com/consumers/oauth2/v2.0/devicecode")
        .form(&[("client_id", CLIENT_ID), ("scope", "XboxLive.signin offline_access")])
        .send().map_err(|e| e.to_string())?
        .text().map_err(|e| e.to_string())?;

    let dc: DeviceCodeResp = serde_json::from_str(&raw)
        .map_err(|_| {
            let msg = serde_json::from_str::<serde_json::Value>(&raw)
                .ok()
                .and_then(|v| v["error_description"].as_str().map(|s| s.to_string()))
                .unwrap_or_else(|| raw[..raw.len().min(300)].to_string());
            format!("Microsoft error: {}", msg)
        })?;

    let _ = app.emit("auth_code", serde_json::json!({
        "url": dc.verification_uri,
        "code": dc.user_code,
    }));

    let device_code = dc.device_code.clone();
    let interval = dc.interval.max(5);

    std::thread::spawn(move || {
        let client = Client::new();
        loop {
            std::thread::sleep(Duration::from_secs(interval));

            let resp = match client
                .post("https://login.microsoftonline.com/consumers/oauth2/v2.0/token")
                .form(&[
                    ("client_id", CLIENT_ID),
                    ("grant_type", "urn:ietf:params:oauth:grant-type:device_code"),
                    ("device_code", &device_code),
                ])
                .send().and_then(|r| r.json::<MsTokenResp>()) {
                    Ok(r) => r,
                    Err(e) => { let _ = app.emit("auth_error", e.to_string()); return; }
                };

            match resp.error.as_deref() {
                Some("authorization_pending") => continue,
                Some("slow_down") => { std::thread::sleep(Duration::from_secs(5)); continue; }
                Some(e) => { let _ = app.emit("auth_error", e.to_string()); return; }
                None => {}
            }

            let ms_token = match resp.access_token { Some(t) => t, None => { let _ = app.emit("auth_error", "no ms token"); return; } };
            let refresh = resp.refresh_token.unwrap_or_default();

            let xbl: XblResp = match client
                .post("https://user.auth.xboxlive.com/user/authenticate")
                .header("Accept", "application/json")
                .json(&serde_json::json!({
                    "Properties": { "AuthMethod": "RPS", "SiteName": "user.auth.xboxlive.com", "RpsTicket": format!("d={}", ms_token) },
                    "RelyingParty": "http://auth.xboxlive.com", "TokenType": "JWT"
                }))
                .send().and_then(|r| r.json()) {
                    Ok(r) => r, Err(e) => { let _ = app.emit("auth_error", format!("xbl: {}", e)); return; }
                };

            let uhs = match xbl.display_claims.xui.first() { Some(x) => x.uhs.clone(), None => { let _ = app.emit("auth_error", "no uhs"); return; } };

            let xsts: XblResp = match client
                .post("https://xsts.auth.xboxlive.com/xsts/authorize")
                .header("Accept", "application/json")
                .json(&serde_json::json!({
                    "Properties": { "SandboxId": "RETAIL", "UserTokens": [xbl.token] },
                    "RelyingParty": "rp://api.minecraftservices.com/", "TokenType": "JWT"
                }))
                .send().and_then(|r| r.json()) {
                    Ok(r) => r, Err(e) => { let _ = app.emit("auth_error", format!("xsts: {}", e)); return; }
                };

            let mc_raw = match client
                .post("https://api.minecraftservices.com/authentication/login_with_xbox")
                .header("Accept", "application/json")
                .json(&serde_json::json!({ "identityToken": format!("XBL3.0 x={};{}", uhs, xsts.token) }))
                .send().and_then(|r| r.text()) {
                    Ok(r) => r, Err(e) => { let _ = app.emit("auth_error", format!("mc token req: {}", e)); return; }
                };
            let mc: McTokenResp = match serde_json::from_str(&mc_raw) {
                Ok(r) => r,
                Err(_) => { let _ = app.emit("auth_error", format!("mc token body: {}", &mc_raw[..mc_raw.len().min(400)])); return; }
            };

            let profile: McProfile = match client
                .get("https://api.minecraftservices.com/minecraft/profile")
                .bearer_auth(&mc.access_token)
                .send().and_then(|r| r.json()) {
                    Ok(p) => p, Err(e) => { let _ = app.emit("auth_error", format!("profile: {}", e)); return; }
                };

            let _ = app.emit("auth_success", serde_json::json!({
                "username": profile.name,
                "uuid": profile.id,
                "accessToken": mc.access_token,
                "refreshToken": refresh,
            }));
            return;
        }
    });

    Ok(())
}

#[tauri::command]
pub fn refresh_account(state: State<AccountState>) -> Result<Account, String> {
    let guard = state.0.lock().unwrap();
    let account = guard.clone().or_else(|| load_account())
        .ok_or("No account saved")?;
    drop(guard);

    if account.refresh_token.is_empty() {
        return Err("No refresh token — please log in again".to_string());
    }

    let client = Client::new();

    let ms_resp: MsTokenResp = client
        .post("https://login.microsoftonline.com/consumers/oauth2/v2.0/token")
        .form(&[
            ("client_id", CLIENT_ID),
            ("grant_type", "refresh_token"),
            ("refresh_token", &account.refresh_token),
            ("scope", "XboxLive.signin offline_access"),
        ])
        .send().map_err(|e| format!("MS refresh: {}", e))?
        .json().map_err(|e| format!("MS refresh parse: {}", e))?;

    if let Some(err) = ms_resp.error {
        return Err(format!("MS refresh error: {} — please log in again", err));
    }

    let ms_token = ms_resp.access_token.ok_or("No MS access token from refresh")?;
    let new_refresh = ms_resp.refresh_token.unwrap_or(account.refresh_token.clone());

    let xbl: XblResp = client
        .post("https://user.auth.xboxlive.com/user/authenticate")
        .header("Accept", "application/json")
        .json(&serde_json::json!({
            "Properties": { "AuthMethod": "RPS", "SiteName": "user.auth.xboxlive.com", "RpsTicket": format!("d={}", ms_token) },
            "RelyingParty": "http://auth.xboxlive.com", "TokenType": "JWT"
        }))
        .send().map_err(|e| format!("XBL: {}", e))?
        .json().map_err(|e| format!("XBL parse: {}", e))?;

    let uhs = xbl.display_claims.xui.first().map(|x| x.uhs.clone()).ok_or("No UHS")?;

    let xsts: XblResp = client
        .post("https://xsts.auth.xboxlive.com/xsts/authorize")
        .header("Accept", "application/json")
        .json(&serde_json::json!({
            "Properties": { "SandboxId": "RETAIL", "UserTokens": [xbl.token] },
            "RelyingParty": "rp://api.minecraftservices.com/", "TokenType": "JWT"
        }))
        .send().map_err(|e| format!("XSTS: {}", e))?
        .json().map_err(|e| format!("XSTS parse: {}", e))?;

    let mc: McTokenResp = client
        .post("https://api.minecraftservices.com/authentication/login_with_xbox")
        .header("Accept", "application/json")
        .json(&serde_json::json!({ "identityToken": format!("XBL3.0 x={};{}", uhs, xsts.token) }))
        .send().map_err(|e| format!("MC token: {}", e))?
        .json().map_err(|e| format!("MC token parse: {}", e))?;

    let profile: McProfile = client
        .get("https://api.minecraftservices.com/minecraft/profile")
        .bearer_auth(&mc.access_token)
        .send().map_err(|e| format!("Profile: {}", e))?
        .json().map_err(|e| format!("Profile parse: {}", e))?;

    let new_account = Account {
        username: profile.name,
        uuid: profile.id,
        access_token: mc.access_token,
        refresh_token: new_refresh,
    };

    save_account_to_disk(&new_account);
    *state.0.lock().unwrap() = Some(new_account.clone());

    Ok(new_account)
}

#[tauri::command]
pub fn get_account(state: State<AccountState>) -> Option<Account> {
    let mut guard = state.0.lock().unwrap();
    if guard.is_none() {
        *guard = load_account();
    }
    guard.clone()
}

/// Get all saved accounts for the account switcher
#[tauri::command]
pub fn get_accounts() -> Vec<Account> {
    let store = load_store();
    // Return accounts without access tokens for security
    store.accounts.iter().map(|a| Account {
        username: a.username.clone(),
        uuid: a.uuid.clone(),
        access_token: String::new(), // Don't send tokens to frontend list
        refresh_token: String::new(),
    }).collect()
}

/// Switch to a different account by UUID
#[tauri::command]
pub fn switch_account(state: State<AccountState>, uuid: String) -> Result<Account, String> {
    let mut store = load_store();
    let idx = store.accounts.iter().position(|a| a.uuid == uuid)
        .ok_or("Account not found")?;
    store.active = idx;
    save_store(&store);

    let account = store.accounts[idx].clone();
    *state.0.lock().unwrap() = Some(account.clone());
    Ok(account)
}

/// Remove an account by UUID
#[tauri::command]
pub fn remove_account(state: State<AccountState>, uuid: String) -> Result<(), String> {
    let mut store = load_store();
    store.accounts.retain(|a| a.uuid != uuid);
    if store.active >= store.accounts.len() {
        store.active = store.accounts.len().saturating_sub(1);
    }
    save_store(&store);

    // Update active state
    let active = store.accounts.get(store.active).cloned();
    *state.0.lock().unwrap() = active;
    Ok(())
}

#[tauri::command]
pub fn save_account(state: State<AccountState>, account: Account) {
    save_account_to_disk(&account);
    *state.0.lock().unwrap() = Some(account);
}

#[tauri::command]
pub fn logout(state: State<AccountState>) {
    // Remove active account only, not all accounts
    let mut store = load_store();
    if store.active < store.accounts.len() {
        store.accounts.remove(store.active);
        if store.active >= store.accounts.len() {
            store.active = store.accounts.len().saturating_sub(1);
        }
    }
    save_store(&store);
    let active = store.accounts.get(store.active).cloned();
    *state.0.lock().unwrap() = active;
}

/// Get system RAM in MB for auto-allocation
#[tauri::command]
pub fn get_system_ram() -> u64 {
    let info = sysinfo::System::new_all();
    info.total_memory() / 1024 / 1024
}

/// Repair game files — delete version data and re-download on next launch
#[tauri::command]
pub fn repair_game(mc_version: String) -> Result<String, String> {
    let game_dir = crate::launcher::game_dir_pub();
    let ver_dir = game_dir.join(format!("versions/{}", mc_version));
    let natives_dir = game_dir.join(format!("natives/{}", mc_version));
    let mods_dir = game_dir.join(format!("profiles/{}/mods", mc_version));

    let mut cleaned = Vec::new();

    // Remove version JSON (will re-download)
    if ver_dir.exists() {
        let _ = std::fs::remove_dir_all(&ver_dir);
        cleaned.push("version data");
    }

    // Remove natives (will re-extract)
    if natives_dir.exists() {
        let _ = std::fs::remove_dir_all(&natives_dir);
        cleaned.push("native libraries");
    }

    // Remove pulsar-core and bloom-core jars (will re-install)
    if mods_dir.exists() {
        if let Ok(entries) = std::fs::read_dir(&mods_dir) {
            for entry in entries.flatten() {
                let name = entry.file_name().to_string_lossy().to_string();
                if (name.starts_with("pulsar-core-") || name.starts_with("bloom-core-")) && name.ends_with(".jar") {
                    let _ = std::fs::remove_file(entry.path());
                    cleaned.push("pulsar-core mod");
                }
            }
        }
    }

    if cleaned.is_empty() {
        Ok("Nothing to repair — game files look clean".to_string())
    } else {
        Ok(format!("Repaired: {}. Launch the game to re-download.", cleaned.join(", ")))
    }
}
