package com.digitalpause.app.ui.settings

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.digitalpause.app.DigitalPauseApp
import com.digitalpause.app.ui.theme.extendedColors

@Composable
fun NotificationSettingsScreen(
    onBack: () -> Unit
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.extendedColors.surfaceContainer)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = "Gentle Prompts", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "Notifications", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Notification Cards Group
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.extendedColors.surfaceContainerLowest)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            NotificationToggleRow(
                icon = Icons.Filled.HourglassTop,
                iconTint = MaterialTheme.colorScheme.secondary,
                title = "Limit Reminders",
                description = "Notify calmly when an app daily limit has been fully reached.",
                isChecked = settings.limitRemindersEnabled,
                onCheckedChange = { isChecked ->
                    prefsManager.updateSettings { it.copy(limitRemindersEnabled = isChecked) }
                }
            )

            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.extendedColors.surfaceContainerLow))

            NotificationToggleRow(
                icon = Icons.Filled.HourglassBottom,
                iconTint = MaterialTheme.colorScheme.tertiary,
                title = "Near Limit Reminders",
                description = "Warn gently when 80% of daily quota is used (e.g. 10m remaining).",
                isChecked = settings.nearLimitRemindersEnabled,
                onCheckedChange = { isChecked ->
                    prefsManager.updateSettings { it.copy(nearLimitRemindersEnabled = isChecked) }
                }
            )

            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.extendedColors.surfaceContainerLow))

            NotificationToggleRow(
                icon = Icons.Filled.Shield,
                iconTint = MaterialTheme.colorScheme.primary,
                title = "Protection Status Alerts",
                description = "Updates when Extra Time or Parent Override expires.",
                isChecked = settings.protectionNotificationsEnabled,
                onCheckedChange = { isChecked ->
                    prefsManager.updateSettings { it.copy(protectionNotificationsEnabled = isChecked) }
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "DigitalPause adheres to strict calm notification policies. You will never receive marketing pings, guilt-inducing alerts, or intrusive notifications.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun NotificationToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = 0.12f))
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(text = title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}
