package com.sclass.backoffice.commission.controller

import com.sclass.backoffice.commission.dto.CommissionDetailResponse
import com.sclass.backoffice.commission.dto.CommissionPageResponse
import com.sclass.backoffice.commission.usecase.GetCommissionDetailUseCase
import com.sclass.backoffice.commission.usecase.GetCommissionListUseCase
import com.sclass.common.dto.ApiResponse
import com.sclass.domain.domains.commission.domain.CommissionStatus
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/commissions")
class CommissionController(
    private val getCommissionListUseCase: GetCommissionListUseCase,
    private val getCommissionDetailUseCase: GetCommissionDetailUseCase,
) {
    @GetMapping
    fun getCommissionList(
        @RequestParam(required = false) studentUserId: String?,
        @RequestParam(required = false) teacherUserId: String?,
        @RequestParam(required = false) status: CommissionStatus?,
        @PageableDefault(size = 20, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable,
    ): ApiResponse<CommissionPageResponse> =
        ApiResponse.success(getCommissionListUseCase.execute(studentUserId, teacherUserId, status, pageable))

    @GetMapping("/{commissionId}")
    fun getCommissionDetail(
        @PathVariable commissionId: Long,
    ): ApiResponse<CommissionDetailResponse> = ApiResponse.success(getCommissionDetailUseCase.execute(commissionId))
}
