package com.sclass.lms.diagnosis.dto

import jakarta.validation.constraints.NotBlank

data class SendDiagnosisVerificationRequest(
    @field:NotBlank val phone: String,
)
