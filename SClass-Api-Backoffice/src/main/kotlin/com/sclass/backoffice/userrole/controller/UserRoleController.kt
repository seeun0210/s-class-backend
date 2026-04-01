package com.sclass.backoffice.userrole.controller

import com.sclass.backoffice.userrole.dto.AddUserRoleRequest
import com.sclass.backoffice.userrole.dto.AddUserRoleResponse
import com.sclass.backoffice.userrole.dto.UpdateUserRoleStateRequest
import com.sclass.backoffice.userrole.usecase.AddUserRoleUseCase
import com.sclass.backoffice.userrole.usecase.DeleteUserRoleUseCase
import com.sclass.backoffice.userrole.usecase.UpdateUserRoleStateUseCase
import com.sclass.common.annotation.CurrentUserId
import com.sclass.common.dto.ApiResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/user-roles")
class UserRoleController(
    private val updateUserRoleStateUseCase: UpdateUserRoleStateUseCase,
    private val addUserRoleUseCase: AddUserRoleUseCase,
    private val deleteUserRoleUseCase: DeleteUserRoleUseCase,
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

    @PostMapping
    fun addUserRole(
        @RequestBody @Valid request: AddUserRoleRequest,
    ): ApiResponse<AddUserRoleResponse> {
        val response = addUserRoleUseCase.execute(request)
        return ApiResponse.success(response)
    }

    @DeleteMapping("/{userRoleId}")
    fun deleteUserRole(
        @PathVariable userRoleId: String,
    ): ApiResponse<Nothing> {
        deleteUserRoleUseCase.execute(userRoleId)
        return ApiResponse.success()
    }
}
