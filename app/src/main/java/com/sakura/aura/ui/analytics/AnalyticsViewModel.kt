package com.sakura.aura.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakura.aura.domain.model.Distribution
import com.sakura.aura.domain.model.Trends
import com.sakura.aura.domain.model.WeekComparison
import com.sakura.aura.domain.usecase.GetComparisonUseCase
import com.sakura.aura.domain.usecase.GetDistributionUseCase
import com.sakura.aura.domain.usecase.GetTrendsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AnalyticsUiState(
    val isLoading: Boolean = true,
    val selectedPeriod: String = "weekly",
    val trends: Trends? = null,
    val distribution: Distribution? = null,
    val comparison: WeekComparison? = null,
    val error: String? = null
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val getTrendsUseCase: GetTrendsUseCase,
    private val getDistributionUseCase: GetDistributionUseCase,
    private val getComparisonUseCase: GetComparisonUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadAllData()
    }

    fun loadAllData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val period = _uiState.value.selectedPeriod
            
            val trendsResult = getTrendsUseCase(period)
            val distResult = getDistributionUseCase()
            val compResult = getComparisonUseCase()
            
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    trends = trendsResult.getOrNull(),
                    distribution = distResult.getOrNull(),
                    comparison = compResult.getOrNull(),
                    error = if (trendsResult.isFailure) trendsResult.exceptionOrNull()?.message else null
                )
            }
        }
    }

    fun changePeriod(period: String) {
        _uiState.update { it.copy(selectedPeriod = period) }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getTrendsUseCase(period).fold(
                onSuccess = { trends ->
                    _uiState.update { it.copy(isLoading = false, trends = trends) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }
}
