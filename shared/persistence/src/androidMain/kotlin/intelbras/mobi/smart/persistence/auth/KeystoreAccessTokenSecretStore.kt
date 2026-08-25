package intelbras.mobi.smart.persistence.auth

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import intelbras.mobi.smart.persistence.ACCESS_TOKEN_SECRET_KEY
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

internal class KeystoreAccessTokenSecretStore(context: Context) : AccessTokenSecretStore {

    private val preferences =
        context.getSharedPreferences(SECRETS_FILE_NAME, Context.MODE_PRIVATE)

    override fun read(): String? {
        val sealedValue = preferences.getString(ACCESS_TOKEN_SECRET_KEY, null) ?: return null
        return runCatching { decrypt(sealedValue) }.getOrElse {
            clear()
            null
        }
    }

    override fun write(token: String) {
        preferences.edit().putString(ACCESS_TOKEN_SECRET_KEY, encrypt(token)).apply()
    }

    override fun clear() {
        preferences.edit().remove(ACCESS_TOKEN_SECRET_KEY).apply()
    }

    private fun encrypt(token: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.ENCRYPT_MODE, secretKey()) }
        val cipherText = cipher.doFinal(token.encodeToByteArray())
        return "${cipher.iv.toBase64()}$SEPARATOR${cipherText.toBase64()}"
    }

    private fun decrypt(sealedValue: String): String {
        val (initializationVector, cipherText) = sealedValue.split(SEPARATOR)
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(
                Cipher.DECRYPT_MODE,
                secretKey(),
                GCMParameterSpec(AUTHENTICATION_TAG_BITS, initializationVector.fromBase64()),
            )
        }
        return cipher.doFinal(cipherText.fromBase64()).decodeToString()
    }

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
        (keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }

        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE).apply {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()
            )
        }.generateKey()
    }

    private fun ByteArray.toBase64(): String = Base64.encodeToString(this, Base64.NO_WRAP)

    private fun String.fromBase64(): ByteArray = Base64.decode(this, Base64.NO_WRAP)

    private companion object {
        const val ANDROID_KEY_STORE = "AndroidKeyStore"
        const val KEY_ALIAS = "intelbras.mobi.smart.access_token"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val AUTHENTICATION_TAG_BITS = 128
        const val SECRETS_FILE_NAME = "smart_home_secrets"
        const val SEPARATOR = ":"
    }
}
