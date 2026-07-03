package com.sakura.aura.data.model.request

data class CreateReadingRequest(
    val dispositivoId: String,
    val bpmPromedio: Double,
    val bpmMaximo: Double,
    val bpmMinimo: Double,
    val gsrRawPromedio: Int,
    val gsrVoltajePromedio: Double,
    val nivelEstres: Double,
    val auraDominante: String,
    val notas: String?,
    val duracionSegundos: Int,
    val fechaInicio: String,
    val fechaFin: String
)
