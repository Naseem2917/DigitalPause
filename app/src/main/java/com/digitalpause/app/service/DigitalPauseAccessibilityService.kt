package com.digitalpause.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.digitalpause.app.DigitalPauseApp
import com.digitalpause.app.ui.block.BlockActivity
import com.digitalpause.app.ui.block.BlockMode

/**
 * Core Accessibility Service for DigitalPause.
 *
 * Handles two independent protection modes:
 *
 * 1. APP LIMITS — when a limited app is in the foreground and usage >= limit,
 *    launch BlockActivity (full-screen mindful pause UI).
 *
 * 2. SHORT VIDEO PROTECTION — when YouTube Shorts or Instagram Reels is detected,
 *    call performGlobalAction(GLOBAL_ACTION_BACK) immediately.
 *    This matches curbox-android's proven approach:
 *    - Do NOT launch a new Activity (that leaves the short video running underneath)
 *    - pressBack() instantly dismisses the Shorts/Reels viewer
 *    - A 3-second cooldown prevents repeated presses
 *
 * Event types registered in accessibility_service_config.xml:
 *   typeWindowStateChanged | typeWindowContentChanged | typeViewScrolled
 */
class DigitalPauseAccessibilityService : AccessibilityService() {

    private val preferencesManager by lazy { DigitalPauseApp.instance.preferencesManager }
    private val usageStatsRepository by lazy { DigitalPauseApp.instance.usageStatsRepository }

    // Per-package last app-limit block timestamp (avoid repeated block screens)
    private val lastAppLimitBlock = mutableMapOf<String, Long>()

    // Per-package last short-video back-press timestamp (avoid repeated presses)
    private val lastShortVideoBack = mutableMapOf<String, Long>()

    override fun onServiceConnected() {
        super.onServiceConnected()
        isRunning = true
        Log.i(TAG, "DigitalPauseAccessibilityService connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val packageName = event.packageName?.toString() ?: return
        if (packageName.isBlank()) return
        if (packageName == applicationContext.packageName) return
        if (packageName == "android" || packageName == "com.android.systemui") return

        val now = SystemClock.elapsedRealtime()
        val settings = preferencesManager.settingsFlow.value

        // ── 1. APP LIMITS PROTECTION ──────────────────────────────────────────
        if (settings.isAppLimitsProtectionEnabled) {
            val limit = preferencesManager.getLimitForPackage(packageName)
            if (limit != null && limit.isEnabled) {
                val lastBlock = lastAppLimitBlock[packageName] ?: 0L
                if (now - lastBlock > APP_LIMIT_DEBOUNCE_MS) {
                    val tempAccess = preferencesManager.getActiveTemporaryAccess(packageName)
                    if (tempAccess == null || tempAccess.isExpired()) {
                        val usedMinutes = usageStatsRepository.getUsageMinutesToday(packageName)
                        if (limit.isLimitReached(usedMinutes)) {
                            Log.i(TAG, "Limit reached: $packageName used=${usedMinutes}min limit=${limit.dailyLimitMinutes}min")
                            lastAppLimitBlock[packageName] = now
                            triggerBlockActivity(packageName, limit.appName, BlockMode.DAILY_LIMIT,
                                limit.totalAllowedMinutes(), usedMinutes)
                            return
                        }
                    }
                }
            }
        }

        // ── 2. SHORT VIDEO PROTECTION ─────────────────────────────────────────
        // Only run for YouTube and Instagram, only on content/window/scroll events
        val isShortVideoTarget = packageName == YOUTUBE_PKG ||
                packageName == INSTAGRAM_PKG ||
                packageName == YOUTUBE_REVANCED_PKG
        val isRelevantEvent = event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
                event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
                event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED

        if (settings.isShortVideoProtectionEnabled && isShortVideoTarget && isRelevantEvent) {
            val lastBack = lastShortVideoBack[packageName] ?: 0L
            if (now - lastBack > SHORT_VIDEO_BACK_COOLDOWN_MS) {
                val tempAccess = preferencesManager.getActiveTemporaryAccess(packageName)
                if (tempAccess == null || tempAccess.isExpired()) {
                    // Get root node — prefer rootInActiveWindow (complete tree)
                    val root = safeGetRoot()
                    if (root != null) {
                        val detected = try {
                            ShortVideoDetector.isShortVideoActive(root, packageName)
                        } catch (e: Exception) {
                            Log.e(TAG, "ShortVideoDetector error: ${e.message}")
                            false
                        } finally {
                            try { root.recycle() } catch (_: Exception) {}
                        }

                        if (detected) {
                            Log.i(TAG, "Short-video DETECTED in $packageName — pressing BACK")
                            lastShortVideoBack[packageName] = now
                            // Press back immediately — this exits Shorts/Reels viewer
                            performGlobalAction(GLOBAL_ACTION_BACK)
                        } else {
                            Log.v(TAG, "Short-video check: NOT detected in $packageName")
                        }
                    }
                }
            }
        }
    }

    private fun safeGetRoot(): android.view.accessibility.AccessibilityNodeInfo? {
        return try {
            rootInActiveWindow
        } catch (e: Exception) {
            Log.w(TAG, "rootInActiveWindow threw: ${e.message}")
            null
        }
    }

    private fun triggerBlockActivity(
        packageName: String,
        appName: String,
        mode: BlockMode,
        limitMinutes: Int,
        usedMinutes: Int
    ) {
        val intent = Intent(this, BlockActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(BlockActivity.EXTRA_PACKAGE_NAME, packageName)
            putExtra(BlockActivity.EXTRA_APP_NAME, appName)
            putExtra(BlockActivity.EXTRA_BLOCK_MODE, mode.name)
            putExtra(BlockActivity.EXTRA_LIMIT_MINUTES, limitMinutes)
            putExtra(BlockActivity.EXTRA_USED_MINUTES, usedMinutes)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {
        Log.w(TAG, "DigitalPauseAccessibilityService interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        Log.i(TAG, "DigitalPauseAccessibilityService destroyed")
    }

    companion object {
        private const val TAG = "DigitalPauseService"
        var isRunning: Boolean = false
            private set

        private const val APP_LIMIT_DEBOUNCE_MS = 3_000L
        // After pressing back once, wait 3s before pressing again
        // This prevents a rapid-fire loop while the UI animates out
        private const val SHORT_VIDEO_BACK_COOLDOWN_MS = 3_000L

        private const val YOUTUBE_PKG = "com.google.android.youtube"
        private const val YOUTUBE_REVANCED_PKG = "app.revanced.android.youtube"
        private const val INSTAGRAM_PKG = "com.instagram.android"
    }
}
