package com.sakura.aura.data.model.request

data class RefreshTokenRequest(
    val token: String,
    val refreshToken: String
)
