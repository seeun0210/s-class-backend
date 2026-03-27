package com.sclass.domain.domains.student.repository

import com.sclass.domain.domains.student.dto.StudentSearchCondition
import com.sclass.domain.domains.student.dto.StudentWithPlatform
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface StudentCustomRepository {
    fun searchStudents(
        condition: StudentSearchCondition,
        page: Pageable,
    ): Page<StudentWithPlatform>
}
