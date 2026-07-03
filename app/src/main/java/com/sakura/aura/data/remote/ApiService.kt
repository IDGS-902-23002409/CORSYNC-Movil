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

    @POST(ApiEndpoints.AUTH_REGISTER)
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<AuthResponse>

    @POST(ApiEndpoints.AUTH_LOGIN)
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    @POST(ApiEndpoints.AUTH_LOGOUT)
    suspend fun logout(
        @Body request: LogoutRequest
    ): Response<Unit>

    @POST(ApiEndpoints.AUTH_REFRESH_TOKEN)
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): Response<AuthResponse>

    @GET(ApiEndpoints.USER_PROFILE)
    suspend fun getProfile(): Response<UserResponse>

    @PUT(ApiEndpoints.USER_PROFILE)
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Response<UserResponse>

    @GET(ApiEndpoints.USER_STATS)
    suspend fun getUserStats(): Response<UserStatsResponse>

    @GET(ApiEndpoints.READINGS)
    suspend fun getReadings(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): Response<List<ReadingResponse>>

    @POST(ApiEndpoints.READINGS)
    suspend fun createReading(
        @Body request: CreateReadingRequest
    ): Response<ReadingResponse>

    @GET(ApiEndpoints.READINGS_SUMMARY)
    suspend fun getReadingsSummary(): Response<ReadingSummaryResponse>

    @GET(ApiEndpoints.CHALLENGES)
    suspend fun getChallenges(): Response<List<ChallengeResponse>>

    @GET(ApiEndpoints.MEDALS)
    suspend fun getMedals(): Response<List<MedalResponse>>
}
