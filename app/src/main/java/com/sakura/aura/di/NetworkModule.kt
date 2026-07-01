package com.sakura.aura.di

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.sakura.aura.data.remote.ApiService
import com.sakura.aura.data.remote.RetrofitClient
import com.sakura.aura.data.remote.SignalRService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // ── EncryptedSharedPreferences para guardar el JWT ─────────────────────
    @Provides
    @Singleton
    fun provideEncryptedPrefs(@ApplicationContext context: Context): EncryptedSharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            "corsync_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }

    // ── ApiService con token inyectado ─────────────────────────────────────
    @Provides
    @Singleton
    fun provideApiService(prefs: EncryptedSharedPreferences): ApiService {
        return RetrofitClient.create(
            tokenProvider = { prefs.getString("jwt_token", null) }
        )
    }

    // ── SignalR Service ────────────────────────────────────────────────────
    @Provides
    @Singleton
    fun provideSignalRService(): SignalRService = SignalRService()
}