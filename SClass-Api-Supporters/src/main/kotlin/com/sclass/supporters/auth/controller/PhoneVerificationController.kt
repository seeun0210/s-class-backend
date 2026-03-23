package com.sclass.supporters.auth.controller

import com.sclass.common.dto.ApiResponse
import com.sclass.supporters.auth.dto.SendPhoneCodeRequest
import com.sclass.supporters.auth.dto.SendPhoneCodeResponse
import com.sclass.supporters.auth.dto.VerifyPhoneCodeRequest
import com.sclass.supporters.auth.dto.VerifyPhoneCodeResponse
import com.sclass.supporters.auth.usecase.PhoneVerificationUseCase
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth/phone")
class PhoneVerificationController(
    private val phoneVerificationUseCase: PhoneVerificationUseCase,
) {
    @PostMapping("/send-code")
    fun sendCode(
        @Valid @RequestBody request: SendPhoneCodeRequest,
    ): ApiResponse<SendPhoneCodeResponse> = ApiResponse.success(phoneVerificationUseCase.sendCode(request))

    @PostMapping("/verify-code")
    fun verifyCode(
        @Valid @RequestBody request: VerifyPhoneCodeRequest,
    ): ApiResponse<VerifyPhoneCodeResponse> = ApiResponse.success(phoneVerificationUseCase.verifyCode(request))
}
