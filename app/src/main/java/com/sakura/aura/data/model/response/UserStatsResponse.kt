package com.sakura.aura.data.model.response

data class UserStatsResponse(
    val bpmPromedio: Double,
    val nivelEstresPromedio: Double,
    val sesionesTotales: Int,
    val auraDominante: String?,
    val rachaActualDias: Int?,
    val ultimaSesion: String?
)
