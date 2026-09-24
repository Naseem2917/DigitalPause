# Product Requirements Document (PRD) — DIGITALPAUSE

> Primary Source of Truth for DigitalPause MVP requirements and constraints.

## 1. What are we building?
**DigitalPause** is a student-focused Android digital-wellbeing application.
- **Tagline**: "Pause. Think. Use Intentionally."
- **One-line Definition**: A student-focused Android app that helps users reduce excessive digital usage through screen-time awareness, app limits, short-form content protection, and controlled parent-authorized overrides.
- **Core Loop**: AWARENESS → CHOICE → PAUSE → INTENTIONAL USE.
- **Context**: University Community Engagement Project (CEP) on "Digital Addiction and Mental Health Awareness Among Students".
- **Philosophy**: Non-medical, non-judgmental, calm companion. Never shames the student.

## 2. Core Features (MVP)
- [x] **1. Onboarding**: Splash, Welcome with mindful breath preview, How DigitalPause Works, Parent Password Setup, Permission Setup, Setup Complete.
- [x] **2. Parent/Guardian Password**: 6-digit PIN created exclusively by parent/guardian. Plaintext never stored or revealed; salted SHA-256 with 16-byte random salt.
- [x] **3. Android Permission Setup**: Usage Access (UsageStatsManager) and Accessibility Service with direct Settings deep-links.
- [x] **4. Screen-Time Dashboard**: Real usage data (today vs yesterday diff, unlock count, daily pacing bar, most used apps, active limits, shield quick toggles, empty state fallback).
- [x] **5. App Limits**: Add, edit, remove (with confirmation), package selection, custom daily hours/minutes.
- [x] **6. Limit Enforcement**: AccessibilityService monitors foreground app. Detects daily limit exhaustion and triggers mindful pause intercept.
- [x] **7. Block Screen**: "Take a Pause", concentric breathing aura animation, target app pill, mindful quote, "Go Back", "Extra 15 min", "Parent Override".
- [x] **8. Extra Time**: Controlled extension (5m, 15m default, 30m) tracked separately without permanently modifying configured daily limits.
- [x] **9. Parent Override**: Temporary exception (15m, 30m, 1h) requiring parent PIN on every override.
- [x] **10. Short-Form Content Protection**: Dedicated shield for YouTube Shorts & Instagram Reels with graceful fallback.
- [x] **11. Protection Status**: Health hub showing status of all 4 shields (App Limits, Short Videos, Usage Access, Accessibility) with one-tap "Fix" actions.
- [x] **12. Local Settings & Notifications**: Appearance (Light / Dark mode), Limit Reminders, Near-Limit Reminders (80%), Protection alerts.
- [x] **13. Physical Pause Reminder Card**: In-app preview of the tangible study desk reminder card ("Pause Before You Scroll - Do I really need this right now?").
- [x] **14. Daily Reset**: Reset according to local device date at midnight without server dependency.

## 3. Out of Scope (Strictly Excluded from MVP)
- Login / Signup / Cloud accounts / Firebase
- Cloud database & remote synchronization
- Parent separate companion mobile app / remote control
- AI chatbot / AI recommendations
- Social features / friends / leaderboards / gamification
- Mood tracking / medical diagnosis
- Subscriptions / payments / ads

## 4. Tech Stack
- **Platform**: Android Native (API 26+)
- **Language**: Kotlin 2.1.0+ / JDK 17
- **UI Toolkit**: Jetpack Compose + Material 3 (following Google Stitch UI/UX design tokens)
- **Local Storage**: DataStore / SharedPreferences + salted SHA-256 for parent password
- **System APIs**: UsageStatsManager (`PACKAGE_USAGE_STATS`), AccessibilityService (`DigitalPauseAccessibilityService`), NotificationManager
- **Design Truth**: Google Stitch Project `15608816765016706799`

## 5. Non-Negotiable Constraints
- **Stitch Visual Fidelity**: Final UI must closely reproduce Stitch screens (colors, typography, spacing, pill shapes, breathing aura).
- **No Fake Statistics**: Never show mock numbers in production dashboard when usage data is unavailable. Show Stitch empty state instead.
- **Zero Plaintext Password**: Cryptographic 16-byte random salt + SHA-256 hash.
- **Local-First & Offline**: All user data remains 100% on the user's hardware.
- **Graceful Failure**: Never crash if short-form video node detection is unsupported or altered by target app updates.

## 6. Success Criteria
- APK compiles and builds cleanly with Gradle.
- Complete navigation between all 28 Stitch screens and dialogs.
- Real usage statistics queried accurately.
- AccessibilityService intercepts restricted apps and launches the mindful pause block screen.
- Parent password verification works reliably without leakage.
