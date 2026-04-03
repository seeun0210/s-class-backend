package com.sclass.backoffice.webhook.controller

import com.sclass.backoffice.webhook.dto.GoogleFormWebhookPayload
import com.sclass.backoffice.webhook.usecase.ReceiveWebhookUseCase
import com.sclass.common.dto.ApiResponse
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class WebhookReceiverController(
    private val receiveWebhookUseCase: ReceiveWebhookUseCase,
) {
    @PostMapping("/webhook/{webhookId}")
    fun receive(
        @PathVariable webhookId: String,
        @RequestHeader("X-Webhook-Secret") secret: String,
        @RequestBody payload: GoogleFormWebhookPayload,
    ): ApiResponse<Unit> {
        receiveWebhookUseCase.execute(webhookId, secret, payload)
        return ApiResponse.success(Unit)
    }
}
