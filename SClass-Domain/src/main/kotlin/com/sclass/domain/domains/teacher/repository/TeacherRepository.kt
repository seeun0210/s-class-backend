package com.sclass.domain.domains.teacher.repository

import com.sclass.domain.domains.teacher.domain.Teacher
import org.springframework.data.jpa.repository.JpaRepository

interface TeacherRepository :
    JpaRepository<Teacher, String>,
    TeacherCustomRepository {
    fun findByUserId(userId: String): Teacher?

    fun existsByUserId(userId: String): Boolean
}
