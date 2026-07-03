package com.sakura.aura.domain.repository

import com.sakura.aura.domain.model.NewReadingData
import com.sakura.aura.domain.model.Reading
import com.sakura.aura.domain.model.ReadingSummary

interface ReadingsRepository {
    suspend fun getReadings(page: Int = 1, pageSize: Int = 20): Result<List<Reading>>
    suspend fun createReading(data: NewReadingData): Result<Reading>
    suspend fun getSummary(): Result<ReadingSummary>
}
