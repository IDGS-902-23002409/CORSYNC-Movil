package com.sakura.aura.data.repository

import com.sakura.aura.data.model.response.ChallengeResponse
import com.sakura.aura.data.model.response.MedalResponse
import com.sakura.aura.data.remote.ApiService
import com.sakura.aura.domain.repository.ChallengesRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChallengesRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : ChallengesRepository {

    override suspend fun getChallenges(): Result<List<ChallengeResponse>> {
        return try {
            val response = apiService.getChallenges()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al obtener desafíos"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMedals(): Result<List<MedalResponse>> {
        return try {
            val response = apiService.getMedals()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al obtener medallas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
