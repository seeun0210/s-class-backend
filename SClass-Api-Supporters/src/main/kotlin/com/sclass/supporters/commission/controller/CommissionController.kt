package com.sclass.supporters.commission.controller

import com.sclass.common.annotation.CurrentUserId
import com.sclass.common.annotation.CurrentUserRole
import com.sclass.common.dto.ApiResponse
import com.sclass.domain.domains.user.domain.Role
import com.sclass.supporters.commission.dto.CommissionListResponse
import com.sclass.supporters.commission.dto.CommissionResponse
import com.sclass.supporters.commission.dto.CommissionTopicListResponse
import com.sclass.supporters.commission.dto.CommissionTopicResponse
import com.sclass.supporters.commission.dto.CreateCommissionRequest
import com.sclass.supporters.commission.dto.ProposeTopicsRequest
import com.sclass.supporters.commission.dto.SelectTopicRequest
import com.sclass.supporters.commission.usecase.CreateCommissionUseCase
import com.sclass.supporters.commission.usecase.GetCommissionDetailUseCase
import com.sclass.supporters.commission.usecase.GetCommissionListUseCase
import com.sclass.supporters.commission.usecase.GetCommissionTopicsUseCase
import com.sclass.supporters.commission.usecase.ProposeTopicsUseCase
import com.sclass.supporters.commission.usecase.SelectTopicUseCase
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/commissions")
class CommissionController(
    private val createCommissionUseCase: CreateCommissionUseCase,
    private val getCommissionDetailUseCase: GetCommissionDetailUseCase,
    private val getCommissionListUseCase: GetCommissionListUseCase,
    private val selectTopicUseCase: SelectTopicUseCase,
    private val proposeTopicsUseCase: ProposeTopicsUseCase,
    private val getCommissionTopicsUseCase: GetCommissionTopicsUseCase,
) {
    @PostMapping
    fun create(
        @CurrentUserId userId: String,
        @Valid @RequestBody request: CreateCommissionRequest,
    ): ApiResponse<CommissionResponse> = ApiResponse.success(createCommissionUseCase.execute(userId, request))

    @GetMapping
    fun list(
        @CurrentUserId userId: String,
        @CurrentUserRole role: String,
    ): ApiResponse<CommissionListResponse> = ApiResponse.success(getCommissionListUseCase.execute(userId, Role.valueOf(role)))

    @GetMapping("/{commissionId}")
    fun detail(
        @CurrentUserId userId: String,
        @PathVariable commissionId: Long,
    ): ApiResponse<CommissionResponse> = ApiResponse.success(getCommissionDetailUseCase.execute(userId, commissionId))

    @PatchMapping("/{commissionId}/topics/{topicId}")
    fun selectTopic(
        @CurrentUserId userId: String,
        @PathVariable commissionId: Long,
        @PathVariable topicId: Long,
        @Valid @RequestBody request: SelectTopicRequest,
    ): ApiResponse<CommissionTopicResponse> = ApiResponse.success(selectTopicUseCase.execute(userId, commissionId, topicId, request))

    @GetMapping("/{commissionId}/topics")
    fun getTopics(
        @CurrentUserId userId: String,
        @PathVariable commissionId: Long,
    ): ApiResponse<CommissionTopicListResponse> = ApiResponse.success(getCommissionTopicsUseCase.execute(userId, commissionId))

    @PostMapping("/{commissionId}/topics")
    fun proposeTopics(
        @CurrentUserId userId: String,
        @PathVariable commissionId: Long,
        @Valid @RequestBody request: ProposeTopicsRequest,
    ): ApiResponse<CommissionTopicListResponse> = ApiResponse.success(proposeTopicsUseCase.execute(userId, commissionId, request))
}
