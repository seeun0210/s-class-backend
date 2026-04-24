package com.sclass.supporters.commission.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.common.exception.BusinessException
import com.sclass.common.exception.GlobalErrorCode
import com.sclass.domain.domains.commission.adaptor.CommissionAdaptor
import com.sclass.domain.domains.commission.adaptor.CommissionFileAdaptor
import com.sclass.domain.domains.commission.exception.CommissionErrorCode
import com.sclass.domain.domains.file.domain.FileType
import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.domain.domains.lesson.domain.LessonStatus
import com.sclass.domain.domains.lesson.exception.LessonAlreadyCompletedException
import com.sclass.supporters.commission.dto.CommissionResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class DeleteCommissionFileUseCase(
    private val commissionAdaptor: CommissionAdaptor,
    private val commissionFileAdaptor: CommissionFileAdaptor,
    private val lessonAdaptor: LessonAdaptor,
) {
    @Transactional
    fun execute(
        teacherUserId: String,
        commissionId: Long,
        commissionFileId: Long,
    ): CommissionResponse {
        val commission = commissionAdaptor.findById(commissionId)
        if (commission.teacherUserId != teacherUserId) {
            throw BusinessException(CommissionErrorCode.UNAUTHORIZED_ACCESS)
        }

        commission.acceptedLessonId?.let { lessonId ->
            val lesson = lessonAdaptor.findById(lessonId)
            if (lesson.status == LessonStatus.COMPLETED) throw LessonAlreadyCompletedException()
        }

        val commissionFile = commissionFileAdaptor.findById(commissionFileId)
        if (commissionFile.commission.id != commissionId) {
            throw BusinessException(CommissionErrorCode.COMMISSION_FILE_NOT_FOUND)
        }
        if (commissionFile.file.fileType != FileType.TASK_SUBMISSION) {
            throw BusinessException(GlobalErrorCode.INVALID_INPUT)
        }

        commissionFileAdaptor.delete(commissionFile)

        val commissionFiles = commissionFileAdaptor.findByCommissionId(commissionId)
        return CommissionResponse.from(commission, commissionFiles)
    }
}
