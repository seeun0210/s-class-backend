package com.sclass.backoffice.membershipproduct.controller

import com.sclass.backoffice.membershipproduct.dto.CreateMembershipProductRequest
import com.sclass.backoffice.membershipproduct.dto.MembershipProductPageResponse
import com.sclass.backoffice.membershipproduct.dto.MembershipProductResponse
import com.sclass.backoffice.membershipproduct.dto.UpdateMembershipProductRequest
import com.sclass.backoffice.membershipproduct.usecase.CreateMembershipProductUseCase
import com.sclass.backoffice.membershipproduct.usecase.GetMembershipProductDetailUseCase
import com.sclass.backoffice.membershipproduct.usecase.GetMembershipProductListUseCase
import com.sclass.backoffice.membershipproduct.usecase.UpdateMembershipProductUseCase
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
@RequestMapping("/api/v1/membership-products")
class MembershipProductController(
    private val createMembershipProductUseCase: CreateMembershipProductUseCase,
    private val updateMembershipProductUseCase: UpdateMembershipProductUseCase,
    private val getMembershipProductListUseCase: GetMembershipProductListUseCase,
    private val getMembershipProductDetailUseCase: GetMembershipProductDetailUseCase,
) {
    @PostMapping
    fun create(
        @Valid @RequestBody request: CreateMembershipProductRequest,
    ): ApiResponse<MembershipProductResponse> = ApiResponse.success(createMembershipProductUseCase.execute(request))

    @GetMapping
    fun getList(
        @PageableDefault(size = 20, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable,
    ): ApiResponse<MembershipProductPageResponse> = ApiResponse.success(getMembershipProductListUseCase.execute(pageable))

    @GetMapping("/{productId}")
    fun getDetail(
        @PathVariable productId: String,
    ): ApiResponse<MembershipProductResponse> = ApiResponse.success(getMembershipProductDetailUseCase.execute(productId))

    @PutMapping("/{productId}")
    fun update(
        @PathVariable productId: String,
        @Valid @RequestBody request: UpdateMembershipProductRequest,
    ): ApiResponse<MembershipProductResponse> = ApiResponse.success(updateMembershipProductUseCase.execute(productId, request))
}
