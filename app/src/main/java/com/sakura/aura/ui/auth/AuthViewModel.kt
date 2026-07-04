package com.sakura.aura.ui.auth

import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakura.aura.data.remote.TokenManager
import com.sakura.aura.domain.model.LoginCredentials
import com.sakura.aura.domain.model.RegistrationData
import com.sakura.aura.domain.usecase.LoginUseCase
import com.sakura.aura.domain.usecase.RegisterUseCase
import com.sakura.aura.security.BiometricCipherHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.crypto.Cipher
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
    private val registerUseCase: RegisterUseCase,
    private val tokenManager: TokenManager,
    private val biometricCipherHelper: BiometricCipherHelper
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

    fun isBiometricEnabled(): Boolean = tokenManager.isBiometricEnabled()

    fun getCipherForDecryption(): Cipher? {
        val ivString = tokenManager.getBiometricIv() ?: return null
        return try {
            val iv = Base64.decode(ivString, Base64.NO_WRAP)
            biometricCipherHelper.getCipherForDecryption(iv)
        } catch (e: Exception) {
            null
        }
    }

    fun loginBiometrically(cipher: Cipher) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val username = tokenManager.getBiometricUsername()
            val encryptedPassword = tokenManager.getBiometricPassword()

            if (username.isNullOrEmpty() || encryptedPassword.isNullOrEmpty()) {
                _uiState.value = AuthUiState.Error("Credenciales biométricas no encontradas.")
                return@launch
            }

            try {
                val decryptedPassword = biometricCipherHelper.decrypt(encryptedPassword, cipher)
                val result = loginUseCase(LoginCredentials(username, decryptedPassword))
                result.fold(
                    onSuccess = { auth ->
                        _uiState.value = AuthUiState.Success(auth.token, auth.user.username)
                    },
                    onFailure = { e ->
                        _uiState.value = AuthUiState.Error(e.message ?: "Error al iniciar sesión con huella")
                    }
                )
            } catch (e: Exception) {
                // Si la llave fue invalidada o hay error al desencriptar, limpiamos la huella
                tokenManager.clearBiometricCredentials()
                biometricCipherHelper.removeSecretKey()
                _uiState.value = AuthUiState.Error("La huella digital cambió o fue invalidada. Inicia sesión con tu contraseña de nuevo.")
            }
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
