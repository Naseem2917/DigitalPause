package com.digitalpause.app.data

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.util.Log
import com.digitalpause.app.model.AppUsageInfo
import com.digitalpause.app.model.DailyScreenTimeSummary
import com.digitalpause.app.model.DayScreenTime
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Accesses Android's UsageStatsManager / UsageEvents to produce genuine foreground screen-time metrics.
 *
 * KEY DESIGN DECISIONS to match Digital Wellbeing:
 * 1. Use queryEvents() – NOT queryUsageStats(). The latter returns overlapping buckets that
 *    can sum to multiples of actual time (seen as 145h, 1008h in screenshots).
 * 2. Track a SINGLE foreground app at a time. When the screen turns off (SCREEN_NON_INTERACTIVE
 *    or KEYGUARD_SHOWN) we finalize the session and set currentApp = null AND
 *    isScreenOn = false. When screen turns back on we do NOT immediately resume the app —
 *    we wait for the next ACTIVITY_RESUMED event.
 * 3. Midnight crossing: we clamp session start to max(sessionStart, queryStart).
 * 4. Trailing session: ONLY close if isScreenOn == true. If the screen is off the trailing
 *    session is already finalized.
 */
class UsageStatsRepository(private val context: Context) {

    private val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
    private val packageManager = context.packageManager

    fun hasUsagePermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun getDailySummary(): DailyScreenTimeSummary {
        if (!hasUsagePermission() || usageStatsManager == null) {
            return DailyScreenTimeSummary(
                isDataAvailable = false,
                statusMessage = "Screen-time data is temporarily unavailable. Usage access is required."
            )
        }

        val (todayStart, todayEnd) = getTodayRange()
        val (yesterdayStart, yesterdayEnd) = getYesterdayRange()

        val todayUsageMap = calculateForegroundUsageMap(todayStart, todayEnd)
        val yesterdayUsageMap = calculateForegroundUsageMap(yesterdayStart, yesterdayEnd)

        val totalTodayMillis = todayUsageMap.values.sum()
        val totalYesterdayMillis = yesterdayUsageMap.values.sum()

        // Clamp totals: can never exceed the query window
        val todayWindowMillis = todayEnd - todayStart
        val yesterdayWindowMillis = yesterdayEnd - yesterdayStart

        val safeToday = totalTodayMillis.coerceAtMost(todayWindowMillis)
        val safeYesterday = totalYesterdayMillis.coerceAtMost(yesterdayWindowMillis)

        val todayMinutes = (safeToday / (1000 * 60)).toInt()
        val yesterdayMinutes = (safeYesterday / (1000 * 60)).toInt()

        Log.d(TAG, "Today: ${todayMinutes}min | Yesterday: ${yesterdayMinutes}min")

        val mostUsed = todayUsageMap.entries
            .filter { it.value > 0 }
            .sortedByDescending { it.value }
            .take(10)
            .map { (pkg, millis) ->
                val usedMins = (millis / (1000 * 60)).toInt()
                val yestMins = ((yesterdayUsageMap[pkg] ?: 0L) / (1000 * 60)).toInt()
                Log.d(TAG, "  App $pkg: ${usedMins}min today, ${yestMins}min yesterday")
                AppUsageInfo(
                    packageName = pkg,
                    appName = getAppName(pkg),
                    usageMinutesToday = usedMins,
                    usageMinutesYesterday = yestMins
                )
            }

        val unlocks = calculateUnlocks(todayStart, todayEnd)

        return DailyScreenTimeSummary(
            todayTotalMinutes = todayMinutes,
            yesterdayTotalMinutes = yesterdayMinutes,
            unlockCount = unlocks,
            mostUsedApps = mostUsed,
            isDataAvailable = true
        )
    }

    fun getUsageListForRange(startTime: Long, endTime: Long): Pair<Int, List<AppUsageInfo>> {
        if (!hasUsagePermission() || usageStatsManager == null) {
            return Pair(0, emptyList())
        }

        val usageMap = calculateForegroundUsageMap(startTime, endTime)
        val windowMillis = endTime - startTime
        val totalMillis = usageMap.values.sum().coerceAtMost(windowMillis)
        val totalMinutes = (totalMillis / (1000 * 60)).toInt()

        val list = usageMap.entries
            .filter { it.value >= 60 * 1000L }
            .sortedByDescending { it.value }
            .map { (pkg, millis) ->
                val minutes = (millis / (1000 * 60)).toInt()
                AppUsageInfo(
                    packageName = pkg,
                    appName = getAppName(pkg),
                    usageMinutesToday = minutes
                )
            }

        return Pair(totalMinutes, list)
    }

