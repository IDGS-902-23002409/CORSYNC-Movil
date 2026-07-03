package com.sakura.aura.data.model.request

data class UpdateProfileRequest(
    val nombreCompleto: String?,
    val nombreEspiritual: String?,
    val signoZodiacal: String?,
    val fotoUrl: String?
)
