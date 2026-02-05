# kiKit - Web Kiosk Wrapper

kiKit is a high-performance kiosk application wrapper built with **Tauri v2** and **Next.js**. It turns a web application into a robust, native-feeling kiosk interface, specifically targeted for **Android tablets** (e.g., iMuz K11).

## Key Features

- **Silent Printing**: Direct thermal printing via USB/Bluetooth without system dialogs (0.5s latency).
- **Hot Updates**: Update the UI/Logic via web deployment without reinstalling the APK.
- **Cost Efficiency**: Optimized for affordable Android tablets.
- **Kiosk Mode**: Features pinning, auto-boot, and wake lock.

## Tech Stack

- **Frontend**: Next.js (React) - Static Export via `next export`
- **Core**: Rust (Tauri v2)
- **Native**: Kotlin (Android Plugin for hardware access)
- **Protocol**: ESC/POS

## Getting Started

### Prerequisites

- Node.js & npm
- Rust & Cargo
- Android Studio & SDK (for Android build)
- Java JDK 17+

### Development

1. Install dependencies:
   ```bash
   npm install
   ```

2. Run in development mode:
   ```bash
   npm run tauri dev
   ```

3. Build for Android:
   ```bash
   npm run tauri android init
   npm run tauri android build
   ```

## License

MIT
