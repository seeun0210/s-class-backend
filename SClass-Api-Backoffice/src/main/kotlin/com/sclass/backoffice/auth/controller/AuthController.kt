package com.sclass.backoffice.auth.controller

import com.sclass.backoffice.auth.dto.LoginRequest
import com.sclass.backoffice.auth.dto.TokenResponse
import com.sclass.backoffice.auth.usecase.LoginUseCase
import com.sclass.common.dto.ApiResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val loginUseCase: LoginUseCase,
) {
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
    ): ApiResponse<TokenResponse> = ApiResponse.success(loginUseCase.execute(request))
}
