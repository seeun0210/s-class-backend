package com.sclass.domain.domains.webhook.domain

import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class WebhookTest {
    @Nested
    inner class ToggleStatus {
        @Test
        fun `ACTIVE 상태에서 toggleStatus 호출 시 INACTIVE로 변경된다`() {
            val webhook =
                Webhook.create(
                    name = "테스트 웹훅",
                    type = WebhookType.GOOGLE_FORM_DIAGNOSIS,
                    fieldMapping = WebhookFieldMapping("학생 이름", "학생 연락처", null),
                )

            webhook.toggleStatus()

            assertEquals(WebhookStatus.INACTIVE, webhook.status)
        }

        @Test
        fun `INACTIVE 상태에서 toggleStatus 호출 시 ACTIVE로 변경된다`() {
            val webhook =
                Webhook.create(
                    name = "테스트 웹훅",
                    type = WebhookType.GOOGLE_FORM_DIAGNOSIS,
                    fieldMapping = WebhookFieldMapping("학생 이름", "학생 연락처", null),
                )
            webhook.toggleStatus()

            webhook.toggleStatus()

            assertEquals(WebhookStatus.ACTIVE, webhook.status)
        }
    }

    @Nested
    inner class Create {
        @Test
        fun `생성 시 secret은 64자 hex 문자열이다`() {
            val webhook =
                Webhook.create(
                    name = "테스트 웹훅",
                    type = WebhookType.GOOGLE_FORM_DIAGNOSIS,
                    fieldMapping = WebhookFieldMapping("학생 이름", "학생 연락처", null),
                )

            assertAll(
                { assertEquals(64, webhook.secret.length) },
                { assertTrue(webhook.secret.all { it.isDigit() || it in 'a'..'f' }) },
            )
        }

        @Test
        fun `생성 시 기본 status는 ACTIVE이다`() {
            val webhook =
                Webhook.create(
                    name = "테스트 웹훅",
                    type = WebhookType.GOOGLE_FORM_DIAGNOSIS,
                    fieldMapping = WebhookFieldMapping("학생 이름", "학생 연락처", null),
                )

            assertEquals(WebhookStatus.ACTIVE, webhook.status)
        }
    }
}
