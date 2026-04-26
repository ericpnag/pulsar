use std::fs;
use std::io;
use std::path::PathBuf;
use std::process::Command;
use std::collections::HashMap;
use sha1::{Sha1, Digest};
use std::sync::{Arc, Mutex};
use std::thread;
use reqwest::blocking::Client;
use serde::Deserialize;
use tauri::{AppHandle, Emitter};

pub fn game_dir_pub() -> PathBuf { game_dir() }

fn game_dir() -> PathBuf {
    #[cfg(target_os = "macos")]
    {
        let home = std::env::var("HOME").unwrap_or_else(|_| ".".to_string());
        PathBuf::from(home).join("Library/Application Support/pulsar")
    }
    #[cfg(target_os = "windows")]
    {
        let appdata = std::env::var("APPDATA").unwrap_or_else(|_| ".".to_string());
        PathBuf::from(appdata).join("pulsar")
    }
    #[cfg(target_os = "linux")]
    {
        let home = std::env::var("HOME").unwrap_or_else(|_| ".".to_string());
        PathBuf::from(home).join(".pulsar")
    }
}

fn emit(app: &AppHandle, pct: u32, msg: &str) {
    let _ = app.emit("launch_progress", serde_json::json!({ "pct": pct, "msg": msg }));
}

/// Returns the major Java version needed for a given MC version
fn java_version_for_mc(mc_version: &str) -> u32 {
    let parts: Vec<&str> = mc_version.split('.').collect();
    let minor: u32 = parts.get(1).and_then(|s| s.parse().ok()).unwrap_or(0);
    let patch: u32 = parts.get(2).and_then(|s| s.parse().ok()).unwrap_or(0);
    if minor >= 21 || (minor == 20 && patch >= 5) { 21 }
    else if minor >= 16 { 17 }
    else { 8 }
}

fn find_java(mc_version: &str) -> String {
    let java_ver = java_version_for_mc(mc_version);
    let java_bin = if cfg!(target_os = "windows") { "java.exe" } else { "java" };

    // 1. Check bundled Java for this version
    let bundled = bundled_java_path(java_ver);
    if bundled.exists() {
        return bundled.to_string_lossy().to_string();
    }

    // 2. Check JAVA_HOME
    if let Ok(java_home) = std::env::var("JAVA_HOME") {
        let bin = PathBuf::from(&java_home).join("bin").join(java_bin);
        if bin.exists() {
            return bin.to_string_lossy().to_string();
        }
    }

    // 3. Check common install locations
    #[cfg(target_os = "macos")]
    {
        let paths: &[&str] = match java_ver {
            8 => &[
                "/Library/Java/JavaVirtualMachines/temurin-8.jdk/Contents/Home/bin/java",
                "/opt/homebrew/opt/openjdk@8/bin/java",
            ],
            17 => &[
                "/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home/bin/java",
                "/opt/homebrew/opt/openjdk@17/bin/java",
            ],
            _ => &[
                "/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home/bin/java",
                "/opt/homebrew/opt/openjdk@21/bin/java",
                "/opt/homebrew/opt/openjdk/bin/java",
            ],
        };
        for path in paths {
            if PathBuf::from(path).exists() { return path.to_string(); }
        }
    }

    java_bin.to_string()
}

fn bundled_java_path(java_ver: u32) -> PathBuf {
    let java_dir = game_dir().join("java");
    let folder = format!("jdk-{}", java_ver);
    #[cfg(target_os = "macos")]
    { java_dir.join(&folder).join("Contents/Home/bin/java") }
    #[cfg(target_os = "windows")]
    { java_dir.join(&folder).join("bin/java.exe") }
    #[cfg(target_os = "linux")]
    { java_dir.join(&folder).join("bin/java") }
}

fn download_java(app: &AppHandle, mc_version: &str) -> Result<(), String> {
    let java_ver = java_version_for_mc(mc_version);
    let bundled = bundled_java_path(java_ver);
    if bundled.exists() { return Ok(()); }

    let _ = app.emit("launch_progress", serde_json::json!({"pct": 5, "msg": format!("Downloading Java {}...", java_ver)}));

    let java_dir = game_dir().join("java");
    fs::create_dir_all(&java_dir).map_err(|e| e.to_string())?;

    // Adoptium Temurin download URLs — version-aware
    #[cfg(target_os = "macos")]
    let url = if cfg!(target_arch = "aarch64") {
        // Java 8 and 17 use x64 with Rosetta on Apple Silicon (old LWJGL lacks arm64 natives)
        // Only Java 21 (MC 1.20.5+) has proper arm64 LWJGL support
        let arch = if java_ver >= 21 { "aarch64" } else { "x64" };
        format!("https://api.adoptium.net/v3/binary/latest/{}/ga/mac/{}/jdk/hotspot/normal/eclipse?project=jdk", java_ver, arch)
    } else {
        format!("https://api.adoptium.net/v3/binary/latest/{}/ga/mac/x64/jdk/hotspot/normal/eclipse?project=jdk", java_ver)
    };
    #[cfg(target_os = "windows")]
    let url = format!("https://api.adoptium.net/v3/binary/latest/{}/ga/windows/x64/jdk/hotspot/normal/eclipse?project=jdk", java_ver);
    #[cfg(target_os = "linux")]
    let url = format!("https://api.adoptium.net/v3/binary/latest/{}/ga/linux/x64/jdk/hotspot/normal/eclipse?project=jdk", java_ver);

    let client = Client::new();
    let response = client.get(&url).send().map_err(|e| format!("Failed to download Java {}: {}", java_ver, e))?;
    if !response.status().is_success() {
        return Err(format!("Java {} download failed: HTTP {}", java_ver, response.status()));
    }

    let bytes = response.bytes().map_err(|e| e.to_string())?;
    let _ = app.emit("launch_progress", serde_json::json!({"pct": 30, "msg": format!("Installing Java {}...", java_ver)}));

    #[cfg(target_os = "macos")]
    {
        let tar_path = java_dir.join("java.tar.gz");
        fs::write(&tar_path, &bytes).map_err(|e| e.to_string())?;
        let status = Command::new("tar")
            .args(["xzf", &tar_path.to_string_lossy(), "-C", &java_dir.to_string_lossy()])
            .status()
            .map_err(|e| e.to_string())?;
        if !status.success() { return Err("Failed to extract Java".to_string()); }
        let _ = fs::remove_file(&tar_path);
        rename_jdk_folder(&java_dir, java_ver);
    }

    #[cfg(target_os = "windows")]
    {
        let zip_path = java_dir.join("java.zip");
        fs::write(&zip_path, &bytes).map_err(|e| e.to_string())?;
        let file = fs::File::open(&zip_path).map_err(|e| e.to_string())?;
        let mut archive = zip::ZipArchive::new(file).map_err(|e| e.to_string())?;
        archive.extract(&java_dir).map_err(|e| e.to_string())?;
        let _ = fs::remove_file(&zip_path);
        rename_jdk_folder(&java_dir, java_ver);
    }

    #[cfg(target_os = "linux")]
    {
        let tar_path = java_dir.join("java.tar.gz");
        fs::write(&tar_path, &bytes).map_err(|e| e.to_string())?;
        let status = Command::new("tar")
            .args(["xzf", &tar_path.to_string_lossy(), "-C", &java_dir.to_string_lossy()])
            .status()
            .map_err(|e| e.to_string())?;
        if !status.success() { return Err("Failed to extract Java".to_string()); }
        let _ = fs::remove_file(&tar_path);
        rename_jdk_folder(&java_dir, java_ver);
    }

    let _ = app.emit("launch_progress", serde_json::json!({"pct": 40, "msg": format!("Java {} installed!", java_ver)}));
    Ok(())
}

