package com.sakura.aura.data.repository

import android.util.Log
import com.sakura.aura.data.model.request.LoginRequest
import com.sakura.aura.data.model.request.RegisterRequest
import com.sakura.aura.data.model.response.AuthResponse
import com.sakura.aura.data.remote.ApiService
import com.sakura.aura.domain.repository.AuthRepository
import java.io.IOException
import java.net.SocketException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : AuthRepository {

    override suspend fun register(request: RegisterRequest): Result<AuthResponse> {
        return try {
            val response = apiService.register(request)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
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
                        Log.i("AuthRepository", "¡Login de rescate exitoso tras caída de registro!")
                        return Result.success(loginResponse.body()!!)
                    } else {
                        val loginError = loginResponse.errorBody()?.string() ?: "Error en login"
                        return Result.failure(Exception("El registro falló por red y el login de rescate no tuvo éxito: $loginError"))
                    }
                } catch (loginException: Exception) {
                    return Result.failure(loginException)
                }
            }

            // Si es un error completamente ajeno a la conexión, lo pasamos normal
            Result.failure(e)
        }
    }

    override suspend fun login(request: LoginRequest): Result<AuthResponse> {
        return try {
            val response = apiService.login(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Credenciales incorrectas"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}