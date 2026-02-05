use tauri::{
    plugin::{Builder, TauriPlugin},
    Runtime,
};



#[cfg(target_os = "android")]
pub fn init<R: Runtime>() -> TauriPlugin<R> {
    Builder::new("printer")
        .setup(|app, api| {
            api.register_android_plugin("com.myongyop.kikit", "PrinterPlugin")?;
            Ok(())
        })
        .build()
}

#[cfg(not(target_os = "android"))]
pub fn init<R: Runtime>() -> TauriPlugin<R> {
    Builder::new("printer")
        .build()
}
