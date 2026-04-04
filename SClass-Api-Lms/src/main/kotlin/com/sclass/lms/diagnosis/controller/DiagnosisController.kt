package com.sclass.lms.diagnosis.controller

import com.sclass.common.dto.ApiResponse
import com.sclass.lms.diagnosis.dto.DiagnosisResultResponse
import com.sclass.lms.diagnosis.dto.SendDiagnosisVerificationRequest
import com.sclass.lms.diagnosis.usecase.GetDiagnosisResultUseCase
import com.sclass.lms.diagnosis.usecase.SendDiagnosisPhoneVerificationUseCase
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/diagnosis")
class DiagnosisController(
    private val sendDiagnosisPhoneVerificationUseCase: SendDiagnosisPhoneVerificationUseCase,
    private val getDiagnosisResultUseCase: GetDiagnosisResultUseCase,
) {
    @PostMapping("/{id}/verify-phone")
    fun sendVerificationCode(
        @PathVariable id: String,
        @Valid @RequestBody request: SendDiagnosisVerificationRequest,
    ): ApiResponse<Unit> {
        sendDiagnosisPhoneVerificationUseCase.execute(id, request.phone)
        return ApiResponse.success(Unit)
    }

    @GetMapping("/{id}")
    fun getDiagnosisResult(
        @PathVariable id: String,
        @RequestParam phone: String,
        @RequestParam code: String,
    ): ApiResponse<DiagnosisResultResponse> = ApiResponse.success(getDiagnosisResultUseCase.execute(id, phone, code))
}
