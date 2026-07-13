package com.sakura.aura.data.repository

import com.sakura.aura.data.mapper.toDomain
import com.sakura.aura.data.remote.ApiService
import com.sakura.aura.domain.model.Challenge
import com.sakura.aura.domain.model.Medal
import com.sakura.aura.domain.repository.ChallengesRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChallengesRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : ChallengesRepository {

    override suspend fun getChallenges(): Result<List<Challenge>> {
        return try {
            val response = apiService.getChallenges()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.map { it.toDomain() })
            } else {
                Result.failure(Exception("Error al obtener desafíos"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMedals(): Result<List<Medal>> {
        return try {
            val response = apiService.getMedals()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.map { it.toDomain() })
            } else {
                Result.failure(Exception("Error al obtener medallas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateChallengeProgress(id: Int, progress: Int): Result<Challenge> {
        return try {
            val response = apiService.updateChallengeProgress(id, com.sakura.aura.data.model.request.UpdateProgressRequest(progress))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception("Error al actualizar progreso del desafío"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
