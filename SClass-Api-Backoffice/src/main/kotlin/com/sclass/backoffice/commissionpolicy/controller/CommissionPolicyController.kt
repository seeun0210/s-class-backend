package com.sclass.backoffice.commissionpolicy.controller

import com.sclass.backoffice.commissionpolicy.dto.CommissionPolicyResponse
import com.sclass.backoffice.commissionpolicy.dto.CreateCommissionPolicyRequest
import com.sclass.backoffice.commissionpolicy.usecase.CreateCommissionPolicyUseCase
import com.sclass.common.dto.ApiResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/commission-policies")
class CommissionPolicyController(
    private val createCommissionPolicyUseCase: CreateCommissionPolicyUseCase,
) {
    @PostMapping
    fun createCommissionPolicy(
        @RequestBody @Valid request: CreateCommissionPolicyRequest,
    ): ApiResponse<CommissionPolicyResponse> = ApiResponse.success(createCommissionPolicyUseCase.execute(request))
}
