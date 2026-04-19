package com.sclass.supporters.membership.controller

import com.sclass.common.annotation.CurrentUserId
import com.sclass.common.dto.ApiResponse
import com.sclass.common.dto.ApiResponse.Companion.success
import com.sclass.supporters.membership.dto.PrepareMembershipPurchaseRequest
import com.sclass.supporters.membership.dto.PrepareMembershipPurchaseResponse
import com.sclass.supporters.membership.usecase.PrepareMembershipPurchaseUseCase
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/memberships")
class MembershipPurchaseController(
    private val prepareMembershipPurchaseUseCase: PrepareMembershipPurchaseUseCase,
) {
    @PostMapping("/purchase")
    fun prepare(
        @CurrentUserId userId: String,
        @RequestBody @Valid request: PrepareMembershipPurchaseRequest,
    ): ApiResponse<PrepareMembershipPurchaseResponse> =
        success(
            prepareMembershipPurchaseUseCase.execute(userId, request.membershipProductId, request.pgType),
        )
}
