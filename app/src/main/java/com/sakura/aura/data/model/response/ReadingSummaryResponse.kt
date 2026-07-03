package com.sakura.aura.data.model.response

data class ReadingSummaryResponse(
    val bpmPromedioGlobal: Double,
    val nivelEstresPromedio: Double,
    val totalSesiones: Int,
    val auraMasFrecuente: String,
    val distribucionAuras: Map<String, Int>?
)
