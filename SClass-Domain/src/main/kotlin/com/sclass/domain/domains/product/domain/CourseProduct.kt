package com.sclass.domain.domains.product.domain

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue("COURSE")
class CourseProduct(
    name: String,
    priceWon: Int,

    @Column(name = "total_lessons", nullable = true)
    val totalLessons: Int,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(columnDefinition = "TEXT")
    var curriculum: String? = null,

    @Column(name = "thumbnail_file_id", length = 26)
    var thumbnailFileId: String? = null,
) : Product(name = name, priceWon = priceWon)
