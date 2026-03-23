package com.sclass.supporters.teacher.controller

import com.sclass.common.annotation.CurrentUserId
import com.sclass.common.dto.ApiResponse
import com.sclass.supporters.teacher.dto.TeacherDocumentResponse
import com.sclass.supporters.teacher.dto.TeacherProfileResponse
import com.sclass.supporters.teacher.dto.UpdateTeacherProfileRequest
import com.sclass.supporters.teacher.dto.UploadTeacherDocumentRequest
import com.sclass.supporters.teacher.usecase.GetMyTeacherProfileUseCase
import com.sclass.supporters.teacher.usecase.SubmitTeacherVerificationUseCase
import com.sclass.supporters.teacher.usecase.UpdateTeacherProfileUseCase
import com.sclass.supporters.teacher.usecase.UploadTeacherDocumentUseCase
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/teachers/me")
class TeacherController(
    private val getMyTeacherProfileUseCase: GetMyTeacherProfileUseCase,
    private val updateTeacherProfileUseCase: UpdateTeacherProfileUseCase,
    private val uploadTeacherDocumentUseCase: UploadTeacherDocumentUseCase,
    private val submitTeacherVerificationUseCase: SubmitTeacherVerificationUseCase,
) {
    @GetMapping
    fun getMyProfile(
        @CurrentUserId userId: String,
    ): ApiResponse<TeacherProfileResponse> = ApiResponse.success(getMyTeacherProfileUseCase.execute(userId))

    @PutMapping("/profile")
    fun updateProfile(
        @CurrentUserId userId: String,
        @Valid @RequestBody request: UpdateTeacherProfileRequest,
    ): ApiResponse<TeacherProfileResponse> = ApiResponse.success(updateTeacherProfileUseCase.execute(userId, request))

    @PostMapping("/documents")
    fun uploadDocument(
        @CurrentUserId userId: String,
        @Valid @RequestBody request: UploadTeacherDocumentRequest,
    ): ApiResponse<TeacherDocumentResponse> = ApiResponse.success(uploadTeacherDocumentUseCase.execute(userId, request))

    @PostMapping("/submit")
    fun submitVerification(
        @CurrentUserId userId: String,
    ): ApiResponse<TeacherProfileResponse> = ApiResponse.success(submitTeacherVerificationUseCase.execute(userId))
}
