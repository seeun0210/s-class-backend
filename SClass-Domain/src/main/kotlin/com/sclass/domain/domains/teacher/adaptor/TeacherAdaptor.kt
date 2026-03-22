package com.sclass.domain.domains.teacher.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.teacher.domain.Teacher
import com.sclass.domain.domains.teacher.exception.TeacherNotFoundException
import com.sclass.domain.domains.teacher.repository.TeacherRepository

@Adaptor
class TeacherAdaptor(
    private val teacherRepository: TeacherRepository,
) {
    fun findById(id: String): Teacher = teacherRepository.findById(id).orElseThrow { TeacherNotFoundException() }

    fun findByIdOrNull(id: String): Teacher? = teacherRepository.findById(id).orElse(null)

    fun findByUserId(userId: String): Teacher = findByUserIdOrNull(userId) ?: throw TeacherNotFoundException()

    fun findByUserIdOrNull(userId: String): Teacher? = teacherRepository.findByUserId(userId)

    fun findByUserIdAndOrganizationId(
        userId: String,
        organizationId: Long,
    ): Teacher = findByUserIdAndOrganizationIdOrNull(userId, organizationId) ?: throw TeacherNotFoundException()

    fun findByUserIdAndOrganizationIdOrNull(
        userId: String,
        organizationId: Long,
    ): Teacher? = teacherRepository.findByUserIdAndOrganizationId(userId, organizationId)

    fun findByUserIdAndOrganizationIdIsNull(userId: String): Teacher? = teacherRepository.findByUserIdAndOrganizationIdIsNull(userId)

    fun findAllByOrganizationId(organizationId: Long): List<Teacher> = teacherRepository.findAllByOrganizationId(organizationId)

    fun existsByUserIdAndOrganizationId(
        userId: String,
        organizationId: Long,
    ): Boolean = teacherRepository.existsByUserIdAndOrganizationId(userId, organizationId)

    fun existsByUserIdAndOrganizationIdIsNull(userId: String): Boolean = teacherRepository.existsByUserIdAndOrganizationIdIsNull(userId)

    fun save(teacher: Teacher): Teacher = teacherRepository.save(teacher)
}
