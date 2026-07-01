package com.sakura.aura.data.remote

import com.sakura.aura.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.security.cert.X509Certificate

object RetrofitClient {

    fun create(tokenProvider: () -> String? = { null }): ApiService {

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            // ── Interceptor de Auth + Headers obligatorios ─────────────────
            .addInterceptor { chain ->
                val token = tokenProvider()
                val original = chain.request()
                val request = original.newBuilder()
                    .apply {
                        // JWT si existe
                        if (token != null) {
                            addHeader("Authorization", "Bearer $token")
                        }
                        // Headers que ASP.NET Core espera
                        addHeader("Accept", "application/json")
                        addHeader("Content-Type", "application/json")
                        addHeader("Connection", "close")
                    }
                    .method(original.method, original.body)
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            // ── Reintento automático ───────────────────────────────────────
            .retryOnConnectionFailure(true)
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}