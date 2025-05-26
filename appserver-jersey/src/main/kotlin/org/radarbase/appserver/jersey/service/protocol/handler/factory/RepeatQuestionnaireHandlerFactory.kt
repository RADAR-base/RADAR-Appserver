package org.radarbase.appserver.jersey.service.protocol.handler.factory

import org.radarbase.appserver.jersey.service.protocol.handler.ProtocolHandler
import org.radarbase.appserver.jersey.service.protocol.handler.impl.RandomRepeatQuestionnaireHandler
import org.radarbase.appserver.jersey.service.protocol.handler.impl.SimpleRepeatQuestionnaireHandler

object RepeatQuestionnaireHandlerFactory {
    fun getRepeatQuestionnaireHandler(questionnaireHandlerType: RepeatQuestionnaireHandlerType): ProtocolHandler {
        return when (questionnaireHandlerType) {
            RepeatQuestionnaireHandlerType.RANDOM -> RandomRepeatQuestionnaireHandler()
            else -> SimpleRepeatQuestionnaireHandler()
        }
    }
}
