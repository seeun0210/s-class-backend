package com.sclass.domain.domains.file.domain

import com.sclass.domain.common.model.BaseTimeEntity
import com.sclass.domain.common.vo.Ulid
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "files")
class File(
    @Id
    @Column(length = 26)
    val id: String,
    @Column(nullable = false)
    val originalFilename: String,
    @Column(nullable = false)
    val storedFilename: String,
    @Column(nullable = false)
    val filePath: String,
    @Column(nullable = false)
    val mimeType: String,
    @Column(nullable = false)
    val fileSize: Long,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val fileType: FileType,
    @Column(nullable = false)
    val uploadedBy: String,
) : BaseTimeEntity() {
    companion object {
        fun create(
            originalFilename: String,
            storedFilename: String,
            filePath: String,
            mimeType: String,
            fileSize: Long,
            fileType: FileType,
            uploadedBy: String,
        ): File =
            File(
                id = Ulid.generate(),
                originalFilename = originalFilename,
                storedFilename = storedFilename,
                filePath = filePath,
                mimeType = mimeType,
                fileSize = fileSize,
                fileType = fileType,
                uploadedBy = uploadedBy,
            )
    }
}
