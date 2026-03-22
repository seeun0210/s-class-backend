package com.sclass.domain.domains.teacher.service

import com.sclass.common.annotation.DomainService
import com.sclass.domain.domains.teacher.adaptor.TeacherAdaptor
import com.sclass.domain.domains.teacher.domain.Teacher
import com.sclass.domain.domains.teacher.exception.TeacherAlreadyExistsException
import org.springframework.transaction.annotation.Transactional

@DomainService
class TeacherDomainService(
    private val teacherAdaptor: TeacherAdaptor,
) {
    @Transactional
    fun register(
        userId: String,
        organizationId: Long?,
    ): Teacher {
        val alreadyExists =
            if (organizationId != null) {
                teacherAdaptor.existsByUserIdAndOrganizationId(userId, organizationId)
            } else {
                teacherAdaptor.existsByUserIdAndOrganizationIdIsNull(userId)
            }
        if (alreadyExists) {
            throw TeacherAlreadyExistsException()
        }
        return teacherAdaptor.save(
            Teacher(
                userId = userId,
                organizationId = organizationId,
            ),
        )
    }

    @Transactional(readOnly = true)
    fun findAllByUserId(userId: String): List<Teacher> = teacherAdaptor.findAllByUserId(userId)

    @Transactional(readOnly = true)
    fun findAllByOrganizationId(organizationId: Long): List<Teacher> = teacherAdaptor.findAllByOrganizationId(organizationId)
}