fn rename_jdk_folder(java_dir: &PathBuf, java_ver: u32) {
    let target_name = format!("jdk-{}", java_ver);
    let target = java_dir.join(&target_name);
    if target.exists() {
        let _ = fs::remove_dir_all(&target);
    }
    if let Ok(entries) = fs::read_dir(java_dir) {
        for entry in entries.flatten() {
            let name = entry.file_name().to_string_lossy().to_string();
            // Match jdk-8uXXX, jdk-17.0.X, jdk-21.0.X, etc.
            if name.starts_with(&format!("jdk-{}", java_ver)) && name != target_name && entry.path().is_dir() {
                let _ = fs::rename(entry.path(), &target);
                break;
            }
            // Java 8 from Adoptium extracts as jdk8uXXX
            if java_ver == 8 && name.starts_with("jdk8u") && entry.path().is_dir() {
                let _ = fs::rename(entry.path(), &target);
                break;
            }
        }
    }
}

// --- Vanilla structs ---

#[derive(Deserialize)]
struct VersionManifest {
    versions: Vec<VersionEntry>,
}

#[derive(Deserialize)]
struct VersionEntry {
    id: String,
    url: String,
    #[serde(rename = "type")]
    r#type: String,
}

#[derive(Deserialize)]
struct VersionJson {
    #[serde(rename = "mainClass")]
    main_class: String,
    libraries: Vec<Library>,
    downloads: ClientDownloads,
    #[serde(rename = "assetIndex")]
    asset_index: AssetIndex,
}

#[derive(Deserialize)]
struct ClientDownloads {
    client: Artifact,
}

#[derive(Deserialize, Clone)]
struct Artifact {
    url: String,
    path: Option<String>,
}

#[derive(Deserialize)]
struct AssetIndex {
    url: String,
    id: String,
}

#[derive(Deserialize, Clone)]
struct Library {
    downloads: Option<LibraryDownloads>,
    rules: Option<Vec<Rule>>,
    natives: Option<HashMap<String, String>>,
}

#[derive(Deserialize, Clone)]
struct LibraryDownloads {
    artifact: Option<Artifact>,
    classifiers: Option<HashMap<String, Artifact>>,
}

#[derive(Deserialize, Clone)]
struct Rule {
    action: String,
    os: Option<OsRule>,
}

#[derive(Deserialize, Clone)]
struct OsRule {
    name: Option<String>,
}

// --- Fabric structs ---

#[derive(Deserialize)]
struct FabricLoaderVersion {
    loader: FabricLoaderInfo,
}

#[derive(Deserialize)]
struct FabricLoaderInfo {
    version: String,
}

#[derive(Deserialize)]
struct FabricVersionJson {
    #[serde(rename = "mainClass")]
    main_class: String,
    libraries: Vec<FabricLibrary>,
    #[serde(default)]
    arguments: FabricArguments,
}

#[derive(Deserialize, Default)]
struct FabricArguments {
    #[serde(default)]
    jvm: Vec<String>,
    #[serde(default)]
    game: Vec<String>,
}

#[derive(Deserialize, Clone)]
struct FabricLibrary {
    name: String,
    url: String,
}

// --- Helpers ---

fn download_file(client: &Client, url: &str, dest: &PathBuf) -> Result<(), String> {
    if dest.exists() {
        return Ok(());
    }
    if let Some(parent) = dest.parent() {
        fs::create_dir_all(parent).map_err(|e| e.to_string())?;
    }
    // URL-encode + characters for Legacy Fabric compatibility
    let encoded_url = url.replace('+', "%2B");
    let resp = client.get(&encoded_url).send().map_err(|e| e.to_string())?;
    if !resp.status().is_success() {
        return Err(format!("HTTP {} downloading {}", resp.status(), url));
    }
    let bytes = resp.bytes().map_err(|e| e.to_string())?;
    // Verify it's not an HTML error page
    if bytes.len() < 100 || (bytes.len() < 2000 && bytes.starts_with(b"<html")) {
        return Err(format!("Invalid download (got HTML) for {}", url));
    }
    fs::write(dest, &bytes).map_err(|e| e.to_string())?;
    Ok(())
}

