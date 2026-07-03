package com.sakura.aura.data.repository

import com.sakura.aura.data.model.request.UpdateProfileRequest
import com.sakura.aura.data.model.response.UserResponse
import com.sakura.aura.data.model.response.UserStatsResponse
import com.sakura.aura.data.remote.ApiService
import com.sakura.aura.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : UserRepository {

    override suspend fun getProfile(): Result<UserResponse> {
        return try {
            val response = apiService.getProfile()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al obtener perfil"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(request: UpdateProfileRequest): Result<UserResponse> {
        return try {
            val response = apiService.updateProfile(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al actualizar perfil"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getStats(): Result<UserStatsResponse> {
        return try {
            val response = apiService.getUserStats()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al obtener estadísticas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
