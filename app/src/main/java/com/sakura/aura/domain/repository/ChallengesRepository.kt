package com.sakura.aura.domain.repository

import com.sakura.aura.domain.model.Challenge
import com.sakura.aura.domain.model.Medal

interface ChallengesRepository {
    suspend fun getChallenges(): Result<List<Challenge>>
    suspend fun getMedals(): Result<List<Medal>>
    suspend fun updateChallengeProgress(id: Int, progress: Int): Result<Challenge>
}
