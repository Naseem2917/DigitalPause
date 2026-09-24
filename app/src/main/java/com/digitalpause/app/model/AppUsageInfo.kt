package com.digitalpause.app.model

/**
 * Usage metrics calculated from Android UsageStatsManager.
 */
data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val usageMinutesToday: Int,
    val usageMinutesYesterday: Int = 0,
    val launchCount: Int = 0,
    val isSystemApp: Boolean = false
) {
    fun formattedUsageToday(): String = formatMinutes(usageMinutesToday)
    fun formattedUsageYesterday(): String = formatMinutes(usageMinutesYesterday)

    companion object {
        fun formatMinutes(totalMinutes: Int): String {
            if (totalMinutes <= 0) return "0m"
            val hours = totalMinutes / 60
            val mins = totalMinutes % 60
            return when {
                hours > 0 && mins > 0 -> "${hours}h ${mins}m"
                hours > 0 -> "${hours}h"
                else -> "${mins}m"
            }
        }

        fun formatDurationDetailed(totalMinutes: Int): String {
            if (totalMinutes <= 0) return "0 minutes"
            val hours = totalMinutes / 60
            val mins = totalMinutes % 60
            return when {
                hours > 0 && mins > 0 -> "$hours ${if (hours == 1) "hr" else "hrs"}, $mins ${if (mins == 1) "min" else "mins"}"
                hours > 0 -> "$hours ${if (hours == 1) "hr" else "hrs"}"
                else -> "$mins ${if (mins == 1) "minute" else "minutes"}"
            }
        }
    }
}

data class DayScreenTime(
    val dayLabel: String, // "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"
    val fullDateLabel: String, // "Thu, 24 Sept"
    val startMillis: Long,
    val endMillis: Long,
    val totalMinutes: Int,
    val isToday: Boolean = false
)

data class DailyScreenTimeSummary(
    val todayTotalMinutes: Int = 0,
    val yesterdayTotalMinutes: Int = 0,
    val unlockCount: Int = 0,
    val mostUsedApps: List<AppUsageInfo> = emptyList(),
    val isDataAvailable: Boolean = true,
    val statusMessage: String? = null
) {
    val differenceMinutes: Int
        get() = todayTotalMinutes - yesterdayTotalMinutes

    val isMoreThanYesterday: Boolean
        get() = differenceMinutes > 0

    fun formattedToday(): String = AppUsageInfo.formatMinutes(todayTotalMinutes)
    fun formattedYesterday(): String = AppUsageInfo.formatMinutes(yesterdayTotalMinutes)

    fun formattedDifference(): String {
        val absDiff = kotlin.math.abs(differenceMinutes)
        val formatted = AppUsageInfo.formatMinutes(absDiff)
        return if (isMoreThanYesterday) "$formatted more than yesterday" else "$formatted less than yesterday"
    }
}
