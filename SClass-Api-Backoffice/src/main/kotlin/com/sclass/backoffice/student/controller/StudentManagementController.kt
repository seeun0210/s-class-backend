package com.sclass.backoffice.student.controller

import com.sclass.backoffice.student.dto.BulkCreateStudentsRequest
import com.sclass.backoffice.student.dto.BulkCreateStudentsResponse
import com.sclass.backoffice.student.dto.CreateStudentRequest
import com.sclass.backoffice.student.dto.CreateStudentResponse
import com.sclass.backoffice.student.dto.StudentDetailResponse
import com.sclass.backoffice.student.dto.StudentPageResponse
import com.sclass.backoffice.student.dto.UpdateStudentStateRequest
import com.sclass.backoffice.student.usecase.BulkCreateStudentsUseCase
import com.sclass.backoffice.student.usecase.CreateStudentUseCase
import com.sclass.backoffice.student.usecase.GetStudentDetailUseCase
import com.sclass.backoffice.student.usecase.GetStudentsUseCase
import com.sclass.backoffice.student.usecase.UpdateStudentStateUseCase
import com.sclass.common.dto.ApiResponse
import com.sclass.domain.domains.student.dto.StudentSearchCondition
import com.sclass.domain.domains.user.domain.Grade
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.UserRoleState
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1/students")
class StudentManagementController(
    private val createStudentUseCase: CreateStudentUseCase,
    private val bulkCreateStudentsUseCase: BulkCreateStudentsUseCase,
    private val getStudentsUseCase: GetStudentsUseCase,
    private val getStudentDetailUseCase: GetStudentDetailUseCase,
    private val updateStudentStateUseCase: UpdateStudentStateUseCase,
) {
    @PostMapping
    fun createStudent(
        @Valid @RequestBody request: CreateStudentRequest,
    ): ApiResponse<CreateStudentResponse> = ApiResponse.success(createStudentUseCase.execute(request))

    @PostMapping("/bulk")
    fun bulkCreateStudents(
        @Valid @RequestBody request: BulkCreateStudentsRequest,
    ): ApiResponse<BulkCreateStudentsResponse> = ApiResponse.success(bulkCreateStudentsUseCase.execute(request))

    @GetMapping
    fun getStudents(
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) email: String?,
        @RequestParam(required = false) grade: Grade?,
        @RequestParam(required = false) school: String?,
        @RequestParam(required = false) state: UserRoleState?,
        @RequestParam(required = false) platform: Platform?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) createdAtFrom: LocalDateTime?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) createdAtTo: LocalDateTime?,
        @PageableDefault(size = 20, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable,
    ): ApiResponse<StudentPageResponse> =
        ApiResponse.success(
            getStudentsUseCase.execute(
                StudentSearchCondition(
                    name = name,
                    email = email,
                    grade = grade,
                    school = school,
                    state = state,
                    platform = platform,
                    createdAtFrom = createdAtFrom,
                    createdAtTo = createdAtTo,
                ),
                pageable,
            ),
        )

    @GetMapping("/{userId}")
    fun getStudentDetail(
        @PathVariable userId: String,
    ): ApiResponse<StudentDetailResponse> = ApiResponse.success(getStudentDetailUseCase.execute(userId))

    @PatchMapping("/{userId}/state")
    fun updateState(
        @PathVariable userId: String,
        @Valid @RequestBody request: UpdateStudentStateRequest,
    ): ApiResponse<Nothing> {
        updateStudentStateUseCase.execute(userId, request)
        return ApiResponse.success()
    }
}
