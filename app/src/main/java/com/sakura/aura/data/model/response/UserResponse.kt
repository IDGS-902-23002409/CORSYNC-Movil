package com.sakura.aura.data.model.response

data class UserResponse(
    val id: Int,
    val username: String,
    val email: String,
    val nombreCompleto: String,
    val role: String,
    val fechaRegistro: String
)