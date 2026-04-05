package com.sclass.supporters.payment.controller

import com.sclass.common.annotation.CurrentUserId
import com.sclass.common.dto.ApiResponse
import com.sclass.infrastructure.nicepay.dto.NicePayWebhookPayload
import com.sclass.supporters.payment.dto.ApprovePaymentRequest
import com.sclass.supporters.payment.dto.ApprovePaymentResponse
import com.sclass.supporters.payment.dto.PreparePaymentRequest
import com.sclass.supporters.payment.dto.PreparePaymentResponse
import com.sclass.supporters.payment.usecase.ApprovePaymentUseCase
import com.sclass.supporters.payment.usecase.HandleNicePayWebhookUseCase
import com.sclass.supporters.payment.usecase.PreparePaymentUseCase
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/payments")
class PaymentController(
    private val preparePaymentUseCase: PreparePaymentUseCase,
    private val approvePaymentUseCase: ApprovePaymentUseCase,
    private val handleNicePayWebhookUseCase: HandleNicePayWebhookUseCase,
) {
    @PostMapping
    fun prepare(
        @CurrentUserId userId: String,
        @Valid @RequestBody request: PreparePaymentRequest,
    ): ApiResponse<PreparePaymentResponse> = ApiResponse.success(preparePaymentUseCase.execute(userId, request))

    @PostMapping("/{paymentId}/approval")
    fun approve(
        @CurrentUserId userId: String,
        @PathVariable paymentId: String,
        @Valid @RequestBody request: ApprovePaymentRequest,
    ): ApiResponse<ApprovePaymentResponse> = ApiResponse.success(approvePaymentUseCase.execute(userId, paymentId, request.tid))

    @PostMapping("/nicepay/webhook", produces = [MediaType.TEXT_HTML_VALUE])
    fun handleNicepayWebhook(
        @RequestBody request: NicePayWebhookPayload,
    ): String {
        handleNicePayWebhookUseCase.execute(request)
        return "OK"
    }
}
