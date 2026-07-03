package com.sakura.aura.data.remote

import com.sakura.aura.BuildConfig
import com.sakura.aura.data.model.request.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    fun create(tokenManager: TokenManager): ApiService {

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val token = tokenManager.getJwtToken()
                val request = original.newBuilder()
                    .apply {
                        if (token != null) {
                            addHeader("Authorization", "Bearer $token")
                        }
                        addHeader("Accept", "application/json")
                        addHeader("Content-Type", "application/json")
                        addHeader("Connection", "close")
                    }
                    .method(original.method, original.body)
                    .build()

                val response = chain.proceed(request)

                if (response.code == 401) {
                    val refreshToken = tokenManager.getRefreshToken()
                    val expiredJwt = tokenManager.getJwtToken()

                    if (refreshToken != null && expiredJwt != null) {
                        response.close()

                        val refreshSuccess = runBlocking {
                            try {
                                val refreshResponse = ApiServiceRefresher.apiService.refreshToken(
                                    RefreshTokenRequest(expiredJwt, refreshToken)
                                )
                                if (refreshResponse.isSuccessful && refreshResponse.body() != null) {
                                    val auth = refreshResponse.body()!!
                                    tokenManager.saveTokens(auth.token, auth.refreshToken)
                                    true
                                } else {
                                    tokenManager.clearTokens()
                                    false
                                }
                            } catch (e: Exception) {
                                tokenManager.clearTokens()
                                false
                            }
                        }

                        if (refreshSuccess) {
                            val newToken = tokenManager.getJwtToken()
                            val retryRequest = original.newBuilder()
                                .apply {
                                    if (newToken != null) {
                                        addHeader("Authorization", "Bearer $newToken")
                                    }
                                    addHeader("Accept", "application/json")
                                    addHeader("Content-Type", "application/json")
                                    addHeader("Connection", "close")
                                }
                                .method(original.method, original.body)
                                .build()
                            return@addInterceptor chain.proceed(retryRequest)
                        }
                    }
                }

                response
            }
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        ApiServiceRefresher.apiService = retrofit.create(ApiService::class.java)
        return retrofit.create(ApiService::class.java)
    }
}

internal object ApiServiceRefresher {
    lateinit var apiService: ApiService
}
