package com.digitalpause.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digitalpause.app.DigitalPauseApp
import com.digitalpause.app.model.ThemeMode
import com.digitalpause.app.ui.navigation.Screen
import com.digitalpause.app.ui.theme.PillShape
import com.digitalpause.app.ui.theme.extendedColors

@Composable
fun SettingsScreen(
    onNavigate: (String) -> Unit
) {
    val prefsManager = remember { DigitalPauseApp.instance.preferencesManager }
    val settings by prefsManager.settingsFlow.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Header
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            Text(
                text = "Preferences",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Section: Protection
        SettingsSectionHeader(title = "PROTECTION")
        SettingsCardGroup {
            SettingsRowItem(
                icon = Icons.Filled.HourglassTop,
                iconTint = MaterialTheme.colorScheme.secondary,
                title = "App Limits",
                subtitle = if (settings.isAppLimitsProtectionEnabled) "Active" else "Paused",
                onClick = { onNavigate(Screen.Limits.route) }
            )
            SettingsRowDivider()
            SettingsRowItem(
                icon = Icons.Filled.PlayCircle,
                iconTint = MaterialTheme.colorScheme.primary,
                title = "Short Video Protection",
                subtitle = if (settings.isShortVideoProtectionEnabled) "Shielding Reels & Shorts" else "Disabled",
                onClick = { onNavigate(Screen.ShortVideoSettings.route) }
            )
            SettingsRowDivider()
            SettingsRowItem(
                icon = Icons.Filled.Security,
                iconTint = MaterialTheme.colorScheme.secondary,
                title = "Protection Status & Permissions",
                subtitle = "Usage Access and Accessibility",
                onClick = { onNavigate(Screen.ProtectionStatus.route) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section: Security
        SettingsSectionHeader(title = "SECURITY")
        SettingsCardGroup {
            SettingsRowItem(
                icon = Icons.Filled.Lock,
                iconTint = MaterialTheme.colorScheme.tertiary,
                title = "Parent Password",
                subtitle = "Change or configure secret guardian PIN",
                onClick = { onNavigate(Screen.ParentPasswordSettings.route) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section: General
        SettingsSectionHeader(title = "GENERAL")
        SettingsCardGroup {
            SettingsRowItem(
                icon = Icons.Filled.Notifications,
                iconTint = MaterialTheme.colorScheme.primary,
                title = "Notifications",
                subtitle = "Daily limit and near-limit reminders",
                onClick = { onNavigate(Screen.NotificationSettings.route) }
            )
            SettingsRowDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.extendedColors.primaryFixed)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DarkMode,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(text = "Appearance", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                        Text(text = settings.themeMode.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Theme Mode Selector Pills
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(PillShape)
                        .background(MaterialTheme.extendedColors.surfaceContainerLow)
                        .padding(3.dp)
                ) {
                    listOf(ThemeMode.LIGHT to "Light", ThemeMode.DARK to "Dark").forEach { (mode, label) ->
                        val isSelected = settings.themeMode == mode
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(PillShape)
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { prefsManager.updateSettings { it.copy(themeMode = mode) } }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section: CEP Companion
        SettingsSectionHeader(title = "STUDENT WELL-BEING (CEP)")
        SettingsCardGroup {
            SettingsRowItem(
                icon = Icons.Filled.CreditCard,
                iconTint = MaterialTheme.colorScheme.secondary,
                title = "Physical Pause Reminder Card",
                subtitle = "Study desk tangible awareness companion",
                onClick = { onNavigate(Screen.PhysicalReminderPreview.route) }
            )
            SettingsRowDivider()
            SettingsRowItem(
                icon = Icons.Filled.Info,
                iconTint = MaterialTheme.colorScheme.primary,
                title = "About DigitalPause",
                subtitle = "Community Engagement Project vision & privacy",
                onClick = { onNavigate(Screen.About.route) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // App Version
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "DigitalPause v1.0.0 (Production MVP)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Pause. Think. Use Intentionally.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsCardGroup(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.extendedColors.surfaceContainerLowest)
    ) {
        content()
    }
}

@Composable
private fun SettingsRowItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = 0.12f))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(text = title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun SettingsRowDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(horizontal = 14.dp)
            .background(MaterialTheme.extendedColors.surfaceContainerLow)
    )
}
