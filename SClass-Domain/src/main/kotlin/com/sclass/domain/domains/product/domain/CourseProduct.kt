package com.sclass.domain.domains.product.domain

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue("COURSE")
class CourseProduct(
    name: String,
    priceWon: Int,

    // Single Table Inheritance: 서브클래스 전용 컬럼은 DB NULL 허용 필수
    // (다른 dtype insert 시 NOT NULL이면 실패). Kotlin 타입은 non-null 유지하여 코드 안정성 확보
    @Column(name = "total_lessons", nullable = true)
    val totalLessons: Int,

    @Column(name = "teacher_payout_per_lesson_won", nullable = true)
    val teacherPayoutPerLessonWon: Int,
) : Product(name = name, priceWon = priceWon)
