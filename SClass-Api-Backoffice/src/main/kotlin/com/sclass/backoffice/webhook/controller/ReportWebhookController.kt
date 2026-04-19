package com.sclass.backoffice.webhook.controller

import com.sclass.backoffice.webhook.usecase.ReceiveReportWebhookUseCase
import com.sclass.common.annotation.Public
import com.sclass.common.dto.ApiResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Public
@RestController
@RequestMapping("/api/report-webhooks")
class ReportWebhookController(
    private val receiveReportWebhookUseCase: ReceiveReportWebhookUseCase,
) {
    @PostMapping("/survey-report")
    fun receive(
        @RequestHeader("X-Webhook-Signature") signature: String,
        @RequestHeader("X-Webhook-Timestamp") timestamp: String,
        @RequestHeader("X-Webhook-Event") event: String,
        @RequestBody rawBody: String,
    ): ApiResponse<Unit> {
        receiveReportWebhookUseCase.execute(signature, timestamp, event, rawBody)
        return ApiResponse.success(Unit)
    }
}
