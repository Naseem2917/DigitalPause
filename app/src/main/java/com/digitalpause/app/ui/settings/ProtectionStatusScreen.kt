package com.digitalpause.app.ui.settings

import android.content.Intent
import android.provider.Settings
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.digitalpause.app.DigitalPauseApp
import com.digitalpause.app.service.DigitalPauseAccessibilityService
import com.digitalpause.app.ui.theme.PillShape
import com.digitalpause.app.ui.theme.extendedColors

@Composable
fun ProtectionStatusScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val prefsManager = remember { DigitalPauseApp.instance.preferencesManager }
    val usageRepo = remember { DigitalPauseApp.instance.usageStatsRepository }
    val settings by prefsManager.settingsFlow.collectAsState()

    var hasUsageAccess by remember { mutableStateOf(usageRepo.hasUsagePermission()) }
    var hasAccessibility by remember { mutableStateOf(DigitalPauseAccessibilityService.isRunning) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasUsageAccess = usageRepo.hasUsagePermission()
                hasAccessibility = DigitalPauseAccessibilityService.isRunning
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val isAppLimitsActive = settings.isAppLimitsProtectionEnabled && hasAccessibility && hasUsageAccess
    val isShortVideoActive = settings.isShortVideoProtectionEnabled && hasAccessibility

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
                Text(text = "Shield Health", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "Protection Status", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Overall Shield State Banner
        val allShieldsGood = hasUsageAccess && hasAccessibility
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(
                    if (allShieldsGood) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    else MaterialTheme.extendedColors.tertiaryFixed.copy(alpha = 0.4f)
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (allShieldsGood) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary)
            ) {
                Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = if (allShieldsGood) "Protection Fully Operational" else "Permissions Need Attention",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (allShieldsGood) "All boundaries are actively enforced locally on device."
                    else "Some permissions are disabled. Tap 'Fix' below to enable them.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "SYSTEM PERMISSIONS",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        // Permission Items
        ProtectionCard(
            title = "Usage Access",
            description = "Enables real-time tracking of screen-time quotas and session pacing.",
            isEnabled = hasUsageAccess,
            icon = Icons.Filled.QueryStats,
            onAction = {
                try {
                    context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                } catch (e: Exception) {
                    context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        ProtectionCard(
            title = "Accessibility Service",
            description = "Enables boundary enforcement when app limits are reached and reels load.",
            isEnabled = hasAccessibility,
            icon = Icons.Filled.AccessibilityNew,
            onAction = {
                try {
                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                } catch (e: Exception) {
                    context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "ACTIVE SHIELDS",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        ProtectionCard(
            title = "App Limits",
            description = "Monitors daily foreground usage for all limited study applications.",
            isEnabled = isAppLimitsActive,
            icon = Icons.Filled.HourglassTop,
            actionLabel = "Configure"
        )

        Spacer(modifier = Modifier.height(10.dp))

        ProtectionCard(
            title = "Short Video Protection",
            description = "Detects and intercepts YouTube Shorts & Instagram Reels feeds.",
            isEnabled = isShortVideoActive,
            icon = Icons.Filled.PlayCircle,
            actionLabel = "Configure"
        )
    }
}

@Composable
private fun ProtectionCard(
    title: String,
    description: String,
    isEnabled: Boolean,
    icon: ImageVector,
    actionLabel: String = "Fix",
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.extendedColors.surfaceContainerLowest)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isEnabled) MaterialTheme.colorScheme.secondaryContainer
                    else MaterialTheme.extendedColors.surfaceContainerHigh
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isEnabled) MaterialTheme.colorScheme.onSecondaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (isEnabled) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .clip(PillShape)
                    .background(MaterialTheme.extendedColors.secondaryFixed.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(imageVector = Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(12.dp))
                Text(text = "Active", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.extendedColors.onSecondaryFixed, fontWeight = FontWeight.Bold)
            }
        } else {
            if (onAction != null) {
                Button(
                    onClick = onAction,
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(text = actionLabel, style = MaterialTheme.typography.labelSmall)
                }
            } else {
                Text(
                    text = "Paused",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier
                        .clip(PillShape)
                        .background(MaterialTheme.extendedColors.surfaceContainerHigh)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
