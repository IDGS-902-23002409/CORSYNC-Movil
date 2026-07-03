package com.sakura.aura.domain.repository

import com.sakura.aura.data.model.request.UpdateProfileRequest
import com.sakura.aura.data.model.response.UserResponse
import com.sakura.aura.data.model.response.UserStatsResponse

interface UserRepository {
    suspend fun getProfile(): Result<UserResponse>
    suspend fun updateProfile(request: UpdateProfileRequest): Result<UserResponse>
    suspend fun getStats(): Result<UserStatsResponse>
}
