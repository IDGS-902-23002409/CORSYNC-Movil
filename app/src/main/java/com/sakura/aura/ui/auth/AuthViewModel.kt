package com.sakura.aura.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakura.aura.domain.model.LoginCredentials
import com.sakura.aura.domain.model.RegistrationData
import com.sakura.aura.domain.usecase.LoginUseCase
import com.sakura.aura.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthUiState {
    object Idle    : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val token: String, val username: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = loginUseCase(LoginCredentials(username, password))
            result.fold(
                onSuccess = { auth ->
                    _uiState.value = AuthUiState.Success(auth.token, auth.user.username)
                },
                onFailure = { e ->
                    _uiState.value = AuthUiState.Error(e.message ?: "Error al iniciar sesión")
                }
            )
        }
    }

    fun register(username: String, email: String, password: String, nombreCompleto: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = registerUseCase(RegistrationData(username, email, password, nombreCompleto))
            result.fold(
                onSuccess = { auth ->
                    _uiState.value = AuthUiState.Success(auth.token, auth.user.username)
                },
                onFailure = { e ->
                    _uiState.value = AuthUiState.Error(e.message ?: "Error al registrarse")
                }
            )
        }
    }

    fun resetState() { _uiState.value = AuthUiState.Idle }
}
