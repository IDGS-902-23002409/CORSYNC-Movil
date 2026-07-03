package com.sakura.aura.domain.usecase

import com.sakura.aura.data.remote.TokenManager
import com.sakura.aura.domain.model.AuthToken
import com.sakura.aura.domain.model.LoginCredentials
import com.sakura.aura.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) {
    suspend operator fun invoke(credentials: LoginCredentials): Result<AuthToken> {
        val result = authRepository.login(credentials)
        result.onSuccess { auth ->
            tokenManager.saveUserInfo(auth.user.username, auth.user.fullName)
        }
        return result
    }
}