fn extract_natives(jar_path: &PathBuf, natives_dir: &PathBuf) -> Result<(), String> {
    let file = fs::File::open(jar_path).map_err(|e| e.to_string())?;
    let mut archive = zip::ZipArchive::new(file).map_err(|e| e.to_string())?;
    for i in 0..archive.len() {
        let mut entry = archive.by_index(i).map_err(|e| e.to_string())?;
        let name = entry.name().to_string();
        if name.starts_with("META-INF") { continue; }
        if name.ends_with(".dylib") || name.ends_with(".so") || name.ends_with(".jnilib") || name.ends_with(".dll") {
            let out_path = natives_dir.join(&name);
            if !out_path.exists() {
                let mut out = fs::File::create(&out_path).map_err(|e| e.to_string())?;
                io::copy(&mut entry, &mut out).map_err(|e| e.to_string())?;
            }
        }
    }
    Ok(())
}

fn current_os_name() -> &'static str {
    #[cfg(target_os = "macos")]
    { "osx" }
    #[cfg(target_os = "windows")]
    { "windows" }
    #[cfg(target_os = "linux")]
    { "linux" }
}

fn library_allowed(lib: &Library) -> bool {
    let rules = match &lib.rules {
        None => return true,
        Some(r) => r,
    };
    let mut allowed = false;
    for rule in rules {
        let os_match = match &rule.os {
            None => true,
            Some(os) => os.name.as_deref() == Some(current_os_name()),
        };
        if os_match {
            allowed = rule.action == "allow";
        }
    }
    allowed
}

#[derive(Clone)]
struct DownloadTask {
    url: String,
    dest: PathBuf,
    is_native: bool,
}

fn artifact_dest(libraries_dir: &PathBuf, artifact: &Artifact) -> Option<PathBuf> {
    artifact.path.as_ref().map(|p| libraries_dir.join(p))
}

/// Convert Maven coordinate (group:artifact:version) to a path + URL
fn maven_to_path(name: &str) -> Option<String> {
    let parts: Vec<&str> = name.split(':').collect();
    if parts.len() < 3 { return None; }
    let group = parts[0].replace('.', "/");
    let artifact = parts[1];
    let version = parts[2];
    Some(format!("{}/{}/{}/{}-{}.jar", group, artifact, version, artifact, version))
}

fn version_game_dir(mc_version: &str) -> PathBuf {
    game_dir().join(format!("profiles/{}", mc_version))
}

/// Install mods into per-version mods folder.
/// bloom-core only installs on supported versions; Fabric API + performance mods install on all Fabric versions.
fn install_mods(client: &Client, _game_dir: &PathBuf, mc_version: &str) -> Result<(), String> {
    let mods_dir = version_game_dir(mc_version).join("mods");
    fs::create_dir_all(&mods_dir).map_err(|e| e.to_string())?;

    // Versions that bloom-core supports
    // 1.8.9 works on Windows via Legacy Fabric, macOS has native library issues
    #[cfg(target_os = "windows")]
    let pulsar_supported = ["1.21.11", "1.8.9"];
    #[cfg(not(target_os = "windows"))]
    let pulsar_supported = ["1.21.11"];

    // Clean old bloom-core / pulsar-core JARs (including pre-rename bloom-core)
    if let Ok(entries) = fs::read_dir(&mods_dir) {
        for entry in entries.flatten() {
            let name = entry.file_name().to_string_lossy().to_string();
            if (name.starts_with("pulsar-core-") || name.starts_with("bloom-core-")) && name.ends_with(".jar") {
                let _ = fs::remove_file(entry.path());
            }
        }
    }

    // Only install bloom-core on supported versions
    if pulsar_supported.contains(&mc_version) {
        let (jar_name, jar_bytes): (&str, &[u8]) = match mc_version {
            "1.8.9" => ("pulsar-core-1.0.0.jar", include_bytes!("../../pulsar-core-1.8.9/build/libs/pulsar-core-1.0.0.jar")),
            _ => ("pulsar-core-1.2.0.jar", include_bytes!("../../pulsar-core-1.21.11/build/libs/pulsar-core-1.2.0.jar")),
        };
        let pulsar_dest = mods_dir.join(jar_name);
        let _ = fs::write(&pulsar_dest, jar_bytes);
    }

    // Download Fabric API from Modrinth
    let fabric_api_dest = mods_dir.join("fabric-api.jar");
    if !fabric_api_dest.exists() {
        let url = format!(
            "https://api.modrinth.com/v2/project/P7dR8mSH/version?game_versions=[\"{}\"]&loaders=[\"fabric\"]",
            mc_version
        );
        if let Ok(resp) = client.get(&url)
            .header("User-Agent", "pulsar-launcher/1.0")
            .send().and_then(|r| r.text())
        {
            #[derive(Deserialize)]
            struct ModrinthVersion { files: Vec<ModrinthFile> }
            #[derive(Deserialize)]
            struct ModrinthFile { url: String }
            if let Ok(versions) = serde_json::from_str::<Vec<ModrinthVersion>>(&resp) {
                if let Some(ver) = versions.first() {
                    if let Some(file) = ver.files.first() {
                        let _ = download_file(client, &file.url, &fabric_api_dest);
                    }
                }
            }
        }
    }

    // Auto-install popular performance & utility mods from Modrinth
    let mut bundled_mods: Vec<(&str, &str)> = vec![
        ("AANobbMI", "sodium.jar"),         // Sodium (rendering optimization, 2x FPS)
        ("YL57xq9U", "iris.jar"),           // Iris Shaders (shader pack support, works with Sodium)
        ("gvQqBUqZ", "lithium.jar"),        // Lithium (game logic optimization)
        ("NNAgCjsB", "entityculling.jar"),   // Entity Culling (skip rendering hidden entities)
        ("5ZwdcRci", "immediatelyfast.jar"), // ImmediatelyFast (HUD, text, entity rendering)
        ("uXXizFIs", "ferritecore.jar"),     // FerriteCore (reduce RAM usage by 50%)
        ("nmDcB62a", "modernfix.jar"),       // ModernFix (startup, memory, performance fixes)
        ("H8CaAYZC", "starlight.jar"),       // Starlight (rewritten lighting engine)
        ("hvFnDODi", "lazydfu.jar"),         // LazyDFU (faster game startup)
        ("1eAoo2KR", "dynamicfps.jar"),      // Dynamic FPS (reduce FPS when unfocused)
        ("fQEb0iXm", "krypton.jar"),         // Krypton (network stack optimization)
    ];

    // 1.8.9 Bedwars mods (Legacy Fabric)
    if mc_version == "1.8.9" || mc_version == "1.8" {
        // Clear default mods that don't work on legacy
        bundled_mods.clear();
    }

    // Speedrun mods for 1.16.x — full legal speedrun setup
    if mc_version.starts_with("1.16") {
        bundled_mods.push(("jnkd7LkJ", "speedrunigt.jar"));   // SpeedRunIGT (in-game timer)
        bundled_mods.push(("PNEi3GLK", "atum.jar"));           // Atum (auto world reset)
        bundled_mods.push(("tKUU4TXD", "worldpreview.jar"));   // WorldPreview (preview while generating)
        bundled_mods.push(("hvFnDODi", "lazydfu.jar"));        // LazyDFU (faster game startup)
        bundled_mods.push(("H8CaAYZC", "starlight.jar"));      // Starlight (fast lighting engine)
        bundled_mods.push(("fQEb0iXm", "krypton.jar"));        // Krypton (network optimization)
    }
    for (project_id, filename) in &bundled_mods {
        let dest = mods_dir.join(filename);
        if !dest.exists() {
            let url = format!(
                "https://api.modrinth.com/v2/project/{}/version?game_versions=[\"{}\"]&loaders=[\"fabric\"]",
                project_id, mc_version
            );
            if let Ok(resp) = client.get(&url)
                .header("User-Agent", "pulsar-launcher/1.0")
                .send().and_then(|r| r.text())
            {
                #[derive(Deserialize)]
                struct MV { files: Vec<MF> }
                #[derive(Deserialize)]
                struct MF { url: String }
                if let Ok(versions) = serde_json::from_str::<Vec<MV>>(&resp) {
                    if let Some(ver) = versions.first() {
                        if let Some(file) = ver.files.first() {
                            let _ = download_file(client, &file.url, &dest);
                        }
                    }
                }
            }
        }
    }

    Ok(())
}

