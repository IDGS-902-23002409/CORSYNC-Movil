package com.sakura.aura.domain.model

data class AuthToken(
    val token: String,
    val refreshToken: String,
    val expiration: String,
    val user: UserProfile
)
