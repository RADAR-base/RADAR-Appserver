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

package org.radarbase.appserver.service.scheduler.quartz;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

/**
 * Generic Service for implementing an interface to the {@link Scheduler}.
 *
 * @see SchedulerServiceImpl
 * @author yatharthranjan
 */
public interface SchedulerService {

  void scheduleJob(JobDetail jobDetail, Trigger trigger);

  void scheduleJobs(Map<JobDetail, Set<? extends Trigger>> jobDetailTriggerMap);

  boolean checkJobExists(JobKey jobKey);

  void updateScheduledJob(
      JobKey jobKey, TriggerKey triggerKey, JobDataMap jobDataMap, Object associatedObject);

  void deleteScheduledJobs(List<JobKey> jobKeys);

  void deleteScheduledJob(JobKey jobKey);
}
