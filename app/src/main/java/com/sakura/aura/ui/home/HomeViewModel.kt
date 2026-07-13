package com.sakura.aura.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakura.aura.data.remote.AppConfig
import com.sakura.aura.domain.model.NewReadingData
import com.sakura.aura.domain.model.Telemetry
import com.sakura.aura.domain.usecase.SaveReadingUseCase
import com.sakura.aura.domain.usecase.ScanAuraUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

private const val SCAN_DURATION_SECONDS = 20L

data class HomeUiState(
    val isConnected  : Boolean       = false,
    val isConnecting : Boolean       = false,
    val isScanning   : Boolean       = false,
    val telemetry    : Telemetry?    = null,
    val auraColor    : AuraColorUi   = AuraColorUi.NEUTRAL,
    val scanResult   : ScanResult?   = null,
    val error        : String?       = null
)

data class ScanResult(
    val auraColor: AuraColorUi,
    val avgBpm: Double,
    val dominantAura: String,
    val stressLevel: Double
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
    private var autoStopJob: Job? = null

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
                        if (sessionTelemetries.size == 2) {
                            startAutoStopTimer()
                        }
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
        autoStopJob?.cancel()
        sessionTelemetries.clear()
        sessionStartDate = Instant.now()
        _uiState.update { it.copy(scanResult = null) }
        viewModelScope.launch {
            _uiState.update { it.copy(isConnecting = true) }
            try {
                if (!_uiState.value.isConnected) {
                    scanAuraUseCase.connect()
                }
                scanAuraUseCase.startScan()
                _uiState.update { it.copy(isScanning = true, isConnecting = false, error = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isConnecting = false, error = "Error al iniciar: ${e.message}") }
            }
        }
    }

    fun stopScan() {
        autoStopJob?.cancel()
        autoStopJob = null
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

    private fun startAutoStopTimer() {
        autoStopJob?.cancel()
        autoStopJob = viewModelScope.launch {
            delay(SCAN_DURATION_SECONDS * 1000L)
            stopScan()
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

            val bpmPart = ((avgBpm - 50.0) / 100.0).coerceIn(0.0, 1.0) * 40.0
            val gsrPart = (avgGsrVoltage / 3.3).coerceIn(0.0, 1.0) * 60.0
            val calculatedStress = (bpmPart + gsrPart).coerceIn(10.0, 95.0)

            val auraColor = AuraColorUi.fromString(dominantAura)

            _uiState.update { it.copy(
                scanResult = ScanResult(
                    auraColor = auraColor,
                    avgBpm = avgBpm,
                    dominantAura = dominantAura,
                    stressLevel = calculatedStress
                ),
                auraColor = auraColor
            ) }

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

    fun resetScan() {
        _uiState.update { it.copy(scanResult = null) }
    }

    override fun onCleared() {
        super.onCleared()
        autoStopJob?.cancel()
        if (_uiState.value.isScanning) {
            saveSessionReading()
        }
        scanAuraUseCase.disconnect()
    }
}
