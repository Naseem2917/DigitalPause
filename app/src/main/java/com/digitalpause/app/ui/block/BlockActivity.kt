package com.digitalpause.app.ui.block

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.digitalpause.app.ui.components.AppIcon
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digitalpause.app.DigitalPauseApp
import com.digitalpause.app.model.AppUsageInfo
import com.digitalpause.app.model.TemporaryAccess
import com.digitalpause.app.model.TemporaryAccessType
import com.digitalpause.app.ui.components.MindfulBreathVisualizer
import com.digitalpause.app.ui.theme.CounterHeroMobile
import com.digitalpause.app.ui.theme.DigitalPauseTheme
import com.digitalpause.app.ui.theme.HeadlineLgMobile
import com.digitalpause.app.ui.theme.PillShape
import com.digitalpause.app.ui.theme.extendedColors
import kotlinx.coroutines.delay

enum class BlockMode {
    DAILY_LIMIT,
    SHORT_VIDEO
}

class BlockActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: ""
        val appName = intent.getStringExtra(EXTRA_APP_NAME) ?: "Protected App"
        val modeStr = intent.getStringExtra(EXTRA_BLOCK_MODE) ?: BlockMode.DAILY_LIMIT.name
        val mode = try { BlockMode.valueOf(modeStr) } catch (e: Exception) { BlockMode.DAILY_LIMIT }
        val limitMinutes = intent.getIntExtra(EXTRA_LIMIT_MINUTES, 60)
        val usedMinutes = intent.getIntExtra(EXTRA_USED_MINUTES, 60)

        setContent {
            DigitalPauseTheme {
                BlockScreenContent(
                    packageName = packageName,
                    appName = appName,
                    mode = mode,
                    limitMinutes = limitMinutes,
                    usedMinutes = usedMinutes,
                    onGoBack = {
                        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_HOME)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(homeIntent)
                        finish()
                    },
                    onTemporaryAccessGranted = {
                        finish()
                    }
                )
            }
        }
    }

    companion object {
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        const val EXTRA_APP_NAME = "extra_app_name"
        const val EXTRA_BLOCK_MODE = "extra_block_mode"
        const val EXTRA_LIMIT_MINUTES = "extra_limit_minutes"
        const val EXTRA_USED_MINUTES = "extra_used_minutes"
    }
}

