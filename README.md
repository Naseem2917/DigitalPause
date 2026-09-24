<div align="center">

<img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" width="100" alt="DigitalPause Logo" />

# DigitalPause

### "Pause. Think. Use Intentionally."

**A mindful screen-time companion for Android — 100% offline, no accounts, no tracking.**

[![Release](https://img.shields.io/github/v/release/Naseem2917/DigitalPause?label=Latest%20Release&color=4338CA)](https://github.com/Naseem2917/DigitalPause/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/Naseem2917/DigitalPause/total?color=0D9488)](https://github.com/Naseem2917/DigitalPause/releases)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Android](https://img.shields.io/badge/Android-8.0%2B-green.svg)](https://android.com)

</div>

---

## 📲 Download & Install

> **No Play Store needed.** Download the APK directly from GitHub Releases.

### ➡️ [Download Latest APK →](https://github.com/Naseem2917/DigitalPause/releases/latest)

**Installation steps:**
1. Click the link above → tap `DigitalPause-v*.apk` under **Assets**
2. On your phone: **Settings → Install unknown apps** → allow your browser
3. Open the downloaded APK and tap **Install**
4. Follow the in-app setup (takes ~2 minutes)

> ⚠️ ColorOS / OPPO users: Settings → Privacy → Special app access → Install unknown apps

---

## ✨ Features

### 📊 Real Screen Time Dashboard
- **Accurate today & yesterday screen time** — powered by `UsageEvents` API (matches Android Digital Wellbeing)
- Per-app usage breakdown with real app icons
- Phone unlock count (debounced, accurate)
- Weekly Mon–Sun bar chart
- Day-by-day navigation

### ⏱️ App Daily Limits
- Set custom daily time limits per app (5 min / 15 min / 30 min / Custom)
- When limit is reached → mindful pause screen appears
- **Extra Time:** up to **30 minutes/day free** without a PIN; more requires Parent PIN
- Limits screen shows live usage vs. limit progress bar
- Pause / resume individual limits

### 🚫 Short-Video Protection
- **YouTube Shorts** → automatically presses Back when Shorts feed is detected
- **Instagram Reels** → automatically presses Back when Reels viewer is open
- Detects via accessibility node IDs (same approach as curbox-android)
- 3-second cooldown prevents repeated back-presses
- Can be toggled on/off in Settings

### 🔐 Parent PIN
- 6-digit parent PIN set during onboarding
- Protects: extra time beyond 30 min/day, limit overrides
- PIN entry uses Android system numeric keyboard (secure, no custom keypad)

### 📱 Suggested Apps Onboarding
- Suggests commonly distracting apps actually installed on your device
- **Suggested ≠ Limited** — no limit is ever created without your explicit action

### 🎨 Design
- Deep Indigo / Mindful Teal / Gentle Amber color palette (Stitch-designed)
- Light & Dark mode with system follow option
- Inter typography, pill buttons, smooth animations
- 100% Jetpack Compose UI

### 🔒 Privacy
- **Fully offline** — zero network requests
- **No account required** — no email, no sign-up
- **No analytics or tracking** — ever
- All data stored locally on device (SharedPreferences)

---

## 🏗️ Architecture

```
DigitalPause/
├── app/src/main/java/com/digitalpause/app/
│   ├── data/
│   │   ├── UsageStatsRepository.kt   # Real screen time via UsageEvents
│   │   └── PreferencesManager.kt     # Local-first persistence
│   ├── service/
│   │   ├── DigitalPauseAccessibilityService.kt  # App limits + short-video back-press
│   │   └── ShortVideoDetector.kt                # YouTube/Instagram Reels detection
│   ├── ui/
│   │   ├── dashboard/    # Main screen — screen time, most used apps
│   │   ├── limits/       # Set / manage / view app limits
│   │   ├── settings/     # Protection settings, parent PIN, theme
│   │   ├── block/        # Mindful pause / limit reached screen
│   │   ├── activity/     # App Activity Details (view all, weekly chart)
│   │   └── onboarding/   # Welcome → PIN → Permissions → Suggested Apps
│   └── model/            # AppLimit, AppUsageInfo, AppSettings, etc.
```

**Tech Stack:**
- Language: **Kotlin**
- UI: **Jetpack Compose** (Material 3)
- Navigation: **Navigation Compose**
- State: **StateFlow + ViewModel**
- Persistence: **SharedPreferences** (local only)
- Screen Time: **UsageStatsManager.queryEvents()**
- Short-video: **AccessibilityService + findAccessibilityNodeInfosByViewId()**

---

## 🔧 Required Permissions

| Permission | Why |
|-----------|-----|
| `PACKAGE_USAGE_STATS` | Read per-app screen time (user must grant in Settings) |
| `BIND_ACCESSIBILITY_SERVICE` | Detect app foreground + short-video viewer |
| `SYSTEM_ALERT_WINDOW` | Show block screen over other apps |
| `RECEIVE_BOOT_COMPLETED` | Restart protection service after reboot |
| `POST_NOTIFICATIONS` | Near-limit reminders (Android 13+) |

---

## 🛠️ Build from Source

**Requirements:** Android Studio Hedgehog or later, JDK 17, Android SDK 26+

```bash
# Clone the repo
git clone https://github.com/Naseem2917/DigitalPause.git
cd DigitalPause

# Build debug APK
./gradlew assembleDebug

# Install on connected device
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 📋 Permissions Setup (After Install)

The app will guide you, but here's what to do manually:

1. **Usage Access** — Settings → Digital Wellbeing & Parental Controls → No app access to usage data → DigitalPause → Allow
2. **Accessibility** — Settings → Accessibility → Installed services → DigitalPause → ON
3. **Display over other apps** — Settings → Apps → DigitalPause → Display over other apps → Allow

---

## 🗺️ Roadmap

- [ ] Focus Mode (block all distracting apps for a session)
- [ ] Bedtime Mode (auto-lock apps after a set time)
- [ ] Weekly/Monthly usage reports (export PDF)
- [ ] Widget for home screen

---

## 🤝 Contributing

Pull requests are welcome! Please read the issues page first and check if your idea is already planned.

---

## 📄 License

MIT License — see [LICENSE](LICENSE) for details.

---

<div align="center">

Made with ❤️ for mindful digital habits

**[⭐ Star this repo](https://github.com/Naseem2917/DigitalPause) · [🐛 Report a Bug](https://github.com/Naseem2917/DigitalPause/issues) · [💡 Request a Feature](https://github.com/Naseem2917/DigitalPause/issues)**

</div>
