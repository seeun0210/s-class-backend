package com.sclass.supporters.course.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.common.exception.ForbiddenException
import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import com.sclass.domain.domains.user.adaptor.UserRoleAdaptor
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.Role
import com.sclass.supporters.course.dto.MyCourseResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetMyCourseListUseCase(
    private val courseAdaptor: CourseAdaptor,
    private val userRoleAdaptor: UserRoleAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(
        userId: String,
        role: String,
    ): List<MyCourseResponse> {
        if (Role.valueOf(role) != Role.TEACHER) throw ForbiddenException()

        val hasLmsTeacher =
            userRoleAdaptor.existsActiveByUserIdAndPlatformAndRole(userId, Platform.LMS, Role.TEACHER)

        if (!hasLmsTeacher) return emptyList()

        return courseAdaptor
            .findAllByTeacherUserIdWithEnrollmentCount(userId)
            .map { MyCourseResponse.from(it) }
    }
}
