package com.sakura.aura.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakura.aura.domain.model.Telemetry
import com.sakura.aura.domain.usecase.ScanAuraUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isConnected  : Boolean       = false,
    val isScanning   : Boolean       = false,
    val telemetry    : Telemetry?    = null,
    val auraColor    : AuraColorUi   = AuraColorUi.NEUTRAL,
    val error        : String?       = null
)

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
    private val scanAuraUseCase: ScanAuraUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            scanAuraUseCase.isConnected.collect { connected ->
                _uiState.update { it.copy(isConnected = connected) }
            }
        }

        viewModelScope.launch {
            scanAuraUseCase.telemetry.collect { telemetry ->
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

        viewModelScope.launch {
            scanAuraUseCase.error.collect { error ->
                if (error != null) {
                    _uiState.update { it.copy(error = error) }
                }
            }
        }
    }

    fun connect() {
        viewModelScope.launch {
            try {
                scanAuraUseCase.connect()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error de conexión: ${e.message}") }
            }
        }
    }

    fun startScan() {
        if (!_uiState.value.isConnected) {
            connect()
        }
        viewModelScope.launch {
            try {
                scanAuraUseCase.startScan()
                _uiState.update { it.copy(isScanning = true, error = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al iniciar: ${e.message}") }
            }
        }
    }

    fun stopScan() {
        viewModelScope.launch {
            try {
                scanAuraUseCase.stopScan()
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
        scanAuraUseCase.disconnect()
    }
}
