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

/**
 * Generic Service for implementing an interface to the [Scheduler].
 *
 * @see SchedulerServiceImpl
 *
 * @author yatharthranjan
 */
interface SchedulerService {
    fun scheduleJob(jobDetail: JobDetail?, trigger: Trigger?)

    fun scheduleJobs(jobDetailTriggerMap: Map<JobDetail, Set<Trigger>>)

    fun checkJobExists(jobKey: JobKey?): Boolean

    fun updateScheduledJob(
        jobKey: JobKey?, triggerKey: TriggerKey?, jobDataMap: JobDataMap, associatedObject: Any?
    )

    fun deleteScheduledJobs(jobKeys: List<JobKey>)

    fun deleteScheduledJob(jobKey: JobKey?)
}
