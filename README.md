# DigitalPause 🛑

<div align="center">

### *"Pause. Think. Use Intentionally."*

A mindful, privacy-first screen time companion and doomscrolling blocker for Android.  
**100% Offline • No Accounts • Zero Tracking • Open Source**

---

[![Latest Release](https://img.shields.io/github/v/release/Naseem2917/DigitalPause?label=Latest%20Release&color=4F46E5&style=for-the-badge)](https://github.com/Naseem2917/DigitalPause/releases/latest)
[![Download APK](https://img.shields.io/badge/Download-APK-0D9488?style=for-the-badge&logo=android&logoColor=white)](https://github.com/Naseem2917/DigitalPause/releases/latest)
[![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)](LICENSE)

</div>

---

## 📥 Download APK (Direct Release)

> 🚀 **You do not need Google Play Store!** You can download the latest installable APK file directly from GitHub Releases.

<div align="center">

### 👉 [**Click Here to Download Latest APK (v1.0.0)**](https://github.com/Naseem2917/DigitalPause/releases/latest) 👈

#### 📦 [Browse All GitHub Releases & Versions](https://github.com/Naseem2917/DigitalPause/releases)

</div>

### 📲 How to Download & Install on Your Phone:

1. **Download APK**:
   - Tap the **[Latest Release](https://github.com/Naseem2917/DigitalPause/releases/latest)** link above.
   - Scroll down to the **Assets** section at the bottom of the release notes.
   - Tap on `DigitalPause.apk` (or `app-debug.apk` / `app-release.apk`) to download.
2. **Allow Installation from Unknown Sources**:
   - If your browser (Chrome/Brave/Firefox) asks *"File might be harmful"* or *"For your security, your phone is not allowed to install unknown apps"*, tap **Settings** and enable **"Allow from this source"**.
3. **Install & Launch**:
   - Tap **Install** and open **DigitalPause**.
4. **Complete Initial Setup**:
   - Walk through the guided 2-minute onboarding to set your 6-digit Parent PIN and grant necessary system permissions.

---

## 💡 About DigitalPause

Most app blockers are punitive, frustrating, and easy to delete in anger. **DigitalPause** takes a different approach: **mindful friction**.

Instead of hard-locking your device, DigitalPause:
1. Gives you **accurate, truthful insights** into your real screen time.
2. Provides a **breathing pause** when daily limits are reached, helping you reflect before continuing.
3. Automatically breaks **compulsive doomscrolling loops** on YouTube Shorts and Instagram Reels by gently backing you out of the infinite feed.
4. Operates **100% on-device** — your screen time habits and personal data never leave your phone.

---

## ✨ Features & Capabilities

### 📊 Truthful Screen Time Dashboard
- **Digital Wellbeing Parity**: Calculated via Android's low-level `UsageEvents` stream — accurately accounts for foreground/background transitions and eliminates the 24h+ accumulation bugs seen in naive blockers.
- **Today vs. Yesterday**: Immediate day-over-day screen time delta.
- **Per-App Breakdown**: High-resolution tracking with native app icons and exact minutes spent.
- **Device Unlock Counter**: Debounced counter tracking how many times you wake and unlock your device daily.
- **7-Day Weekly Chart**: Interactive Monday–Sunday usage bars to spot weekly habits.

### ⏱️ Custom Daily App Limits
- Set custom daily allowances per app (e.g. 15m, 30m, 1h, or custom minutes).
- **Mindful Pause Overlay**: When time expires, a serene breathing visualizer appears rather than an aggressive block screen.
- **Emergency Extra Time**: Users can claim up to **30 minutes of free emergency extension per day** without a PIN. Any extension beyond 30 minutes requires the Master PIN.
- Real-time limit progress bars in the Limits tab.

### 🚫 Doomscrolling & Short-Video Blocker
- **YouTube Shorts Protection**: Instantly detects when YouTube Shorts playback begins and executes a gentle Back action to return to your subscriptions/home feed.
- **Instagram Reels Protection**: Detects the Reels viewer and auto-returns to the main feed.
- **Smart Cooldown**: 3-second debounce window prevents back-button loops or visual flickering.
- Fully toggleable in Settings.

### 🔐 Parent / Master PIN Protection
- 6-digit master PIN established during onboarding.
- Protects:
  - Modifying existing limits
  - Changing short-video protection settings
  - Bypassing daily limits beyond the 30-minute grace buffer
- Native numeric entry with error feedback and lockout protection.

### 🛡️ 100% Offline & Private by Design
- **No Internet Permission**: The app doesn't even declare `android.permission.INTERNET` in its manifest. It cannot send data anywhere.
- **No Analytics / Telemetry**: No Firebase, no Google Analytics, no third-party trackers.
- **No Cloud Accounts**: No email, passwords, phone numbers, or social logins required.

---

## 🏗️ Architecture & Tech Stack

```
DigitalPause/
├── app/src/main/java/com/digitalpause/app/
│   ├── data/
│   │   ├── UsageStatsRepository.kt       # UsageEvents-based screen time engine
│   │   ├── PreferencesManager.kt         # Local SharedPreferences & migrations
│   │   └── PasswordManager.kt            # Salted hash PIN authentication
│   ├── service/
│   │   ├── DigitalPauseAccessibilityService.kt # Foreground detection & auto-back action
│   │   └── ShortVideoDetector.kt         # Node tree view ID inspector (curbox approach)
│   ├── ui/
│   │   ├── dashboard/                    # Screen time stats, weekly charts, app ranking
│   │   ├── limits/                       # App limit cards, creation & editing
│   │   ├── block/                        # Mindful breath visualizer & pause screen
│   │   ├── activity/                     # Detailed per-app daily & weekly analytics
│   │   ├── onboarding/                   # Welcome -> PIN setup -> Permissions guide
│   │   ├── settings/                     # Toggles, PIN reset, about screen
│   │   └── theme/                        # Material 3 Stitch design system
│   └── model/                            # Data classes (AppLimit, UsageInfo, Settings)
```

- **Language**: Kotlin 2.0
- **UI Framework**: Jetpack Compose + Material Design 3
- **Design System**: Tailored Deep Indigo (`#312E81`) / Mindful Teal (`#0D9488`) / Gentle Amber
- **State Management**: Kotlin `StateFlow` + Android Architecture Components `ViewModel`
- **Detection Engine**: Android `AccessibilityService` with `findAccessibilityNodeInfosByViewId`
- **Screen Time Engine**: Android `UsageStatsManager.queryEvents()` with interactive session reconstruction

---

## 🔒 Permissions Explained

| Permission | Android Name | Why It Is Needed |
| :--- | :--- | :--- |
| **Usage Access** | `PACKAGE_USAGE_STATS` | Required to read daily per-app usage times and unlock counts from Android system. |
| **Accessibility Service** | `BIND_ACCESSIBILITY_SERVICE` | Required to detect foreground package changes and identify YouTube Shorts / Instagram Reels containers. |
| **Display Over Other Apps** | `SYSTEM_ALERT_WINDOW` | Required to display the mindful pause screen when your daily limit expires. |
| **Run at Startup** | `RECEIVE_BOOT_COMPLETED` | Ensures your limits and protection automatically resume after your phone restarts. |
| **Notifications** | `POST_NOTIFICATIONS` | Alerts you when you have 5 minutes remaining before reaching an app limit. |

---

## ⚙️ Device-Specific Setup Guide

Some Android manufacturers (MIUI, ColorOS, OxygenOS, OneUI) have aggressive background battery killers. To ensure DigitalPause works reliably:

### 🔹 Xiaomi / Redmi / POCO (MIUI / HyperOS)
1. Go to **Settings → Apps → Manage Apps → DigitalPause**.
2. Enable **Autostart**.
3. Set **Battery Saver** to **No Restrictions**.
4. Lock DigitalPause in the Recent Apps tray.

### 🔹 OPPO / Realme / OnePlus (ColorOS / OxygenOS)
1. Go to **Settings → Apps → App Management → DigitalPause**.
2. Tap **Battery Usage** → Allow **Background activity** and **Auto-launch**.
3. Under **Special app access**, ensure **Display over other apps** and **Usage access** are Allowed.

### 🔹 Samsung (One UI)
1. Go to **Settings → Battery and device care → Battery → Background usage limits**.
2. Add **DigitalPause** to **Never sleeping apps**.

---

## 🛠️ Building from Source

If you prefer to build the APK yourself:

### Prerequisites:
- Android Studio Ladybug / Hedgehog or newer
- JDK 17 or higher
- Android SDK 26+

```bash
# 1. Clone the repository
git clone https://github.com/Naseem2917/DigitalPause.git
cd DigitalPause

# 2. Build Debug APK
./gradlew assembleDebug

# 3. The compiled APK will be located at:
# app/build/outputs/apk/debug/app-debug.apk

# 4. Install directly to a connected USB device:
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 🗺️ Future Roadmap

- [ ] Focus Sessions (Pomodoro timer with blocklist)
- [ ] Bedtime Wind-Down mode (grayscale & auto-lock)
- [ ] Daily screen time goal notification
- [ ] Home screen quick-stats widget

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).

<div align="center">

Made with ❤️ for mindful digital habits.

**[⭐ Star on GitHub](https://github.com/Naseem2917/DigitalPause) • [🐛 Report Bug](https://github.com/Naseem2917/DigitalPause/issues) • [📥 Download APK](https://github.com/Naseem2917/DigitalPause/releases/latest)**

</div>
