package com.sclass.domain.domains.student.repository

import com.sclass.domain.domains.student.domain.Student
import org.springframework.data.jpa.repository.JpaRepository

interface StudentRepository : JpaRepository<Student, String> {
    fun findByUserId(userId: String): Student?

    fun existsByUserId(userId: String): Boolean
}
