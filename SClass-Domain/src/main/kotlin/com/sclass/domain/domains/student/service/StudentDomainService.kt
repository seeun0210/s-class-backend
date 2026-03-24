package com.sclass.domain.domains.student.service

import com.sclass.common.annotation.DomainService
import com.sclass.domain.domains.student.adaptor.StudentAdaptor
import com.sclass.domain.domains.student.domain.Student
import com.sclass.domain.domains.student.exception.StudentAlreadyExistsException
import com.sclass.domain.domains.user.domain.Grade
import com.sclass.domain.domains.user.domain.User
import org.springframework.transaction.annotation.Transactional

@DomainService
class StudentDomainService(
    private val studentAdaptor: StudentAdaptor,
) {
    @Transactional
    fun register(user: User): Student {
        if (studentAdaptor.existsByUserId(user.id)) {
            throw StudentAlreadyExistsException()
        }
        return studentAdaptor.save(Student(user = user))
    }

    @Transactional(readOnly = true)
    fun findByUserId(userId: String): Student = studentAdaptor.findByUserId(userId)

    @Transactional
    fun updateProfile(
        student: Student,
        grade: Grade,
        school: String,
        parentPhoneNumber: String?,
    ): Student {
        student.updateProfile(grade = grade, school = school, parentPhoneNumber = parentPhoneNumber)
        return studentAdaptor.save(student)
    }
}
