package com.sclass.supporters.payment.controller

import com.sclass.common.annotation.CurrentUserId
import com.sclass.common.dto.ApiResponse
import com.sclass.infrastructure.nicepay.dto.NicePayWebhookPayload
import com.sclass.supporters.payment.dto.PreparePaymentRequest
import com.sclass.supporters.payment.dto.PreparePaymentResponse
import com.sclass.supporters.payment.usecase.HandleNicePayReturnUseCase
import com.sclass.supporters.payment.usecase.HandleNicePayWebhookUseCase
import com.sclass.supporters.payment.usecase.PreparePaymentUseCase
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/api/v1/payments")
class PaymentController(
    private val preparePaymentUseCase: PreparePaymentUseCase,
    private val handleNicePayWebhookUseCase: HandleNicePayWebhookUseCase,
    private val handleNicePayReturnUseCase: HandleNicePayReturnUseCase,
) {
    @PostMapping
    fun prepare(
        @CurrentUserId userId: String,
        @Valid @RequestBody request: PreparePaymentRequest,
    ): ApiResponse<PreparePaymentResponse> = ApiResponse.success(preparePaymentUseCase.execute(userId, request))

    @PostMapping("/nicepay/webhook", produces = [MediaType.TEXT_HTML_VALUE])
    fun handleNicepayWebhook(
        @RequestBody request: NicePayWebhookPayload,
    ): String {
        handleNicePayWebhookUseCase.execute(request.orderId, request)
        return "OK"
    }

    @PostMapping("/nicepay")
    fun handleNicePayReturn(
        @RequestParam authResultCode: String,
        @RequestParam(required = false) tid: String?,
        @RequestParam orderId: String,
        @RequestParam amount: Int,
        @RequestParam(required = false) authToken: String?,
        @RequestParam(required = false) signature: String?,
    ): ResponseEntity<Unit> {
        val redirectUrl =
            handleNicePayReturnUseCase.execute(
                authResultCode = authResultCode,
                tid = tid,
                orderId = orderId,
                amount = amount,
                authToken = authToken,
                signature = signature,
            )
        return ResponseEntity
            .status(HttpStatus.FOUND)
            .location(URI.create(redirectUrl))
            .build()
    }
}
