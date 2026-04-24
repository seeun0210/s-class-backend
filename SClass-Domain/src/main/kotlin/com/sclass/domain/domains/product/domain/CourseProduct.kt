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
    var totalLessons: Int,

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
    fun updateFulfillmentInfo(
        newCurriculum: String?,
        newTotalLessons: Int?,
        newRequiresMatching: Boolean?,
    ) {
        newCurriculum?.let { curriculum = it }
        newTotalLessons?.let { totalLessons = it }
        newRequiresMatching?.let { requiresMatching = it }
    }
}
