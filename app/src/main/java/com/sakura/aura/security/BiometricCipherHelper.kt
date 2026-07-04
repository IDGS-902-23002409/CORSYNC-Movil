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

    private val keyStore: KeyStore = KeyStore.getInstance(KEY_PROVIDER).apply {
        load(null)
    }

    /**
     * Genera una clave simétrica en el Android Keystore que requiere autenticación del usuario.
     */
    private fun generateSecretKey() {
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
            // Requerir autenticación biométrica del usuario para usar esta clave
            .setUserAuthenticationRequired(true)
            // Invalidar la clave si se registran nuevas huellas en el dispositivo (grado bancario)
            .setInvalidatedByBiometricEnrollment(true)

        keyGenerator.init(builder.build())
        keyGenerator.generateKey()
    }

    /**
     * Obtiene la clave secreta desde el Keystore, generándola si no existe.
     */
    private fun getSecretKey(): SecretKey {
        return (keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.secretKey
            ?: run {
                generateSecretKey()
                (keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
            }
    }

    /**
     * Inicializa un Cipher para cifrado.
     */
    fun getCipherForEncryption(): Cipher {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        return cipher
    }

    /**
     * Inicializa un Cipher para descifrado usando el IV proporcionado.
     */
    fun getCipherForDecryption(iv: ByteArray): Cipher {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(TAG_SIZE, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
        return cipher
    }

    /**
     * Cifra un texto utilizando el Cipher autenticado de la huella digital.
     */
    fun encrypt(plainText: String, cipher: Cipher): EncryptedData {
        val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        val encryptedString = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        val ivString = Base64.encodeToString(cipher.iv, Base64.NO_WRAP)
        return EncryptedData(encryptedString, ivString)
    }

    /**
     * Descifra un texto utilizando el Cipher autenticado de la huella digital.
     */
    fun decrypt(cipherText: String, cipher: Cipher): String {
        val decodedBytes = Base64.decode(cipherText, Base64.NO_WRAP)
        val decryptedBytes = cipher.doFinal(decodedBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }

    /**
     * Elimina la clave del Keystore si se deshabilita la biometría.
     */
    fun removeSecretKey() {
        if (keyStore.containsAlias(KEY_ALIAS)) {
            keyStore.deleteEntry(KEY_ALIAS)
        }
    }
}

data class EncryptedData(
    val cipherText: String,
    val iv: String
)
