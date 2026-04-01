package com.sclass.domain.domains.teacher.service

import com.sclass.common.annotation.DomainService
import com.sclass.domain.domains.teacher.adaptor.TeacherAdaptor
import com.sclass.domain.domains.teacher.adaptor.TeacherDocumentAdaptor
import com.sclass.domain.domains.teacher.domain.MajorCategory
import com.sclass.domain.domains.teacher.domain.Teacher
import com.sclass.domain.domains.teacher.domain.TeacherEducation
import com.sclass.domain.domains.teacher.exception.TeacherAlreadyExistsException
import com.sclass.domain.domains.user.adaptor.UserRoleAdaptor
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.Role
import com.sclass.domain.domains.user.domain.User
import com.sclass.domain.domains.user.domain.UserRoleState
import com.sclass.domain.domains.user.exception.RoleNotFoundException
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@DomainService
class TeacherDomainService(
    private val teacherAdaptor: TeacherAdaptor,
    private val teacherDocumentAdaptor: TeacherDocumentAdaptor,
    private val userRoleAdaptor: UserRoleAdaptor,
) {
    @Transactional
    fun register(
        user: User,
        education: TeacherEducation = TeacherEducation(),
    ): Teacher {
        if (teacherAdaptor.existsByUserId(user.id)) {
            throw TeacherAlreadyExistsException()
        }
        return teacherAdaptor.save(Teacher(user = user, education = education))
    }

    @Transactional
    fun updateProfile(
        teacher: Teacher,
        platform: Platform,
        birthDate: LocalDate,
        selfIntroduction: String?,
        majorCategory: MajorCategory,
        university: String,
        major: String,
        highSchool: String,
        address: String,
        residentNumber: String,
    ): Teacher {
        val userRole =
            userRoleAdaptor.findByUserIdAndPlatformAndRole(teacher.user.id, platform, Role.TEACHER)
                ?: throw RoleNotFoundException()
        teacher.updateProfile(
            state = userRole.state,
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
    fun submitForVerification(
        teacher: Teacher,
        platform: Platform,
    ): Teacher {
        val userRole =
            userRoleAdaptor.findByUserIdAndPlatformAndRole(teacher.user.id, platform, Role.TEACHER)
                ?: throw RoleNotFoundException()
        val documents = teacherDocumentAdaptor.findAllByTeacherId(teacher.id)
        teacher.recordSubmission(documents, userRole.state)
        userRole.changeStateTo(UserRoleState.PENDING)
        userRoleAdaptor.save(userRole)
        return teacherAdaptor.save(teacher)
    }

    @Transactional
    fun approve(
        teacher: Teacher,
        platform: Platform,
        approvedBy: String,
    ): Teacher {
        val userRole =
            userRoleAdaptor.findByUserIdAndPlatformAndRole(teacher.user.id, platform, Role.TEACHER)
                ?: throw RoleNotFoundException()
        userRole.approve(approvedBy)
        userRoleAdaptor.save(userRole)
        return teacherAdaptor.save(teacher)
    }

    @Transactional
    fun reject(
        teacher: Teacher,
        platform: Platform,
        reason: String,
    ): Teacher {
        val userRole =
            userRoleAdaptor.findByUserIdAndPlatformAndRole(teacher.user.id, platform, Role.TEACHER)
                ?: throw RoleNotFoundException()
        userRole.reject(reason)
        userRoleAdaptor.save(userRole)
        return teacherAdaptor.save(teacher)
    }
}