@Composable
fun BlockScreenContent(
    packageName: String,
    appName: String,
    mode: BlockMode,
    limitMinutes: Int,
    usedMinutes: Int,
    onGoBack: () -> Unit,
    onTemporaryAccessGranted: () -> Unit
) {
    val prefsManager = remember { DigitalPauseApp.instance.preferencesManager }
    val passwordManager = remember { DigitalPauseApp.instance.passwordManager }

    var showExtraTimeDialog by remember { mutableStateOf(false) }
    var showParentOverrideDialog by remember { mutableStateOf(false) }
    var showParentPasswordForExtraTime by remember { mutableStateOf(false) }
    var pendingExtraMinutes by remember { mutableIntStateOf(15) }

    val freeExtraTimeUsed = prefsManager.getDailyFreeExtraTimeUsed()
    val remainingFreeMinutes = maxOf(0, com.digitalpause.app.data.PreferencesManager.MAX_DAILY_FREE_EXTRA_TIME_MINUTES - freeExtraTimeUsed)

    if (showExtraTimeDialog) {
        ExtraTimeSelectionDialog(
            appName = appName,
            remainingFreeMinutes = remainingFreeMinutes,
            onDismiss = { showExtraTimeDialog = false },
            onSelectMinutes = { extraMins ->
                showExtraTimeDialog = false
                if (extraMins <= remainingFreeMinutes) {
                    // Free extra time allowed within 30 min daily maximum
                    prefsManager.addDailyFreeExtraTimeUsed(extraMins)
                    prefsManager.grantTemporaryAccess(
                        TemporaryAccess(
                            packageName = packageName,
                            type = TemporaryAccessType.EXTRA_TIME,
                            durationMinutes = extraMins
                        )
                    )
                    onTemporaryAccessGranted()
                } else {
                    // Allowance exceeded -> Require Parent Password
                    pendingExtraMinutes = extraMins
                    showParentPasswordForExtraTime = true
                }
            }
        )
    }

    if (showParentPasswordForExtraTime) {
        ParentOverrideFlowDialog(
            appName = appName,
            passwordManager = passwordManager,
            customTitle = "Parent Approval Required",
            customSubtitle = "Free daily extra time limit reached ($freeExtraTimeUsed/30m used today). Ask your parent/guardian to enter their PIN to authorize $pendingExtraMinutes min extra time.",
            onDismiss = { showParentPasswordForExtraTime = false },
            onOverrideGranted = { _ ->
                showParentPasswordForExtraTime = false
                prefsManager.grantTemporaryAccess(
                    TemporaryAccess(
                        packageName = packageName,
                        type = TemporaryAccessType.EXTRA_TIME,
                        durationMinutes = pendingExtraMinutes
                    )
                )
                onTemporaryAccessGranted()
            }
        )
    }

    if (showParentOverrideDialog) {
        ParentOverrideFlowDialog(
            appName = appName,
            passwordManager = passwordManager,
            onDismiss = { showParentOverrideDialog = false },
            onOverrideGranted = { durationMins ->
                showParentOverrideDialog = false
                prefsManager.grantTemporaryAccess(
                    TemporaryAccess(
                        packageName = packageName,
                        type = TemporaryAccessType.PARENT_OVERRIDE,
                        durationMinutes = durationMins
                    )
                )
                onTemporaryAccessGranted()
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Ambient Breathing Concentric Rings Visual
        MindfulBreathVisualizer(size = 210.dp, centerCaption = "Breathe")

        Spacer(modifier = Modifier.height(16.dp))

        // Target App Pill & Stat Summary
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .clip(PillShape)
                .background(MaterialTheme.extendedColors.surfaceContainer)
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            AppIcon(
                packageName = packageName,
                size = 22.dp
            )
            Text(
                text = if (mode == BlockMode.SHORT_VIDEO) "$appName Shorts" else appName,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(MaterialTheme.colorScheme.outline))
            Text(
                text = if (mode == BlockMode.DAILY_LIMIT)
                    "${AppUsageInfo.formatMinutes(usedMinutes)} / ${AppUsageInfo.formatMinutes(limitMinutes)}"
                else "Short-form feed",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Status Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .clip(PillShape)
                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                .padding(horizontal = 12.dp, vertical = 5.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Spa,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = if (mode == BlockMode.DAILY_LIMIT) "Daily Limit Reached • Rest & Reflect"
                else "Short Video Shield Active",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Headline & Empathetic Narrative
        Text(
            text = if (mode == BlockMode.DAILY_LIMIT) "Take a Pause" else "Pause Before Scrolling",
            style = HeadlineLgMobile.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (mode == BlockMode.DAILY_LIMIT)
                "$appName's daily limit has been reached. Step away for a moment, take a deep breath, and check in with your focus."
            else
                "Short-form video feeds are designed to capture your focus. Take a 5-second breath before choosing whether to continue.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Non-judgmental Affirmation / Mindful Quote Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.extendedColors.surfaceContainerLow)
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Filled.WbSunny,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "“Mindful moments are not lost time, but space created for what matters.”",
                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Daily Wisdom • Digital Vitality",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Primary Action: Calm Disconnection (Go Back)
        Button(
            onClick = onGoBack,
            shape = PillShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Go Back",
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Secondary Safe Extension Options (Grid)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { showExtraTimeDialog = true },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Icon(imageVector = Icons.Filled.HourglassTop, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Extra 15 min", style = MaterialTheme.typography.labelMedium)
            }

            Button(
                onClick = { showParentOverrideDialog = true },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.extendedColors.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Icon(imageVector = Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Parent Override", style = MaterialTheme.typography.labelMedium)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Supportive Calm Schedule Reset Note
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "Limits reset automatically tonight at 12:00 AM.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun ExtraTimeSelectionDialog(
    appName: String,
    remainingFreeMinutes: Int,
    onDismiss: () -> Unit,
    onSelectMinutes: (Int) -> Unit
) {
    var selectedMinutes by remember { mutableIntStateOf(15) } // 15 min default (PRD Section 16)

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Take a Mindful Extension",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Choose a small controlled extension for $appName. Once ended, protection resumes immediately.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Free Extra Time Daily Allowance Badge (PRD Section 16 & User Requirement)
            Box(
                modifier = Modifier
                    .clip(PillShape)
                    .background(
                        if (remainingFreeMinutes > 0) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                        else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
                    )
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(
                    text = if (remainingFreeMinutes > 0) "Free Allowance Remaining: ${remainingFreeMinutes}m / 30m"
                    else "Daily 30m Free Limit Reached (Parent PIN required)",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (remainingFreeMinutes > 0) MaterialTheme.colorScheme.onSecondaryContainer
                    else MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Options: 5m, 15m, 30m
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(5 to "5 min", 15 to "15 min (Default)", 30 to "30 min").forEach { (mins, label) ->
                    val isSelected = selectedMinutes == mins
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.secondaryContainer
                                else MaterialTheme.extendedColors.surfaceContainerLow
                            )
                            .clickable { selectedMinutes = mins }
                            .padding(vertical = 12.dp)
                    ) {
                        Text(
                            text = if (mins == 15) "15 min" else label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
                            else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            val requiresPassword = selectedMinutes > remainingFreeMinutes

            Button(
                onClick = { onSelectMinutes(selectedMinutes) },
                shape = PillShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (requiresPassword) MaterialTheme.colorScheme.tertiaryContainer
                    else MaterialTheme.colorScheme.primaryContainer,
                    contentColor = if (requiresPassword) MaterialTheme.colorScheme.onTertiaryContainer
                    else MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                if (requiresPassword) {
                    Icon(imageVector = Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Parent PIN Required ($selectedMinutes min)", style = MaterialTheme.typography.labelLarge)
                } else {
                    Text(text = "Grant Extra $selectedMinutes min (Free)", style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun ParentOverrideFlowDialog(
    appName: String,
    passwordManager: com.digitalpause.app.data.PasswordManager,
    customTitle: String? = null,
    customSubtitle: String? = null,
    onDismiss: () -> Unit,
    onOverrideGranted: (Int) -> Unit
) {
    var step by remember { mutableIntStateOf(1) } // 1 = PIN check, 2 = duration select
    var pin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedDuration by remember { mutableIntStateOf(15) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (step == 1) {
                // Screen 18: Parent Override Password
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = customTitle ?: "Parent/Guardian Override",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = customSubtitle ?: "Ask your parent or guardian to enter their secret PIN to authorize temporary access for $appName.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // PIN Dots
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.extendedColors.surfaceContainerLow)
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (i in 0 until 6) {
                        val isFilled = i < pin.length
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isFilled) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant
                                )
                        )
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mini Keypad
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val rows = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("C", "0", "OK")
                    )
                    for (row in rows) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (item in row) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.extendedColors.surfaceContainerHigh)
                                        .clickable {
                                            errorMessage = null
                                            when (item) {
                                                "C" -> if (pin.isNotEmpty()) pin = pin.dropLast(1)
                                                "OK" -> {
                                                    if (passwordManager.verifyPassword(pin)) {
                                                        if (customTitle != null) onOverrideGranted(15) else step = 2
                                                    } else {
                                                        errorMessage = "Incorrect password. Try Again."
                                                        pin = ""
                                                    }
                                                }
                                                else -> {
                                                    if (pin.length < 6) pin += item
                                                    if (pin.length == 6) {
                                                        if (passwordManager.verifyPassword(pin)) {
                                                            if (customTitle != null) onOverrideGranted(15) else step = 2
                                                        } else {
                                                            errorMessage = "Incorrect password. Try Again."
                                                            pin = ""
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                ) {
                                    Text(
                                        text = item,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

            } else {
                // Screen 19: Override Duration
                Text(
                    text = "Select Override Duration",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "How long should access be allowed? Protection will automatically return after this time.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Options: 15m, 30m, 1 hour (PRD Section 20)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(15 to "15 minutes", 30 to "30 minutes", 60 to "1 hour").forEach { (dur, label) ->
                        val isSelected = selectedDuration == dur
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.secondaryContainer
                                    else MaterialTheme.extendedColors.surfaceContainerLow
                                )
                                .clickable { selectedDuration = dur }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.Spa,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { onOverrideGranted(selectedDuration) },
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(text = "Authorize Temporary Access", style = MaterialTheme.typography.labelLarge)
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
