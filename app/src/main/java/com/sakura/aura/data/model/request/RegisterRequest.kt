package com.sakura.aura.data.model.request

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val nombreCompleto: String
)