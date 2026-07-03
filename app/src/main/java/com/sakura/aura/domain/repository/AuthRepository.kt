package com.sakura.aura.domain.repository


import com.sakura.aura.data.model.request.LoginRequest
import com.sakura.aura.data.model.request.RegisterRequest
import com.sakura.aura.data.model.response.AuthResponse

interface AuthRepository {

    suspend fun register(request: RegisterRequest): Result<AuthResponse>

    suspend fun login(request: LoginRequest): Result<AuthResponse>
}