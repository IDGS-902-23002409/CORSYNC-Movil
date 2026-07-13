package com.sakura.aura.domain.usecase

import com.sakura.aura.domain.model.Trends
import com.sakura.aura.domain.repository.AnalyticsRepository
import javax.inject.Inject

class GetTrendsUseCase @Inject constructor(
    private val analyticsRepository: AnalyticsRepository
) {
    suspend operator fun invoke(period: String, days: Int = 30, weeks: Int = 4, months: Int = 6): Result<Trends> {
        return analyticsRepository.getTrends(period, days, weeks, months)
    }
}
