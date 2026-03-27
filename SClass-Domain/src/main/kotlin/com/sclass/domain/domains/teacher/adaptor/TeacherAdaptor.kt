package com.sclass.domain.domains.teacher.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.teacher.domain.Teacher
import com.sclass.domain.domains.teacher.dto.TeacherSearchCondition
import com.sclass.domain.domains.teacher.dto.TeacherWithPlatform
import com.sclass.domain.domains.teacher.exception.TeacherNotFoundException
import com.sclass.domain.domains.teacher.repository.TeacherRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

@Adaptor
class TeacherAdaptor(
    private val teacherRepository: TeacherRepository,
) {
    fun findById(id: String): Teacher = teacherRepository.findById(id).orElseThrow { TeacherNotFoundException() }

    fun findByIdOrNull(id: String): Teacher? = teacherRepository.findById(id).orElse(null)

    fun findByUserId(userId: String): Teacher = findByUserIdOrNull(userId) ?: throw TeacherNotFoundException()

    fun findByUserIdOrNull(userId: String): Teacher? = teacherRepository.findByUserId(userId)

    fun existsByUserId(userId: String): Boolean = teacherRepository.existsByUserId(userId)

    fun save(teacher: Teacher): Teacher = teacherRepository.save(teacher)

    fun searchTeachers(
        condition: TeacherSearchCondition,
        pageable: Pageable,
    ): Page<TeacherWithPlatform> = teacherRepository.searchTeachers(condition, pageable)
}
