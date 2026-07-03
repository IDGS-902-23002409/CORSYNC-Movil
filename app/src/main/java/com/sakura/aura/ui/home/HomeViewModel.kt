package com.sakura.aura.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.security.crypto.EncryptedSharedPreferences
import com.sakura.aura.data.model.response.TelemetryResponse
import com.sakura.aura.data.remote.SignalRService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── Estado de la pantalla Home ────────────────────────────────────────────────
data class HomeUiState(
    val isConnected  : Boolean           = false,
    val isScanning   : Boolean           = false,
    val telemetry    : TelemetryResponse? = null,
    val auraColor    : AuraColorUi       = AuraColorUi.NEUTRAL,
    val error        : String?           = null
)

// ── Colores de Aura mapeados desde el string del backend ──────────────────────
enum class AuraColorUi(val hex: Long, val label: String) {
    NEUTRAL (0xFFCCCCCC, "Sin datos"),
    ROJA    (0xFFE74C3C, "Roja"),
    AZUL    (0xFF5DADE2, "Azul"),
    VERDE   (0xFF2ECC71, "Verde"),
    VIOLETA (0xFF9B59B6, "Violeta"),
    NARANJA (0xFFE67E22, "Naranja"),
    ROSA    (0xFFE91E8C, "Rosa");

    companion object {
        fun fromString(value: String): AuraColorUi = entries.firstOrNull {
            it.label.equals(value, ignoreCase = true)
        } ?: NEUTRAL
    }
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val signalRService : SignalRService,
    private val prefs          : EncryptedSharedPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // Observar estado de conexión SignalR
        viewModelScope.launch {
            signalRService.isConnected.collect { connected ->
                _uiState.update { it.copy(isConnected = connected) }
            }
        }

        // Observar telemetría entrante
        viewModelScope.launch {
            signalRService.telemetry.collect { telemetry ->
                telemetry?.let {
                    _uiState.update { state ->
                        state.copy(
                            telemetry  = telemetry,
                            auraColor  = AuraColorUi.fromString(telemetry.aura)
                        )
                    }
                }
            }
        }

        // Observar errores
        viewModelScope.launch {
            signalRService.error.collect { error ->
                if (error != null) {
                    _uiState.update { it.copy(error = error) }
                }
            }
        }
    }

    // ── Conectar y registrar en el Hub ────────────────────────────────────
    fun connect() {
        val token = prefs.getString("jwt_token", null)
        if (token == null) {
            _uiState.update { it.copy(error = "No hay sesión activa") }
            return
        }
        viewModelScope.launch {
            try {
                signalRService.connect(token)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error de conexión: ${e.message}") }
            }
        }
    }

    // ── Iniciar escaneo ───────────────────────────────────────────────────
    fun startScan() {
        if (!_uiState.value.isConnected) {
            connect()   // conectar primero si no está conectado
        }
        viewModelScope.launch {
            try {
                signalRService.startMeasurement()
                _uiState.update { it.copy(isScanning = true, error = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al iniciar: ${e.message}") }
            }
        }
    }

    // ── Detener escaneo ───────────────────────────────────────────────────
    fun stopScan() {
        viewModelScope.launch {
            try {
                signalRService.stopMeasurement()
                _uiState.update { it.copy(isScanning = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al detener: ${e.message}") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        signalRService.disconnect()
    }
}