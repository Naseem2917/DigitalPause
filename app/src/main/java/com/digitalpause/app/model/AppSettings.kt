package com.digitalpause.app.model

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class AppSettings(
    val isOnboardingCompleted: Boolean = false,
    val isShortVideoProtectionEnabled: Boolean = true,
    val isAppLimitsProtectionEnabled: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.LIGHT,
    val limitRemindersEnabled: Boolean = true,
    val nearLimitRemindersEnabled: Boolean = true,
    val protectionNotificationsEnabled: Boolean = true,
    val lastResetDate: String = ""
)

data class ProtectionStatus(
    val usageAccessGranted: Boolean,
    val accessibilityServiceEnabled: Boolean,
    val appLimitsActive: Boolean,
    val shortVideoProtectionActive: Boolean
) {
    val isFullyProtected: Boolean
        get() = usageAccessGranted && accessibilityServiceEnabled && (appLimitsActive || shortVideoProtectionActive)
}
