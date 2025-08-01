/*
 * Copyright 2025 King's College London
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.radarbase.appserver.jersey.service.quartz

import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
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
    private val coroutineScope: CoroutineScope,
    jobListener: JobListener?,
    schedulerListener: SchedulerListener?,
) : SchedulerService {

    init {
        if (!scheduler.isStarted) {
            scheduler.start()
        }
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
        coroutineScope.launch {
            scheduler.scheduleJob(jobDetail, trigger)
        }
    }

    override fun checkJobExists(jobKey: JobKey): Boolean {
        return scheduler.checkExists(jobKey)
    }

    override fun checkTriggerExists(triggerKey: TriggerKey): Boolean {
        return scheduler.checkExists(triggerKey)
    }

    override fun scheduleJobs(jobDetailTriggerMap: Map<JobDetail, Set<Trigger>>) {
        coroutineScope.launch {
            scheduler.scheduleJobs(jobDetailTriggerMap, true)
        }
    }

    override fun updateScheduledJob(
        jobKey: JobKey, triggerKey: TriggerKey, jobDataMap: JobDataMap, associatedObject: Any?,
    ) {
        coroutineScope.launch {
            require(checkJobExists(jobKey)) { "The Specified Job Key does not exist : $jobKey" }
            require(checkTriggerExists(triggerKey)) { "The Specified Trigger Key does not exist :$triggerKey" }

            val jobDetail = scheduler.getJobDetail(jobKey)
            jobDetail.jobDataMap.putAll(jobDataMap.wrappedMap)

            val trigger = scheduler.getTrigger(triggerKey) as SimpleTriggerImpl
            trigger.jobDataMap.putAll(jobDataMap.wrappedMap)

            if (associatedObject is Scheduled) {
                val scheduledTime =
                    requireNotNull(associatedObject.scheduledTime) { "The scheduled time cannot be null" }
                trigger.setStartTime(Date(scheduledTime.toEpochMilli()))
            }

            scheduler.addJob(jobDetail, true)
            scheduler.rescheduleJob(triggerKey, trigger)
        }
    }

    override fun deleteScheduledJobs(jobKeys: List<JobKey>) {
        // The scheduler::deleteJobs method does not unschedule jobs so using a deleteJob.
        jobKeys.forEach(this@SchedulerServiceImpl::deleteScheduledJob)
    }

    override fun deleteScheduledJob(jobKey: JobKey) {
        coroutineScope.launch {
            if (scheduler.checkExists(jobKey)) {
                jobKey.let(scheduler::deleteJob)
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SchedulerServiceImpl::class.java)
    }
}
