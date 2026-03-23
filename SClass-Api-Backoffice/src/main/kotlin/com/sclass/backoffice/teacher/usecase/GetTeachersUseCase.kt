package com.sclass.backoffice.teacher.usecase

import com.sclass.backoffice.teacher.dto.TeacherListResponse
import com.sclass.backoffice.teacher.dto.TeacherPageResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.teacher.adaptor.TeacherAdaptor
import com.sclass.domain.domains.teacher.domain.TeacherVerificationStatus
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetTeachersUseCase(
    private val teacherAdaptor: TeacherAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(
        status: TeacherVerificationStatus,
        pageable: Pageable,
    ): TeacherPageResponse {
        val page = teacherAdaptor.findAllByVerificationStatus(status, pageable)
        return TeacherPageResponse(
            content = page.content.map { TeacherListResponse.from(it) },
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            currentPage = page.number,
        )
    }
}
