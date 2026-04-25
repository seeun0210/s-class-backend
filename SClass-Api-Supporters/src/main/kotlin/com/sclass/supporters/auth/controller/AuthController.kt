package com.sclass.supporters.auth.controller

import com.sclass.common.dto.ApiResponse
import com.sclass.supporters.auth.dto.LoginRequest
import com.sclass.supporters.auth.dto.RefreshRequest
import com.sclass.supporters.auth.dto.RegisterRequest
import com.sclass.supporters.auth.dto.TokenResponse
import com.sclass.supporters.auth.usecase.LoginUseCase
import com.sclass.supporters.auth.usecase.LogoutUseCase
import com.sclass.supporters.auth.usecase.RefreshUseCase
import com.sclass.supporters.auth.usecase.RegisterUseCase
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val registerUseCase: RegisterUseCase,
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val refreshUseCase: RefreshUseCase,
) {
    @PostMapping("/register")
    fun register(
        @Valid @RequestBody request: RegisterRequest,
    ): ApiResponse<TokenResponse> = ApiResponse.success(registerUseCase.execute(request))

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
    ): ApiResponse<TokenResponse> = ApiResponse.success(loginUseCase.execute(request))

    @PostMapping("/refresh")
    fun refresh(
        @Valid @RequestBody request: RefreshRequest,
    ): ApiResponse<TokenResponse> = ApiResponse.success(refreshUseCase.execute(request))

    @PostMapping("/logout")
    fun logout(
        @Valid @RequestBody request: RefreshRequest,
    ): ApiResponse<Nothing> {
        logoutUseCase.execute(request)
        return ApiResponse.success()
    }
}
