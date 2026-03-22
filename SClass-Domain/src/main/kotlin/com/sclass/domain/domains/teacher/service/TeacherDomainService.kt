package com.sclass.domain.domains.teacher.service

import com.sclass.common.annotation.DomainService
import com.sclass.domain.domains.teacher.adaptor.TeacherAdaptor
import com.sclass.domain.domains.teacher.domain.Teacher
import com.sclass.domain.domains.teacher.exception.TeacherAlreadyExistsException
import com.sclass.domain.domains.user.domain.User
import org.springframework.transaction.annotation.Transactional

@DomainService
class TeacherDomainService(
    private val teacherAdaptor: TeacherAdaptor,
) {
    @Transactional
    fun register(user: User): Teacher {
        if (teacherAdaptor.existsByUserId(user.id)) {
            throw TeacherAlreadyExistsException()
        }
        return teacherAdaptor.save(Teacher(user = user))
    }

    @Transactional(readOnly = true)
    fun findByUserId(userId: String): Teacher = teacherAdaptor.findByUserId(userId)
}
