package com.sakura.aura.domain.usecase

import com.sakura.aura.domain.model.Challenge
import com.sakura.aura.domain.model.Medal
import com.sakura.aura.domain.repository.ChallengesRepository
import javax.inject.Inject

class GetChallengesUseCase @Inject constructor(
    private val challengesRepository: ChallengesRepository
) {
    suspend fun getChallenges(): Result<List<Challenge>> {
        return challengesRepository.getChallenges()
    }

    suspend fun getMedals(): Result<List<Medal>> {
        return challengesRepository.getMedals()
    }
}
