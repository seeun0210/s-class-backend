package com.sclass.backoffice.product.rollingmembership.controller

import com.sclass.backoffice.product.rollingmembership.dto.CreateRollingMembershipRequest
import com.sclass.backoffice.product.rollingmembership.dto.RollingMembershipPageResponse
import com.sclass.backoffice.product.rollingmembership.dto.RollingMembershipResponse
import com.sclass.backoffice.product.rollingmembership.dto.UpdateRollingMembershipRequest
import com.sclass.backoffice.product.rollingmembership.usecase.CreateRollingMembershipUseCase
import com.sclass.backoffice.product.rollingmembership.usecase.GetRollingMembershipDetailUseCase
import com.sclass.backoffice.product.rollingmembership.usecase.GetRollingMembershipListUseCase
import com.sclass.backoffice.product.rollingmembership.usecase.UpdateRollingMembershipUseCase
import com.sclass.common.dto.ApiResponse
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/rolling-memberships")
class RollingMembershipProductController(
    private val createRollingMembershipUseCase: CreateRollingMembershipUseCase,
    private val updateRollingMembershipUseCase: UpdateRollingMembershipUseCase,
    private val getRollingMembershipListUseCase: GetRollingMembershipListUseCase,
    private val getRollingMembershipDetailUseCase: GetRollingMembershipDetailUseCase,
) {
    @PostMapping
    fun create(
        @Valid @RequestBody request: CreateRollingMembershipRequest,
    ): ApiResponse<RollingMembershipResponse> = ApiResponse.success(createRollingMembershipUseCase.execute(request))

    @GetMapping
    fun getList(
        @PageableDefault(size = 20, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable,
    ): ApiResponse<RollingMembershipPageResponse> = ApiResponse.success(getRollingMembershipListUseCase.execute(pageable))

    @GetMapping("/{productId}")
    fun getDetail(
        @PathVariable productId: String,
    ): ApiResponse<RollingMembershipResponse> = ApiResponse.success(getRollingMembershipDetailUseCase.execute(productId))

    @PutMapping("/{productId}")
    fun update(
        @PathVariable productId: String,
        @Valid @RequestBody request: UpdateRollingMembershipRequest,
    ): ApiResponse<RollingMembershipResponse> = ApiResponse.success(updateRollingMembershipUseCase.execute(productId, request))
}
