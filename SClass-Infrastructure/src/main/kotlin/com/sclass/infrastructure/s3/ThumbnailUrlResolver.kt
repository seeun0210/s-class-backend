package com.sclass.infrastructure.s3

import org.springframework.stereotype.Component

@Component
class ThumbnailUrlResolver(
    private val s3Service: S3Service,
) {
    fun resolve(fileId: String?): String? =
        fileId?.let {
            s3Service.getPublicUrl("${S3Service.PUBLIC_PREFIX}$COURSE_THUMBNAIL_SUBDIR/$it")
        }

    companion object {
        // must match FileType.COURSE_THUMBNAIL.name.lowercase() used by CreateFileUseCase
        private const val COURSE_THUMBNAIL_SUBDIR = "course_thumbnail"
    }
}
