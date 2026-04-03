package com.sclass.domain.domains.webhook.domain

import com.sclass.domain.common.vo.Ulid
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class WebhookLogTest {
    private fun createLog() =
        WebhookLog(
            webhookId = Ulid.generate(),
            payload = """{"학생 이름": "홍길동"}""",
        )

    @Test
    fun `markProcessing 호출 시 status가 PROCESSING으로 변경된다`() {
        val log = createLog()
        log.markProcessing()
        assertEquals(WebhookLogStatus.PROCESSING, log.status)
    }

    @Test
    fun `markCompleted 호출 시 status가 COMPLETED로 변경된다`() {
        val log = createLog()
        log.markCompleted()
        assertEquals(WebhookLogStatus.COMPLETED, log.status)
    }

    @Test
    fun `markFailed 호출 시 status와 errorMessage가 설정된다`() {
        val log = createLog()
        log.markFailed("외부 API 호출 실패")
        assertAll(
            { assertEquals(WebhookLogStatus.FAILED, log.status) },
            { assertEquals("외부 API 호출 실패", log.errorMessage) },
        )
    }

    @Test
    fun `linkDiagnosis 호출 시 diagnosisId가 설정된다`() {
        val log = createLog()
        val diagnosisId = Ulid.generate()
        log.linkDiagnosis(diagnosisId)
        assertEquals(diagnosisId, log.diagnosisId)
    }
}
