package com.sclass.supporters.commission.scheduler

import com.sclass.domain.domains.commission.adaptor.CommissionAdaptor
import com.sclass.domain.domains.user.adaptor.UserAdaptor
import com.sclass.infrastructure.message.CommissionNotificationSender
import org.quartz.JobExecutionContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.quartz.QuartzJobBean

class CommissionNoResponseReminderJob : QuartzJobBean() {
    @Autowired
    lateinit var commissionAdaptor: CommissionAdaptor

    @Autowired
    lateinit var userAdaptor: UserAdaptor

    @Autowired
    lateinit var commissionNotificationSender: CommissionNotificationSender

    override fun executeInternal(context: JobExecutionContext) {
        val data = context.mergedJobDataMap
        val commissionId = data.getLong("commissionId")
        val elapsedTime = data.getString("elapsedTime")

        val commission =
            commissionAdaptor.findByIdOrNull(commissionId)
                ?: return
        val teacher = userAdaptor.findById(commission.teacherUserId)
        val student = userAdaptor.findById(commission.studentUserId)

        val phoneNumber = teacher.phoneNumber ?: return
        commissionNotificationSender.sendNoResponseReminder(
            phoneNumber = phoneNumber,
            teacherName = teacher.name,
            studentName = student.name,
            elapsedTime = elapsedTime,
            commissionId = commissionId.toString(),
        )
    }
}
