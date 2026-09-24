package com.digitalpause.app.data

import android.content.Context
import com.digitalpause.app.model.AppLimit
import com.digitalpause.app.model.AppSettings
import com.digitalpause.app.model.TemporaryAccess
import com.digitalpause.app.model.TemporaryAccessType
import com.digitalpause.app.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Local-first persistence manager for DigitalPause limits, temporary access grants,
 * and user settings.
 */
class PreferencesManager(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(loadSettingsInternal())
    val settingsFlow: StateFlow<AppSettings> = _settingsFlow.asStateFlow()

    private val _limitsFlow = MutableStateFlow(loadLimitsInternal())
    val limitsFlow: StateFlow<List<AppLimit>> = _limitsFlow.asStateFlow()

    private val _temporaryAccessFlow = MutableStateFlow(loadTemporaryAccessInternal())
    val temporaryAccessFlow: StateFlow<List<TemporaryAccess>> = _temporaryAccessFlow.asStateFlow()

    init {
        runDataMigration()
        checkDailyReset()
    }

    // ==========================================
    // SETTINGS MANAGEMENT
    // ==========================================

    private fun loadSettingsInternal(): AppSettings {
        return AppSettings(
            isOnboardingCompleted = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false),
            isShortVideoProtectionEnabled = prefs.getBoolean(KEY_SHORT_VIDEO_ENABLED, true),
            isAppLimitsProtectionEnabled = prefs.getBoolean(KEY_APP_LIMITS_ENABLED, true),
            themeMode = ThemeMode.valueOf(prefs.getString(KEY_THEME_MODE, ThemeMode.LIGHT.name) ?: ThemeMode.LIGHT.name),
            limitRemindersEnabled = prefs.getBoolean(KEY_LIMIT_REMINDERS, true),
            nearLimitRemindersEnabled = prefs.getBoolean(KEY_NEAR_LIMIT_REMINDERS, true),
            protectionNotificationsEnabled = prefs.getBoolean(KEY_PROTECTION_NOTIFS, true),
            lastResetDate = prefs.getString(KEY_LAST_RESET_DATE, "") ?: ""
        )
    }

    fun updateSettings(transform: (AppSettings) -> AppSettings) {
        val current = _settingsFlow.value
        val updated = transform(current)
        prefs.edit()
            .putBoolean(KEY_ONBOARDING_COMPLETED, updated.isOnboardingCompleted)
            .putBoolean(KEY_SHORT_VIDEO_ENABLED, updated.isShortVideoProtectionEnabled)
            .putBoolean(KEY_APP_LIMITS_ENABLED, updated.isAppLimitsProtectionEnabled)
            .putString(KEY_THEME_MODE, updated.themeMode.name)
            .putBoolean(KEY_LIMIT_REMINDERS, updated.limitRemindersEnabled)
            .putBoolean(KEY_NEAR_LIMIT_REMINDERS, updated.nearLimitRemindersEnabled)
            .putBoolean(KEY_PROTECTION_NOTIFS, updated.protectionNotificationsEnabled)
            .putString(KEY_LAST_RESET_DATE, updated.lastResetDate)
            .apply()
        _settingsFlow.value = updated
    }

    // ==========================================
    // APP LIMITS MANAGEMENT
    // ==========================================

    private fun loadLimitsInternal(): List<AppLimit> {
        val jsonStr = prefs.getString(KEY_APP_LIMITS_JSON, null) ?: return emptyList()
        val list = mutableListOf<AppLimit>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    AppLimit(
                        packageName = obj.getString("packageName"),
                        appName = obj.getString("appName"),
                        dailyLimitMinutes = obj.getInt("dailyLimitMinutes"),
                        extraTimeUsedMinutes = obj.optInt("extraTimeUsedMinutes", 0),
                        isEnabled = obj.optBoolean("isEnabled", true),
                        createdAtMillis = obj.optLong("createdAtMillis", System.currentTimeMillis())
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun saveLimitsInternal(limits: List<AppLimit>) {
        val array = JSONArray()
        for (limit in limits) {
            val obj = JSONObject()
            obj.put("packageName", limit.packageName)
            obj.put("appName", limit.appName)
            obj.put("dailyLimitMinutes", limit.dailyLimitMinutes)
            obj.put("extraTimeUsedMinutes", limit.extraTimeUsedMinutes)
            obj.put("isEnabled", limit.isEnabled)
            obj.put("createdAtMillis", limit.createdAtMillis)
            array.put(obj)
        }
        prefs.edit().putString(KEY_APP_LIMITS_JSON, array.toString()).apply()
        _limitsFlow.value = limits
    }

    fun saveLimit(newOrUpdated: AppLimit) {
        // Safety guard: never save a limit with 0 or negative minutes
        if (newOrUpdated.dailyLimitMinutes <= 0) return
        val current = _limitsFlow.value.toMutableList()
        val index = current.indexOfFirst { it.packageName == newOrUpdated.packageName }
        if (index >= 0) {
            current[index] = newOrUpdated
        } else {
            current.add(newOrUpdated)
        }
        saveLimitsInternal(current)
    }

    fun removeLimit(packageName: String) {
        val current = _limitsFlow.value.filter { it.packageName != packageName }
        saveLimitsInternal(current)
        clearTemporaryAccess(packageName)
    }

    fun updateExtraTime(packageName: String, additionalMinutes: Int) {
        val current = _limitsFlow.value.map {
            if (it.packageName == packageName) {
                it.copy(extraTimeUsedMinutes = it.extraTimeUsedMinutes + additionalMinutes)
            } else it
        }
        saveLimitsInternal(current)
    }

    fun getLimitForPackage(packageName: String): AppLimit? {
        return _limitsFlow.value.firstOrNull { it.packageName == packageName && it.isEnabled }
    }

    // ==========================================
    // TEMPORARY ACCESS GRANTS (EXTRA TIME / OVERRIDE)
    // ==========================================

    private fun loadTemporaryAccessInternal(): List<TemporaryAccess> {
        val jsonStr = prefs.getString(KEY_TEMP_ACCESS_JSON, null) ?: return emptyList()
        val list = mutableListOf<TemporaryAccess>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val access = TemporaryAccess(
                    packageName = obj.getString("packageName"),
                    type = TemporaryAccessType.valueOf(obj.getString("type")),
                    grantedAtMillis = obj.getLong("grantedAtMillis"),
                    durationMinutes = obj.getInt("durationMinutes"),
                    expiresAtMillis = obj.getLong("expiresAtMillis")
                )
                if (!access.isExpired()) {
                    list.add(access)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun saveTemporaryAccessInternal(accessList: List<TemporaryAccess>) {
        val validList = accessList.filter { !it.isExpired() }
        val array = JSONArray()
        for (item in validList) {
            val obj = JSONObject()
            obj.put("packageName", item.packageName)
            obj.put("type", item.type.name)
            obj.put("grantedAtMillis", item.grantedAtMillis)
            obj.put("durationMinutes", item.durationMinutes)
            obj.put("expiresAtMillis", item.expiresAtMillis)
            array.put(obj)
        }
        prefs.edit().putString(KEY_TEMP_ACCESS_JSON, array.toString()).apply()
        _temporaryAccessFlow.value = validList
    }

    fun grantTemporaryAccess(access: TemporaryAccess) {
        val current = _temporaryAccessFlow.value.filter { it.packageName != access.packageName }.toMutableList()
        current.add(access)
        saveTemporaryAccessInternal(current)

        if (access.type == TemporaryAccessType.EXTRA_TIME) {
            updateExtraTime(access.packageName, access.durationMinutes)
        }
    }

    fun getActiveTemporaryAccess(packageName: String): TemporaryAccess? {
        val active = _temporaryAccessFlow.value.firstOrNull { it.packageName == packageName && !it.isExpired() }
        if (active == null) {
            // Clean up any stale expired entry
            val filtered = _temporaryAccessFlow.value.filter { !it.isExpired() }
            if (filtered.size != _temporaryAccessFlow.value.size) {
                saveTemporaryAccessInternal(filtered)
            }
        }
        return active
    }

    fun clearTemporaryAccess(packageName: String) {
        val current = _temporaryAccessFlow.value.filter { it.packageName != packageName }
        saveTemporaryAccessInternal(current)
    }

    fun getDailyFreeExtraTimeUsed(): Int {
        checkDailyReset()
        return prefs.getInt(KEY_DAILY_FREE_EXTRA_TIME_USED, 0)
    }

    fun addDailyFreeExtraTimeUsed(minutes: Int) {
        checkDailyReset()
        val current = prefs.getInt(KEY_DAILY_FREE_EXTRA_TIME_USED, 0)
        prefs.edit().putInt(KEY_DAILY_FREE_EXTRA_TIME_USED, current + minutes).apply()
    }

    // ==========================================
    // DATA MIGRATION
    // ==========================================

    /**
     * Clears stale data from previous app versions.
     * Previous builds may have auto-created limits (e.g. Instagram) during onboarding.
     * We detect version bumps via DATA_VERSION key and wipe limits on upgrade.
     */
    private fun runDataMigration() {
        val storedVersion = prefs.getInt(KEY_DATA_VERSION, 0)
        if (storedVersion < CURRENT_DATA_VERSION) {
            // Migration: remove any limits that exist with 0 or negative dailyLimitMinutes
            // (these are leftover from bugs in earlier builds)
            val validLimits = _limitsFlow.value.filter { it.dailyLimitMinutes > 0 }
            if (validLimits.size != _limitsFlow.value.size) {
                saveLimitsInternal(validLimits)
            }
            prefs.edit().putInt(KEY_DATA_VERSION, CURRENT_DATA_VERSION).apply()
        }
    }

    // ==========================================
    // DAILY RESET LOGIC
    // ==========================================

    fun checkDailyReset() {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastReset = _settingsFlow.value.lastResetDate

        if (lastReset != todayStr) {
            // New day has arrived! Reset extra time and prune expired temporary access
            val currentLimits = _limitsFlow.value.map { it.copy(extraTimeUsedMinutes = 0) }
            saveLimitsInternal(currentLimits)
            saveTemporaryAccessInternal(emptyList())

            prefs.edit().putInt(KEY_DAILY_FREE_EXTRA_TIME_USED, 0).apply()

            updateSettings { it.copy(lastResetDate = todayStr) }
        }
    }

    companion object {
        const val MAX_DAILY_FREE_EXTRA_TIME_MINUTES = 30

        // Bump this integer whenever a breaking data migration is needed
        private const val CURRENT_DATA_VERSION = 2

        private const val PREFS_NAME = "digitalpause_preferences"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_SHORT_VIDEO_ENABLED = "short_video_protection_enabled"
        private const val KEY_APP_LIMITS_ENABLED = "app_limits_protection_enabled"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_LIMIT_REMINDERS = "limit_reminders_enabled"
        private const val KEY_NEAR_LIMIT_REMINDERS = "near_limit_reminders_enabled"
        private const val KEY_PROTECTION_NOTIFS = "protection_notifications_enabled"
        private const val KEY_LAST_RESET_DATE = "last_reset_date"
        private const val KEY_APP_LIMITS_JSON = "app_limits_json"
        private const val KEY_TEMP_ACCESS_JSON = "temporary_access_json"
        private const val KEY_DAILY_FREE_EXTRA_TIME_USED = "daily_free_extra_time_used"
        private const val KEY_DATA_VERSION = "data_version"
    }
}