#[tauri::command]
pub fn get_versions(show_snapshots: Option<bool>, show_betas: Option<bool>) -> Result<Vec<String>, String> {
    let client = Client::new();
    let game_dir = game_dir();
    let manifest_path = game_dir.join("version_manifest.json");
    if let Ok(bytes) = client.get("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json").send().and_then(|r| r.bytes()) {
        let _ = fs::create_dir_all(&game_dir);
        let _ = fs::write(&manifest_path, &bytes);
    }
    let text = fs::read_to_string(&manifest_path).map_err(|e| e.to_string())?;
    let manifest: VersionManifest = serde_json::from_str(&text).map_err(|e| e.to_string())?;
    let snaps = show_snapshots.unwrap_or(false);
    let betas = show_betas.unwrap_or(false);
    let versions: Vec<String> = manifest.versions.iter()
        .filter(|v| {
            v.r#type == "release"
                || (snaps && v.r#type == "snapshot")
                || (betas && (v.r#type == "old_beta" || v.r#type == "old_alpha"))
        })
        .map(|v| v.id.clone())
        .collect();
    Ok(versions)
}

#[tauri::command]
pub fn launch_minecraft(app: AppHandle, version: String, username: Option<String>, uuid: Option<String>, access_token: Option<String>, ram_mb: Option<u32>, java_args: Option<String>) -> Result<(), String> {
    // Run everything in a background thread so we don't block Tauri IPC
    std::thread::spawn(move || {
        match do_launch(&app, &version, username, uuid, access_token, ram_mb, java_args) {
            Ok(()) => {
                let _ = app.emit("launch_done", serde_json::json!({ "success": true }));
            }
            Err(e) => {
                let _ = app.emit("launch_error", serde_json::json!({ "error": format!("{}", e) }));
            }
        }
    });
    Ok(())
}

fn do_launch(app: &AppHandle, version: &str, username: Option<String>, uuid: Option<String>, access_token: Option<String>, ram_mb: Option<u32>, java_args: Option<String>) -> Result<(), String> {
    // Auto-download the right Java version for this MC version
    download_java(app, version)?;

    let client = Client::new();
    let game_dir = game_dir();
    let versions_dir = game_dir.join(format!("versions/{}", version));
    let libraries_dir = game_dir.join("libraries");
    let natives_dir = game_dir.join(format!("natives/{}", version));

    fs::create_dir_all(&versions_dir).map_err(|e| e.to_string())?;
    fs::create_dir_all(&libraries_dir).map_err(|e| e.to_string())?;
    fs::create_dir_all(&natives_dir).map_err(|e| e.to_string())?;

    emit(&app, 5, "Checking version manifest...");

    let manifest_path = game_dir.join("version_manifest.json");
    download_file(&client, "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json", &manifest_path)?;
    let manifest: VersionManifest = serde_json::from_str(
        &fs::read_to_string(&manifest_path).map_err(|e| e.to_string())?
    ).map_err(|e| e.to_string())?;

    emit(&app, 8, "Fetching version metadata...");

    let version_entry = manifest.versions.iter().find(|v| v.id == version)
        .ok_or_else(|| format!("{} not found in manifest", version))?;

    let version_json_path = versions_dir.join(format!("{}.json", version));
    download_file(&client, &version_entry.url, &version_json_path)?;
    let version_json: VersionJson = serde_json::from_str(
        &fs::read_to_string(&version_json_path).map_err(|e| e.to_string())?
    ).map_err(|e| e.to_string())?;

    emit(&app, 10, "Downloading client jar...");
    let client_jar = versions_dir.join(format!("{}.jar", version));
    download_file(&client, &version_json.downloads.client.url, &client_jar)?;

    // --- Fabric loader (optional — skip for unsupported versions) ---
    emit(&app, 12, "Checking Fabric support...");

    let mut fabric_json: Option<FabricVersionJson> = None;
    let mut fabric_classpath: Vec<PathBuf> = Vec::new();

    // Try regular Fabric, fall back to Legacy Fabric on Windows for old versions
    let fabric_meta_url = format!("https://meta.fabricmc.net/v2/versions/loader/{}", version);
    let mut fabric_resp = client.get(&fabric_meta_url).send().and_then(|r| r.text()).unwrap_or_default();
    let mut meta_base = "https://meta.fabricmc.net".to_string();

    // Check if regular Fabric has loaders for this version
    if let Ok(loaders) = serde_json::from_str::<Vec<FabricLoaderVersion>>(&fabric_resp) {
        if loaders.is_empty() {
            // Try Legacy Fabric (works on Windows, not macOS ARM)
            #[cfg(not(all(target_os = "macos", target_arch = "aarch64")))]
            {
                let legacy_url = format!("https://meta.legacyfabric.net/v2/versions/loader/{}", version);
                fabric_resp = client.get(&legacy_url).send().and_then(|r| r.text()).unwrap_or_default();
                meta_base = "https://meta.legacyfabric.net".to_string();
                emit(&app, 13, "Using Legacy Fabric...");
            }
        }
    }

    if let Ok(loaders) = serde_json::from_str::<Vec<FabricLoaderVersion>>(&fabric_resp) {
        if let Some(first) = loaders.first() {
            let loader_version = &first.loader.version;
            let fabric_json_url = format!(
                "{}/v2/versions/loader/{}/{}/profile/json",
                meta_base, version, loader_version
            );
                if let Ok(fj_text) = client.get(&fabric_json_url).send().and_then(|r| r.text()) {
                    if let Ok(fj) = serde_json::from_str::<FabricVersionJson>(&fj_text) {
                        emit(&app, 14, "Downloading Fabric libraries...");
                        for lib in &fj.libraries {
                            if let Some(path) = maven_to_path(&lib.name) {
                                let dest = libraries_dir.join(&path);
                                let url = format!("{}{}", lib.url, path);
                                let _ = download_file(&client, &url, &dest);
                                if dest.exists() { fabric_classpath.push(dest); }
                            }
                        }
                        fabric_json = Some(fj);
                    }
                }
        }
    }

    let has_fabric = fabric_json.is_some();
    if !has_fabric {
        emit(&app, 14, "Fabric not available — launching vanilla...");
    }

    // --- Vanilla libraries ---
    emit(&app, 18, "Checking libraries...");

    let mut tasks: Vec<DownloadTask> = Vec::new();

    for lib in &version_json.libraries {
        if !library_allowed(lib) { continue; }
        if let Some(dl) = &lib.downloads {
            if let Some(artifact) = &dl.artifact {
                if let Some(dest) = artifact_dest(&libraries_dir, artifact) {
                    tasks.push(DownloadTask { url: artifact.url.clone(), dest, is_native: false });
                }
            }
            if let Some(classifiers) = &dl.classifiers {
                let native_key = if cfg!(target_os = "macos") { "osx" }
                    else if cfg!(target_os = "windows") { "windows" }
                    else { "linux" };
                let key = lib.natives.as_ref()
                    .and_then(|n| n.get(native_key))
                    .map(|s| s.as_str())
                    .unwrap_or("");

                #[cfg(target_os = "macos")]
                let candidates = ["natives-macos-arm64", "natives-macos", "natives-osx", key];
                #[cfg(target_os = "windows")]
                let candidates = ["natives-windows", "natives-windows-x86_64", key, ""];
                #[cfg(target_os = "linux")]
                let candidates = ["natives-linux", key, "", ""];
                for candidate in candidates {
                    if let Some(artifact) = classifiers.get(candidate) {
                        if let Some(dest) = artifact_dest(&libraries_dir, artifact) {
                            tasks.push(DownloadTask { url: artifact.url.clone(), dest, is_native: true });
                            break;
                        }
                    }
                }
            }
        }
    }

    // Parallel downloads — 8 threads
    let total = tasks.len();
    let completed = Arc::new(Mutex::new(0usize));
    let errors: Arc<Mutex<Vec<String>>> = Arc::new(Mutex::new(Vec::new()));
    let app_arc = Arc::new(app.clone());
    let natives_dir_arc = Arc::new(natives_dir.clone());

    let chunk_size = (total / 8).max(1);
    let chunks: Vec<Vec<DownloadTask>> = tasks.chunks(chunk_size).map(|c| c.to_vec()).collect();

    let handles: Vec<_> = chunks.into_iter().map(|chunk| {
        let completed = Arc::clone(&completed);
        let errors = Arc::clone(&errors);
        let app_arc = Arc::clone(&app_arc);
        let natives_dir = Arc::clone(&natives_dir_arc);

        thread::spawn(move || {
            let client = Client::new();
            for task in chunk {
                if let Err(e) = download_file(&client, &task.url, &task.dest) {
                    errors.lock().unwrap().push(e);
                    continue;
                }
                if task.is_native {
                    let _ = extract_natives(&task.dest, &natives_dir);
                }
                let mut c = completed.lock().unwrap();
                *c += 1;
                let pct = 20 + (*c * 60 / total) as u32;
                let _ = app_arc.emit("launch_progress", serde_json::json!({
                    "pct": pct,
                    "msg": format!("Downloading libraries... ({}/{})", *c, total)
                }));
            }
        })
    }).collect();

    for h in handles { let _ = h.join(); }

    let errs = errors.lock().unwrap();
    if !errs.is_empty() {
        return Err(format!("Download error: {}", errs[0]));
    }
    drop(errs);

    // Assets
    emit(&app, 82, "Fetching assets index...");
    let assets_dir = game_dir.join("assets");
    fs::create_dir_all(assets_dir.join("indexes")).map_err(|e| e.to_string())?;
    let asset_index_path = assets_dir.join("indexes").join(format!("{}.json", version_json.asset_index.id));
    download_file(&client, &version_json.asset_index.url, &asset_index_path)?;

    #[derive(Deserialize)]
    struct AssetIndexFile { objects: HashMap<String, AssetObject> }
    #[derive(Deserialize)]
    struct AssetObject { hash: String }

    let index_text = fs::read_to_string(&asset_index_path).map_err(|e| e.to_string())?;
    let asset_index: AssetIndexFile = serde_json::from_str(&index_text).map_err(|e| e.to_string())?;

    let objects_dir = assets_dir.join("objects");
    let asset_tasks: Vec<(String, PathBuf)> = asset_index.objects.values()
        .map(|obj| {
            let prefix = &obj.hash[..2];
            let dest = objects_dir.join(prefix).join(&obj.hash);
            let url = format!("https://resources.download.minecraft.net/{}/{}", prefix, obj.hash);
            (url, dest)
        })
        .filter(|(_, dest)| !dest.exists())
        .collect();

    let asset_total = asset_tasks.len();
    if asset_total > 0 {
        emit(&app, 83, &format!("Downloading {} assets...", asset_total));
        let asset_completed = Arc::new(Mutex::new(0usize));
        let asset_errors: Arc<Mutex<Vec<String>>> = Arc::new(Mutex::new(Vec::new()));
        let app_arc2 = Arc::new(app.clone());

        let chunk_size = (asset_total / 16).max(1);
        let chunks: Vec<Vec<(String, PathBuf)>> = asset_tasks.chunks(chunk_size).map(|c| c.to_vec()).collect();

        let handles: Vec<_> = chunks.into_iter().map(|chunk| {
            let completed = Arc::clone(&asset_completed);
            let errors = Arc::clone(&asset_errors);
            let app_arc = Arc::clone(&app_arc2);
            let total = asset_total;

            thread::spawn(move || {
                let client = Client::new();
                for (url, dest) in chunk {
                    if let Some(parent) = dest.parent() {
                        let _ = fs::create_dir_all(parent);
                    }
                    if let Err(e) = download_file(&client, &url, &dest) {
                        errors.lock().unwrap().push(e);
                        continue;
                    }
                    let mut c = completed.lock().unwrap();
                    *c += 1;
                    if *c % 50 == 0 || *c == total {
                        let pct = 83 + (*c * 10 / total) as u32;
                        let _ = app_arc.emit("launch_progress", serde_json::json!({
                            "pct": pct,
                            "msg": format!("Downloading assets... ({}/{})", *c, total)
                        }));
                    }
                }
            })
        }).collect();

        for h in handles { let _ = h.join(); }
    }

    // Install mods only if Fabric is available
    if has_fabric {
        emit(&app, 94, "Installing Pulsar mods...");
        install_mods(&client, &game_dir, &version)?;
    }

    // Build classpath: Fabric libs + vanilla libs + client jar
    emit(&app, 96, "Building classpath...");
    let mut classpath_entries: Vec<PathBuf> = Vec::new();

    // Fabric libraries first
    for entry in &fabric_classpath {
        if entry.exists() { classpath_entries.push(entry.clone()); }
    }

    // Vanilla libraries
    for lib in &version_json.libraries {
        if !library_allowed(lib) { continue; }
        if let Some(dl) = &lib.downloads {
            if let Some(artifact) = &dl.artifact {
                if let Some(dest) = artifact_dest(&libraries_dir, artifact) {
                    if dest.exists() { classpath_entries.push(dest); }
                }
            }
        }
    }
    classpath_entries.push(client_jar);

    let separator = if cfg!(target_os = "windows") { ";" } else { ":" };
    let classpath = classpath_entries.iter()
        .map(|p| p.to_string_lossy().to_string())
        .collect::<Vec<_>>()
        .join(separator);

    emit(&app, 99, "Launching Pulsar Client...");

    let ver_game_dir = version_game_dir(&version);
    fs::create_dir_all(&ver_game_dir).map_err(|e| e.to_string())?;
    let game_dir_str = ver_game_dir.to_string_lossy().to_string();
    let assets_dir_str = assets_dir.to_string_lossy().to_string();
    let natives_dir_str = natives_dir.to_string_lossy().to_string();

    // Find Java
    let java_cmd = find_java(version);

    // Install Pulsar Agent for 1.8.9 (Java agent injection — no mod loader needed)
    let is_agent_version = version == "1.8.9" || version == "1.8";
    if is_agent_version {
        let agent_dir = game_dir.join("agent");
        fs::create_dir_all(&agent_dir).map_err(|e| e.to_string())?;
        let agent_dest = agent_dir.join("pulsar-agent.jar");
        let agent_jar: &[u8] = include_bytes!("../../pulsar-agent/build/libs/pulsar-agent-1.0.0.jar");
        let _ = fs::write(&agent_dest, agent_jar);
    }

    // Build JVM args
    let mut jvm_args: Vec<String> = Vec::new();

    // Add Java agent for 1.8.9 (Lunar Client-style injection)
    if is_agent_version {
        let agent_path = game_dir.join("agent/pulsar-agent.jar");
        jvm_args.push(format!("-javaagent:{}", agent_path.to_string_lossy()));
    }
    // -XstartOnFirstThread is needed for LWJGL 3 (1.13+) on macOS but breaks LWJGL 2 (1.12.2 and below)
    #[cfg(target_os = "macos")]
    {
        let parts: Vec<&str> = version.split('.').collect();
        let minor: u32 = parts.get(1).and_then(|s| s.parse().ok()).unwrap_or(0);
        if minor >= 13 {
            jvm_args.push("-XstartOnFirstThread".to_string());
        }
    }
    // Apply Fabric JVM arguments if available
    if let Some(ref fj) = fabric_json {
        for arg in &fj.arguments.jvm {
            jvm_args.push(arg.trim().to_string());
        }
    }
    let ram = ram_mb.unwrap_or(2048);
    jvm_args.push(format!("-Xmx{}M", ram));
    jvm_args.push(format!("-Xms{}M", std::cmp::min(512, ram)));
    // Custom JVM args from settings
    if let Some(ref extra) = java_args {
        for arg in extra.split_whitespace() {
            if !arg.is_empty() { jvm_args.push(arg.to_string()); }
        }
    }
    // GC optimization for smoother FPS
    jvm_args.push("-XX:+UseG1GC".to_string());
    jvm_args.push("-XX:MaxGCPauseMillis=20".to_string());
    jvm_args.push("-XX:G1HeapRegionSize=32M".to_string());
    jvm_args.push("-XX:+UnlockExperimentalVMOptions".to_string());
    jvm_args.push("-XX:G1NewSizePercent=20".to_string());
    jvm_args.push("-XX:G1ReservePercent=20".to_string());
    jvm_args.push("-XX:+DisableExplicitGC".to_string());
    jvm_args.push("-XX:+AlwaysPreTouch".to_string());
    jvm_args.push("-XX:+ParallelRefProcEnabled".to_string());
    jvm_args.push(format!("-Djava.library.path={}", natives_dir_str));
    jvm_args.push(format!("-Djna.tmpdir={}", natives_dir_str));
    jvm_args.push(format!("-Dorg.lwjgl.system.SharedLibraryExtractPath={}", natives_dir_str));
    jvm_args.push(format!("-Dio.netty.native.workdir={}", natives_dir_str));
    jvm_args.push("-Dminecraft.launcher.brand=pulsar-launcher".to_string());
    jvm_args.push("-Dminecraft.launcher.version=1.2.0".to_string());
    jvm_args.push("-cp".to_string());
    jvm_args.push(classpath);
    // Use Fabric main class if available, otherwise vanilla
    let main_class = if let Some(ref fj) = fabric_json {
        fj.main_class.clone()
    } else {
        version_json.main_class.clone()
    };
    jvm_args.push(main_class);
    let user_type = if access_token.is_some() { "msa" } else { "offline" };

    // Extract XUID from the MC access token JWT payload (base64url decode)
    let xuid = access_token.as_deref().and_then(|token| {
        let parts: Vec<&str> = token.split('.').collect();
        if parts.len() < 2 { return None; }
        let payload = parts[1];
        // base64url to standard base64
        let mut b64 = payload.replace('-', "+").replace('_', "/");
        while b64.len() % 4 != 0 { b64.push('='); }
        // Simple base64 decode using a lookup table
        fn b64_decode(input: &str) -> Option<Vec<u8>> {
            let table = b"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
            let mut out = Vec::new();
            let chars: Vec<u8> = input.bytes().filter(|&b| b != b'=').collect();
            for chunk in chars.chunks(4) {
                let vals: Vec<u8> = chunk.iter().filter_map(|&c| table.iter().position(|&t| t == c).map(|p| p as u8)).collect();
                if vals.len() >= 2 { out.push((vals[0] << 2) | (vals.get(1).unwrap_or(&0) >> 4)); }
                if vals.len() >= 3 { out.push((vals[1] << 4) | (vals.get(2).unwrap_or(&0) >> 2)); }
                if vals.len() >= 4 { out.push((vals[2] << 6) | vals[3]); }
            }
            Some(out)
        }
        let decoded = b64_decode(&b64)?;
        let json: serde_json::Value = serde_json::from_slice(&decoded).ok()?;
        json["xuid"].as_str().map(|s| s.to_string())
    }).unwrap_or_default();

    // Mojang profile API returns UUID without dashes, but Minecraft expects dashed format
    let raw_uuid = uuid.as_deref().unwrap_or("00000000000000000000000000000000");
    let dashed_uuid = if raw_uuid.contains('-') {
        raw_uuid.to_string()
    } else if raw_uuid.len() == 32 {
        format!("{}-{}-{}-{}-{}",
            &raw_uuid[0..8], &raw_uuid[8..12], &raw_uuid[12..16],
            &raw_uuid[16..20], &raw_uuid[20..32])
    } else {
        raw_uuid.to_string()
    };

    jvm_args.push("--username".to_string()); jvm_args.push(username.as_deref().unwrap_or("Player").to_string());
    jvm_args.push("--version".to_string()); jvm_args.push(version.to_string());
    jvm_args.push("--gameDir".to_string()); jvm_args.push(game_dir_str);
    jvm_args.push("--assetsDir".to_string()); jvm_args.push(assets_dir_str);
    jvm_args.push("--assetIndex".to_string()); jvm_args.push(version_json.asset_index.id.clone());
    jvm_args.push("--uuid".to_string()); jvm_args.push(dashed_uuid);
    jvm_args.push("--accessToken".to_string()); jvm_args.push(access_token.as_deref().unwrap_or("0").to_string());
    jvm_args.push("--userType".to_string()); jvm_args.push(user_type.to_string());
    jvm_args.push("--versionType".to_string()); jvm_args.push("release".to_string());
    if !xuid.is_empty() {
        jvm_args.push("--xuid".to_string()); jvm_args.push(xuid);
    }
    jvm_args.push("--clientId".to_string()); jvm_args.push("ef9218b2-b1ed-47c5-ab68-2554ef79b244".to_string());
    jvm_args.push("--userProperties".to_string()); jvm_args.push("{}".to_string());

    // Open log file in the game directory so we can see what's happening
    let log_path = ver_game_dir.join("pulsar-game.log");
    let log_file = std::fs::File::create(&log_path).ok();

    // Write the launch arguments to the log header (mask the access token for safety)
    if let Some(ref f) = log_file {
        use std::io::Write;
        let mut writer = std::io::BufWriter::new(f);
        let _ = writeln!(writer, "=== Pulsar Launcher v1.2.0 ===");
        let _ = writeln!(writer, "Java: {}", java_cmd);
        let _ = writeln!(writer, "Args:");
        let mut mask_next = false;
        for arg in &jvm_args {
            if mask_next {
                let _ = writeln!(writer, "  [TOKEN HIDDEN, {} chars]", arg.len());
                mask_next = false;
            } else {
                let _ = writeln!(writer, "  {}", arg);
                if arg == "--accessToken" { mask_next = true; }
            }
        }
        let _ = writeln!(writer, "=== Game output ===");
        let _ = writer.flush();
    }

    let mut child = Command::new(&java_cmd)
        .args(&jvm_args)
        .stdout(if let Some(ref f) = log_file { std::process::Stdio::from(f.try_clone().unwrap()) } else { std::process::Stdio::piped() })
        .stderr(if let Some(ref f) = log_file { std::process::Stdio::from(f.try_clone().unwrap()) } else { std::process::Stdio::piped() })
        .spawn()
        .map_err(|e| format!("Failed to start Java: {}", e))?;

    // Wait a moment and check if the process crashed immediately
    thread::sleep(std::time::Duration::from_secs(3));
    match child.try_wait() {
        Ok(Some(exit)) if !exit.success() => {
            // Read the log file we just wrote to
            let log_contents = std::fs::read_to_string(&log_path).unwrap_or_default();
            let last_lines: String = log_contents.lines().rev().take(15).collect::<Vec<_>>().into_iter().rev().collect::<Vec<_>>().join("\n");
            return Err(format!("Game crashed (exit {}): {}", exit.code().unwrap_or(-1), last_lines));
        }
        Ok(Some(_)) => {}
        Ok(None) => {}
        Err(e) => return Err(format!("Failed to check game process: {}", e)),
    }

    Ok(())
}

#[tauri::command]
pub fn install_mod(project_id: String, mc_version: String) -> Result<String, String> {
    let client = Client::new();
    let mods_dir = version_game_dir(&mc_version).join("mods");
    fs::create_dir_all(&mods_dir).map_err(|e| e.to_string())?;

    let url = format!(
        "https://api.modrinth.com/v2/project/{}/version?game_versions=[\"{}\"]&loaders=[\"fabric\"]",
        project_id, mc_version
    );
    let resp = client.get(&url)
        .header("User-Agent", "pulsar-launcher/1.0")
        .send().map_err(|e| e.to_string())?
        .text().map_err(|e| e.to_string())?;

    #[derive(Deserialize)]
    struct ModVersion { files: Vec<ModFile> }
    #[derive(Deserialize)]
    struct ModFile { url: String, filename: String }

    let versions: Vec<ModVersion> = serde_json::from_str(&resp).map_err(|e| e.to_string())?;
    let ver = versions.first().ok_or("No version found for this Minecraft version")?;
    let file = ver.files.first().ok_or("No file found")?;

    let dest = mods_dir.join(&file.filename);
    if !dest.exists() {
        let bytes = client.get(&file.url)
            .send().map_err(|e| e.to_string())?
            .bytes().map_err(|e| e.to_string())?;
        fs::write(&dest, &bytes).map_err(|e| e.to_string())?;
    }

    Ok(file.filename.clone())
}

#[tauri::command]
pub fn uninstall_mod(filename: String, mc_version: String) -> Result<(), String> {
    let path = version_game_dir(&mc_version).join("mods").join(&filename);
    if path.exists() {
        fs::remove_file(&path).map_err(|e| e.to_string())?;
    }
    Ok(())
}

#[tauri::command]
pub fn list_installed_mods(mc_version: String) -> Result<Vec<String>, String> {
    let mods_dir = version_game_dir(&mc_version).join("mods");
    if !mods_dir.exists() { return Ok(vec![]); }
    let mut mods = Vec::new();
    for entry in fs::read_dir(&mods_dir).map_err(|e| e.to_string())? {
        let entry = entry.map_err(|e| e.to_string())?;
        let name = entry.file_name().to_string_lossy().to_string();
        if name.ends_with(".jar") {
            mods.push(name);
        }
    }
    Ok(mods)
}

#[tauri::command]
pub fn get_mod_hashes(mc_version: String) -> Result<HashMap<String, String>, String> {
    let mods_dir = version_game_dir(&mc_version).join("mods");
    if !mods_dir.exists() { return Ok(HashMap::new()); }
    let mut result = HashMap::new();
    for entry in fs::read_dir(&mods_dir).map_err(|e| e.to_string())? {
        let entry = entry.map_err(|e| e.to_string())?;
        let name = entry.file_name().to_string_lossy().to_string();
        if name.ends_with(".jar") {
            let bytes = fs::read(entry.path()).map_err(|e| e.to_string())?;
            let hash = Sha1::digest(&bytes);
            result.insert(name, format!("{:x}", hash));
        }
    }
    Ok(result)
}

#[tauri::command]
pub fn install_resourcepack(project_id: String, mc_version: String) -> Result<String, String> {
    let client = Client::new();
    let packs_dir = version_game_dir(&mc_version).join("resourcepacks");
    fs::create_dir_all(&packs_dir).map_err(|e| e.to_string())?;

    let url = format!(
        "https://api.modrinth.com/v2/project/{}/version?game_versions=[\"{}\"]",
        project_id, mc_version
    );
    let resp = client.get(&url)
        .header("User-Agent", "pulsar-launcher/1.0")
        .send().map_err(|e| e.to_string())?
        .text().map_err(|e| e.to_string())?;

    #[derive(Deserialize)]
    struct PackVersion { files: Vec<PackFile> }
    #[derive(Deserialize)]
    struct PackFile { url: String, filename: String }

    let versions: Vec<PackVersion> = serde_json::from_str(&resp).map_err(|e| e.to_string())?;
    let ver = versions.first().ok_or("No version found for this Minecraft version")?;
    let file = ver.files.first().ok_or("No file found")?;

    let dest = packs_dir.join(&file.filename);
    if !dest.exists() {
        let bytes = client.get(&file.url)
            .send().map_err(|e| e.to_string())?
            .bytes().map_err(|e| e.to_string())?;
        fs::write(&dest, &bytes).map_err(|e| e.to_string())?;
    }

    Ok(file.filename.clone())
}

fn cosmetics_path() -> PathBuf {
    game_dir().join("pulsar-cosmetics.json")
}

#[tauri::command]
pub fn get_cosmetics() -> Result<String, String> {
    let path = cosmetics_path();
    if path.exists() {
        fs::read_to_string(&path).map_err(|e| e.to_string())
    } else {
        Ok(r#"{"v":2,"points":500,"purchased":[],"equipped":{}}"#.to_string())
    }
}

#[tauri::command]
pub fn save_cosmetics(data: String) -> Result<(), String> {
    let path = cosmetics_path();
    if let Some(parent) = path.parent() {
        let _ = fs::create_dir_all(parent);
    }
    fs::write(&path, &data).map_err(|e| e.to_string())
}
