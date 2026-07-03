package com.sakura.aura.domain.repository

import com.sakura.aura.data.model.request.CreateReadingRequest
import com.sakura.aura.data.model.response.ReadingResponse
import com.sakura.aura.data.model.response.ReadingSummaryResponse

interface ReadingsRepository {
    suspend fun getReadings(page: Int = 1, pageSize: Int = 20): Result<List<ReadingResponse>>
    suspend fun createReading(request: CreateReadingRequest): Result<ReadingResponse>
    suspend fun getSummary(): Result<ReadingSummaryResponse>
}
