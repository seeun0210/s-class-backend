package com.sclass.supporters.inquiry.controller

import com.sclass.common.annotation.CurrentUserId
import com.sclass.common.dto.ApiResponse
import com.sclass.domain.domains.inquiryplan.domain.InquiryPlanSourceType
import com.sclass.supporters.inquiry.dto.CreateInquiryPlanRequest
import com.sclass.supporters.inquiry.dto.InquiryPlanDetailResponse
import com.sclass.supporters.inquiry.dto.InquiryPlanResponse
import com.sclass.supporters.inquiry.dto.InquiryPlanStatusResponse
import com.sclass.supporters.inquiry.usecase.CreateInquiryPlanUseCase
import com.sclass.supporters.inquiry.usecase.GetInquiryPlanStatusUseCase
import com.sclass.supporters.inquiry.usecase.GetInquiryPlanUseCase
import com.sclass.supporters.inquiry.usecase.GetInquiryPlansUseCase
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/inquiry-plans")
class InquiryPlanController(
    private val createInquiryPlanUseCase: CreateInquiryPlanUseCase,
    private val getInquiryPlansUseCase: GetInquiryPlansUseCase,
    private val getInquiryPlanUseCase: GetInquiryPlanUseCase,
    private val getInquiryPlanStatusUseCase: GetInquiryPlanStatusUseCase,
) {
    @PostMapping
    fun create(
        @CurrentUserId userId: String,
        @Valid @RequestBody request: CreateInquiryPlanRequest,
    ): ApiResponse<InquiryPlanResponse> = ApiResponse.success(createInquiryPlanUseCase.execute(userId, request))

    @GetMapping
    fun list(
        @CurrentUserId userId: String,
        @RequestParam sourceType: InquiryPlanSourceType,
        @RequestParam sourceRefId: Long,
        @PageableDefault(size = 20) pageable: Pageable,
    ): ApiResponse<Page<InquiryPlanResponse>> =
        ApiResponse.success(getInquiryPlansUseCase.execute(userId, sourceType, sourceRefId, pageable))

    @GetMapping("/{id}")
    fun detail(
        @CurrentUserId userId: String,
        @PathVariable id: Long,
    ): ApiResponse<InquiryPlanDetailResponse> = ApiResponse.success(getInquiryPlanUseCase.execute(userId, id))

    @GetMapping("/{id}/status")
    fun status(
        @CurrentUserId userId: String,
        @PathVariable id: Long,
    ): ApiResponse<InquiryPlanStatusResponse> = ApiResponse.success(getInquiryPlanStatusUseCase.execute(userId, id))
}
