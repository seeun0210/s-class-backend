package com.sclass.domain.domains.student.repository

import com.sclass.domain.domains.organization.domain.OrganizationUser
import com.sclass.domain.domains.student.domain.Student
import com.sclass.domain.domains.student.domain.StudentDocument
import com.sclass.domain.domains.student.dto.StudentSearchCondition
import com.sclass.domain.domains.student.dto.StudentWithRoles
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface StudentCustomRepository {
    fun searchStudents(
        condition: StudentSearchCondition,
        page: Pageable,
    ): Page<StudentWithRoles>

    fun findByIdWithUser(id: String): Student?

    fun findByUserIdWithUser(userId: String): Student?

    fun findDocumentsWithFileByStudentId(studentId: String): List<StudentDocument>

    fun findAcademicDocumentsWithFileByUserIds(userIds: List<String>): Map<String, List<StudentDocument>>

    fun findOrganizationsByUserId(userId: String): List<OrganizationUser>
}
