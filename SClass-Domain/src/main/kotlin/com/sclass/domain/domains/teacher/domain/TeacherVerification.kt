package com.sclass.domain.domains.teacher.domain

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.time.LocalDateTime

@Embeddable
data class TeacherVerification(
    @Column
    val submittedAt: LocalDateTime? = null,

    @Column
    val approvedAt: LocalDateTime? = null,

    @Column(length = 26)
    val approvedBy: String? = null,

    @Column(length = 500)
    val rejectionReason: String? = null,
)
