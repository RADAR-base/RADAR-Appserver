package org.radarbase.appserver.microservices.cloud.messaging.service.quartz

import jakarta.inject.Inject
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.radarbase.appserver.microservices.cloud.messaging.service.transmitter.DataMessageTransmitter
import org.radarbase.appserver.microservices.cloud.messaging.service.transmitter.NotificationTransmitter
import org.radarbase.appserver.microservices.core.exception.FcmMessageTransmitException
import org.radarbase.appserver.microservices.core.exception.MessageTransmitException
import org.radarbase.appserver.microservices.core.service.FcmDataMessageService
import org.radarbase.appserver.microservices.core.service.FcmNotificationService
import org.radarbase.appserver.microservices.core.service.quartz.MessageType
import org.radarbase.jersey.service.AsyncCoroutineService
import org.slf4j.LoggerFactory

/**
 * A [org.quartz.Job] that sends notification/message to the device or email when executed.
 */
class MessageJob @Inject constructor(
    private val notificationTransmitter: NotificationTransmitter,
    private val dataMessageTransmitter: DataMessageTransmitter,
    private val notificationService: FcmNotificationService,
    private val dataMessageService: FcmDataMessageService,
    private val asyncService: AsyncCoroutineService,
) : Job {
    /**
     * Called by the `[org.quartz.Scheduler]` when a `[org.quartz.Trigger]
     `*  fires that is associated with the `Job`.
     *
     * The implementation may wish to set a [result][org.quartz.JobExecutionContext.setResult]
     * object on the [org.quartz.JobExecutionContext] before this method exits. The result itself is
     * meaningless to Quartz, but may be informative to `[org.quartz.JobListener]s`
     * or `[org.quartz.TriggerListener]s` that are watching the job's execution.
     *
     * @param context context containing jobs details and data added when creating the job.
     * @throws org.quartz.JobExecutionException if an error occurred while executing.
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
                    asyncService.runBlocking {
                        val notification =
                            notificationService.getNotificationByProjectIdAndSubjectIdAndNotificationId(
                                projectId,
                                subjectId,
                                messageId,
                            )

                        notificationTransmitter.let { transmitter ->
                            try {
                                transmitter.send(notification)
                            } catch (e: MessageTransmitException) {
                                exceptions.add(e)
                            }
                        }
                    }
                }

                MessageType.DATA -> {
                    asyncService.runBlocking {
                        val dataMessage = dataMessageService.getDataMessageByProjectIdAndSubjectIdAndDataMessageId(
                            projectId,
                            subjectId,
                            messageId,
                        )

                        dataMessageTransmitter.let { transmitter ->
                            try {
                                transmitter.send(dataMessage)
                            } catch (e: MessageTransmitException) {
                                exceptions.add(e)
                            }
                        }
                    }
                }

                MessageType.UNKNOWN -> {
                    logger.warn("Not executing job with type MessageType.UNKNOWN")
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
        val fcmException: FcmMessageTransmitException? =
            exceptions.filterIsInstance<FcmMessageTransmitException>().firstOrNull()
        if (fcmException != null) {
            throw JobExecutionException("Could not transmit a message", fcmException)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MessageJob::class.java)
    }
}
