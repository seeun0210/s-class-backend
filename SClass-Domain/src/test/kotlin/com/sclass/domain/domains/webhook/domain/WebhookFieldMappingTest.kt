package com.sclass.domain.domains.webhook.domain

import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class WebhookFieldMappingTest {
    private val mapping =
        WebhookFieldMapping(
            studentNameQuestion = "학생 이름",
            studentPhoneQuestion = "학생 연락처",
            parentPhoneQuestion = "학부모 연락처",
        )

    @Nested
    inner class ExtractFrom {
        @Test
        fun `모든 필드가 존재하면 정상 추출한다`() {
            val answers =
                mapOf(
                    "학생 이름" to "홍길동",
                    "학생 연락처" to "010-1234-5678",
                    "학부모 연락처" to "010-9876-5432",
                )

            val result = mapping.extractFrom(answers)

            assertAll(
                { assertEquals("홍길동", result.studentName) },
                { assertEquals("010-1234-5678", result.studentPhone) },
                { assertEquals("010-9876-5432", result.parentPhone) },
            )
        }

        @Test
        fun `학부모 연락처가 없어도 정상 추출한다`() {
            val mappingWithNullParent =
                WebhookFieldMapping(
                    studentNameQuestion = "학생 이름",
                    studentPhoneQuestion = "학생 연락처",
                    parentPhoneQuestion = null,
                )
            val answers =
                mapOf(
                    "학생 이름" to "홍길동",
                    "학생 연락처" to "010-1234-5678",
                )

            val result = mappingWithNullParent.extractFrom(answers)

            assertAll(
                { assertEquals("홍길동", result.studentName) },
                { assertEquals("010-1234-5678", result.studentPhone) },
                { assertNull(result.parentPhone) },
            )
        }

        @Test
        fun `학생 이름 질문이 answers에 없으면 예외를 던진다`() {
            val answers = mapOf("다른 질문" to "값")

            assertThrows(IllegalArgumentException::class.java) {
                mapping.extractFrom(answers)
            }
        }
    }
}
