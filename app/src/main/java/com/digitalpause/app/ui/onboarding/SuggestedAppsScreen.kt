package com.digitalpause.app.ui.onboarding

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.digitalpause.app.DigitalPauseApp
import com.digitalpause.app.model.AppLimit
import com.digitalpause.app.model.AppUsageInfo
import com.digitalpause.app.ui.components.AppIcon
import com.digitalpause.app.ui.theme.PillShape
import com.digitalpause.app.ui.theme.extendedColors

/**
 * Onboarding Step 4: Suggested Apps.
 * Suggests distraction apps that are ACTUALLY installed on the user's phone.
 * STRICT REQUIREMENT: Suggested != Limited.
 * A limit is NEVER created automatically; only explicitly selected and confirmed apps receive limits.
 */
@Composable
fun SuggestedAppsScreen(
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val usageRepo = remember { DigitalPauseApp.instance.usageStatsRepository }
    val prefsManager = remember { DigitalPauseApp.instance.preferencesManager }

    var suggestedApps by remember { mutableStateOf<List<AppUsageInfo>>(emptyList()) }
    // Map of selected package -> selected limit minutes (default 30m)
    val selectedLimits = remember { mutableStateMapOf<String, Int>() }

    LaunchedEffect(Unit) {
        val installed = usageRepo.getInstalledLaunchableApps()
        val popularPackages = listOf(
            "com.instagram.android",
            "com.google.android.youtube",
            "com.whatsapp",
            "com.facebook.katana",
            "com.snapchat.android",
            "org.telegram.messenger",
            "com.twitter.android",
            "com.reddit.frontpage",
            "com.netflix.mediaclient",
            "com.zhiliaoapp.musically",
            "com.ss.android.ugc.trill"
        )
        // Find installed apps that match popular distraction apps
        val matched = installed.filter { app -> popularPackages.contains(app.packageName) }
        suggestedApps = if (matched.isNotEmpty()) {
            matched
        } else {
            // Fallback to top 5 installed launchable apps
            installed.take(5)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        // Step Navigation Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
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

            Text(
                text = "Suggested Apps",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            TextButton(onClick = onContinue) {
                Text(
                    text = "Skip",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hero Explanation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.extendedColors.surfaceContainerLowest)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Icon(
                    imageVector = Icons.Filled.HourglassTop,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "Optional App Limits",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Choose which apps you want to limit. No limits are set automatically.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Installed on your device:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        // List of Installed Suggested Apps
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(suggestedApps, key = { it.packageName }) { app ->
                val isSelected = selectedLimits.containsKey(app.packageName)
                val currentLimit = selectedLimits[app.packageName] ?: 30

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                            else MaterialTheme.extendedColors.surfaceContainerLowest
                        )
                        .clickable {
                            if (isSelected) {
                                selectedLimits.remove(app.packageName)
                            } else {
                                selectedLimits[app.packageName] = 30 // default 30 min if selected
                            }
                        }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            AppIcon(
                                packageName = app.packageName,
                                size = 42.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = app.appName,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (isSelected) "Limit: ${AppUsageInfo.formatMinutes(currentLimit)} / day" else "No limit",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Checkbox pill
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.extendedColors.surfaceContainerHigh
                                )
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // If selected, show duration picker
                    if (isSelected) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(15 to "15m", 30 to "30m", 60 to "1h").forEach { (mins, label) ->
                                val isPresetActive = currentLimit == mins
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(PillShape)
                                        .background(
                                            if (isPresetActive) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.extendedColors.surfaceContainerLow
                                        )
                                        .clickable { selectedLimits[app.packageName] = mins }
                                        .padding(vertical = 6.dp)
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isPresetActive) Color.White else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (isPresetActive) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action Buttons
        Button(
            onClick = {
                // Save limits ONLY for apps explicitly checked
                for ((pkg, limitMins) in selectedLimits) {
                    val app = suggestedApps.firstOrNull { it.packageName == pkg }
                    val name = app?.appName ?: usageRepo.getAppName(pkg)
                    prefsManager.saveLimit(
                        AppLimit(
                            packageName = pkg,
                            appName = name,
                            dailyLimitMinutes = limitMins
                        )
                    )
                }
                onContinue()
            },
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            val count = selectedLimits.size
            Text(
                text = if (count > 0) "Save $count ${if (count == 1) "Limit" else "Limits"} & Continue" else "Continue without Limits",
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        TextButton(
            onClick = onContinue,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = "Skip for now",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
