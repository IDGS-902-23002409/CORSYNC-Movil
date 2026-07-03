package com.sakura.aura.domain.usecase

import com.sakura.aura.data.remote.TokenManager
import com.sakura.aura.domain.repository.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) {
    suspend operator fun invoke(): Result<Unit> {
        val jwtToken = tokenManager.getJwtToken()
        val refreshToken = tokenManager.getRefreshToken()
        return if (jwtToken != null && refreshToken != null) {
            authRepository.logout(jwtToken, refreshToken)
        } else {
            tokenManager.clearAll()
            Result.success(Unit)
        }
    }
}
