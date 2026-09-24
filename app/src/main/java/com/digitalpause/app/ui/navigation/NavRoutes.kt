package com.digitalpause.app.ui.navigation

sealed class Screen(val route: String) {
    // Onboarding Flow
    data object Splash : Screen("splash")
    data object Welcome : Screen("welcome")
    data object HowItWorks : Screen("how_it_works")
    data object ParentPasswordSetup : Screen("parent_password_setup")
    data object PermissionsSetup : Screen("permissions_setup")
    data object SuggestedApps : Screen("suggested_apps")
    data object SetupComplete : Screen("setup_complete")

    // Main 3 Bottom Tabs
    data object Dashboard : Screen("dashboard")
    data object Limits : Screen("limits")
    data object Settings : Screen("settings")

    // Activity & Details
    data object ActivityDetails : Screen("activity_details")

    // Limits Sub-screens
    data object AddLimit : Screen("add_limit")
    data object EditLimit : Screen("edit_limit/{packageName}") {
        fun createRoute(packageName: String) = "edit_limit/$packageName"
    }
    data object LimitDetail : Screen("limit_detail/{packageName}") {
        fun createRoute(packageName: String) = "limit_detail/$packageName"
    }

    // Settings Sub-screens
    data object ProtectionStatus : Screen("protection_status")
    data object ShortVideoSettings : Screen("short_video_settings")
    data object NotificationSettings : Screen("notification_settings")
    data object ParentPasswordSettings : Screen("parent_password_settings")
    data object About : Screen("about")
    data object PhysicalReminderPreview : Screen("physical_reminder_preview")
}
