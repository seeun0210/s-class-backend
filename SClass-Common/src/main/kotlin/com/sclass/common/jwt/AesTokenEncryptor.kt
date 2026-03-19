package com.sclass.common.jwt

import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

@Component
class AesTokenEncryptor(
    private val properties: TokenEncryptionProperties,
) {
    companion object {
        private const val ALGORITHM = "AES"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 128
    }

    private fun getSecretKey(): SecretKeySpec {
        val keyBytes = properties.secretKey.toByteArray(StandardCharsets.UTF_8)
        require(keyBytes.size == 32) { "AES-256 key must be 32 bytes" }
        return SecretKeySpec(keyBytes, ALGORITHM)
    }

    fun encrypt(plainToken: String): String {
        val iv = ByteArray(GCM_IV_LENGTH)
        SecureRandom().nextBytes(iv)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), GCMParameterSpec(GCM_TAG_LENGTH, iv))
        val encrypted = cipher.doFinal(plainToken.toByteArray(StandardCharsets.UTF_8))
        val combined = iv + encrypted
        return Base64.getUrlEncoder().withoutPadding().encodeToString(combined)
    }

    fun decrypt(encryptedToken: String): String {
        val combined = Base64.getUrlDecoder().decode(encryptedToken)
        val iv = combined.copyOfRange(0, GCM_IV_LENGTH)
        val encrypted = combined.copyOfRange(GCM_IV_LENGTH, combined.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), GCMParameterSpec(GCM_TAG_LENGTH, iv))
        return String(cipher.doFinal(encrypted), StandardCharsets.UTF_8)
    }
}
