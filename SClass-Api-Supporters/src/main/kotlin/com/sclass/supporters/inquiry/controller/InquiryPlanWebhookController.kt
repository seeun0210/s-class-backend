package com.sclass.supporters.inquiry.controller

import com.sclass.common.dto.ApiResponse
import com.sclass.supporters.inquiry.usecase.ReceiveReportCallbackUseCase
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/webhooks")
class InquiryPlanWebhookController(
    private val receiveReportCallbackUseCase: ReceiveReportCallbackUseCase,
) {
    @PostMapping("/report")
    fun receiveReportCallback(
        @RequestHeader("X-Webhook-Signature") signature: String,
        @RequestHeader("X-Webhook-Timestamp") timestamp: String,
        @RequestBody rawBody: String,
    ): ApiResponse<Unit> {
        receiveReportCallbackUseCase.execute(signature, timestamp, rawBody)
        return ApiResponse.success(Unit)
    }
}
