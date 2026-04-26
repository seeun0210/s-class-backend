package com.sclass.backoffice.oauth.controller

import com.sclass.backoffice.oauth.dto.CentralGoogleConnectionStatusResponse
import com.sclass.backoffice.oauth.dto.ConnectCentralGoogleRequest
import com.sclass.backoffice.oauth.usecase.ConnectCentralGoogleUseCase
import com.sclass.backoffice.oauth.usecase.DisconnectCentralGoogleUseCase
import com.sclass.backoffice.oauth.usecase.GetCentralGoogleConnectionStatusUseCase
import com.sclass.backoffice.oauth.usecase.IssueCentralGoogleOAuthStateUseCase
import com.sclass.common.annotation.CurrentUserId
import com.sclass.common.dto.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.CacheControl
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/google/calendar/central")
class CentralGoogleConnectionController(
    private val connectCentralGoogleUseCase: ConnectCentralGoogleUseCase,
    private val disconnectCentralGoogleUseCase: DisconnectCentralGoogleUseCase,
    private val getCentralGoogleConnectionStatusUseCase: GetCentralGoogleConnectionStatusUseCase,
    private val issueCentralGoogleOAuthStateUseCase: IssueCentralGoogleOAuthStateUseCase,
) {
    @PostMapping("/connect")
    fun connect(
        @CurrentUserId adminUserId: String,
        @Valid @RequestBody request: ConnectCentralGoogleRequest,
    ): ApiResponse<CentralGoogleConnectionStatusResponse> = ApiResponse.success(connectCentralGoogleUseCase.execute(adminUserId, request))

    @DeleteMapping
    fun disconnect(): ApiResponse<Unit> {
        disconnectCentralGoogleUseCase.execute()
        return ApiResponse.success(Unit)
    }

    @GetMapping
    fun status(): ApiResponse<CentralGoogleConnectionStatusResponse> =
        ApiResponse.success(getCentralGoogleConnectionStatusUseCase.execute())

    @PostMapping("/state")
    fun issueState(
        @CurrentUserId adminUserId: String,
    ) = ApiResponse.success(
        data = issueCentralGoogleOAuthStateUseCase.execute(adminUserId),
        cacheControl = CacheControl.noStore(),
    )
}
