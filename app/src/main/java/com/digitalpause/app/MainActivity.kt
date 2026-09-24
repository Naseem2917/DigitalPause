package com.digitalpause.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.digitalpause.app.model.ThemeMode
import com.digitalpause.app.ui.activity.AppActivityDetailsScreen
import com.digitalpause.app.ui.components.DigitalPauseBottomNavBar
import com.digitalpause.app.ui.dashboard.DashboardScreen
import com.digitalpause.app.ui.dashboard.DashboardViewModel
import com.digitalpause.app.ui.limits.AddAppLimitScreen
import com.digitalpause.app.ui.limits.AppLimitDetailScreen
import com.digitalpause.app.ui.limits.AppLimitsScreen
import com.digitalpause.app.ui.limits.AppLimitsViewModel
import com.digitalpause.app.ui.navigation.Screen
import com.digitalpause.app.ui.onboarding.HowItWorksScreen
import com.digitalpause.app.ui.onboarding.ParentPasswordSetupScreen
import com.digitalpause.app.ui.onboarding.PermissionsSetupScreen
import com.digitalpause.app.ui.onboarding.SetupCompleteScreen
import com.digitalpause.app.ui.onboarding.SplashScreen
import com.digitalpause.app.ui.onboarding.SuggestedAppsScreen
import com.digitalpause.app.ui.onboarding.WelcomeScreen
import com.digitalpause.app.ui.settings.AboutScreen
import com.digitalpause.app.ui.settings.NotificationSettingsScreen
import com.digitalpause.app.ui.settings.ParentPasswordSettingsScreen
import com.digitalpause.app.ui.settings.PhysicalReminderPreviewScreen
import com.digitalpause.app.ui.settings.ProtectionStatusScreen
import com.digitalpause.app.ui.settings.SettingsScreen
import com.digitalpause.app.ui.settings.ShortVideoProtectionScreen
import com.digitalpause.app.ui.theme.DigitalPauseTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val prefsManager = remember { DigitalPauseApp.instance.preferencesManager }
            val passwordManager = remember { DigitalPauseApp.instance.passwordManager }
            val settings by prefsManager.settingsFlow.collectAsState()

            val isDark = when (settings.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            DigitalPauseTheme(darkTheme = isDark) {
                DigitalPauseAppNav(
                    isOnboardingCompleted = settings.isOnboardingCompleted,
                    onSaveParentPassword = { pin ->
                        passwordManager.saveParentPassword(pin)
                    },
                    onCompleteOnboarding = {
                        prefsManager.updateSettings { it.copy(isOnboardingCompleted = true) }
                    }
                )
            }
        }
    }
}

@Composable
fun DigitalPauseAppNav(
    isOnboardingCompleted: Boolean,
    onSaveParentPassword: (String) -> Unit,
    onCompleteOnboarding: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isBottomBarVisible = currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.Limits.route,
        Screen.Settings.route
    )

    val dashboardViewModel: DashboardViewModel = viewModel()
    val limitsViewModel: AppLimitsViewModel = viewModel()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (isBottomBarVisible) {
                DigitalPauseBottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        if (route == Screen.Dashboard.route) {
                            navController.popBackStack(Screen.Dashboard.route, inclusive = false)
                        } else {
                            navController.navigate(route) {
                                popUpTo(Screen.Dashboard.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(bottom = if (isBottomBarVisible) innerPadding.calculateBottomPadding() else innerPadding.calculateTopPadding() * 0)
        ) {
            // ==========================================
            // ONBOARDING FLOW
            // ==========================================
            composable(Screen.Splash.route) {
                SplashScreen(
                    isOnboardingCompleted = isOnboardingCompleted,
                    onNavigateToNext = { completed ->
                        val nextRoute = if (completed) Screen.Dashboard.route else Screen.Welcome.route
                        navController.navigate(nextRoute) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Welcome.route) {
                WelcomeScreen(
                    onGetStarted = {
                        navController.navigate(Screen.HowItWorks.route)
                    }
                )
            }

            composable(Screen.HowItWorks.route) {
                HowItWorksScreen(
                    onBack = { navController.popBackStack() },
                    onContinue = {
                        navController.navigate(Screen.ParentPasswordSetup.route)
                    }
                )
            }

            composable(Screen.ParentPasswordSetup.route) {
                ParentPasswordSetupScreen(
                    onBack = { navController.popBackStack() },
                    onPasswordSaved = { pin ->
                        onSaveParentPassword(pin)
                        navController.navigate(Screen.PermissionsSetup.route)
                    }
                )
            }

            composable(Screen.PermissionsSetup.route) {
                PermissionsSetupScreen(
                    onBack = { navController.popBackStack() },
                    onContinue = {
                        navController.navigate(Screen.SuggestedApps.route)
                    }
                )
            }

            composable(Screen.SuggestedApps.route) {
                SuggestedAppsScreen(
                    onBack = { navController.popBackStack() },
                    onContinue = {
                        navController.navigate(Screen.SetupComplete.route)
                    }
                )
            }

            composable(Screen.SetupComplete.route) {
                SetupCompleteScreen(
                    onFinishSetup = {
                        onCompleteOnboarding()
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    }
                )
            }

            // ==========================================
            // MAIN 3 TABS
            // ==========================================
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToLimits = {
                        navController.navigate(Screen.Limits.route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToAppDetail = { pkg ->
                        navController.navigate(Screen.LimitDetail.createRoute(pkg))
                    },
                    onNavigateToActivityDetails = {
                        navController.navigate(Screen.ActivityDetails.route)
                    }
                )
            }

            composable(Screen.Limits.route) {
                AppLimitsScreen(
                    viewModel = limitsViewModel,
                    onNavigateToAddLimit = {
                        navController.navigate(Screen.AddLimit.route)
                    },
                    onNavigateToDetail = { pkg ->
                        navController.navigate(Screen.LimitDetail.createRoute(pkg))
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigate = { route ->
                        navController.navigate(route)
                    }
                )
            }

            // ==========================================
            // APP ACTIVITY DETAILS / VIEW ALL
            // ==========================================
            composable(Screen.ActivityDetails.route) {
                AppActivityDetailsScreen(
                    onBack = { navController.popBackStack() },
                    onAppClick = { pkg ->
                        navController.navigate(Screen.LimitDetail.createRoute(pkg))
                    }
                )
            }

            // ==========================================
            // LIMITS SUB-SCREENS
            // ==========================================
            composable(Screen.AddLimit.route) {
                AddAppLimitScreen(
                    viewModel = limitsViewModel,
                    onBack = { navController.popBackStack() },
                    onLimitSaved = {
                        navController.popBackStack()
                        dashboardViewModel.loadData()
                    }
                )
            }

            composable(
                route = Screen.LimitDetail.route,
                arguments = listOf(navArgument("packageName") { type = NavType.StringType })
            ) { backStackEntry ->
                val packageName = backStackEntry.arguments?.getString("packageName") ?: ""
                AppLimitDetailScreen(
                    packageName = packageName,
                    viewModel = limitsViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // ==========================================
            // SETTINGS SUB-SCREENS
            // ==========================================
            composable(Screen.ProtectionStatus.route) {
                ProtectionStatusScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.ShortVideoSettings.route) {
                ShortVideoProtectionScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.NotificationSettings.route) {
                NotificationSettingsScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.ParentPasswordSettings.route) {
                ParentPasswordSettingsScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.About.route) {
                AboutScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.PhysicalReminderPreview.route) {
                PhysicalReminderPreviewScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
