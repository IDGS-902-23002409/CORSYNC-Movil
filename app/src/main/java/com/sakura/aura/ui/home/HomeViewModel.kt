package com.sakura.aura.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakura.aura.data.remote.AppConfig
import com.sakura.aura.domain.model.NewReadingData
import com.sakura.aura.domain.model.Telemetry
import com.sakura.aura.domain.usecase.SaveReadingUseCase
import com.sakura.aura.domain.usecase.ScanAuraUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
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
    private val scanAuraUseCase: ScanAuraUseCase,
    private val saveReadingUseCase: SaveReadingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val sessionTelemetries = mutableListOf<Telemetry>()
    private var sessionStartDate: Instant? = null

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
                    if (_uiState.value.isScanning) {
                        sessionTelemetries.add(telemetry)
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
        sessionTelemetries.clear()
        sessionStartDate = Instant.now()
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
                saveSessionReading()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al detener: ${e.message}") }
            }
        }
    }

    private fun saveSessionReading() {
        val endDate = Instant.now()
        val startDate = sessionStartDate ?: endDate.minusSeconds(10)
        val durationSeconds = Duration.between(startDate, endDate).seconds.toInt().coerceAtLeast(1)

        if (sessionTelemetries.isNotEmpty()) {
            val avgBpm = sessionTelemetries.map { it.bpm }.average()
            val maxBpm = sessionTelemetries.map { it.bpm }.maxOrNull() ?: avgBpm
            val minBpm = sessionTelemetries.map { it.bpm }.minOrNull() ?: avgBpm
            val avgGsrRaw = sessionTelemetries.map { it.gsrRaw }.average().toInt()
            val avgGsrVoltage = sessionTelemetries.map { it.gsrVoltage }.average()

            val dominantAura = sessionTelemetries.map { it.aura }
                .groupBy { it }
                .maxByOrNull { it.value.size }
                ?.key ?: "Neutral"

            // Stress level formula: based on avgBpm and avgGsrVoltage
            val bpmPart = ((avgBpm - 50.0) / 100.0).coerceIn(0.0, 1.0) * 40.0
            val gsrPart = (avgGsrVoltage / 3.3).coerceIn(0.0, 1.0) * 60.0
            val calculatedStress = (bpmPart + gsrPart).coerceIn(10.0, 95.0)

            val newReading = NewReadingData(
                deviceId = AppConfig.DEVICE_ID,
                avgBpm = avgBpm,
                maxBpm = maxBpm,
                minBpm = minBpm,
                avgGsrRaw = avgGsrRaw,
                avgGsrVoltage = avgGsrVoltage,
                stressLevel = calculatedStress,
                dominantAura = dominantAura,
                notes = "Sesión de escaneo de aura",
                durationSeconds = durationSeconds,
                startDate = startDate.toString(),
                endDate = endDate.toString()
            )

            viewModelScope.launch {
                saveReadingUseCase(newReading).onFailure { e ->
                    _uiState.update { it.copy(error = "Error al guardar sesión: ${e.message}") }
                }
            }
        }
        sessionTelemetries.clear()
        sessionStartDate = null
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        if (_uiState.value.isScanning) {
            saveSessionReading()
        }
        scanAuraUseCase.disconnect()
    }
}