    fun getWeeklyDays(): List<DayScreenTime> {
        val days = mutableListOf<DayScreenTime>()
        val cal = Calendar.getInstance()
        val currentDayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        val currentYear = cal.get(Calendar.YEAR)

        cal.firstDayOfWeek = Calendar.MONDAY
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val fullDateFormat = SimpleDateFormat("EEE, d MMM", Locale.getDefault())
        val now = System.currentTimeMillis()

        for (i in 0..6) {
            val startMillis = cal.timeInMillis
            val dayLabel = dayFormat.format(cal.time)
            val fullDateLabel = fullDateFormat.format(cal.time)
            val isToday = (cal.get(Calendar.DAY_OF_YEAR) == currentDayOfYear && cal.get(Calendar.YEAR) == currentYear)
            val isFuture = cal.timeInMillis > now

            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MILLISECOND, 999)
            val endMillis = minOf(cal.timeInMillis, now)

            val totalMinutes = if (!isFuture && hasUsagePermission()) {
                val usageMap = calculateForegroundUsageMap(startMillis, endMillis)
                val windowMs = endMillis - startMillis
                val totalMs = usageMap.values.sum().coerceAtMost(windowMs)
                (totalMs / (1000 * 60)).toInt()
            } else {
                0
            }

            days.add(
                DayScreenTime(
                    dayLabel = dayLabel,
                    fullDateLabel = fullDateLabel,
                    startMillis = startMillis,
                    endMillis = endMillis,
                    totalMinutes = totalMinutes,
                    isToday = isToday
                )
            )

            cal.add(Calendar.DAY_OF_YEAR, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
        }

        return days
    }

    /**
     * Core foreground-time calculation using UsageEvents.
     *
     * State machine:
     *   isScreenOn: tracks whether the screen is currently interactive (not locked/off).
     *   currentFgApp: the package currently in foreground. null if screen is off or no app started.
     *   fgStart: timestamp when currentFgApp entered the foreground.
     *
     * Rules:
     *   ACTIVITY_RESUMED:
     *     - If there was a previous foreground app (and screen on), finalize its session.
     *     - Set currentFgApp = this package, fgStart = event time.
     *   ACTIVITY_PAUSED:
     *     - If currentFgApp == this package and screen is on, finalize session. currentFgApp = null.
     *     - (Do NOT use "midnight boundary" hack — it causes huge overcounting.)
     *   SCREEN_NON_INTERACTIVE / KEYGUARD_SHOWN (screen locked/off):
     *     - Finalize any active session. isScreenOn = false. currentFgApp = null.
     *   SCREEN_INTERACTIVE / KEYGUARD_HIDDEN (screen unlocked):
     *     - isScreenOn = true. Do NOT resume any app — wait for ACTIVITY_RESUMED.
     *   Trailing close (after all events):
     *     - ONLY if isScreenOn == true AND currentFgApp != null (screen still on).
     */
    fun calculateForegroundUsageMap(startTime: Long, endTime: Long): Map<String, Long> {
        val manager = usageStatsManager ?: return emptyMap()
        val usageMap = mutableMapOf<String, Long>()
        val excludedPackages = getExcludedPackageNames()

        try {
            val events = manager.queryEvents(startTime, endTime)
            val event = UsageEvents.Event()

            var currentFgApp: String? = null
            var fgStart = 0L
            var isScreenOn = true  // Assume screen on at start of day

            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                val time = event.timeStamp
                val pkg = event.packageName ?: continue

                when (event.eventType) {
                    UsageEvents.Event.ACTIVITY_RESUMED -> {
                        if (excludedPackages.contains(pkg)) {
                            // Finalise the previous app when excluded app comes to foreground
                            if (currentFgApp != null && isScreenOn) {
                                val duration = time - fgStart
                                if (duration in 1..(endTime - startTime)) {
                                    usageMap[currentFgApp!!] = (usageMap[currentFgApp!!] ?: 0L) + duration
                                    Log.v(TAG, "Finalize (replaced by excluded) ${currentFgApp}: +${duration/1000}s")
                                }
                                currentFgApp = null
                            }
                            continue
                        }

                        // Finalize previous app if any
                        if (currentFgApp != null && currentFgApp != pkg && isScreenOn) {
                            val duration = time - fgStart
                            if (duration in 1..(endTime - startTime)) {
                                usageMap[currentFgApp!!] = (usageMap[currentFgApp!!] ?: 0L) + duration
                                Log.v(TAG, "Finalize (replaced) ${currentFgApp}: +${duration/1000}s")
                            }
                        }

                        currentFgApp = pkg
                        // Clamp start to query window start (midnight boundary crossing)
                        fgStart = maxOf(time, startTime)
                        isScreenOn = true
                    }

                    UsageEvents.Event.ACTIVITY_PAUSED -> {
                        if (currentFgApp == pkg && isScreenOn) {
                            val duration = time - fgStart
                            if (duration in 1..(endTime - startTime) && !excludedPackages.contains(pkg)) {
                                usageMap[pkg] = (usageMap[pkg] ?: 0L) + duration
                                Log.v(TAG, "Finalize (paused) $pkg: +${duration/1000}s")
                            }
                            currentFgApp = null
                        }
                    }

                    UsageEvents.Event.SCREEN_NON_INTERACTIVE,
                    UsageEvents.Event.KEYGUARD_SHOWN -> {
                        // Screen going off: finalize current session
                        if (currentFgApp != null && isScreenOn) {
                            val duration = time - fgStart
                            if (duration in 1..(endTime - startTime) && !excludedPackages.contains(currentFgApp!!)) {
                                usageMap[currentFgApp!!] = (usageMap[currentFgApp!!] ?: 0L) + duration
                                Log.v(TAG, "Finalize (screen off) ${currentFgApp}: +${duration/1000}s")
                            }
                        }
                        currentFgApp = null
                        isScreenOn = false
                    }

                    UsageEvents.Event.SCREEN_INTERACTIVE,
                    UsageEvents.Event.KEYGUARD_HIDDEN -> {
                        // Screen came back on — do NOT resume app; wait for ACTIVITY_RESUMED
                        isScreenOn = true
                        // Reset currentFgApp since the user was on lockscreen
                        currentFgApp = null
                    }
                }
            }

            // Trailing close: ONLY if screen is still on AND an app is still in foreground
            if (currentFgApp != null && isScreenOn && !excludedPackages.contains(currentFgApp!!)) {
                val duration = endTime - fgStart
                if (duration in 1..(endTime - startTime)) {
                    usageMap[currentFgApp!!] = (usageMap[currentFgApp!!] ?: 0L) + duration
                    Log.v(TAG, "Finalize (trailing) ${currentFgApp}: +${duration/1000}s")
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error querying usage events: ${e.message}", e)
        }

        return usageMap
    }

    fun getUsageMinutesToday(packageName: String): Int {
        if (!hasUsagePermission() || usageStatsManager == null) return 0
        val (todayStart, todayEnd) = getTodayRange()
        val usageMap = calculateForegroundUsageMap(todayStart, todayEnd)
        val millis = usageMap[packageName] ?: 0L
        return (millis / (1000 * 60)).toInt()
    }

    fun calculateUnlocks(startTime: Long, endTime: Long): Int {
        val manager = usageStatsManager ?: return 0
        return try {
            val events = manager.queryEvents(startTime, endTime)
            val event = UsageEvents.Event()
            var count = 0
            var lastUnlockTime = 0L

            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                if (event.eventType == UsageEvents.Event.KEYGUARD_HIDDEN) {
                    val time = event.timeStamp
                    if (time - lastUnlockTime > 2000L) {
                        count++
                        lastUnlockTime = time
                    }
                }
            }
            count
        } catch (e: Exception) {
            Log.e(TAG, "Error querying unlock events", e)
            0
        }
    }

