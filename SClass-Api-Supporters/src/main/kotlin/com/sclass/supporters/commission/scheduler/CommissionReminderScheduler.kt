package com.sclass.supporters.commission.scheduler

import com.sclass.infrastructure.scheduler.ReminderScheduler
import org.quartz.JobDataMap
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import kotlin.jvm.java

@Component
class CommissionReminderScheduler(
    private val reminderScheduler: ReminderScheduler,
) {
    fun scheduleNoRespReminders(
        commissionId: Long,
        createdAt: LocalDateTime,
    ) {
        reminderScheduler.scheduleMultiple(
            jobKey = "noResp_$commissionId",
            group = "COMM_NO_RESP",
            jobClass = CommissionNoResponseReminderJob::class.java,
            triggers =
                listOf(
                    createdAt.plusHours(8) to
                        JobDataMap(
                            mapOf(
                                "commissionId" to commissionId,
                                "elapsedTime" to
                                    "8시간",
                            ),
                        ),
                    createdAt.plusHours(60) to
                        JobDataMap(
                            mapOf(
                                "commissionId" to commissionId,
                                "elapsedTime" to
                                    "60시간",
                            ),
                        ),
                    createdAt.plusHours(64) to
                        JobDataMap(
                            mapOf(
                                "commissionId" to commissionId,
                                "elapsedTime" to
                                    "64시간",
                            ),
                        ),
                    createdAt.plusHours(71) to
                        JobDataMap(
                            mapOf(
                                "commissionId" to commissionId,
                                "elapsedTime" to
                                    "71시간",
                            ),
                        ),
                ),
        )
    }

    fun cancelNoRespReminders(commissionId: Long) {
        reminderScheduler.cancel("noResp_$commissionId", "COMM_NO_RESP")
    }

    fun resetInactiveReminder(commissionId: Long) {
        reminderScheduler.cancel("inactive_$commissionId", "COMM_INACTIVE")
        reminderScheduler.schedule(
            jobKey = "inactive_$commissionId",
            group = "COMM_INACTIVE",
            jobClass = CommissionInactiveReminderJob::class.java,
            triggerAt = LocalDateTime.now().plusDays(7),
            jobData =
                JobDataMap(
                    mapOf(
                        "commissionId" to commissionId,
                        "lastActivityAt" to LocalDateTime.now().toString(),
                    ),
                ),
        )
    }

    fun cancelAllReminders(commissionId: Long) {
        cancelNoRespReminders(commissionId)
        reminderScheduler.cancel("inactive_$commissionId", "COMM_INACTIVE")
    }
}
