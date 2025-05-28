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
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.radarbase.appserver.jersey.exception.FcmMessageTransmitException
import org.radarbase.appserver.jersey.exception.MessageTransmitException
import org.radarbase.appserver.jersey.service.FcmDataMessageService
import org.radarbase.appserver.jersey.service.FcmNotificationService
import org.radarbase.appserver.jersey.service.transmitter.DataMessageTransmitter
import org.radarbase.appserver.jersey.service.transmitter.NotificationTransmitter
import org.slf4j.LoggerFactory

/**
 * A [Job] that sends notification/message to the device or email when executed.
 */
class MessageJob @Inject constructor(
    private val notificationTransmitters: List<NotificationTransmitter>,
    private val dataMessageTransmitters: List<DataMessageTransmitter>,
    private val notificationService: FcmNotificationService,
    private val dataMessageService: FcmDataMessageService
) : Job {
    /**
     * Called by the `[org.quartz.Scheduler]` when a `[org.quartz.Trigger]
    `*  fires that is associated with the `Job`.
     *
     *
     * The implementation may wish to set a [result][JobExecutionContext.setResult]
     * object on the [JobExecutionContext] before this method exits. The result itself is
     * meaningless to Quartz, but may be informative to `[org.quartz.JobListener]s`
     * or `[org.quartz.TriggerListener]s` that are watching the job's execution.
     *
     * @param context context containing jobs details and data added when creating the job.
     * @throws JobExecutionException if an error occurred while executing.
     */
    @Throws(JobExecutionException::class)
    override fun execute(context: JobExecutionContext) {
        val jobDataMap = context.jobDetail.jobDataMap
        val type = MessageType.valueOf(jobDataMap.getString("messageType"))
        val projectId = jobDataMap.getString("projectId")
        val subjectId = jobDataMap.getString("subjectId")
        val messageId = jobDataMap.getLong("messageId")
        val exceptions: MutableList<Exception> = mutableListOf()
        try {
            when (type) {
                MessageType.NOTIFICATION -> {
                    val notification = notificationService.getNotificationByProjectIdAndSubjectIdAndNotificationId(
                        projectId, subjectId, messageId
                    )
                    notificationTransmitters.forEach { transmitter ->
                        try {
                            transmitter.send(notification)
                        } catch (e: MessageTransmitException) {
                            exceptions.add(e)
                        }
                    }
                }

                MessageType.DATA -> {
                    val dataMessage = dataMessageService.getDataMessageByProjectIdAndSubjectIdAndDataMessageId(
                        projectId, subjectId, messageId
                    )
                    dataMessageTransmitters.forEach { transmitter ->
                        try {
                            transmitter.send(dataMessage)
                        } catch (e: MessageTransmitException) {
                            exceptions.add(e)
                        }
                    }
                }

                MessageType.UNKNOWN -> {
                    logger.debug("Not executing job with type MessageType.UNKNOWN")
                }
            }
        } catch (e: Exception) {
            logger.error("Could not transmit a message", e)
            throw JobExecutionException("Could not transmit a message", e)
        }

        /**
         * Exceptions that occurred while transmitting the message via the
         * transmitters. At present, only the FcmTransmitter affects the job execution state.
         */
        val fcmException: FcmMessageTransmitException? = exceptions.filterIsInstance<FcmMessageTransmitException>()
            .firstOrNull()
        if (fcmException != null) {
            throw JobExecutionException("Could not transmit a message", fcmException)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MessageJob::class.java)
    }
}
