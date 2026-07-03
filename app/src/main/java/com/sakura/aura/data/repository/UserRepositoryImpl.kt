package com.sakura.aura.data.repository

import com.sakura.aura.data.mapper.toDomain
import com.sakura.aura.data.mapper.toRequest
import com.sakura.aura.data.remote.ApiService
import com.sakura.aura.domain.model.ProfileUpdateData
import com.sakura.aura.domain.model.UserProfile
import com.sakura.aura.domain.model.UserStats
import com.sakura.aura.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : UserRepository {

    override suspend fun getProfile(): Result<UserProfile> {
        return try {
            val response = apiService.getProfile()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception("Error al obtener perfil"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(data: ProfileUpdateData): Result<UserProfile> {
        return try {
            val response = apiService.updateProfile(data.toRequest())
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception("Error al actualizar perfil"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getStats(): Result<UserStats> {
        return try {
            val response = apiService.getUserStats()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception("Error al obtener estadísticas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
