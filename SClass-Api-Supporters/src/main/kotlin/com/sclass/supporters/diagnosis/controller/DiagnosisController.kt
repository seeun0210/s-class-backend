package com.sclass.supporters.diagnosis.controller

import com.sclass.common.dto.ApiResponse
import com.sclass.supporters.diagnosis.dto.DiagnosisResultResponse
import com.sclass.supporters.diagnosis.dto.SendDiagnosisVerificationRequest
import com.sclass.supporters.diagnosis.usecase.GetDiagnosisResultUseCase
import com.sclass.supporters.diagnosis.usecase.SendDiagnosisPhoneVerificationUseCase
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
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

    @PostMapping("/{id}")
    fun getDiagnosisResult(
        @PathVariable id: String,
        @Valid @RequestBody request: GetDiagnosisResultRequest,
    ): ApiResponse<DiagnosisResultResponse> = ApiResponse.success(getDiagnosisResultUseCase.execute(id, request.phone, request.code))
}

data class GetDiagnosisResultRequest(
    @field:NotBlank val phone: String,
    @field:NotBlank val code: String,
)
