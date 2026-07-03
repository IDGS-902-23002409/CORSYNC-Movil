package com.sakura.aura.domain.usecase

import com.sakura.aura.domain.model.UserProfile
import com.sakura.aura.domain.repository.UserRepository
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<UserProfile> = userRepository.getProfile()
}
