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
    // Live endpoint uses "verification_uri", older uses "verification_url"
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
struct XblClaims {
    xui: Vec<XblXui>,
}

#[derive(Deserialize)]
struct XblXui {
    uhs: String,
}

#[derive(Deserialize)]
struct McTokenResp {
    access_token: String,
}

#[derive(Deserialize)]
struct McProfile {
    id: String,
    name: String,
}

#[derive(Serialize, Deserialize, Clone, Default, Debug)]
pub struct Account {
    pub username: String,
    pub uuid: String,
    pub access_token: String,
    pub refresh_token: String,
}

pub struct AccountState(pub Mutex<Option<Account>>);

fn account_path() -> PathBuf {
    #[cfg(target_os = "macos")]
    {
        let home = std::env::var("HOME").unwrap_or_else(|_| ".".to_string());
        PathBuf::from(home).join("Library/Application Support/bloom/account.json")
    }
    #[cfg(target_os = "windows")]
    {
        let appdata = std::env::var("APPDATA").unwrap_or_else(|_| ".".to_string());
        PathBuf::from(appdata).join("bloom/account.json")
    }
    #[cfg(target_os = "linux")]
    {
        let home = std::env::var("HOME").unwrap_or_else(|_| ".".to_string());
        PathBuf::from(home).join(".bloom/account.json")
    }
}

fn load_account() -> Option<Account> {
    let path = account_path();
    let data = std::fs::read_to_string(&path).ok()?;
    serde_json::from_str(&data).ok()
}

fn save_account_to_disk(account: &Account) {
    let path = account_path();
    if let Some(parent) = path.parent() {
        let _ = std::fs::create_dir_all(parent);
    }
    let _ = std::fs::write(&path, serde_json::to_string_pretty(account).unwrap_or_default());
}

fn delete_account_from_disk() {
    let _ = std::fs::remove_file(account_path());
}

#[tauri::command]
pub fn start_microsoft_login(app: AppHandle) -> Result<(), String> {
    let client = Client::new();

    // Step 1: request device code
    let raw = client
        .post("https://login.microsoftonline.com/consumers/oauth2/v2.0/devicecode")
        .form(&[("client_id", CLIENT_ID), ("scope", "XboxLive.signin offline_access")])
        .send().map_err(|e| e.to_string())?
        .text().map_err(|e| e.to_string())?;

    let dc: DeviceCodeResp = serde_json::from_str(&raw)
        .map_err(|_| {
            // Parse the error from Microsoft so we can show it
            let msg = serde_json::from_str::<serde_json::Value>(&raw)
                .ok()
                .and_then(|v| v["error_description"].as_str().map(|s| s.to_string()))
                .unwrap_or_else(|| raw[..raw.len().min(300)].to_string());
            format!("Microsoft error: {}", msg)
        })?;

    // Tell the frontend to show the code
    let _ = app.emit("auth_code", serde_json::json!({
        "url": dc.verification_uri,
        "code": dc.user_code,
    }));

    let device_code = dc.device_code.clone();
    let interval = dc.interval.max(5);

    // Poll in background thread
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

            // XBL
            let xbl: XblResp = match client
                .post("https://user.auth.xboxlive.com/user/authenticate")
                .header("Accept", "application/json")
                .json(&serde_json::json!({
                    "Properties": { "AuthMethod": "RPS", "SiteName": "user.auth.xboxlive.com", "RpsTicket": format!("d={}", ms_token) },
                    "RelyingParty": "http://auth.xboxlive.com",
                    "TokenType": "JWT"
                }))
                .send().and_then(|r| r.json()) {
                    Ok(r) => r, Err(e) => { let _ = app.emit("auth_error", format!("xbl: {}", e)); return; }
                };

            let uhs = match xbl.display_claims.xui.first() { Some(x) => x.uhs.clone(), None => { let _ = app.emit("auth_error", "no uhs"); return; } };

            // XSTS
            let xsts: XblResp = match client
                .post("https://xsts.auth.xboxlive.com/xsts/authorize")
                .header("Accept", "application/json")
                .json(&serde_json::json!({
                    "Properties": { "SandboxId": "RETAIL", "UserTokens": [xbl.token] },
                    "RelyingParty": "rp://api.minecraftservices.com/",
                    "TokenType": "JWT"
                }))
                .send().and_then(|r| r.json()) {
                    Ok(r) => r, Err(e) => { let _ = app.emit("auth_error", format!("xsts: {}", e)); return; }
                };

            // Minecraft token
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

            // Profile
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
pub fn get_account(state: State<AccountState>) -> Option<Account> {
    let mut guard = state.0.lock().unwrap();
    if guard.is_none() {
        *guard = load_account();
    }
    guard.clone()
}

#[tauri::command]
pub fn save_account(state: State<AccountState>, account: Account) {
    save_account_to_disk(&account);
    *state.0.lock().unwrap() = Some(account);
}

#[tauri::command]
pub fn logout(state: State<AccountState>) {
    delete_account_from_disk();
    *state.0.lock().unwrap() = None;
}
