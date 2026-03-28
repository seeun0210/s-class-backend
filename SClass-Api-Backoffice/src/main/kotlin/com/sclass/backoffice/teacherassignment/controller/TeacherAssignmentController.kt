package com.sclass.backoffice.teacherassignment.controller

import com.sclass.backoffice.teacherassignment.dto.AssignTeacherRequest
import com.sclass.backoffice.teacherassignment.dto.TeacherAssignmentPageResponse
import com.sclass.backoffice.teacherassignment.dto.UnassignTeacherRequest
import com.sclass.backoffice.teacherassignment.usecase.AssignTeacherUseCase
import com.sclass.backoffice.teacherassignment.usecase.SearchTeacherAssignmentsUseCase
import com.sclass.backoffice.teacherassignment.usecase.UnassignTeacherUseCase
import com.sclass.common.annotation.CurrentUserId
import com.sclass.common.dto.ApiResponse
import com.sclass.domain.domains.teacherassignment.dto.TeacherAssignmentSearchCondition
import com.sclass.domain.domains.user.domain.Platform
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/teacher-assignments")
class TeacherAssignmentController(
    private val assignTeacherUseCase: AssignTeacherUseCase,
    private val unassignTeacherUseCase: UnassignTeacherUseCase,
    private val searchTeacherAssignmentsUseCase: SearchTeacherAssignmentsUseCase,
) {
    @PostMapping
    fun assignTeacher(
        @Valid @RequestBody request: AssignTeacherRequest,
        @CurrentUserId currentUserId: String,
    ): ApiResponse<Nothing> {
        assignTeacherUseCase.execute(request, currentUserId)
        return ApiResponse.success()
    }

    @DeleteMapping
    fun unassignTeacher(
        @Valid @RequestBody request: UnassignTeacherRequest,
    ): ApiResponse<Nothing> {
        unassignTeacherUseCase.execute(request)
        return ApiResponse.success()
    }

    @GetMapping
    fun searchTeacherAssignments(
        @RequestParam(required = false) platform: Platform?,
        @RequestParam(required = false) organizationId: Long?,
        @RequestParam(required = false) teacherName: String?,
        @RequestParam(required = false) studentName: String?,
        @PageableDefault(
            size = 20,
            sort = ["assignedAt"],
            direction =
                Sort.Direction.DESC,
        ) pageable: Pageable,
    ): ApiResponse<TeacherAssignmentPageResponse> =
        ApiResponse.success(
            searchTeacherAssignmentsUseCase.execute(
                TeacherAssignmentSearchCondition(
                    platform = platform,
                    organizationId = organizationId,
                    teacherName = teacherName,
                    studentName = studentName,
                ),
                pageable,
            ),
        )
}
