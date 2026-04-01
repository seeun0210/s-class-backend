package com.sclass.supporters.config

import com.google.cloud.storage.Storage
import com.sclass.infrastructure.email.EmailSender
import com.sclass.infrastructure.message.CommissionNotificationSender
import com.sclass.infrastructure.message.VerificationCodeSender
import com.sclass.infrastructure.oauth.OAuthClientFactory
import com.sclass.infrastructure.oauth.client.OAuthClient
import io.mockk.mockk
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.presigner.S3Presigner

@TestConfiguration
class IntegrationTestConfig {
    @Bean
    @Primary
    fun mockOAuthClient(): OAuthClient = mockk<OAuthClient>(relaxed = true)

    @Bean
    @Primary
    fun mockOAuthClientFactory(): OAuthClientFactory = mockk<OAuthClientFactory>()

    @Bean
    @Primary
    fun mockS3Client(): S3Client = mockk<S3Client>(relaxed = true)

    @Bean
    @Primary
    fun mockS3Presigner(): S3Presigner = mockk<S3Presigner>(relaxed = true)

    @Bean
    @Primary
    fun mockGcsStorage(): Storage = mockk<Storage>(relaxed = true)

    @Bean
    @Primary
    fun mockMessageSender(): VerificationCodeSender = mockk<VerificationCodeSender>(relaxed = true)

    @Bean
    @Primary
    fun mockEmailSender(): EmailSender = mockk<EmailSender>(relaxed = true)

    @Bean
    @Primary
    fun mockCommissionNotificationSender(): CommissionNotificationSender = mockk<CommissionNotificationSender>(relaxed = true)
}
