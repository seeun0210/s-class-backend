package com.sclass.domain.domains.teacher.domain

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.time.LocalDate
import java.time.LocalDateTime

@Embeddable
data class TeacherContract(
    @Column
    val policeCheckAt: LocalDateTime? = null,

    @Column
    val contractStartDate: LocalDate? = null,

    @Column
    val contractEndDate: LocalDate? = null,
)
