package com.sakura.aura.domain.repository

import com.sakura.aura.data.model.response.ChallengeResponse
import com.sakura.aura.data.model.response.MedalResponse

interface ChallengesRepository {
    suspend fun getChallenges(): Result<List<ChallengeResponse>>
    suspend fun getMedals(): Result<List<MedalResponse>>
}
