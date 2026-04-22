package com.sclass.domain.domains.product.domain

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue("COURSE")
class CourseProduct(
    name: String,
    priceWon: Int,
    description: String? = null,
    thumbnailFileId: String? = null,

    @Column(name = "total_lessons", nullable = true)
    val totalLessons: Int,

    @Column(columnDefinition = "TEXT")
    var curriculum: String? = null,

    @Column(name = "requires_matching", nullable = true)
    var requiresMatching: Boolean = false,
) : Product(
        name = name,
        priceWon = priceWon,
        description = description,
        thumbnailFileId = thumbnailFileId,
    ) {
    fun updateCurriculum(newCurriculum: String?) {
        newCurriculum?.let { curriculum = it }
    }
}
