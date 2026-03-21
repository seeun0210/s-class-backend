package com.sclass.management.auth.controller

import com.sclass.common.dto.ApiResponse
import com.sclass.management.auth.dto.OAuthCompleteSignupRequest
import com.sclass.management.auth.dto.OAuthLoginRequest
import com.sclass.management.auth.dto.OAuthLoginResponse
import com.sclass.management.auth.dto.TokenResponse
import com.sclass.management.auth.usecase.OAuthLoginUseCase
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth/oauth")
class OAuthController(
    private val oAuthLoginUseCase: OAuthLoginUseCase,
) {
    @PostMapping("/login")
    fun oauthLogin(
        @Valid @RequestBody request: OAuthLoginRequest,
    ): ApiResponse<OAuthLoginResponse> = ApiResponse.success(oAuthLoginUseCase.login(request))

    @PostMapping("/complete-signup")
    fun completeSignup(
        @Valid @RequestBody request: OAuthCompleteSignupRequest,
    ): ApiResponse<TokenResponse> = ApiResponse.success(oAuthLoginUseCase.completeSignup(request))
}
