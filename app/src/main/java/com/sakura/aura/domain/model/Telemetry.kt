package com.sakura.aura.domain.model

data class Telemetry(
    val id: Int,
    val deviceId: String,
    val ir: Int,
    val bpm: Double,
    val avgBpm: Int,
    val gsrRaw: Int,
    val gsrVoltage: Double,
    val aura: String,
    val timestamp: String
)
