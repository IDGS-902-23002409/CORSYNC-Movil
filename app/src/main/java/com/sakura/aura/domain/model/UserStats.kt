package com.sakura.aura.domain.model

data class UserStats(
    val avgBpm: Double,
    val avgStressLevel: Double,
    val totalSessions: Int,
    val dominantAura: String?,
    val currentStreakDays: Int?,
    val lastSession: String?
)
