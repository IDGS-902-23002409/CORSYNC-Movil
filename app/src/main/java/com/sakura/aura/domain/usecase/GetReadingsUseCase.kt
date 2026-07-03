package com.sakura.aura.domain.usecase

import com.sakura.aura.domain.model.Reading
import com.sakura.aura.domain.model.ReadingSummary
import com.sakura.aura.domain.repository.ReadingsRepository
import javax.inject.Inject

class GetReadingsUseCase @Inject constructor(
    private val readingsRepository: ReadingsRepository
) {
    suspend fun getReadings(page: Int = 1, pageSize: Int = 20): Result<List<Reading>> {
        return readingsRepository.getReadings(page, pageSize)
    }

    suspend fun getSummary(): Result<ReadingSummary> {
        return readingsRepository.getSummary()
    }
}
