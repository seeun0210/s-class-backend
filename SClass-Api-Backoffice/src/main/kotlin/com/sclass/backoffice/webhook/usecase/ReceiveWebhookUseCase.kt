package com.sclass.backoffice.webhook.usecase

import com.sclass.backoffice.webhook.dto.GoogleFormWebhookPayload
import com.sclass.backoffice.webhook.event.DiagnosisRequestedEvent
import com.sclass.common.annotation.UseCase
import com.sclass.domain.common.vo.Ulid
import com.sclass.domain.domains.diagnosis.adaptor.DiagnosisAdaptor
import com.sclass.domain.domains.diagnosis.domain.Diagnosis
import com.sclass.domain.domains.webhook.adaptor.WebhookAdaptor
import com.sclass.domain.domains.webhook.adaptor.WebhookLogAdaptor
import com.sclass.domain.domains.webhook.domain.WebhookLog
import com.sclass.domain.domains.webhook.domain.WebhookStatus
import com.sclass.domain.domains.webhook.exception.WebhookInactiveException
import com.sclass.domain.domains.webhook.exception.WebhookInvalidSecretException
import org.springframework.context.ApplicationEventPublisher
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import tools.jackson.databind.ObjectMapper

@UseCase
class ReceiveWebhookUseCase(
    private val webhookAdaptor: WebhookAdaptor,
    private val webhookLogAdaptor: WebhookLogAdaptor,
    private val diagnosisAdaptor: DiagnosisAdaptor,
    private val objectMapper: ObjectMapper,
    private val eventPublisher: ApplicationEventPublisher,
) {
    @Transactional
    fun execute(
        webhookId: String,
        secret: String,
        payload: GoogleFormWebhookPayload,
    ) {
        val webhook = webhookAdaptor.findById(webhookId)

        if (webhook.secret != secret) throw WebhookInvalidSecretException()
        if (webhook.status == WebhookStatus.INACTIVE) throw WebhookInactiveException()

        val log =
            webhookLogAdaptor.save(
                WebhookLog(
                    webhookId = webhookId,
                    payload = objectMapper.writeValueAsString(payload),
                ),
            )

        val extracted = webhook.fieldMapping.extractFrom(payload.answers)
        val requestId = Ulid.generate()

        val diagnosis =
            diagnosisAdaptor.save(
                Diagnosis(
                    requestId = requestId,
                    studentName = extracted.studentName,
                    studentPhone = extracted.studentPhone,
                    parentPhone = extracted.parentPhone,
                    requestData = objectMapper.writeValueAsString(payload.answers),
                ),
            )

        log.markProcessing()
        log.linkDiagnosis(diagnosis.id)

        val callbackUrl =
            ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/report-webhooks/survey-report")
                .build()
                .toUriString()

        eventPublisher.publishEvent(
            DiagnosisRequestedEvent(
                diagnosisId = diagnosis.id,
                requestId = requestId,
                studentName = extracted.studentName,
                answers = payload.answers,
                callbackUrl = callbackUrl,
            ),
        )
    }
}
