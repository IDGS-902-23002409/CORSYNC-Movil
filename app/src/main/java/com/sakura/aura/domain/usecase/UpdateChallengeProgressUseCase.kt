package com.sakura.aura.domain.usecase

import com.sakura.aura.domain.model.Challenge
import com.sakura.aura.domain.repository.ChallengesRepository
import javax.inject.Inject

class UpdateChallengeProgressUseCase @Inject constructor(
    private val challengesRepository: ChallengesRepository
) {
    suspend operator fun invoke(id: Int, progress: Int): Result<Challenge> {
        return challengesRepository.updateChallengeProgress(id, progress)
    }
}
