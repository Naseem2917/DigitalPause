package com.digitalpause.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digitalpause.app.DigitalPauseApp
import com.digitalpause.app.model.AppLimit
import com.digitalpause.app.model.AppSettings
import com.digitalpause.app.model.DailyScreenTimeSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val summary: DailyScreenTimeSummary = DailyScreenTimeSummary(),
    val limits: List<AppLimit> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val isLoading: Boolean = false,
    val isFocusBreathActive: Boolean = false
)

class DashboardViewModel : ViewModel() {

    private val usageRepo = DigitalPauseApp.instance.usageStatsRepository
    private val prefsManager = DigitalPauseApp.instance.preferencesManager

    private val _uiState = MutableStateFlow(DashboardUiState(isLoading = true))
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadData()
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            prefsManager.limitsFlow.collect { limits ->
                _uiState.value = _uiState.value.copy(limits = limits)
            }
        }
        viewModelScope.launch {
            prefsManager.settingsFlow.collect { settings ->
                _uiState.value = _uiState.value.copy(settings = settings)
            }
        }
    }

    fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            prefsManager.checkDailyReset()
            val summary = usageRepo.getDailySummary()
            _uiState.value = _uiState.value.copy(
                summary = summary,
                isLoading = false
            )
        }
    }

    fun toggleShortVideoProtection() {
        val current = _uiState.value.settings.isShortVideoProtectionEnabled
        prefsManager.updateSettings { it.copy(isShortVideoProtectionEnabled = !current) }
    }

    fun toggleAppLimitsProtection() {
        val current = _uiState.value.settings.isAppLimitsProtectionEnabled
        prefsManager.updateSettings { it.copy(isAppLimitsProtectionEnabled = !current) }
    }

    fun startFocusBreath() {
        _uiState.value = _uiState.value.copy(isFocusBreathActive = true)
    }

    fun dismissFocusBreath() {
        _uiState.value = _uiState.value.copy(isFocusBreathActive = false)
    }
}
