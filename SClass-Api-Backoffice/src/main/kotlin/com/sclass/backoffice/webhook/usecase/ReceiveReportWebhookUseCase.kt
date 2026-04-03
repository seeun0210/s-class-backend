package com.sclass.backoffice.webhook.usecase

import com.sclass.backoffice.webhook.dto.SurveyReportCallbackPayload
import com.sclass.backoffice.webhook.event.SurveyReportCompletedEvent
import com.sclass.backoffice.webhook.event.SurveyReportFailedEvent
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.diagnosis.adaptor.DiagnosisAdaptor
import com.sclass.domain.domains.diagnosis.domain.DiagnosisStatus
import com.sclass.domain.domains.webhook.exception.WebhookInvalidSecretException
import org.springframework.context.ApplicationEventPublisher
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper
import java.security.MessageDigest
import java.time.Instant
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.abs

@UseCase
class ReceiveReportWebhookUseCase(
    private val diagnosisAdaptor: DiagnosisAdaptor,
    private val objectMapper: ObjectMapper,
    private val eventPublisher: ApplicationEventPublisher,
) {
    @Transactional
    fun execute(
        signature: String,
        timestamp: String,
        event: String,
        rawBody: String,
    ) {
        verifyTimestamp(timestamp)

        val payload = objectMapper.readValue(rawBody, SurveyReportCallbackPayload::class.java)

        val diagnosis = diagnosisAdaptor.findByRequestId(payload.requestId)

        verifySignature(diagnosis.callbackSecret, timestamp, rawBody, signature)

        if (diagnosis.status == DiagnosisStatus.COMPLETED || diagnosis.status == DiagnosisStatus.FAILED) return

        when (event) {
            "survey_report.completed" ->
                eventPublisher.publishEvent(
                    SurveyReportCompletedEvent(
                        diagnosisId = diagnosis.id,
                        studentName = diagnosis.studentName,
                        studentPhone = diagnosis.studentPhone,
                        parentPhone = diagnosis.parentPhone,
                        reportData = objectMapper.writeValueAsString(payload.result),
                    ),
                )
            "survey_report.failed" ->
                eventPublisher.publishEvent(
                    SurveyReportFailedEvent(
                        diagnosisId = diagnosis.id,
                        errorCode = payload.error?.code,
                        retryable = payload.error?.retryable ?: false,
                    ),
                )
        }
    }

    private fun verifyTimestamp(timestamp: String) {
        val ts = timestamp.toLongOrNull() ?: throw WebhookInvalidSecretException()
        if (abs(Instant.now().epochSecond - ts) > 300) throw WebhookInvalidSecretException()
    }

    private fun verifySignature(
        secret: String,
        timestamp: String,
        rawBody: String,
        signature: String,
    ) {
        val expected =
            Mac
                .getInstance("HmacSHA256")
                .apply { init(SecretKeySpec(secret.toByteArray(), "HmacSHA256")) }
                .doFinal("$timestamp.$rawBody".toByteArray())
                .joinToString("", prefix = "sha256=") { "%02x".format(it) }
        if (!MessageDigest.isEqual(expected.toByteArray(), signature.toByteArray())) {
            throw WebhookInvalidSecretException()
        }
    }
}
