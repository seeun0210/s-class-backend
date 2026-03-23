package com.sclass.backoffice.teacher.controller

import com.sclass.backoffice.teacher.dto.RejectTeacherRequest
import com.sclass.backoffice.teacher.dto.TeacherPageResponse
import com.sclass.backoffice.teacher.usecase.ApproveTeacherUseCase
import com.sclass.backoffice.teacher.usecase.GetTeachersUseCase
import com.sclass.backoffice.teacher.usecase.RejectTeacherUseCase
import com.sclass.common.annotation.CurrentUserId
import com.sclass.common.dto.ApiResponse
import com.sclass.domain.domains.teacher.domain.TeacherVerificationStatus
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/backoffice/teachers")
class TeacherManagementController(
    private val getTeachersUseCase: GetTeachersUseCase,
    private val approveTeacherUseCase: ApproveTeacherUseCase,
    private val rejectTeacherUseCase: RejectTeacherUseCase,
) {
    @GetMapping
    fun getTeachers(
        @RequestParam status: TeacherVerificationStatus,
        @PageableDefault(size = 20, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable,
    ): ApiResponse<TeacherPageResponse> = ApiResponse.success(getTeachersUseCase.execute(status, pageable))

    @PostMapping("/{teacherId}/approve")
    fun approve(
        @PathVariable teacherId: String,
        @CurrentUserId userId: String,
    ): ApiResponse<Nothing> {
        approveTeacherUseCase.execute(teacherId, userId)
        return ApiResponse.success()
    }

    @PostMapping("/{teacherId}/reject")
    fun reject(
        @PathVariable teacherId: String,
        @Valid @RequestBody request: RejectTeacherRequest,
    ): ApiResponse<Nothing> {
        rejectTeacherUseCase.execute(teacherId, request.reason!!)
        return ApiResponse.success()
    }
}
