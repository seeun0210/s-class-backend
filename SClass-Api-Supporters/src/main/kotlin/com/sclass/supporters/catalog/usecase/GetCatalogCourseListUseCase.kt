package com.sclass.supporters.catalog.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import com.sclass.supporters.catalog.dto.CatalogCourseResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetCatalogCourseListUseCase(
    private val courseAdaptor: CourseAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(): List<CatalogCourseResponse> =
        courseAdaptor.findAllCatalogCourses().map {
            CatalogCourseResponse.from(it)
        }
}
