package com.sakura.aura.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.security.crypto.EncryptedSharedPreferences
import com.sakura.aura.data.model.request.LoginRequest
import com.sakura.aura.data.model.request.RegisterRequest
import com.sakura.aura.data.remote.ApiService
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
    private val apiService: ApiService,
    private val prefs: EncryptedSharedPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // ── Login ──────────────────────────────────────────────────────────────
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val response = apiService.login(LoginRequest(username, password))
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    // Guardar token de forma segura
                    prefs.edit()
                        .putString("jwt_token", body.token)
                        .putString("username", body.user.username)
                        .putString("nombre_completo", body.user.nombreCompleto)
                        .apply()
                    _uiState.value = AuthUiState.Success(body.token, body.user.username)
                } else {
                    _uiState.value = AuthUiState.Error(
                        when (response.code()) {
                            401  -> "Usuario o contraseña incorrectos"
                            else -> "Error ${response.code()}"
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Sin conexión: ${e.message}")
            }
        }
    }

    // ── Register ───────────────────────────────────────────────────────────
    fun register(username: String, email: String, password: String, nombreCompleto: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val response = apiService.register(
                    RegisterRequest(username, email, password, nombreCompleto)
                )
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    prefs.edit()
                        .putString("jwt_token", body.token)
                        .putString("username", body.user.username)
                        .putString("nombre_completo", body.user.nombreCompleto)
                        .apply()
                    _uiState.value = AuthUiState.Success(body.token, body.user.username)
                } else {
                    _uiState.value = AuthUiState.Error(
                        when (response.code()) {
                            400  -> "Datos inválidos o contraseña muy débil"
                            409  -> "El usuario o correo ya están registrados"
                            else -> "Error ${response.code()}"
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Sin conexión: ${e.message}")
            }
        }
    }

    fun resetState() { _uiState.value = AuthUiState.Idle }
}