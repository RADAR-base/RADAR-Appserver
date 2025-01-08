/*
 *
 *  *
 *  *  * Copyright 2018 King's College London
 *  *  *
 *  *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  * you may not use this file except in compliance with the License.
 *  *  * You may obtain a copy of the License at
 *  *  *
 *  *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *  *
 *  *  * Unless required by applicable law or agreed to in writing, software
 *  *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  * See the License for the specific language governing permissions and
 *  *  * limitations under the License.
 *  *  *
 *  *
 *
 */
package org.radarbase.appserver.service.scheduler.quartz

import org.quartz.*
import org.quartz.impl.triggers.SimpleTriggerImpl
import org.radarbase.appserver.entity.Scheduled
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.util.*
import java.util.function.Consumer

/**
 * An implementation of the [SchedulerService] providing async access to schedule,
 * update and delete Jobs.
 *
 * @author yatharthranjan
 */
@Service
class SchedulerServiceImpl : SchedulerService {
    @Transient
    private val scheduler: Scheduler

    constructor(scheduler: Scheduler) {
        this.scheduler = scheduler
    }

    @Autowired
    constructor(scheduler: Scheduler, jobListener: JobListener?, schedulerListener: SchedulerListener?) {
        this.scheduler = scheduler
        if (jobListener != null && schedulerListener != null) {
            try {
                scheduler.listenerManager.addJobListener(jobListener)
                scheduler.listenerManager.addSchedulerListener(schedulerListener)
            } catch (exc: SchedulerException) {
                log.warn("The Listeners could not be added to the scheduler", exc)
            }
        } else {
            log.warn("The Listeners cannot be null and will not be added to the scheduler")
        }
    }

    @Async
    override fun scheduleJob(jobDetail: JobDetail?, trigger: Trigger?) {
        scheduler.scheduleJob(jobDetail, trigger)
    }

    override fun checkJobExists(jobKey: JobKey?): Boolean {
        return scheduler.checkExists(jobKey)
    }

    @Async
    override fun scheduleJobs(jobDetailTriggerMap: Map<JobDetail?, Set<Trigger>>?) {
        scheduler.scheduleJobs(jobDetailTriggerMap, true)
    }

    @Async
    override fun updateScheduledJob(
        jobKey: JobKey?, triggerKey: TriggerKey?, jobDataMap: JobDataMap, associatedObject: Any?
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

    @Async
    override fun deleteScheduledJobs(jobKeys: List<JobKey>) {
        // The scheduler.deleteJobs method does not unschedule jobs so using a deleteJob.
        jobKeys.forEach(Consumer { jobKey: JobKey? -> this.deleteScheduledJob(jobKey) })
    }

    @Async
    override fun deleteScheduledJob(jobKey: JobKey?) {
        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(SchedulerServiceImpl::class.java)
    }
}
