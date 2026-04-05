package com.sclass.supporters.commission.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.coin.service.CoinDomainService
import com.sclass.domain.domains.commission.adaptor.CommissionAdaptor
import com.sclass.domain.domains.commission.adaptor.CommissionFileAdaptor
import com.sclass.domain.domains.commission.domain.Commission
import com.sclass.domain.domains.commission.domain.CommissionFile
import com.sclass.domain.domains.commission.domain.GuideInfo
import com.sclass.domain.domains.file.adaptor.FileAdaptor
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.teacherassignment.adaptor.TeacherAssignmentAdaptor
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.supporters.commission.dto.CommissionResponse
import com.sclass.supporters.commission.dto.CreateCommissionRequest
import com.sclass.supporters.commission.event.CommissionAssignedEvent
import com.sclass.supporters.commission.scheduler.CommissionReminderScheduler
import org.springframework.context.ApplicationEventPublisher
import org.springframework.transaction.annotation.Transactional
import java.time.format.DateTimeFormatter

@UseCase
class CreateCommissionUseCase(
    private val commissionAdaptor: CommissionAdaptor,
    private val commissionFileAdaptor: CommissionFileAdaptor,
    private val teacherAssignmentAdaptor: TeacherAssignmentAdaptor,
    private val fileAdaptor: FileAdaptor,
    private val eventPublisher: ApplicationEventPublisher,
    private val commissionReminderScheduler: CommissionReminderScheduler,
    private val coinDomainService: CoinDomainService,
    private val productAdaptor: ProductAdaptor,
) {
    @Transactional
    fun execute(
        studentUserId: String,
        request: CreateCommissionRequest,
    ): CommissionResponse {
        val assignment =
            teacherAssignmentAdaptor.findActiveByStudentUserIdAndPlatformAndOrganizationId(
                studentUserId = studentUserId,
                platform = Platform.SUPPORTERS,
                organizationId = null,
            )

        val commission =
            commissionAdaptor.save(
                Commission(
                    studentUserId = studentUserId,
                    teacherUserId = assignment.teacherUserId,
                    outputFormat = request.outputFormat,
                    activityType = request.activityType,
                    guideInfo =
                        GuideInfo(
                            subject = request.guideInfo.subject,
                            volume = request.guideInfo.volume,
                            requiredElements = request.guideInfo.requiredElements,
                            gradingCriteria = request.guideInfo.gradingCriteria,
                            teacherEmphasis = request.guideInfo.teacherEmphasis,
                        ),
                ),
            )

        val commissionProduct = productAdaptor.findActiveCommissionProduct()

        coinDomainService.deduct(
            userId = studentUserId,
            amount = commissionProduct.coinCost,
            referenceId = commission.id.toString(),
            description = "의뢰 생성 코인 차감",
        )

        request.fileIds?.let { fileIds ->
            val files = fileIds.map { fileAdaptor.findById(it) }
            commissionFileAdaptor.saveAll(
                files.map { CommissionFile(commission = commission, file = it) },
            )
        }

        commissionReminderScheduler.scheduleNoRespReminders(commission.id, commission.createdAt)
        commissionReminderScheduler.resetInactiveReminder(commission.id)

        eventPublisher.publishEvent(
            CommissionAssignedEvent(
                teacherUserId = commission.teacherUserId,
                studentUserId = studentUserId,
                commissionId = commission.id.toString(),
                subject = commission.guideInfo.subject,
                createdAt = commission.createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
            ),
        )

        return CommissionResponse.from(commission)
    }
}
