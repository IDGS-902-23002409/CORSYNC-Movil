package com.sakura.aura.domain.usecase

import com.sakura.aura.domain.model.WeekComparison
import com.sakura.aura.domain.repository.AnalyticsRepository
import javax.inject.Inject

class GetComparisonUseCase @Inject constructor(
    private val analyticsRepository: AnalyticsRepository
) {
    suspend operator fun invoke(): Result<WeekComparison> {
        return analyticsRepository.getComparison()
    }
}