    fun getInstalledLaunchableApps(): List<AppUsageInfo> {
        val launchable = getLaunchablePackageNames()
        val excluded = getExcludedPackageNames()
        val list = mutableListOf<AppUsageInfo>()
        val (todayStart, todayEnd) = getTodayRange()
        val todayUsage = calculateForegroundUsageMap(todayStart, todayEnd)

        for (pkg in launchable) {
            if (excluded.contains(pkg)) continue
            val usageMillis = todayUsage[pkg] ?: 0L
            list.add(
                AppUsageInfo(
                    packageName = pkg,
                    appName = getAppName(pkg),
                    usageMinutesToday = (usageMillis / (1000 * 60)).toInt()
                )
            )
        }
        return list.sortedBy { it.appName.lowercase() }
    }

    private fun getLaunchablePackageNames(): Set<String> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                intent,
                PackageManager.ResolveInfoFlags.of(0L)
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(intent, 0)
        }
        return resolveInfos.map { it.activityInfo.packageName }.toSet()
    }

    private fun getHomePackageNames(): Set<String> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                intent,
                PackageManager.ResolveInfoFlags.of(0L)
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(intent, 0)
        }
        return resolveInfos.map { it.activityInfo.packageName }.toSet()
    }

    /**
     * All packages that should NEVER be counted in screen-time:
     * - DigitalPause itself (debug and release)
     * - Android OS internals
     * - SystemUI
     * - Launchers / home screens
     * - Common system settings packages
     */
    private fun getExcludedPackageNames(): Set<String> {
        val homePackages = getHomePackageNames()
        val alwaysExcluded = setOf(
            context.packageName,
            "com.digitalpause.app",
            "com.digitalpause.app.debug",
            "android",
            "com.android.systemui",
            "com.android.launcher",
            "com.android.launcher2",
            "com.android.launcher3",
            "com.google.android.apps.nexuslauncher",
            "com.sec.android.app.launcher",
            "com.oppo.launcher",
            "com.coloros.launcher",
            "com.miui.home",
            "com.huawei.android.launcher",
            "com.oneplus.launcher",
            "com.realme.launcher",
            "com.google.android.permissioncontroller",
            "com.android.settings",
            "com.android.phone",
            "com.android.dialer"
        )
        return alwaysExcluded + homePackages
    }

    fun getAppName(packageName: String): String {
        return try {
            val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getApplicationInfo(
                    packageName,
                    PackageManager.ApplicationInfoFlags.of(0L)
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getApplicationInfo(packageName, 0)
            }
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName.substringAfterLast('.')
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }

    fun getTodayRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        val end = System.currentTimeMillis()
        return Pair(start, end)
    }

    fun getYesterdayRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis
        return Pair(start, end)
    }

    companion object {
        private const val TAG = "UsageStatsRepo"
    }
}
