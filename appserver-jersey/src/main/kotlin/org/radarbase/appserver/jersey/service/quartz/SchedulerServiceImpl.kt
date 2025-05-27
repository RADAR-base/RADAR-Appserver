package org.radarbase.appserver.jersey.service.quartz

import jakarta.inject.Inject
import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.JobKey
import org.quartz.JobListener
import org.quartz.Scheduler
import org.quartz.SchedulerException
import org.quartz.SchedulerListener
import org.quartz.Trigger
import org.quartz.TriggerKey
import org.quartz.impl.triggers.SimpleTriggerImpl
import org.radarbase.appserver.jersey.entity.Scheduled
import org.slf4j.LoggerFactory
import java.util.Date

class SchedulerServiceImpl @Inject constructor(
    private val scheduler: Scheduler,
    jobListener: JobListener?,
    schedulerListener: SchedulerListener?,
) : SchedulerService {

    init {
        if (jobListener != null && schedulerListener != null) {
            try {
                scheduler.listenerManager.addJobListener(jobListener)
                scheduler.listenerManager.addSchedulerListener(schedulerListener)
            } catch (exc: SchedulerException) {
                logger.warn("The Listeners could not be added to the scheduler", exc)
            }
        } else {
            logger.warn("The Listeners cannot be null and will not be added to the scheduler")
        }
    }

    override fun scheduleJob(jobDetail: JobDetail, trigger: Trigger) {
        scheduler.scheduleJob(jobDetail, trigger)
    }

    override fun checkJobExists(jobKey: JobKey): Boolean {
        return scheduler.checkExists(jobKey)
    }

    override fun scheduleJobs(jobDetailTriggerMap: Map<JobDetail, Set<Trigger>>) {
        scheduler.scheduleJobs(jobDetailTriggerMap, true)
    }

    override fun updateScheduledJob(
        jobKey: JobKey, triggerKey: TriggerKey, jobDataMap: JobDataMap, associatedObject: Any?
    ) {
        require(scheduler.checkExists(jobKey)) { "The Specified Job Key does not exist : $jobKey" }

        require(scheduler.checkExists(triggerKey)) { "The Specified Trigger Key does not exist :$triggerKey" }

        val jobDetail = scheduler.getJobDetail(jobKey)
        jobDetail.jobDataMap.putAll(jobDataMap.wrappedMap)

        val trigger = scheduler.getTrigger(triggerKey) as SimpleTriggerImpl
        trigger.jobDataMap.putAll(jobDataMap.wrappedMap)

        if (associatedObject is Scheduled) {
            val scheduledObject = associatedObject
            trigger.setStartTime(Date(scheduledObject.scheduledTime!!.toEpochMilli()))
        }

        scheduler.addJob(jobDetail, true)

        scheduler.rescheduleJob(triggerKey, trigger)
    }

    override fun deleteScheduledJobs(jobKeys: List<JobKey>) {
        // The scheduler.deleteJobs method does not unschedule jobs so using a deleteJob.
        jobKeys.forEach(this::deleteScheduledJob)
    }

    override fun deleteScheduledJob(jobKey: JobKey) {
        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SchedulerServiceImpl::class.java)
    }
}
