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
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerListener;

public class QuartzNotificationJobListener implements JobListener {

  /**
   * <p>
   * Get the name of the <code>JobListener</code>.
   * </p>
   */
  @Override
  public String getName() {
    return getClass().getName();
  }

  /**
   * <p>
   * Called by the <code>{@link Scheduler}</code> when a <code>{@link JobDetail}</code> is about to
   * be executed (an associated <code>{@link Trigger}</code> has occurred).
   * </p>
   *
   * <p>
   * This method will not be invoked if the execution of the Job was vetoed by a <code>{@link
   * TriggerListener}</code>.
   * </p>
   *
   * @see #jobExecutionVetoed(JobExecutionContext)
   */
  @Override
  public void jobToBeExecuted(JobExecutionContext context) {

  }

  /**
   * <p>
   * Called by the <code>{@link Scheduler}</code> when a <code>{@link JobDetail}</code> was about to
   * be executed (an associated <code>{@link Trigger}</code> has occurred), but a <code>{@link
   * TriggerListener}</code> vetoed it's execution.
   * </p>
   *
   * @see #jobToBeExecuted(JobExecutionContext)
   */
  @Override
  public void jobExecutionVetoed(JobExecutionContext context) {

  }

  /**
   * <p>
   * Called by the <code>{@link Scheduler}</code> after a <code>{@link JobDetail}</code> has been
   * executed, and be for the associated <code>Trigger</code>'s
   * <code>triggered(xx)</code> method has been called.
   * </p>
   */
  @Override
  public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
    // update state to Executed
  }
}
