package com.sclass.backoffice.userrole.controller

import com.sclass.backoffice.userrole.dto.UpdateUserRoleStateRequest
import com.sclass.backoffice.userrole.usecase.UpdateUserRoleStateUseCase
import com.sclass.common.annotation.CurrentUserId
import com.sclass.common.dto.ApiResponse
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/user-roles")
class UserRoleController(
    private val updateUserRoleStateUseCase: UpdateUserRoleStateUseCase,
) {
    @PatchMapping("/{userRoleId}/state")
    fun updateState(
        @PathVariable userRoleId: String,
        @RequestBody request: UpdateUserRoleStateRequest,
        @CurrentUserId userId: String,
    ): ApiResponse<Nothing> {
        updateUserRoleStateUseCase.execute(userRoleId, request, userId)
        return ApiResponse.success()
    }
}
