package com.sclass.domain.domains.product.domain

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue("COURSE")
class CourseProduct(
    name: String,
    priceWon: Int,

    @Column(name = "total_lessons", nullable = false)
    val totalLessons: Int,

    @Column(name = "teacher_payout_per_lesson_won", nullable = false)
    val teacherPayoutPerLessonWon: Int,
) : Product(name = name, priceWon = priceWon)
