package com.sakura.aura.data.repository

import com.sakura.aura.data.model.request.CreateReadingRequest
import com.sakura.aura.data.model.response.ReadingResponse
import com.sakura.aura.data.model.response.ReadingSummaryResponse
import com.sakura.aura.data.remote.ApiService
import com.sakura.aura.domain.repository.ReadingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReadingsRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : ReadingsRepository {

    override suspend fun getReadings(page: Int, pageSize: Int): Result<List<ReadingResponse>> {
        return try {
            val response = apiService.getReadings(page, pageSize)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al obtener lecturas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createReading(request: CreateReadingRequest): Result<ReadingResponse> {
        return try {
            val response = apiService.createReading(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error al guardar lectura"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSummary(): Result<ReadingSummaryResponse> {
        return try {
            val response = apiService.getReadingsSummary()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al obtener resumen"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
