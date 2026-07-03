package com.sakura.aura.domain.model

data class NewReadingData(
    val deviceId: String,
    val avgBpm: Double,
    val maxBpm: Double,
    val minBpm: Double,
    val avgGsrRaw: Int,
    val avgGsrVoltage: Double,
    val stressLevel: Double,
    val dominantAura: String,
    val notes: String?,
    val durationSeconds: Int,
    val startDate: String,
    val endDate: String
)
