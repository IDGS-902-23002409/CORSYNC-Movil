package com.sakura.aura.data.repository

import android.util.Log
import com.sakura.aura.data.mapper.toDomain
import com.sakura.aura.data.mapper.toLoginRequest
import com.sakura.aura.data.mapper.toRefreshRequest
import com.sakura.aura.data.mapper.toRegisterRequest
import com.sakura.aura.data.model.request.LogoutRequest
import com.sakura.aura.data.remote.ApiService
import com.sakura.aura.data.remote.TokenManager
import com.sakura.aura.domain.model.AuthToken
import com.sakura.aura.domain.model.LoginCredentials
import com.sakura.aura.domain.model.RegistrationData
import com.sakura.aura.domain.repository.AuthRepository
import java.io.IOException
import java.net.SocketException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun register(data: RegistrationData): Result<AuthToken> {
        return try {
            val response = apiService.register(data.toRegisterRequest())
            if (response.isSuccessful && response.body() != null) {
                val auth = response.body()!!.toDomain()
                tokenManager.saveTokens(auth.token, auth.refreshToken)
                Result.success(auth)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            val errorMessage = e.message ?: ""
            if (e is SocketException || e is IOException || errorMessage.contains("reset", ignoreCase = true)) {
                Log.w("AuthRepository", "Fallo de conexión detectado (posible reset). Intentando Login de rescate...")
                return try {
                    val loginResponse = apiService.login(
                        LoginCredentials(data.username, data.password).toLoginRequest()
                    )
                    if (loginResponse.isSuccessful && loginResponse.body() != null) {
                        val auth = loginResponse.body()!!.toDomain()
                        tokenManager.saveTokens(auth.token, auth.refreshToken)
                        Log.i("AuthRepository", "¡Login de rescate exitoso tras caída de registro!")
                        Result.success(auth)
                    } else {
                        val loginError = loginResponse.errorBody()?.string() ?: "Error en login"
                        Result.failure(Exception("El registro falló por red y el login de rescate no tuvo éxito: $loginError"))
                    }
                } catch (loginException: Exception) {
                    Result.failure(loginException)
                }
            }
            Result.failure(e)
        }
    }

    override suspend fun login(credentials: LoginCredentials): Result<AuthToken> {
        return try {
            val response = apiService.login(credentials.toLoginRequest())
            if (response.isSuccessful && response.body() != null) {
                val auth = response.body()!!.toDomain()
                tokenManager.saveTokens(auth.token, auth.refreshToken)
                Result.success(auth)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Credenciales incorrectas"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(jwtToken: String, refreshToken: String): Result<Unit> {
        return try {
            val response = apiService.logout(LogoutRequest(jwtToken, refreshToken))
            tokenManager.clearTokens()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al cerrar sesión"))
            }
        } catch (e: Exception) {
            tokenManager.clearTokens()
            Result.success(Unit)
        }
    }

    override suspend fun refreshToken(token: AuthToken): Result<AuthToken> {
        return try {
            val response = apiService.refreshToken(token.toRefreshRequest())
            if (response.isSuccessful && response.body() != null) {
                val auth = response.body()!!.toDomain()
                tokenManager.saveTokens(auth.token, auth.refreshToken)
                Result.success(auth)
            } else {
                Result.failure(Exception("No se pudo renovar el token"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
