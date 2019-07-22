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

import static com.pivovarit.function.ThrowingPredicate.unchecked;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.triggers.SimpleTriggerImpl;
import org.radarbase.appserver.entity.Scheduled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * An implementation of the {@link SchedulerService} providing Synchronized access to schedule,
 * update and delete Jobs.
 *
 * @author yatharthranjan
 */
@Service
public class SchedulerServiceImpl implements SchedulerService {

  private transient Scheduler scheduler;

  public SchedulerServiceImpl(@Autowired Scheduler scheduler) {
    this.scheduler = scheduler;
  }

  @Async
  @SneakyThrows
  @Override
  public void scheduleJob(JobDetail jobDetail, Trigger trigger) {
    scheduler.scheduleJob(jobDetail, trigger);
  }

  @Async
  @SneakyThrows
  @Override
  public void scheduleJobs(Map<JobDetail, Set<? extends Trigger>> jobDetailTriggerMap) {
    scheduler.scheduleJobs(jobDetailTriggerMap, true);
  }

  @Async
  @SneakyThrows
  @Override
  public void updateScheduledJob(
      JobKey jobKey, TriggerKey triggerKey, JobDataMap jobDataMap, Object associatedObject) {

    if (!scheduler.checkExists(jobKey)) {
      throw new IllegalArgumentException("The Specified Job Key does not exist : " + jobKey);
    }

    if (!scheduler.checkExists(triggerKey)) {
      throw new IllegalArgumentException("The Specified Trigger Key does not exist :" + triggerKey);
    }

    JobDetail jobDetail = scheduler.getJobDetail(jobKey);
    jobDetail.getJobDataMap().putAll(jobDataMap.getWrappedMap());

    SimpleTriggerImpl trigger = (SimpleTriggerImpl) scheduler.getTrigger(triggerKey);
    trigger.getJobDataMap().putAll(jobDataMap.getWrappedMap());

    if (associatedObject instanceof Scheduled) {
      Scheduled scheduledObject = (Scheduled) associatedObject;
      trigger.setStartTime(new Date(scheduledObject.getScheduledTime().toEpochMilli()));
    }

    scheduler.addJob(jobDetail, true);

    scheduler.rescheduleJob(triggerKey, trigger);
  }

  @Async
  @SneakyThrows
  @Override
  public void deleteScheduledJobs(List<JobKey> jobKeys) {
    List<JobKey> jobKeysExist =
        jobKeys.stream().filter(unchecked(scheduler::checkExists)).collect(Collectors.toList());
    scheduler.deleteJobs(jobKeysExist);
  }

  @Async
  @SneakyThrows
  @Override
  public void deleteScheduledJob(JobKey jobKey) {
    if (scheduler.checkExists(jobKey)) {
      scheduler.deleteJob(jobKey);
    }
  }
}
