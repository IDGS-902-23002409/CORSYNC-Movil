package com.sakura.aura.data.remote

import androidx.security.crypto.EncryptedSharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    private val prefs: EncryptedSharedPreferences
) {
    companion object {
        private const val KEY_JWT_TOKEN = "jwt_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USERNAME = "username"
        private const val KEY_NOMBRE_COMPLETO = "nombre_completo"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_BIOMETRIC_PASSWORD = "biometric_password"
        private const val KEY_BIOMETRIC_IV = "biometric_iv"
        private const val KEY_BIOMETRIC_USERNAME = "biometric_username"
    }

    fun getJwtToken(): String? = prefs.getString(KEY_JWT_TOKEN, null)

    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)

    fun getNombreCompleto(): String? = prefs.getString(KEY_NOMBRE_COMPLETO, null)

    fun saveTokens(jwt: String, refresh: String) {
        prefs.edit()
            .putString(KEY_JWT_TOKEN, jwt)
            .putString(KEY_REFRESH_TOKEN, refresh)
            .apply()
    }

    fun saveUserInfo(username: String, nombreCompleto: String) {
        prefs.edit()
            .putString(KEY_USERNAME, username)
            .putString(KEY_NOMBRE_COMPLETO, nombreCompleto)
            .apply()
    }

    // ── Funciones de Biometría ─────────────────────────────────────────────

    fun isBiometricEnabled(): Boolean = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun getBiometricPassword(): String? = prefs.getString(KEY_BIOMETRIC_PASSWORD, null)

    fun getBiometricIv(): String? = prefs.getString(KEY_BIOMETRIC_IV, null)

    fun getBiometricUsername(): String? = prefs.getString(KEY_BIOMETRIC_USERNAME, null)

    fun saveBiometricCredentials(username: String, passwordCipher: String, iv: String) {
        prefs.edit()
            .putString(KEY_BIOMETRIC_USERNAME, username)
            .putString(KEY_BIOMETRIC_PASSWORD, passwordCipher)
            .putString(KEY_BIOMETRIC_IV, iv)
            .putBoolean(KEY_BIOMETRIC_ENABLED, true)
            .apply()
    }

    fun clearBiometricCredentials() {
        prefs.edit()
            .remove(KEY_BIOMETRIC_USERNAME)
            .remove(KEY_BIOMETRIC_PASSWORD)
            .remove(KEY_BIOMETRIC_IV)
            .putBoolean(KEY_BIOMETRIC_ENABLED, false)
            .apply()
    }

    fun clearTokens() {
        prefs.edit()
            .remove(KEY_JWT_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
