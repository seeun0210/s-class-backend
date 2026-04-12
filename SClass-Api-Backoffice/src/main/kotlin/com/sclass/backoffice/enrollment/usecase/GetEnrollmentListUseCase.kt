package com.sclass.backoffice.enrollment.usecase

import com.sclass.backoffice.enrollment.dto.EnrollmentListResponse
import com.sclass.backoffice.enrollment.dto.EnrollmentPageResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.enrollment.domain.EnrollmentStatus
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetEnrollmentListUseCase(
    private val enrollmentAdaptor: EnrollmentAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(
        studentUserId: String?,
        teacherUserId: String?,
        courseId: Long?,
        status: EnrollmentStatus?,
        pageable: Pageable,
    ): EnrollmentPageResponse {
        val page = enrollmentAdaptor.searchEnrollments(studentUserId, teacherUserId, courseId, status, pageable)
        return EnrollmentPageResponse(
            content = page.content.map { EnrollmentListResponse.from(it) },
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            currentPage = page.number,
        )
    }
}
