package com.sclass.supporters.auth.controller

import com.sclass.common.dto.ApiResponse
import com.sclass.supporters.auth.dto.SendEmailCodeRequest
import com.sclass.supporters.auth.dto.SendEmailCodeResponse
import com.sclass.supporters.auth.dto.VerifyEmailCodeRequest
import com.sclass.supporters.auth.dto.VerifyEmailCodeResponse
import com.sclass.supporters.auth.usecase.EmailVerificationUseCase
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth/email")
class EmailVerificationController(
    private val emailVerificationUseCase: EmailVerificationUseCase,
) {
    @PostMapping("/send-code")
    fun sendCode(
        @Valid @RequestBody request: SendEmailCodeRequest,
    ): ApiResponse<SendEmailCodeResponse> = ApiResponse.success(emailVerificationUseCase.sendCode(request))

    @PostMapping("/verify-code")
    fun verifyCode(
        @Valid @RequestBody request: VerifyEmailCodeRequest,
    ): ApiResponse<VerifyEmailCodeResponse> = ApiResponse.success(emailVerificationUseCase.verifyCode(request))
}
