package com.sakura.aura.domain.repository

import com.sakura.aura.domain.model.ProfileUpdateData
import com.sakura.aura.domain.model.UserProfile
import com.sakura.aura.domain.model.UserStats

interface UserRepository {
    suspend fun getProfile(): Result<UserProfile>
    suspend fun updateProfile(data: ProfileUpdateData): Result<UserProfile>
    suspend fun getStats(): Result<UserStats>
}
