package com.sclass.backoffice.teacher.usecase

import com.sclass.backoffice.teacher.dto.TeacherListResponse
import com.sclass.backoffice.teacher.dto.TeacherPageResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.teacher.dto.TeacherSearchCondition
import com.sclass.domain.domains.teacher.service.TeacherDomainService
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetTeachersUseCase(
    private val teacherDomainService: TeacherDomainService,
) {
    @Transactional(readOnly = true)
    fun execute(
        condition: TeacherSearchCondition,
        pageable: Pageable,
    ): TeacherPageResponse {
        val page = teacherDomainService.searchTeachers(condition, pageable)
        return TeacherPageResponse(
            content = page.content.map { TeacherListResponse.from(it.teacher, it.platform) },
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            currentPage = page.number,
        )
    }
}
