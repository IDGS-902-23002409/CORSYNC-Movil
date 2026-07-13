package com.sakura.aura.data.repository

import com.sakura.aura.data.mapper.toDomain
import com.sakura.aura.data.remote.ApiService
import com.sakura.aura.domain.model.Distribution
import com.sakura.aura.domain.model.Trends
import com.sakura.aura.domain.model.WeekComparison
import com.sakura.aura.domain.repository.AnalyticsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : AnalyticsRepository {

    override suspend fun getTrends(
        period: String,
        days: Int,
        weeks: Int,
        months: Int
    ): Result<Trends> {
        return try {
            val response = apiService.getTrends(period, days, weeks, months)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception("Error al obtener tendencias"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDistribution(): Result<Distribution> {
        return try {
            val response = apiService.getDistribution()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception("Error al obtener distribución"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getComparison(): Result<WeekComparison> {
        return try {
            val response = apiService.getComparison()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception("Error al obtener comparación semanal"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
