package com.sakura.aura.domain.usecase

import com.sakura.aura.data.remote.SignalRService
import com.sakura.aura.data.remote.TokenManager
import com.sakura.aura.domain.model.Telemetry
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ScanAuraUseCase @Inject constructor(
    private val signalRService: SignalRService,
    private val tokenManager: TokenManager
) {
    val isConnected: StateFlow<Boolean> get() = signalRService.isConnected
    val telemetry: StateFlow<Telemetry?> get() = signalRService.domainTelemetry
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
