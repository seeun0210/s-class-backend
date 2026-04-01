package com.sclass.domain.domains.teacher.domain

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.time.LocalDate

@Embeddable
data class TeacherContract(
    @Column
    val policeCheckAt: LocalDate? = null,

    @Column
    val contractStartDate: LocalDate? = null,

    @Column
    val contractEndDate: LocalDate? = null,
)
