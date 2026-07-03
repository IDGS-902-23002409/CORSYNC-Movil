package com.sakura.aura.data.remote

import com.sakura.aura.data.model.request.CreateReadingRequest
import com.sakura.aura.data.model.request.LoginRequest
import com.sakura.aura.data.model.request.LogoutRequest
import com.sakura.aura.data.model.request.RefreshTokenRequest
import com.sakura.aura.data.model.request.RegisterRequest
import com.sakura.aura.data.model.request.UpdateProfileRequest
import com.sakura.aura.data.model.response.AuthResponse
import com.sakura.aura.data.model.response.ChallengeResponse
import com.sakura.aura.data.model.response.MedalResponse
import com.sakura.aura.data.model.response.ReadingResponse
import com.sakura.aura.data.model.response.ReadingSummaryResponse
import com.sakura.aura.data.model.response.UserResponse
import com.sakura.aura.data.model.response.UserStatsResponse
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── Auth ───────────────────────────────────────────────────────────────
    @POST("api/Auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<AuthResponse>

    @POST("api/Auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    @POST("api/Auth/logout")
    suspend fun logout(
        @Body request: LogoutRequest
    ): Response<Unit>

    @POST("api/Auth/refresh-token")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): Response<AuthResponse>

    // ── User ───────────────────────────────────────────────────────────────
    @GET("api/User/profile")
    suspend fun getProfile(): Response<UserResponse>

    @PUT("api/User/profile")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Response<UserResponse>

    @GET("api/User/stats")
    suspend fun getUserStats(): Response<UserStatsResponse>

    // ── Readings ───────────────────────────────────────────────────────────
    @GET("api/Readings")
    suspend fun getReadings(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): Response<List<ReadingResponse>>

    @POST("api/Readings")
    suspend fun createReading(
        @Body request: CreateReadingRequest
    ): Response<ReadingResponse>

    @GET("api/Readings/summary")
    suspend fun getReadingsSummary(): Response<ReadingSummaryResponse>

    // ── Challenges ─────────────────────────────────────────────────────────
    @GET("api/Challenges")
    suspend fun getChallenges(): Response<List<ChallengeResponse>>

    @GET("api/Medals")
    suspend fun getMedals(): Response<List<MedalResponse>>
}
