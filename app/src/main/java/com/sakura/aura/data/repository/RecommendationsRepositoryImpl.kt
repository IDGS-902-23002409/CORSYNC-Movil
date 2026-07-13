package com.sakura.aura.data.repository

import com.sakura.aura.data.mapper.toDomain
import com.sakura.aura.data.remote.ApiService
import com.sakura.aura.domain.model.RecommendationsPackage
import com.sakura.aura.domain.repository.RecommendationsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecommendationsRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : RecommendationsRepository {

    override suspend fun getRecommendations(): Result<RecommendationsPackage> {
        return try {
            val response = apiService.getRecommendations()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception("Error al obtener recomendaciones"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
