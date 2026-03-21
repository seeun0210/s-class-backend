package com.sclass.domain.config

import com.sclass.common.jwt.AesTokenEncryptor
import com.sclass.common.jwt.JwtProperties
import com.sclass.common.jwt.JwtTokenProvider
import com.sclass.common.jwt.TokenEncryptionProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
@EnableConfigurationProperties(JwtProperties::class, TokenEncryptionProperties::class)
class DomainTestConfig {
    @Bean
    fun jwtTokenProvider(jwtProperties: JwtProperties): JwtTokenProvider = JwtTokenProvider(jwtProperties)

    @Bean
    fun aesTokenEncryptor(properties: TokenEncryptionProperties): AesTokenEncryptor = AesTokenEncryptor(properties)
}
