package com.sakura.aura.domain.model

data class WeekSummary(
    val avgBpm: Double,
    val avgStress: Double,
    val sessions: Int
)

data class WeekComparison(
    val currentWeek: WeekSummary,
    val previousWeek: WeekSummary,
    val bpmChangePct: Double,
    val stressChangePct: Double,
    val sessionsChangePct: Double,
    val trend: String
)
