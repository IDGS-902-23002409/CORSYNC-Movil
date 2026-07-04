package com.sakura.aura.domain.usecase

import com.sakura.aura.domain.model.NewReadingData
import com.sakura.aura.domain.model.Reading
import com.sakura.aura.domain.repository.ReadingsRepository
import javax.inject.Inject

class SaveReadingUseCase @Inject constructor(
    private val readingsRepository: ReadingsRepository
) {
    suspend operator fun invoke(data: NewReadingData): Result<Reading> {
        return readingsRepository.createReading(data)
    }
}
