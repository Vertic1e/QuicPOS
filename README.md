# QuicPOS - Modern Android Point of Sale (POS)

QuicPOS is a modern, offline-first Android Point of Sale system built with Jetpack Compose, Material 3, Clean Architecture, and Kotlin Coroutines. Designed for retail, restaurants, and convenience stores.

---

## 📲 Direct APK Installation (For POS Terminals / Tablets / Phones)

You can find the ready-to-install APK in the [`release-apk/`](./release-apk/QuicPOS.apk) directory.

### Quick Install on POS Devices:
1. **Via Browser / Direct Download**:
   - Download [`release-apk/QuicPOS.apk`](./release-apk/QuicPOS.apk) directly on your Android POS terminal or tablet.
   - Tap the downloaded `.apk` file and grant "Install unknown apps" permission to install.
2. **Via USB Flash Drive**:
   - Copy `QuicPOS.apk` onto a USB drive.
   - Insert into the POS terminal's USB port, open File Manager, and install.
3. **Via ADB (Command Line)**:
   ```bash
   adb install -r release-apk/QuicPOS.apk
   ```

---

## ✨ Key Features

### 🛒 1. Sales & Catalog Management
- **Dynamic Grid Sizing**: Configurable **2 Columns**, **3 Columns**, **4 Columns**, or Auto-adaptive grid to maximize visibility.
- **Accessibility & Big Font Scaling**: Font size slider scaling up to **180%** to fill the item tiles.
- **Dual Currency Support**: Real-time dual currency display (e.g. USD and KHR ៛) with live exchange rate conversion.
- **Barcode & QR Scanning**: Integrated camera-based barcode scanning for rapid item lookup.
- **Favorites & Categories**: Quick category filtering and favorites ribbon.

### 🖨️ 2. Universal Thermal Printer Support
- **Built-in POS Printers**: Native ESC/POS printing support for integrated thermal printers (Sunmi, Telpo, Android POS terminals).
- **Bluetooth Thermal Printers**: Wireless ESC/POS SPP connection for portable 58mm and 80mm printers.
- **Network Wi-Fi / LAN Printers**: Direct raw TCP socket printing on port 9100.
- **Receipt Header Logo**: Automatic image rasterization (`GS v 0` ESC/POS raster bit image) to print your business logo on receipts.
- **Receipt Preview**: Live visual receipt paper preview before printing.

### 📊 3. Back Office & Owner Analytics
- **6-Digit PIN Authentication**: Owner security lock protecting store analytics and sensitive settings (Default PIN: `000000`).
- **Dashboard Gauges**: Circular arc progress gauges for Total Receipts, Net Sales, and Average Sale with percentage comparisons.
- **Hourly Sales Chart**: Interactive 24-hour vertical bar chart showing sales velocity throughout the day.
- **Top Items Sold**: Real-time breakdown of item quantities and revenue generation.

### 🎨 4. Theme & Customization
- **Multi-Theme Support**:
  - **Night Blue** (Default Sleek POS Theme)
  - **Dark Green**
  - **Light Theme**
  - **AMOLED Black**

---

## 🛠️ Tech Stack & Architecture

- **UI**: 100% Jetpack Compose + Material Design 3
- **Architecture**: MVVM + Clean Architecture (Domain, Data, UI)
- **Dependency Injection**: Dagger Hilt
- **Local Database**: Room SQLite (WAL mode enabled)
- **Asynchronous**: Kotlin Coroutines & Flow (Reactive MVI/StateFlow)
- **Image Loading**: Coil 2.x
- **Target SDK**: Android 15 (API 35) | Min SDK: Android 8.0 (API 26)

---

## 🏗️ Build from Source

```bash
git clone https://github.com/Vertic1e/QuicPOS.git
cd QuicPOS
./gradlew assembleDebug
```
The generated APK will be at `app/build/outputs/apk/debug/app-debug.apk`.
