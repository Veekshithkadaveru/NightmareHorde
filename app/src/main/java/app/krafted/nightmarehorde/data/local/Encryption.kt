package app.krafted.nightmarehorde.data.local

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

class SaveCorruptionException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)

@Singleton
class SaveEncryption @Inject constructor() {

    private companion object {
        const val KEY_ALIAS = "nightmare_horde_save_key"
        const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_TAG_LENGTH = 128
        const val GCM_IV_LENGTH = 12
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        keyStore.getEntry(KEY_ALIAS, null)?.let {
            return (it as KeyStore.SecretKeyEntry).secretKey
        }
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
        keyGenerator.init(
            KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return keyGenerator.generateKey()
    }

    fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val iv = cipher.iv
        val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        val combined = iv + cipherText
        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    fun decrypt(encryptedText: String): String {
        try {
            val combined = Base64.decode(encryptedText, Base64.DEFAULT)
            val iv = combined.copyOfRange(0, GCM_IV_LENGTH)
            val cipherText = combined.copyOfRange(GCM_IV_LENGTH, combined.size)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(GCM_TAG_LENGTH, iv))
            return String(cipher.doFinal(cipherText), Charsets.UTF_8)
        } catch (e: Exception) {
            throw SaveCorruptionException("Failed to decrypt save data", e)
        }
    }
}
