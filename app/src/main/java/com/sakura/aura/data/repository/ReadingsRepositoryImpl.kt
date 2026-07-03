package com.sakura.aura.data.repository

import com.sakura.aura.data.mapper.toDomain
import com.sakura.aura.data.mapper.toRequest
import com.sakura.aura.data.remote.ApiService
import com.sakura.aura.domain.model.NewReadingData
import com.sakura.aura.domain.model.Reading
import com.sakura.aura.domain.model.ReadingSummary
import com.sakura.aura.domain.repository.ReadingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReadingsRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : ReadingsRepository {

    override suspend fun getReadings(page: Int, pageSize: Int): Result<List<Reading>> {
        return try {
            val response = apiService.getReadings(page, pageSize)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.map { it.toDomain() })
            } else {
                Result.failure(Exception("Error al obtener lecturas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createReading(data: NewReadingData): Result<Reading> {
        return try {
            val response = apiService.createReading(data.toRequest())
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error al guardar lectura"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSummary(): Result<ReadingSummary> {
        return try {
            val response = apiService.getReadingsSummary()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception("Error al obtener resumen"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
