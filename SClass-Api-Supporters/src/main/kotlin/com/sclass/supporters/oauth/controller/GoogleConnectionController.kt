package com.sclass.supporters.oauth.controller

import com.sclass.common.annotation.CurrentUserId
import com.sclass.common.annotation.CurrentUserRole
import com.sclass.common.dto.ApiResponse
import com.sclass.common.exception.ForbiddenException
import com.sclass.domain.domains.user.domain.Role
import com.sclass.supporters.oauth.dto.ConnectGoogleRequest
import com.sclass.supporters.oauth.dto.GoogleConnectionStatusResponse
import com.sclass.supporters.oauth.dto.GoogleOAuthStateResponse
import com.sclass.supporters.oauth.usecase.ConnectGoogleUseCase
import com.sclass.supporters.oauth.usecase.DisconnectGoogleUseCase
import com.sclass.supporters.oauth.usecase.GetGoogleConnectionStatusUseCase
import com.sclass.supporters.oauth.usecase.IssueGoogleOAuthStateUseCase
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
    private val issueStateUseCase: IssueGoogleOAuthStateUseCase,
) {
    @PostMapping("/connect")
    fun connect(
        @CurrentUserId userId: String,
        @CurrentUserRole role: String,
        @Valid @RequestBody request: ConnectGoogleRequest,
    ): ApiResponse<GoogleConnectionStatusResponse> =
        ApiResponse.success(
            connectGoogleUseCase.execute(userId, requireTeacherRole(role), request),
        )

    @DeleteMapping
    fun disconnect(
        @CurrentUserId userId: String,
        @CurrentUserRole role: String,
    ): ApiResponse<Unit> {
        requireTeacherRole(role)
        disconnectGoogleUseCase.execute(userId)
        return ApiResponse.success(Unit)
    }

    @GetMapping
    fun status(
        @CurrentUserId userId: String,
        @CurrentUserRole role: String,
    ): ApiResponse<GoogleConnectionStatusResponse> {
        requireTeacherRole(role)
        return ApiResponse.success(getStatusUseCase.execute(userId))
    }

    @GetMapping("/state")
    fun issue(
        @CurrentUserId userId: String,
        @CurrentUserRole role: String,
    ): ApiResponse<GoogleOAuthStateResponse> =
        ApiResponse.success(
            issueStateUseCase.execute(userId, requireTeacherRole(role)),
        )

    private fun requireTeacherRole(role: String): Role {
        val currentRole =
            runCatching { Role.valueOf(role) }
                .getOrElse { throw ForbiddenException() }
        if (currentRole != Role.TEACHER) throw ForbiddenException()
        return currentRole
    }
}
