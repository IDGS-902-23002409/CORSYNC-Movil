package com.sakura.aura.data.model.request

data class LogoutRequest(
    val token: String,
    val refreshToken: String
)
