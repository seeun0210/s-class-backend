package com.sclass.domain.domains.product.domain

import com.sclass.domain.common.model.BaseTimeEntity
import com.sclass.domain.common.vo.Ulid
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "products")
abstract class Product(
    @Id
    @Column(length = 26)
    val id: String = Ulid.generate(),

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var priceWon: Int,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "thumbnail_file_id", length = 26)
    var thumbnailFileId: String? = null,

    @Column(nullable = false)
    var visible: Boolean = false,
) : BaseTimeEntity() {
    fun show() {
        visible = true
    }

    fun hide() {
        visible = false
    }
}
