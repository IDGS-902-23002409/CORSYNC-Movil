package com.sakura.aura.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakura.aura.data.model.request.LogoutRequest
import com.sakura.aura.data.model.response.UserResponse
import com.sakura.aura.data.model.response.UserStatsResponse
import com.sakura.aura.data.remote.TokenManager
import com.sakura.aura.domain.repository.AuthRepository
import com.sakura.aura.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: UserResponse? = null,
    val stats: UserStatsResponse? = null,
    val error: String? = null,
    val isLoggingOut: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
        loadStats()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            userRepository.getProfile().fold(
                onSuccess = { user ->
                    _uiState.update { it.copy(isLoading = false, user = user) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            userRepository.getStats().fold(
                onSuccess = { stats ->
                    _uiState.update { it.copy(stats = stats) }
                },
                onFailure = { }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingOut = true) }
            val jwt = tokenManager.getJwtToken()
            val refresh = tokenManager.getRefreshToken()
            if (jwt != null && refresh != null) {
                authRepository.logout(LogoutRequest(jwt, refresh))
            } else {
                tokenManager.clearAll()
            }
            _uiState.update { it.copy(isLoggingOut = false) }
        }
    }
}
