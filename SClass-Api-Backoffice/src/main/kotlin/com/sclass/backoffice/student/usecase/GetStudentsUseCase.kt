package com.sclass.backoffice.student.usecase

import com.sclass.backoffice.student.dto.StudentListResponse
import com.sclass.backoffice.student.dto.StudentPageResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.student.adaptor.StudentAdaptor
import com.sclass.domain.domains.student.dto.StudentSearchCondition
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetStudentsUseCase(
    private val studentAdaptor: StudentAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(
        condition: StudentSearchCondition,
        pageable: Pageable,
    ): StudentPageResponse {
        val page = studentAdaptor.searchStudents(condition, pageable)
        return StudentPageResponse(
            content = page.content.map { StudentListResponse.from(it.student, it.roles) },
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            currentPage = page.number,
        )
    }
}
