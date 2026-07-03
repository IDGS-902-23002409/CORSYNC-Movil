package com.sakura.aura.domain.repository

import com.sakura.aura.domain.model.AuthToken
import com.sakura.aura.domain.model.LoginCredentials
import com.sakura.aura.domain.model.RegistrationData

interface AuthRepository {
    suspend fun register(data: RegistrationData): Result<AuthToken>
    suspend fun login(credentials: LoginCredentials): Result<AuthToken>
    suspend fun logout(jwtToken: String, refreshToken: String): Result<Unit>
    suspend fun refreshToken(token: AuthToken): Result<AuthToken>
}
