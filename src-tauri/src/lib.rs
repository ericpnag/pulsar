mod launcher;
mod auth;
use auth::AccountState;
use std::sync::Mutex;

#[tauri::command]
fn open_browser(url: String) -> Result<(), String> {
    #[cfg(target_os = "macos")]
    std::process::Command::new("open").arg(&url).spawn().map_err(|e| e.to_string())?;
    #[cfg(target_os = "windows")]
    std::process::Command::new("cmd").args(["/C", "start", &url]).spawn().map_err(|e| e.to_string())?;
    #[cfg(target_os = "linux")]
    std::process::Command::new("xdg-open").arg(&url).spawn().map_err(|e| e.to_string())?;
    Ok(())
}

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    use tauri::Manager;
    tauri::Builder::default()
        .plugin(tauri_plugin_opener::init())
        .setup(|_app| {
            Ok(())
        })
        .manage(AccountState(Mutex::new(None)))
        .invoke_handler(tauri::generate_handler![
            launcher::launch_minecraft,
            launcher::get_versions,
            launcher::install_mod,
            launcher::uninstall_mod,
            launcher::list_installed_mods,
            launcher::get_mod_hashes,
            launcher::install_resourcepack,
            launcher::get_cosmetics,
            launcher::save_cosmetics,
            auth::start_microsoft_login,
            auth::get_account,
            auth::save_account,
            auth::refresh_account,
            auth::logout,
            open_browser,
        ])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
