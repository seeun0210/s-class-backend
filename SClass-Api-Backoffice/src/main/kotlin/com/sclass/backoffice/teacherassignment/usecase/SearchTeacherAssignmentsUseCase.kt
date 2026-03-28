package com.sclass.backoffice.teacherassignment.usecase

import com.sclass.backoffice.teacherassignment.dto.TeacherAssignmentListResponse
import com.sclass.backoffice.teacherassignment.dto.TeacherAssignmentPageResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.teacherassignment.adaptor.TeacherAssignmentAdaptor
import com.sclass.domain.domains.teacherassignment.dto.TeacherAssignmentSearchCondition
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional

@UseCase
class SearchTeacherAssignmentsUseCase(
    private val teacherAssignmentAdaptor: TeacherAssignmentAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(
        condition: TeacherAssignmentSearchCondition,
        pageable: Pageable,
    ): TeacherAssignmentPageResponse {
        val page =
            teacherAssignmentAdaptor.searchActiveAssignments(condition, pageable)
        return TeacherAssignmentPageResponse(
            content =
                page.content.map {
                    TeacherAssignmentListResponse.from(it)
                },
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            currentPage = page.number,
        )
    }
}
