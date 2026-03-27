package com.sclass.backoffice.teacher.controller

import com.sclass.backoffice.teacher.dto.CreateTeacherRequest
import com.sclass.backoffice.teacher.dto.CreateTeacherResponse
import com.sclass.backoffice.teacher.dto.TeacherPageResponse
import com.sclass.backoffice.teacher.dto.UpdateVerificationStatusRequest
import com.sclass.backoffice.teacher.usecase.CreateTeacherUseCase
import com.sclass.backoffice.teacher.usecase.GetTeachersUseCase
import com.sclass.backoffice.teacher.usecase.UpdateTeacherVerificationUseCase
import com.sclass.common.annotation.CurrentUserId
import com.sclass.common.dto.ApiResponse
import com.sclass.domain.domains.teacher.domain.MajorCategory
import com.sclass.domain.domains.teacher.domain.TeacherVerificationStatus
import com.sclass.domain.domains.teacher.dto.TeacherSearchCondition
import com.sclass.domain.domains.user.domain.Platform
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
@RequestMapping("/api/v1/teachers")
class TeacherManagementController(
    private val getTeachersUseCase: GetTeachersUseCase,
    private val updateTeacherVerificationUseCase: UpdateTeacherVerificationUseCase,
    private val createTeacherUseCase: CreateTeacherUseCase,
) {
    @PostMapping
    fun createTeacher(
        @Valid @RequestBody request: CreateTeacherRequest,
    ): ApiResponse<CreateTeacherResponse> = ApiResponse.success(createTeacherUseCase.execute(request))

    @GetMapping
    fun getTeachers(
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) email: String?,
        @RequestParam(required = false) university: String?,
        @RequestParam(required = false) major: String?,
        @RequestParam(required = false) majorCategory: MajorCategory?,
        @RequestParam(required = false) verificationStatus: TeacherVerificationStatus?,
        @RequestParam(required = false) platform: Platform?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) submittedAtFrom: LocalDateTime?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) submittedAtTo: LocalDateTime?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) createdAtFrom: LocalDateTime?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) createdAtTo: LocalDateTime?,
        @PageableDefault(size = 20, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable,
    ): ApiResponse<TeacherPageResponse> =
        ApiResponse.success(
            getTeachersUseCase.execute(
                TeacherSearchCondition(
                    name = name,
                    email = email,
                    university = university,
                    major = major,
                    majorCategory = majorCategory,
                    verificationStatus = verificationStatus,
                    platform = platform,
                    submittedAtFrom = submittedAtFrom,
                    submittedAtTo = submittedAtTo,
                    createdAtFrom = createdAtFrom,
                    createdAtTo = createdAtTo,
                ),
                pageable,
            ),
        )

    @PatchMapping("/{teacherId}/verification-status")
    fun updateVerificationStatus(
        @PathVariable teacherId: String,
        @RequestBody request: UpdateVerificationStatusRequest,
        @CurrentUserId userId: String,
    ): ApiResponse<Nothing> {
        updateTeacherVerificationUseCase.execute(teacherId, request, userId)
        return ApiResponse.success()
    }
}
