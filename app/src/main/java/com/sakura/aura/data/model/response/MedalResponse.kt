package com.sakura.aura.data.model.response

data class MedalResponse(
    val id: Int,
    val nombre: String,
    val descripcion: String?,
    val icono: String?,
    val fechaObtenida: String?
)
