package com.sakura.aura.domain.usecase

import com.sakura.aura.domain.model.RecommendationsPackage
import com.sakura.aura.domain.repository.RecommendationsRepository
import javax.inject.Inject

class GetRecommendationsUseCase @Inject constructor(
    private val recommendationsRepository: RecommendationsRepository
) {
    suspend operator fun invoke(): Result<RecommendationsPackage> {
        return recommendationsRepository.getRecommendations()
    }
}
