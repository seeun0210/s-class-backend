package com.sclass.backoffice.lesson.usecase

import com.sclass.backoffice.lesson.dto.LessonResponse
import com.sclass.backoffice.lesson.dto.UpdateSubstituteTeacherRequest
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.domain.domains.user.adaptor.UserAdaptor
import org.springframework.transaction.annotation.Transactional

@UseCase
class UpdateSubstituteTeacherUseCase(
    private val lessonAdaptor: LessonAdaptor,
    private val userAdaptor: UserAdaptor,
) {
    @Transactional
    fun execute(
        lessonId: Long,
        request: UpdateSubstituteTeacherRequest,
    ): LessonResponse {
        val lesson = lessonAdaptor.findById(lessonId)
        val teacherUserId = request.teacherUserId
        if (teacherUserId.isNullOrBlank()) {
            lesson.unassignSubstitute()
        } else {
            userAdaptor.findById(teacherUserId)
            lesson.assignSubstitute(teacherUserId)
        }
        return LessonResponse.from(lesson)
    }
}
