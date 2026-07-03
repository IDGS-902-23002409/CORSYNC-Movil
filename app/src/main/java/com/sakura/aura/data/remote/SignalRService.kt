package com.sakura.aura.data.remote

import com.google.gson.Gson
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import com.sakura.aura.BuildConfig
import com.sakura.aura.data.model.response.TelemetryResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class SignalRService {

    companion object {
        const val DEVICE_ID = "ESP32_MAX30102_01"
    }

    private var hubConnection: HubConnection? = null
    private val gson = Gson()

    private val _isConnected  = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _telemetry = MutableStateFlow<TelemetryResponse?>(null)
    val telemetry: StateFlow<TelemetryResponse?> = _telemetry.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // ── Conectar en hilo IO ────────────────────────────────────────────────
    suspend fun connect(jwtToken: String) = withContext(Dispatchers.IO) {
        try {
            val hubUrl = "${BuildConfig.SIGNALR_HUB_URL}?access_token=$jwtToken"

            hubConnection = HubConnectionBuilder
                .create(hubUrl)
                .build()

            hubConnection?.on(
                "ReceiveTelemetry",
                { data: Any ->
                    try {
                        val json  = gson.toJson(data)
                        val telem = gson.fromJson(json, TelemetryResponse::class.java)
                        _telemetry.value = telem
                    } catch (e: Exception) {
                        _error.value = "Error parseando telemetría: ${e.message}"
                    }
                },
                Any::class.java
            )

            hubConnection?.start()?.blockingAwait()

            val connected = hubConnection?.connectionState == HubConnectionState.CONNECTED
            _isConnected.value = connected

            if (connected) {
                hubConnection?.invoke("RegisterMobile", DEVICE_ID)
            }
        } catch (e: Exception) {
            _error.value = "Error conectando: ${e.message}"
            _isConnected.value = false
        }
    }

    fun startMeasurement() {
        if (hubConnection?.connectionState == HubConnectionState.CONNECTED) {
            hubConnection?.invoke("StartMeasurement", DEVICE_ID)
        }
    }

    fun stopMeasurement() {
        if (hubConnection?.connectionState == HubConnectionState.CONNECTED) {
            hubConnection?.invoke("StopMeasurement", DEVICE_ID)
            _telemetry.value = null
        }
    }

    fun disconnect() {
        stopMeasurement()
        hubConnection?.stop()
        _isConnected.value = false
    }
}