package com.sclass.supporters.commission.controller

import com.sclass.common.dto.ApiResponse
import com.sclass.supporters.commission.dto.CommissionPolicyResponse
import com.sclass.supporters.commission.usecase.GetCommissionPolicyUseCase
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/commission-policy")
class CommissionPolicyController(
    private val getCommissionPolicyUseCase: GetCommissionPolicyUseCase,
) {
    @GetMapping
    fun getActive(): ApiResponse<CommissionPolicyResponse> = ApiResponse.success(getCommissionPolicyUseCase.execute())
}
