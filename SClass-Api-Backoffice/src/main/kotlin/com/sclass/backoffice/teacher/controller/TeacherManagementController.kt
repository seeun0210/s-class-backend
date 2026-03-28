package com.sclass.backoffice.teacher.controller

import com.sclass.backoffice.teacher.dto.BulkCreateTeachersRequest
import com.sclass.backoffice.teacher.dto.BulkCreateTeachersResponse
import com.sclass.backoffice.teacher.dto.CreateTeacherRequest
import com.sclass.backoffice.teacher.dto.CreateTeacherResponse
import com.sclass.backoffice.teacher.dto.TeacherDetailResponse
import com.sclass.backoffice.teacher.dto.TeacherPageResponse
import com.sclass.backoffice.teacher.usecase.BulkCreateTeachersUseCase
import com.sclass.backoffice.teacher.usecase.CreateTeacherUseCase
import com.sclass.backoffice.teacher.usecase.GetTeacherDetailUseCase
import com.sclass.backoffice.teacher.usecase.GetTeachersUseCase
import com.sclass.backoffice.teacher.usecase.UpdateTeacherContractUseCase
import com.sclass.backoffice.teacher.usecase.UpdateTeacherEducationUseCase
import com.sclass.backoffice.teacher.usecase.UpdateTeacherPersonalInfoUseCase
import com.sclass.backoffice.teacher.usecase.UpdateTeacherProfileUseCase
import com.sclass.common.dto.ApiResponse
import com.sclass.domain.domains.teacher.domain.MajorCategory
import com.sclass.domain.domains.teacher.domain.TeacherContract
import com.sclass.domain.domains.teacher.domain.TeacherEducation
import com.sclass.domain.domains.teacher.domain.TeacherPersonalInfo
import com.sclass.domain.domains.teacher.domain.TeacherProfile
import com.sclass.domain.domains.teacher.dto.TeacherSearchCondition
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
@RequestMapping("/api/v1/teachers")
class TeacherManagementController(
    private val getTeachersUseCase: GetTeachersUseCase,
    private val getTeacherDetailUseCase: GetTeacherDetailUseCase,
    private val createTeacherUseCase: CreateTeacherUseCase,
    private val bulkCreateTeachersUseCase: BulkCreateTeachersUseCase,
    private val updateTeacherContractUseCase: UpdateTeacherContractUseCase,
    private val updateTeacherEducationUseCase: UpdateTeacherEducationUseCase,
    private val updateTeacherPersonalInfoUseCase: UpdateTeacherPersonalInfoUseCase,
    private val updateTeacherProfileUseCase: UpdateTeacherProfileUseCase,
) {
    @PostMapping
    fun createTeacher(
        @Valid @RequestBody request: CreateTeacherRequest,
    ): ApiResponse<CreateTeacherResponse> = ApiResponse.success(createTeacherUseCase.execute(request))

    @PostMapping("/bulk")
    fun bulkCreateTeachers(
        @Valid @RequestBody request: BulkCreateTeachersRequest,
    ): ApiResponse<BulkCreateTeachersResponse> = ApiResponse.success(bulkCreateTeachersUseCase.execute(request))

    @GetMapping
    fun getTeachers(
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) email: String?,
        @RequestParam(required = false) university: String?,
        @RequestParam(required = false) major: String?,
        @RequestParam(required = false) majorCategory: MajorCategory?,
        @RequestParam(required = false) state: UserRoleState?,
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
                    state = state,
                    platform = platform,
                    submittedAtFrom = submittedAtFrom,
                    submittedAtTo = submittedAtTo,
                    createdAtFrom = createdAtFrom,
                    createdAtTo = createdAtTo,
                ),
                pageable,
            ),
        )

    @GetMapping("/{userId}")
    fun getTeacherDetail(
        @PathVariable userId: String,
    ): ApiResponse<TeacherDetailResponse> = ApiResponse.success(getTeacherDetailUseCase.execute(userId))

    @PatchMapping("/{userId}/profile")
    fun updateTeacherProfile(
        @PathVariable userId: String,
        @RequestBody request: TeacherProfile,
    ): ApiResponse<Nothing> {
        updateTeacherProfileUseCase.execute(userId, request)
        return ApiResponse.success()
    }

    @PatchMapping("/{userId}/education")
    fun updateTeacherEducation(
        @PathVariable userId: String,
        @RequestBody request: TeacherEducation,
    ): ApiResponse<Nothing> {
        updateTeacherEducationUseCase.execute(userId, request)
        return ApiResponse.success()
    }

    @PatchMapping("/{userId}/personal-info")
    fun updateTeacherPersonalInfo(
        @PathVariable userId: String,
        @RequestBody request: TeacherPersonalInfo,
    ): ApiResponse<Nothing> {
        updateTeacherPersonalInfoUseCase.execute(userId, request)
        return ApiResponse.success()
    }

    @PatchMapping("/{userId}/contract")
    fun updateTeacherContract(
        @PathVariable userId: String,
        @RequestBody request: TeacherContract,
    ): ApiResponse<Nothing> {
        updateTeacherContractUseCase.execute(userId, request)
        return ApiResponse.success()
    }
}
