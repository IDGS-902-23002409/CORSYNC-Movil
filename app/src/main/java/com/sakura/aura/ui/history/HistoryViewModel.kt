package com.sakura.aura.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakura.aura.data.model.response.ReadingResponse
import com.sakura.aura.data.model.response.ReadingSummaryResponse
import com.sakura.aura.domain.repository.ReadingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val isLoading: Boolean = true,
    val readings: List<ReadingResponse> = emptyList(),
    val summary: ReadingSummaryResponse? = null,
    val error: String? = null
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val readingsRepository: ReadingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            readingsRepository.getReadings().fold(
                onSuccess = { readings ->
                    _uiState.update { it.copy(isLoading = false, readings = readings) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
        viewModelScope.launch {
            readingsRepository.getSummary().fold(
                onSuccess = { summary ->
                    _uiState.update { it.copy(summary = summary) }
                },
                onFailure = { }
            )
        }
    }
}
