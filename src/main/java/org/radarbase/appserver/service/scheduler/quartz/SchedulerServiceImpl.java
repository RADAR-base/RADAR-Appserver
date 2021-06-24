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

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerListener;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.triggers.SimpleTriggerImpl;
import org.radarbase.appserver.entity.Scheduled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * An implementation of the {@link SchedulerService} providing asynchronised access to schedule,
 * update and delete Jobs.
 *
 * @author yatharthranjan
 */
@Service
@Slf4j
public class SchedulerServiceImpl implements SchedulerService {

  private transient Scheduler scheduler;

  public SchedulerServiceImpl(Scheduler scheduler) {
    this.scheduler = scheduler;
  }

  @Autowired
  public SchedulerServiceImpl(
      Scheduler scheduler, JobListener jobListener, SchedulerListener schedulerListener) {
    this.scheduler = scheduler;
    if (jobListener != null && schedulerListener != null) {
      try {
        scheduler.getListenerManager().addJobListener(jobListener);
        scheduler.getListenerManager().addSchedulerListener(schedulerListener);
      } catch (SchedulerException exc) {
        log.warn("The Listeners could not be added to the scheduler", exc);
      }
    } else {
      log.warn("The Listeners cannot be null and will not be added to the scheduler");
    }
  }

  @Async
  @SneakyThrows
  @Override
  public void scheduleJob(JobDetail jobDetail, Trigger trigger) {
    scheduler.scheduleJob(jobDetail, trigger);
  }

  @SneakyThrows
  @Override
  public boolean checkJobExists(JobKey jobKey) {
    return scheduler.checkExists(jobKey);
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
    // The scheduler.deleteJobs method does not unschedule jobs so using a deleteJob.
    jobKeys.forEach(this::deleteScheduledJob);
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
