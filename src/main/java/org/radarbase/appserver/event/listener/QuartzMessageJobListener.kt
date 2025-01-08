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

import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.quartz.JobListener
import org.radarbase.appserver.event.state.MessageState
import org.radarbase.appserver.event.state.dto.DataMessageStateEventDto
import org.radarbase.appserver.event.state.dto.NotificationStateEventDto
import org.radarbase.appserver.repository.DataMessageRepository
import org.radarbase.appserver.repository.NotificationRepository
import org.radarbase.appserver.service.MessageType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class QuartzMessageJobListener(
    @Transient private val messageStateEventPublisher: ApplicationEventPublisher,
    @Transient private val notificationRepository: NotificationRepository,
    @Transient private val dataMessageRepository: DataMessageRepository
) : JobListener {
    /**
     * Get the name of the `JobListener`.
     */
    override fun getName(): String {
        return javaClass.getName()
    }

    /**
     * Called by the `[Scheduler]` when a `[JobDetail]` is about to
     * be executed (an associated `[Trigger]` has occurred).
     *
     *
     * This method will not be invoked if the execution of the Job was vetoed by a `{
     * TriggerListener}`.
     *
     * @see .jobExecutionVetoed
     */
    override fun jobToBeExecuted(context: JobExecutionContext?) {
        // Not implemented
    }

    /**
     * Called by the `[Scheduler]` when a `[JobDetail]` was about to
     * be executed (an associated `[Trigger]` has occurred), but a `{
     * TriggerListener}` vetoed it's execution.
     *
     * @see .jobToBeExecuted
     */
    override fun jobExecutionVetoed(context: JobExecutionContext?) {
        // Not implemented
    }

    /**
     * Called by the `[Scheduler]` after a `[JobDetail]` has been
     * executed, and be for the associated `Trigger`'s `triggered(xx)` method
     * has been called.
     */
    override fun jobWasExecuted(context: JobExecutionContext, jobException: JobExecutionException?) {
        val jobDataMap = context.getMergedJobDataMap()
        val messageId = jobDataMap.getLongValue("messageId")
        val messageType = jobDataMap.getString("messageType")
        if (messageType == null) {
            QuartzMessageJobListener.log.warn("The message type does not exist.")
            return
        }

        val type = MessageType.valueOf(messageType)
        when (type) {
            MessageType.NOTIFICATION -> {
                val notification =
                    notificationRepository.findById(messageId)
                if (notification.isEmpty()) {
                    QuartzMessageJobListener.log.warn("The notification does not exist in database and yet was scheduled.")
                    return
                }
                if (jobException != null) {
                    val additionalInfo: MutableMap<String, String> = hashMapOf()
                    additionalInfo.put("error", jobException.message!!)
                    additionalInfo.put("error_description", jobException.toString())
                    val notificationStateEventError =
                        NotificationStateEventDto(
                            this, notification.get(), MessageState.ERRORED, additionalInfo, Instant.now()
                        )
                    messageStateEventPublisher.publishEvent(notificationStateEventError)

                    QuartzMessageJobListener.log.warn("The job could not be executed.", jobException)
                    return
                }

                val notificationStateEvent =
                    NotificationStateEventDto(
                        this, notification.get(), MessageState.EXECUTED, null, Instant.now()
                    )
                messageStateEventPublisher.publishEvent(notificationStateEvent)
            }

            MessageType.DATA -> {
                val dataMessage =
                    dataMessageRepository.findById(messageId)
                if (dataMessage.isEmpty()) {
                    log.warn("The data message does not exist in database and yet was scheduled.")
                    return
                }

                if (jobException != null) {
                    val additionalInfo: MutableMap<String, String> = hashMapOf()
                    additionalInfo.put("error", jobException.message!!)
                    additionalInfo.put("error_description", jobException.toString())
                    val dataMessageStateEventError =
                        DataMessageStateEventDto(
                            this, dataMessage.get(), MessageState.ERRORED, additionalInfo, Instant.now()
                        )
                    messageStateEventPublisher.publishEvent(dataMessageStateEventError)

                    log.warn("The job could not be executed.", jobException)
                    return
                }

                val dataMessageStateEvent =
                    DataMessageStateEventDto(
                        this, dataMessage.get(), MessageState.EXECUTED, null, Instant.now()
                    )
                messageStateEventPublisher.publishEvent(dataMessageStateEvent)
            }

            else -> log.warn("The message type does not exist.")
        }
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(QuartzMessageJobListener::class.java)
    }
}
