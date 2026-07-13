package com.sakura.aura.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricCipherHelper @Inject constructor() {

    companion object {
        private const val KEY_PROVIDER = "AndroidKeyStore"
        private const val KEY_ALIAS = "com.sakura.aura.biometric_key"
        private const val TRANSFORMATION = "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_GCM}/${KeyProperties.ENCRYPTION_PADDING_NONE}"
        private const val TAG_SIZE = 128
    }

    private val keyStore: KeyStore? by lazy {
        try {
            KeyStore.getInstance(KEY_PROVIDER).apply { load(null) }
        } catch (e: Exception) {
            null
        }
    }

    private fun generateSecretKey() {
        val ks = keyStore ?: return
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KEY_PROVIDER
        )

        val builder = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(true)
            .setInvalidatedByBiometricEnrollment(true)

        keyGenerator.init(builder.build())
        keyGenerator.generateKey()
    }

    private fun getSecretKey(): SecretKey? {
        val ks = keyStore ?: return null
        return try {
            (ks.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.secretKey
                ?: run {
                    generateSecretKey()
                    (ks.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.secretKey
                }
        } catch (e: Exception) {
            null
        }
    }

    fun getCipherForEncryption(): Cipher? {
        val key = getSecretKey() ?: return null
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            cipher
        } catch (e: Exception) {
            null
        }
    }

    fun getCipherForDecryption(iv: ByteArray): Cipher? {
        val key = getSecretKey() ?: return null
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(TAG_SIZE, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, spec)
            cipher
        } catch (e: Exception) {
            null
        }
    }

    fun encrypt(plainText: String, cipher: Cipher): EncryptedData {
        val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        val encryptedString = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        val ivString = Base64.encodeToString(cipher.iv, Base64.NO_WRAP)
        return EncryptedData(encryptedString, ivString)
    }

    fun decrypt(cipherText: String, cipher: Cipher): String {
        val decodedBytes = Base64.decode(cipherText, Base64.NO_WRAP)
        val decryptedBytes = cipher.doFinal(decodedBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }

    fun removeSecretKey() {
        try {
            keyStore?.deleteEntry(KEY_ALIAS)
        } catch (e: Exception) { }
    }
}

data class EncryptedData(
    val cipherText: String,
    val iv: String
)
