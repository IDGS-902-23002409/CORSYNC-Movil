package com.sakura.aura.data.remote

import com.sakura.aura.data.model.request.LoginRequest
import com.sakura.aura.data.model.request.RegisterRequest
import com.sakura.aura.data.model.response.AuthResponse
import com.sakura.aura.data.model.response.UserResponse
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("api/Auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<AuthResponse>

    @POST("api/Auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    @GET("api/Auth/profile")
    suspend fun getProfile(): Response<UserResponse>
}