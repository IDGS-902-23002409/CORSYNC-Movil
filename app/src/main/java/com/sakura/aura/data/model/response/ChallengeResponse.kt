package com.sakura.aura.data.model.response

data class ChallengeResponse(
    val id: Int,
    val titulo: String,
    val descripcion: String,
    val icono: String?,
    val tipo: String?,
    val metaObjetivo: Int,
    val unidadMedida: String?,
    val puntos: Int,
    val progresoActual: Int,
    val completado: Boolean,
    val porcentajeProgreso: Double,
    val fechaCompletado: String?
)
