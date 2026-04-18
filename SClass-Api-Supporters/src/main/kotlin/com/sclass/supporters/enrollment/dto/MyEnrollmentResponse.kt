package com.sclass.supporters.enrollment.dto

import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.domain.domains.enrollment.domain.EnrollmentStatus
import com.sclass.domain.domains.enrollment.domain.EnrollmentType
import com.sclass.domain.domains.enrollment.dto.EnrollmentWithCourseDto
import java.time.LocalDateTime

data class MyEnrollmentResponse(
    val id: Long,
    val courseId: Long,
    val status: EnrollmentStatus,
    val enrollmentType: EnrollmentType,
    val tuitionAmountWon: Int,
    val course: CourseSummary?,
) {
    data class CourseSummary(
        val name: String,
        val description: String?,
        val thumbnailFileId: String?,
        val teacherName: String?,
        val courseStatus: CourseStatus,
        val enrollmentDeadLine: LocalDateTime?,
        val startAt: LocalDateTime?,
        val endAt: LocalDateTime?,
    )

    companion object {
        fun from(dto: EnrollmentWithCourseDto): MyEnrollmentResponse {
            val summary =
                dto.course?.let { course ->
                    CourseSummary(
                        name = dto.courseProduct?.name ?: "",
                        description = dto.courseProduct?.description,
                        thumbnailFileId = dto.courseProduct?.thumbnailFileId,
                        teacherName = dto.teacherName,
                        courseStatus = course.status,
                        enrollmentDeadLine = course.enrollmentDeadLine,
                        startAt = course.startAt,
                        endAt = course.endAt,
                    )
                }
            return MyEnrollmentResponse(
                id = dto.enrollment.id,
                courseId = dto.enrollment.courseId,
                status = dto.enrollment.status,
                enrollmentType = dto.enrollment.enrollmentType,
                tuitionAmountWon = dto.enrollment.tuitionAmountWon,
                course = summary,
            )
        }
    }
}
