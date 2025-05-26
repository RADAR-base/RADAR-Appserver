package org.radarbase.appserver.jersey.service.protocol.handler.factory

import org.radarbase.appserver.jersey.entity.Task
import org.radarbase.appserver.jersey.service.protocol.handler.ProtocolHandler
import org.radarbase.appserver.jersey.service.protocol.handler.impl.CompletedQuestionnaireHandler

object CompletedQuestionnaireHandlerFactory {
    fun getCompletedQuestionnaireHandler(prevTasks: List<Task>, prevTimezone: String): ProtocolHandler {
        return CompletedQuestionnaireHandler(prevTasks, prevTimezone)
    }
}
