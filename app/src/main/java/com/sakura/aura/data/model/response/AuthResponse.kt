package com.sakura.aura.data.model.response

data class AuthResponse(
    val token: String,
    val expiration: String,
    val user: UserResponse
)