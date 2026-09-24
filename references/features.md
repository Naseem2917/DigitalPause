# Features Tracking — DIGITALPAUSE

> Track everything that's planned, in progress, or recently done.
> Once something is stable and verified, move its entry to `locked.md`.

## 🟢 Done (Recently Implemented)
- [x] **Project Scaffolding**: Gradle 9.5 build setup, AGP 8.8.2, Kotlin 2.1.0 with Compose Compiler plugin, Android SDK API 35/36.1 compatibility (`2026-09-24`).
- [x] **Stitch Design System Tokens**: `Color.kt`, `Type.kt` (with Inter & tabular figures), `Shape.kt` (pill shapes & card corners), `Theme.kt` supporting Light and Dark modes (`2026-09-24`).
- [x] **Local Security & Storage**: `PasswordManager.kt` with 16-byte random salt + SHA-256 hash in constant-time comparison, `PreferencesManager.kt` for local limits and settings (`2026-09-24`).
- [x] **Usage & System Layer**: `UsageStatsRepository.kt` querying real foreground usage, today vs yesterday delta, unlock counts, installed app enumeration (`2026-09-24`).
- [x] **Accessibility Service & Enforcement**: `DigitalPauseAccessibilityService.kt` with debounce loop prevention and `ShortVideoDetector.kt` for YouTube Shorts and Instagram Reels (`2026-09-24`).
- [x] **Onboarding Flow (Screens 1 to 6)**: Splash, Welcome with breathing preview, How It Works, Parent Password Setup with dual PIN visualizer, Permissions Setup, Setup Complete (`2026-09-24`).
- [x] **Dashboard (Screens 7 & 8)**: Bento hero card, yesterday comparison, unlocks, pacing bar, Most Used Apps, Active Limits, Shield toggles, Start 15m Focus Breath dialog, and Empty State (`2026-09-24`).
- [x] **App Limits Hub (Screens 9 to 12)**: App Limits list, Add App Limit with app search and duration presets, Edit App Limit, App Limit Detail with deletion confirmation (`2026-09-24`).
- [x] **Block Screen & Mindful Intercept (Screens 14 to 20, 22)**: Concentric breathing aura, target app pill, mindful quote, Go Back, Extra Time selection (5m, 15m, 30m), Parent Override PIN verification with duration selection (15m, 30m, 1h), Short Video intercept (`2026-09-24`).
- [x] **Settings Hub (Screens 21, 23 to 28)**: Settings menu, Protection Status health hub, Short Video Protection settings, Notification settings, Parent Password change settings, About DigitalPause (CEP context), Physical Reminder Card preview (`2026-09-24`).

## 🟡 In progress
- [ ] **Build Validation & Packaging**: Executing `./gradlew.bat assembleDebug` to produce installable APK.
- [ ] **End-to-End Device Testing**: Verifying app on physical or emulator target across all test scenarios in PRD Section 42.

## 🔵 Planned
- [ ] Continuous refinement based on physical device feedback.
- [ ] Optional printable asset generator for the Physical Reminder Card.

## 🐛 Known bugs
- None currently reported.
