package com.sakura.aura.domain.model

data class TrendDataPoint(
    val date: String,
    val avgBpm: Double,
    val maxBpm: Double,
    val minBpm: Double,
    val avgStress: Double,
    val avgGsr: Double,
    val sessions: Int,
    val avgDurationSeconds: Int
)

data class Trends(
    val period: String,
    val dataPoints: List<TrendDataPoint>
)
