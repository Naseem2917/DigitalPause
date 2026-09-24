# Technical Decisions Log — DIGITALPAUSE

> Record any technical, architectural, or design decision so future AI sessions don't silently undo or contradict them.

---
### 2026-09-24 — Google Stitch as Visual Source of Truth
**Decision:** Adopt all layout structures, color hex values, typography tokens (Inter), border radiuses (pills & 16-24dp cards), and visual components directly from Google Stitch project `15608816765016706799`.
**Reason:** Master PRD explicitly mandates Stitch as the primary source of truth, prohibiting generic Material 3 or custom divergent UI.
**Alternatives considered:** Generic Android Material You design (rejected per PRD Section 0 & 30).

---
### 2026-09-24 — Salted SHA-256 for Parent Password Security
**Decision:** Store parent password with a 16-byte cryptographically secure random salt (`SecureRandom`) and SHA-256 hash, using constant-time comparison (`MessageDigest.isEqual`).
**Reason:** The child/student must never be able to inspect plaintext passwords from device storage, and timing attacks must be avoided.
**Alternatives considered:** Plaintext SharedPreferences (rejected for severe insecurity), Android Keystore Biometrics (rejected because the parent, not the device owner, authenticates the override).

---
### 2026-09-24 — Local-First Offline Architecture with org.json
**Decision:** Store configured limits, settings, and temporary access grants locally using `PreferencesManager` with Android SDK built-in `org.json`.
**Reason:** Keeps the application 100% offline, privacy-first, zero cloud dependencies, and eliminates bloated third-party reflection libraries.
**Alternatives considered:** Room database (unnecessary boilerplate for small list of limits), Firebase / Cloud DB (explicitly forbidden by PRD Section 2).

---
### 2026-09-24 — Modular Accessibility Intercept & Short-Form Video Protection
**Decision:** Implement `DigitalPauseAccessibilityService` paired with an isolated `ShortVideoDetector` that defensively checks node hierarchies for YouTube Shorts and Instagram Reels.
**Reason:** Android does not allow selective OS-level package disabling for sub-features like Shorts/Reels; AccessibilityService allows real-time foreground detection and mindful pause interception without disrupting regular educational videos.
**Alternatives considered:** Foreground polling service (heavy battery drain, cannot detect in-app navigation to Reels/Shorts).

---
### 2026-09-24 — Clean MVVM Navigation Architecture
**Decision:** Single-Activity Compose architecture with `MainActivity` hosting bottom navigation across 3 primary tabs (Dashboard, Limits, Settings) and full-screen `BlockActivity` for foreground pause intercepts.
**Reason:** Keeps navigation predictable, ensures the block intercept can launch immediately from background service without colliding with the main backstack, and complies with PRD Section 10 & 14.
**Alternatives considered:** Multi-activity for every screen (clunky in modern Compose), single activity for both block and main (risk of backstack confusion when returning from blocked apps).
