package com.sakura.aura.data.mapper

import com.sakura.aura.data.model.request.RefreshTokenRequest
import com.sakura.aura.data.model.response.AuthResponse
import com.sakura.aura.domain.model.AuthToken

fun AuthResponse.toDomain(): AuthToken = AuthToken(
    token = token,
    refreshToken = refreshToken,
    expiration = expiration,
    user = user.toDomain()
)

fun AuthToken.toRefreshRequest(): RefreshTokenRequest = RefreshTokenRequest(
    token = token,
    refreshToken = refreshToken
)
