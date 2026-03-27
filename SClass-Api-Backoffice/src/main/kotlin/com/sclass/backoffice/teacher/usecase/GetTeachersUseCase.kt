package com.sclass.backoffice.teacher.usecase

import com.sclass.backoffice.teacher.dto.TeacherListResponse
import com.sclass.backoffice.teacher.dto.TeacherPageResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.teacher.adaptor.TeacherAdaptor
import com.sclass.domain.domains.teacher.dto.TeacherSearchCondition
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetTeachersUseCase(
    private val teacherAdaptor: TeacherAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(
        condition: TeacherSearchCondition,
        pageable: Pageable,
    ): TeacherPageResponse {
        val page = teacherAdaptor.searchTeachers(condition, pageable)
        return TeacherPageResponse(
            content = page.content.map { TeacherListResponse.from(it.teacher, it.platform) },
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            currentPage = page.number,
        )
    }
}
