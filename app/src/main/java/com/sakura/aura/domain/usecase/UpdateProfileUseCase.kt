package com.sakura.aura.domain.usecase

import com.sakura.aura.domain.model.ProfileUpdateData
import com.sakura.aura.domain.model.UserProfile
import com.sakura.aura.domain.repository.UserRepository
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(data: ProfileUpdateData): Result<UserProfile> =
        userRepository.updateProfile(data)
}
