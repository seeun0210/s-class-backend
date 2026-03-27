package com.sclass.domain.domains.teacher.service

import com.sclass.common.annotation.DomainService
import com.sclass.domain.domains.teacher.adaptor.TeacherAdaptor
import com.sclass.domain.domains.teacher.adaptor.TeacherDocumentAdaptor
import com.sclass.domain.domains.teacher.domain.MajorCategory
import com.sclass.domain.domains.teacher.domain.Teacher
import com.sclass.domain.domains.teacher.exception.TeacherAlreadyExistsException
import com.sclass.domain.domains.user.domain.User
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@DomainService
class TeacherDomainService(
    private val teacherAdaptor: TeacherAdaptor,
    private val teacherDocumentAdaptor: TeacherDocumentAdaptor,
) {
    @Transactional
    fun register(user: User): Teacher {
        if (teacherAdaptor.existsByUserId(user.id)) {
            throw TeacherAlreadyExistsException()
        }
        return teacherAdaptor.save(Teacher(user = user))
    }

    @Transactional
    fun updateProfile(
        teacher: Teacher,
        birthDate: LocalDate,
        selfIntroduction: String?,
        majorCategory: MajorCategory,
        university: String,
        major: String,
        highSchool: String,
        address: String,
        residentNumber: String,
    ): Teacher {
        teacher.updateProfile(
            birthDate = birthDate,
            selfIntroduction = selfIntroduction,
            majorCategory = majorCategory,
            university = university,
            major = major,
            highSchool = highSchool,
            address = address,
            residentNumber = residentNumber,
        )
        return teacherAdaptor.save(teacher)
    }

    @Transactional
    fun submitForVerification(teacher: Teacher): Teacher {
        val documents = teacherDocumentAdaptor.findAllByTeacherId(teacher.id)
        teacher.submitForVerification(documents)
        return teacherAdaptor.save(teacher)
    }

    @Transactional
    fun approve(
        teacher: Teacher,
        approvedBy: String,
    ): Teacher {
        teacher.approve(approvedBy)
        return teacherAdaptor.save(teacher)
    }

    @Transactional
    fun reject(
        teacher: Teacher,
        reason: String,
    ): Teacher {
        teacher.reject(reason)
        return teacherAdaptor.save(teacher)
    }
}
