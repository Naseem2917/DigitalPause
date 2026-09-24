# 🔒 Don't Change — DIGITALPAUSE

> Everything listed here is **working and strictly constrained by the PRD / Stitch design**.
> Any AI session must treat this as read-only unless the user explicitly requests to change it.
> Before editing any file touching these, re-read this list first.

## Locked features & constraints
- **Google Stitch Visual Theme & Tokens** — files: `app/src/main/java/com/digitalpause/app/ui/theme/*`
  - *Why it's locked*: Google Stitch Project `15608816765016706799` is the primary visual source of truth. Colors (`#4338CA` Indigo, `#0D9488` Teal, `#D97706` Amber, `#E11D48` Crimson, slate containers), typography (Inter with tabular numbers), pill shapes, and card radii must NOT be arbitrarily modified or redesigned.
- **Parent Password Security Model** — file: `app/src/main/java/com/digitalpause/app/data/PasswordManager.kt`
  - *Why it's locked*: PRD Section 4 & 36 mandates zero plaintext password storage, 16-byte random salt, SHA-256 hash, and constant-time comparison (`MessageDigest.isEqual`). Never expose password values in logs or UI.
- **No Mock/Fake Production Data** — file: `app/src/main/java/com/digitalpause/app/data/UsageStatsRepository.kt`
  - *Why it's locked*: PRD Section 5 & 41 strictly forbids hard-coding fake statistics when usage access is missing. The system must render the dedicated Stitch empty state instead.
- **Local-First & Offline Architecture** — file: `app/src/main/java/com/digitalpause/app/data/PreferencesManager.kt`
  - *Why it's locked*: PRD Section 2 strictly excludes cloud databases, user logins, remote syncing, and advertising.
- **Modular Short-Form Video Detection** — file: `app/src/main/java/com/digitalpause/app/service/ShortVideoDetector.kt`
  - *Why it's locked*: PRD Section 22 requires isolated, defensive detection that never crashes the application and fails safely if target app UI changes.

## Locked files / folders
- `app/src/main/res/drawable/ic_digitalpause_logo.xml` — official Stitch squircle logo with mindful breathing ring and pause bars
- `app/src/main/res/xml/accessibility_service_config.xml` — Android accessibility configuration
- `references/` — context-keeper documentation

## Rules for the AI
1. Never rewrite a locked file wholesale — only make the minimal diff needed.
2. If a requested change *requires* touching a locked area, stop and ask first.
3. After any change, mentally check: did this touch anything above? If yes, flag it.
