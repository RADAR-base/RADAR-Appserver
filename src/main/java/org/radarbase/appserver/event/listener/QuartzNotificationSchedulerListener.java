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

package org.radarbase.appserver.event.listener;

import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerListener;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

public class QuartzNotificationSchedulerListener implements SchedulerListener {

  /**
   * Called by the <code>{@link Scheduler}</code> when a <code>{@link JobDetail}</code> is
   * scheduled.
   */
  @Override
  public void jobScheduled(Trigger trigger) {
    //update state to scheduled
  }

  /**
   * Called by the <code>{@link Scheduler}</code> when a <code>{@link JobDetail}</code> is
   * unscheduled.
   *
   * @see SchedulerListener#schedulingDataCleared()
   */
  @Override
  public void jobUnscheduled(TriggerKey triggerKey) {
    // update state to cancelled
  }

  /**
   * Called by the <code>{@link Scheduler}</code> when a <code>{@link Trigger}</code> has reached
   * the condition in which it will never fire again.
   */
  @Override
  public void triggerFinalized(Trigger trigger) {}

  /**
   * Called by the <code>{@link Scheduler}</code> when a <code>{@link Trigger}</code> has been
   * paused.
   */
  @Override
  public void triggerPaused(TriggerKey triggerKey) {}

  /**
   * Called by the <code>{@link Scheduler}</code> when a group of <code>{@link Trigger}s</code> has
   * been paused.
   *
   * <p>If all groups were paused then triggerGroup will be null
   *
   * @param triggerGroup the paused group, or null if all were paused
   */
  @Override
  public void triggersPaused(String triggerGroup) {}

  /**
   * Called by the <code>{@link Scheduler}</code> when a <code>{@link Trigger}</code> has been
   * un-paused.
   */
  @Override
  public void triggerResumed(TriggerKey triggerKey) {}

  /**
   * Called by the <code>{@link Scheduler}</code> when a group of <code>{@link Trigger}s</code> has
   * been un-paused.
   */
  @Override
  public void triggersResumed(String triggerGroup) {}

  /**
   * Called by the <code>{@link Scheduler}</code> when a <code>{@link JobDetail}</code> has been
   * added.
   */
  @Override
  public void jobAdded(JobDetail jobDetail) {}

  /**
   * Called by the <code>{@link Scheduler}</code> when a <code>{@link JobDetail}</code> has been
   * deleted.
   */
  @Override
  public void jobDeleted(JobKey jobKey) {}

  /**
   * Called by the <code>{@link Scheduler}</code> when a <code>{@link JobDetail}</code> has been
   * paused.
   */
  @Override
  public void jobPaused(JobKey jobKey) {}

  /**
   * Called by the <code>{@link Scheduler}</code> when a group of <code>{@link JobDetail}s</code>
   * has been paused.
   *
   * @param jobGroup the paused group, or null if all were paused
   */
  @Override
  public void jobsPaused(String jobGroup) {}

  /**
   * Called by the <code>{@link Scheduler}</code> when a <code>{@link JobDetail}</code> has been
   * un-paused.
   */
  @Override
  public void jobResumed(JobKey jobKey) {}

  /**
   * Called by the <code>{@link Scheduler}</code> when a group of <code>{@link JobDetail}s</code>
   * has been un-paused.
   */
  @Override
  public void jobsResumed(String jobGroup) {}

  /**
   * Called by the <code>{@link Scheduler}</code> when a serious error has occurred within the
   * scheduler - such as repeated failures in the <code>JobStore</code>, or the inability to
   * instantiate a <code>Job</code> instance when its <code>Trigger</code> has fired.
   *
   * <p>The <code>getErrorCode()</code> method of the given SchedulerException can be used to
   * determine more specific information about the type of error that was encountered.
   */
  @Override
  public void schedulerError(String msg, SchedulerException cause) {}

  /**
   * Called by the <code>{@link Scheduler}</code> to inform the listener that it has move to standby
   * mode.
   */
  @Override
  public void schedulerInStandbyMode() {}

  /** Called by the <code>{@link Scheduler}</code> to inform the listener that it has started. */
  @Override
  public void schedulerStarted() {}

  /** Called by the <code>{@link Scheduler}</code> to inform the listener that it is starting. */
  @Override
  public void schedulerStarting() {}

  /** Called by the <code>{@link Scheduler}</code> to inform the listener that it has shutdown. */
  @Override
  public void schedulerShutdown() {}

  /**
   * Called by the <code>{@link Scheduler}</code> to inform the listener that it has begun the
   * shutdown sequence.
   */
  @Override
  public void schedulerShuttingdown() {}

  /**
   * Called by the <code>{@link Scheduler}</code> to inform the listener that all jobs, triggers and
   * calendars were deleted.
   */
  @Override
  public void schedulingDataCleared() {}
}
