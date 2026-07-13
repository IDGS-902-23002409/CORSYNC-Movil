package com.sakura.aura.domain.model

data class Recommendation(
    val id: Int,
    val type: String,
    val title: String,
    val description: String,
    val priority: String,
    val icon: String,
    val durationMinutes: Int,
    val category: String
)

data class SuggestedChallenge(
    val challengeId: Int,
    val title: String,
    val reason: String,
    val priorityMatch: Double
)

data class RecommendationsPackage(
    val currentStressLevel: String,
    val stressScore: Double,
    val wellnessCategories: Map<String, String>,
    val recommendations: List<Recommendation>,
    val suggestedChallenges: List<SuggestedChallenge>,
    val motivationalMessage: String
)
