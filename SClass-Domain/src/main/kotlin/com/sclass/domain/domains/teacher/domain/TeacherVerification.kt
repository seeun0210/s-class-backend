package com.sclass.domain.domains.teacher.domain

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import java.time.LocalDateTime

@Embeddable
data class TeacherVerification(
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val verificationStatus: TeacherVerificationStatus = TeacherVerificationStatus.DRAFT,

    @Column
    val submittedAt: LocalDateTime? = null,

    @Column
    val approvedAt: LocalDateTime? = null,

    @Column(length = 26)
    val approvedBy: String? = null,

    @Column(length = 500)
    val rejectionReason: String? = null,
)
