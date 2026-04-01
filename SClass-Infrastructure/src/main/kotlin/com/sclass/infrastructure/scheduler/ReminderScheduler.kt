package com.sclass.infrastructure.scheduler

import org.quartz.Job
import org.quartz.JobBuilder
import org.quartz.JobDataMap
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.TriggerBuilder
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

@Component
class ReminderScheduler(
    private val scheduler: Scheduler,
) {
    fun schedule(
        jobKey: String,
        group: String,
        jobClass: Class<out Job>,
        triggerAt: LocalDateTime,
        jobData: JobDataMap = JobDataMap(),
    ) {
        val jk = JobKey.jobKey(jobKey, group)
        if (scheduler.checkExists(jk)) scheduler.deleteJob(jk)

        val job =
            JobBuilder
                .newJob(jobClass)
                .withIdentity(jk)
                .storeDurably()
                .build()

        val trigger =
            TriggerBuilder
                .newTrigger()
                .forJob(jk)
                .startAt(Date.from(triggerAt.atZone(ZoneId.systemDefault()).toInstant()))
                .usingJobData(JobDataMap(jobData))
                .build()

        scheduler.scheduleJob(job, trigger)
    }

    fun scheduleMultiple(
        jobKey: String,
        group: String,
        jobClass: Class<out Job>,
        triggers: List<Pair<LocalDateTime, JobDataMap>>,
    ) {
        val jk = JobKey.jobKey(jobKey, group)
        if (scheduler.checkExists(jk)) scheduler.deleteJob(jk)

        val job =
            JobBuilder
                .newJob(jobClass)
                .withIdentity(jk)
                .storeDurably()
                .build()
        scheduler.addJob(job, true)

        triggers.forEach { (triggerAt, data) ->
            val trigger =
                TriggerBuilder
                    .newTrigger()
                    .forJob(jk)
                    .startAt(Date.from(triggerAt.atZone(ZoneId.systemDefault()).toInstant()))
                    .usingJobData(JobDataMap(data))
                    .build()
            scheduler.scheduleJob(trigger)
        }
    }

    fun cancel(
        jobKey: String,
        group: String,
    ) {
        scheduler.deleteJob(JobKey.jobKey(jobKey, group))
    }
}
