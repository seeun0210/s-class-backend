package com.sclass.backoffice.commissionpolicy.controller

import com.sclass.backoffice.commissionpolicy.dto.CommissionPolicyListResponse
import com.sclass.backoffice.commissionpolicy.dto.CommissionPolicyResponse
import com.sclass.backoffice.commissionpolicy.dto.CreateCommissionPolicyRequest
import com.sclass.backoffice.commissionpolicy.dto.UpdateCommissionPolicyActiveRequest
import com.sclass.backoffice.commissionpolicy.usecase.CreateCommissionPolicyUseCase
import com.sclass.backoffice.commissionpolicy.usecase.GetAdminCommissionPolicyListUseCase
import com.sclass.backoffice.commissionpolicy.usecase.UpdateCommissionPolicyActiveUseCase
import com.sclass.common.dto.ApiResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/commission-policies")
class CommissionPolicyController(
    private val createCommissionPolicyUseCase: CreateCommissionPolicyUseCase,
    private val getAdminCommissionPolicyListUseCase: GetAdminCommissionPolicyListUseCase,
    private val updateCommissionPolicyActiveUseCase: UpdateCommissionPolicyActiveUseCase,
) {
    @GetMapping
    fun getCommissionPolicies(): ApiResponse<CommissionPolicyListResponse> =
        ApiResponse.success(getAdminCommissionPolicyListUseCase.execute())

    @PostMapping
    fun createCommissionPolicy(
        @RequestBody @Valid request: CreateCommissionPolicyRequest,
    ): ApiResponse<CommissionPolicyResponse> = ApiResponse.success(createCommissionPolicyUseCase.execute(request))

    @PatchMapping("/{id}")
    fun updateActive(
        @PathVariable id: String,
        @RequestBody @Valid request: UpdateCommissionPolicyActiveRequest,
    ): ApiResponse<Unit> {
        updateCommissionPolicyActiveUseCase.execute(id, request.active)
        return ApiResponse.success(Unit)
    }
}
