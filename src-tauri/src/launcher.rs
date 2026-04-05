use std::fs;
use std::io;
use std::path::PathBuf;
use std::process::Command;
use std::collections::HashMap;
use std::sync::{Arc, Mutex};
use std::thread;
use reqwest::blocking::Client;
use serde::Deserialize;
use tauri::{AppHandle, Emitter};

fn game_dir() -> PathBuf {
    #[cfg(target_os = "macos")]
    {
        let home = std::env::var("HOME").unwrap_or_else(|_| ".".to_string());
        PathBuf::from(home).join("Library/Application Support/bloom")
    }
    #[cfg(target_os = "windows")]
    {
        let appdata = std::env::var("APPDATA").unwrap_or_else(|_| ".".to_string());
        PathBuf::from(appdata).join("bloom")
    }
    #[cfg(target_os = "linux")]
    {
        let home = std::env::var("HOME").unwrap_or_else(|_| ".".to_string());
        PathBuf::from(home).join(".bloom")
    }
}

fn emit(app: &AppHandle, pct: u32, msg: &str) {
    let _ = app.emit("launch_progress", serde_json::json!({ "pct": pct, "msg": msg }));
}

fn find_java() -> String {
    // Check JAVA_HOME first
    if let Ok(java_home) = std::env::var("JAVA_HOME") {
        let bin = PathBuf::from(&java_home).join("bin").join(if cfg!(target_os = "windows") { "java.exe" } else { "java" });
        if bin.exists() {
            return bin.to_string_lossy().to_string();
        }
    }
    // Platform defaults
    #[cfg(target_os = "macos")]
    { "/usr/bin/java".to_string() }
    #[cfg(target_os = "windows")]
    { "java".to_string() }
    #[cfg(target_os = "linux")]
    { "java".to_string() }
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
    let bytes = client.get(url).send().map_err(|e| e.to_string())?.bytes().map_err(|e| e.to_string())?;
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

/// Install bloom-core.jar and Fabric API into per-version mods folder
fn install_mods(client: &Client, _game_dir: &PathBuf, mc_version: &str) -> Result<(), String> {
    let mods_dir = version_game_dir(mc_version).join("mods");
    fs::create_dir_all(&mods_dir).map_err(|e| e.to_string())?;

    // Embed bloom-core JARs directly in binary
    let bloom_dest = mods_dir.join("bloom-core-1.0.0.jar");
    let bloom_jar: &[u8] = match mc_version {
        "1.21.11" => include_bytes!("../../bloom-core-1.21.11/build/libs/bloom-core-1.0.0.jar"),
        _ => include_bytes!("../../bloom-core/build/libs/bloom-core-1.0.0.jar"),
    };
    let _ = fs::write(&bloom_dest, bloom_jar);

    // Download Fabric API from Modrinth
    let fabric_api_dest = mods_dir.join("fabric-api.jar");
    if !fabric_api_dest.exists() {
        // Get latest Fabric API version for this MC version from Modrinth
        let url = format!(
            "https://api.modrinth.com/v2/project/P7dR8mSH/version?game_versions=[\"{}\"]&loaders=[\"fabric\"]",
            mc_version
        );
        let resp = client.get(&url)
            .header("User-Agent", "bloom-launcher/1.0")
            .send().map_err(|e| e.to_string())?
            .text().map_err(|e| e.to_string())?;

        #[derive(Deserialize)]
        struct ModrinthVersion { files: Vec<ModrinthFile> }
        #[derive(Deserialize)]
        struct ModrinthFile { url: String }

        let versions: Vec<ModrinthVersion> = serde_json::from_str(&resp).map_err(|e| e.to_string())?;
        if let Some(ver) = versions.first() {
            if let Some(file) = ver.files.first() {
                download_file(client, &file.url, &fabric_api_dest)?;
            }
        }
    }

    Ok(())
}

#[tauri::command]
pub fn get_versions() -> Result<Vec<String>, String> {
    let client = Client::new();
    let game_dir = game_dir();
    let manifest_path = game_dir.join("version_manifest.json");
    if let Ok(bytes) = client.get("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json").send().and_then(|r| r.bytes()) {
        let _ = fs::create_dir_all(&game_dir);
        let _ = fs::write(&manifest_path, &bytes);
    }
    let text = fs::read_to_string(&manifest_path).map_err(|e| e.to_string())?;
    let manifest: VersionManifest = serde_json::from_str(&text).map_err(|e| e.to_string())?;
    let versions: Vec<String> = manifest.versions.iter()
        .filter(|v| v.r#type == "release" && v.id.starts_with("1.21"))
        .map(|v| v.id.clone())
        .collect();
    Ok(versions)
}

#[tauri::command]
pub fn launch_minecraft(app: AppHandle, version: String, username: Option<String>, uuid: Option<String>, access_token: Option<String>) -> Result<(), String> {
    // Run everything in a background thread so we don't block Tauri IPC
    std::thread::spawn(move || {
        if let Err(e) = do_launch(&app, &version, username, uuid, access_token) {
            let _ = app.emit("launch_progress", serde_json::json!({ "pct": 0, "msg": format!("Error: {}", e) }));
        }
    });
    Ok(())
}

fn do_launch(app: &AppHandle, version: &str, username: Option<String>, uuid: Option<String>, access_token: Option<String>) -> Result<(), String> {
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

    // --- Fabric loader ---
    emit(&app, 12, "Fetching Fabric loader...");

    let fabric_meta_url = format!(
        "https://meta.fabricmc.net/v2/versions/loader/{}",
        version
    );
    let fabric_meta_text = client.get(&fabric_meta_url)
        .send().map_err(|e| e.to_string())?
        .text().map_err(|e| e.to_string())?;
    let fabric_loaders: Vec<FabricLoaderVersion> = serde_json::from_str(&fabric_meta_text)
        .map_err(|e| format!("Fabric meta parse: {}", e))?;
    let loader_version = fabric_loaders.first()
        .ok_or("No Fabric loader found for this version")?
        .loader.version.clone();

    let fabric_json_url = format!(
        "https://meta.fabricmc.net/v2/versions/loader/{}/{}/profile/json",
        version, loader_version
    );
    let fabric_json_text = client.get(&fabric_json_url)
        .send().map_err(|e| e.to_string())?
        .text().map_err(|e| e.to_string())?;
    let fabric_json: FabricVersionJson = serde_json::from_str(&fabric_json_text)
        .map_err(|e| format!("Fabric JSON parse: {}", e))?;

    // Download Fabric libraries
    emit(&app, 14, "Downloading Fabric libraries...");
    let mut fabric_classpath: Vec<PathBuf> = Vec::new();
    for lib in &fabric_json.libraries {
        if let Some(path) = maven_to_path(&lib.name) {
            let dest = libraries_dir.join(&path);
            let url = format!("{}{}", lib.url, path);
            download_file(&client, &url, &dest)?;
            fabric_classpath.push(dest);
        }
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

    // Install mods (bloom-core + Fabric API)
    emit(&app, 94, "Installing Bloom mods...");
    install_mods(&client, &game_dir, &version)?;

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

    emit(&app, 99, "Launching Bloom Client...");

    let ver_game_dir = version_game_dir(&version);
    fs::create_dir_all(&ver_game_dir).map_err(|e| e.to_string())?;
    let game_dir_str = ver_game_dir.to_string_lossy().to_string();
    let assets_dir_str = assets_dir.to_string_lossy().to_string();
    let natives_dir_str = natives_dir.to_string_lossy().to_string();

    // Find Java
    let java_cmd = find_java();

    // Build JVM args
    let mut jvm_args: Vec<String> = Vec::new();
    #[cfg(target_os = "macos")]
    jvm_args.push("-XstartOnFirstThread".to_string());
    jvm_args.push("-Xmx2048M".to_string());
    jvm_args.push("-Xms512M".to_string());
    jvm_args.push(format!("-Djava.library.path={}", natives_dir_str));
    jvm_args.push("-cp".to_string());
    jvm_args.push(classpath);
    jvm_args.push(fabric_json.main_class.clone());
    let user_type = if access_token.is_some() { "msa" } else { "offline" };
    jvm_args.push("--username".to_string()); jvm_args.push(username.as_deref().unwrap_or("Player").to_string());
    jvm_args.push("--version".to_string()); jvm_args.push(version.to_string());
    jvm_args.push("--gameDir".to_string()); jvm_args.push(game_dir_str);
    jvm_args.push("--assetsDir".to_string()); jvm_args.push(assets_dir_str);
    jvm_args.push("--assetIndex".to_string()); jvm_args.push(version_json.asset_index.id.clone());
    jvm_args.push("--uuid".to_string()); jvm_args.push(uuid.as_deref().unwrap_or("00000000-0000-0000-0000-000000000000").to_string());
    jvm_args.push("--accessToken".to_string()); jvm_args.push(access_token.as_deref().unwrap_or("0").to_string());
    jvm_args.push("--userType".to_string()); jvm_args.push(user_type.to_string());
    jvm_args.push("--versionType".to_string()); jvm_args.push("release".to_string());

    Command::new(&java_cmd)
        .args(&jvm_args)
        .spawn()
        .map_err(|e| e.to_string())?;

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
        .header("User-Agent", "bloom-launcher/1.0")
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
pub fn install_resourcepack(project_id: String, mc_version: String) -> Result<String, String> {
    let client = Client::new();
    let packs_dir = game_dir().join("resourcepacks");
    fs::create_dir_all(&packs_dir).map_err(|e| e.to_string())?;

    let url = format!(
        "https://api.modrinth.com/v2/project/{}/version?game_versions=[\"{}\"]",
        project_id, mc_version
    );
    let resp = client.get(&url)
        .header("User-Agent", "bloom-launcher/1.0")
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
