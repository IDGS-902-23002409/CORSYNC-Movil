package com.sakura.aura.domain.repository

import com.sakura.aura.domain.model.RecommendationsPackage

interface RecommendationsRepository {
    suspend fun getRecommendations(): Result<RecommendationsPackage>
}
