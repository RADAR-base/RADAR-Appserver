package org.radarbase.appserver.jersey.service.quartz

import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.JobKey
import org.quartz.Trigger
import org.quartz.TriggerKey

interface SchedulerService {
    fun scheduleJob(jobDetail: JobDetail, trigger: Trigger)

    fun scheduleJobs(jobDetailTriggerMap: Map<JobDetail, Set<Trigger>>)

    fun checkJobExists(jobKey: JobKey): Boolean

    fun updateScheduledJob(
        jobKey: JobKey, triggerKey: TriggerKey, jobDataMap: JobDataMap, associatedObject: Any?
    )

    fun deleteScheduledJobs(jobKeys: List<JobKey>)

    fun deleteScheduledJob(jobKey: JobKey)
}
