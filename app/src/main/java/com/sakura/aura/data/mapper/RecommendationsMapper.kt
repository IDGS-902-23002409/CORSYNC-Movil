package com.sakura.aura.data.mapper

import com.sakura.aura.data.model.response.RecommendationItemResponse
import com.sakura.aura.data.model.response.RecommendationsPackageResponse
import com.sakura.aura.data.model.response.SuggestedChallengeResponse
import com.sakura.aura.domain.model.Recommendation
import com.sakura.aura.domain.model.RecommendationsPackage
import com.sakura.aura.domain.model.SuggestedChallenge

fun RecommendationItemResponse.toDomain(): Recommendation = Recommendation(
    id = id,
    type = tipo,
    title = titulo,
    description = descripcion,
    priority = prioridad,
    icon = icono,
    durationMinutes = duracionMinutos,
    category = categoria
)

fun SuggestedChallengeResponse.toDomain(): SuggestedChallenge = SuggestedChallenge(
    challengeId = challengeId,
    title = titulo,
    reason = razon,
    priorityMatch = prioridadMatch
)

fun RecommendationsPackageResponse.toDomain(): RecommendationsPackage = RecommendationsPackage(
    currentStressLevel = nivelEstresActual,
    stressScore = scoreEstres,
    wellnessCategories = categoriasBienestar,
    recommendations = recomendaciones.map { it.toDomain() },
    suggestedChallenges = desafiosSugeridos.map { it.toDomain() },
    motivationalMessage = mensajeMotivacional
)
