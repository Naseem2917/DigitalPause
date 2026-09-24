package com.digitalpause.app.ui.activity

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.HourglassFull
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digitalpause.app.DigitalPauseApp
import com.digitalpause.app.model.AppLimit
import com.digitalpause.app.model.AppUsageInfo
import com.digitalpause.app.model.DayScreenTime
import com.digitalpause.app.ui.components.AppIcon
import com.digitalpause.app.ui.theme.PillShape
import com.digitalpause.app.ui.theme.extendedColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Stitch Screens 6 & 7 / App Activity Details.
 * Displays genuine foreground app activity with weekly bar chart and real application metadata.
 */
@Composable
fun AppActivityDetailsScreen(
    onBack: () -> Unit,
    onAppClick: (String) -> Unit
) {
    val usageRepo = remember { DigitalPauseApp.instance.usageStatsRepository }
    val prefsManager = remember { DigitalPauseApp.instance.preferencesManager }
    val limits by prefsManager.limitsFlow.collectAsState()

    var weeklyDays by remember { mutableStateOf<List<DayScreenTime>>(emptyList()) }
    var selectedDayIndex by remember { mutableIntStateOf(-1) }
    var selectedDayTotalMinutes by remember { mutableIntStateOf(0) }
    var appList by remember { mutableStateOf<List<AppUsageInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val days = usageRepo.getWeeklyDays()
            weeklyDays = days
            // Default to today or the last active day
            val todayIdx = days.indexOfFirst { it.isToday }.let { if (it >= 0) it else days.size - 1 }
            selectedDayIndex = todayIdx
        }
    }

    LaunchedEffect(selectedDayIndex, weeklyDays) {
        if (selectedDayIndex in weeklyDays.indices) {
            isLoading = true
            val day = weeklyDays[selectedDayIndex]
            withContext(Dispatchers.IO) {
                val (total, apps) = usageRepo.getUsageListForRange(day.startMillis, day.endMillis)
                selectedDayTotalMinutes = total
                appList = apps
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = "App activity details",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )

            IconButton(onClick = { /* Menu */ }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Menu",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))

                // Filter Pill ("Screen time v")
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 22.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Screen time ▾",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Big Headline Duration
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = AppUsageInfo.formatDurationDetailed(selectedDayTotalMinutes),
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val selectedDay = weeklyDays.getOrNull(selectedDayIndex)
                    Text(
                        text = if (selectedDay?.isToday == true) "Today" else selectedDay?.fullDateLabel ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Weekly Bar Chart (Mon - Sun)
                WeeklyBarChart(
                    days = weeklyDays,
                    selectedIndex = selectedDayIndex,
                    onSelectDay = { idx -> selectedDayIndex = idx }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Day Navigator (< Thu, 24 Sept >)
                val selectedDay = weeklyDays.getOrNull(selectedDayIndex)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (selectedDayIndex > 0) selectedDayIndex--
                        },
                        enabled = selectedDayIndex > 0
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Previous Day",
                            tint = if (selectedDayIndex > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant
                        )
                    }

                    Text(
                        text = selectedDay?.fullDateLabel ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    IconButton(
                        onClick = {
                            if (selectedDayIndex < weeklyDays.size - 1) selectedDayIndex++
                        },
                        enabled = selectedDayIndex < weeklyDays.size - 1
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next Day",
                            tint = if (selectedDayIndex < weeklyDays.size - 1) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // App Usage Items List
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Loading real device usage...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (appList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No app activity recorded for this day.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(appList, key = { it.packageName }) { app ->
                    val matchingLimit = limits.firstOrNull { it.packageName == app.packageName }
                    val hasLimit = matchingLimit != null

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onAppClick(app.packageName) }
                            .padding(vertical = 12.dp, horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            AppIcon(
                                packageName = app.packageName,
                                size = 44.dp
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = app.appName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = AppUsageInfo.formatDurationDetailed(app.usageMinutesToday),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Hourglass icon for setting/viewing limit
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .clickable { onAppClick(app.packageName) }
                        ) {
                            Icon(
                                imageVector = if (hasLimit) Icons.Filled.HourglassFull else Icons.Filled.HourglassEmpty,
                                contentDescription = "Manage Limit",
                                tint = if (hasLimit) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

/**
 * Renders weekly vertical bar chart matching Stitch Screens 6 & 7 and Android system wellbeing style.
 */
@Composable
private fun WeeklyBarChart(
    days: List<DayScreenTime>,
    selectedIndex: Int,
    onSelectDay: (Int) -> Unit
) {
    val maxMinutes = maxOf(9 * 60, (days.maxOfOrNull { it.totalMinutes } ?: 0) + 60)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.extendedColors.surfaceContainerLowest)
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            // Horizontal reference guide lines for 0h, 3h, 6h, 9h
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("9 h", "6 h", "3 h", "0 h").forEach { label ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Bars row
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 30.dp), // Clearance for the right text labels
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                days.forEachIndexed { index, day ->
                    val isSelected = index == selectedIndex
                    val heightRatio = if (maxMinutes > 0) (day.totalMinutes.toFloat() / maxMinutes.toFloat()).coerceIn(0.04f, 1f) else 0.04f

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelectDay(index) }
                    ) {
                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .height((110 * heightRatio).dp)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(
                                    when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        day.totalMinutes > 0 -> MaterialTheme.colorScheme.secondaryContainer
                                        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                                    }
                                )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Days of Week Labels (Mon, Tue, Wed, Thu, Fri, Sat, Sun)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 30.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            days.forEachIndexed { index, day ->
                val isSelected = index == selectedIndex
                Text(
                    text = day.dayLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
