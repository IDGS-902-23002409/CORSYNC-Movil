package com.sakura.aura.data.mapper

import com.sakura.aura.data.model.request.UpdateProfileRequest
import com.sakura.aura.data.model.response.UserResponse
import com.sakura.aura.data.model.response.UserStatsResponse
import com.sakura.aura.domain.model.LoginCredentials
import com.sakura.aura.domain.model.ProfileUpdateData
import com.sakura.aura.domain.model.RegistrationData
import com.sakura.aura.domain.model.UserProfile
import com.sakura.aura.domain.model.UserStats

fun UserResponse.toDomain(): UserProfile = UserProfile(
    id = id,
    username = username,
    email = email,
    fullName = nombreCompleto,
    spiritualName = nombreEspiritual,
    zodiacSign = signoZodiacal,
    photoUrl = fotoUrl,
    role = role,
    registrationDate = fechaRegistro
)

fun UserStatsResponse.toDomain(): UserStats = UserStats(
    avgBpm = bpmPromedio,
    avgStressLevel = nivelEstresPromedio,
    totalSessions = sesionesTotales,
    dominantAura = auraDominante,
    currentStreakDays = rachaActualDias,
    lastSession = ultimaSesion
)

fun ProfileUpdateData.toRequest(): UpdateProfileRequest = UpdateProfileRequest(
    nombreCompleto = fullName,
    nombreEspiritual = spiritualName,
    signoZodiacal = zodiacSign,
    fotoUrl = photoUrl
)

fun RegistrationData.toRegisterRequest() = com.sakura.aura.data.model.request.RegisterRequest(
    username = username,
    email = email,
    password = password,
    nombreCompleto = fullName
)

fun LoginCredentials.toLoginRequest() = com.sakura.aura.data.model.request.LoginRequest(
    username = username,
    password = password
)
