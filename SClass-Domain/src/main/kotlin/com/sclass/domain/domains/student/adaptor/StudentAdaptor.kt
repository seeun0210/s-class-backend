package com.sclass.domain.domains.student.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.student.domain.Student
import com.sclass.domain.domains.student.exception.StudentNotFoundException
import com.sclass.domain.domains.student.repository.StudentRepository

@Adaptor
class StudentAdaptor(
    private val studentRepository: StudentRepository,
) {
    fun findById(id: String): Student = studentRepository.findById(id).orElseThrow { StudentNotFoundException() }

    fun findByIdOrNull(id: String): Student? = studentRepository.findById(id).orElse(null)

    fun findAllByUserId(userId: String): List<Student> = studentRepository.findAllByUserId(userId)

    fun findByUserIdAndOrganizationId(
        userId: String,
        organizationId: Long,
    ): Student = findByUserIdAndOrganizationIdOrNull(userId, organizationId) ?: throw StudentNotFoundException()

    fun findByUserIdAndOrganizationIdOrNull(
        userId: String,
        organizationId: Long,
    ): Student? = studentRepository.findByUserIdAndOrganizationId(userId, organizationId)

    fun findByUserIdAndOrganizationIdIsNull(userId: String): Student? = studentRepository.findByUserIdAndOrganizationIdIsNull(userId)

    fun findAllByOrganizationId(organizationId: Long): List<Student> = studentRepository.findAllByOrganizationId(organizationId)

    fun existsByUserIdAndOrganizationId(
        userId: String,
        organizationId: Long,
    ): Boolean = studentRepository.existsByUserIdAndOrganizationId(userId, organizationId)

    fun existsByUserIdAndOrganizationIdIsNull(userId: String): Boolean = studentRepository.existsByUserIdAndOrganizationIdIsNull(userId)

    fun save(student: Student): Student = studentRepository.save(student)
}
