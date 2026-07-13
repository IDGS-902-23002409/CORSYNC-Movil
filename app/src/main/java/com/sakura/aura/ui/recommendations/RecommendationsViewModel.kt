package com.sakura.aura.ui.recommendations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakura.aura.domain.model.RecommendationsPackage
import com.sakura.aura.domain.usecase.GetRecommendationsUseCase
import com.sakura.aura.domain.usecase.UpdateChallengeProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecommendationsUiState(
    val isLoading: Boolean = true,
    val recommendationsPackage: RecommendationsPackage? = null,
    val acceptedChallengeId: Int? = null,
    val challengeMessage: String? = null,
    val error: String? = null
)

@HiltViewModel
class RecommendationsViewModel @Inject constructor(
    private val getRecommendationsUseCase: GetRecommendationsUseCase,
    private val updateChallengeProgressUseCase: UpdateChallengeProgressUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecommendationsUiState())
    val uiState: StateFlow<RecommendationsUiState> = _uiState.asStateFlow()

    init {
        loadRecommendations()
    }

    fun loadRecommendations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getRecommendationsUseCase().fold(
                onSuccess = { pack ->
                    _uiState.update { it.copy(isLoading = false, recommendationsPackage = pack, error = null) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun acceptSuggestedChallenge(challengeId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            updateChallengeProgressUseCase(challengeId, 1).fold(
                onSuccess = { challenge ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            acceptedChallengeId = challengeId,
                            challengeMessage = "¡Desafío '${challenge.title}' aceptado y progresado con éxito!"
                        ) 
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(challengeMessage = null, acceptedChallengeId = null) }
    }
}
