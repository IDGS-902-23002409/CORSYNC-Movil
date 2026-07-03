package com.sakura.aura.data.model.response

data class TelemetryResponse(
    val id: Int,
    val dispositivoId: String,
    val ir: Int,
    val bpm: Double,       // BPM en tiempo real
    val bpmPromedio: Int,  // Número estable a mostrar
    val gsrRaw: Int,       // GSR crudo
    val gsrVoltaje: Double,// Voltaje 0.0 - 3.3V
    val aura: String,      // "Roja", "Azul", "Verde", etc.
    val fechaHora: String
)