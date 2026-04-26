package com.sclass.supporters.oauth.controller

import com.sclass.common.annotation.CurrentUserId
import com.sclass.common.dto.ApiResponse
import com.sclass.supporters.oauth.dto.ConnectGoogleRequest
import com.sclass.supporters.oauth.dto.GoogleConnectionStatusResponse
import com.sclass.supporters.oauth.usecase.ConnectGoogleUseCase
import com.sclass.supporters.oauth.usecase.DisconnectGoogleUseCase
import com.sclass.supporters.oauth.usecase.GetGoogleConnectionStatusUseCase
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/teachers/me/google")
class GoogleConnectionController(
    private val connectGoogleUseCase: ConnectGoogleUseCase,
    private val disconnectGoogleUseCase: DisconnectGoogleUseCase,
    private val getStatusUseCase: GetGoogleConnectionStatusUseCase,
) {
    @PostMapping("/connect")
    fun connect(
        @CurrentUserId userId: String,
        @Valid @RequestBody request: ConnectGoogleRequest,
    ): ApiResponse<GoogleConnectionStatusResponse> = ApiResponse.success(connectGoogleUseCase.execute(userId, request))

    @DeleteMapping
    fun disconnect(
        @CurrentUserId userId: String,
    ): ApiResponse<Unit> {
        disconnectGoogleUseCase.execute(userId)
        return ApiResponse.success(Unit)
    }

    @GetMapping
    fun status(
        @CurrentUserId userId: String,
    ): ApiResponse<GoogleConnectionStatusResponse> = ApiResponse.success(getStatusUseCase.execute(userId))
}
