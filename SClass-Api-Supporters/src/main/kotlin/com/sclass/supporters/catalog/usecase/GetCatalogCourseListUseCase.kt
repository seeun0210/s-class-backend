package com.sclass.supporters.catalog.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import com.sclass.infrastructure.s3.ThumbnailUrlResolver
import com.sclass.supporters.catalog.dto.CatalogCoursePageResponse
import com.sclass.supporters.catalog.dto.CatalogCourseResponse
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetCatalogCourseListUseCase(
    private val courseAdaptor: CourseAdaptor,
    private val thumbnailUrlResolver: ThumbnailUrlResolver,
) {
    @Transactional(readOnly = true)
    fun execute(pageable: Pageable): CatalogCoursePageResponse {
        val page = courseAdaptor.findAllCatalogCourses(pageable)
        return CatalogCoursePageResponse(
            content =
                page.content.map {
                    CatalogCourseResponse.from(it, thumbnailUrlResolver.resolve(it.courseProduct?.thumbnailFileId))
                },
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            currentPage = page.number,
        )
    }
}
