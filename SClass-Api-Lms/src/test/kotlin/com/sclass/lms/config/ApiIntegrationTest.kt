package com.sclass.lms.config

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(IntegrationTestConfig::class)
@TestPropertySource(
    properties = [
        "cloud.aws.s3.endpoint=http://localhost:4566",
        "cloud.gcp.storage.credentials-location=classpath:test-gcs-credentials.json",
    ],
)
annotation class ApiIntegrationTest
