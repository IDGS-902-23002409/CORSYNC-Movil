package com.sakura.aura.ui.challenges

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakura.aura.data.model.response.ChallengeResponse
import com.sakura.aura.data.model.response.MedalResponse
import com.sakura.aura.domain.repository.ChallengesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChallengesUiState(
    val isLoading: Boolean = true,
    val challenges: List<ChallengeResponse> = emptyList(),
    val medals: List<MedalResponse> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ChallengesViewModel @Inject constructor(
    private val challengesRepository: ChallengesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChallengesUiState())
    val uiState: StateFlow<ChallengesUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            challengesRepository.getChallenges().fold(
                onSuccess = { challenges ->
                    _uiState.update { it.copy(isLoading = false, challenges = challenges) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
        viewModelScope.launch {
            challengesRepository.getMedals().fold(
                onSuccess = { medals ->
                    _uiState.update { it.copy(medals = medals) }
                },
                onFailure = { }
            )
        }
    }
}
