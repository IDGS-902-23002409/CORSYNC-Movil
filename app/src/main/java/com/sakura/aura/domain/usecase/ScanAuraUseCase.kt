package com.sakura.aura.domain.usecase

import com.sakura.aura.data.remote.SignalRService
import com.sakura.aura.data.remote.TokenManager
import com.sakura.aura.domain.model.Telemetry
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ScanAuraUseCase @Inject constructor(
    private val signalRService: SignalRService,
    private val tokenManager: TokenManager
) {
    val isConnected: StateFlow<Boolean> get() = signalRService.isConnected

    /** Última lectura, para mostrar en vivo. */
    val telemetry: StateFlow<Telemetry?> get() = signalRService.domainTelemetry

    /** Todas las lecturas, para acumular la sesión sin perder ninguna. */
    val telemetryStream: SharedFlow<Telemetry> get() = signalRService.telemetryStream

    val error: StateFlow<String?> get() = signalRService.error

    suspend fun connect() {
        val token = tokenManager.getJwtToken()
        if (token != null) {
            signalRService.connect(token)
        }
    }

    fun startScan() {
        signalRService.startMeasurement()
    }

    fun stopScan() {
        signalRService.stopMeasurement()
    }

    fun disconnect() {
        signalRService.disconnect()
    }
}
