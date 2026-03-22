package com.sclass.domain.domains.student.service

import com.sclass.common.annotation.DomainService
import com.sclass.domain.domains.student.adaptor.StudentAdaptor
import com.sclass.domain.domains.student.domain.Student
import com.sclass.domain.domains.student.exception.StudentAlreadyExistsException
import com.sclass.domain.domains.user.domain.User
import org.springframework.transaction.annotation.Transactional

@DomainService
class StudentDomainService(
    private val studentAdaptor: StudentAdaptor,
) {
    @Transactional
    fun register(
        user: User,
        organizationId: Long?,
    ): Student {
        val alreadyExists =
            if (organizationId != null) {
                studentAdaptor.existsByUserIdAndOrganizationId(user.id, organizationId)
            } else {
                studentAdaptor.existsByUserIdAndOrganizationIdIsNull(user.id)
            }
        if (alreadyExists) {
            throw StudentAlreadyExistsException()
        }
        return studentAdaptor.save(
            Student(
                user = user,
                organizationId = organizationId,
            ),
        )
    }

    @Transactional(readOnly = true)
    fun findAllByUserId(userId: String): List<Student> = studentAdaptor.findAllByUserId(userId)

    @Transactional(readOnly = true)
    fun findAllByOrganizationId(organizationId: Long): List<Student> = studentAdaptor.findAllByOrganizationId(organizationId)
}
