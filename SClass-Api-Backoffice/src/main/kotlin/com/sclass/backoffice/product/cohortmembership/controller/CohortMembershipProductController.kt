package com.sclass.backoffice.product.cohortmembership.controller

import com.sclass.backoffice.product.cohortmembership.dto.CohortMembershipPageResponse
import com.sclass.backoffice.product.cohortmembership.dto.CohortMembershipResponse
import com.sclass.backoffice.product.cohortmembership.dto.CreateCohortMembershipRequest
import com.sclass.backoffice.product.cohortmembership.dto.UpdateCohortMembershipRequest
import com.sclass.backoffice.product.cohortmembership.usecase.CreateCohortMembershipUseCase
import com.sclass.backoffice.product.cohortmembership.usecase.GetCohortMembershipDetailUseCase
import com.sclass.backoffice.product.cohortmembership.usecase.GetCohortMembershipListUseCase
import com.sclass.backoffice.product.cohortmembership.usecase.UpdateCohortMembershipUseCase
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
@RequestMapping("/api/v1/cohort-memberships")
class CohortMembershipProductController(
    private val createCohortMembershipUseCase: CreateCohortMembershipUseCase,
    private val updateCohortMembershipUseCase: UpdateCohortMembershipUseCase,
    private val getCohortMembershipListUseCase: GetCohortMembershipListUseCase,
    private val getCohortMembershipDetailUseCase: GetCohortMembershipDetailUseCase,
) {
    @PostMapping
    fun create(
        @Valid @RequestBody request: CreateCohortMembershipRequest,
    ): ApiResponse<CohortMembershipResponse> = ApiResponse.success(createCohortMembershipUseCase.execute(request))

    @GetMapping
    fun getList(
        @PageableDefault(size = 20, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable,
    ): ApiResponse<CohortMembershipPageResponse> = ApiResponse.success(getCohortMembershipListUseCase.execute(pageable))

    @GetMapping("/{productId}")
    fun getDetail(
        @PathVariable productId: String,
    ): ApiResponse<CohortMembershipResponse> = ApiResponse.success(getCohortMembershipDetailUseCase.execute(productId))

    @PutMapping("/{productId}")
    fun update(
        @PathVariable productId: String,
        @Valid @RequestBody request: UpdateCohortMembershipRequest,
    ): ApiResponse<CohortMembershipResponse> = ApiResponse.success(updateCohortMembershipUseCase.execute(productId, request))
}
