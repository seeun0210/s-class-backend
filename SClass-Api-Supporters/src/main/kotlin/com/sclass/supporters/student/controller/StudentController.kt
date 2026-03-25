package com.sclass.supporters.student.controller

import com.sclass.common.annotation.CurrentUserId
import com.sclass.common.dto.ApiResponse
import com.sclass.supporters.student.dto.StudentDocumentResponse
import com.sclass.supporters.student.dto.StudentProfileResponse
import com.sclass.supporters.student.dto.UpdateStudentProfileRequest
import com.sclass.supporters.student.dto.UploadStudentDocumentRequest
import com.sclass.supporters.student.usecase.GetMyStudentProfileUseCase
import com.sclass.supporters.student.usecase.UpdateStudentProfileUseCase
import com.sclass.supporters.student.usecase.UploadStudentDocumentUseCase
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/students/me")
class StudentController(
    private val getMyStudentProfileUseCase: GetMyStudentProfileUseCase,
    private val updateStudentProfileUseCase: UpdateStudentProfileUseCase,
    private val uploadStudentDocumentUseCase: UploadStudentDocumentUseCase,
) {
    @GetMapping
    fun getMyProfile(
        @CurrentUserId userId: String,
    ): ApiResponse<StudentProfileResponse> = ApiResponse.success(getMyStudentProfileUseCase.execute(userId))

    @PutMapping("/profile")
    fun updateProfile(
        @CurrentUserId userId: String,
        @Valid @RequestBody request: UpdateStudentProfileRequest,
    ): ApiResponse<StudentProfileResponse> = ApiResponse.success(updateStudentProfileUseCase.execute(userId, request))

    @PostMapping("/documents")
    fun uploadDocument(
        @CurrentUserId userId: String,
        @Valid @RequestBody request: UploadStudentDocumentRequest,
    ): ApiResponse<StudentDocumentResponse> = ApiResponse.success(uploadStudentDocumentUseCase.execute(userId, request))
}
