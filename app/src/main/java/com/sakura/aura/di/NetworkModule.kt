package com.sakura.aura.di

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.sakura.aura.data.remote.ApiService
import com.sakura.aura.data.remote.RetrofitClient
import com.sakura.aura.data.remote.SignalRService
import com.sakura.aura.data.remote.TokenManager
import com.sakura.aura.data.repository.AuthRepositoryImpl
import com.sakura.aura.data.repository.ChallengesRepositoryImpl
import com.sakura.aura.data.repository.ReadingsRepositoryImpl
import com.sakura.aura.data.repository.UserRepositoryImpl
import com.sakura.aura.domain.repository.AuthRepository
import com.sakura.aura.domain.repository.ChallengesRepository
import com.sakura.aura.domain.repository.ReadingsRepository
import com.sakura.aura.domain.repository.UserRepository
import com.sakura.aura.data.repository.AnalyticsRepositoryImpl
import com.sakura.aura.data.repository.RecommendationsRepositoryImpl
import com.sakura.aura.domain.repository.AnalyticsRepository
import com.sakura.aura.domain.repository.RecommendationsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideEncryptedPrefs(@ApplicationContext context: Context): EncryptedSharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return try {
            EncryptedSharedPreferences.create(
                context,
                "corsync_secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            ) as EncryptedSharedPreferences
        } catch (e: Exception) {
            context.deleteSharedPreferences("corsync_secure_prefs")
            EncryptedSharedPreferences.create(
                context,
                "corsync_secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            ) as EncryptedSharedPreferences
        }
    }

    @Provides
    @Singleton
    fun provideTokenManager(prefs: EncryptedSharedPreferences): TokenManager {
        return TokenManager(prefs)
    }

    @Provides
    @Singleton
    fun provideApiService(tokenManager: TokenManager): ApiService {
        return RetrofitClient.create(tokenManager)
    }

    @Provides
    @Singleton
    fun provideSignalRService(): SignalRService = SignalRService()

    @Provides
    @Singleton
    fun provideAuthRepository(impl: AuthRepositoryImpl): AuthRepository = impl

    @Provides
    @Singleton
    fun provideUserRepository(impl: UserRepositoryImpl): UserRepository = impl

    @Provides
    @Singleton
    fun provideReadingsRepository(impl: ReadingsRepositoryImpl): ReadingsRepository = impl

    @Provides
    @Singleton
    fun provideChallengesRepository(impl: ChallengesRepositoryImpl): ChallengesRepository = impl

    @Provides
    @Singleton
    fun provideAnalyticsRepository(impl: AnalyticsRepositoryImpl): AnalyticsRepository = impl

    @Provides
    @Singleton
    fun provideRecommendationsRepository(impl: RecommendationsRepositoryImpl): RecommendationsRepository = impl
}
