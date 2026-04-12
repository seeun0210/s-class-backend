package com.sclass.backoffice.course.usecase

import com.sclass.backoffice.course.dto.CourseListResponse
import com.sclass.backoffice.course.dto.CoursePageResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import com.sclass.domain.domains.course.domain.CourseStatus
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetCourseListUseCase(
    private val courseAdaptor: CourseAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(
        teacherUserId: String?,
        status: CourseStatus?,
        pageable: Pageable,
    ): CoursePageResponse {
        val page = courseAdaptor.searchCourses(teacherUserId, status, pageable)
        return CoursePageResponse(
            content = page.content.map { CourseListResponse.from(it) },
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            currentPage = page.number,
        )
    }
}
