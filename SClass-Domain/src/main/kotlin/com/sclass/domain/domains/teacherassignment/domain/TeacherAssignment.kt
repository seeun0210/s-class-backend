package com.sclass.domain.domains.teacherassignment.domain

import com.sclass.domain.domains.user.domain.Platform
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "teacher_assignments")
class TeacherAssignment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "student_id", nullable = false, length = 26)
    val studentId: String,

    @Column(name = "teacher_id", nullable = false, length = 26)
    val teacherId: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val platform: Platform,

    @Column(name = "organization_id")
    val organizationId: Long? = null,

    @Column(name = "assigned_by", nullable = false, length = 26)
    val assignedBy: String,

    @Column(name = "assigned_at", nullable = false, updatable = false)
    val assignedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "unassigned_at")
    var unassignedAt: LocalDateTime? = null,
)
