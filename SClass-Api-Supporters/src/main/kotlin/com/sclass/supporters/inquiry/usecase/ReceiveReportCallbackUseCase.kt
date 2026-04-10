package com.sclass.supporters.inquiry.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.inquiryplan.adaptor.InquiryPlanAdaptor
import com.sclass.domain.domains.inquiryplan.domain.InquiryPlanStatus
import com.sclass.domain.domains.webhook.exception.WebhookInvalidSecretException
import com.sclass.infrastructure.report.ReportServiceProperties
import com.sclass.infrastructure.report.dto.ReportCallbackPayload
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper
import java.security.MessageDigest
import java.time.Instant
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.abs

@UseCase
class ReceiveReportCallbackUseCase(
    private val inquiryPlanAdaptor: InquiryPlanAdaptor,
    private val objectMapper: ObjectMapper,
    private val properties: ReportServiceProperties,
) {
    @Transactional
    fun execute(
        signature: String,
        timestamp: String,
        rawBody: String,
    ) {
        verifyTimestamp(timestamp)
        verifySignature(timestamp, rawBody, signature)

        val payload = objectMapper.readValue(rawBody, ReportCallbackPayload::class.java)
        val plan = inquiryPlanAdaptor.findById(payload.requestId.toLong())

        if (plan.status != InquiryPlanStatus.PENDING) return

        when (payload.event) {
            "report.completed" -> plan.markReady(payload.result?.topic)
            "report.failed" -> plan.markFailed(payload.error?.message ?: "알 수 없는 오류")
        }
        inquiryPlanAdaptor.save(plan)
    }

    private fun verifyTimestamp(timestamp: String) {
        val ts = timestamp.toLongOrNull() ?: throw WebhookInvalidSecretException()
        if (abs(Instant.now().epochSecond - ts) > 300) throw WebhookInvalidSecretException()
    }

    private fun verifySignature(
        timestamp: String,
        rawBody: String,
        signature: String,
    ) {
        val expected =
            Mac
                .getInstance("HmacSHA256")
                .apply { init(SecretKeySpec(properties.callbackSecret.toByteArray(), "HmacSHA256")) }
                .doFinal("$timestamp.$rawBody".toByteArray())
                .joinToString("", prefix = "sha256=") { "%02x".format(it) }
        if (!MessageDigest.isEqual(expected.toByteArray(), signature.toByteArray())) {
            throw WebhookInvalidSecretException()
        }
    }
}
