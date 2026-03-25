package com.sclass.common.jwt

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AesTokenEncryptorTest {
    private lateinit var encryptor: AesTokenEncryptor

    @BeforeEach
    fun setUp() {
        val properties = TokenEncryptionProperties(secretKey = "01234567890123456789012345678901")
        encryptor = AesTokenEncryptor(properties)
    }

    @Test
    fun `암호화 후 복호화하면 원본 텍스트가 복원된다`() {
        val plainText = "test-jwt-token-value"

        val encrypted = encryptor.encrypt(plainText)
        val decrypted = encryptor.decrypt(encrypted)

        assertEquals(plainText, decrypted)
    }

    @Test
    fun `같은 평문을 암호화해도 매번 다른 암호문이 생성된다`() {
        val plainText = "same-token"

        val encrypted1 = encryptor.encrypt(plainText)
        val encrypted2 = encryptor.encrypt(plainText)

        assertNotEquals(encrypted1, encrypted2)
    }

    @Test
    fun `암호문이 변조되면 복호화에 실패한다`() {
        val encrypted = encryptor.encrypt("test-token")
        val tampered = encrypted.substring(0, encrypted.length - 2) + "XX"

        assertThrows<Exception> {
            encryptor.decrypt(tampered)
        }
    }

    @Test
    fun `다른 키로 복호화하면 실패한다`() {
        val encrypted = encryptor.encrypt("test-token")

        val otherProperties = TokenEncryptionProperties(secretKey = "abcdefghijklmnopqrstuvwxyz012345")
        val otherEncryptor = AesTokenEncryptor(otherProperties)

        assertThrows<Exception> {
            otherEncryptor.decrypt(encrypted)
        }
    }
}
