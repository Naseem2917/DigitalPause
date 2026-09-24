package com.digitalpause.app.ui.limits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digitalpause.app.DigitalPauseApp
import com.digitalpause.app.model.AppLimit
import com.digitalpause.app.model.AppUsageInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AppLimitsUiState(
    val limits: List<AppLimit> = emptyList(),
    val usageMap: Map<String, Int> = emptyMap(),
    val installedApps: List<AppUsageInfo> = emptyList(),
    val isLoading: Boolean = false,
    val selectedAppForLimit: AppUsageInfo? = null,
    val saveSuccess: Boolean = false
)

class AppLimitsViewModel : ViewModel() {

    private val prefsManager = DigitalPauseApp.instance.preferencesManager
    private val usageRepo = DigitalPauseApp.instance.usageStatsRepository

    private val _uiState = MutableStateFlow(AppLimitsUiState(isLoading = true))
    val uiState: StateFlow<AppLimitsUiState> = _uiState.asStateFlow()

    init {
        loadData()
        observeLimits()
    }

    private fun observeLimits() {
        viewModelScope.launch {
            prefsManager.limitsFlow.collect { limits ->
                _uiState.value = _uiState.value.copy(limits = limits)
            }
        }
    }

    fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            val limits = prefsManager.limitsFlow.value
            val usage = mutableMapOf<String, Int>()
            for (limit in limits) {
                usage[limit.packageName] = usageRepo.getUsageMinutesToday(limit.packageName)
            }
            val installed = usageRepo.getInstalledLaunchableApps()
            _uiState.value = _uiState.value.copy(
                limits = limits,
                usageMap = usage,
                installedApps = installed,
                isLoading = false
            )
        }
    }

    fun saveLimit(packageName: String, appName: String, dailyLimitMinutes: Int) {
        if (dailyLimitMinutes <= 0) return
        val existing = prefsManager.getLimitForPackage(packageName)
        val limit = existing?.copy(dailyLimitMinutes = dailyLimitMinutes) ?: AppLimit(
            packageName = packageName,
            appName = appName,
            dailyLimitMinutes = dailyLimitMinutes,
            isEnabled = true
        )
        prefsManager.saveLimit(limit)
        loadData()
    }

    fun toggleLimitEnabled(limit: AppLimit) {
        prefsManager.saveLimit(limit.copy(isEnabled = !limit.isEnabled))
    }

    fun deleteLimit(packageName: String) {
        prefsManager.removeLimit(packageName)
        loadData()
    }
}
