package com.sakura.aura.domain.usecase

import com.sakura.aura.domain.model.UserStats
import com.sakura.aura.domain.repository.UserRepository
import javax.inject.Inject

class GetUserStatsUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<UserStats> = userRepository.getStats()
}
