package com.sakura.aura.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakura.aura.data.remote.TokenManager
import com.sakura.aura.domain.model.LoginCredentials
import com.sakura.aura.domain.model.UserProfile
import com.sakura.aura.domain.model.UserStats
import com.sakura.aura.domain.usecase.GetUserProfileUseCase
import com.sakura.aura.domain.usecase.GetUserStatsUseCase
import com.sakura.aura.domain.usecase.LoginUseCase
import com.sakura.aura.domain.usecase.LogoutUseCase
import com.sakura.aura.security.BiometricCipherHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.crypto.Cipher
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: UserProfile? = null,
    val stats: UserStats? = null,
    val error: String? = null,
    val isLoggingOut: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val isVerifyingPassword: Boolean = false,
    val passwordError: String? = null,
    val passwordSuccess: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getUserStatsUseCase: GetUserStatsUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val tokenManager: TokenManager,
    private val loginUseCase: LoginUseCase,
    private val biometricCipherHelper: BiometricCipherHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
        loadStats()
        _uiState.update { it.copy(isBiometricEnabled = tokenManager.isBiometricEnabled()) }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getUserProfileUseCase().fold(
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
            getUserStatsUseCase().fold(
                onSuccess = { stats ->
                    _uiState.update { it.copy(stats = stats) }
                },
                onFailure = { }
            )
        }
    }

    fun verifyPassword(password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isVerifyingPassword = true, passwordError = null, passwordSuccess = false) }
            val username = tokenManager.getUsername() ?: ""
            val result = loginUseCase(LoginCredentials(username, password))
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isVerifyingPassword = false, passwordSuccess = true) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isVerifyingPassword = false, passwordError = e.message ?: "Contraseña incorrecta") }
                }
            )
        }
    }

    fun enableBiometric(cipher: Cipher, passwordVerified: String) {
        val username = tokenManager.getUsername() ?: ""
        try {
            val encryptedData = biometricCipherHelper.encrypt(passwordVerified, cipher)
            tokenManager.saveBiometricCredentials(
                username = username,
                passwordCipher = encryptedData.cipherText,
                iv = encryptedData.iv
            )
            _uiState.update { it.copy(isBiometricEnabled = true, passwordSuccess = false, passwordError = null) }
        } catch (e: Exception) {
            _uiState.update { it.copy(passwordError = "Error al cifrar credenciales biométricas") }
        }
    }

    fun disableBiometric() {
        tokenManager.clearBiometricCredentials()
        biometricCipherHelper.removeSecretKey()
        _uiState.update { it.copy(isBiometricEnabled = false) }
    }

    fun clearPasswordVerificationState() {
        _uiState.update { it.copy(passwordError = null, passwordSuccess = false, isVerifyingPassword = false) }
    }

    fun getCipherForEncryption(): Cipher? {
        return biometricCipherHelper.getCipherForEncryption()
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingOut = true) }
            logoutUseCase()
            _uiState.update { it.copy(isLoggingOut = false) }
        }
    }
}
