package com.sclass.domain.domains.student.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.organization.domain.OrganizationUser
import com.sclass.domain.domains.student.domain.Student
import com.sclass.domain.domains.student.domain.StudentDocument
import com.sclass.domain.domains.student.dto.StudentSearchCondition
import com.sclass.domain.domains.student.dto.StudentWithRoles
import com.sclass.domain.domains.student.exception.StudentNotFoundException
import com.sclass.domain.domains.student.repository.StudentRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

@Adaptor
class StudentAdaptor(
    private val studentRepository: StudentRepository,
) {
    fun findById(id: String): Student = studentRepository.findById(id).orElseThrow { StudentNotFoundException() }

    fun findByIdOrNull(id: String): Student? = studentRepository.findById(id).orElse(null)

    fun findByUserId(userId: String): Student = findByUserIdOrNull(userId) ?: throw StudentNotFoundException()

    fun findByUserIdOrNull(userId: String): Student? = studentRepository.findByUserId(userId)

    fun existsByUserId(userId: String): Boolean = studentRepository.existsByUserId(userId)

    fun save(student: Student): Student = studentRepository.save(student)

    fun searchStudents(
        condition: StudentSearchCondition,
        pageable: Pageable,
    ): Page<StudentWithRoles> = studentRepository.searchStudents(condition, pageable)

    fun findByIdWithUser(id: String): Student = studentRepository.findByIdWithUser(id) ?: throw StudentNotFoundException()

    fun findByUserIdWithUser(userId: String): Student = studentRepository.findByUserIdWithUser(userId) ?: throw StudentNotFoundException()

    fun findDocumentsWithFileByStudentId(studentId: String): List<StudentDocument> =
        studentRepository.findDocumentsWithFileByStudentId(studentId)

    fun findDocumentsWithFileByUserIds(userIds: List<String>): Map<String, List<StudentDocument>> =
        studentRepository.findDocumentsWithFileByUserIds(userIds)

    fun findOrganizationsByUserId(userId: String): List<OrganizationUser> = studentRepository.findOrganizationsByUserId(userId)
}
