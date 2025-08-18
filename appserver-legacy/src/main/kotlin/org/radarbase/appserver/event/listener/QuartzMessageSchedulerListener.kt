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
package org.radarbase.appserver.event.listener

import org.quartz.JobDetail
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.SchedulerException
import org.quartz.SchedulerListener
import org.quartz.Trigger
import org.quartz.TriggerKey
import org.radarbase.appserver.event.state.MessageState
import org.radarbase.appserver.event.state.dto.DataMessageStateEventDto
import org.radarbase.appserver.event.state.dto.NotificationStateEventDto
import org.radarbase.appserver.repository.DataMessageRepository
import org.radarbase.appserver.repository.NotificationRepository
import org.radarbase.appserver.service.MessageType
import org.radarbase.appserver.service.scheduler.quartz.QuartzNamingStrategy
import org.radarbase.appserver.service.scheduler.quartz.SimpleQuartzNamingStrategy
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class QuartzMessageSchedulerListener(
    @Transient private val messageStateEventPublisher: ApplicationEventPublisher,
    @Transient private val notificationRepository: NotificationRepository,
    @Transient private val dataMessageRepository: DataMessageRepository,
    @Transient private val scheduler: Scheduler,
) : SchedulerListener {
    /**
     * Called by the `[Scheduler]` when a `[JobDetail]` is
     * scheduled.
     */
    override fun jobScheduled(trigger: Trigger) {
        val jobDetail: JobDetail
        try {
            jobDetail = scheduler.getJobDetail(trigger.jobKey)
        } catch (exc: SchedulerException) {
            log.warn(
                "Encountered error while getting job information from Trigger: ",
                exc,
            )
            return
        }

        val jobDataMap = jobDetail.jobDataMap
        val type = MessageType.valueOf(jobDataMap.getString("messageType"))
        val messageId = jobDataMap.getLongValue("messageId")

        when (type) {
            MessageType.NOTIFICATION -> {
                val notification = notificationRepository.findByIdOrNull(messageId)
                if (notification == null) {
                    log.warn("The notification does not exist in database and yet was scheduled.")
                    return
                }
                val notificationStateEvent =
                    NotificationStateEventDto(
                        this,
                        notification,
                        MessageState.SCHEDULED,
                        null,
                        Instant.now(),
                    )
                messageStateEventPublisher.publishEvent(notificationStateEvent)
            }

            MessageType.DATA -> {
                val dataMessage = dataMessageRepository.findByIdOrNull(messageId)
                if (dataMessage == null) {
                    log.warn("The data message does not exist in database and yet was scheduled.")
                    return
                }
                val dataMessageStateEvent = DataMessageStateEventDto(
                    this,
                    dataMessage,
                    MessageState.SCHEDULED,
                    null,
                    Instant.now(),
                )
                messageStateEventPublisher.publishEvent(dataMessageStateEvent)
            }

            else -> {}
        }
    }

    /**
     * Called by the `[Scheduler]` when a `[JobDetail]` is
     * unscheduled.
     *
     * @see SchedulerListener.schedulingDataCleared
     */
    override fun jobUnscheduled(triggerKey: TriggerKey) {
        val notificationId: Long
        try {
            notificationId = NAMING_STRATEGY.getMessageId(triggerKey.name)!!.toLong()
        } catch (_: NumberFormatException) {
            log.warn("The message id could not be established from unscheduled trigger.")
            return
        }
        val notification = notificationRepository.findByIdOrNull(notificationId)

        if (notification == null) {
            log.warn("The notification does not exist in database and yet was unscheduled.")
            return
        }
        val notificationStateEvent =
            NotificationStateEventDto(
                this,
                notification,
                MessageState.CANCELLED,
                null,
                Instant.now(),
            )
        messageStateEventPublisher.publishEvent(notificationStateEvent)
    }

    /**
     * Called by the `[Scheduler]` when a `[Trigger]` has reached
     * the condition in which it will never fire again.
     */
    override fun triggerFinalized(trigger: Trigger?) {
        // Not implemented
    }

    /**
     * Called by the `[Scheduler]` when a `[Trigger]` has been
     * paused.
     */
    override fun triggerPaused(triggerKey: TriggerKey?) {
        // Not implemented
    }

    /**
     * Called by the `[Scheduler]` when a group of `[Trigger]s` has
     * been paused.
     *
     *
     * If all groups were paused then triggerGroup will be null
     *
     * @param triggerGroup the paused group, or null if all were paused
     */
    override fun triggersPaused(triggerGroup: String?) {
        // Not implemented
    }

    /**
     * Called by the `[Scheduler]` when a `[Trigger]` has been
     * un-paused.
     */
    override fun triggerResumed(triggerKey: TriggerKey?) {
        // Not implemented
    }

    /**
     * Called by the `[Scheduler]` when a group of `[Trigger]s` has
     * been un-paused.
     */
    override fun triggersResumed(triggerGroup: String?) {
        // Not implemented
    }

    /**
     * Called by the `[Scheduler]` when a `[JobDetail]` has been
     * added.
     */
    override fun jobAdded(jobDetail: JobDetail?) {
        // Not implemented
    }

    /**
     * Called by the `[Scheduler]` when a `[JobDetail]` has been
     * deleted.
     */
    override fun jobDeleted(jobKey: JobKey?) {
        // Not implemented
    }

    /**
     * Called by the `[Scheduler]` when a `[JobDetail]` has been
     * paused.
     */
    override fun jobPaused(jobKey: JobKey?) {
        // Not implemented
    }

    /**
     * Called by the `[Scheduler]` when a group of `[JobDetail]s`
     * has been paused.
     *
     * @param jobGroup the paused group, or null if all were paused
     */
    override fun jobsPaused(jobGroup: String?) {
        // Not implemented
    }

    /**
     * Called by the `[Scheduler]` when a `[JobDetail]` has been
     * un-paused.
     */
    override fun jobResumed(jobKey: JobKey?) {
        // Not implemented
    }

    /**
     * Called by the `[Scheduler]` when a group of `[JobDetail]s`
     * has been un-paused.
     */
    override fun jobsResumed(jobGroup: String?) {
        // Not implemented
    }

    /**
     * Called by the `[Scheduler]` when a serious error has occurred within the
     * scheduler - such as repeated failures in the `JobStore`, or the inability to
     * instantiate a `Job` instance when its `Trigger` has fired.
     *
     *
     * The `getErrorCode()` method of the given SchedulerException can be used to
     * determine more specific information about the type of error that was encountered.
     */
    override fun schedulerError(msg: String?, cause: SchedulerException?) {
        // Not implemented
    }

    /**
     * Called by the `[Scheduler]` to inform the listener that it has move to standby
     * mode.
     */
    override fun schedulerInStandbyMode() {
        // Not implemented
    }

    /**
     * Called by the `[Scheduler]` to inform the listener that it has started.
     */
    override fun schedulerStarted() {
        // Not implemented
    }

    /**
     * Called by the `[Scheduler]` to inform the listener that it is starting.
     */
    override fun schedulerStarting() {
        // Not implemented
    }

    /**
     * Called by the `[Scheduler]` to inform the listener that it has shutdown.
     */
    override fun schedulerShutdown() {
        // Not implemented
    }

    /**
     * Called by the `[Scheduler]` to inform the listener that it has begun the
     * shutdown sequence.
     */
    override fun schedulerShuttingdown() {
        // Not implemented
    }

    /**
     * Called by the `[Scheduler]` to inform the listener that all jobs, triggers and
     * calendars were deleted.
     */
    override fun schedulingDataCleared() {
        // Not implemented
    }

    companion object {
        private val NAMING_STRATEGY: QuartzNamingStrategy = SimpleQuartzNamingStrategy()
        private val log: Logger = LoggerFactory.getLogger(QuartzMessageSchedulerListener::class.java)
    }
}
