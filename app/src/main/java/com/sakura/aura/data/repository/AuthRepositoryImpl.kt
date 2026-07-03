package com.sakura.aura.data.repository

import android.util.Log
import com.sakura.aura.data.model.request.LoginRequest
import com.sakura.aura.data.model.request.LogoutRequest
import com.sakura.aura.data.model.request.RefreshTokenRequest
import com.sakura.aura.data.model.request.RegisterRequest
import com.sakura.aura.data.model.response.AuthResponse
import com.sakura.aura.data.remote.ApiService
import com.sakura.aura.data.remote.TokenManager
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

    override suspend fun register(request: RegisterRequest): Result<AuthResponse> {
        return try {
            val response = apiService.register(request)

            if (response.isSuccessful && response.body() != null) {
                val auth = response.body()!!
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

                try {
                    val loginRequest = LoginRequest(
                        username = request.username,
                        password = request.password
                    )

                    val loginResponse = apiService.login(loginRequest)

                    if (loginResponse.isSuccessful && loginResponse.body() != null) {
                        val auth = loginResponse.body()!!
                        tokenManager.saveTokens(auth.token, auth.refreshToken)
                        Log.i("AuthRepository", "¡Login de rescate exitoso tras caída de registro!")
                        return Result.success(auth)
                    } else {
                        val loginError = loginResponse.errorBody()?.string() ?: "Error en login"
                        return Result.failure(Exception("El registro falló por red y el login de rescate no tuvo éxito: $loginError"))
                    }
                } catch (loginException: Exception) {
                    return Result.failure(loginException)
                }
            }

            Result.failure(e)
        }
    }

    override suspend fun login(request: LoginRequest): Result<AuthResponse> {
        return try {
            val response = apiService.login(request)
            if (response.isSuccessful && response.body() != null) {
                val auth = response.body()!!
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

    override suspend fun logout(request: LogoutRequest): Result<Unit> {
        return try {
            val response = apiService.logout(request)
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

    override suspend fun refreshToken(request: RefreshTokenRequest): Result<AuthResponse> {
        return try {
            val response = apiService.refreshToken(request)
            if (response.isSuccessful && response.body() != null) {
                val auth = response.body()!!
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
