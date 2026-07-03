package com.sakura.aura.data.mapper

import com.sakura.aura.data.model.response.ChallengeResponse
import com.sakura.aura.data.model.response.MedalResponse
import com.sakura.aura.domain.model.Challenge
import com.sakura.aura.domain.model.Medal

fun ChallengeResponse.toDomain(): Challenge = Challenge(
    id = id,
    title = titulo,
    description = descripcion,
    icon = icono,
    type = tipo,
    targetGoal = metaObjetivo,
    unit = unidadMedida,
    points = puntos,
    currentProgress = progresoActual,
    completed = completado,
    progressPercentage = porcentajeProgreso,
    completedDate = fechaCompletado
)

fun MedalResponse.toDomain(): Medal = Medal(
    id = id,
    name = nombre,
    description = descripcion,
    icon = icono,
    obtainedDate = fechaObtenida
)
