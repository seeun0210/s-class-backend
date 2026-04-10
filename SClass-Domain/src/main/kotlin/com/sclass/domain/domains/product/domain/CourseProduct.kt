package com.sclass.domain.domains.product.domain

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue("COURSE")
class CourseProduct(
    name: String,
    priceWon: Int,

    @Column(name = "total_sessions", nullable = false)
    val totalSessions: Int,

    @Column(name = "teacher_payout_per_session_won", nullable = false)
    val teacherPayoutPerSessionWon: Int,
) : Product(name = name, priceWon = priceWon)
