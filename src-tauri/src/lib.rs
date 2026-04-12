mod launcher;
mod auth;
use auth::AccountState;
use std::sync::Mutex;

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
            launcher::install_resourcepack,
            launcher::get_cosmetics,
            launcher::save_cosmetics,
            auth::start_microsoft_login,
            auth::get_account,
            auth::save_account,
            auth::refresh_account,
            auth::logout,
        ])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
