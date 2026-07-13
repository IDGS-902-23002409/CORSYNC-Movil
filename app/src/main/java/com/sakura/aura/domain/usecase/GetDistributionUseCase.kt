package com.sakura.aura.domain.usecase

import com.sakura.aura.domain.model.Distribution
import com.sakura.aura.domain.repository.AnalyticsRepository
import javax.inject.Inject

class GetDistributionUseCase @Inject constructor(
    private val analyticsRepository: AnalyticsRepository
) {
    suspend operator fun invoke(): Result<Distribution> {
        return analyticsRepository.getDistribution()
    }
}
