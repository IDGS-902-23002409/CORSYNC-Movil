package com.sakura.aura.domain.model

data class ReadingSummary(
    val globalAvgBpm: Double,
    val globalAvgStress: Double,
    val totalSessions: Int,
    val mostFrequentAura: String,
    val auraDistribution: Map<String, Int>?
)
